package io.pinecone.integration.controlPlane.index.pod;

import io.pinecone.PineconeControlPlaneClient;
import io.pinecone.helpers.RandomStringBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.client.ApiException;
import org.openapitools.client.model.*;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CreateDescribeListAndDeleteIndexTest {
    private PineconeControlPlaneClient controlPlaneClient;
    private final String apiKey = System.getenv("PINECONE_API_KEY");
    private final String environment = System.getenv("PINECONE_ENVIRONMENT");

    @BeforeEach
    public void setUp() {
        controlPlaneClient = new PineconeControlPlaneClient(apiKey);
    }

    @Test
    public void createAndDelete() throws InterruptedException {
        String indexName = RandomStringBuilder.build("index-name", 8);
        CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().environment(environment).podType("p1.x1");
        CreateIndexRequestSpec createIndexRequestSpec = new CreateIndexRequestSpec().pod(podSpec);

        // Create the index
        CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                .name(indexName)
                .metric(IndexMetric.COSINE)
                .dimension(10)
                .spec(createIndexRequestSpec);
        controlPlaneClient.createIndex(createIndexRequest);

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
