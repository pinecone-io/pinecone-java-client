package io.pinecone.integration.controlPlane.pod;

import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeBadRequestException;
import io.pinecone.exceptions.PineconeUnmappedHttpException;
import io.pinecone.helpers.RandomStringBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.*;

import java.util.Random;

import static io.pinecone.helpers.IndexManager.waitUntilIndexIsReady;
import static org.junit.jupiter.api.Assertions.*;

public class CreateDescribeListAndDeleteIndexTest {
    private static final String environment = System.getenv("PINECONE_ENVIRONMENT");
    private static final String indexName = RandomStringBuilder.build("create-index", 8);
    private static Pinecone controlPlaneClient = new Pinecone(System.getenv("PINECONE_API_KEY"));

    @BeforeAll
    public static void setUp() throws InterruptedException {
        // Create the index
        CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().environment(environment).podType("p1.x1");
        CreateIndexRequestSpec createIndexRequestSpec = new CreateIndexRequestSpec().pod(podSpec);
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
        assertNotNull(indexModel.getSpec().getPod());
        assertEquals("p1.x1", indexModel.getSpec().getPod().getPodType());

        // List the index
        IndexList indexList = controlPlaneClient.listIndexes();
        assertNotNull(indexList.getIndexes());
        assertTrue(indexList.getIndexes().stream().anyMatch(index -> indexName.equals(index.getName())));
    }

    @Test
    public void createIndexWithPodsAndPodType() {
        String podIndexName = RandomStringBuilder.build("create-pod", 8);
        CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().environment(environment).pods(2).podType(
                "p1.x2");
        CreateIndexRequestSpec createIndexRequestSpec = new CreateIndexRequestSpec().pod(podSpec);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                .name(podIndexName)
                .metric(IndexMetric.COSINE)
                .dimension(10)
                .spec(createIndexRequestSpec);

        IndexModel createdIndex = controlPlaneClient.createIndex(createIndexRequest);
        assertEquals(createdIndex.getName(), podIndexName);
        assertEquals(createdIndex.getSpec().getPod().getPods(), 2);
        assertEquals(createdIndex.getSpec().getPod().getPodType(), "p1.x2");
        assertEquals(createdIndex.getStatus().getReady(), false);
        assertEquals(createdIndex.getStatus().getState(), IndexModelStatus.StateEnum.INITIALIZING);

        controlPlaneClient.deleteIndex(podIndexName);
    }

    @Test
    public void createIndexWithInvalidName() {
        CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().environment(environment).podType("p1.x1");
        CreateIndexRequestSpec createIndexRequestSpec = new CreateIndexRequestSpec().pod(podSpec);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                .name("Invalid-name")
                .metric(IndexMetric.COSINE)
                .dimension(10)
                .spec(createIndexRequestSpec);

        try {
            controlPlaneClient.createIndex(createIndexRequest);

            fail("Expected to throw PineconeBadRequestException");
        } catch (PineconeBadRequestException expected) {
            assertTrue(expected.getLocalizedMessage().contains("must consist of lower case alphanumeric characters or" +
                    " '-'"));
        }
    }

    @Test
    public void createIndexWithInvalidDimension() {
        CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().environment(environment).podType("p1.x1");
        CreateIndexRequestSpec createIndexRequestSpec = new CreateIndexRequestSpec().pod(podSpec);
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
    public void createIndexWithInvalidPods() {
        CreateIndexRequestSpecPod podSpec =
                new CreateIndexRequestSpecPod().environment(environment).pods(-1).podType("p1.x1");
        CreateIndexRequestSpec createIndexRequestSpec = new CreateIndexRequestSpec().pod(podSpec);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                .name("invalid-pods")
                .metric(IndexMetric.COSINE)
                .dimension(10)
                .spec(createIndexRequestSpec);

        try {
            controlPlaneClient.createIndex(createIndexRequest);

            fail("Expected to throw PineconeBadRequestException");
        } catch (PineconeBadRequestException expected) {
            assertTrue(expected.getLocalizedMessage().contains("pods"));
            assertTrue(expected.getLocalizedMessage().contains("must be greater than 0"));
        }
    }

    @Test
    public void createIndexWithInvalidReplicas() {
        CreateIndexRequestSpecPod podSpec =
                new CreateIndexRequestSpecPod().environment(environment).pods(1).replicas(-1).podType("p1.x1");
        CreateIndexRequestSpec createIndexRequestSpec = new CreateIndexRequestSpec().pod(podSpec);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                .name("invalid-replicas")
                .metric(IndexMetric.COSINE)
                .dimension(10)
                .spec(createIndexRequestSpec);

        try {
            controlPlaneClient.createIndex(createIndexRequest);

            fail("Expected to throw PineconeBadRequestException");
        } catch (PineconeBadRequestException expected) {
            assertTrue(expected.getLocalizedMessage().contains("replicas"));
            assertTrue(expected.getLocalizedMessage().contains("must be greater than 0"));
        }
    }

    @Test
    public void createIndexWithInvalidPodsToShards() {
        CreateIndexRequestSpecPod podSpec =
                new CreateIndexRequestSpecPod().environment(environment).pods(5).replicas(2).shards(2).podType("p1.x1");
        CreateIndexRequestSpec createIndexRequestSpec = new CreateIndexRequestSpec().pod(podSpec);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                .name("invalid-shards")
                .metric(IndexMetric.COSINE)
                .dimension(10)
                .spec(createIndexRequestSpec);

        try {
            controlPlaneClient.createIndex(createIndexRequest);

            fail("Expected to throw PineconeBadRequestException");
        } catch (PineconeBadRequestException expected) {
            assertTrue(expected.getLocalizedMessage().contains("total pods must be divisible by number of shards"));
        }
    }
}
