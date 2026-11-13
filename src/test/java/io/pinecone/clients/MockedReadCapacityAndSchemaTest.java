package io.pinecone.clients;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.pinecone.helpers.MockPinecone;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openapitools.db_control.client.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Offline counterparts of the live serverless ReadCapacityAndSchemaTest: each case sends its create or configure call
 * through the public client to {@link MockPinecone}, then checks the JSON that went over the wire and how the client
 * parsed the read capacity and schema in the response.
 */
public class MockedReadCapacityAndSchemaTest {
    @RegisterExtension
    final MockPinecone mock = new MockPinecone();

    private static final String ON_DEMAND_RESPONSE =
            "{\"mode\":\"OnDemand\",\"status\":{\"state\":\"Ready\"}}";

    private static String dedicatedResponse(int shards, int replicas) {
        return "{\"mode\":\"Dedicated\","
                + "\"dedicated\":{\"node_type\":\"t1\",\"scaling\":\"Manual\","
                + "\"manual\":{\"shards\":" + shards + ",\"replicas\":" + replicas + "}},"
                + "\"status\":{\"state\":\"Scaling\",\"current_shards\":1,\"current_replicas\":1}}";
    }

    private static String indexResponse(String name, String readCapacity, String schema, String embed) {
        return "{"
                + "\"name\":\"" + name + "\","
                + "\"dimension\":1536,"
                + "\"metric\":\"cosine\","
                + "\"host\":\"" + name + "-abc.svc.pinecone.io\","
                + "\"deletion_protection\":\"disabled\","
                + "\"tags\":{\"env\":\"test\"},"
                + (embed == null ? "" : "\"embed\":" + embed + ",")
                + "\"spec\":{\"serverless\":{\"cloud\":\"aws\",\"region\":\"us-west-2\","
                + "\"read_capacity\":" + readCapacity
                + (schema == null ? "" : ",\"schema\":" + schema)
                + "}},"
                + "\"status\":{\"ready\":false,\"state\":\"Initializing\"},"
                + "\"vector_type\":\"dense\""
                + "}";
    }

    private static Map<String, String> envTag() {
        Map<String, String> tags = new HashMap<>();
        tags.put("env", "test");
        return tags;
    }

    private static ReadCapacity dedicated(int shards, int replicas) {
        ReadCapacityDedicatedConfig config = new ReadCapacityDedicatedConfig()
                .nodeType("t1")
                .scaling("Manual")
                .manual(new ScalingConfigManual().shards(shards).replicas(replicas));
        return new ReadCapacity(new ReadCapacityDedicatedSpec().mode("Dedicated").dedicated(config));
    }

    private static BackupModelSchema filterable(String... fieldNames) {
        Map<String, BackupModelSchemaFieldsValue> fields = new HashMap<>();
        for (String field : fieldNames) {
            fields.put(field, new BackupModelSchemaFieldsValue().filterable(true));
        }
        return new BackupModelSchema().fields(fields);
    }

    private JsonObject onlyRequestBody(String method, String path) {
        List<RecordedRequest> requests = mock.restRequests();
        assertEquals(1, requests.size());
        RecordedRequest request = requests.get(0);
        assertEquals(method, request.getMethod());
        assertEquals(path, request.getPath());
        assertEquals("2025-10", request.getHeader("X-Pinecone-Api-Version"));
        return JsonParser.parseString(request.getBody().readUtf8()).getAsJsonObject();
    }

    private static void assertDedicatedRequest(JsonObject readCapacity, int shards, int replicas) {
        assertEquals("Dedicated", readCapacity.get("mode").getAsString());
        JsonObject dedicated = readCapacity.getAsJsonObject("dedicated");
        assertEquals("t1", dedicated.get("node_type").getAsString());
        assertEquals("Manual", dedicated.get("scaling").getAsString());
        assertEquals(shards, dedicated.getAsJsonObject("manual").get("shards").getAsInt());
        assertEquals(replicas, dedicated.getAsJsonObject("manual").get("replicas").getAsInt());
    }

    private static void assertFilterableFields(JsonObject schema, String... fieldNames) {
        JsonObject fields = schema.getAsJsonObject("fields");
        assertEquals(fieldNames.length, fields.size());
        for (String field : fieldNames) {
            assertTrue(fields.getAsJsonObject(field).get("filterable").getAsBoolean(), field);
        }
    }

    private static ServerlessSpecResponse serverless(IndexModel index) {
        return index.getSpec().getIndexModelServerless().getServerless();
    }

