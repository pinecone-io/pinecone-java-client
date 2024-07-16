package io.pinecone.integration.controlPlane.serverless;

import io.pinecone.clients.Pinecone;
import io.pinecone.helpers.RandomStringBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openapitools.control.client.model.DeletionProtection;
import org.openapitools.control.client.model.IndexModel;

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
}
