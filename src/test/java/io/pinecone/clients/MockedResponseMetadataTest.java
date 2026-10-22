package io.pinecone.clients;

import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.pinecone.commons.IndexInterface;
import io.pinecone.configs.ResponseMetadata;
import io.pinecone.helpers.MockPinecone;
import io.pinecone.proto.DescribeIndexStatsRequest;
import io.pinecone.proto.DescribeIndexStatsResponse;
import io.pinecone.proto.FetchRequest;
import io.pinecone.proto.FetchResponse;
import io.pinecone.proto.QueryRequest;
import io.pinecone.proto.QueryResponse;
import io.pinecone.proto.UpdateRequest;
import io.pinecone.proto.UpdateResponse;
import io.pinecone.proto.UpsertRequest;
import io.pinecone.proto.UpsertResponse;
import io.pinecone.proto.Usage;
import io.pinecone.proto.VectorServiceGrpc;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks the {@link ResponseMetadata} that {@code withResponseMetadataListener} reports for data-plane calls against
 * {@link MockPinecone}: operation, index, namespace, host, server duration, usage counts, and gRPC status mapping.
 */
public class MockedResponseMetadataTest {
    private static final Metadata.Key<String> DURATION =
            Metadata.Key.of("x-pinecone-response-duration-ms", Metadata.ASCII_STRING_MARSHALLER);

    @RegisterExtension
    final MockPinecone mock = new MockPinecone();

    private final List<ResponseMetadata> reported = Collections.synchronizedList(new ArrayList<>());

    static class CountingVectorService extends VectorServiceGrpc.VectorServiceImplBase {
        volatile Status queryStatus = Status.OK;

        @Override
        public void upsert(UpsertRequest request, StreamObserver<UpsertResponse> response) {
            response.onNext(UpsertResponse.newBuilder().setUpsertedCount(request.getVectorsCount()).build());
            response.onCompleted();
        }

        @Override
        public void query(QueryRequest request, StreamObserver<QueryResponse> response) {
            if (!queryStatus.isOk()) {
                response.onError(queryStatus.asRuntimeException());
                return;
            }
            response.onNext(QueryResponse.newBuilder()
                    .setNamespace(request.getNamespace())
                    .setUsage(Usage.newBuilder().setReadUnits(5))
                    .build());
            response.onCompleted();
        }

        @Override
        public void fetch(FetchRequest request, StreamObserver<FetchResponse> response) {
            response.onNext(FetchResponse.newBuilder().setUsage(Usage.newBuilder().setReadUnits(1)).build());
            response.onCompleted();
        }

        @Override
        public void update(UpdateRequest request, StreamObserver<UpdateResponse> response) {
            response.onNext(UpdateResponse.newBuilder().setMatchedRecords(2).build());
            response.onCompleted();
        }

        @Override
        public void describeIndexStats(DescribeIndexStatsRequest request,
                                       StreamObserver<DescribeIndexStatsResponse> response) {
            response.onNext(DescribeIndexStatsResponse.newBuilder().setDimension(3).build());
            response.onCompleted();
        }
    }

