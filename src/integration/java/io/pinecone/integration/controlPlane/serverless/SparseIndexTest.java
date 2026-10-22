package io.pinecone.integration.controlPlane.serverless;

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.helpers.CreatedIndexes;
import io.pinecone.helpers.Poll;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.UpsertResponse;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import org.junit.jupiter.api.*;
import org.openapitools.db_control.client.model.IndexModel;

import java.util.*;

import static io.pinecone.helpers.ResourceCleanup.waitUntilIndexIsDeleted;
import static io.pinecone.helpers.TestUtilities.waitUntilIndexIsReady;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SparseIndexTest {
    static String indexName;
    static Pinecone pinecone;
    static CreatedIndexes createdIndexes;

    @BeforeAll
    public static void setUp() throws InterruptedException {
        pinecone = new Pinecone
                .Builder(System.getenv("PINECONE_API_KEY"))
                .withSourceTag("pinecone_test")
                .build();
        createdIndexes = new CreatedIndexes(pinecone);
        indexName = createdIndexes.register(RandomStringBuilder.buildResourceName("sparse-index", 8));
    }

    @AfterAll
    public static void deleteCreatedIndexes() {
        createdIndexes.deleteAll();
    }

    @Test
    @Order(1)
    public void createSparseIndex() {
        Map<String, String> tags = new HashMap<>();
        tags.put("env", "test");

        // Create sparse Index
        IndexModel indexModel = pinecone.createSparseServelessIndex(indexName,
                "aws",
                "us-east-1",
                "enabled",
                tags,
                "sparse");

        assertNotNull(indexModel);
        assertEquals(indexName, indexModel.getName());
        assertEquals("dotproduct", indexModel.getMetric());
        assertEquals(indexModel.getDeletionProtection(), "enabled");
        assertEquals(indexModel.getTags(), tags);
        assertEquals(indexModel.getVectorType(), "sparse");
    }

    @Test
    @Order(2)
    public void configureSparseIndex() throws InterruptedException {
        String key = "flag";
        String value = "internal";
        Map<String, String> tags = new HashMap<>();
        tags.put(key, value);

        // Wait until index is ready
        waitUntilIndexIsReady(pinecone, indexName, 200000);

        // Disable deletion protection and add more index tags
        pinecone.configureServerlessIndex(indexName, "disabled", tags, null);

        // Describe index to confirm deletion protection is disabled
        IndexModel indexModel = Poll.until("index " + indexName + " shows the configure changes", 60000, 1000,
                () -> pinecone.describeIndex(indexName),
                model -> "disabled".equals(model.getDeletionProtection())
                        && model.getTags() != null && value.equals(model.getTags().get(key)));
        assertEquals(indexModel.getDeletionProtection(), "disabled");
        assert indexModel.getTags() != null;
        assertEquals(indexModel.getTags().get(key), value);
    }

    @Disabled
    // @Order(3)
    public void upsertAndQueryVectors() {
        Index index = pinecone.getIndexConnection(indexName);
        String id = "v1";
        ArrayList<Long> indices = new ArrayList<>();
        indices.add(1L);
        indices.add(2L);

        ArrayList<Float> values = new ArrayList<>();
        values.add(1f);
        values.add(2f);

        UpsertResponse upsertResponse = index.upsert("v1", Collections.emptyList(), indices, values, null, "");
        assertEquals(1, upsertResponse.getUpsertedCount());

        // Query by vector id
        QueryResponseWithUnsignedIndices queryResponse = index.queryByVectorId(1, id, true, false);
        assertEquals(1, queryResponse.getMatchesList().size());
        assertEquals(id, queryResponse.getMatches(0).getId());
        assertEquals(queryResponse.getMatches(0).getSparseValuesWithUnsignedIndices().getIndicesWithUnsigned32IntList(), indices);
        assertEquals(queryResponse.getMatches(0).getSparseValuesWithUnsignedIndices().getValuesList(), values);
    }

    @Test
    @Order(4)
    public void deleteSparseIndex() throws InterruptedException {
        // Delete sparse index
        pinecone.deleteIndex(indexName);

        // Confirm the index is deleted by polling describe index until it returns resource not found
        waitUntilIndexIsDeleted(pinecone, indexName);
    }
}
