package io.pinecone.integration.controlPlane.serverless;

import io.pinecone.clients.Pinecone;
import io.pinecone.helpers.CreatedIndexes;
import io.pinecone.helpers.RandomStringBuilder;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.*;
import org.openapitools.db_control.client.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The one live read-capacity and schema check: the backend accepts a Dedicated read capacity plus a metadata schema
 * and echoes both back. Every other request shape (OnDemand, Dedicated alone, schema alone, create-for-model,
 * configure) is checked offline in MockedReadCapacityAndSchemaTest, so the live suite creates one index with one
 * dedicated read node here instead of six indexes with up to ten.
 */
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

    private static final CreatedIndexes createdIndexes = new CreatedIndexes(controlPlaneClient);

    @AfterAll
    static void cleanUp() {
        createdIndexes.deleteAll();
    }

    @Test
    public void createServerlessIndexWithBothReadCapacityAndSchema() {
        String indexName = createdIndexes.register(RandomStringBuilder.buildResourceName("both-config-index", 8));
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

        assertNotNull(indexModel);
        assertEquals(indexName, indexModel.getName());
        assertEquals("cosine", indexModel.getMetric());
        assertEquals(1536, indexModel.getDimension());

        ServerlessSpecResponse spec = indexModel.getSpec().getIndexModelServerless().getServerless();
        ReadCapacityDedicatedSpecResponse parsedReadCapacity = spec.getReadCapacity().getReadCapacityDedicatedSpecResponse();
        assertEquals("Dedicated", parsedReadCapacity.getMode());
        assertEquals("t1", parsedReadCapacity.getDedicated().getNodeType());
        assertEquals(1, parsedReadCapacity.getDedicated().getManual().getShards());
        assertEquals(1, parsedReadCapacity.getDedicated().getManual().getReplicas());
        assertEquals(fields.keySet(), spec.getSchema().getFields().keySet());
    }

}
