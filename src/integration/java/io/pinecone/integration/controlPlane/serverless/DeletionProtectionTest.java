package io.pinecone.integration.controlPlane.serverless;

import io.pinecone.clients.Pinecone;
import io.pinecone.helpers.RandomStringBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openapitools.db_control.client.model.DeletionProtection;
import org.openapitools.db_control.client.model.IndexModel;

public class DeletionProtectionTest {
    private static final Pinecone controlPlaneClient = new Pinecone
            .Builder(System.getenv("PINECONE_API_KEY"))
            .withSourceTag("pinecone_test")
            .build();

    @Test
    public void createIndexWithDeletionProtectionEnabled() {
        String indexName = RandomStringBuilder.build("create-serv", 8);
        // Create serverless index with deletion protection enabled
        controlPlaneClient.createServerlessIndex(indexName, "cosine", 3, "aws", "us-west-2", DeletionProtection.ENABLED);
        // Describe index to verify deletion protection is enabled
        IndexModel indexModel = controlPlaneClient.describeIndex(indexName);
        DeletionProtection deletionProtection = indexModel.getDeletionProtection();
        Assertions.assertEquals(deletionProtection, DeletionProtection.ENABLED);
    }

    @Test
    public void createPodIndexWithDeletionProtectionDisabled() {
        String indexName = RandomStringBuilder.build("create-pod", 8);
        // Create serverless index with deletion protection disabled
        controlPlaneClient.createServerlessIndex(indexName, "cosine", 3, "aws", "us-west-2", DeletionProtection.DISABLED);
        IndexModel indexModel = controlPlaneClient.describeIndex(indexName);
        DeletionProtection deletionProtection = indexModel.getDeletionProtection();
        Assertions.assertEquals(deletionProtection, DeletionProtection.DISABLED);
        // Configure index to enable deletionProtection
        controlPlaneClient.configureServerlessIndex(indexName, DeletionProtection.ENABLED);
        indexModel = controlPlaneClient.describeIndex(indexName);
        deletionProtection = indexModel.getDeletionProtection();
        Assertions.assertEquals(deletionProtection, DeletionProtection.ENABLED);
        // Configure index to disable deletionProtection
        controlPlaneClient.configureServerlessIndex(indexName, DeletionProtection.DISABLED);
        // Delete index
        controlPlaneClient.deleteIndex(indexName);
    }
}
