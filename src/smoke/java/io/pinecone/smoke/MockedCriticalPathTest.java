package io.pinecone.smoke;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.commons.IndexInterface;
import io.pinecone.configs.PineconeConfig;
import io.pinecone.configs.PineconeConnection;
import io.pinecone.proto.QueryRequest;
import io.pinecone.proto.QueryResponse;
import io.pinecone.proto.ScoredVector;
import io.pinecone.proto.UpsertRequest;
import io.pinecone.proto.UpsertResponse;
import io.pinecone.proto.VectorServiceGrpc;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.db_control.client.model.IndexModel;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mocked critical-path smoke test — connect → upsert → query.
 *
 * <p>The key-free half of the Java SDK's smoke-test coverage. Unlike the keyed suites in
 * {@code src/integration} (which hit a real backend and require {@code PINECONE_API_KEY}) and the
 * localServer suite (which requires the Dockerized mock index), this test stands up its own
 * in-process mocks and runs on every pull request with no API key.
 *
 * <p>It guards the three most critical use cases against a regression in the request/response
 * plumbing:
 * <ol>
 *   <li>connect — construct a client and resolve an index host via {@code describeIndex}</li>
 *   <li>upsert  — write vectors to the data plane</li>
 *   <li>query   — read them back by vector similarity</li>
 * </ol>
 *
 * <p>Mocks are injected at the transport layer, not at the SDK's public API:
 * <ul>
 *   <li>The <em>control plane</em> ({@code describeIndex}) is REST (OkHttp), mocked with
 *       OkHttp {@code MockWebServer} set as the client's base path.</li>
 *   <li>The <em>data plane</em> ({@code upsert} / {@code query}) is gRPC, mocked with an
 *       in-process {@code io.grpc.Server} from {@code grpc-testing} implementing
 *       {@code VectorServiceGrpc.VectorServiceImplBase}. The channel is injected via
 *       {@code PineconeConfig.setCustomManagedChannel}.</li>
 * </ul>
 *
 * <p>Everything above the wire — config resolution, request building, proto marshaling, and
 * response deserialization — is the real code path. This is the Java analogue of the Python
 * SDK's respx-backed gate and the Go SDK's in-process {@code grpc.Server} gate.
 */
class MockedCriticalPathTest {

    private static final String INDEX_NAME = "mocked-critical-path";
    private static final String NAMESPACE = "smoke-ns";

    private MockWebServer controlPlane;
    private Server dataPlane;
    private ManagedChannel dataChannel;
    private MockVectorService mockSvc;
    private String serverName;

    // ---------------------------------------------------------------------------
    // Mock data-plane service
    // ---------------------------------------------------------------------------

    /**
     * In-process implementation of the data-plane gRPC service. Records every call so the
     * test can assert on-wire correctness.
     */
    static class MockVectorService extends VectorServiceGrpc.VectorServiceImplBase {
        final AtomicInteger upsertCalls = new AtomicInteger(0);
        final AtomicInteger queryCalls = new AtomicInteger(0);
        final AtomicReference<UpsertRequest> lastUpsert = new AtomicReference<>();
        final AtomicReference<QueryRequest> lastQuery = new AtomicReference<>();

        @Override
        public void upsert(UpsertRequest req, StreamObserver<UpsertResponse> resp) {
            upsertCalls.incrementAndGet();
            lastUpsert.set(req);
            resp.onNext(UpsertResponse.newBuilder().setUpsertedCount(req.getVectorsCount()).build());
            resp.onCompleted();
        }

        @Override
        public void query(QueryRequest req, StreamObserver<QueryResponse> resp) {
            queryCalls.incrementAndGet();
            lastQuery.set(req);
            resp.onNext(QueryResponse.newBuilder()
                    .addMatches(ScoredVector.newBuilder().setId("v1").setScore(0.99f).build())
                    .addMatches(ScoredVector.newBuilder().setId("v2").setScore(0.87f).build())
                    .build());
            resp.onCompleted();
        }
    }

    // ---------------------------------------------------------------------------
    // Setup / teardown
    // ---------------------------------------------------------------------------

    @BeforeEach
    void setUp() throws IOException {
        // Start the REST control-plane mock.
        controlPlane = new MockWebServer();
        controlPlane.start();

        // Start the in-process gRPC data-plane mock.
        serverName = InProcessServerBuilder.generateName();
        mockSvc = new MockVectorService();
        dataPlane = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(mockSvc)
                .build()
                .start();

        dataChannel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();
    }

    @AfterEach
    void tearDown() throws IOException {
        controlPlane.shutdown();
        dataPlane.shutdownNow();
        dataChannel.shutdownNow();
    }

    // ---------------------------------------------------------------------------
    // Helper: build describe-index JSON response
    // ---------------------------------------------------------------------------

