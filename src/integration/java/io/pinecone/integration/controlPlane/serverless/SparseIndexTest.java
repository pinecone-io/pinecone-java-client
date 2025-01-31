package io.pinecone.integration.controlPlane.serverless;

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeNotFoundException;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.UpsertResponse;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import org.junit.jupiter.api.*;
import org.openapitools.db_control.client.model.DeletionProtection;
import org.openapitools.db_control.client.model.IndexModel;

import java.util.*;

import static io.pinecone.helpers.TestUtilities.waitUntilIndexIsReady;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SparseIndexTest {
    String indexName = RandomStringBuilder.build("sparse-index", 8);
    Pinecone pinecone = new Pinecone
            .Builder(System.getenv("PINECONE_API_KEY"))
            .withSourceTag("pinecone_test")
            .build();

    @Test
    @Order(1)
    public void createSparseIndex() {
        Map<String, String> tags = new HashMap<>();
        tags.put("env", "test");

        // Create sparse Index
        IndexModel indexModel = pinecone.createSparseServelessIndex(indexName,
                "aws",
                "us-east-1",
                DeletionProtection.ENABLED,
                tags,
                "sparse");

        assertNotNull(indexModel);
        assertEquals(indexName, indexModel.getName());
        assertEquals(IndexModel.MetricEnum.DOTPRODUCT, indexModel.getMetric());
        assertEquals(indexModel.getDeletionProtection(), DeletionProtection.ENABLED);
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
        pinecone.configureServerlessIndex(indexName, DeletionProtection.DISABLED, tags);
        Thread.sleep(7000);

        // Describe index to confirm deletion protection is disabled
        IndexModel indexModel = pinecone.describeIndex(indexName);
        assertEquals(indexModel.getDeletionProtection(), DeletionProtection.DISABLED);
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
        assertEquals(upsertResponse.getUpsertedCount(), 1);

        // Query by vector id
        QueryResponseWithUnsignedIndices queryResponse = index.queryByVectorId(1, id, true, false);
        assertEquals(queryResponse.getMatchesList().size(), 1);
        assertEquals(queryResponse.getMatches(0).getId(), id);
        assertEquals(queryResponse.getMatches(0).getSparseValuesWithUnsignedIndices().getIndicesWithUnsigned32IntList(), indices);
        assertEquals(queryResponse.getMatches(0).getSparseValuesWithUnsignedIndices().getValuesList(), values);
    }

    @Test
    @Order(4)
    public void deleteSparseIndex() throws InterruptedException {
        // Delete sparse index
        pinecone.deleteIndex(indexName);
        Thread.sleep(5000);

        // Confirm the index is deleted by calling describe index which should return resource not found
        assertThrows(PineconeNotFoundException.class, () -> pinecone.describeIndex(indexName));
    }
}
