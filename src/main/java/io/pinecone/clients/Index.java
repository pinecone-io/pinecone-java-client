package io.pinecone.clients;

import com.google.protobuf.Struct;
import io.pinecone.commons.IndexInterface;
import io.pinecone.configs.PineconeConnection;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.proto.*;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;

import java.util.List;

public class Index implements IndexInterface<UpsertResponse,
        QueryResponseWithUnsignedIndices,
        FetchResponse,
        UpdateResponse,
        DeleteResponse,
        DescribeIndexStatsResponse> {

    private final PineconeConnection connection;
    private final String indexName;
    private final VectorServiceGrpc.VectorServiceBlockingStub blockingStub;

    public Index(PineconeConnection connection, String indexName) {
        if (connection == null) {
            throw new PineconeValidationException("Pinecone connection object cannot be null.");
        }

        this.connection = connection;
        this.indexName = indexName;
        this.blockingStub = connection.getBlockingStub();
    }

    @Override
    public UpsertResponse upsert(List<VectorWithUnsignedIndices> vectorList,
                                 String namespace) {
        UpsertRequest upsertRequest = validateUpsertRequest(vectorList, namespace);
        return blockingStub.upsert(upsertRequest);
    }

    @Override
    public UpsertResponse upsert(String id,
                                 List<Float> values) {
        return upsert(id, values, null, null, null, null);
    }

    @Override
    public UpsertResponse upsert(String id,
                                 List<Float> values,
                                 String namespace) {
        return upsert(id, values, null, null, null, namespace);
    }

    @Override
    public UpsertResponse upsert(String id,
                                 List<Float> values,
                                 List<Long> sparseIndices,
                                 List<Float> sparseValues,
                                 com.google.protobuf.Struct metadata,
                                 String namespace) {
        UpsertRequest upsertRequest = validateUpsertRequest(id, values, sparseIndices, sparseValues, metadata,
                namespace);

        return blockingStub.upsert(upsertRequest);
    }

    @Override
    public QueryResponseWithUnsignedIndices query(int topK,
                                                  List<Float> vector,
                                                  List<Long> sparseIndices,
                                                  List<Float> sparseValues,
                                                  String id,
                                                  String namespace,
                                                  Struct filter,
                                                  boolean includeValues,
                                                  boolean includeMetadata) {
        QueryRequest queryRequest = validateQueryRequest(topK, vector, sparseIndices, sparseValues, id, namespace,
                filter, includeValues, includeMetadata);

        return new QueryResponseWithUnsignedIndices(blockingStub.query(queryRequest));
    }

    @Override
    public QueryResponseWithUnsignedIndices queryByVectorId(int topK,
                                                            String id,
                                                            String namespace,
                                                            Struct filter,
                                                            boolean includeValues,
                                                            boolean includeMetadata) {
        return query(topK, null, null, null, id, namespace, filter, includeValues, includeMetadata);
    }

    @Override
    public QueryResponseWithUnsignedIndices queryByVectorId(int topK,
                                                            String id,
                                                            String namespace,
                                                            Struct filter) {
        return query(topK, null, null, null, id, namespace, filter, false, false);
    }

    @Override
    public QueryResponseWithUnsignedIndices queryByVectorId(int topK,
                                                            String id,
                                                            String namespace,
                                                            boolean includeValues,
                                                            boolean includeMetadata) {
        return query(topK, null, null, null, id, namespace, null, includeValues, includeMetadata);
    }

    @Override
    public QueryResponseWithUnsignedIndices queryByVectorId(int topK,
                                                            String id,
                                                            String namespace) {
        return query(topK, null, null, null, id, namespace, null, false, false);
    }

    @Override
    public QueryResponseWithUnsignedIndices queryByVectorId(int topK, String id, boolean includeValues, boolean includeMetadata) {
        return query(topK, null, null, null, id, null, null, includeValues, includeMetadata);
    }

    @Override
    public QueryResponseWithUnsignedIndices queryByVectorId(int topK,
                                                            String id) {
        return query(topK, null, null, null, id, null, null, false, false);
    }

    @Override
    public QueryResponseWithUnsignedIndices queryByVector(int topK, List<Float> vector, String namespace, Struct filter, boolean includeValues, boolean includeMetadata) {
        return query(topK, vector, null, null, null, namespace, filter, includeValues, includeMetadata);
    }

    @Override
    public QueryResponseWithUnsignedIndices queryByVector(int topK, List<Float> vector, String namespace, Struct filter) {
        return query(topK, vector, null, null, null, namespace, filter, false, false);
    }

    @Override
    public QueryResponseWithUnsignedIndices queryByVector(int topK, List<Float> vector, String namespace, boolean includeValues, boolean includeMetadata) {
        return query(topK, vector, null, null, null, namespace, null, includeValues, includeMetadata);
    }

    @Override
    public QueryResponseWithUnsignedIndices queryByVector(int topK, List<Float> vector, String namespace) {
        return query(topK, vector, null, null, null, namespace, null, false, false);
    }

    @Override
    public QueryResponseWithUnsignedIndices queryByVector(int topK, List<Float> vector, boolean includeValues, boolean includeMetadata) {
        return query(topK, vector, null, null, null, null, null, includeValues, includeMetadata);
    }

    @Override
    public QueryResponseWithUnsignedIndices queryByVector(int topK, List<Float> vector) {
        return query(topK, vector, null, null, null, null, null, false, false);
    }

    @Override
    public FetchResponse fetch(List<String> ids) {
        return fetch(ids, null);
    }

    @Override
    public FetchResponse fetch(List<String> ids,
                               String namespace) {
        FetchRequest fetchRequest = validateFetchRequest(ids, namespace);

        return blockingStub.fetch(fetchRequest);
    }

    @Override
    public UpdateResponse update(String id,
                                 List<Float> values) {
        return update(id, values, null, null, null, null);
    }

    @Override
    public UpdateResponse update(String id,
                                 List<Float> values,
                                 String namespace) {
        return update(id, values, null, namespace, null, null);
    }

    @Override
    public UpdateResponse update(String id,
                                 List<Float> values,
                                 Struct metadata,
                                 String namespace,
                                 List<Long> sparseIndices,
                                 List<Float> sparseValues) {
        UpdateRequest updateRequest = validateUpdateRequest(id, values, metadata, namespace, sparseIndices,
                sparseValues);

        return blockingStub.update(updateRequest);
    }

    @Override
    public DeleteResponse deleteByIds(List<String> ids, String namespace) {
        return delete(ids, false, namespace, null);
    }

    @Override
    public DeleteResponse deleteByIds(List<String> ids) {
        return delete(ids, false, null, null);
    }

    public DeleteResponse deleteByFilter(Struct filter, String namespace) {
        return delete(null, false, namespace, filter);
    }

    @Override
    public DeleteResponse deleteByFilter(Struct filter) {
        return delete(null, false, null, filter);
    }

    @Override
    public DeleteResponse deleteAll(String namespace) {
        return delete(null, true, namespace, null);
    }

    @Override
    public DeleteResponse delete(List<String> ids,
                                 boolean deleteAll,
                                 String namespace,
                                 Struct filter) {
        DeleteRequest deleteRequest = validateDeleteRequest(ids, deleteAll, namespace, filter);

        return blockingStub.delete(deleteRequest);
    }

    @Override
    public DescribeIndexStatsResponse describeIndexStats() {
        DescribeIndexStatsRequest describeIndexStatsRequest = validateDescribeIndexStatsRequest(null);

        return blockingStub.describeIndexStats(describeIndexStatsRequest);
    }

    @Override
    public DescribeIndexStatsResponse describeIndexStats(Struct filter) {
        DescribeIndexStatsRequest describeIndexStatsRequest = validateDescribeIndexStatsRequest(filter);

        return blockingStub.describeIndexStats(describeIndexStatsRequest);
    }

    @Override
    public void close() {
        Pinecone.closeConnection(indexName);
        connection.close();
    }
}
