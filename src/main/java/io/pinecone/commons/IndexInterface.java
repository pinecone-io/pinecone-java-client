package io.pinecone.commons;

import com.google.protobuf.Struct;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.proto.*;
import io.pinecone.proto.FetchByMetadataRequest;
import io.pinecone.proto.FetchByMetadataResponse;
import io.pinecone.unsigned_indices_model.SparseValuesWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;

import java.util.ArrayList;
import java.util.List;

import static io.pinecone.utils.SparseIndicesConverter.convertUnsigned32IntToSigned32Int;

/**
 * The vector operations shared by {@link io.pinecone.clients.Index} and {@link io.pinecone.clients.AsyncIndex},
 * plus helper methods that validate input and build the gRPC requests they send.
 *
 * <p>Application code uses {@code Index} or {@code AsyncIndex} directly; the type parameters let the same method
 * return a plain response in {@code Index} and a {@code ListenableFuture} of it in {@code AsyncIndex}.
 *
 * @param <T> The return type for upsert operations.
 * @param <U> The return type for query operations.
 * @param <V> The return type for fetch operations.
 * @param <W> The return type for update operations.
 * @param <X> The return type for delete operations.
 * @param <Y> The return type for describing index stats.
 * @param <Z> The return type for listing vector IDs.
 * @param <A> The return type for listing namespaces.
 * @param <B> The return type for describing namespaces.
 */
public interface IndexInterface<T, U, V, W, X, Y, Z, A, B> extends AutoCloseable {

    /**
     * Checks one vector and builds the gRPC upsert request that carries it.
     *
     * <p>{@link io.pinecone.clients.Index} and {@link io.pinecone.clients.AsyncIndex} call this for each single-vector
     * {@code upsert}, so application code does not need to. The vector is checked as
     * {@link #buildUpsertVector buildUpsertVector} checks it.
     *
     * <pre>{@code
     * UpsertRequest request = index.validateUpsertRequest(
     *         "doc-1", Arrays.asList(0.12f, 0.48f, 0.91f), null, null, null, "articles-en");
     * }</pre>
     *
     * @param id the vector id, such as {@code "doc-1"}; must not be null or empty
     * @param values the dense values; must not be null, so pass an empty list for a sparse index
     * @param sparseIndices the sparse indices as unsigned 32-bit values, or null for no sparse values; set together
     *                      with {@code sparseValues}
     * @param sparseValues the values for {@code sparseIndices}, the same length as it, or null for no sparse values
     * @param metadata the metadata to store with the vector, or null for none
     * @param namespace the namespace to write to, such as {@code "articles-en"}; null or {@code ""} means the default
     *                  namespace
     * @return the request, holding one vector
     * @throws PineconeValidationException if the id is null or empty, only one of the sparse lists is set, the sparse
     *         lists differ in length, or a sparse index is outside the unsigned 32-bit range
     */
    default UpsertRequest validateUpsertRequest(String id,
                                                List<Float> values,
                                                List<Long> sparseIndices,
                                                List<Float> sparseValues,
                                                Struct metadata,
                                                String namespace) {
        UpsertRequest.Builder upsertRequest = UpsertRequest.newBuilder();

        Vector vector = buildUpsertVector(id, values, sparseIndices, sparseValues, metadata);

        upsertRequest.addVectors(vector);

        if (namespace != null) {
            upsertRequest.setNamespace(namespace);
        }
        return upsertRequest.build();
    }

    /**
     * Checks a batch of vectors and builds the gRPC upsert request that carries them.
     *
     * <p>{@link io.pinecone.clients.Index} and {@link io.pinecone.clients.AsyncIndex} call this for
     * {@code upsert(List, String)}, so application code does not need to. Each vector is checked as
     * {@link #buildUpsertVector buildUpsertVector} checks it.
     *
     * <pre>{@code
     * UpsertRequest request = index.validateUpsertRequest(Arrays.asList(
     *         IndexInterface.buildUpsertVectorWithUnsignedIndices("doc-1", Arrays.asList(0.12f, 0.48f, 0.91f), null, null, null),
     *         IndexInterface.buildUpsertVectorWithUnsignedIndices("doc-2", Arrays.asList(0.33f, 0.05f, 0.72f), null, null, null)),
     *         "articles-en");
     * }</pre>
     *
     * @param vectorWithUnsignedIndicesList the vectors to write; must not be null
     * @param namespace the namespace to write to, such as {@code "articles-en"}; {@code ""} means the default
     *                  namespace. Must not be null.
     * @return the request, holding every vector in the list
     * @throws PineconeValidationException if a vector has no id, has only one of the sparse lists, has sparse lists
     *         of different lengths, or has a sparse index outside the unsigned 32-bit range
     */
    default UpsertRequest validateUpsertRequest(List<VectorWithUnsignedIndices> vectorWithUnsignedIndicesList,
                                                String namespace) {
        List<Vector> vectors = new ArrayList<>(vectorWithUnsignedIndicesList.size());

        for (VectorWithUnsignedIndices vectorWithUnsignedIndices : vectorWithUnsignedIndicesList) {
            SparseValuesWithUnsignedIndices sparseValuesWithUnsignedIndices = vectorWithUnsignedIndices.getSparseValuesWithUnsignedIndices();

                Vector vector = buildUpsertVector(vectorWithUnsignedIndices.getId(),
                        vectorWithUnsignedIndices.getValuesList(),
                        (sparseValuesWithUnsignedIndices != null) ? sparseValuesWithUnsignedIndices.getIndicesWithUnsigned32IntList() : null,
                        (sparseValuesWithUnsignedIndices != null) ? sparseValuesWithUnsignedIndices.getValuesList() : null,
                        vectorWithUnsignedIndices.getMetadata());
                vectors.add(vector);

        }

        UpsertRequest.Builder upsertRequest = UpsertRequest.newBuilder().addAllVectors(vectors);
        if (namespace != null) {
            upsertRequest.setNamespace(namespace);
        }
        return upsertRequest.build();
    }

    /**
     * Checks one vector's fields and builds the gRPC {@link Vector} that an upsert request carries.
     *
     * <p>{@code Index} and {@code AsyncIndex} call this for every vector they upsert, so application code rarely needs
     * it: to write vectors, pass them to an {@code upsert} overload, or build a batch with
     * {@link #buildUpsertVectorWithUnsignedIndices buildUpsertVectorWithUnsignedIndices}.
     *
     * <pre>{@code
     * Vector vector = index.buildUpsertVector(
     *         "doc-1", Arrays.asList(0.12f, 0.48f, 0.91f), Arrays.asList(10L, 45L), Arrays.asList(0.5f, 0.3f), null);
     * }</pre>
     *
     * @param id the vector id, such as {@code "doc-1"}; must not be null or empty
     * @param values the dense values, whose length must match the index dimension; must not be null, so pass an empty
     *               list for a sparse index
     * @param sparseIndices the sparse indices as unsigned 32-bit values, or null for no sparse values; set together
     *                      with {@code sparseValues}
     * @param sparseValues the values for {@code sparseIndices}, the same length as it, or null for no sparse values
     * @param metadata the metadata to store with the vector, or null for none
     * @return the vector, with sparse indices converted to the signed form the gRPC message uses
     * @throws PineconeValidationException if the id is null or empty, only one of the sparse lists is set, the sparse
     *         lists differ in length, or a sparse index is outside the unsigned 32-bit range
     */
    default Vector buildUpsertVector(String id,
                                     List<Float> values,
                                     List<Long> sparseIndices,
                                     List<Float> sparseValues,
                                     Struct metadata) {
         if (id == null || id.isEmpty()) {
            throw new PineconeValidationException("Invalid upsert request. Please ensure that id is provided.");
        }

        Vector.Builder vectorBuilder = Vector.newBuilder()
                .setId(id)
                .addAllValues(values);

        if ((sparseIndices != null && sparseValues == null) || (sparseIndices == null && sparseValues != null)) {
            throw new PineconeValidationException("Invalid upsert request. Please ensure that both sparse indices and" +
                    " values are present.");
        }

        if (sparseIndices != null) {
            if (sparseIndices.size() != sparseValues.size()) {
                throw new PineconeValidationException("Invalid upsert request. Please ensure that both sparse indices" +
                        " and values are of the same length.");
            }

            vectorBuilder.setSparseValues(SparseValues.newBuilder()
                    .addAllIndices(convertUnsigned32IntToSigned32Int(sparseIndices))
                    .addAllValues(sparseValues)
                    .build());
        }

        if (metadata != null) {
            vectorBuilder.setMetadata(metadata);
        }

        return vectorBuilder.build();
    }

