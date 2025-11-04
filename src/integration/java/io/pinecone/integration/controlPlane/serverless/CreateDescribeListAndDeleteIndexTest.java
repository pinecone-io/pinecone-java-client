package io.pinecone.integration.controlPlane.serverless;

import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeBadRequestException;
import io.pinecone.exceptions.PineconeNotFoundException;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.helpers.TestResourcesManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openapitools.db_control.client.model.*;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class CreateDescribeListAndDeleteIndexTest {

    private static final TestResourcesManager indexManager = TestResourcesManager.getInstance();
    private static final Pinecone controlPlaneClient = new Pinecone
            .Builder(System.getenv("PINECONE_API_KEY"))
            .withSourceTag("pinecone_test")
            .build();
    private static String indexName;
    private static int dimension;

    @BeforeAll
    public static void setUp() throws InterruptedException {
        indexName = indexManager.getOrCreateServerlessIndex();
        dimension = indexManager.getDimension();
    }

    @Test
    public void describeAndListIndex() {
        // Describe the index
        IndexModel indexModel = controlPlaneClient.describeIndex(indexName);
        assertNotNull(indexModel);
        assertEquals(dimension, indexModel.getDimension());
        assertEquals(indexName, indexModel.getName());
        assertEquals("dotproduct", indexModel.getMetric());
        assertNotNull(indexModel.getSpec().getIndexModelServerless());

        // List the index
        IndexList indexList = controlPlaneClient.listIndexes();
        assertNotNull(indexList.getIndexes());
        assertTrue(indexList.getIndexes().stream().anyMatch(index -> indexName.equals(index.getName())));
    }

    @Test
    public void createServerlessIndexWithInvalidName() {
        try {
            controlPlaneClient.createServerlessIndex("Invalid-name", "cosine", 3, "aws", "us-west-2", "disabled", null);

            fail("Expected to throw PineconeBadRequestException");
        } catch (PineconeBadRequestException expected) {
            assertTrue(expected.getLocalizedMessage().contains("Name must consist of lower case alphanumeric characters or '-'"));
        }
    }

    @Test
    public void createServerlessIndexWithInvalidDimension() {
        try {
            controlPlaneClient.createServerlessIndex("serverless-test-index", "cosine", -3, "aws", "us-west-2", "disabled", null);
            fail("Expected to throw PineconeValidationException");
        } catch (PineconeValidationException expected) {
            assertTrue(expected.getLocalizedMessage().contains("Dimension must be greater than 0"));
        }
    }

    @Test
    public void createServerlessIndexWithInvalidRegion() {
        try {
            controlPlaneClient.createServerlessIndex("serverless-test-index", "cosine", 3, "aws", "invalid-region", "disabled", null);
            fail("Expected to throw PineconeNotFoundException");
        } catch (PineconeNotFoundException expected) {
            assertTrue(expected.getLocalizedMessage().contains("Resource cloud: aws region: invalid-region not found"));
        }
    }
}
