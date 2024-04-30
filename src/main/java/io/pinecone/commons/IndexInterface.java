package io.pinecone.commons;

import com.google.protobuf.Struct;
import io.pinecone.clients.Index;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.proto.*;
import io.pinecone.unsigned_indices_model.SparseValuesWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;

import java.util.ArrayList;
import java.util.List;

import static io.pinecone.utils.SparseIndicesConverter.convertUnsigned32IntToSigned32Int;

/**
 * The {@code IndexInterface} provides a set of methods for performing operations
 * such as upserting, querying, fetching, updating, and deleting vectors in an index.
 * It supports operations with various types of data including dense vectors, sparse vectors,
 * and metadata associated with the vectors. This interface is designed to be generic to support
 * different implementations for handling vector storage and retrieval.
 *
 * @param <T> The return type for upsert operations.
 * @param <U> The return type for query operations.
 * @param <V> The return type for fetch operations.
 * @param <W> The return type for update operations.
 * @param <X> The return type for delete operations.
 * @param <Y> The return type for describing index stats.
 * @param <Z> The return type for listing vector IDs.
 */
public interface IndexInterface<T, U, V, W, X, Y, Z> extends AutoCloseable {

    /**
     * Validates and builds an upsert request with a single vector and optional namespace.
     *
     * @param id The unique identifier of the vector
     * @param values The values of the dense vector
     * @param sparseIndices The indices for the sparse vector representation
     * @param sparseValues The values corresponding to the indices in the sparse vector representation
     * @param metadata The metadata associated with the vector
     * @param namespace The namespace of the vector (optional)
     * @return An {@link UpsertRequest} built from the provided arguments
     * @throws PineconeValidationException if there are invalid arguments
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
     * Validates and builds an upsert request with a list of vectors and optional namespace. This method
     * is useful for batch upsert operations.
     *
     * @param vectorWithUnsignedIndicesList A list of {@link VectorWithUnsignedIndices} objects representing the vectors to upsert.
     * @param namespace The namespace within which these vectors should be upserted. This parameter is optional.
     * @return An {@link UpsertRequest} object constructed based on the provided vectors and namespace.
     * @throws PineconeValidationException if any of the vectors contain invalid arguments.
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

        return UpsertRequest.newBuilder().addAllVectors(vectors).setNamespace(namespace).build();
    }

    /**
     * Constructs a {@link Vector} object while validating the provided arguments. This helper method is used in a
     * variety of upsert operations.
     *
     * @param id The unique identifier for the vector.
     * @param values The dense vector values.
     * @param sparseIndices Indices for sparse vector representation.
     * @param sparseValues Values for the sparse indices.
     * @param metadata Metadata associated with the vector.
     * @return A {@link Vector} object constructed from the provided arguments.
     * @throws PineconeValidationException if there are invalid arguments.
     */
    default Vector buildUpsertVector(String id,
                                     List<Float> values,
                                     List<Long> sparseIndices,
                                     List<Float> sparseValues,
                                     Struct metadata) {
        if (id == null || id.isEmpty() || values == null || values.isEmpty()) {
            throw new PineconeValidationException("Invalid upsert request. Please ensure that both id and values are " +
                    "provided.");
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
     * Constructs a {@link VectorWithUnsignedIndices} object while validating the provided arguments. This helper method
     * is used in a variety of upsert operations.
     *
     * @param id The unique identifier for the vector.
     * @param values The dense vector values.
     * @param sparseIndices Indices for sparse vector representation, using unsigned integers.
     * @param sparseValues Values for the sparse indices.
     * @param metadata Metadata associated with the vector.
     * @return A {@link VectorWithUnsignedIndices} object constructed from the provided arguments.
     * @throws PineconeValidationException if there are invalid arguments.
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
     * Validates and constructs a {@link QueryRequest} object.
     *
     * @param topK The number of top matching vectors to retrieve.
     * @param vector The query vector for similarity search. If querying with vector, id must be null.
     * @param sparseIndices Indices for sparse query vector.
     * @param sparseValues Values for the sparse query vector.
     * @param id The unique identifier of the vector to query. If querying with id, vector must be null.
     * @param namespace The namespace from which to query vectors.
     * @param filter The filter to apply. You can use vector metadata to limit your search.
     * @param includeValues Flag indicating whether to include vector values in the response.
     * @param includeMetadata Flag indicating whether to include metadata in the response.
     * @return A {@link QueryRequest} object constructed from the provided arguments.
     * @throws PineconeValidationException if there are invalid request arguments.
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
     * Validates and constructs a fetch request for retrieving vectors by their ids from a namespace.
     *
     * @param ids The list of vector IDs to fetch.
     * @param namespace The namespace to fetch vectors from. If not specified, the default namespace is used.
     * @return A {@link FetchRequest} object constructed from the provided IDs and namespace.
     * @throws PineconeValidationException if the vector IDs are not provided.
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
     * Validates and constructs an update request for modifying an existing vector.
     *
     * @param id The unique ID of the vector to update.
     * @param values The new values for the vector. If provided, these values replace the existing ones.
     * @param metadata The new metadata for the vector. If provided, it replaces the existing metadata.
     * @param namespace The namespace of the vector to be updated. If not specified, the default namespace is used.
     * @param sparseIndices Indices for updating a sparse vector representation.
     * @param sparseValues Values corresponding to the sparse indices.
     * @return An {@link UpdateRequest} object constructed from the provided parameters.
     * @throws PineconeValidationException if there are invalid arguments.
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
     * Validates and constructs a delete request for removing vectors by their IDs or based on a filter.
     * Can also handle requests for deleting all vectors within a namespace.
     *
     * @param ids The list of vector IDs to delete.
     * @param deleteAll Flag indicating whether to delete all vectors in the namespace.
     * @param namespace The namespace from which to delete the vectors. This parameter is optional if deleteAll is true.
     * @param filter A metadata filter used to select the vectors to delete. Mutually exclusive with ids and deleteAll.
     * @return A {@link DeleteRequest} object constructed from the provided parameters.
     * @throws PineconeValidationException if the request parameters are invalid.
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
     * Validates and constructs a request to describe the statistics of the index.
     *
     * @param filter If a filter is provided, the operation returns statistics for vectors that match the filter.
     * @return A {@link DescribeIndexStatsRequest} object constructed from the provided filter.
     */
    default DescribeIndexStatsRequest validateDescribeIndexStatsRequest(Struct filter) {
        DescribeIndexStatsRequest.Builder describeIndexStatsRequest = DescribeIndexStatsRequest.newBuilder();

        if (filter != null) {
            describeIndexStatsRequest.setFilter(filter);
        }
        return describeIndexStatsRequest.build();
    }

    /**
     * Performs an upsert operation for a list of vectors within a namespace. If namespace is empty a default namespace is used.
     *
     * @param vectorList A list of vectors with unsigned indices to upsert.
     * @param namespace The namespace where the vectors should be upserted. This is optional.
     * @return A generic type {@code T} indicating the result of the upsert operation.
     */
    T upsert(List<VectorWithUnsignedIndices> vectorList, String namespace);

    /**
     * Performs an upsert operation for a single vector in the default namespace.
     *
     * @param id The unique identifier of the vector.
     * @param values The list of floating-point values that represent the vector.
     * @return A generic type {@code T} indicating the result of the upsert operation.
     */
    T upsert(String id, List<Float> values);

    /**
     * Performs an upsert operation for a single vector in the specified namespace.
     *
     * @param id The unique identifier of the vector.
     * @param values The list of floating-point values that represent the vector.
     * @param namespace The namespace where the vector should be upserted. This is optional.
     * @return A generic type {@code T} indicating the result of the upsert operation.
     */
    T upsert(String id, List<Float> values, String namespace);

    /**
     * Upserts a vector with both dense and sparse components, and optional metadata, into a specified namespace.
     *
     * @param id The unique identifier for the vector.
     * @param values The dense vector values.
     * @param sparseIndices Indices for sparse vector representation.
     * @param sparseValues Values for the sparse indices.
     * @param metadata Metadata associated with the vector.
     * @param namespace The namespace to upsert the vector into. This is optional.
     * @return A generic type {@code T} indicating the result of the upsert operation.
     */
    T upsert(String id, List<Float> values, List<Long> sparseIndices, List<Float> sparseValues, Struct metadata,
             String namespace);

    /**
     * Queries a namespace using a query vector. Retrieves the ids of the most similar items, along with their similarity scores.
     *
     * @param topK The number of top similar vectors to retrieve.
     * @param id The unique id of a vector to be used as a query vector.
     * @return A generic type {@code U} representing the query results.
     */
    U queryByVectorId(int topK, String id);

    /**
     * Queries a namespace using a query vector identified by an ID, with options to include values and metadata in the results.
     * Retrieves the IDs of the most similar items, along with their similarity scores, and optionally, their values and metadata.
     *
     * @param topK The number of top similar vectors to retrieve.
     * @param id The unique ID of a vector to be used as a query vector.
     * @param includeValues Flag indicating whether to include the vector values in the query results.
     * @param includeMetadata Flag indicating whether to include the vector metadata in the query results.
     * @return A generic type {@code U} representing the query results, including optional values and metadata based on parameters.
     */
    U queryByVectorId(int topK, String id, boolean includeValues, boolean includeMetadata);

    /**
     * Queries a specified namespace using a vector ID. Retrieves the IDs of the most similar items, along with their similarity scores.
     *
     * @param topK The number of top similar vectors to retrieve.
     * @param id The unique ID of a vector to be used as a query vector.
     * @param namespace The namespace within which the query is performed.
     * @return A generic type {@code U} representing the query results.
     */
    U queryByVectorId(int topK, String id, String namespace);

    /**
     * Queries a specified namespace using a vector ID, with options to include values and metadata in the results.
     * Retrieves the IDs of the most similar items, along with their similarity scores, and optionally, their values and metadata.
     *
     * @param topK The number of top similar vectors to retrieve.
     * @param id The unique ID of a vector to be used as a query vector.
     * @param namespace The namespace within which the query is performed.
     * @param includeValues Flag indicating whether to include the vector values in the query results.
     * @param includeMetadata Flag indicating whether to include the vector metadata in the query results.
     * @return A generic type {@code U} representing the query results, including optional values and metadata based on parameters.
     */
    U queryByVectorId(int topK, String id, String namespace, boolean includeValues, boolean includeMetadata);

    /**
     * Queries a specified namespace using a vector ID and a custom filter. Retrieves the IDs of the most similar items, along with their similarity scores.
     *
     * @param topK The number of top similar vectors to retrieve.
     * @param id The unique ID of a vector to be used as a query vector.
     * @param namespace The namespace within which the query is performed.
     * @param filter A Struct defining additional filtering criteria for the query.
     * @return A generic type {@code U} representing the query results, filtered according to the provided criteria.
     */
    U queryByVectorId(int topK, String id, String namespace, Struct filter);

    /**
     * Queries a specified namespace using a vector ID, a custom filter, and options to include values and metadata in the results.
     * Retrieves the IDs of the most similar items, along with their similarity scores, and optionally, their values and metadata, all while applying a filter.
     *
     * @param topK The number of top similar vectors to retrieve.
     * @param id The unique ID of a vector to be used as a query vector.
     * @param namespace The namespace within which the query is performed.
     * @param filter A Struct defining additional filtering criteria for the query.
     * @param includeValues Flag indicating whether to include the vector values in the query results.
     * @param includeMetadata Flag indicating whether to include the vector metadata in the query results.
     * @return A generic type {@code U} representing the query results, including optional values and metadata based on parameters and filtered according to the provided criteria.
     */
    U queryByVectorId(int topK, String id, String namespace, Struct filter, boolean includeValues, boolean includeMetadata);

    /**
     * Queries the default namespace using a provided query vector. Retrieves the IDs of the most similar items,
     * along with their similarity scores.
     *
     * @param topK The number of top similar vectors to retrieve.
     * @param vector The query vector as a list of floating-point values.
     * @return A generic type {@code U} representing the query results.
     */
    U queryByVector(int topK, List<Float> vector);

    /**
     * Queries the default namespace using a provided query vector, with options to include values and metadata in the results.
     * Retrieves the IDs of the most similar items, along with their similarity scores, and optionally, their values and metadata.
     *
     * @param topK The number of top similar vectors to retrieve.
     * @param vector The query vector as a list of floating-point values.
     * @param includeValues Flag indicating whether to include the vector values in the query results.
     * @param includeMetadata Flag indicating whether to include the vector metadata in the query results.
     * @return A generic type {@code U} representing the query results, including optional values and metadata based on parameters.
     */
    U queryByVector(int topK, List<Float> vector, boolean includeValues, boolean includeMetadata);

    /**
     * Queries a specified namespace using a provided query vector. Retrieves the IDs of the most similar items,
     * along with their similarity scores.
     *
     * @param topK The number of top similar vectors to retrieve.
     * @param vector The query vector as a list of floating-point values.
     * @param namespace The namespace within which the query is performed.
     * @return A generic type {@code U} representing the query results.
     */
    U queryByVector(int topK, List<Float> vector, String namespace);

    /**
     * Queries a specified namespace using a provided query vector, with options to include values and metadata in the results.
     * Retrieves the IDs of the most similar items, along with their similarity scores, and optionally, their values and metadata.
     *
     * @param topK The number of top similar vectors to retrieve.
     * @param vector The query vector as a list of floating-point values.
     * @param namespace The namespace within which the query is performed.
     * @param includeValues Flag indicating whether to include the vector values in the query results.
     * @param includeMetadata Flag indicating whether to include the vector metadata in the query results.
     * @return A generic type {@code U} representing the query results, including optional values and metadata based on parameters.
     */
    U queryByVector(int topK, List<Float> vector, String namespace, boolean includeValues, boolean includeMetadata);

    /**
     * Queries a specified namespace using a provided query vector and a custom filter. Retrieves the IDs of the most similar items,
     * along with their similarity scores.
     *
     * @param topK The number of top similar vectors to retrieve.
     * @param vector The query vector as a list of floating-point values.
     * @param namespace The namespace within which the query is performed.
     * @param filter A Struct defining additional filtering criteria for the query.
     * @return A generic type {@code U} representing the query results, filtered according to the provided criteria.
     */
    U queryByVector(int topK, List<Float> vector, String namespace, Struct filter);

    /**
     * Queries a specified namespace using a provided query vector, a custom filter, and options to include values and metadata in the results.
     * Retrieves the IDs of the most similar topK items, along with their similarity scores.
     *
     * @param topK The number of top similar vectors to retrieve.
     * @param vector The query vector as a list of floating-point values.
     * @param namespace The namespace within which the query is performed.
     * @param filter A Struct defining additional filtering criteria for the query.
     * @param includeValues Flag indicating whether to include the vector values in the query results.
     * @param includeMetadata Flag indicating whether to include the vector metadata in the query results.
     * @return A generic type {@code U} representing the query results, including optional values and metadata based on parameters and filtered according to the provided criteria.
     */
    U queryByVector(int topK, List<Float> vector, String namespace, Struct filter, boolean includeValues, boolean includeMetadata);

    /**
     * Queries a namespace using a provided query vector. Retrieves the IDs of the most similar items, along with their similarity scores.
     *
     * @param topK The number of top similar vectors to retrieve.
     * @param vector The dense query vector as a list of floating-point values. Each query can contain only one of the parameters: vector or id.
     * @param sparseIndices The indices of the sparse vector representation, if applicable.
     * @param sparseValues The values at the specified indices of the sparse vector representation.
     * @param id The unique ID of a vector to be used as a query vector. Each query can contain only one of the parameters: vector or id.
     * @param namespace The namespace within which the query is performed.
     * @param filter A Struct defining additional filtering criteria for the query.
     * @param includeValues Flag indicating whether to include the vector values in the query results.
     * @param includeMetadata Flag indicating whether to include the vector metadata in the query results.
     * @return A generic type {@code U} representing the query results containing a list of the closest vectors and a namespace name.
     *
     */
    U query(int topK, List<Float> vector, List<Long> sparseIndices, List<Float> sparseValues, String id,
            String namespace, Struct filter, boolean includeValues, boolean includeMetadata);

    /**
     * Looks up vectors by ID from the default namespace.
     *
     * @param ids A list of unique identifiers for the vectors to fetch.
     * @return A generic type {@code V} representing the fetched vectors and the namespace.
     */
    V fetch(List<String> ids);

    /**
     * Looks up vectors by ID from a specified namespace.
     *
     * @param ids A list of unique identifiers for the vectors to fetch.
     * @param namespace The namespace to fetch vectors from.
     * @return A generic type {@code V} representing the fetched vectors and the namespace.
     */
    V fetch(List<String> ids, String namespace);

    /**
     * Updates an existing vector by ID with new values in the default namespace.
     *
     * @param id The unique identifier of the vector to update.
     * @param values The new list of vector values to assign to set.
     * @return A generic type {@code W} representing the result of the update operation.
     */
    W update(String id, List<Float> values);

    /**
     * Updates an existing vector by ID with new values in a specified namespace.
     *
     * @param id The unique identifier of the vector to update.
     * @param values The new list of vector values to assign to set.
     * @param namespace The namespace in which to update the vector.
     * @return A generic type {@code W} representing the result of the update operation.
     */
    W update(String id, List<Float> values, String namespace);

    /**
     * Updates an existing vector by ID in a specified namespace. Allows updating the vector's values, metadata,
     * and sparse representation.
     *
     * @param id The unique identifier of the vector to be updated.
     * @param values The new list of floating-point values for the vector's dense representation. Previous values are overwritten.
     * @param metadata Optional new metadata to associate with the vector. If provided, it replaces the existing metadata.
     * @param namespace The namespace in which the vector resides and will be updated.
     * @param sparseIndices Indices for the sparse vector representation, if applicable.
     * @param sparseValues Values for the sparse vector representation. Must correspond to the provided indices.
     * @return A generic type {@code W} representing the result of the update operation.
     */
    W update(String id, List<Float> values, Struct metadata, String namespace, List<Long> sparseIndices,
             List<Float> sparseValues);

    /**
     * Deletes a list of vectors by ID from the default namespace.
     *
     * @param ids A list of unique vector IDs to be deleted.
     * @return A generic type {@code X} indicating the result of the delete operation.
     */
    X deleteByIds(List<String> ids);

    /**
     * Deletes vectors identified by a list of unique IDs from a specified namespace.
     *
     * @param ids A list of unique vector IDs to be deleted.
     * @param namespace The namespace from which the vectors will be deleted.
     * @return A generic type {@code X} indicating the result of the delete operation.
     */
    X deleteByIds(List<String> ids, String namespace);

    /**
     * Deletes vectors matching a specific metadata filter criteria from the default namespace.
     *
     * @param filter The metadata filter used to select vectors to delete.
     * @return A generic type {@code X} indicating the result of the delete operation.
     */
    X deleteByFilter(Struct filter);

    /**
     * Deletes vectors matching a specific metadata filter criteria from a specified namespace.
     *
     * @param filter The metadata filter used to select vectors to delete.
     * @param namespace The namespace from which the vectors matching the filter will be deleted.
     * @return A generic type {@code X} indicating the result of the delete operation.
     */
    X deleteByFilter(Struct filter, String namespace);

    /**
     * Deletes all vectors within a specified namespace. If {@code null} is passed, all vectors in the
     * default namespace are deleted.
     *
     * @param namespace The namespace from which all vectors will be deleted. If not specified the default will be used.
     * @return A generic type {@code X} indicating the result of the delete all operation.
     */
    X deleteAll(String namespace);

    /**
     * A flexible delete operation allowing for the deletion of vectors by ID, by filter, or all vectors in a namespace.
     *
     * @param ids A list of unique identifiers for specific vectors to be deleted. Ignored if deleteAll is true.
     * @param deleteAll A boolean flag indicating whether to delete all vectors in the specified namespace.
     * @param namespace The namespace from which vectors will be deleted. If not specified, the default namespace is used.
     * @param filter An optional Struct defining additional filtering criteria for vectors to be deleted. Ignored if deleteAll is true.
     * @return A generic type {@code X} indicating the result of the delete operation.
     */
    X delete(List<String> ids, boolean deleteAll, String namespace, Struct filter);

    /**
     * Retrieves statistics about the index's contents, such as vector count per namespace, and number of dimensions.
     *
     * @return A generic type {@code Y} that contains the stats about the index.
     */
    Y describeIndexStats();

    /**
     * Retrieves statistics about the index's contents for vectors that satisfy the applied metadata filter.
     *
     * @param filter A metadata filter used to select vectors that satisfy the filter criteria.
     * @return A generic type {@code Y} that contains the stats about the index.
     */
    Y describeIndexStats(Struct filter);

    /**
     * <p>Validates the parameters for a list endpoint operation.
     *
     * <p>It throws a {@link PineconeValidationException} if any required validation fails. The {@code "...Required"}
     * parameters indicate which method signature is used.
     *
     * <p>Example
     * <pre>{@code
     *      try {
     *          String namespace = "example-namespace";
     *          String prefix = "example-prefix";
     *          String paginationToken = "token123";
     *          Integer limit = 50;
     *
     *           // Indicate which parameters are required
     *          validateListEndpointParameters(namespace, prefix, paginationToken, limit, true, true, true);
     *
     *          } catch (PineconeValidationException e) {
     *           System.err.println("Validation error: " + e.getMessage());
     *          }
     * }</pre>
     *:
     * @param namespace The namespace parameter which is validated based on the {@code namespaceRequired} flag.
     * @param prefix The prefix parameter which is validated based on the {@code prefixRequired} flag.
     * @param paginationToken The pagination token parameter which is validated based on the {@code paginationTokenRequired} flag.
     * @param limit The limit for the number of items, validated to be a positive integer if {@code limitRequired} is true.
     * @param namespaceRequired Specifies if the namespace parameter is required and should be validated.
     * @param prefixRequired Specifies if the prefix parameter is required and should be validated.
     * @param paginationTokenRequired Specifies if the pagination token parameter is required and should be validated.
     * @param limitRequired Specifies if the limit parameter is required and should be a positive integer.
     * @throws PineconeValidationException if any parameter fails its validation check based on its requirements.
     */
    default void validateListEndpointParameters(String namespace, String prefix, String paginationToken,
                                                Integer limit, boolean namespaceRequired, boolean prefixRequired,
                                                boolean paginationTokenRequired, boolean limitRequired) {
        if (namespaceRequired && (namespace == null || namespace.isEmpty())) {
            throw new PineconeValidationException("Namespace cannot be null or empty");
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
     * Retrieves up to {@code 100} vector IDs from an index from the default namespace.
     *
     * @return A generic type {@code Y} that contains vector IDs.
     */
    Z list();
}