    /** Sends {@code value} as x-pinecone-response-duration-ms in the trailers, or in the headers if {@code inHeaders}. */
    static ServerInterceptor serverDuration(String value, boolean inHeaders) {
        return new ServerInterceptor() {
            @Override
            public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
                                                                         ServerCallHandler<ReqT, RespT> next) {
                return next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
                    @Override
                    public void sendHeaders(Metadata responseHeaders) {
                        if (inHeaders) {
                            responseHeaders.put(DURATION, value);
                        }
                        super.sendHeaders(responseHeaders);
                    }

                    @Override
                    public void close(Status status, Metadata trailers) {
                        if (!inHeaders) {
                            trailers.put(DURATION, value);
                        }
                        super.close(status, trailers);
                    }
                }, headers);
            }
        };
    }

    private Pinecone client() {
        return mock.clientBuilder().withResponseMetadataListener(reported::add).build();
    }

    private List<ResponseMetadata> reported() {
        synchronized (reported) {
            return new ArrayList<>(reported);
        }
    }

    @Test
    public void trackedCallsReportOperationNamespaceCountsAndServerDuration() {
        mock.stubDescribeIndex("metadata-tracked", 3);
        mock.vectorService(new CountingVectorService(), serverDuration("42", false));

        try (Index index = client().getIndexConnection("metadata-tracked")) {
            index.upsert(Arrays.asList(
                    IndexInterface.buildUpsertVectorWithUnsignedIndices("v1", Arrays.asList(0.1f, 0.2f, 0.3f), null, null, null),
                    IndexInterface.buildUpsertVectorWithUnsignedIndices("v2", Arrays.asList(0.4f, 0.5f, 0.6f), null, null, null),
                    IndexInterface.buildUpsertVectorWithUnsignedIndices("v3", Arrays.asList(0.7f, 0.8f, 0.9f), null, null, null)),
                    "ns-upsert");
            index.query(1, Arrays.asList(0.1f, 0.2f, 0.3f), null, null, null, "ns-query", null, false, false);
            index.fetch(Arrays.asList("v1"), "ns-fetch");
            index.update("v1", Arrays.asList(1f, 1f, 1f), "ns-update");
        }

        List<ResponseMetadata> metadata = reported();
        assertEquals(4, metadata.size());
        String[] operations = {"upsert", "query", "fetch", "update"};
        for (int i = 0; i < operations.length; i++) {
            ResponseMetadata m = metadata.get(i);
            assertEquals(operations[i], m.getOperationName());
            assertEquals("metadata-tracked", m.getIndexName(), operations[i]);
            assertEquals("ns-" + operations[i], m.getNamespace(), operations[i]);
            assertEquals(mock.grpcHost(), m.getServerAddress(), operations[i]);
            assertEquals(Long.valueOf(42), m.getServerDurationMs(), operations[i]);
            assertTrue(m.getClientDurationMs() >= 0, operations[i]);
            assertTrue(m.isSuccess(), operations[i]);
            assertEquals("OK", m.getGrpcStatusCode(), operations[i]);
            assertNull(m.getErrorType(), operations[i]);
        }

        assertEquals(Integer.valueOf(3), metadata.get(0).getUpsertedCount());
        assertNull(metadata.get(0).getReadUnits());
        assertEquals(Integer.valueOf(5), metadata.get(1).getReadUnits());
        assertNull(metadata.get(1).getUpsertedCount());
        assertEquals(Integer.valueOf(1), metadata.get(2).getReadUnits());
        assertEquals(Integer.valueOf(2), metadata.get(3).getMatchedRecords());
        assertNull(metadata.get(3).getReadUnits());
    }

    @Test
    public void failedCallReportsGrpcStatusAndErrorType() {
        Map<Status.Code, String> expected = new LinkedHashMap<>();
        expected.put(Status.Code.INVALID_ARGUMENT, "validation");
        expected.put(Status.Code.FAILED_PRECONDITION, "validation");
        expected.put(Status.Code.UNAVAILABLE, "connection");
        expected.put(Status.Code.INTERNAL, "server");
        expected.put(Status.Code.RESOURCE_EXHAUSTED, "rate_limit");
        expected.put(Status.Code.DEADLINE_EXCEEDED, "timeout");
        expected.put(Status.Code.UNAUTHENTICATED, "auth");
        expected.put(Status.Code.PERMISSION_DENIED, "auth");
        expected.put(Status.Code.NOT_FOUND, "not_found");

        mock.stubDescribeIndex("metadata-errors", 3);
        CountingVectorService service = new CountingVectorService();
        mock.vectorService(service, serverDuration("7", false));

        try (Index index = client().getIndexConnection("metadata-errors")) {
            for (Map.Entry<Status.Code, String> entry : expected.entrySet()) {
                service.queryStatus = Status.fromCode(entry.getKey()).withDescription("mocked " + entry.getKey());
                StatusRuntimeException thrown = assertThrows(StatusRuntimeException.class, () ->
                        index.query(1, Arrays.asList(0.1f, 0.2f, 0.3f), null, null, null, "ns", null, false, false));
                assertEquals(entry.getKey(), thrown.getStatus().getCode());
            }
        }

        List<ResponseMetadata> metadata = reported();
        assertEquals(expected.size(), metadata.size());
        int i = 0;
        for (Map.Entry<Status.Code, String> entry : expected.entrySet()) {
            ResponseMetadata m = metadata.get(i++);
            String code = entry.getKey().name();
            assertEquals("query", m.getOperationName(), code);
            assertEquals("ns", m.getNamespace(), code);
            assertFalse(m.isSuccess(), code);
            assertEquals("error", m.getStatus(), code);
            assertEquals(code, m.getGrpcStatusCode());
            assertEquals(entry.getValue(), m.getErrorType(), code);
            assertEquals(Long.valueOf(7), m.getServerDurationMs(), code);
            assertNull(m.getReadUnits(), code);
        }
    }

    @Test
    public void untrackedOperationIsNotReported() {
        mock.stubDescribeIndex("metadata-untracked", 3);
        mock.vectorService(new CountingVectorService());

        try (Index index = client().getIndexConnection("metadata-untracked")) {
            assertEquals(3, index.describeIndexStats().getDimension());
        }

        assertEquals("VectorService/DescribeIndexStats", mock.grpcCalls().get(0).method);
        assertTrue(reported().isEmpty());
    }

    @Test
    public void serverDurationFallsBackToHeadersAndIgnoresInvalidValues() {
        mock.stubDescribeIndex("metadata-duration", 3);
        mock.vectorService(new CountingVectorService(), serverDuration("13", true));
        try (Index index = client().getIndexConnection("metadata-duration")) {
            index.fetch(Arrays.asList("v1"), "ns");
        }

        mock.stubDescribeIndex("metadata-bad-duration", 3);
        mock.vectorService(new CountingVectorService(), serverDuration("not-a-number", false));
        try (Index index = client().getIndexConnection("metadata-bad-duration")) {
            index.fetch(Arrays.asList("v1"), "ns");
        }

        List<ResponseMetadata> metadata = reported();
        assertEquals(2, metadata.size());
        assertEquals(Long.valueOf(13), metadata.get(0).getServerDurationMs());
        assertNull(metadata.get(1).getServerDurationMs());
        assertNull(metadata.get(1).getNetworkOverheadMs());
        assertTrue(metadata.get(1).isSuccess());
    }

    @Test
    public void listenerExceptionDoesNotFailTheCall() {
        mock.stubDescribeIndex("metadata-throwing-listener", 3);
        mock.vectorService(new CountingVectorService());
        Pinecone client = mock.clientBuilder().withResponseMetadataListener(m -> {
            reported.add(m);
            throw new IllegalStateException("listener failure");
        }).build();

        try (Index index = client.getIndexConnection("metadata-throwing-listener")) {
            assertEquals(1, index.fetch(Arrays.asList("v1"), "ns").getUsage().getReadUnits());
        }

        assertEquals(1, reported().size());
    }

    @Test
    public void asyncCallIsReported() throws Exception {
        mock.stubDescribeIndex("metadata-async", 3);
        mock.vectorService(new CountingVectorService(), serverDuration("9", false));

        try (AsyncIndex index = client().getAsyncIndexConnection("metadata-async")) {
            index.query(1, Arrays.asList(0.1f, 0.2f, 0.3f), null, null, null, "ns-async", null, false, false)
                    .get(10, TimeUnit.SECONDS);
        }

        List<ResponseMetadata> metadata = reported();
        assertEquals(1, metadata.size());
        assertEquals("query", metadata.get(0).getOperationName());
        assertEquals("metadata-async", metadata.get(0).getIndexName());
        assertEquals("ns-async", metadata.get(0).getNamespace());
        assertEquals(Long.valueOf(9), metadata.get(0).getServerDurationMs());
        assertEquals(Integer.valueOf(5), metadata.get(0).getReadUnits());
    }
}
