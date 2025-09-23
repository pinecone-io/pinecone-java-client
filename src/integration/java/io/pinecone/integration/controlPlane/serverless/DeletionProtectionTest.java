package io.pinecone.integration.controlPlane.serverless;

import io.pinecone.clients.Pinecone;
import io.pinecone.helpers.RandomStringBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openapitools.db_control.client.model.DeletionProtection;
import org.openapitools.db_control.client.model.IndexModel;

import java.util.HashMap;
import java.util.Map;

public class DeletionProtectionTest {
    private static final Pinecone controlPlaneClient = new Pinecone
            .Builder(System.getenv("PINECONE_API_KEY"))
            .withSourceTag("pinecone_test")
            .build();

    @Test
    public void createIndexWithDeletionProtectionEnabled() throws InterruptedException {
        String indexName = RandomStringBuilder.build("create-serv", 8);
        HashMap<String, String> expectedTags = new HashMap<>();
        expectedTags.put("test", "deletion-protection-enabled");
        // Create serverless index with deletion protection enabled
        controlPlaneClient.createServerlessIndex(indexName, "cosine", 3, "aws", "us-west-2", DeletionProtection.ENABLED, expectedTags);
        // Wait for index to be created
        Thread.sleep(5000);
        // Describe index to verify deletion protection is enabled
        IndexModel indexModel = controlPlaneClient.describeIndex(indexName);
        DeletionProtection deletionProtection = indexModel.getDeletionProtection();
        Assertions.assertEquals(DeletionProtection.ENABLED, deletionProtection);
        Map<String, String> actualTags = indexModel.getTags();
        Assertions.assertEquals(expectedTags, actualTags);
    }

    @Test
    public void createIndexWithDeletionProtectionDisabled() throws InterruptedException {
        String indexName = RandomStringBuilder.build("create-pod", 8);
        HashMap<String, String> expectedTags = new HashMap<>();
        expectedTags.put("test", "deletion-protection-disabled");
        // Create serverless index with deletion protection disabled
        controlPlaneClient.createServerlessIndex(indexName, "cosine", 3, "aws", "us-west-2", DeletionProtection.DISABLED, expectedTags);
        // Wait for index to be created
        Thread.sleep(5000);
        IndexModel indexModel = controlPlaneClient.describeIndex(indexName);
        DeletionProtection deletionProtection = indexModel.getDeletionProtection();
        Assertions.assertEquals(DeletionProtection.DISABLED, deletionProtection);
        Map<String, String> actualTags = indexModel.getTags();
        Assertions.assertEquals(expectedTags, actualTags);
        // Configure index to enable deletionProtection
        controlPlaneClient.configureServerlessIndex(indexName, DeletionProtection.ENABLED, expectedTags, null);
        // Wait for index to be configured
        Thread.sleep(5000);
        indexModel = controlPlaneClient.describeIndex(indexName);
        deletionProtection = indexModel.getDeletionProtection();
        Assertions.assertEquals(DeletionProtection.ENABLED, deletionProtection);
        // Configure index to disable deletionProtection
        controlPlaneClient.configureServerlessIndex(indexName, DeletionProtection.DISABLED, expectedTags, null);
        // Wait for index to be configured
        Thread.sleep(5000);
        // Delete index
        controlPlaneClient.deleteIndex(indexName);
    }
}