    /**
     * Builds a {@link VectorWithUnsignedIndices} to pass to {@code upsert(List, String)} when writing a batch.
     *
     * <p>The dense values must not be empty, so this cannot build a vector for a sparse index; for that, construct a
     * {@code VectorWithUnsignedIndices} with an empty values list and a {@code SparseValuesWithUnsignedIndices}. Sparse
     * indices are checked against the unsigned 32-bit range when the batch is upserted, not here.
     *
     * <pre>{@code
     * Struct metadata = Struct.newBuilder()
     *         .putFields("category", Value.newBuilder().setStringValue("news").build())
     *         .build();
     * VectorWithUnsignedIndices vector = IndexInterface.buildUpsertVectorWithUnsignedIndices(
     *         "doc-1", Arrays.asList(0.12f, 0.48f, 0.91f), null, null, metadata);
     *
     * index.upsert(Collections.singletonList(vector), "articles-en");
     * }</pre>
     *
     * @param id the vector id, such as {@code "doc-1"}; must not be null or empty
     * @param values the dense values, whose length must match the index dimension; must not be null or empty
     * @param sparseIndices the sparse indices as unsigned 32-bit values, or null for no sparse values; set together
     *                      with {@code sparseValues}
     * @param sparseValues the values for {@code sparseIndices}, the same length as it, or null for no sparse values
     * @param metadata the metadata to store with the vector, or null for none
     * @return the vector, ready to add to the list passed to {@code upsert}
     * @throws PineconeValidationException if the id or values are null or empty, only one of the sparse lists is set,
     *         or the sparse lists differ in length
     */
    static VectorWithUnsignedIndices buildUpsertVectorWithUnsignedIndices(String id,
                                                                          List<Float> values,
                                                                          List<Long> sparseIndices,
                                                                          List<Float> sparseValues,
                                                                          Struct metadata) {
        if (id == null || id.isEmpty() || values == null || values.isEmpty()) {
            throw new PineconeValidationException("Invalid upsert request. Please ensure that both id and values are " +
                    "provided.");
        }

        VectorWithUnsignedIndices vectorWithUnsignedIndices = new VectorWithUnsignedIndices(id, values);

        if ((sparseIndices != null && sparseValues == null) || (sparseIndices == null && sparseValues != null)) {
            throw new PineconeValidationException("Invalid upsert request. Please ensure that both sparse indices and" +
                    " values are present.");
        }

        if (sparseIndices != null) {
            if (sparseIndices.size() != sparseValues.size()) {
                throw new PineconeValidationException("Invalid upsert request. Please ensure that both sparse indices" +
                        " and values are of the same length.");
            }

            SparseValuesWithUnsignedIndices sparseValuesWithUnsignedIndices = new SparseValuesWithUnsignedIndices();
            sparseValuesWithUnsignedIndices.setIndicesWithUnsigned32Int(sparseIndices);
            sparseValuesWithUnsignedIndices.setValues(sparseValues);
            vectorWithUnsignedIndices.setSparseValuesWithUnsignedIndices(sparseValuesWithUnsignedIndices);
        }

        if (metadata != null) {
            vectorWithUnsignedIndices.setMetadata(metadata);
        }

        return vectorWithUnsignedIndices;
    }

    /**
     * Checks the arguments of a query and builds the gRPC query request.
     *
     * <p>{@link io.pinecone.clients.Index} and {@link io.pinecone.clients.AsyncIndex} call this for each
     * {@code query}, {@code queryByVector} and {@code queryByVectorId} call, so application code does not need to.
     * Pass either a query vector or the id of a stored vector, not both.
     *
     * <pre>{@code
     * QueryRequest request = index.validateQueryRequest(
     *         10, Arrays.asList(0.12f, 0.48f, 0.91f), null, null, null, "articles-en", null, false, true);
     * }</pre>
     *
     * @param topK the number of matches to return, such as 10
     * @param vector the dense query vector, or null or empty when querying by {@code id} or with sparse values only
     * @param sparseIndices the sparse query indices as unsigned 32-bit values, or null for none; set together with
     *                      {@code sparseValues}
     * @param sparseValues the values for {@code sparseIndices}, the same length as it, or null for none
     * @param id the id of a stored vector to use as the query, such as {@code "doc-1"}, or null or empty when
     *           querying with a vector
     * @param namespace the namespace to search, such as {@code "articles-en"}; null or {@code ""} means the default
     *                  namespace
     * @param filter a metadata filter such as {@code {"category": {"$eq": "news"}}} built as a {@code Struct}, or
     *               null for no filter
     * @param includeValues whether matches include their vector values
     * @param includeMetadata whether matches include their metadata
     * @return the request
     * @throws PineconeValidationException if both {@code vector} and {@code id} are set, only one of the sparse lists
     *         is set, the sparse lists differ in length, or a sparse index is outside the unsigned 32-bit range
     */
    default QueryRequest validateQueryRequest(int topK,
                                              List<Float> vector,
                                              List<Long> sparseIndices,
                                              List<Float> sparseValues,
                                              String id,
                                              String namespace,
                                              Struct filter,
                                              boolean includeValues,
                                              boolean includeMetadata) {
        QueryRequest.Builder queryRequest = QueryRequest.newBuilder();

        if (id != null && !id.isEmpty() && vector != null && !vector.isEmpty()) {
            throw new PineconeValidationException("Invalid query request. Cannot query with both vector id and vector" +
                    " values.");
        }

        if (id != null && !id.isEmpty()) {
            queryRequest.setId(id);
        }

        if (namespace != null) {
            queryRequest.setNamespace(namespace);
        }

        queryRequest.setTopK(topK)
                .setIncludeValues(includeValues)
                .setIncludeMetadata(includeMetadata);

        if (filter != null) {
            queryRequest.setFilter(filter);
        }

        if (vector != null && !vector.isEmpty()) {
            queryRequest.addAllVector(vector);
        }

        if ((sparseIndices != null && sparseValues == null) || (sparseIndices == null && sparseValues != null)) {
            throw new PineconeValidationException("Invalid upsert request. Please ensure that both sparse indices and" +
                    " values are present.");
        }

        if (sparseIndices != null) {
            if (sparseIndices.size() != sparseValues.size()) {
                throw new PineconeValidationException("Invalid upsert request. Please ensure that both sparse indices" +
                        " and values are of the same length.");
            }
            queryRequest.setSparseVector(SparseValues.newBuilder()
                    .addAllIndices(convertUnsigned32IntToSigned32Int(sparseIndices))
                    .addAllValues(sparseValues)
                    .build());
        }
        return queryRequest.build();
    }

    /**
     * Checks the arguments of a fetch by id and builds the gRPC fetch request.
     *
     * <p>{@link io.pinecone.clients.Index} and {@link io.pinecone.clients.AsyncIndex} call this for each
     * {@code fetch}, so application code does not need to.
     *
     * <pre>{@code
     * FetchRequest request = index.validateFetchRequest(Arrays.asList("doc-1", "doc-2"), "articles-en");
     * }</pre>
     *
     * @param ids the ids to fetch, such as {@code "doc-1"}; must not be null or empty
     * @param namespace the namespace to fetch from, such as {@code "articles-en"}; null or {@code ""} means the
     *                  default namespace
     * @return the request
     * @throws PineconeValidationException if {@code ids} is null or empty
     */
    default FetchRequest validateFetchRequest(List<String> ids, String namespace) {
        FetchRequest.Builder fetchRequest = FetchRequest.newBuilder();

        if (ids == null || ids.isEmpty()) {
            throw new PineconeValidationException("Invalid fetch request. Vector ids must be present");
        }

        fetchRequest.addAllIds(ids);

        if (namespace != null) {
            fetchRequest.setNamespace(namespace);
        }
        return fetchRequest.build();
    }

    /**
     * Checks the arguments of a fetch by metadata and builds the gRPC request.
     *
     * <p>{@link io.pinecone.clients.Index} and {@link io.pinecone.clients.AsyncIndex} call this for each
     * {@code fetchByMetadata}, so application code does not need to. Unlike most operations, the namespace is
     * required: pass {@code "__default__"} for the default namespace.
     *
     * <pre>{@code
     * Struct filter = Struct.newBuilder()
     *         .putFields("category", Value.newBuilder()
     *                 .setStructValue(Struct.newBuilder()
     *                         .putFields("$eq", Value.newBuilder().setStringValue("news").build()))
     *                 .build())
     *         .build();
     * FetchByMetadataRequest request = index.validateFetchByMetadataRequest("articles-en", filter, 50, null);
     * }</pre>
     *
     * @param namespace the namespace to fetch from, such as {@code "articles-en"}; must not be null or empty
     * @param filter a metadata filter such as {@code {"category": {"$eq": "news"}}} built as a {@code Struct}; the
     *               client accepts null, but the server rejects a request without a filter
     * @param limit the most vectors to return in one page, such as 50; null, zero or a negative value means the
     *              server's default
     * @param paginationToken the token from the previous page's {@code getPagination().getNext()}, or null or empty
     *                        for the first page
     * @return the request
     * @throws PineconeValidationException if {@code namespace} is null or empty
     */
    default FetchByMetadataRequest validateFetchByMetadataRequest(String namespace,
                                                                  Struct filter,
                                                                  Integer limit,
                                                                  String paginationToken) {
        FetchByMetadataRequest.Builder fetchByMetadataRequest = FetchByMetadataRequest.newBuilder();

        if (namespace == null || namespace.isEmpty()) {
            throw new PineconeValidationException("Invalid fetch by metadata request. Namespace must be present");
        }
        fetchByMetadataRequest.setNamespace(namespace);

        if (filter != null) {
            fetchByMetadataRequest.setFilter(filter);
        }

        if (limit != null && limit > 0) {
            fetchByMetadataRequest.setLimit(limit);
        }

        if (paginationToken != null && !paginationToken.isEmpty()) {
            fetchByMetadataRequest.setPaginationToken(paginationToken);
        }

        return fetchByMetadataRequest.build();
    }

