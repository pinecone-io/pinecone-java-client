package io.pinecone.integration.controlPlane.serverless;

import io.pinecone.clients.Pinecone;
import io.pinecone.helpers.RandomStringBuilder;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.*;
import org.openapitools.db_control.client.ApiException;
import org.openapitools.db_control.client.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.pinecone.helpers.TestUtilities.waitUntilIndexIsReady;
import static io.pinecone.helpers.TestUtilities.waitUntilReadCapacityIsReady;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
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

    @Test
    @Order(1)
    public void createServerlessIndexWithOnDemandReadCapacity() throws InterruptedException {
        String indexNameOnDemand = RandomStringBuilder.build("ondemand-index", 8);
        Map<String, String> tags = new HashMap<>();
        tags.put("env", "test");
        tags.put("read-capacity", "ondemand");

        // Create index with OnDemand read capacity
        ReadCapacity readCapacity = new ReadCapacity(new ReadCapacityOnDemandSpec().mode("OnDemand"));
        try {
            IndexModel indexModel = controlPlaneClient.createServerlessIndex(
                    indexNameOnDemand, "cosine", 1536, "aws", "us-west-2",
                    "disabled", tags, readCapacity, null);

            assertNotNull(indexModel);
            assertEquals(indexNameOnDemand, indexModel.getName());
            assertEquals("cosine", indexModel.getMetric());
            assertEquals(1536, indexModel.getDimension());
            assertEquals("disabled", indexModel.getDeletionProtection());
            assertEquals(tags, indexModel.getTags());

            // Wait for index to be ready and verify read capacity
            waitUntilIndexIsReady(controlPlaneClient, indexNameOnDemand);
            IndexModel describedIndex = controlPlaneClient.describeIndex(indexNameOnDemand);
            assertNotNull(describedIndex.getSpec().getIndexModelServerless());
            // Note: Read capacity response may not be immediately available in describe
        } finally {
            controlPlaneClient.deleteIndex(indexNameOnDemand);
        }
    }

    @Test
    @Order(2)
    public void createServerlessIndexWithDedicatedReadCapacity() throws InterruptedException {
        String indexNameDedicated = RandomStringBuilder.build("dedicated-index", 8);
        Map<String, String> tags = new HashMap<>();
        tags.put("env", "test");
        tags.put("read-capacity", "dedicated");

        // Create index with Dedicated read capacity
        ScalingConfigManual manual = new ScalingConfigManual().shards(1).replicas(1);
        ReadCapacityDedicatedConfig dedicated = new ReadCapacityDedicatedConfig()
                .nodeType("t1")
                .scaling("Manual")
                .manual(manual);
        ReadCapacity readCapacity = new ReadCapacity(
                new ReadCapacityDedicatedSpec().mode("Dedicated").dedicated(dedicated));

        try {
            IndexModel indexModel = controlPlaneClient.createServerlessIndex(
                    indexNameDedicated, "cosine", 1536, "aws", "us-west-2",
                    "disabled", tags, readCapacity, null);

            assertNotNull(indexModel);
            assertEquals(indexNameDedicated, indexModel.getName());
            assertEquals("cosine", indexModel.getMetric());
            assertEquals(1536, indexModel.getDimension());
            assertEquals("disabled", indexModel.getDeletionProtection());
            assertEquals(tags, indexModel.getTags());

            // Wait for index to be ready
            waitUntilIndexIsReady(controlPlaneClient, indexNameDedicated);
        } catch (Exception e) {
            assumeTrue(!isDedicatedReadCapacityQuotaExceeded(e), dedicatedReadCapacityQuotaExceededMessage(e));
            throw e;
        } finally {
            controlPlaneClient.deleteIndex(indexNameDedicated);
        }
    }

    @Test
    @Order(3)
    public void createServerlessIndexWithMetadataSchema() throws InterruptedException {
        String indexNameWithSchema = RandomStringBuilder.build("schema-index", 8);
        Map<String, String> tags = new HashMap<>();
        tags.put("env", "test");
        tags.put("schema", "configured");

        // Create index with metadata schema
        Map<String, BackupModelSchemaFieldsValue> fields = new HashMap<>();
        fields.put("genre", new BackupModelSchemaFieldsValue().filterable(true));
        fields.put("year", new BackupModelSchemaFieldsValue().filterable(true));
        fields.put("description", new BackupModelSchemaFieldsValue().filterable(true));
        BackupModelSchema schema = new BackupModelSchema().fields(fields);

        try {
            IndexModel indexModel = controlPlaneClient.createServerlessIndex(
                    indexNameWithSchema, "cosine", 1536, "aws", "us-west-2",
                    "disabled", tags, null, schema);

            assertNotNull(indexModel);
            assertEquals(indexNameWithSchema, indexModel.getName());
            assertEquals("cosine", indexModel.getMetric());
            assertEquals(1536, indexModel.getDimension());
            assertEquals("disabled", indexModel.getDeletionProtection());
            assertEquals(tags, indexModel.getTags());

            // Wait for index to be ready
            waitUntilIndexIsReady(controlPlaneClient, indexNameWithSchema);
        } finally {
            controlPlaneClient.deleteIndex(indexNameWithSchema);
        }
    }

    @Test
    @Order(4)
    public void createServerlessIndexWithBothReadCapacityAndSchema() throws InterruptedException {
        String indexName = RandomStringBuilder.build("both-config-index", 8);
        Map<String, String> tags = new HashMap<>();
        tags.put("env", "test");

        // Create index with both Dedicated read capacity and metadata schema
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

        try {
            IndexModel indexModel = controlPlaneClient.createServerlessIndex(
                    indexName, "cosine", 1536, "aws", "us-west-2",
                    "disabled", tags, readCapacity, schema);

            assertNotNull(indexModel);
            assertEquals(indexName, indexModel.getName());
            assertEquals("cosine", indexModel.getMetric());
            assertEquals(1536, indexModel.getDimension());

            // Wait for index to be ready
            waitUntilIndexIsReady(controlPlaneClient, indexName);
        } catch (Exception e) {
            assumeTrue(!isDedicatedReadCapacityQuotaExceeded(e), dedicatedReadCapacityQuotaExceededMessage(e));
            throw e;
        } finally {
            controlPlaneClient.deleteIndex(indexName);
        }
    }

    @Test
    @Order(5)
    public void createIndexForModelWithReadCapacityAndSchema() throws InterruptedException, ApiException {
        String indexNameForModel = RandomStringBuilder.build("model-index", 8);
        Map<String, String> tags = new HashMap<>();
        tags.put("env", "test");

        // Create index for model with Dedicated read capacity and metadata schema
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

        try {
            IndexModel indexModel = controlPlaneClient.createIndexForModel(
                    indexNameForModel, "aws", "us-east-1", embed,
                    "disabled", tags, readCapacity, schema);

            assertNotNull(indexModel);
            assertEquals(indexNameForModel, indexModel.getName());
            assertEquals("disabled", indexModel.getDeletionProtection());
            assertEquals(tags, indexModel.getTags());

            // Wait for index to be ready
            waitUntilIndexIsReady(controlPlaneClient, indexNameForModel);
        } catch (ApiException e) {
            assumeTrue(!isDedicatedReadCapacityQuotaExceeded(e), dedicatedReadCapacityQuotaExceededMessage(e));
            throw e;
        } catch (RuntimeException e) {
            assumeTrue(!isDedicatedReadCapacityQuotaExceeded(e), dedicatedReadCapacityQuotaExceededMessage(e));
            throw e;
        } finally {
            controlPlaneClient.deleteIndex(indexNameForModel);
        }
    }

    @Test
    @Order(6)
    public void configureReadCapacityOnExistingIndex() throws InterruptedException {
        String indexNameToConfigure = RandomStringBuilder.build("configure-index", 8);
        Map<String, String> tags = new HashMap<>();
        tags.put("env", "test");

        // First, create an index without read capacity configuration (defaults to OnDemand)
        try {
            IndexModel indexModel = controlPlaneClient.createServerlessIndex(
                    indexNameToConfigure, "cosine", 1536, "aws", "us-west-2",
                    "disabled", tags, null, null);

            assertNotNull(indexModel);
            assertEquals(indexNameToConfigure, indexModel.getName());

            // Wait for index to be ready
            waitUntilIndexIsReady(controlPlaneClient, indexNameToConfigure);
            // Wait for read capacity to be ready before configuring
            waitUntilReadCapacityIsReady(controlPlaneClient, indexNameToConfigure);

            // Configure to Dedicated read capacity (minimal capacity for CI stability)
            IndexModel configuredIndex = controlPlaneClient.configureServerlessIndex(
                    indexNameToConfigure, "disabled", tags, null, "Dedicated", "t1", 1, 1);

            assertNotNull(configuredIndex);
            assertEquals(indexNameToConfigure, configuredIndex.getName());

            // Wait a bit for configuration to apply
            Thread.sleep(10000);

            // Verify the configuration by describing the index
            IndexModel describedIndex = controlPlaneClient.describeIndex(indexNameToConfigure);
            assertNotNull(describedIndex);
            assertEquals(indexNameToConfigure, describedIndex.getName());
        } catch (Exception e) {
            assumeTrue(!isDedicatedReadCapacityQuotaExceeded(e), dedicatedReadCapacityQuotaExceededMessage(e));
            throw e;
        } finally {
            controlPlaneClient.deleteIndex(indexNameToConfigure);
        }
    }

    // Note: Tests for switching read capacity modes and scaling are omitted due to API rate limits.
    // Read capacity settings can only be updated once per hour per index. The following scenarios
    // would require multiple configurations on the same index and would hit rate limits:
    // - Switching from Dedicated to OnDemand
    // - Scaling dedicated read capacity (changing shards/replicas)
    // These operations are still supported by the API, but cannot be tested in CI/CD due to rate limits.

    private static boolean isDedicatedReadCapacityQuotaExceeded(Throwable t) {
        String message = extractMessage(t);
        return message != null && message.contains("max dedicated capacity read nodes");
    }

    private static String dedicatedReadCapacityQuotaExceededMessage(Throwable t) {
        String message = extractMessage(t);
        return "Skipping test: dedicated read capacity quota exceeded. " + (message == null ? "" : message);
    }

    private static String extractMessage(Throwable t) {
        Throwable cur = t;
        while (cur != null) {
            if (cur.getMessage() != null && !cur.getMessage().isEmpty()) {
                return cur.getMessage();
            }
            cur = cur.getCause();
        }
        return null;
    }
}

