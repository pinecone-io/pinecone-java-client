package io.pinecone.integration.controlPlane.pod;

import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeBadRequestException;
import io.pinecone.exceptions.PineconeUnmappedHttpException;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.helpers.TestIndexResourcesManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.*;

import static org.junit.jupiter.api.Assertions.*;

public class CreateDescribeListAndDeleteIndexTest {

    private static final TestIndexResourcesManager indexManager = TestIndexResourcesManager.getInstance();
    private static Pinecone controlPlaneClient = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
    private static String indexName;
    private static int indexDimension;
    private static String indexPodType;
    private static final String environment =    System.getenv("PINECONE_ENVIRONMENT");

//    @BeforeAll
//    public static void setUp() throws InterruptedException {
//        indexName = indexManager.getPodIndexName();
//        indexDimension = indexManager.getDimension();
//        IndexModel podIndex = indexManager.getPodIndexModel();
//        indexPodType = podIndex.getSpec().getPod().getPodType();
//    }

    @Test
    public void describeAndListIndex() {
        // Describe the index
        IndexModel indexModel = controlPlaneClient.describeIndex(indexName);
        assertNotNull(indexModel);
        assertEquals(indexDimension, indexModel.getDimension());
        assertEquals(indexName, indexModel.getName());
        assertEquals(IndexMetric.COSINE, indexModel.getMetric());
        assertNotNull(indexModel.getSpec().getPod());
        assertEquals(indexPodType, indexModel.getSpec().getPod().getPodType());

        // List the index
        IndexList indexList = controlPlaneClient.listIndexes();
        assertNotNull(indexList.getIndexes());
        assertTrue(indexList.getIndexes().stream().anyMatch(index -> indexName.equals(index.getName())));
    }

    @Test
    public void createIndexWithPodsAndPodType() {
        String podIndexName = RandomStringBuilder.build("create-pod", 8);
        String podType = "p1.x2";
        CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().environment(environment).pods(2).podType(podType);
        CreateIndexRequestSpec createIndexRequestSpec = new CreateIndexRequestSpec().pod(podSpec);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                .name(podIndexName)
                .dimension(10)
                .metric(IndexMetric.EUCLIDEAN)
                .spec(createIndexRequestSpec);

        IndexModel createdIndex = controlPlaneClient.createIndex(createIndexRequest);
        assertEquals(createdIndex.getName(), podIndexName);
        assertEquals(createdIndex.getMetric(), IndexMetric.EUCLIDEAN);
        assertEquals(createdIndex.getSpec().getPod().getPods(), 2);
        assertEquals(createdIndex.getSpec().getPod().getPodType(), podType);
        assertEquals(createdIndex.getStatus().getReady(), false);
        assertEquals(createdIndex.getStatus().getState(), IndexModelStatus.StateEnum.INITIALIZING);

        controlPlaneClient.deleteIndex(podIndexName);
    }

    @Test
    public void createPodsIndexWithMinimumRequiredParams() {
        String podIndexName = RandomStringBuilder.build("create-pod-with-min", 8);
        Integer dimension = 3;
        String environment = "us-east-1-aws";
        String podType = "p1.x1";
        String metric = "cosine";
        IndexModel podsIndex = controlPlaneClient.createPodsIndex(podIndexName, dimension, environment, metric,
                podType);

        assertEquals(podIndexName, podsIndex.getName());
        assertEquals(dimension, podsIndex.getDimension());
        assertEquals(environment, podsIndex.getSpec().getPod().getEnvironment());
        assertEquals(metric, podsIndex.getMetric().toString());
        assertEquals(podType, podsIndex.getSpec().getPod().getPodType());

        // Confirm defaults are put in by the backend when not supplied by the user
        assertEquals(IndexMetric.COSINE, podsIndex.getMetric());
        assertEquals(1, podsIndex.getSpec().getPod().getPods());
        assertEquals(1, podsIndex.getSpec().getPod().getReplicas());
        assertEquals(1, podsIndex.getSpec().getPod().getShards());
        assertNull(podsIndex.getSpec().getPod().getMetadataConfig());
        assertNull(podsIndex.getSpec().getPod().getSourceCollection());

        // Cleanup
        controlPlaneClient.deleteIndex(podIndexName);
    }

    @Test
    public void CreatePodsIndexWithInvalidIndexName() {
        String podIndexName = "Invalid-name";
        Integer dimension = 3;
        String environment = "us-east-1-aws";
        String podType = "p1.x1";

        // Empty indexName
        assertThrows(PineconeValidationException.class, () -> {
            controlPlaneClient.createPodsIndex(indexName, dimension, environment, podType);
        });
    }

    @Test
    public void createIndexWithInvalidName() {
        CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().environment(environment).podType(indexPodType);
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
            assertTrue(expected.getLocalizedMessage().contains("Name must consist of lower case alphanumeric characters or '-'"));
        }
    }

    @Test
    public void createIndexWithInvalidDimension() {
        CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().environment(environment).podType(indexPodType);
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
            assertTrue(expected.getLocalizedMessage().contains("dimension: invalid value: integer `-1`, expected u32"));
        }
    }

    @Test
    public void createIndexWithInvalidPods() {
        CreateIndexRequestSpecPod podSpec =
                new CreateIndexRequestSpecPod().environment(environment).pods(-1).podType(indexPodType);
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
            assertTrue(expected.getLocalizedMessage().contains("Invalid value for pods: must be greater than 0"));
        }
    }

    @Test
    public void createIndexWithInvalidReplicas() {
        CreateIndexRequestSpecPod podSpec =
                new CreateIndexRequestSpecPod().environment(environment).pods(1).replicas(-1).podType(indexPodType);
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
            assertTrue(expected.getLocalizedMessage().contains("Invalid value for replicas: must be greater than 0"));
        }
    }

    @Test
    public void createIndexWithInvalidPodsToShards() {
        CreateIndexRequestSpecPod podSpec =
                new CreateIndexRequestSpecPod().environment(environment).pods(5).replicas(2).shards(2).podType(indexPodType);
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
            assertTrue(expected.getLocalizedMessage().contains("Invalid value for pods: total pods must be divisible by number of shards"));
        }
    }
}