    /**
     * Checks the arguments of an update by id and builds the gRPC update request.
     *
     * <p>{@link io.pinecone.clients.Index} and {@link io.pinecone.clients.AsyncIndex} call this for each
     * {@code update}, so application code does not need to. Metadata passed here is merged into the vector's
     * existing metadata, not swapped in for it.
     *
     * <pre>{@code
     * UpdateRequest request = index.validateUpdateRequest(
     *         "doc-1", Arrays.asList(0.12f, 0.48f, 0.91f), null, "articles-en", null, null);
     * }</pre>
     *
     * @param id the id of the vector to update, such as {@code "doc-1"}; must not be null
     * @param values the new dense values, or null to keep the current ones
     * @param metadata the metadata fields to set, or null to keep the current metadata
     * @param namespace the namespace that holds the vector, such as {@code "articles-en"}; null or {@code ""} means
     *                  the default namespace
     * @param sparseIndices the new sparse indices as unsigned 32-bit values, or null to keep the current sparse
     *                      values; set together with {@code sparseValues}
     * @param sparseValues the values for {@code sparseIndices}, the same length as it, or null
     * @return the request
     * @throws PineconeValidationException if the id is null, only one of the sparse lists is set, the sparse lists
     *         differ in length, or a sparse index is outside the unsigned 32-bit range
     */
    default UpdateRequest validateUpdateRequest(String id,
                                                List<Float> values,
                                                Struct metadata,
                                                String namespace,
                                                List<Long> sparseIndices,
                                                List<Float> sparseValues) {
        UpdateRequest.Builder updateRequest = UpdateRequest.newBuilder();

        if (id == null) {
            throw new PineconeValidationException("Invalid update request. Vector id must be present");
        }
        updateRequest.setId(id);

        if (values != null) {
            updateRequest.addAllValues(values);
        }

        if (metadata != null) {
            updateRequest.setSetMetadata(metadata);
        }

        if (namespace != null) {
            updateRequest.setNamespace(namespace);
        }

        if ((sparseIndices != null && sparseValues == null) || (sparseIndices == null && sparseValues != null)) {
            throw new PineconeValidationException("Invalid upsert request. Please ensure that both sparse indices and" +
                    " values are present.");
        }

        if (sparseIndices != null) {
            if (sparseIndices.size() != sparseValues.size()) {
                throw new PineconeValidationException("Invalid upsert request. Please ensure that both sparse indices" +
                        " and values are of the same length.");
            }
            updateRequest.setSparseValues(SparseValues.newBuilder()
                    .addAllIndices(convertUnsigned32IntToSigned32Int(sparseIndices))
                    .addAllValues(sparseValues)
                    .build());
        }
        return updateRequest.build();
    }

    /**
     * Checks the arguments of an update by metadata filter and builds the gRPC update request.
     *
     * <p>{@link io.pinecone.clients.Index} and {@link io.pinecone.clients.AsyncIndex} call this for each
     * {@code updateByMetadata}, so application code does not need to. The metadata is merged into every matching
     * vector's existing metadata.
     *
     * <pre>{@code
     * Struct filter = Struct.newBuilder()
     *         .putFields("category", Value.newBuilder()
     *                 .setStructValue(Struct.newBuilder()
     *                         .putFields("$eq", Value.newBuilder().setStringValue("news").build()))
     *                 .build())
     *         .build();
     * Struct metadata = Struct.newBuilder()
     *         .putFields("archived", Value.newBuilder().setBoolValue(true).build())
     *         .build();
     * UpdateRequest request = index.validateUpdateByMetadataRequest(filter, metadata, "articles-en", true);
     * }</pre>
     *
     * @param filter a metadata filter such as {@code {"category": {"$eq": "news"}}} built as a {@code Struct} that
     *               selects the vectors to update; must not be null
     * @param metadata the metadata fields to set on each matching vector, or null for none
     * @param namespace the namespace to update, such as {@code "articles-en"}; null or {@code ""} means the default
     *                  namespace
     * @param dryRun true to only count the matching vectors without changing them; null means false
     * @return the request
     * @throws PineconeValidationException if {@code filter} is null
     */
    default UpdateRequest validateUpdateByMetadataRequest(Struct filter,
                                                          Struct metadata,
                                                          String namespace,
                                                          Boolean dryRun) {
        UpdateRequest.Builder updateRequest = UpdateRequest.newBuilder();

        if (filter == null) {
            throw new PineconeValidationException("Invalid update by metadata request. Filter must be present");
        }
        updateRequest.setFilter(filter);

        if (metadata != null) {
            updateRequest.setSetMetadata(metadata);
        }

        if (namespace != null) {
            updateRequest.setNamespace(namespace);
        }

        if (dryRun != null) {
            updateRequest.setDryRun(dryRun);
        }

        return updateRequest.build();
    }

    /**
     * Builds the gRPC delete request from ids, a delete-all flag or a metadata filter.
     *
     * <p>{@link io.pinecone.clients.Index} and {@link io.pinecone.clients.AsyncIndex} call this for each delete
     * method, so application code does not need to. Despite its name, it does not check the arguments: it copies
     * whichever are set into the request, so set exactly one of {@code ids}, {@code deleteAll} or {@code filter}.
     *
     * <pre>{@code
     * DeleteRequest request = index.validateDeleteRequest(Arrays.asList("doc-1", "doc-2"), false, "articles-en", null);
     * }</pre>
     *
     * @param ids the ids to delete, such as {@code "doc-1"}, or null or empty when deleting by flag or filter
     * @param deleteAll true to delete every vector in the namespace, which can't be undone
     * @param namespace the namespace to delete from, such as {@code "articles-en"}; null or {@code ""} means the
     *                  default namespace
     * @param filter a metadata filter such as {@code {"category": {"$eq": "news"}}} built as a {@code Struct}, or
     *               null when deleting by ids or flag
     * @return the request
     */
    default DeleteRequest validateDeleteRequest(List<String> ids,
                                                boolean deleteAll,
                                                String namespace,
                                                Struct filter) {
        DeleteRequest.Builder deleteRequest = DeleteRequest.newBuilder().setDeleteAll(deleteAll);

        if (ids != null && !ids.isEmpty()) {
            deleteRequest.addAllIds(ids);
        }

        if (namespace != null) {
            deleteRequest.setNamespace(namespace);
        }

        if (filter != null) {
            deleteRequest.setFilter(filter);
        }
        return deleteRequest.build();
    }

    /**
     * Builds the gRPC request for index statistics, with an optional metadata filter.
     *
     * <p>{@link io.pinecone.clients.Index} and {@link io.pinecone.clients.AsyncIndex} call this for each
     * {@code describeIndexStats}, so application code does not need to. It does not check the filter; serverless
     * indexes reject a non-empty one.
     *
     * <pre>{@code
     * DescribeIndexStatsRequest request = index.validateDescribeIndexStatsRequest(null);
     * }</pre>
     *
     * @param filter a metadata filter such as {@code {"category": {"$eq": "news"}}} built as a {@code Struct}, or
     *               null for statistics over every vector
     * @return the request
     */
    default DescribeIndexStatsRequest validateDescribeIndexStatsRequest(Struct filter) {
        DescribeIndexStatsRequest.Builder describeIndexStatsRequest = DescribeIndexStatsRequest.newBuilder();

        if (filter != null) {
            describeIndexStatsRequest.setFilter(filter);
        }
        return describeIndexStatsRequest.build();
    }

    /**
     * Writes a batch of vectors to a namespace, replacing any existing vector with the same id.
     *
     * <p>Use this overload to load data: one request carries many vectors. The server rejects a request that is too
     * large, so split big loads into several calls. To write a single vector, the other {@code upsert} overloads are
     * shorter. A namespace that does not exist yet is created. Writes are not visible instantly: a query or stats call
     * made right after an upsert may not reflect it yet.
     *
     * @param vectorList the vectors to write, for example built with
     *                   {@link IndexInterface#buildUpsertVectorWithUnsignedIndices}; each needs an id and dense values
     *                   whose length matches the index dimension (an empty list, not null, for a sparse index)
     * @param namespace the namespace to write to, such as {@code "articles-en"}; {@code ""} writes to the default
     *                  namespace. Must not be null.
     * @return the server's response, including the number of vectors written
     * @throws PineconeValidationException if a vector has no id, has sparse indices without sparse values or the
     *         reverse, has sparse lists of different lengths, or has a sparse index outside the unsigned 32-bit range;
     *         {@code AsyncIndex} throws it from the call, not through the future
     */
    T upsert(List<VectorWithUnsignedIndices> vectorList, String namespace);

    /**
     * Writes one dense vector to the default namespace, replacing any existing vector with the same id.
     *
     * <p>To write to a named namespace, use {@link #upsert(String, List, String)}; to write many vectors, use
     * {@link #upsert(List, String)}. A query made right after an upsert may not reflect it yet.
     *
     * @param id the vector id, such as {@code "doc-1"}; must not be null or empty
     * @param values the dense values; the length must match the index dimension. Must not be null.
     * @return the server's response, including the number of vectors written
     * @throws PineconeValidationException if {@code id} is null or empty; {@code AsyncIndex} throws it from the
     *         call, not through the future
     */
    T upsert(String id, List<Float> values);

