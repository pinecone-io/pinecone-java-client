package io.pinecone.integration.controlPlane.serverless;

import io.pinecone.clients.Pinecone;
import io.pinecone.helpers.CreatedIndexes;
import io.pinecone.helpers.RandomStringBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openapitools.db_control.client.model.IndexModel;

import java.util.HashMap;
import java.util.Map;

public class DeletionProtectionTest {
    private static final Pinecone controlPlaneClient = new Pinecone
            .Builder(System.getenv("PINECONE_API_KEY"))
            .withSourceTag("pinecone_test")
            .build();
    private static final CreatedIndexes createdIndexes = new CreatedIndexes(controlPlaneClient);

    @AfterAll
    public static void deleteCreatedIndexes() {
        createdIndexes.deleteAll();
    }

    @Test
    public void createIndexWithDeletionProtectionEnabled() {
        String indexName = createdIndexes.register(RandomStringBuilder.buildResourceName("create-serv", 8));
        HashMap<String, String> expectedTags = new HashMap<>();
        expectedTags.put("test", "deletion-protection-enabled");
        // Create serverless index with deletion protection enabled
        controlPlaneClient.createServerlessIndex(indexName, "cosine", 3, "aws", "us-west-2", "enabled", expectedTags);
        // Describe index to verify deletion protection is enabled
        IndexModel indexModel = controlPlaneClient.describeIndex(indexName);
        String deletionProtection = indexModel.getDeletionProtection();
        Assertions.assertEquals(deletionProtection, "enabled");
        Map<String, String> actualTags = indexModel.getTags();
        Assertions.assertEquals(expectedTags, actualTags);
    }

    @Test
    public void createPodIndexWithDeletionProtectionDisabled() {
        String indexName = createdIndexes.register(RandomStringBuilder.buildResourceName("create-pod", 8));
        HashMap<String, String> expectedTags = new HashMap<>();
        expectedTags.put("test", "deletion-protection-disabled");
        // Create serverless index with deletion protection disabled
        controlPlaneClient.createServerlessIndex(indexName, "cosine", 3, "aws", "us-west-2", "disabled", expectedTags);
        IndexModel indexModel = controlPlaneClient.describeIndex(indexName);
        String deletionProtection = indexModel.getDeletionProtection();
        Assertions.assertEquals("disabled", deletionProtection);
        Map<String, String> actualTags = indexModel.getTags();
        Assertions.assertEquals(expectedTags, actualTags);
        // Configure index to enable deletionProtection
        controlPlaneClient.configureServerlessIndex(indexName, "enabled", expectedTags, null);
        indexModel = controlPlaneClient.describeIndex(indexName);
        deletionProtection = indexModel.getDeletionProtection();
        Assertions.assertEquals("enabled", deletionProtection);
        // Configure index to disable deletionProtection
        controlPlaneClient.configureServerlessIndex(indexName, "disabled", expectedTags, null);
    }
}
