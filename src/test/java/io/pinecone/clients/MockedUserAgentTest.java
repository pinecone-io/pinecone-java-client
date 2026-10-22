package io.pinecone.clients;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.grpc.stub.StreamObserver;
import io.pinecone.helpers.MockPinecone;
import io.pinecone.proto.UpsertRequest;
import io.pinecone.proto.UpsertResponse;
import io.pinecone.proto.VectorServiceGrpc;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openapitools.inference.client.model.EmbeddingsList;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.pinecone.commons.Constants.pineconeClientVersion;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks that the User-Agent, including the {@code withSourceTag} value, reaches the wire on every plane: REST
 * control plane, gRPC data plane (sync and async), REST records and imports, and inference.
 */
public class MockedUserAgentTest {
    @RegisterExtension
    final MockPinecone mock = new MockPinecone();

    private static final String BASE_USER_AGENT = "lang=java; pineconeClientVersion=" + pineconeClientVersion;
    // Already in normalized form, so these tests check propagation independently of the normalization bug below.
    private static final String SOURCE_TAG = "my_agent_20";
    private static final String TAGGED_USER_AGENT = BASE_USER_AGENT + "; source_tag=" + SOURCE_TAG;

    private static final VectorServiceGrpc.VectorServiceImplBase UPSERT_SERVICE = new VectorServiceGrpc.VectorServiceImplBase() {
        @Override
        public void upsert(UpsertRequest request, StreamObserver<UpsertResponse> response) {
            response.onNext(UpsertResponse.newBuilder().setUpsertedCount(request.getVectorsCount()).build());
            response.onCompleted();
        }
    };

    private static final String SEARCH_RESPONSE = "{\"result\":{\"hits\":[]},\"usage\":{\"read_units\":1}}";

    private void exerciseControlAndDataPlanes(Pinecone client) throws Exception {
        mock.vectorService(UPSERT_SERVICE);
        String grpcIndex = "ua-grpc-" + System.nanoTime();
        mock.stubDescribeIndex(grpcIndex, 3);
        Index index = client.getIndexConnection(grpcIndex);
        try {
            index.upsert("v1", Arrays.asList(1f, 2f, 3f), "ns");
        } finally {
            index.close();
        }

        String restIndex = "ua-rest-" + System.nanoTime();
        mock.stubDescribeIndex(restIndex, 3, mock.restHost());
        mock.stub("POST", "/records/namespaces/ns/search", 200, SEARCH_RESPONSE);
        Index recordsIndex = client.getIndexConnection(restIndex);
        try {
            recordsIndex.searchRecordsByText("apples", "ns", null, 1, null, null);
        } finally {
            recordsIndex.close();
        }
    }

    private void assertEveryRequestCarries(String expectedUserAgent, int expectedRestRequests, int expectedGrpcCalls) {
        List<RecordedRequest> rest = mock.restRequests();
        assertEquals(expectedRestRequests, rest.size());
        for (RecordedRequest request : rest) {
            assertEquals(expectedUserAgent, request.getHeader("User-Agent"), request.getMethod() + " " + request.getPath());
        }
        List<MockPinecone.GrpcCall> grpc = mock.grpcCalls();
        assertEquals(expectedGrpcCalls, grpc.size());
        for (MockPinecone.GrpcCall call : grpc) {
            // grpc-java appends its own transport token, e.g. "<ours> grpc-java-netty/1.x".
            String userAgent = call.header("user-agent");
            assertNotNull(userAgent, call.method);
            assertTrue(userAgent.startsWith(expectedUserAgent + " grpc-java-"), call.method + ": " + userAgent);
        }
    }

    @Test
    public void defaultUserAgentHasNoSourceTagOnAnyPlane() throws Exception {
        exerciseControlAndDataPlanes(mock.client());

        assertEveryRequestCarries(BASE_USER_AGENT, 3, 1);
        assertFalse(mock.restRequests().get(0).getHeader("User-Agent").contains("source_tag"));
    }