    /**
     * Writes one dense vector to a namespace, replacing any existing vector with the same id.
     *
     * <p>A namespace that does not exist yet is created. To add sparse values or metadata, use
     * {@link #upsert(String, List, List, List, Struct, String)}; to write many vectors, use
     * {@link #upsert(List, String)}. A query made right after an upsert may not reflect it yet.
     *
     * @param id the vector id, such as {@code "doc-1"}; must not be null or empty
     * @param values the dense values; the length must match the index dimension. Must not be null.
     * @param namespace the namespace to write to, such as {@code "articles-en"}; null or {@code ""} writes to the
     *                  default namespace
     * @return the server's response, including the number of vectors written
     * @throws PineconeValidationException if {@code id} is null or empty; {@code AsyncIndex} throws it from the
     *         call, not through the future
     */
    T upsert(String id, List<Float> values, String namespace);

    /**
     * Writes one vector, with optional sparse values and metadata, to a namespace, replacing any existing vector
     * with the same id.
     *
     * <p>Use this overload when the vector has sparse values or metadata to filter on; pass null for the parts it
     * does not have. A namespace that does not exist yet is created. To write many vectors, use
     * {@link #upsert(List, String)}. A query made right after an upsert may not reflect it yet.
     *
     * @param id the vector id, such as {@code "doc-1"}; must not be null or empty
     * @param values the dense values; the length must match the index dimension. For a sparse index, pass an empty
     *               list. Must not be null.
     * @param sparseIndices the positions of the sparse values as unsigned 32-bit integers, such as
     *                      {@code Arrays.asList(10L, 45L)}; null if the vector has no sparse values
     * @param sparseValues the sparse values, one per entry in {@code sparseIndices}; null if the vector has no sparse
     *                     values
     * @param metadata fields to store with the vector and filter on in queries, such as a category or a year; null
     *                 for none
     * @param namespace the namespace to write to, such as {@code "articles-en"}; null or {@code ""} writes to the
     *                  default namespace
     * @return the server's response, including the number of vectors written
     * @throws PineconeValidationException if {@code id} is null or empty, only one of {@code sparseIndices} and
     *         {@code sparseValues} is given, they differ in length, or a sparse index is outside the unsigned 32-bit
     *         range;
     *         {@code AsyncIndex} throws it from the call, not through the future
     */
    T upsert(String id, List<Float> values, List<Long> sparseIndices, List<Float> sparseValues, Struct metadata,
             String namespace);

    /**
     * Searches the default namespace for the vectors most similar to a vector already stored in it and returns their
     * ids and scores.
     *
     * <p>If no vector has the id, the response has no matches. To search a named namespace, filter on metadata, or get
     * each match's metadata, use {@link #queryByVectorId(int, String, String, Struct, boolean, boolean)}.
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param id the id of a stored vector to search from, such as {@code "doc-1"}; if no vector has this id,
     *           the response has no matches
     * @return the matches; {@code getMatchesList()} holds up to {@code topK} matches, each with {@code getId()}
     *         and {@code getScore()}
     */
    U queryByVectorId(int topK, String id);

    /**
     * Searches the default namespace for the vectors most similar to a vector already stored in it.
     *
     * <p>If no vector has the id, the response has no matches. To search a named namespace or filter on metadata, use
     * {@link #queryByVectorId(int, String, String, Struct, boolean, boolean)}.
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param id the id of a stored vector to search from, such as {@code "doc-1"}; if no vector has this id,
     *           the response has no matches
     * @param includeValues true to return each match's vector values; false to leave them out
     * @param includeMetadata true to return each match's metadata; false to leave it out
     * @return the matches; {@code getMatchesList()} holds up to {@code topK} matches, each with {@code getId()},
     *         {@code getScore()} and, when requested, {@code getValuesList()} and {@code getMetadata()}
     */
    U queryByVectorId(int topK, String id, boolean includeValues, boolean includeMetadata);

    /**
     * Searches a namespace for the vectors most similar to a vector already stored in it and returns their ids and
     * scores.
     *
     * <p>If no vector has the id, the response has no matches. To filter on metadata or get each match's metadata or
     * values, use {@link #queryByVectorId(int, String, String, Struct, boolean, boolean)}.
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param id the id of a stored vector to search from, such as {@code "doc-1"}; if no vector has this id,
     *           the response has no matches
     * @param namespace the namespace to search, such as {@code "articles-en"}; null or {@code ""} searches the
     *                  default namespace
     * @return the matches; {@code getMatchesList()} holds up to {@code topK} matches, each with {@code getId()}
     *         and {@code getScore()}
     */
    U queryByVectorId(int topK, String id, String namespace);

    /**
     * Searches a namespace for the vectors most similar to a vector already stored in it.
     *
     * <p>If no vector has the id, the response has no matches. To filter on metadata, use
     * {@link #queryByVectorId(int, String, String, Struct, boolean, boolean)}.
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param id the id of a stored vector to search from, such as {@code "doc-1"}; if no vector has this id,
     *           the response has no matches
     * @param namespace the namespace to search, such as {@code "articles-en"}; null or {@code ""} searches the
     *                  default namespace
     * @param includeValues true to return each match's vector values; false to leave them out
     * @param includeMetadata true to return each match's metadata; false to leave it out
     * @return the matches; {@code getMatchesList()} holds up to {@code topK} matches, each with {@code getId()},
     *         {@code getScore()} and, when requested, {@code getValuesList()} and {@code getMetadata()}
     */
    U queryByVectorId(int topK, String id, String namespace, boolean includeValues, boolean includeMetadata);

    /**
     * Searches a namespace for the vectors most similar to a vector already stored in it, keeping only those whose
     * metadata matches a filter, and returns their ids and scores.
     *
     * <p>If no vector has the id, the response has no matches. To also get each match's metadata or values, use
     * {@link #queryByVectorId(int, String, String, Struct, boolean, boolean)}.
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param id the id of a stored vector to search from, such as {@code "doc-1"}; if no vector has this id,
     *           the response has no matches
     * @param namespace the namespace to search, such as {@code "articles-en"}; null or {@code ""} searches the
     *                  default namespace
     * @param filter a metadata filter in Pinecone's filter syntax, such as {@code {"category": {"$eq": "news"}}}
     *               built as a {@code Struct}; only vectors whose metadata matches are returned. Null for no
     *               filter.
     * @return the matches; {@code getMatchesList()} holds up to {@code topK} matches, each with {@code getId()}
     *         and {@code getScore()}
     */
    U queryByVectorId(int topK, String id, String namespace, Struct filter);

    /**
     * Searches a namespace for the vectors most similar to a vector already stored in it, keeping only those whose
     * metadata matches a filter.
     *
     * <p>Use this to find items similar to one you already have, such as related articles, without sending its values;
     * the server uses the stored values of the vector with that id. Pass a null filter to search every vector in the
     * namespace. If no vector has the id, the response has no matches rather than an error. To search with values you
     * supply, use {@link #queryByVector(int, List, String, Struct, boolean, boolean)}.
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param id the id of a stored vector to search from, such as {@code "doc-1"}; if no vector has this id,
     *           the response has no matches
     * @param namespace the namespace to search, such as {@code "articles-en"}; null or {@code ""} searches the
     *                  default namespace
     * @param filter a metadata filter in Pinecone's filter syntax, such as {@code {"category": {"$eq": "news"}}}
     *               built as a {@code Struct}; only vectors whose metadata matches are returned. Null for no
     *               filter.
     * @param includeValues true to return each match's vector values; false to leave them out
     * @param includeMetadata true to return each match's metadata; false to leave it out
     * @return the matches; {@code getMatchesList()} holds up to {@code topK} matches, each with {@code getId()},
     *         {@code getScore()} and, when requested, {@code getValuesList()} and {@code getMetadata()}
     */
    U queryByVectorId(int topK, String id, String namespace, Struct filter, boolean includeValues, boolean includeMetadata);

    /**
     * Searches the default namespace for the vectors most similar to a dense query vector and returns their ids and
     * scores.
     *
     * <p>To search a named namespace, filter on metadata, or get each match's metadata, use
     * {@link #queryByVector(int, List, String, Struct, boolean, boolean)}.
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param vector the dense query vector, such as {@code Arrays.asList(0.12f, 0.48f, 0.91f)}; the server rejects
     *               a vector whose length does not match the index dimension
     * @return the matches; {@code getMatchesList()} holds up to {@code topK} matches, each with {@code getId()}
     *         and {@code getScore()}
     */
    U queryByVector(int topK, List<Float> vector);

    /**
     * Searches the default namespace for the vectors most similar to a dense query vector.
     *
     * <p>To search a named namespace or filter on metadata, use
     * {@link #queryByVector(int, List, String, Struct, boolean, boolean)}.
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param vector the dense query vector, such as {@code Arrays.asList(0.12f, 0.48f, 0.91f)}; the server rejects
     *               a vector whose length does not match the index dimension
     * @param includeValues true to return each match's vector values; false to leave them out
     * @param includeMetadata true to return each match's metadata; false to leave it out
     * @return the matches; {@code getMatchesList()} holds up to {@code topK} matches, each with {@code getId()},
     *         {@code getScore()} and, when requested, {@code getValuesList()} and {@code getMetadata()}
     */
    U queryByVector(int topK, List<Float> vector, boolean includeValues, boolean includeMetadata);

