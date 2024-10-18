package io.pinecone.integration.controlPlane.pod;

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
    public void createPodIndexWithDeletionProtectionEnabled() {
        String indexName = RandomStringBuilder.build("create-pod", 8);
        // Create pod index with deletion protection enabled
        controlPlaneClient.createPodsIndex(indexName, 3, "us-east-1-aws", "p1.x1", DeletionProtection.ENABLED);
        IndexModel indexModel = controlPlaneClient.describeIndex(indexName);
        DeletionProtection deletionProtection = indexModel.getDeletionProtection();
        Assertions.assertEquals(deletionProtection, DeletionProtection.ENABLED);
        // Configure index to disable deletionProtection
        controlPlaneClient.configurePodsIndex(indexName, DeletionProtection.DISABLED);
        // Delete index
        controlPlaneClient.deleteIndex(indexName);
    }

    @Test
    public void createPodIndexWithDeletionProtectionDisabled() {
        String indexName = RandomStringBuilder.build("create-pod", 8);
        // Create pod index with deletion protection disabled
        controlPlaneClient.createPodsIndex(indexName, 3, "us-east-1-aws", "p1.x1");
        IndexModel indexModel = controlPlaneClient.describeIndex(indexName);
        DeletionProtection deletionProtection = indexModel.getDeletionProtection();
        Assertions.assertEquals(deletionProtection, DeletionProtection.DISABLED);
        // Configure index to enable deletionProtection
        controlPlaneClient.configurePodsIndex(indexName, DeletionProtection.ENABLED);
        indexModel = controlPlaneClient.describeIndex(indexName);
        deletionProtection = indexModel.getDeletionProtection();
        Assertions.assertEquals(deletionProtection, DeletionProtection.ENABLED);
        // Configure index to disable deletionProtection
        controlPlaneClient.configurePodsIndex(indexName, DeletionProtection.DISABLED);
        // Delete index
        controlPlaneClient.deleteIndex(indexName);
    }
}
