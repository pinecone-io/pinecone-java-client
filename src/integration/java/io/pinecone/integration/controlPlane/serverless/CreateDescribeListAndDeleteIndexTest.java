package io.pinecone.integration.controlPlane.serverless;

import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeBadRequestException;
import io.pinecone.exceptions.PineconeNotFoundException;
import io.pinecone.exceptions.PineconeUnmappedHttpException;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.helpers.TestIndexResourcesManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.*;

import static org.junit.jupiter.api.Assertions.*;

public class CreateDescribeListAndDeleteIndexTest {

    private static final TestIndexResourcesManager indexManager = TestIndexResourcesManager.getInstance();
    // Serverless currently has limited availability in specific regions, hard-code us-west-2 for now
    private static final String serverlessRegion = "us-west-2";
    private static final Pinecone controlPlaneClient = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
    private static String indexName;
    private static int dimension;

    @BeforeAll
    public static void setUp() throws InterruptedException {
        indexName = indexManager.getServerlessIndexName();
        dimension = indexManager.getDimension();
    }

    @Test
    public void describeAndListIndex() {
        // Describe the index
        IndexModel indexModel = controlPlaneClient.describeIndex(indexName);
        assertNotNull(indexModel);
        assertEquals(dimension, indexModel.getDimension());
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
        ServerlessSpec serverlessSpec = new ServerlessSpec().cloud(ServerlessSpec.CloudEnum.AWS).region(serverlessRegion);
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
            assertTrue(expected.getLocalizedMessage().contains("Name must consist of lower case alphanumeric characters or '-'"));
        }
    }

    @Test
    public void createServerlessIndexWithInvalidName() {
        try {
            controlPlaneClient.createServerlessIndex("Invalid-name", "cosine", 3, "aws", "us-west-2");

            fail("Expected to throw PineconeBadRequestException");
        } catch (PineconeBadRequestException expected) {
            assertTrue(expected.getLocalizedMessage().contains("Name must consist of lower case alphanumeric characters or '-'"));
        }
    }

    @Test
    public void createIndexWithInvalidDimension() {
        ServerlessSpec serverlessSpec = new ServerlessSpec().cloud(ServerlessSpec.CloudEnum.AWS).region(serverlessRegion);
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
            assertTrue(expected.getLocalizedMessage().contains("dimension: invalid value: integer `-1`, expected u32"));
        }
    }

    @Test
    public void createServerlessIndexWithInvalidDimension() {
        try {
            controlPlaneClient.createServerlessIndex("serverless-test-index", "cosine", -3, "aws", "us-west-2");
            fail("Expected to throw PineconeValidationException");
        } catch (PineconeValidationException expected) {
            assertTrue(expected.getLocalizedMessage().contains("Dimension must be greater than 0"));
        }
    }

    @Test
    public void createIndexInvalidCloud() {
        ServerlessSpec serverlessSpec = new ServerlessSpec().cloud(ServerlessSpec.CloudEnum.AZURE).region(serverlessRegion);
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
            assertTrue(expected.getLocalizedMessage().contains("Resource cloud: azure region: us-west-2 not found"));
        }
    }

    @Test
    public void createServerlessIndexWithInvalidCloud() {
        try {
            controlPlaneClient.createServerlessIndex("serverless-test-index", "cosine", 3, "blah", "us-west-2");
            fail("Expected to throw PineconeValidationException");
        } catch (PineconeValidationException expected) {
            assertTrue(expected.getLocalizedMessage().contains(String.format("Cloud must be one of %s",
                    ServerlessSpec.CloudEnum.values())));
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
            assertTrue(expected.getLocalizedMessage().contains("Resource cloud: aws region: invalid-region not found"));
        }
    }

    @Test
    public void createServerlessIndexWithInvalidRegion() {
        try {
            controlPlaneClient.createServerlessIndex("serverless-test-index", "cosine", 3, "aws", "invalid-region");
            fail("Expected to throw PineconeNotFoundException");
        } catch (PineconeNotFoundException expected) {
            assertTrue(expected.getLocalizedMessage().contains("Resource cloud: aws region: invalid-region not found"));
        }
    }
}