    /**
     * Searches a namespace for the vectors most similar to a dense query vector and returns their ids and scores.
     *
     * <p>To filter on metadata or get each match's metadata or values, use
     * {@link #queryByVector(int, List, String, Struct, boolean, boolean)}.
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param vector the dense query vector, such as {@code Arrays.asList(0.12f, 0.48f, 0.91f)}; the server rejects
     *               a vector whose length does not match the index dimension
     * @param namespace the namespace to search, such as {@code "articles-en"}; null or {@code ""} searches the
     *                  default namespace
     * @return the matches; {@code getMatchesList()} holds up to {@code topK} matches, each with {@code getId()}
     *         and {@code getScore()}
     */
    U queryByVector(int topK, List<Float> vector, String namespace);

    /**
     * Searches a namespace for the vectors most similar to a dense query vector.
     *
     * <p>To keep only matches whose metadata matches a filter, use
     * {@link #queryByVector(int, List, String, Struct, boolean, boolean)}.
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param vector the dense query vector, such as {@code Arrays.asList(0.12f, 0.48f, 0.91f)}; the server rejects
     *               a vector whose length does not match the index dimension
     * @param namespace the namespace to search, such as {@code "articles-en"}; null or {@code ""} searches the
     *                  default namespace
     * @param includeValues true to return each match's vector values; false to leave them out
     * @param includeMetadata true to return each match's metadata; false to leave it out
     * @return the matches; {@code getMatchesList()} holds up to {@code topK} matches, each with {@code getId()},
     *         {@code getScore()} and, when requested, {@code getValuesList()} and {@code getMetadata()}
     */
    U queryByVector(int topK, List<Float> vector, String namespace, boolean includeValues, boolean includeMetadata);

    /**
     * Searches a namespace for the vectors most similar to a dense query vector, keeping only those whose metadata
     * matches a filter, and returns their ids and scores.
     *
     * <p>To also get each match's metadata or values, use
     * {@link #queryByVector(int, List, String, Struct, boolean, boolean)}.
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param vector the dense query vector, such as {@code Arrays.asList(0.12f, 0.48f, 0.91f)}; the server rejects
     *               a vector whose length does not match the index dimension
     * @param namespace the namespace to search, such as {@code "articles-en"}; null or {@code ""} searches the
     *                  default namespace
     * @param filter a metadata filter in Pinecone's filter syntax, such as {@code {"category": {"$eq": "news"}}}
     *               built as a {@code Struct}; only vectors whose metadata matches are returned. Null for no
     *               filter.
     * @return the matches; {@code getMatchesList()} holds up to {@code topK} matches, each with {@code getId()}
     *         and {@code getScore()}
     */
    U queryByVector(int topK, List<Float> vector, String namespace, Struct filter);

    /**
     * Searches a namespace for the vectors most similar to a dense query vector, keeping only those whose metadata
     * matches a filter.
     *
     * <p>This is the overload for most dense searches; pass a null filter to search every vector in the namespace. To
     * search from a vector already stored in the index, use
     * {@link #queryByVectorId(int, String, String, Struct, boolean, boolean)}; for sparse or hybrid queries, use
     * {@link #query(int, List, List, List, String, String, Struct, boolean, boolean)}. A query made right after an
     * upsert may not reflect it yet.
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param vector the dense query vector, such as {@code Arrays.asList(0.12f, 0.48f, 0.91f)}; the server rejects
     *               a vector whose length does not match the index dimension
     * @param namespace the namespace to search, such as {@code "articles-en"}; null or {@code ""} searches the
     *                  default namespace
     * @param filter a metadata filter in Pinecone's filter syntax, such as {@code {"category": {"$eq": "news"}}}
     *               built as a {@code Struct}; only vectors whose metadata matches are returned. Null for no
     *               filter.
     * @param includeValues true to return each match's vector values; false to leave them out
     * @param includeMetadata true to return each match's metadata; false to leave it out
     * @return the matches; {@code getMatchesList()} holds up to {@code topK} matches, each with {@code getId()},
     *         {@code getScore()} and, when requested, {@code getValuesList()} and {@code getMetadata()}
     */
    U queryByVector(int topK, List<Float> vector, String namespace, Struct filter, boolean includeValues, boolean includeMetadata);

    /**
     * Searches a namespace for the vectors most similar to a query given as dense values, sparse values, both, or the
     * id of a stored vector.
     *
     * <p>Use this overload for sparse indexes and for hybrid queries that combine dense and sparse values; for a
     * dense-only query, {@link #queryByVector(int, List, String, Struct, boolean, boolean)} and
     * {@link #queryByVectorId(int, String, String, Struct, boolean, boolean)} are shorter. Give either values or an id,
     * not both. A dense index needs dense values (sparse values alone are rejected) and accepts sparse values only if
     * its metric is dotproduct; a sparse index needs sparse values and rejects dense ones. The server checks this, not
     * the client. A query made right after an upsert may not reflect it yet.
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param vector the dense query values, such as {@code Arrays.asList(0.12f, 0.48f, 0.91f)}; their length
     *               must match the index dimension. Null or empty to query by sparse values only or by {@code id}.
     * @param sparseIndices the positions of the sparse query values as unsigned 32-bit integers, such as
     *                      {@code Arrays.asList(10L, 45L)}; null for no sparse values
     * @param sparseValues the sparse query values, one per entry in {@code sparseIndices}; null for no sparse values
     * @param id the id of a stored vector to search from instead of passing values, such as {@code "doc-1"}; null
     *           or empty to search with the values given. If no vector has this id, the response has no matches.
     * @param namespace the namespace to search, such as {@code "articles-en"}; null or {@code ""} searches the
     *                  default namespace
     * @param filter a metadata filter in Pinecone's filter syntax, such as {@code {"category": {"$eq": "news"}}}
     *               built as a {@code Struct}; only vectors whose metadata matches are returned. Null for no
     *               filter.
     * @param includeValues true to return each match's vector values; false to leave them out
     * @param includeMetadata true to return each match's metadata; false to leave it out
     * @return the matches; {@code getMatchesList()} holds up to {@code topK} matches, each with {@code getId()},
     *         {@code getScore()} and, when requested, {@code getValuesList()} and {@code getMetadata()}
     * @throws PineconeValidationException if both {@code id} and {@code vector} are given, only one of
     *         {@code sparseIndices} and {@code sparseValues} is given, they differ in length, or a sparse index is
     *         outside the unsigned 32-bit range;
     *         {@code AsyncIndex} throws it from the call, not through the future
     */
    U query(int topK, List<Float> vector, List<Long> sparseIndices, List<Float> sparseValues, String id,
            String namespace, Struct filter, boolean includeValues, boolean includeMetadata);

    /**
     * Fetches vectors by id from the default namespace, with their values and metadata.
     *
     * <p>To fetch from a named namespace, use {@link #fetch(List, String)}. Ids that are not in the namespace are
     * left out of the result rather than reported as an error, so check the returned map for each id you need.
     *
     * @param ids the ids of the vectors to fetch, such as {@code Arrays.asList("doc-1", "doc-2")}; must not be null
     *            or empty, and the server rejects a request with too many ids
     * @return the fetched vectors; {@code getVectorsMap()} maps each id that was found to its {@code Vector}, which
     *         holds {@code getValuesList()}, {@code getSparseValues()} and {@code getMetadata()}
     * @throws PineconeValidationException if {@code ids} is null or empty; {@code AsyncIndex} throws it from the
     *         call, not through the future
     */
    V fetch(List<String> ids);

    /**
     * Fetches vectors by id from a namespace, with their values and metadata.
     *
     * <p>Use this when you know the ids; to find vectors by their metadata instead, use
     * {@code fetchByMetadata} on {@code Index} or {@code AsyncIndex}. Ids that are not in the namespace are left
     * out of the result rather than reported as an error, so check the returned map for each id you need. A fetch
     * made right after an upsert may not reflect it yet.
     *
     * @param ids the ids of the vectors to fetch, such as {@code Arrays.asList("doc-1", "doc-2")}; must not be null
     *            or empty, and the server rejects a request with too many ids
     * @param namespace the namespace to fetch from, such as {@code "articles-en"}; null or {@code ""} means the
     *                  default namespace
     * @return the fetched vectors; {@code getVectorsMap()} maps each id that was found to its {@code Vector}, which
     *         holds {@code getValuesList()}, {@code getSparseValues()} and {@code getMetadata()}
     * @throws PineconeValidationException if {@code ids} is null or empty; {@code AsyncIndex} throws it from the
     *         call, not through the future
     */
    V fetch(List<String> ids, String namespace);


