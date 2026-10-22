package io.pinecone.clients;

import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.pinecone.commons.IndexInterface;
import io.pinecone.exceptions.PineconeNotFoundException;
import io.pinecone.helpers.MockPinecone;
import io.pinecone.proto.QueryRequest;
import io.pinecone.proto.QueryResponse;
import io.pinecone.proto.ScoredVector;
import io.pinecone.proto.UpsertRequest;
import io.pinecone.proto.UpsertResponse;
import io.pinecone.proto.VectorServiceGrpc;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Drives connect, upsert and query through the public client against {@link MockPinecone}, so request building,
 * headers, gRPC metadata and response parsing all run for real with no API key.
 */
public class MockedCriticalPathTest {
    @RegisterExtension
    final MockPinecone mock = new MockPinecone();

    static class EchoVectorService extends VectorServiceGrpc.VectorServiceImplBase {
        @Override
        public void upsert(UpsertRequest request, StreamObserver<UpsertResponse> response) {
            response.onNext(UpsertResponse.newBuilder().setUpsertedCount(request.getVectorsCount()).build());
            response.onCompleted();
        }

        @Override
        public void query(QueryRequest request, StreamObserver<QueryResponse> response) {
            response.onNext(QueryResponse.newBuilder()
                    .setNamespace(request.getNamespace())
                    .addMatches(ScoredVector.newBuilder().setId("v1").setScore(0.99f).build())
                    .addMatches(ScoredVector.newBuilder().setId("v2").setScore(0.87f).build())
                    .build());
            response.onCompleted();
        }
    }

    @Test
    public void connectUpsertQuery() {
        mock.stubDescribeIndex("critical-path", 3);
        mock.vectorService(new EchoVectorService());

        List<VectorWithUnsignedIndices> vectors = Arrays.asList(
                IndexInterface.buildUpsertVectorWithUnsignedIndices("v1", Arrays.asList(0.1f, 0.2f, 0.3f), null, null, null),
                IndexInterface.buildUpsertVectorWithUnsignedIndices("v2", Arrays.asList(0.4f, 0.5f, 0.6f), null, null, null),
                IndexInterface.buildUpsertVectorWithUnsignedIndices("v3", Arrays.asList(0.7f, 0.8f, 0.9f), null, null, null));
        UpsertResponse upserted;
        QueryResponseWithUnsignedIndices queried;
        try (Index index = mock.client().getIndexConnection("critical-path")) {
            upserted = index.upsert(vectors, "ns");
            queried = index.query(2, Arrays.asList(0.1f, 0.2f, 0.3f), null, null, null, "ns", null, false, false);
        }

        assertEquals(3, upserted.getUpsertedCount());
        assertEquals(2, queried.getMatchesList().size());
        assertEquals("v1", queried.getMatchesList().get(0).getId());
        assertEquals(0.99f, queried.getMatchesList().get(0).getScore(), 1e-6f);
        assertEquals("v2", queried.getMatchesList().get(1).getId());
        assertEquals("ns", queried.getNamespace());

        List<RecordedRequest> restRequests = mock.restRequests();
        assertEquals(1, restRequests.size());
        RecordedRequest describe = restRequests.get(0);
        assertEquals("GET", describe.getMethod());
        assertEquals("/indexes/critical-path", describe.getPath());
        assertEquals("2025-10", describe.getHeader("X-Pinecone-Api-Version"));
        assertEquals(MockPinecone.API_KEY, describe.getHeader("Api-Key"));

        List<MockPinecone.GrpcCall> calls = mock.grpcCalls();
        assertEquals(2, calls.size());
        assertEquals("VectorService/Upsert", calls.get(0).method);
        assertEquals("VectorService/Query", calls.get(1).method);
        for (MockPinecone.GrpcCall call : calls) {
            assertEquals("2025-10", call.header("X-Pinecone-Api-Version"), call.method);
            assertEquals(MockPinecone.API_KEY, call.header("api-key"), call.method);
        }

        UpsertRequest sentUpsert = (UpsertRequest) calls.get(0).request;
        assertEquals("ns", sentUpsert.getNamespace());
        assertEquals(3, sentUpsert.getVectorsCount());
        assertEquals("v3", sentUpsert.getVectors(2).getId());
        assertEquals(Arrays.asList(0.7f, 0.8f, 0.9f), sentUpsert.getVectors(2).getValuesList());

        QueryRequest sentQuery = (QueryRequest) calls.get(1).request;
        assertEquals("ns", sentQuery.getNamespace());
        assertEquals(2, sentQuery.getTopK());
        assertEquals(Arrays.asList(0.1f, 0.2f, 0.3f), sentQuery.getVectorList());
        assertFalse(sentQuery.getIncludeValues());
        assertFalse(sentQuery.getIncludeMetadata());
    }

    @Test
    public void asyncQuerySendsApiVersionMetadata() throws Exception {
        mock.stubDescribeIndex("critical-path-async", 3);
        mock.vectorService(new EchoVectorService());

        QueryResponseWithUnsignedIndices queried;
        try (AsyncIndex index = mock.client().getAsyncIndexConnection("critical-path-async")) {
            queried = index.query(1, Arrays.asList(0.1f, 0.2f, 0.3f), null, null, null, "ns", null, false, false)
                    .get(10, TimeUnit.SECONDS);
        }

        assertEquals("v1", queried.getMatchesList().get(0).getId());
        MockPinecone.GrpcCall call = mock.grpcCalls().get(0);
        assertEquals("VectorService/Query", call.method);
        assertEquals("2025-10", call.header("X-Pinecone-Api-Version"));
        assertEquals(MockPinecone.API_KEY, call.header("api-key"));
    }

    @Test
    public void unmatchedRestRouteIsReportedByMethodAndPath() {
        assertThrows(PineconeNotFoundException.class, () -> mock.client().describeIndex("not-stubbed"));

        AssertionError error = assertThrows(AssertionError.class, mock::verifyNoUnmatchedRequests);
        assertEquals("Requests with no mock route: [REST GET /indexes/not-stubbed]", error.getMessage());
    }

    @Test
    public void unimplementedGrpcMethodIsReportedByName() {
        mock.stubDescribeIndex("no-stats", 3);

        try (Index index = mock.client().getIndexConnection("no-stats")) {
            StatusRuntimeException thrown = assertThrows(StatusRuntimeException.class, index::describeIndexStats);
            assertEquals(io.grpc.Status.Code.UNIMPLEMENTED, thrown.getStatus().getCode());
        }

        AssertionError error = assertThrows(AssertionError.class, mock::verifyNoUnmatchedRequests);
        assertEquals("Requests with no mock route: [gRPC VectorService/DescribeIndexStats]", error.getMessage());
    }
}