    /**
     * Returns a {@code MockResponse} shaped like a {@code GET /indexes/{name}} response.
     * The {@code host} field is intentionally left as an opaque string because the
     * in-process channel is obtained from {@code PineconeConfig.setCustomManagedChannel},
     * which bypasses host-based dialing entirely.
     */
    private MockResponse describeIndexResponse() {
        String body = "{"
                + "\"name\":\"" + INDEX_NAME + "\","
                + "\"dimension\":3,"
                + "\"metric\":\"cosine\","
                + "\"host\":\"mocked-data-host.pinecone.io\","
                + "\"spec\":{\"serverless\":{"
                +   "\"cloud\":\"gcp\",\"region\":\"us-east1\","
                +   "\"read_capacity\":{\"mode\":\"OnDemand\",\"status\":{\"state\":\"Ready\"}}"
                + "}},"
                + "\"status\":{\"ready\":true,\"state\":\"Ready\"},"
                + "\"vector_type\":\"dense\""
                + "}";
        return new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(body);
    }

    // ---------------------------------------------------------------------------
    // Test
    // ---------------------------------------------------------------------------

    /**
     * Drives connect → upsert → query against fully in-process mocks with no
     * {@code PINECONE_API_KEY}. The dummy API key below only satisfies the constructor;
     * the mocks never validate it.
     */
    @Test
    void testConnectUpsertQueryAgainstMockedBackend() throws Exception {
        // Queue the describe-index response so the control-plane call succeeds.
        controlPlane.enqueue(describeIndexResponse());

        // 1. connect: build the Pinecone client against the mocked control plane
        //    and resolve the data-plane host via describeIndex.
        String controlPlaneUrl = controlPlane.url("").toString();
        Pinecone pc = new Pinecone.Builder("mocked-key")
                .withHost(controlPlaneUrl)
                .build();

        IndexModel idx = pc.describeIndex(INDEX_NAME);
        assertEquals(INDEX_NAME, idx.getName(), "describeIndex should return our index name");
        assertNotNull(idx.getHost(), "resolved host must be non-null");

        // Verify the control-plane request was a GET to /indexes/{name}.
        RecordedRequest ctrlReq = controlPlane.takeRequest();
        assertEquals("GET", ctrlReq.getMethod());
        assertTrue(ctrlReq.getPath().endsWith("/indexes/" + INDEX_NAME),
                "control-plane path should end with /indexes/" + INDEX_NAME);

        // 2. Build the data-plane Index directly with the in-process channel,
        //    bypassing host-based dialing so no real network is used.
        PineconeConfig dataCfg = new PineconeConfig("mocked-key", null);
        dataCfg.setCustomManagedChannel(dataChannel);
        PineconeConnection connection = new PineconeConnection(dataCfg, INDEX_NAME);
        Index index = new Index(dataCfg, connection, INDEX_NAME);

        // 3. upsert: write vectors to the mocked data plane.
        List<VectorWithUnsignedIndices> vectors = Arrays.asList(
                IndexInterface.buildUpsertVectorWithUnsignedIndices("v1", Arrays.asList(0.1f, 0.2f, 0.3f), null, null, null),
                IndexInterface.buildUpsertVectorWithUnsignedIndices("v2", Arrays.asList(0.4f, 0.5f, 0.6f), null, null, null),
                IndexInterface.buildUpsertVectorWithUnsignedIndices("v3", Arrays.asList(0.7f, 0.8f, 0.9f), null, null, null));
        UpsertResponse upsertResp = index.upsert(vectors, NAMESPACE);
        assertEquals(3, upsertResp.getUpsertedCount(),
                "mock should echo back the number of vectors upserted");

        // 4. query: read them back by vector similarity.
        QueryResponseWithUnsignedIndices queryResp = index.query(
                2,
                Arrays.asList(0.1f, 0.2f, 0.3f),
                null, null, null, NAMESPACE, null,
                false, false);
        List<ScoredVectorWithUnsignedIndices> matches = queryResp.getMatchesList();
        assertEquals(2, matches.size(), "mock returns two matches");
        assertEquals("v1", matches.get(0).getId());
        assertEquals(0.99f, matches.get(0).getScore(), 1e-6f);
        assertEquals("v2", matches.get(1).getId());

        // Verify every leg of the critical path was exercised at the transport level.
        assertEquals(1, mockSvc.upsertCalls.get(), "upsert should hit the data plane exactly once");
        assertEquals(1, mockSvc.queryCalls.get(), "query should hit the data plane exactly once");

        UpsertRequest sentUpsert = mockSvc.lastUpsert.get();
        assertEquals(NAMESPACE, sentUpsert.getNamespace(), "namespace must reach the wire for upsert");
        assertEquals(3, sentUpsert.getVectorsCount(), "all three vectors must reach the wire");

        QueryRequest sentQuery = mockSvc.lastQuery.get();
        assertEquals(NAMESPACE, sentQuery.getNamespace(), "namespace must reach the wire for query");
        assertEquals(2, sentQuery.getTopK(), "topK must reach the wire");
        assertEquals(Arrays.asList(0.1f, 0.2f, 0.3f), sentQuery.getVectorList(),
                "query vector must reach the wire");
    }
}