    /**
     * Replaces the dense values of one existing vector in the default namespace, keeping its sparse values and
     * metadata.
     *
     * <p>To update a vector in a named namespace, use {@link #update(String, List, String)}; to change
     * metadata or sparse values, use {@link #update(String, List, Struct, String, List, List)}.
     * If no vector has that id, nothing changes; use upsert to create vectors. A query made right after an
     * update may not reflect it yet.
     *
     * @param id the id of the vector to update, such as {@code "doc-1"}; must not be null, and the server rejects
     *           an empty id
     * @param values the new dense values, such as {@code Arrays.asList(0.12f, 0.48f, 0.91f)}; the length must
     *               match the index dimension
     * @return the server's response; a normal return means the server accepted the update
     * @throws PineconeValidationException if {@code id} is null; {@code AsyncIndex} throws it from the call, not
     *         through the future
     */
    W update(String id, List<Float> values);

    /**
     * Replaces the dense values of one existing vector in a namespace, keeping its sparse values and metadata.
     *
     * <p>To change metadata or sparse values, use {@link #update(String, List, Struct, String, List, List)}.
     * If no vector has that id in the namespace, nothing changes; use upsert to create vectors. A query made
     * right after an update may not reflect it yet.
     *
     * @param id the id of the vector to update, such as {@code "doc-1"}; must not be null, and the server rejects
     *           an empty id
     * @param values the new dense values, such as {@code Arrays.asList(0.12f, 0.48f, 0.91f)}; the length must
     *               match the index dimension
     * @param namespace the namespace that holds the vector, such as {@code "articles-en"}; null or
     *                  {@code ""} means the default namespace
     * @return the server's response; a normal return means the server accepted the update
     * @throws PineconeValidationException if {@code id} is null; {@code AsyncIndex} throws it from the call, not
     *         through the future
     */
    W update(String id, List<Float> values, String namespace);

    /**
     * Changes the dense values, sparse values or metadata of one existing vector in a namespace, keeping the
     * parts you pass null for.
     *
     * <p>Metadata is merged: each field you pass is set, and the vector's other fields are kept. To replace all of
     * a vector's metadata, upsert the vector again. If no vector has that id in the namespace, nothing changes;
     * use upsert to create vectors. To change every vector that matches a metadata filter, use
     * {@link #updateByMetadata(Struct, Struct, String)}. A query made right after an update may not reflect it
     * yet.
     *
     * @param id the id of the vector to update, such as {@code "doc-1"}; must not be null, and the server rejects
     *           an empty id
     * @param values the new dense values, such as {@code Arrays.asList(0.12f, 0.48f, 0.91f)}; the length must
     *               match the index dimension. Null or an empty list keeps the current values; pass null for a
     *               sparse index.
     * @param metadata the metadata fields to set, such as a new category; null keeps the current metadata
     * @param namespace the namespace that holds the vector, such as {@code "articles-en"}; null or {@code ""}
     *                  means the default namespace
     * @param sparseIndices the positions of the new sparse values as unsigned 32-bit integers, such as
     *                      {@code Arrays.asList(10L, 45L)}; null keeps the current sparse values
     * @param sparseValues the new sparse values, one per entry in {@code sparseIndices}; null keeps the current
     *                     sparse values
     * @return the server's response; a normal return means the server accepted the update
     * @throws PineconeValidationException if {@code id} is null, only one of {@code sparseIndices} and
     *         {@code sparseValues} is given, they differ in length, or a sparse index is outside the unsigned
     *         32-bit range;
     *         {@code AsyncIndex} throws it from the call, not through the future
     */
    W update(String id, List<Float> values, Struct metadata, String namespace, List<Long> sparseIndices,
             List<Float> sparseValues);

    /**
     * Sets metadata fields on every vector in a namespace whose metadata matches a filter.
     *
     * <p>Metadata is merged: each field you pass is set on every matching vector, and their other fields are kept.
     * Vector values can't be changed this way; to change one vector's values, use
     * {@link #update(String, List, Struct, String, List, List)}. To count the matches without changing anything, use
     * {@link #updateByMetadata(Struct, Struct, String, boolean)} with {@code dryRun} set to true. A query made right
     * after an update may not reflect it yet.
     *
     * @param filter the metadata filter that selects the vectors to update, such as
     *               {@code {"category": {"$eq": "news"}}} built as a {@code Struct}; must not be null, and the server
     *               rejects an empty filter
     * @param metadata the metadata fields to set on each matching vector, such as {@code {"status": "archived"}}
     * @param namespace the namespace that holds the vectors, such as {@code "articles-en"}; null or {@code ""} means
     *                  the default namespace
     * @return the server's response; {@code getMatchedRecords()} is the number of vectors the filter matched, and
     *         {@code hasMatchedRecords()} is false if the server could not report it
     * @throws PineconeValidationException if {@code filter} is null; {@code AsyncIndex} throws it from the
     *         call, not through the future
     */
    W updateByMetadata(Struct filter, Struct metadata, String namespace);

    /**
     * Sets metadata fields on every vector in a namespace whose metadata matches a filter, or only counts the
     * matches.
     *
     * <p>Run it with {@code dryRun} set to true first to see how many vectors the filter matches, then with false to
     * apply the change. Metadata is merged: each field you pass is set on every matching vector, and their other
     * fields are kept. Vector values can't be changed this way. A query made right after an update may not reflect it
     * yet.
     *
     * @param filter the metadata filter that selects the vectors to update, such as
     *               {@code {"category": {"$eq": "news"}}} built as a {@code Struct}; must not be null, and the server
     *               rejects an empty filter
     * @param metadata the metadata fields to set on each matching vector, such as {@code {"status": "archived"}}
     * @param namespace the namespace that holds the vectors, such as {@code "articles-en"}; null or {@code ""} means
     *                  the default namespace
     * @param dryRun true to only count the vectors the filter matches, without changing them; false to update them
     * @return the server's response; {@code getMatchedRecords()} is the number of vectors the filter matched, and
     *         {@code hasMatchedRecords()} is false if the server could not report it
     * @throws PineconeValidationException if {@code filter} is null; {@code AsyncIndex} throws it from the
     *         call, not through the future
     */
    W updateByMetadata(Struct filter, Struct metadata, String namespace, boolean dryRun);

    /**
     * Deletes vectors by id from the default namespace.
     *
     * <p>To delete from a named namespace, use {@link #deleteByIds(List, String)}. Ids that are not in the namespace
     * are ignored rather than reported as an error. A query made right after a delete may not reflect it yet.
     *
     * @param ids the ids of the vectors to delete, such as {@code Arrays.asList("doc-1", "doc-2")}; the server
     *            rejects a null or empty list and a request with too many ids
     * @return the server's response, which has no fields; a normal return means the server accepted the delete
     */
    X deleteByIds(List<String> ids);

    /**
     * Deletes vectors by id from a namespace.
     *
     * <p>Ids that are not in the namespace are ignored rather than reported as an error. To delete vectors by metadata
     * instead, use {@link #deleteByFilter(Struct, String)}. A query made right after a delete may not reflect it yet.
     *
     * @param ids the ids of the vectors to delete, such as {@code Arrays.asList("doc-1", "doc-2")}; the server
     *            rejects a null or empty list and a request with too many ids
     * @param namespace the namespace to delete from, such as {@code "articles-en"}; null or {@code ""} means the
     *                  default namespace
     * @return the server's response, which has no fields; a normal return means the server accepted the delete
     */
    X deleteByIds(List<String> ids, String namespace);

    /**
     * Deletes every vector in the default namespace whose metadata matches a filter.
     *
     * <p>To delete from a named namespace, use {@link #deleteByFilter(Struct, String)}. The delete can't be undone, and
     * a filter broader than you meant deletes more than you meant, so try the filter in a query first. A query made
     * right after a delete may not reflect it yet.
     *
     * @param filter a metadata filter in Pinecone's filter syntax, such as {@code {"category": {"$eq": "archived"}}}
     *               built as a {@code Struct}; every vector whose metadata matches is deleted. The server rejects
     *               a null or empty filter.
     * @return the server's response, which has no fields; a normal return means the server accepted the delete
     */
    X deleteByFilter(Struct filter);

    /**
     * Deletes every vector in a namespace whose metadata matches a filter.
     *
     * <p>The delete can't be undone, and a filter broader than you meant deletes more than you meant, so try the filter
     * in a query first. To delete specific vectors, use {@link #deleteByIds(List, String)}. A query made right after a
     * delete may not reflect it yet.
     *
     * @param filter a metadata filter in Pinecone's filter syntax, such as {@code {"category": {"$eq": "archived"}}}
     *               built as a {@code Struct}; every vector whose metadata matches is deleted. The server rejects
     *               a null or empty filter.
     * @param namespace the namespace to delete from, such as {@code "articles-en"}; null or {@code ""} means the
     *                  default namespace
     * @return the server's response, which has no fields; a normal return means the server accepted the delete
     */
    X deleteByFilter(Struct filter, String namespace);

    /**
     * Deletes every vector in a namespace.
     *
     * <p>The delete can't be undone. A null or empty namespace deletes every vector in the default namespace, so check
     * the argument before you call this. To delete only some vectors, use {@link #deleteByIds(List, String)} or
     * {@link #deleteByFilter(Struct, String)}. A query made right after a delete may not reflect it yet.
     *
     * @param namespace the namespace to empty, such as {@code "articles-en"}; null or {@code ""} means the
     *                  default namespace
     * @return the server's response, which has no fields; a normal return means the server accepted the delete
     */
    X deleteAll(String namespace);

