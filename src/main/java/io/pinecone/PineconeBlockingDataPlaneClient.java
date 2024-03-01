package io.pinecone;

import com.google.protobuf.Struct;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.proto.*;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;

import java.util.List;

import static io.pinecone.utils.SparseIndicesConverter.convertUnsigned32IntToSigned32Int;

public class PineconeBlockingDataPlaneClient {

    private final VectorServiceGrpc.VectorServiceBlockingStub blockingStub;

    public PineconeBlockingDataPlaneClient(VectorServiceGrpc.VectorServiceBlockingStub blockingStub) {
        if (blockingStub == null) {
            throw new PineconeValidationException("BlockingStub cannot be null.");
        }

        this.blockingStub = blockingStub;
    }

    public UpsertResponse upsert(String id,
                                 List<Float> values) {
        return upsert(id, values, null, null, null, null);
    }

    public UpsertResponse upsert(String id,
                                 List<Float> values,
                                 String namespace) {
        return upsert(id, values, null, null, null, namespace);
    }

    public UpsertResponse upsert(String id,
                                 List<Float> values,
                                 List<Long> sparseIndices,
                                 List<Float> sparseValues,
                                 com.google.protobuf.Struct metadata,
                                 String namespace) {
        UpsertRequest.Builder upsertRequest = UpsertRequest.newBuilder();

        if (id == null || id.isEmpty() || values == null || values.isEmpty()) {
            throw new PineconeValidationException("Invalid upsert request. Please ensure that both id and values are provided.");
        }

        Vector.Builder vectorBuilder = Vector.newBuilder()
                .setId(id)
                .addAllValues(values);

        if ((sparseIndices != null && sparseValues == null) || (sparseIndices == null && sparseValues != null)) {
            throw new PineconeValidationException("Invalid upsert request. Please ensure that both sparse indices and values are present.");
        }
        if (sparseIndices != null) {
            if (sparseIndices.size() != sparseValues.size()) {
                throw new PineconeValidationException("Invalid upsert request. Please ensure that both sparse indices and values are of the same length.");
            }
            vectorBuilder.setSparseValues(SparseValues.newBuilder()
                    .addAllIndices(convertUnsigned32IntToSigned32Int(sparseIndices))
                    .addAllValues(sparseValues)
                    .build());
        }

        if (metadata != null) {
            vectorBuilder.setMetadata(metadata);
        }

        upsertRequest.addVectors(vectorBuilder.build());

        if (namespace != null) {
            upsertRequest.setNamespace(namespace);
        }

        return blockingStub.upsert(upsertRequest.build());
    }

    public QueryResponseWithUnsignedIndices query(int topK,
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
            throw new PineconeValidationException("Invalid query request. Cannot query with both vector id and vector values.");
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
            throw new PineconeValidationException("Invalid upsert request. Please ensure that both sparse indices and values are present.");
        }

        if (sparseIndices != null) {
            if (sparseIndices.size() != sparseValues.size()) {
                throw new PineconeValidationException("Invalid upsert request. Please ensure that both sparse indices and values are of the same length.");
            }
            queryRequest.setSparseVector(SparseValues.newBuilder()
                    .addAllIndices(convertUnsigned32IntToSigned32Int(sparseIndices))
                    .addAllValues(sparseValues)
                    .build());
        }

        return new QueryResponseWithUnsignedIndices(blockingStub.query(queryRequest.build()));
    }

    public QueryResponseWithUnsignedIndices queryByVectorId(int topK,
                                         String id,
                                         String namespace,
                                         Struct filter,
                                         boolean includeValues,
                                         boolean includeMetadata) {
        return query(topK, null, null, null, id, namespace, filter, includeValues, includeMetadata);
    }

    public QueryResponseWithUnsignedIndices queryByVectorId(int topK,
                                         String id,
                                         String namespace,
                                         Struct filter) {
        return query(topK, null, null, null, id, namespace, filter, false, false);
    }

    public QueryResponseWithUnsignedIndices queryByVectorId(int topK,
                                         String id,
                                         String namespace) {
        return query(topK, null, null, null, id, namespace, null, false, false);
    }

    public QueryResponseWithUnsignedIndices queryByVectorId(int topK,
                                         String id) {
        return query(topK, null, null, null, id, null, null, false, false);
    }

    public FetchResponse fetch(List<String> ids) {
        return fetch(ids, null);
    }

    public FetchResponse fetch(List<String> ids,
                               String namespace) {
        FetchRequest.Builder fetchRequest = FetchRequest.newBuilder();

        if (ids == null || ids.isEmpty()) {
            throw new PineconeValidationException("Invalid fetch request. Vector ids must be present");
        }

        fetchRequest.addAllIds(ids);

        if (namespace != null) {
            fetchRequest.setNamespace(namespace);
        }

        return blockingStub.fetch(fetchRequest.build());
    }

    public UpdateResponse update(String id,
                                 List<Float> values) {
        return update(id, values, null, null, null, null);
    }

    public UpdateResponse update(String id,
                                 List<Float> values,
                                 String namespace) {
        return update(id, values, null, namespace, null, null);
    }

    public UpdateResponse update(String id,
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
            throw new PineconeValidationException("Invalid upsert request. Please ensure that both sparse indices and values are present.");
        }

        if (sparseIndices != null) {
            if (sparseIndices.size() != sparseValues.size()) {
                throw new PineconeValidationException("Invalid upsert request. Please ensure that both sparse indices and values are of the same length.");
            }
            updateRequest.setSparseValues(SparseValues.newBuilder()
                    .addAllIndices(convertUnsigned32IntToSigned32Int(sparseIndices))
                    .addAllValues(sparseValues)
                    .build());
        }

        return blockingStub.update(updateRequest.build());
    }

    public DeleteResponse deleteByIds(List<String> ids, String namespace) {
        return delete(ids, false, namespace, null);
    }

    public DeleteResponse deleteByIds(List<String> ids) {
        return delete(ids, false, null, null);
    }

    public DeleteResponse deleteByFilter(Struct filter, String namespace) {
        return delete(null, false, namespace, filter);
    }

    public DeleteResponse deleteByFilter(Struct filter) {
        return delete(null, false, null, filter);
    }

    public DeleteResponse deleteAll(String namespace) {
        return delete(null, true, namespace, null);
    }

    public DeleteResponse delete(List<String> ids,
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

        return blockingStub.delete(deleteRequest.build());
    }

    public DescribeIndexStatsResponse describeIndexStats(Struct filter) {
        DescribeIndexStatsRequest.Builder describeIndexStatsRequest = DescribeIndexStatsRequest.newBuilder();

        if (filter != null) {
            describeIndexStatsRequest.setFilter(filter);
        }

        return blockingStub.describeIndexStats(describeIndexStatsRequest.build());
    }
}
