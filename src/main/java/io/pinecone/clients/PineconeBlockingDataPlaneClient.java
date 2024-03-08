package io.pinecone.clients;

import com.google.protobuf.Struct;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.proto.*;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;

import java.util.List;

import static io.pinecone.utils.SparseIndicesConverter.convertUnsigned32IntToSigned32Int;

public class PineconeBlockingDataPlaneClient implements PineconeDataPlaneClient<UpsertResponse, QueryResponseWithUnsignedIndices, FetchResponse, UpdateResponse, DeleteResponse, DescribeIndexStatsResponse> {

    private final VectorServiceGrpc.VectorServiceBlockingStub blockingStub;

    public PineconeBlockingDataPlaneClient(VectorServiceGrpc.VectorServiceBlockingStub blockingStub) {
        if (blockingStub == null) {
            throw new PineconeValidationException("BlockingStub cannot be null.");
        }

        this.blockingStub = blockingStub;
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
        UpsertRequest upsertRequest = validateUpsertRequest(id, values, sparseIndices, sparseValues, metadata, namespace);

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
        QueryRequest queryRequest = validateQueryRequest(topK, vector, sparseIndices, sparseValues, id, namespace, filter, includeValues, includeMetadata);

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
                                         String namespace) {
        return query(topK, null, null, null, id, namespace, null, false, false);
    }
    @Override
    public QueryResponseWithUnsignedIndices queryByVectorId(int topK,
                                         String id) {
        return query(topK, null, null, null, id, null, null, false, false);
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
        UpdateRequest updateRequest = validateUpdateRequest(id, values, metadata, namespace, sparseIndices, sparseValues);

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
    public DescribeIndexStatsResponse describeIndexStats(Struct filter) {
        DescribeIndexStatsRequest describeIndexStatsRequest = validateDescribeIndexStatsRequest(filter);

        return blockingStub.describeIndexStats(describeIndexStatsRequest);
    }
}
