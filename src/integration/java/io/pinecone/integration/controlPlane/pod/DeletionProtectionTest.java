package io.pinecone.integration.controlPlane.pod;

import io.pinecone.clients.Pinecone;
import io.pinecone.helpers.CreatedIndexes;
import io.pinecone.helpers.RandomStringBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openapitools.db_control.client.model.IndexModel;

@Disabled
@Tag("pod")
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
    public void createPodIndexWithDeletionProtectionEnabled() {
        String indexName = createdIndexes.register(RandomStringBuilder.buildResourceName("create-pod", 8));
        // Create pod index with deletion protection enabled
        controlPlaneClient.createPodsIndex(indexName, 3, "us-east-1-aws", "p1.x1", "enabled");
        IndexModel indexModel = controlPlaneClient.describeIndex(indexName);
        String deletionProtection = indexModel.getDeletionProtection();
        Assertions.assertEquals("enabled", deletionProtection);
        // Configure index to disable deletionProtection
        controlPlaneClient.configurePodsIndex(indexName, "disabled");
    }

    @Test
    public void createPodIndexWithDeletionProtectionDisabled() {
        String indexName = createdIndexes.register(RandomStringBuilder.buildResourceName("create-pod", 8));
        // Create pod index with deletion protection disabled
        controlPlaneClient.createPodsIndex(indexName, 3, "us-east-1-aws", "p1.x1");
        IndexModel indexModel = controlPlaneClient.describeIndex(indexName);
        String deletionProtection = indexModel.getDeletionProtection();
        Assertions.assertEquals("disabled", deletionProtection);
        // Configure index to enable deletionProtection
        controlPlaneClient.configurePodsIndex(indexName, "enabled");
        indexModel = controlPlaneClient.describeIndex(indexName);
        deletionProtection = indexModel.getDeletionProtection();
        Assertions.assertEquals("enabled", deletionProtection);
        // Configure index to disable deletionProtection
        controlPlaneClient.configurePodsIndex(indexName, "disabled");
    }
}
