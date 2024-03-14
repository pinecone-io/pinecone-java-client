package io.pinecone.clients;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.Struct;
import io.pinecone.commons.IndexInterface;
import io.pinecone.configs.PineconeConnection;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.proto.*;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class AsyncIndex implements IndexInterface<ListenableFuture<UpsertResponse>,
        ListenableFuture<QueryResponseWithUnsignedIndices>,
        ListenableFuture<FetchResponse>,
        ListenableFuture<UpdateResponse>,
        ListenableFuture<DeleteResponse>,
        ListenableFuture<DescribeIndexStatsResponse>> {

    private final PineconeConnection connection;
    private final VectorServiceGrpc.VectorServiceFutureStub asyncStub;

    private static final Logger logger = LoggerFactory.getLogger(AsyncIndex.class);

    public AsyncIndex(PineconeConnection connection) {
        if (connection == null) {
            throw new PineconeValidationException("Pinecone connection object cannot be null.");
        }
        this.connection = connection;
        this.asyncStub = connection.getFutureStub();
    }

    public ListenableFuture<UpsertResponse> upsert(List<VectorWithUnsignedIndices> vectorList,
                                                   String namespace) {
        UpsertRequest upsertRequest = validateUpsertRequest(vectorList, namespace);
        return asyncStub.upsert(upsertRequest);
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

        return asyncStub.upsert(upsertRequest);
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

        ListenableFuture<QueryResponse> queryResponseFuture = asyncStub.query(queryRequest);

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

        return asyncStub.fetch(fetchRequest);
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

        return asyncStub.update(updateRequest);
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

        return asyncStub.delete(deleteRequest);
    }

    @Override
    public ListenableFuture<DescribeIndexStatsResponse> describeIndexStats(Struct filter) {
        DescribeIndexStatsRequest describeIndexStatsRequest = validateDescribeIndexStatsRequest(filter);

        return asyncStub.describeIndexStats(describeIndexStatsRequest);
    }

    /**
     * Close the connection and release all resources. A PineconeConnection's underlying gRPC components use resources
     * like threads and TCP connections. To prevent leaking these resources the connection should be closed when it
     * will no longer be used. If it may be used again leave it running.
     */
    @Override
    public void close() {
        try {
            logger.debug("closing channel");
            connection.getChannel().shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn("Channel shutdown interrupted before termination confirmed");
        }
    }
}
