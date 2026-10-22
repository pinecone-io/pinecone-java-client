package io.pinecone.clients;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.pinecone.exceptions.PineconeAlreadyExistsException;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.helpers.MockPinecone;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openapitools.db_control.client.ApiException;
import org.openapitools.db_control.client.model.BackupModelSchema;
import org.openapitools.db_control.client.model.BackupModelSchemaFieldsValue;
import org.openapitools.db_control.client.model.ByocSpec;
import org.openapitools.db_control.client.model.IndexModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Offline coverage of {@link Pinecone#createByocIndex}: the JSON sent to {@code POST /indexes}, how the BYOC spec in
 * the response is parsed, argument validation, and error mapping. BYOC needs a provisioned environment, so there is no
 * live counterpart.
 */
public class MockedByocIndexTest {
    @RegisterExtension
    final MockPinecone mock = new MockPinecone();

    private static String byocIndexResponse(String name, String schema) {
        return "{"
                + "\"name\":\"" + name + "\","
                + "\"dimension\":1536,"
                + "\"metric\":\"dotproduct\","
                + "\"host\":\"" + name + "-abc.svc.byoc.pinecone.io\","
                + "\"deletion_protection\":\"enabled\","
                + "\"tags\":{\"env\":\"test\"},"
                + "\"spec\":{\"byoc\":{\"environment\":\"aws-us-east-1-b921\""
                + (schema == null ? "" : ",\"schema\":" + schema)
                + "}},"
                + "\"status\":{\"ready\":false,\"state\":\"Initializing\"},"
                + "\"vector_type\":\"dense\""
                + "}";
    }

    private JsonObject onlyCreateRequestBody() {
        List<RecordedRequest> requests = mock.restRequests();
        assertEquals(1, requests.size());
        RecordedRequest request = requests.get(0);
        assertEquals("POST", request.getMethod());
        assertEquals("/indexes", request.getPath());
        assertEquals("2025-10", request.getHeader("X-Pinecone-Api-Version"));
        assertEquals(MockPinecone.API_KEY, request.getHeader("Api-Key"));
        return JsonParser.parseString(request.getBody().readUtf8()).getAsJsonObject();
    }

    @Test
    public void minimalOverloadSendsByocSpecWithDeletionProtectionDisabled() {
        mock.stub("POST", "/indexes", 201, byocIndexResponse("byoc-min", null));

        IndexModel index = mock.client().createByocIndex("byoc-min", "dotproduct", 1536, "aws-us-east-1-b921");

        JsonObject body = onlyCreateRequestBody();
        assertEquals("byoc-min", body.get("name").getAsString());
        assertEquals("dotproduct", body.get("metric").getAsString());
        assertEquals(1536, body.get("dimension").getAsInt());
        assertEquals("disabled", body.get("deletion_protection").getAsString());
        JsonObject spec = body.getAsJsonObject("spec");
        assertEquals(1, spec.size());
        JsonObject byoc = spec.getAsJsonObject("byoc");
        assertEquals(1, byoc.size());
        assertEquals("aws-us-east-1-b921", byoc.get("environment").getAsString());

        assertEquals("byoc-min", index.getName());
        assertEquals("byoc-min-abc.svc.byoc.pinecone.io", index.getHost());
        assertEquals("dotproduct", index.getMetric());
        assertEquals(1536, index.getDimension());
        assertEquals("enabled", index.getDeletionProtection());
        assertEquals("test", index.getTags().get("env"));
        assertEquals("Initializing", index.getStatus().getState());
        ByocSpec byocSpec = index.getSpec().getIndexModelBYOC().getByoc();
        assertEquals("aws-us-east-1-b921", byocSpec.getEnvironment());
        assertNull(byocSpec.getSchema());
    }

    @Test
    public void fullOverloadSendsDeletionProtectionTagsAndSchema() {
        mock.stub("POST", "/indexes", 201, byocIndexResponse("byoc-full",
                "{\"fields\":{\"genre\":{\"filterable\":true},\"year\":{\"filterable\":true}}}"));
        Map<String, String> tags = new HashMap<>();
        tags.put("env", "test");
        Map<String, BackupModelSchemaFieldsValue> fields = new HashMap<>();
        fields.put("genre", new BackupModelSchemaFieldsValue().filterable(true));
        fields.put("year", new BackupModelSchemaFieldsValue().filterable(true));

        IndexModel index = mock.client().createByocIndex("byoc-full", "dotproduct", 1536, "aws-us-east-1-b921",
                "enabled", tags, new BackupModelSchema().fields(fields));

        JsonObject body = onlyCreateRequestBody();
        assertEquals("enabled", body.get("deletion_protection").getAsString());
        assertEquals(1, body.getAsJsonObject("tags").size());
        assertEquals("test", body.getAsJsonObject("tags").get("env").getAsString());
        JsonObject byoc = body.getAsJsonObject("spec").getAsJsonObject("byoc");
        assertEquals("aws-us-east-1-b921", byoc.get("environment").getAsString());
        JsonObject sentFields = byoc.getAsJsonObject("schema").getAsJsonObject("fields");
        assertEquals(2, sentFields.size());
        assertTrue(sentFields.getAsJsonObject("genre").get("filterable").getAsBoolean());
        assertTrue(sentFields.getAsJsonObject("year").get("filterable").getAsBoolean());

        BackupModelSchema schema = index.getSpec().getIndexModelBYOC().getByoc().getSchema();
        assertEquals(2, schema.getFields().size());
        assertTrue(schema.getFields().get("genre").getFilterable());
        assertTrue(schema.getFields().get("year").getFilterable());
    }

    @Test
    public void nullOrEmptyMetricDefaultsToCosineAndNoTagsSendEmptyMap() {
        mock.stub("POST", "/indexes", 201, byocIndexResponse("byoc-defaults", null));
        Pinecone client = mock.client();

        client.createByocIndex("byoc-defaults", null, 8, "env-1", "disabled", new HashMap<>(), null);
        client.createByocIndex("byoc-defaults", "", 8, "env-1", "disabled", null, null);

        List<RecordedRequest> requests = mock.restRequests();
        assertEquals(2, requests.size());
        for (RecordedRequest request : requests) {
            JsonObject body = JsonParser.parseString(request.getBody().readUtf8()).getAsJsonObject();
            assertEquals("cosine", body.get("metric").getAsString());
            assertEquals(0, body.getAsJsonObject("tags").size());
            assertFalse(body.getAsJsonObject("spec").getAsJsonObject("byoc").has("schema"));
        }
    }

    @Test
    public void invalidArgumentsAreRejectedBeforeAnyRequest() {
        Pinecone client = mock.client();

        assertEquals("Index name cannot be null or empty", assertThrows(PineconeValidationException.class,
                () -> client.createByocIndex(null, "cosine", 8, "env-1")).getMessage());
        assertEquals("Index name cannot be null or empty", assertThrows(PineconeValidationException.class,
                () -> client.createByocIndex("", "cosine", 8, "env-1")).getMessage());
        assertTrue(assertThrows(PineconeValidationException.class,
                () -> client.createByocIndex("byoc", "cosine", 0, "env-1")).getMessage()
                .startsWith("Dimension must be greater than 0"));
        assertEquals("Environment cannot be null or empty", assertThrows(PineconeValidationException.class,
                () -> client.createByocIndex("byoc", "cosine", 8, null)).getMessage());
        assertEquals("Environment cannot be null or empty", assertThrows(PineconeValidationException.class,
                () -> client.createByocIndex("byoc", "cosine", 8, "")).getMessage());

        assertEquals(0, mock.restRequests().size());
    }

    @Test
    public void conflictIsMappedToAlreadyExists() {
        String error = "{\"error\":{\"code\":\"ALREADY_EXISTS\",\"message\":\"Resource already exists\"},\"status\":409}";
        mock.stub("POST", "/indexes", 409, error);

        PineconeAlreadyExistsException thrown = assertThrows(PineconeAlreadyExistsException.class,
                () -> mock.client().createByocIndex("byoc-dup", "cosine", 8, "env-1"));

        assertEquals(error, thrown.getMessage());
        assertEquals(409, assertInstanceOf(ApiException.class, thrown.getCause()).getCode());
    }
}
