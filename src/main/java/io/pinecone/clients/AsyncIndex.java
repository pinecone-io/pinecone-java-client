package io.pinecone.clients;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.Struct;
import io.pinecone.commons.PineconeDataPlaneInterface;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.proto.*;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;

import java.util.List;

public class AsyncIndex implements PineconeDataPlaneInterface<ListenableFuture<UpsertResponse>,
        ListenableFuture<QueryResponseWithUnsignedIndices>, ListenableFuture<FetchResponse>,
        ListenableFuture<UpdateResponse>, ListenableFuture<DeleteResponse>,
        ListenableFuture<DescribeIndexStatsResponse>> {

    private final VectorServiceGrpc.VectorServiceFutureStub futureStub;

    public AsyncIndex(VectorServiceGrpc.VectorServiceFutureStub futureStub) {
        if (futureStub == null) {
            throw new PineconeValidationException("FutureStub cannot be null.");
        }

        this.futureStub = futureStub;
    }

    @Override
    public ListenableFuture<UpsertResponse> upsert(String id,
                                                   List<Float> values) {
        return upsert(id, values, null, null, null, null);
    }

    @Override
    public ListenableFuture<UpsertResponse> upsert(String id,
                                                   List<Float> values,
                                                   String namespace) {
        return upsert(id, values, null, null, null, namespace);
    }

    @Override
    public ListenableFuture<UpsertResponse> upsert(String id,
                                                   List<Float> values,
                                                   List<Long> sparseIndices,
                                                   List<Float> sparseValues,
                                                   com.google.protobuf.Struct metadata,
                                                   String namespace) {
        UpsertRequest upsertRequest = validateUpsertRequest(id, values, sparseIndices, sparseValues, metadata,
                namespace);

        return futureStub.upsert(upsertRequest);
    }

    @Override
    public ListenableFuture<QueryResponseWithUnsignedIndices> query(int topK,
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

        ListenableFuture<QueryResponse> queryResponseFuture = futureStub.query(queryRequest);

        return Futures.transform(queryResponseFuture,
                QueryResponseWithUnsignedIndices::new, MoreExecutors.directExecutor());
    }

    @Override
    public ListenableFuture<QueryResponseWithUnsignedIndices> queryByVectorId(int topK,
                                                                              String id,
                                                                              String namespace,
                                                                              Struct filter,
                                                                              boolean includeValues,
                                                                              boolean includeMetadata) {
        return query(topK, null, null, null, id, namespace, filter, includeValues, includeMetadata);
    }

    @Override
    public ListenableFuture<QueryResponseWithUnsignedIndices> queryByVectorId(int topK,
                                                                              String id,
                                                                              String namespace,
                                                                              Struct filter) {
        return query(topK, null, null, null, id, namespace, filter, false, false);
    }

    @Override
    public ListenableFuture<QueryResponseWithUnsignedIndices> queryByVectorId(int topK,
                                                                              String id,
                                                                              String namespace) {
        return query(topK, null, null, null, id, namespace, null, false, false);
    }

    @Override
    public ListenableFuture<QueryResponseWithUnsignedIndices> queryByVectorId(int topK,
                                                                              String id) {
        return query(topK, null, null, null, id, null, null, false, false);
    }

    @Override
    public ListenableFuture<FetchResponse> fetch(List<String> ids) {
        return fetch(ids, null);
    }

    @Override
    public ListenableFuture<FetchResponse> fetch(List<String> ids,
                                                 String namespace) {
        FetchRequest fetchRequest = validateFetchRequest(ids, namespace);

        return futureStub.fetch(fetchRequest);
    }

    @Override
    public ListenableFuture<UpdateResponse> update(String id,
                                                   List<Float> values) {
        return update(id, values, null, null, null, null);
    }

    @Override
    public ListenableFuture<UpdateResponse> update(String id,
                                                   List<Float> values,
                                                   String namespace) {
        return update(id, values, null, namespace, null, null);
    }

    @Override
    public ListenableFuture<UpdateResponse> update(String id,
                                                   List<Float> values,
                                                   Struct metadata,
                                                   String namespace,
                                                   List<Long> sparseIndices,
                                                   List<Float> sparseValues) {
        UpdateRequest updateRequest = validateUpdateRequest(id, values, metadata, namespace, sparseIndices,
                sparseValues);

        return futureStub.update(updateRequest);
    }

    @Override
    public ListenableFuture<DeleteResponse> deleteByIds(List<String> ids, String namespace) {
        return delete(ids, false, namespace, null);
    }

    @Override
    public ListenableFuture<DeleteResponse> deleteByIds(List<String> ids) {
        return delete(ids, false, null, null);
    }

    @Override
    public ListenableFuture<DeleteResponse> deleteByFilter(Struct filter, String namespace) {
        return delete(null, false, namespace, filter);
    }

    @Override
    public ListenableFuture<DeleteResponse> deleteByFilter(Struct filter) {
        return delete(null, false, null, filter);
    }

    @Override
    public ListenableFuture<DeleteResponse> deleteAll(String namespace) {
        return delete(null, true, namespace, null);
    }

    @Override
    public ListenableFuture<DeleteResponse> delete(List<String> ids,
                                                   boolean deleteAll,
                                                   String namespace,
                                                   Struct filter) {
        DeleteRequest deleteRequest = validateDeleteRequest(ids, deleteAll, namespace, filter);

        return futureStub.delete(deleteRequest);
    }

    @Override
    public ListenableFuture<DescribeIndexStatsResponse> describeIndexStats(Struct filter) {
        DescribeIndexStatsRequest describeIndexStatsRequest = validateDescribeIndexStatsRequest(filter);

        return futureStub.describeIndexStats(describeIndexStatsRequest);
    }
}
