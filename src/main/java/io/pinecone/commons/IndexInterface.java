package io.pinecone.commons;

import com.google.protobuf.Struct;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.proto.*;
import io.pinecone.unsigned_indices_model.SparseValuesWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;

import java.util.ArrayList;
import java.util.List;

import static io.pinecone.utils.SparseIndicesConverter.convertUnsigned32IntToSigned32Int;

public interface IndexInterface<T, U, V, W, X, Y> extends AutoCloseable {

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

    default DescribeIndexStatsRequest validateDescribeIndexStatsRequest(Struct filter) {
        DescribeIndexStatsRequest.Builder describeIndexStatsRequest = DescribeIndexStatsRequest.newBuilder();

        if (filter != null) {
            describeIndexStatsRequest.setFilter(filter);
        }
        return describeIndexStatsRequest.build();
    }

    T upsert(List<VectorWithUnsignedIndices> vectorList, String namespace);

    T upsert(String id, List<Float> values);

    T upsert(String id, List<Float> values, String namespace);

    T upsert(String id, List<Float> values, List<Long> sparseIndices, List<Float> sparseValues, Struct metadata,
             String namespace);

    U queryByVectorId(int topK, String id);

    U queryByVectorId(int topK, String id, boolean includeValues, boolean includeMetadata);

    U queryByVectorId(int topK, String id, String namespace);

    U queryByVectorId(int topK, String id, String namespace, boolean includeValues, boolean includeMetadata);

    U queryByVectorId(int topK, String id, String namespace, Struct filter);

    U queryByVectorId(int topK, String id, String namespace, Struct filter, boolean includeValues,
                      boolean includeMetadata);

    U queryByVector(int topK, List<Float> vector);

    U queryByVector(int topK, List<Float> vector, boolean includeValues, boolean includeMetadata);

    U queryByVector(int topK, List<Float> vector, String namespace);

    U queryByVector(int topK, List<Float> vector, String namespace, boolean includeValues, boolean includeMetadata);

    U queryByVector(int topK, List<Float> vector, String namespace, Struct filter);

    U queryByVector(int topK, List<Float> vector, String namespace, Struct filter, boolean includeValues, boolean includeMetadata);

    U query(int topK, List<Float> vector, List<Long> sparseIndices, List<Float> sparseValues, String id,
            String namespace, Struct filter, boolean includeValues, boolean includeMetadata);

    V fetch(List<String> ids);

    V fetch(List<String> ids, String namespace);

    W update(String id, List<Float> values);

    W update(String id, List<Float> values, String namespace);

    W update(String id, List<Float> values, Struct metadata, String namespace, List<Long> sparseIndices,
             List<Float> sparseValues);

    X deleteByIds(List<String> ids);

    X deleteByIds(List<String> ids, String namespace);

    X deleteByFilter(Struct filter);

    X deleteByFilter(Struct filter, String namespace);

    X deleteAll(String namespace);

    X delete(List<String> ids, boolean deleteAll, String namespace, Struct filter);

    Y describeIndexStats();

    Y describeIndexStats(Struct filter);
}
