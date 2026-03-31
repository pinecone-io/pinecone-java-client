package io.pinecone.integration.controlPlane.serverless;

import io.pinecone.clients.Pinecone;
import io.pinecone.helpers.RandomStringBuilder;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.*;
import org.openapitools.db_control.client.ApiException;
import org.openapitools.db_control.client.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.pinecone.helpers.TestUtilities.waitUntilIndexIsReady;
import static io.pinecone.helpers.TestUtilities.waitUntilReadCapacityIsReady;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReadCapacityAndSchemaTest {
    private static final Pinecone controlPlaneClient = new Pinecone
            .Builder(System.getenv("PINECONE_API_KEY"))
            .withSourceTag("pinecone_test")
            .withOkHttpClient(new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build())
            .build();

    private static final List<String> createdIndexNames = new ArrayList<>();

    @AfterAll
    static void cleanUp() {
        for (String name : createdIndexNames) {
            try {
                controlPlaneClient.deleteIndex(name);
            } catch (Exception ignored) {
            }
        }
    }

    @Test
    @Order(1)
    public void createServerlessIndexWithOnDemandReadCapacity() {
        String indexName = RandomStringBuilder.build("ondemand-index", 8);
        Map<String, String> tags = new HashMap<>();
        tags.put("env", "test");
        tags.put("read-capacity", "ondemand");

        ReadCapacity readCapacity = new ReadCapacity(new ReadCapacityOnDemandSpec().mode("OnDemand"));
        IndexModel indexModel = controlPlaneClient.createServerlessIndex(
                indexName, "cosine", 1536, "aws", "us-west-2",
                "disabled", tags, readCapacity, null);
        createdIndexNames.add(indexName);

        assertNotNull(indexModel);
        assertEquals(indexName, indexModel.getName());
        assertEquals("cosine", indexModel.getMetric());
        assertEquals(1536, indexModel.getDimension());
        assertEquals("disabled", indexModel.getDeletionProtection());
        assertEquals(tags, indexModel.getTags());
    }

    @Test
    @Order(2)
    public void createServerlessIndexWithDedicatedReadCapacity() {
        String indexName = RandomStringBuilder.build("dedicated-index", 8);
        Map<String, String> tags = new HashMap<>();
        tags.put("env", "test");
        tags.put("read-capacity", "dedicated");

        ScalingConfigManual manual = new ScalingConfigManual().shards(2).replicas(2);
        ReadCapacityDedicatedConfig dedicated = new ReadCapacityDedicatedConfig()
                .nodeType("t1")
                .scaling("Manual")
                .manual(manual);
        ReadCapacity readCapacity = new ReadCapacity(
                new ReadCapacityDedicatedSpec().mode("Dedicated").dedicated(dedicated));

        IndexModel indexModel = controlPlaneClient.createServerlessIndex(
                indexName, "cosine", 1536, "aws", "us-west-2",
                "disabled", tags, readCapacity, null);
        createdIndexNames.add(indexName);

        assertNotNull(indexModel);
        assertEquals(indexName, indexModel.getName());
        assertEquals("cosine", indexModel.getMetric());
        assertEquals(1536, indexModel.getDimension());
        assertEquals("disabled", indexModel.getDeletionProtection());
        assertEquals(tags, indexModel.getTags());
    }

    @Test
    @Order(3)
    public void createServerlessIndexWithMetadataSchema() {
        String indexName = RandomStringBuilder.build("schema-index", 8);
        Map<String, String> tags = new HashMap<>();
        tags.put("env", "test");
        tags.put("schema", "configured");

        Map<String, BackupModelSchemaFieldsValue> fields = new HashMap<>();
        fields.put("genre", new BackupModelSchemaFieldsValue().filterable(true));
        fields.put("year", new BackupModelSchemaFieldsValue().filterable(true));
        fields.put("description", new BackupModelSchemaFieldsValue().filterable(true));
        BackupModelSchema schema = new BackupModelSchema().fields(fields);

        IndexModel indexModel = controlPlaneClient.createServerlessIndex(
                indexName, "cosine", 1536, "aws", "us-west-2",
                "disabled", tags, null, schema);
        createdIndexNames.add(indexName);

        assertNotNull(indexModel);
        assertEquals(indexName, indexModel.getName());
        assertEquals("cosine", indexModel.getMetric());
        assertEquals(1536, indexModel.getDimension());
        assertEquals("disabled", indexModel.getDeletionProtection());
        assertEquals(tags, indexModel.getTags());
    }

    @Test
    @Order(4)
    public void createServerlessIndexWithBothReadCapacityAndSchema() {
        String indexName = RandomStringBuilder.build("both-config-index", 8);
        Map<String, String> tags = new HashMap<>();
        tags.put("env", "test");

        ScalingConfigManual manual = new ScalingConfigManual().shards(1).replicas(1);
        ReadCapacityDedicatedConfig dedicated = new ReadCapacityDedicatedConfig()
                .nodeType("t1")
                .scaling("Manual")
                .manual(manual);
        ReadCapacity readCapacity = new ReadCapacity(
                new ReadCapacityDedicatedSpec().mode("Dedicated").dedicated(dedicated));

        Map<String, BackupModelSchemaFieldsValue> fields = new HashMap<>();
        fields.put("category", new BackupModelSchemaFieldsValue().filterable(true));
        fields.put("tags", new BackupModelSchemaFieldsValue().filterable(true));
        BackupModelSchema schema = new BackupModelSchema().fields(fields);

        IndexModel indexModel = controlPlaneClient.createServerlessIndex(
                indexName, "cosine", 1536, "aws", "us-west-2",
                "disabled", tags, readCapacity, schema);
        createdIndexNames.add(indexName);

        assertNotNull(indexModel);
        assertEquals(indexName, indexModel.getName());
        assertEquals("cosine", indexModel.getMetric());
        assertEquals(1536, indexModel.getDimension());
    }

    @Test
    @Order(5)
    public void createIndexForModelWithReadCapacityAndSchema() throws ApiException {
        String indexName = RandomStringBuilder.build("model-index", 8);
        Map<String, String> tags = new HashMap<>();
        tags.put("env", "test");

        ScalingConfigManual manual = new ScalingConfigManual().shards(1).replicas(1);
        ReadCapacityDedicatedConfig dedicated = new ReadCapacityDedicatedConfig()
                .nodeType("t1")
                .scaling("Manual")
                .manual(manual);
        ReadCapacity readCapacity = new ReadCapacity(
                new ReadCapacityDedicatedSpec().mode("Dedicated").dedicated(dedicated));

        Map<String, BackupModelSchemaFieldsValue> fields = new HashMap<>();
        fields.put("category", new BackupModelSchemaFieldsValue().filterable(true));
        BackupModelSchema schema = new BackupModelSchema().fields(fields);

        CreateIndexForModelRequestEmbed embed = new CreateIndexForModelRequestEmbed();
        embed.model("multilingual-e5-large");
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("text", "my-sample-text");
        embed.fieldMap(fieldMap);

        IndexModel indexModel = controlPlaneClient.createIndexForModel(
                indexName, "aws", "us-east-1", embed,
                "disabled", tags, readCapacity, schema);
        createdIndexNames.add(indexName);

        assertNotNull(indexModel);
        assertEquals(indexName, indexModel.getName());
        assertEquals("disabled", indexModel.getDeletionProtection());
        assertEquals(tags, indexModel.getTags());
    }

    @Test
    @Order(6)
    public void configureReadCapacityOnExistingIndex() throws InterruptedException {
        String indexName = RandomStringBuilder.build("configure-index", 8);
        Map<String, String> tags = new HashMap<>();
        tags.put("env", "test");

        IndexModel indexModel = controlPlaneClient.createServerlessIndex(
                indexName, "cosine", 1536, "aws", "us-west-2",
                "disabled", tags, null, null);
        createdIndexNames.add(indexName);

        assertNotNull(indexModel);
        assertEquals(indexName, indexModel.getName());

        // Must be ready before configuring read capacity
        waitUntilIndexIsReady(controlPlaneClient, indexName);
        waitUntilReadCapacityIsReady(controlPlaneClient, indexName);

        IndexModel configuredIndex = controlPlaneClient.configureServerlessIndex(
                indexName, "disabled", tags, null, "Dedicated", "t1", 2, 2);

        assertNotNull(configuredIndex);
        assertEquals(indexName, configuredIndex.getName());

        Thread.sleep(10000);

        IndexModel describedIndex = controlPlaneClient.describeIndex(indexName);
        assertNotNull(describedIndex);
        assertEquals(indexName, describedIndex.getName());
    }

    // Note: Tests for switching read capacity modes and scaling are omitted due to API rate limits.
    // Read capacity settings can only be updated once per hour per index. The following scenarios
    // would require multiple configurations on the same index and would hit rate limits:
    // - Switching from Dedicated to OnDemand
    // - Scaling dedicated read capacity (changing shards/replicas)
    // These operations are still supported by the API, but cannot be tested in CI/CD due to rate limits.
}