    private static void assertDedicatedResponse(ReadCapacityResponse readCapacity, int shards, int replicas) {
        ReadCapacityDedicatedSpecResponse dedicated = readCapacity.getReadCapacityDedicatedSpecResponse();
        assertEquals("Dedicated", dedicated.getMode());
        assertEquals("t1", dedicated.getDedicated().getNodeType());
        assertEquals("Manual", dedicated.getDedicated().getScaling());
        assertEquals(shards, dedicated.getDedicated().getManual().getShards());
        assertEquals(replicas, dedicated.getDedicated().getManual().getReplicas());
        assertEquals("Scaling", dedicated.getStatus().getState());
        assertEquals(1, dedicated.getStatus().getCurrentShards());
        assertEquals(1, dedicated.getStatus().getCurrentReplicas());
    }

    private static void assertFilterableFields(BackupModelSchema schema, String... fieldNames) {
        assertEquals(fieldNames.length, schema.getFields().size());
        for (String field : fieldNames) {
            assertTrue(schema.getFields().get(field).getFilterable(), field);
        }
    }

    @Test
    public void createServerlessIndexWithOnDemandReadCapacity() {
        mock.stub("POST", "/indexes", 201, indexResponse("ondemand", ON_DEMAND_RESPONSE, null, null));

        ReadCapacity readCapacity = new ReadCapacity(new ReadCapacityOnDemandSpec().mode("OnDemand"));
        IndexModel index = mock.client().createServerlessIndex(
                "ondemand", "cosine", 1536, "aws", "us-west-2", "disabled", envTag(), readCapacity, null);

        JsonObject body = onlyRequestBody("POST", "/indexes");
        assertEquals("ondemand", body.get("name").getAsString());
        assertEquals("cosine", body.get("metric").getAsString());
        assertEquals(1536, body.get("dimension").getAsInt());
        assertEquals("disabled", body.get("deletion_protection").getAsString());
        assertEquals("test", body.getAsJsonObject("tags").get("env").getAsString());
        JsonObject spec = body.getAsJsonObject("spec").getAsJsonObject("serverless");
        assertEquals("aws", spec.get("cloud").getAsString());
        assertEquals("us-west-2", spec.get("region").getAsString());
        JsonObject sentReadCapacity = spec.getAsJsonObject("read_capacity");
        assertEquals("OnDemand", sentReadCapacity.get("mode").getAsString());
        assertFalse(sentReadCapacity.has("dedicated"));
        assertFalse(spec.has("schema"));

        assertEquals("ondemand", index.getName());
        assertEquals(envTag(), index.getTags());
        ReadCapacityOnDemandSpecResponse parsed = serverless(index).getReadCapacity().getReadCapacityOnDemandSpecResponse();
        assertEquals("OnDemand", parsed.getMode());
        assertEquals("Ready", parsed.getStatus().getState());
        assertNull(serverless(index).getSchema());
    }

    @Test
    public void createServerlessIndexWithDedicatedReadCapacity() {
        mock.stub("POST", "/indexes", 201, indexResponse("dedicated", dedicatedResponse(2, 2), null, null));

        IndexModel index = mock.client().createServerlessIndex(
                "dedicated", "cosine", 1536, "aws", "us-west-2", "disabled", envTag(), dedicated(2, 2), null);

        JsonObject spec = onlyRequestBody("POST", "/indexes").getAsJsonObject("spec").getAsJsonObject("serverless");
        assertDedicatedRequest(spec.getAsJsonObject("read_capacity"), 2, 2);
        assertFalse(spec.has("schema"));

        assertDedicatedResponse(serverless(index).getReadCapacity(), 2, 2);
    }

    @Test
    public void createServerlessIndexWithMetadataSchema() {
        String schemaJson = "{\"fields\":{\"genre\":{\"filterable\":true},\"year\":{\"filterable\":true},"
                + "\"description\":{\"filterable\":true}}}";
        mock.stub("POST", "/indexes", 201, indexResponse("schema", ON_DEMAND_RESPONSE, schemaJson, null));

        IndexModel index = mock.client().createServerlessIndex(
                "schema", "cosine", 1536, "aws", "us-west-2", "disabled", envTag(), null,
                filterable("genre", "year", "description"));

        JsonObject spec = onlyRequestBody("POST", "/indexes").getAsJsonObject("spec").getAsJsonObject("serverless");
        assertFalse(spec.has("read_capacity"));
        assertFilterableFields(spec.getAsJsonObject("schema"), "genre", "year", "description");

        assertFilterableFields(serverless(index).getSchema(), "genre", "year", "description");
    }

    @Test
    public void createServerlessIndexWithBothReadCapacityAndSchema() {
        String schemaJson = "{\"fields\":{\"category\":{\"filterable\":true},\"tags\":{\"filterable\":true}}}";
        mock.stub("POST", "/indexes", 201, indexResponse("both", dedicatedResponse(1, 1), schemaJson, null));

        IndexModel index = mock.client().createServerlessIndex(
                "both", "cosine", 1536, "aws", "us-west-2", "disabled", envTag(), dedicated(1, 1),
                filterable("category", "tags"));

        JsonObject spec = onlyRequestBody("POST", "/indexes").getAsJsonObject("spec").getAsJsonObject("serverless");
        assertDedicatedRequest(spec.getAsJsonObject("read_capacity"), 1, 1);
        assertFilterableFields(spec.getAsJsonObject("schema"), "category", "tags");

        assertDedicatedResponse(serverless(index).getReadCapacity(), 1, 1);
        assertFilterableFields(serverless(index).getSchema(), "category", "tags");
    }

