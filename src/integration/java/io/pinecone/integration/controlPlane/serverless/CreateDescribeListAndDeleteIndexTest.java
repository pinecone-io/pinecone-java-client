package io.pinecone.integration.controlPlane.serverless;

import io.pinecone.clients.Pinecone;
import io.pinecone.helpers.RandomStringBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.*;

import java.util.Objects;

import static io.pinecone.helpers.IndexManager.waitUntilIndexIsReady;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CreateDescribeListAndDeleteIndexTest {
    private Pinecone controlPlaneClient;

    @BeforeEach
    public void setUp() {
        controlPlaneClient = new Pinecone(System.getenv("PINECONE_API_KEY"));
    }

    @Test
    public void createAndDelete() throws InterruptedException {
        String indexName = RandomStringBuilder.build("index-name", 8);
        ServerlessSpec serverlessSpec = new ServerlessSpec().cloud(ServerlessSpec.CloudEnum.AWS).region("us-west-2");
        CreateIndexRequestSpec createIndexRequestSpec = new CreateIndexRequestSpec().serverless(serverlessSpec);

        // Create the index
        CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                .name(indexName)
                .metric(IndexMetric.COSINE)
                .dimension(10)
                .spec(createIndexRequestSpec);
        controlPlaneClient.createIndex(createIndexRequest);

        waitUntilIndexIsReady(controlPlaneClient, indexName);

        // Describe the index
        IndexModel indexModel = controlPlaneClient.describeIndex(indexName);
        assertNotNull(indexModel);
        assertEquals(10, indexModel.getDimension());
        assertEquals(indexName, indexModel.getName());
        assertEquals(IndexMetric.COSINE, indexModel.getMetric());

        // List the index
        IndexList indexList = controlPlaneClient.listIndexes();
        assert !Objects.requireNonNull(indexList.getIndexes()).isEmpty();

        // Delete the index
        controlPlaneClient.deleteIndex(indexName);
        Thread.sleep(3500);
    }
}
