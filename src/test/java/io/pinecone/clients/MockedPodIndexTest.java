package io.pinecone.clients;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.pinecone.exceptions.PineconeForbiddenException;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.helpers.MockPinecone;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openapitools.db_control.client.ApiException;
import org.openapitools.db_control.client.model.IndexModel;
import org.openapitools.db_control.client.model.PodSpec;
import org.openapitools.db_control.client.model.PodSpecMetadataConfig;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Offline coverage of the pod-based control-plane calls: the JSON each {@link Pinecone#createPodsIndex} overload
 * sends to {@code POST /indexes}, the JSON each {@link Pinecone#configurePodsIndex} overload sends to
 * {@code PATCH /indexes/{name}}, how the pod spec in the response is parsed, validation, and error mapping.
 */
public class MockedPodIndexTest {
    @RegisterExtension
    final MockPinecone mock = new MockPinecone();

    private static final String POD_INDEX_RESPONSE = "{"
            + "\"name\":\"pod-idx\","
            + "\"metric\":\"euclidean\","
            + "\"dimension\":3,"
            + "\"host\":\"pod-idx-abc.svc.us-east1-gcp.pinecone.io\","
            + "\"deletion_protection\":\"enabled\","
            + "\"tags\":{\"env\":\"test\"},"
            + "\"spec\":{\"pod\":{\"environment\":\"us-east1-gcp\",\"replicas\":2,\"shards\":3,\"pods\":6,"
            + "\"pod_type\":\"p1.x2\",\"metadata_config\":{\"indexed\":[\"genre\"]},\"source_collection\":\"coll\"}},"
            + "\"status\":{\"ready\":true,\"state\":\"Ready\"},"
            + "\"vector_type\":\"dense\""
            + "}";

    private static PodSpecMetadataConfig genreOnly() {
        return new PodSpecMetadataConfig().indexed(Arrays.asList("genre"));
    }

    private static JsonObject body(RecordedRequest request, String method, String path) {
        assertEquals(method, request.getMethod());
        assertEquals(path, request.getPath());
        assertEquals("2025-10", request.getHeader("X-Pinecone-Api-Version"));
        assertEquals(MockPinecone.API_KEY, request.getHeader("Api-Key"));
        return JsonParser.parseString(request.getBody().readUtf8()).getAsJsonObject();
    }

    private static JsonObject createBody(RecordedRequest request) {
        return body(request, "POST", "/indexes");
    }

    private static JsonObject pod(JsonObject body) {
        JsonObject spec = body.getAsJsonObject("spec");
        assertEquals(1, spec.size());
        return spec.getAsJsonObject("pod");
    }

    private static Set<String> keys(JsonObject object) {
        return new HashSet<>(object.keySet());
    }

    private static Set<String> setOf(String... values) {
        return new HashSet<>(Arrays.asList(values));
    }

    @Test
    public void fullCreatePodsIndexSendsEveryFieldAndParsesThePodSpec() {
        mock.stub("POST", "/indexes", 201, POD_INDEX_RESPONSE);
        Map<String, String> tags = new HashMap<>();
        tags.put("env", "test");

        IndexModel index = mock.client().createPodsIndex("pod-idx", 3, "us-east1-gcp", "p1.x2", "euclidean",
                2, 3, 6, genreOnly(), "coll", "enabled", tags);

        List<RecordedRequest> requests = mock.restRequests();
        assertEquals(1, requests.size());
        JsonObject body = createBody(requests.get(0));
        assertEquals("pod-idx", body.get("name").getAsString());
        assertEquals(3, body.get("dimension").getAsInt());
        assertEquals("euclidean", body.get("metric").getAsString());
        assertEquals("enabled", body.get("deletion_protection").getAsString());
        assertEquals("test", body.getAsJsonObject("tags").get("env").getAsString());
        assertEquals(1, body.getAsJsonObject("tags").size());
        JsonObject pod = pod(body);
        assertEquals(setOf("environment", "pod_type", "replicas", "shards", "pods", "metadata_config",
                "source_collection"), keys(pod));
        assertEquals("us-east1-gcp", pod.get("environment").getAsString());
        assertEquals("p1.x2", pod.get("pod_type").getAsString());
        assertEquals(2, pod.get("replicas").getAsInt());
        assertEquals(3, pod.get("shards").getAsInt());
        assertEquals(6, pod.get("pods").getAsInt());
        assertEquals("genre", pod.getAsJsonObject("metadata_config").getAsJsonArray("indexed").get(0).getAsString());
        assertEquals("coll", pod.get("source_collection").getAsString());

        assertEquals("pod-idx", index.getName());
        assertEquals("pod-idx-abc.svc.us-east1-gcp.pinecone.io", index.getHost());
        assertEquals(3, index.getDimension());
        assertEquals("euclidean", index.getMetric());
        assertEquals("enabled", index.getDeletionProtection());
        assertEquals("test", index.getTags().get("env"));
        PodSpec podSpec = index.getSpec().getIndexModelPodBased().getPod();
        assertEquals("us-east1-gcp", podSpec.getEnvironment());
        assertEquals("p1.x2", podSpec.getPodType());
        assertEquals(2, podSpec.getReplicas());
        assertEquals(3, podSpec.getShards());
        assertEquals(6, podSpec.getPods());
        assertEquals(Arrays.asList("genre"), podSpec.getMetadataConfig().getIndexed());
        assertEquals("coll", podSpec.getSourceCollection());
    }

    @Test
    public void eachCreatePodsIndexOverloadSendsOnlyItsOwnFields() {
        mock.stub("POST", "/indexes", 201, POD_INDEX_RESPONSE);
        Pinecone client = mock.client();

        client.createPodsIndex("pod-idx", 3, "env", "p1.x1");
        client.createPodsIndex("pod-idx", 3, "env", "p1.x1", "enabled");
        client.createPodsIndex("pod-idx", 3, "env", "p1.x1", "dotproduct", genreOnly());
        client.createPodsIndex("pod-idx", 3, "env", "p1.x1", "cosine", "coll");
        client.createPodsIndex("pod-idx", 3, "env", "p1.x1", 4);
        client.createPodsIndex("pod-idx", 3, "env", "p1.x1", 4, genreOnly());
        client.createPodsIndex("pod-idx", 3, "env", "p1.x1", 2, 3);
        client.createPodsIndex("pod-idx", 3, "env", "p1.x1", 2, 3, genreOnly());

        List<RecordedRequest> requests = mock.restRequests();
        assertEquals(8, requests.size());
        String[] expectedMetric = {null, null, "dotproduct", "cosine", null, null, null, null};
        String[] expectedProtection = {"disabled", "enabled", "disabled", "disabled", "disabled", "disabled",
                "disabled", "disabled"};
        List<Set<String>> expectedPodKeys = Arrays.asList(
                setOf("environment", "pod_type"),
                setOf("environment", "pod_type"),
                setOf("environment", "pod_type", "metadata_config"),
                setOf("environment", "pod_type", "source_collection"),
                setOf("environment", "pod_type", "pods"),
                setOf("environment", "pod_type", "pods", "metadata_config"),
                setOf("environment", "pod_type", "replicas", "shards"),
                setOf("environment", "pod_type", "replicas", "shards", "metadata_config"));
        JsonObject[] bodies = new JsonObject[requests.size()];
        for (int i = 0; i < requests.size(); i++) {
            String overload = "overload #" + i;
            JsonObject body = createBody(requests.get(i));
            bodies[i] = body;
            assertEquals("pod-idx", body.get("name").getAsString(), overload);
            assertEquals(3, body.get("dimension").getAsInt(), overload);
            if (expectedMetric[i] == null) {
                assertFalse(body.has("metric"), overload);
            } else {
                assertEquals(expectedMetric[i], body.get("metric").getAsString(), overload);
            }
            assertEquals(expectedProtection[i], body.get("deletion_protection").getAsString(), overload);
            assertEquals(0, body.getAsJsonObject("tags").size(), overload);
            JsonObject pod = pod(body);
            assertEquals(expectedPodKeys.get(i), keys(pod), overload);
            assertEquals("env", pod.get("environment").getAsString(), overload);
            assertEquals("p1.x1", pod.get("pod_type").getAsString(), overload);
        }
        assertEquals("coll", pod(bodies[3]).get("source_collection").getAsString());
        assertEquals(4, pod(bodies[4]).get("pods").getAsInt());
        assertEquals(2, pod(bodies[6]).get("replicas").getAsInt());
        assertEquals(3, pod(bodies[6]).get("shards").getAsInt());
    }

    @Test
    public void invalidCreateArgumentsAreRejectedBeforeAnyRequest() {
        Pinecone client = mock.client();

        assertEquals("indexName cannot be null or empty", assertThrows(PineconeValidationException.class,
                () -> client.createPodsIndex("", 3, "env", "p1.x1")).getMessage());
        assertEquals("Dimension cannot be null", assertThrows(PineconeValidationException.class,
                () -> client.createPodsIndex("pod-idx", null, "env", "p1.x1")).getMessage());
        assertTrue(assertThrows(PineconeValidationException.class,
                () -> client.createPodsIndex("pod-idx", 0, "env", "p1.x1")).getMessage()
                .startsWith("Dimension must be greater than 0"));
        assertEquals("Environment cannot be null or empty", assertThrows(PineconeValidationException.class,
                () -> client.createPodsIndex("pod-idx", 3, null, "p1.x1")).getMessage());
        assertEquals("podType cannot be null or empty", assertThrows(PineconeValidationException.class,
                () -> client.createPodsIndex("pod-idx", 3, "env", "")).getMessage());
        assertEquals("Number of pods does not equal number of shards times number of replicas",
                assertThrows(PineconeValidationException.class, () -> client.createPodsIndex("pod-idx", 3, "env",
                        "p1.x1", null, 2, 3, 5, null, null, "disabled", null)).getMessage());

        assertEquals(0, mock.restRequests().size());
    }

    @Test
    public void eachConfigurePodsIndexOverloadSendsOnlyItsOwnFields() {
        mock.stub("PATCH", "/indexes/pod-idx", 202, POD_INDEX_RESPONSE);
        Pinecone client = mock.client();
        Map<String, String> tags = new HashMap<>();
        tags.put("env", "test");

        IndexModel configured = client.configurePodsIndex("pod-idx", "p1.x2", 3, "enabled", tags);
        client.configurePodsIndex("pod-idx", 3, "disabled");
        client.configurePodsIndex("pod-idx", "enabled");

        List<RecordedRequest> requests = mock.restRequests();
        assertEquals(3, requests.size());

        JsonObject full = body(requests.get(0), "PATCH", "/indexes/pod-idx");
        assertEquals(setOf("spec", "deletion_protection", "tags"), keys(full));
        assertEquals(setOf("pod_type", "replicas"), keys(pod(full)));
        assertEquals("p1.x2", pod(full).get("pod_type").getAsString());
        assertEquals(3, pod(full).get("replicas").getAsInt());
        assertEquals("enabled", full.get("deletion_protection").getAsString());
        assertEquals("test", full.getAsJsonObject("tags").get("env").getAsString());

        JsonObject replicasOnly = body(requests.get(1), "PATCH", "/indexes/pod-idx");
        assertEquals(setOf("replicas"), keys(pod(replicasOnly)));
        assertEquals(3, pod(replicasOnly).get("replicas").getAsInt());
        assertEquals("disabled", replicasOnly.get("deletion_protection").getAsString());
        assertEquals(0, replicasOnly.getAsJsonObject("tags").size());

        JsonObject protectionOnly = body(requests.get(2), "PATCH", "/indexes/pod-idx");
        assertFalse(pod(protectionOnly).has("replicas"));
        assertFalse(pod(protectionOnly).has("pod_type"));
        assertEquals("enabled", protectionOnly.get("deletion_protection").getAsString());
        assertEquals(0, protectionOnly.getAsJsonObject("tags").size());

        assertEquals("p1.x2", configured.getSpec().getIndexModelPodBased().getPod().getPodType());
    }

    @Test
    public void invalidConfigureArgumentsAreRejectedBeforeAnyRequest() {
        Pinecone client = mock.client();

        assertEquals("indexName cannot be null or empty", assertThrows(PineconeValidationException.class,
                () -> client.configurePodsIndex(null, "enabled")).getMessage());
        assertEquals("indexName cannot be null or empty", assertThrows(PineconeValidationException.class,
                () -> client.configurePodsIndex("", "p1.x2", 2, "enabled", null)).getMessage());
        assertEquals("Number of replicas must be >= 1", assertThrows(PineconeValidationException.class,
                () -> client.configurePodsIndex("pod-idx", 0, "disabled")).getMessage());

        assertEquals(0, mock.restRequests().size());
    }

    @Test
    public void podQuotaErrorIsMappedToPineconeForbiddenException() {
        String error = "{\"error\":{\"code\":\"FORBIDDEN\",\"message\":\"Increase your quota or upgrade to create "
                + "more indexes. You've reached the max pods allowed in project Default (5).\"},\"status\":403}";
        mock.stub("PATCH", "/indexes/pod-idx", 403, error);

        PineconeForbiddenException thrown = assertThrows(PineconeForbiddenException.class,
                () -> mock.client().configurePodsIndex("pod-idx", 30, "disabled"));

        assertEquals(error, thrown.getMessage());
        assertEquals(403, assertInstanceOf(ApiException.class, thrown.getCause()).getCode());
    }
}