    @Test
    public void sourceTagReachesControlPlaneGrpcAndRecords() throws Exception {
        exerciseControlAndDataPlanes(mock.clientBuilder().withSourceTag(SOURCE_TAG).build());

        assertEveryRequestCarries(TAGGED_USER_AGENT, 3, 1);
    }

    @Test
    public void sourceTagReachesAsyncGrpcAndImports() throws Exception {
        Pinecone client = mock.clientBuilder().withSourceTag(SOURCE_TAG).build();

        mock.vectorService(UPSERT_SERVICE);
        String grpcIndex = "ua-async-grpc-" + System.nanoTime();
        mock.stubDescribeIndex(grpcIndex, 3);
        AsyncIndex asyncIndex = client.getAsyncIndexConnection(grpcIndex);
        try {
            asyncIndex.upsert("v1", Arrays.asList(1f, 2f, 3f), "ns").get();
        } finally {
            asyncIndex.close();
        }

        String restIndex = "ua-async-rest-" + System.nanoTime();
        mock.stubDescribeIndex(restIndex, 3, mock.restHost());
        mock.stub("GET", "/bulk/imports", 200, "{\"data\":[]}");
        AsyncIndex importsIndex = client.getAsyncIndexConnection(restIndex);
        try {
            importsIndex.listImports();
        } finally {
            importsIndex.close();
        }

        assertEveryRequestCarries(TAGGED_USER_AGENT, 3, 1);
    }

    @Test
    public void sourceTagReachesInference() throws Exception {
        // Inference ignores withHost, so a custom client redirects its api.pinecone.io calls to the mock.
        HttpUrl mockUrl = HttpUrl.get(mock.restUrl());
        OkHttpClient redirecting = new OkHttpClient.Builder()
                .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
                        .url(chain.request().url().newBuilder()
                                .scheme(mockUrl.scheme()).host(mockUrl.host()).port(mockUrl.port()).build())
                        .build()))
                .build();
        mock.stub("POST", "/embed", 200, "{"
                + "\"model\":\"multilingual-e5-large\","
                + "\"vector_type\":\"dense\","
                + "\"data\":[{\"values\":[0.1,0.2],\"vector_type\":\"dense\"}],"
                + "\"usage\":{\"total_tokens\":3}"
                + "}");
        Inference inference = new Pinecone.Builder(MockPinecone.API_KEY)
                .withOkHttpClient(redirecting)
                .withSourceTag(SOURCE_TAG)
                .build()
                .getInferenceClient();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("input_type", "query");
        EmbeddingsList embeddings = inference.embed("multilingual-e5-large", parameters, Collections.singletonList("apples"));

        assertEquals("multilingual-e5-large", embeddings.getModel());
        assertEquals(3, embeddings.getUsage().getTotalTokens());
        assertEveryRequestCarries(TAGGED_USER_AGENT, 1, 0);
        RecordedRequest request = mock.restRequests().get(0);
        assertEquals("/embed", request.getPath());
        assertEquals("2025-10", request.getHeader("X-Pinecone-Api-Version"));
        assertEquals(MockPinecone.API_KEY, request.getHeader("Api-Key"));
        JsonObject body = JsonParser.parseString(request.getBody().readUtf8()).getAsJsonObject();
        assertEquals("multilingual-e5-large", body.get("model").getAsString());
        assertEquals("query", body.getAsJsonObject("parameters").get("input_type").getAsString());
        assertEquals("apples", body.getAsJsonArray("inputs").get(0).getAsJsonObject().get("text").getAsString());
    }

    // Bug report: https://github.com/pinecone-io/pinecone-java-client-internal/issues/12 (Builder.withSourceTag bypasses PineconeConfig.normalizeSourceTag).
    @Disabled("withSourceTag is sent un-normalized because the 4-arg PineconeConfig constructor skips normalizeSourceTag; see https://github.com/pinecone-io/pinecone-java-client-internal/issues/12")
    @Test
    public void builderSourceTagIsNormalizedOnTheWire() throws Exception {
        exerciseControlAndDataPlanes(mock.clientBuilder().withSourceTag("  My Agent 2.0!  ").build());

        assertEveryRequestCarries(TAGGED_USER_AGENT, 3, 1);
    }
}