    @Test
    public void createIndexForModelWithReadCapacityAndSchema() throws Exception {
        String schemaJson = "{\"fields\":{\"category\":{\"filterable\":true}}}";
        String embedJson = "{\"model\":\"multilingual-e5-large\",\"metric\":\"cosine\",\"dimension\":1024,"
                + "\"vector_type\":\"dense\",\"field_map\":{\"text\":\"my-sample-text\"}}";
        mock.stub("POST", "/indexes/create-for-model", 201,
                indexResponse("model", dedicatedResponse(1, 1), schemaJson, embedJson));

        CreateIndexForModelRequestEmbed embed = new CreateIndexForModelRequestEmbed();
        embed.model("multilingual-e5-large");
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("text", "my-sample-text");
        embed.fieldMap(fieldMap);
        IndexModel index = mock.client().createIndexForModel(
                "model", "aws", "us-east-1", embed, "disabled", envTag(), dedicated(1, 1), filterable("category"));

        JsonObject body = onlyRequestBody("POST", "/indexes/create-for-model");
        assertEquals("model", body.get("name").getAsString());
        assertEquals("aws", body.get("cloud").getAsString());
        assertEquals("us-east-1", body.get("region").getAsString());
        assertEquals("disabled", body.get("deletion_protection").getAsString());
        assertEquals("test", body.getAsJsonObject("tags").get("env").getAsString());
        JsonObject sentEmbed = body.getAsJsonObject("embed");
        assertEquals("multilingual-e5-large", sentEmbed.get("model").getAsString());
        assertEquals("my-sample-text", sentEmbed.getAsJsonObject("field_map").get("text").getAsString());
        assertDedicatedRequest(body.getAsJsonObject("read_capacity"), 1, 1);
        assertFilterableFields(body.getAsJsonObject("schema"), "category");

        assertEquals("model", index.getName());
        assertEquals("multilingual-e5-large", index.getEmbed().getModel());
        assertDedicatedResponse(serverless(index).getReadCapacity(), 1, 1);
        assertFilterableFields(serverless(index).getSchema(), "category");
    }

    @Test
    public void configureReadCapacityOnExistingIndex() {
        mock.stub("PATCH", "/indexes/configure", 202,
                indexResponse("configure", dedicatedResponse(2, 2), null, null));

        IndexModel index = mock.client().configureServerlessIndex(
                "configure", "disabled", envTag(), null, "Dedicated", "t1", 2, 2);

        JsonObject body = onlyRequestBody("PATCH", "/indexes/configure");
        assertEquals("disabled", body.get("deletion_protection").getAsString());
        assertEquals("test", body.getAsJsonObject("tags").get("env").getAsString());
        assertFalse(body.has("embed"));
        assertDedicatedRequest(body.getAsJsonObject("spec").getAsJsonObject("serverless")
                .getAsJsonObject("read_capacity"), 2, 2);

        assertDedicatedResponse(serverless(index).getReadCapacity(), 2, 2);
    }

    @Test
    public void configureReadCapacityWithNullDeletionProtectionOmitsIt() {
        mock.stub("PATCH", "/indexes/configure", 202,
                indexResponse("configure", dedicatedResponse(3, 2), null, null));

        mock.client().configureServerlessIndex("configure", null, null, null, "Dedicated", "t1", 3, 2);

        JsonObject body = onlyRequestBody("PATCH", "/indexes/configure");
        assertFalse(body.has("deletion_protection"));
        assertDedicatedRequest(body.getAsJsonObject("spec").getAsJsonObject("serverless")
                .getAsJsonObject("read_capacity"), 3, 2);
    }

    @Test
    public void configureReadCapacityToOnDemand() {
        mock.stub("PATCH", "/indexes/configure", 202, indexResponse("configure", ON_DEMAND_RESPONSE, null, null));

        IndexModel index = mock.client().configureServerlessIndex(
                "configure", "disabled", null, null, "OnDemand", null, null, null);

        JsonObject body = onlyRequestBody("PATCH", "/indexes/configure");
        assertEquals(0, body.getAsJsonObject("tags").size());
        JsonObject sentReadCapacity = body.getAsJsonObject("spec").getAsJsonObject("serverless")
                .getAsJsonObject("read_capacity");
        assertEquals("OnDemand", sentReadCapacity.get("mode").getAsString());
        assertFalse(sentReadCapacity.has("dedicated"));

        assertEquals("OnDemand", serverless(index).getReadCapacity().getReadCapacityOnDemandSpecResponse().getMode());
    }
}