    /**
     * Deletes vectors by id, by metadata filter, or all vectors in a namespace, depending on the arguments.
     *
     * <p>{@link #deleteByIds(List, String)}, {@link #deleteByFilter(Struct, String)} and {@link #deleteAll(String)} are
     * clearer; use this method when the kind of delete is decided at run time. When {@code deleteAll} is true, pass
     * null for {@code ids} and {@code filter}: the server rejects a request that combines them. When a filter is given,
     * the server deletes by the filter and ignores {@code ids}. A query made right after a delete may not reflect it
     * yet.
     *
     * @param ids the ids of the vectors to delete, such as {@code Arrays.asList("doc-1", "doc-2")}; null or
     *            empty when deleting by filter or deleting all. The server rejects a request with too many ids.
     * @param deleteAll true to delete every vector in the namespace, which can't be undone; false to delete by
     *                  id or filter
     * @param namespace the namespace to delete from, such as {@code "articles-en"}; null or {@code ""} means the
     *                  default namespace
     * @param filter a metadata filter in Pinecone's filter syntax, such as {@code {"category": {"$eq": "archived"}}}
     *               built as a {@code Struct}; every vector whose metadata matches is deleted. Null when deleting
     *               by id or deleting all; the server rejects an empty filter.
     * @return the server's response, which has no fields; a normal return means the server accepted the delete
     */
    X delete(List<String> ids, boolean deleteAll, String namespace, Struct filter);

    /**
     * Returns the number of vectors in the index and in each of its namespaces, plus the index's dimension.
     *
     * <p>Use it to check how many vectors a load wrote and which namespaces hold them. Counts can lag behind recent
     * writes. On serverless indexes the default namespace appears under the key {@code "__default__"}. The server
     * rejects the call for an index with too many namespaces; use {@link #listNamespaces()} and
     * {@link #describeNamespace(String)} instead.
     *
     * @return the statistics: {@code getTotalVectorCount()}, {@code getNamespacesMap()} (namespace name to a
     *         summary whose {@code getVectorCount()} is that namespace's count) and {@code getDimension()} (0 for a
     *         sparse index)
     */
    Y describeIndexStats();

    /**
     * Returns index statistics whose per-namespace counts include only vectors whose metadata matches a filter.
     *
     * <p>Only pod-based indexes accept a filter: serverless indexes reject a non-empty filter, so use
     * {@link #describeIndexStats()} there. The filter narrows the per-namespace counts only;
     * {@code getTotalVectorCount()} still counts every vector in the index. Counts can lag behind recent writes.
     *
     * @param filter a metadata filter in Pinecone's filter syntax, such as {@code {"category": {"$eq": "news"}}}
     *               built as a {@code Struct}; null or an empty filter returns unfiltered statistics
     * @return the statistics: {@code getNamespacesMap()} (namespace name to a summary whose {@code getVectorCount()}
     *         counts the matching vectors), {@code getTotalVectorCount()} and {@code getDimension()}
     */
    Y describeIndexStats(Struct filter);

    /**
     * Checks the arguments of a {@code list} overload and throws for the first one that is missing or invalid.
     *
     * <p>{@link io.pinecone.clients.Index} and {@link io.pinecone.clients.AsyncIndex} call this before each list
     * request, so application code does not need to. Each {@code ...Required} flag says whether the overload takes that
     * argument; arguments whose flag is false are not checked.
     *
     * <pre>{@code
     * index.validateListEndpointParameters("articles-en", "doc-1#", null, 50, true, true, false, true);
     * }</pre>
     *
     * @param namespace the namespace; when required it must not be null ({@code ""} is the default namespace)
     * @param prefix the id prefix; when required it must not be null or empty
     * @param paginationToken the pagination token; when required it must not be null or empty
     * @param limit the page size; when required it must not be null and must be positive
     * @param namespaceRequired whether to check {@code namespace}
     * @param prefixRequired whether to check {@code prefix}
     * @param paginationTokenRequired whether to check {@code paginationToken}
     * @param limitRequired whether to check {@code limit}
     * @throws PineconeValidationException if a required argument is missing or invalid
     */
    default void validateListEndpointParameters(String namespace, String prefix, String paginationToken,
                                                Integer limit, boolean namespaceRequired, boolean prefixRequired,
                                                boolean paginationTokenRequired, boolean limitRequired) {
        if (namespaceRequired && (namespace == null)) {
            throw new PineconeValidationException("Namespace cannot be null");
        }
        if (prefixRequired && (prefix == null || prefix.isEmpty())) {
            throw new PineconeValidationException("Prefix cannot be null or empty");
        }
        if (paginationTokenRequired && (paginationToken == null || paginationToken.isEmpty())) {
            throw new PineconeValidationException("Pagination token cannot be null or empty");
        }
        if (limitRequired && (limit == null || limit <= 0)) {
            throw new PineconeValidationException("Limit must be a positive integer");
        }
    }

    /**
     * Lists the first page of vector ids in the default namespace.
     *
     * <p>Each call returns one page of ids in sorted order. When {@code hasPagination()} is true on the response, pass
     * {@code getPagination().getNext()} to {@link #list(String, int, String)} to get the next page. To list a named
     * namespace, use {@link #list(String)}. A vector upserted just before the call may not be listed yet.
     *
     * @return the page of ids: {@code getVectorsList()} holds an item per vector, whose {@code getId()} is the id, and
     *         {@code getPagination().getNext()} is the token for the next page when {@code hasPagination()} is true
     */
    Z list();

    /**
     * Lists the first page of vector ids in a namespace.
     *
     * <p>Each call returns one page of ids in sorted order. When {@code hasPagination()} is true on the response, pass
     * {@code getPagination().getNext()} to {@link #list(String, int, String)} to get the next page. To list only ids
     * that start with a prefix, use {@link #list(String, String)}; to set the page size, use
     * {@link #list(String, int)}. A vector upserted just before the call may not be listed yet.
     *
     * @param namespace the namespace to list, such as {@code "articles-en"}; {@code ""} means the default namespace.
     *                  Must not be null. A namespace that does not exist returns an empty page.
     * @return the page of ids: {@code getVectorsList()} holds an item per vector, whose {@code getId()} is the id, and
     *         {@code getPagination().getNext()} is the token for the next page when {@code hasPagination()} is true
     * @throws PineconeValidationException if {@code namespace} is null; thrown by the call itself, not through the
     *                                     future
     */
    Z list(String namespace);

    /**
     * Lists the next page of vector ids in a namespace, with a page size.
     *
     * <p>Get the first page with {@link #list(String, int)}, then pass each response's
     * {@code getPagination().getNext()} here until {@code hasPagination()} is false. Ids come back in sorted order. A
     * vector upserted just before the call may not be listed yet.
     *
     * @param namespace the namespace to list, such as {@code "articles-en"}; {@code ""} means the default namespace.
     *                  Must not be null. A namespace that does not exist returns an empty page.
     * @param limit the most ids to return in this page, such as {@code 50}; must be positive, and the server rejects a
     *              limit above its maximum
     * @param paginationToken the token for the next page, from {@code getPagination().getNext()} on the previous
     *                        response. Must not be null or empty.
     * @return the page of ids: {@code getVectorsList()} holds an item per vector, whose {@code getId()} is the id, and
     *         {@code getPagination().getNext()} is the token for the next page when {@code hasPagination()} is true
     * @throws PineconeValidationException if {@code namespace} is null, {@code paginationToken} is null or empty, or
     *                                     {@code limit} is not positive; thrown by the call itself, not through the
     *                                     future
     */
    Z list(String namespace, int limit, String paginationToken);

    /**
     * Lists the first page of vector ids in a namespace, with a page size.
     *
     * <p>Each call returns one page of ids in sorted order. When {@code hasPagination()} is true on the response, pass
     * {@code getPagination().getNext()} to {@link #list(String, int, String)} to get the next page. A vector upserted
     * just before the call may not be listed yet.
     *
     * @param namespace the namespace to list, such as {@code "articles-en"}; {@code ""} means the default namespace.
     *                  Must not be null. A namespace that does not exist returns an empty page.
     * @param limit the most ids to return in this page, such as {@code 50}; must be positive, and the server rejects a
     *              limit above its maximum
     * @return the page of ids: {@code getVectorsList()} holds an item per vector, whose {@code getId()} is the id, and
     *         {@code getPagination().getNext()} is the token for the next page when {@code hasPagination()} is true
     * @throws PineconeValidationException if {@code namespace} is null or {@code limit} is not positive; thrown by the
     *                                     call itself, not through the future
     */
    Z list(String namespace, int limit);

