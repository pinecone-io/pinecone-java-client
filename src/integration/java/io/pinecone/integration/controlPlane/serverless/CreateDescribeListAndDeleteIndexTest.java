package io.pinecone.integration.controlPlane.serverless;

import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeBadRequestException;
import io.pinecone.exceptions.PineconeException;
import io.pinecone.exceptions.PineconeNotFoundException;
import io.pinecone.exceptions.PineconeUnmappedHttpException;
import io.pinecone.helpers.RandomStringBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.*;

import static io.pinecone.helpers.IndexManager.waitUntilIndexIsReady;
import static org.junit.jupiter.api.Assertions.*;

public class CreateDescribeListAndDeleteIndexTest {
    private static Pinecone controlPlaneClient = new Pinecone(System.getenv("PINECONE_API_KEY"));
    private static final String indexName = RandomStringBuilder.build("create-index", 8);
    @BeforeAll
    public static void setUp() throws InterruptedException {
        // Create the index
        ServerlessSpec serverlessSpec = new ServerlessSpec().cloud(ServerlessSpec.CloudEnum.AWS).region("us-west-2");
        CreateIndexRequestSpec createIndexRequestSpec = new CreateIndexRequestSpec().serverless(serverlessSpec);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                .name(indexName)
                .metric(IndexMetric.COSINE)
                .dimension(10)
                .spec(createIndexRequestSpec);
        controlPlaneClient.createIndex(createIndexRequest);
        waitUntilIndexIsReady(controlPlaneClient, indexName);
    }

    @AfterAll
    public static void cleanUp() {
        // Delete the index
        controlPlaneClient.deleteIndex(indexName);
    }

    @Test
    public void describeAndListIndex() {
        // Describe the index
        IndexModel indexModel = controlPlaneClient.describeIndex(indexName);
        assertNotNull(indexModel);
        assertEquals(10, indexModel.getDimension());
        assertEquals(indexName, indexModel.getName());
        assertEquals(IndexMetric.COSINE, indexModel.getMetric());
        assertNotNull(indexModel.getSpec().getServerless());

        // List the index
        IndexList indexList = controlPlaneClient.listIndexes();
        assertNotNull(indexList.getIndexes());
        assertTrue(indexList.getIndexes().stream().anyMatch(index -> indexName.equals(index.getName())));
    }

    @Test
    public void createIndexWithInvalidName() {
        ServerlessSpec serverlessSpec = new ServerlessSpec().cloud(ServerlessSpec.CloudEnum.AWS).region("us-west-2");
        CreateIndexRequestSpec createIndexRequestSpec = new CreateIndexRequestSpec().serverless(serverlessSpec);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                .name("Invalid-name")
                .metric(IndexMetric.COSINE)
                .dimension(10)
                .spec(createIndexRequestSpec);

        try {
            controlPlaneClient.createIndex(createIndexRequest);

            fail("Expected to throw PineconeBadRequestException");
        } catch (PineconeBadRequestException expected) {
            assertTrue(expected.getLocalizedMessage().contains("must consist of lower case alphanumeric characters or '-'"));
        }
    }

    @Test
    public void createIndexWithInvalidDimension() {
        ServerlessSpec serverlessSpec = new ServerlessSpec().cloud(ServerlessSpec.CloudEnum.AWS).region("us-west-2");
        CreateIndexRequestSpec createIndexRequestSpec = new CreateIndexRequestSpec().serverless(serverlessSpec);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                .name("invalid-dimension")
                .metric(IndexMetric.COSINE)
                .dimension(-1)
                .spec(createIndexRequestSpec);

        try {
            controlPlaneClient.createIndex(createIndexRequest);

            fail("Expected to throw PineconeUnmappedHttpException");
        } catch (PineconeUnmappedHttpException expected) {
            assertTrue(expected.getLocalizedMessage().contains("dimension: invalid value"));
        }
    }

    @Test
    public void createIndexInvalidCloud() {
        ServerlessSpec serverlessSpec = new ServerlessSpec().cloud(ServerlessSpec.CloudEnum.AZURE).region("us-west-2");
        CreateIndexRequestSpec createIndexRequestSpec = new CreateIndexRequestSpec().serverless(serverlessSpec);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                .name("invalid-cloud")
                .metric(IndexMetric.COSINE)
                .dimension(1)
                .spec(createIndexRequestSpec);

        try {
            controlPlaneClient.createIndex(createIndexRequest);

            fail("Expected to throw PineconeNotFoundException");
        } catch (PineconeNotFoundException expected) {
            assertTrue(expected.getLocalizedMessage().contains("not found"));
        }
    }

    @Test
    public void createIndexInvalidRegion() {
        ServerlessSpec serverlessSpec = new ServerlessSpec().cloud(ServerlessSpec.CloudEnum.AWS).region("invalid-region");
        CreateIndexRequestSpec createIndexRequestSpec = new CreateIndexRequestSpec().serverless(serverlessSpec);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                .name("invalid-region")
                .metric(IndexMetric.COSINE)
                .dimension(1)
                .spec(createIndexRequestSpec);

        try {
            controlPlaneClient.createIndex(createIndexRequest);

            fail("Expected to throw PineconeNotFoundException");
        } catch (PineconeNotFoundException expected) {
            assertTrue(expected.getLocalizedMessage().contains("not found"));
        }
    }
}
