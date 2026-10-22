package io.pinecone.clients;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.pinecone.exceptions.PineconeBadRequestException;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.helpers.MockPinecone;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openapitools.db_control.client.ApiException;
import org.openapitools.db_control.client.model.IndexModel;
import org.openapitools.db_control.client.model.ServerlessSpecResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Offline coverage of {@link Pinecone#createSparseServelessIndex}: the JSON sent to {@code POST /indexes}, how the
 * sparse index in the response is parsed, argument validation, and error mapping.
 */
public class MockedSparseIndexTest {
    @RegisterExtension
    final MockPinecone mock = new MockPinecone();

    private static String sparseIndexResponse(String name) {
        return "{"
                + "\"name\":\"" + name + "\","
                + "\"metric\":\"dotproduct\","
                + "\"host\":\"" + name + "-abc.svc.aped-4627-b74a.pinecone.io\","
                + "\"deletion_protection\":\"enabled\","
                + "\"tags\":{\"env\":\"test\"},"
                + "\"spec\":{\"serverless\":{\"cloud\":\"aws\",\"region\":\"us-east-1\",\"read_capacity\":{\"mode\":\"OnDemand\",\"status\":{\"state\":\"Ready\"}}}},"
                + "\"status\":{\"ready\":false,\"state\":\"Initializing\"},"
                + "\"vector_type\":\"sparse\""
                + "}";
    }

    private static JsonObject createRequestBody(RecordedRequest request) {
        assertEquals("POST", request.getMethod());
        assertEquals("/indexes", request.getPath());
        assertEquals("2025-10", request.getHeader("X-Pinecone-Api-Version"));
        assertEquals(MockPinecone.API_KEY, request.getHeader("Api-Key"));
        return JsonParser.parseString(request.getBody().readUtf8()).getAsJsonObject();
    }

    @Test
    public void sendsDotproductSparseServerlessRequestWithoutDimension() {
        mock.stub("POST", "/indexes", 201, sparseIndexResponse("sparse-idx"));
        Map<String, String> tags = new HashMap<>();
        tags.put("env", "test");

        IndexModel index = mock.client().createSparseServelessIndex(
                "sparse-idx", "aws", "us-east-1", "enabled", tags, "sparse");

        List<RecordedRequest> requests = mock.restRequests();
        assertEquals(1, requests.size());
        JsonObject body = createRequestBody(requests.get(0));
        assertEquals("sparse-idx", body.get("name").getAsString());
        assertEquals("dotproduct", body.get("metric").getAsString());
        assertEquals("sparse", body.get("vector_type").getAsString());
        assertEquals("enabled", body.get("deletion_protection").getAsString());
        assertFalse(body.has("dimension"));
        assertEquals(1, body.getAsJsonObject("tags").size());
        assertEquals("test", body.getAsJsonObject("tags").get("env").getAsString());
        JsonObject spec = body.getAsJsonObject("spec");
        assertEquals(1, spec.size());
        JsonObject serverless = spec.getAsJsonObject("serverless");
        assertEquals(2, serverless.size());
        assertEquals("aws", serverless.get("cloud").getAsString());
        assertEquals("us-east-1", serverless.get("region").getAsString());

        assertEquals("sparse-idx", index.getName());
        assertEquals("sparse-idx-abc.svc.aped-4627-b74a.pinecone.io", index.getHost());
        assertEquals("dotproduct", index.getMetric());
        assertEquals("sparse", index.getVectorType());
        assertNull(index.getDimension());
        assertEquals("enabled", index.getDeletionProtection());
        assertEquals("test", index.getTags().get("env"));
        assertEquals("Initializing", index.getStatus().getState());
        ServerlessSpecResponse serverlessSpec = index.getSpec().getIndexModelServerless().getServerless();
        assertEquals("aws", serverlessSpec.getCloud());
        assertEquals("us-east-1", serverlessSpec.getRegion());
        assertEquals("OnDemand", serverlessSpec.getReadCapacity().getReadCapacityOnDemandSpecResponse().getMode());
    }

    @Test
    public void vectorTypeIsCheckedCaseInsensitivelyAndSentAsGiven() {
        mock.stub("POST", "/indexes", 201, sparseIndexResponse("sparse-idx"));
        Pinecone client = mock.client();

        client.createSparseServelessIndex("sparse-idx", "aws", "us-east-1", "disabled", null, "SPARSE");
        client.createSparseServelessIndex("sparse-idx", "aws", "us-east-1", "disabled", new HashMap<>(), "dense");

        List<RecordedRequest> requests = mock.restRequests();
        assertEquals(2, requests.size());
        JsonObject upper = createRequestBody(requests.get(0));
        assertEquals("SPARSE", upper.get("vector_type").getAsString());
        assertEquals("disabled", upper.get("deletion_protection").getAsString());
        assertEquals(0, upper.getAsJsonObject("tags").size());
        JsonObject dense = createRequestBody(requests.get(1));
        assertEquals("dense", dense.get("vector_type").getAsString());
        assertEquals("dotproduct", dense.get("metric").getAsString());
        assertEquals(0, dense.getAsJsonObject("tags").size());
    }

    @Test
    public void invalidArgumentsAreRejectedBeforeAnyRequest() {
        Pinecone client = mock.client();

        assertEquals("Index name cannot be null or empty", assertThrows(PineconeValidationException.class,
                () -> client.createSparseServelessIndex(null, "aws", "us-east-1", "disabled", null, "sparse"))
                .getMessage());
        assertEquals("Index name cannot be null or empty", assertThrows(PineconeValidationException.class,
                () -> client.createSparseServelessIndex("", "aws", "us-east-1", "disabled", null, "sparse"))
                .getMessage());
        assertEquals("Cloud cannot be null or empty", assertThrows(PineconeValidationException.class,
                () -> client.createSparseServelessIndex("sparse", null, "us-east-1", "disabled", null, "sparse"))
                .getMessage());
        assertEquals("Cloud cannot be null or empty", assertThrows(PineconeValidationException.class,
                () -> client.createSparseServelessIndex("sparse", "", "us-east-1", "disabled", null, "sparse"))
                .getMessage());
        assertEquals("Region cannot be null or empty", assertThrows(PineconeValidationException.class,
                () -> client.createSparseServelessIndex("sparse", "aws", null, "disabled", null, "sparse"))
                .getMessage());
        assertEquals("Region cannot be null or empty", assertThrows(PineconeValidationException.class,
                () -> client.createSparseServelessIndex("sparse", "aws", "", "disabled", null, "sparse"))
                .getMessage());
        assertEquals("vectorType must be sparse or dense", assertThrows(PineconeValidationException.class,
                () -> client.createSparseServelessIndex("sparse", "aws", "us-east-1", "disabled", null, "hybrid"))
                .getMessage());

        assertEquals(0, mock.restRequests().size());
    }

    @Test
    public void badRequestIsMappedToPineconeBadRequestException() {
        String error = "{\"error\":{\"code\":\"INVALID_ARGUMENT\",\"message\":\"Sparse vector indexes must use the metric dotproduct\"},\"status\":400}";
        mock.stub("POST", "/indexes", 400, error);

        PineconeBadRequestException thrown = assertThrows(PineconeBadRequestException.class,
                () -> mock.client().createSparseServelessIndex("sparse", "aws", "us-east-1", "disabled", null, "sparse"));

        assertEquals(error, thrown.getMessage());
        assertEquals(400, assertInstanceOf(ApiException.class, thrown.getCause()).getCode());
    }
}