    /**
     * Lists the first page of vector ids in a namespace that start with a prefix.
     *
     * <p>Give ids a shared prefix when you upsert them, such as {@code "doc-1#chunk-1"} for the chunks of one document,
     * and this lists them as a group. Each call returns one page of ids in sorted order. When {@code hasPagination()}
     * is true on the response, pass {@code getPagination().getNext()} to {@link #list(String, String, String)} to get
     * the next page. A vector upserted just before the call may not be listed yet.
     *
     * @param namespace the namespace to list, such as {@code "articles-en"}; {@code ""} means the default namespace.
     *                  Must not be null. A namespace that does not exist returns an empty page.
     * @param prefix the start of the ids to list, such as {@code "doc-1#"}, so that only ids beginning with it are
     *               returned. Must not be null or empty.
     * @return the page of ids: {@code getVectorsList()} holds an item per vector, whose {@code getId()} is the id, and
     *         {@code getPagination().getNext()} is the token for the next page when {@code hasPagination()} is true
     * @throws PineconeValidationException if {@code namespace} is null or {@code prefix} is null or empty; thrown by
     *                                     the call itself, not through the future
     */
    Z list(String namespace, String prefix);

    /**
     * Lists the first page of vector ids in a namespace that start with a prefix, with a page size.
     *
     * <p>Each call returns one page of ids in sorted order. When {@code hasPagination()} is true on the response, pass
     * {@code getPagination().getNext()} to {@link #list(String, String, String)} to get the next page. A vector
     * upserted just before the call may not be listed yet.
     *
     * @param namespace the namespace to list, such as {@code "articles-en"}; {@code ""} means the default namespace.
     *                  Must not be null. A namespace that does not exist returns an empty page.
     * @param prefix the start of the ids to list, such as {@code "doc-1#"}, so that only ids beginning with it are
     *               returned. Must not be null or empty.
     * @param limit the most ids to return in this page, such as {@code 50}; must be positive, and the server rejects a
     *              limit above its maximum
     * @return the page of ids: {@code getVectorsList()} holds an item per vector, whose {@code getId()} is the id, and
     *         {@code getPagination().getNext()} is the token for the next page when {@code hasPagination()} is true
     * @throws PineconeValidationException if {@code namespace} is null, {@code prefix} is null or empty, or
     *                                     {@code limit} is not positive; thrown by the call itself, not through the
     *                                     future
     */
    Z list(String namespace, String prefix, int limit);

    /**
     * Lists the next page of vector ids in a namespace that start with a prefix.
     *
     * <p>Get the first page with {@link #list(String, String)}, then pass each response's
     * {@code getPagination().getNext()} here until {@code hasPagination()} is false. Ids come back in sorted order. A
     * vector upserted just before the call may not be listed yet.
     *
     * @param namespace the namespace to list, such as {@code "articles-en"}; {@code ""} means the default namespace.
     *                  Must not be null. A namespace that does not exist returns an empty page.
     * @param prefix the start of the ids to list, such as {@code "doc-1#"}, so that only ids beginning with it are
     *               returned. Must not be null or empty.
     * @param paginationToken the token for the next page, from {@code getPagination().getNext()} on the previous
     *                        response. Must not be null or empty. It carries the prefix of the listing that returned
     *                        it, so pass the same prefix or the server rejects the request.
     * @return the page of ids: {@code getVectorsList()} holds an item per vector, whose {@code getId()} is the id, and
     *         {@code getPagination().getNext()} is the token for the next page when {@code hasPagination()} is true
     * @throws PineconeValidationException if {@code namespace} is null, {@code prefix} is null or empty, or
     *                                     {@code paginationToken} is null or empty; thrown by the call itself, not
     *                                     through the future
     */
    Z list(String namespace, String prefix, String paginationToken);

    /**
     * Lists the next page of vector ids in a namespace that start with a prefix, with a page size.
     *
     * <p>Get the first page with {@link #list(String, String, int)}, then pass each response's
     * {@code getPagination().getNext()} here until {@code hasPagination()} is false. Ids come back in sorted order. A
     * vector upserted just before the call may not be listed yet.
     *
     * @param namespace the namespace to list, such as {@code "articles-en"}; {@code ""} means the default namespace.
     *                  Must not be null. A namespace that does not exist returns an empty page.
     * @param prefix the start of the ids to list, such as {@code "doc-1#"}, so that only ids beginning with it are
     *               returned. Must not be null or empty.
     * @param paginationToken the token for the next page, from {@code getPagination().getNext()} on the previous
     *                        response. Must not be null or empty. It carries the prefix of the listing that returned
     *                        it, so pass the same prefix or the server rejects the request.
     * @param limit the most ids to return in this page, such as {@code 50}; must be positive, and the server rejects a
     *              limit above its maximum
     * @return the page of ids: {@code getVectorsList()} holds an item per vector, whose {@code getId()} is the id, and
     *         {@code getPagination().getNext()} is the token for the next page when {@code hasPagination()} is true
     * @throws PineconeValidationException if {@code namespace} is null, {@code prefix} is null or empty,
     *                                     {@code paginationToken} is null or empty, or {@code limit} is not positive;
     *                                     thrown by the call itself, not through the future
     */
    Z list(String namespace, String prefix, String paginationToken, int limit);

    /**
     * Lists the first page of namespaces in this index.
     *
     * <p>Namespace operations work on serverless indexes only; the server rejects them on a pod-based index. Records
     * written without a namespace are listed under {@code "__default__"}. Each call returns one page: when
     * {@code hasPagination()} is true on the response, pass {@code getPagination().getNext()} to
     * {@link #listNamespaces(String)} to get the next page.
     *
     * @return the first page of namespaces, with each namespace's name and record count and, when more pages remain,
     *         the token for the next page
     */
    A listNamespaces();

    /**
     * Lists the next page of namespaces in this index, continuing a previous listing.
     *
     * <p>Pass the token from {@code getPagination().getNext()} of the previous response. A response without pagination
     * is the last page. To get the first page, call {@link #listNamespaces()}.
     *
     * @param paginationToken the token from the previous page's {@code getPagination().getNext()}; must not be null
     * @return the next page of namespaces, in the same shape as {@link #listNamespaces()}
     */
    A listNamespaces(String paginationToken);

    /**
     * Lists one page of namespaces in this index, with a page size you choose.
     *
     * <p>Use this overload to set the page size. Pagination works as in {@link #listNamespaces(String)}.
     *
     * @param paginationToken the token from the previous page's {@code getPagination().getNext()}, or null for the
     *                        first page
     * @param limit           the maximum number of namespaces to return in this page, for example {@code 10}
     * @return one page of namespaces, in the same shape as {@link #listNamespaces()}
     */
    A listNamespaces(String paginationToken, int limit);

    /**
     * Lists one page of the namespaces in this index whose names start with a prefix.
     *
     * <p>Use this overload when you name namespaces by a convention, such as one namespace per language or per tenant.
     * {@code getTotalCount()} on the response reports how many namespaces match across all pages. Pagination works as
     * in {@link #listNamespaces(String)}; pass the same prefix on every page.
     *
     * @param prefix          the start of the namespace names to list, for example {@code "articles-"}; null or empty
     *                        lists every namespace
     * @param paginationToken the token from the previous page's {@code getPagination().getNext()}, or null for the
     *                        first page
     * @param limit           the maximum number of namespaces to return in this page, for example {@code 50}
     * @return one page of matching namespaces and the number of matches across all pages
     */
    A listNamespaces(String prefix, String paginationToken, int limit);

    /**
     * Creates an empty namespace in this index.
     *
     * <p>Upserting records into a namespace that does not exist creates it, so you need this method only to create a
     * namespace before writing to it. To choose which metadata fields are indexed for filtering, use
     * {@link #createNamespace(String, MetadataSchema)} instead. Namespace operations work on serverless indexes only;
     * the server rejects them on a pod-based index.
     *
     * @param name the name of the namespace to create, for example {@code "articles-en"}; the server rejects an empty
     *             name and {@code "__default__"}
     * @return the new namespace's description
     */
    B createNamespace(String name);

    /**
     * Creates an empty namespace in this index that indexes only the metadata fields you list.
     *
     * <p>Only fields marked filterable in the schema can be used in metadata filters on records in this namespace.
     * Without a schema, every metadata field is indexed. Namespace operations work on serverless indexes only; the
     * server rejects them on a pod-based index.
     *
     * @param name   the name of the namespace to create, for example {@code "articles-en"}; the server rejects an empty
     *               name and {@code "__default__"}
     * @param schema the metadata fields to index for filtering; null creates the namespace without a schema, so every
     *               metadata field is indexed
     * @return the new namespace's description
     */
    B createNamespace(String name, io.pinecone.proto.MetadataSchema schema);

    /**
     * Describes one namespace in this index, including how many records it holds.
     *
     * <p>To describe the default namespace, pass {@code "__default__"}; the server rejects an empty string. Namespace
     * operations work on serverless indexes only; the server rejects them on a pod-based index.
     *
     * @param namespace the namespace to describe, for example {@code "articles-en"}; must not be null
     * @return the namespace's name, record count, and metadata schema if it has one
     */
    B describeNamespace(String namespace);

    /**
     * Deletes a namespace and every record in it.
     *
     * <p>This cannot be undone. To delete only some records, use {@code deleteByIds} or {@code deleteByFilter} instead.
     * To delete the default namespace, pass {@code "__default__"}; the server rejects an empty string. Namespace
     * operations work on serverless indexes only; the server rejects them on a pod-based index.
     *
     * @param namespace the namespace to delete, for example {@code "articles-en"}; must not be null
     * @return an empty delete response
     */
    X deleteNamespace(String namespace);
}
