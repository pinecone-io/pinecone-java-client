package io.pinecone.clients;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.grpc.stub.StreamObserver;
import io.pinecone.commons.IndexInterface;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.helpers.MockPinecone;
import io.pinecone.proto.*;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.function.Executable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks that the namespace, list and records calls on {@link Index} and {@link AsyncIndex} reject a namespace, prefix
 * or record id holding an unpaired UTF-16 surrogate before sending anything (issue #118), and that a well-formed
 * surrogate pair is sent unchanged.
 */
public class MockedSurrogateNamespaceTest {
    @RegisterExtension
    final MockPinecone mock = new MockPinecone();

    static final String LONE_HIGH = "x\uD83D";
    static final String EMOJI = "x😀";
    static final String EMOJI_UPSERT_PATH = "/records/namespaces/x%F0%9F%98%80/upsert";

    static class CannedVectorService extends VectorServiceGrpc.VectorServiceImplBase {
        @Override
        public void list(ListRequest request, StreamObserver<ListResponse> response) {
            response.onNext(ListResponse.getDefaultInstance());
            response.onCompleted();
        }

        @Override
        public void listNamespaces(ListNamespacesRequest request, StreamObserver<ListNamespacesResponse> response) {
            response.onNext(ListNamespacesResponse.getDefaultInstance());
            response.onCompleted();
        }

        @Override
        public void createNamespace(CreateNamespaceRequest request, StreamObserver<NamespaceDescription> response) {
            response.onNext(NamespaceDescription.newBuilder().setName(request.getName()).build());
            response.onCompleted();
        }

        @Override
        public void describeNamespace(DescribeNamespaceRequest request,
                                      StreamObserver<NamespaceDescription> response) {
            response.onNext(NamespaceDescription.newBuilder().setName(request.getNamespace()).build());
            response.onCompleted();
        }

        @Override
        public void deleteNamespace(DeleteNamespaceRequest request, StreamObserver<DeleteResponse> response) {
            response.onNext(DeleteResponse.getDefaultInstance());
            response.onCompleted();
        }
    }

    interface Await {
        Object on(Object result) throws Exception;
    }

    static final Await SYNC = result -> result;
    static final Await ASYNC = result -> ((Future<?>) result).get(10, TimeUnit.SECONDS);

    private void connect(String indexName) {
        mock.stubDescribeIndex(indexName, 3);
        mock.vectorService(new CannedVectorService());
    }

    @Test
    public void loneSurrogatesAreRejectedBeforeSending() {
        connect("surrogate-namespaces");
        try (Index index = mock.client().getIndexConnection("surrogate-namespaces")) {
            assertEveryCallRejects(index);
            assertRejects("describe namespace", "Namespace", () -> index.describeNamespace(LONE_HIGH));
            assertRejects("delete namespace", "Namespace", () -> index.deleteNamespace(LONE_HIGH));
        }
        assertEquals(0, mock.grpcCalls().size());
    }

    @Test
    public void asyncLoneSurrogatesAreRejectedBeforeSending() {
        connect("surrogate-namespaces-async");
        try (AsyncIndex index = mock.client().getAsyncIndexConnection("surrogate-namespaces-async")) {
            assertEveryCallRejects(index);
            assertRejects("describe namespace", "Namespace", () -> index.describeNamespace(LONE_HIGH));
            assertRejects("delete namespace", "Namespace", () -> index.deleteNamespace(LONE_HIGH));
        }
        assertEquals(0, mock.grpcCalls().size());
    }

    @Test
    public void surrogatePairsAreSentUnchanged() throws Exception {
        connect("surrogate-namespace-pairs");
        try (Index index = mock.client().getIndexConnection("surrogate-namespace-pairs")) {
            sendWellFormedPairs(index, SYNC);
            index.describeNamespace(EMOJI);
            index.deleteNamespace(EMOJI);
        }
        assertWellFormedPairsSent();
    }

    @Test
    public void asyncSurrogatePairsAreSentUnchanged() throws Exception {
        connect("surrogate-namespace-pairs-async");
        try (AsyncIndex index = mock.client().getAsyncIndexConnection("surrogate-namespace-pairs-async")) {
            sendWellFormedPairs(index, ASYNC);
            ASYNC.on(index.describeNamespace(EMOJI));
            ASYNC.on(index.deleteNamespace(EMOJI));
        }
        assertWellFormedPairsSent();
    }

    @Test
    public void upsertRecordsRejectsLoneSurrogatesBeforeSending() {
        mock.stubDescribeIndex("surrogate-records", 3, mock.restHost());
        Index index = mock.client().getIndexConnection("surrogate-records");

        assertRejects("upsert records", "Namespace",
                () -> index.upsertRecords(LONE_HIGH, Collections.singletonList(record("ok"))));
        assertRejects("upsert records", "Record id",
                () -> index.upsertRecords("ns", Arrays.asList(record("ok"), record(LONE_HIGH))));

        assertEquals(Collections.emptyList(), dataPlaneRequests());
    }

    @Test
    public void upsertRecordsSendsSurrogatePairsUnchanged() throws Exception {
        mock.stubDescribeIndex("surrogate-records-pairs", 3, mock.restHost());
        mock.stub("POST", EMOJI_UPSERT_PATH, 201, "");

        mock.client().getIndexConnection("surrogate-records-pairs")
                .upsertRecords(EMOJI, Collections.singletonList(record(EMOJI)));

        List<RecordedRequest> requests = dataPlaneRequests();
        assertEquals(1, requests.size());
        assertEquals(EMOJI_UPSERT_PATH, requests.get(0).getRequestUrl().encodedPath());
        JsonObject line = JsonParser.parseString(requests.get(0).getBody().readUtf8().trim()).getAsJsonObject();
        assertEquals(EMOJI, line.get("_id").getAsString());
    }

    private static void assertEveryCallRejects(IndexInterface<?, ?, ?, ?, ?, ?, ?, ?, ?> index) {
        assertRejects("list", "Namespace", () -> index.list(LONE_HIGH));
        assertRejects("list", "Namespace", () -> index.list(LONE_HIGH, 10));
        assertRejects("list", "Namespace", () -> index.list(LONE_HIGH, 10, "token"));
        assertRejects("list", "Namespace", () -> index.list(LONE_HIGH, "doc-"));
        assertRejects("list", "Namespace", () -> index.list(LONE_HIGH, "doc-", 10));
        assertRejects("list", "Namespace", () -> index.list(LONE_HIGH, "doc-", "token"));
        assertRejects("list", "Namespace", () -> index.list(LONE_HIGH, "doc-", "token", 10));
        assertRejects("list", "Prefix", () -> index.list("ns", LONE_HIGH));
        assertRejects("list", "Prefix", () -> index.list("ns", LONE_HIGH, 10));
        assertRejects("list", "Prefix", () -> index.list("ns", LONE_HIGH, "token"));
        assertRejects("list", "Prefix", () -> index.list("ns", LONE_HIGH, "token", 10));
        assertRejects("list namespaces", "Prefix", () -> index.listNamespaces(LONE_HIGH, null, 10));
        assertRejects("create namespace", "Namespace", () -> index.createNamespace(LONE_HIGH));
        assertRejects("create namespace", "Namespace", () -> index.createNamespace(LONE_HIGH, null));
    }

    private static void assertRejects(String request, String field, Executable call) {
        PineconeValidationException e = assertThrows(PineconeValidationException.class, call);
        String expected = String.format("Invalid %s request. %s contains an unpaired UTF-16 surrogate (\\uD83D) at "
                + "index 1", request, field);
        assertTrue(e.getMessage().startsWith(expected), e.getMessage());
    }

    private static void sendWellFormedPairs(IndexInterface<?, ?, ?, ?, ?, ?, ?, ?, ?> index, Await await)
            throws Exception {
        await.on(index.list(EMOJI, EMOJI, "token", 10));
        await.on(index.listNamespaces(EMOJI, null, 10));
        await.on(index.createNamespace(EMOJI));
        await.on(index.createNamespace(EMOJI, null));
    }

    private void assertWellFormedPairsSent() {
        List<MockPinecone.GrpcCall> calls = mock.grpcCalls();
        assertEquals(6, calls.size());
        ListRequest list = (ListRequest) calls.get(0).request;
        assertEquals(EMOJI, list.getNamespace());
        assertEquals(EMOJI, list.getPrefix());
        assertEquals(EMOJI, ((ListNamespacesRequest) calls.get(1).request).getPrefix());
        assertEquals(EMOJI, ((CreateNamespaceRequest) calls.get(2).request).getName());
        assertEquals(EMOJI, ((CreateNamespaceRequest) calls.get(3).request).getName());
        assertEquals(EMOJI, ((DescribeNamespaceRequest) calls.get(4).request).getNamespace());
        assertEquals(EMOJI, ((DeleteNamespaceRequest) calls.get(5).request).getNamespace());
    }

    private static Map<String, String> record(String id) {
        Map<String, String> record = new HashMap<>();
        record.put("_id", id);
        record.put("chunk_text", "apples");
        return record;
    }

    private List<RecordedRequest> dataPlaneRequests() {
        List<RecordedRequest> dataPlane = new ArrayList<>();
        for (RecordedRequest request : mock.restRequests()) {
            if (!request.getPath().startsWith("/indexes/")) {
                dataPlane.add(request);
            }
        }
        return dataPlane;
    }
}
