package io.pinecone.integration.controlPlane.pod;

import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeBadRequestException;
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

    @BeforeAll
    public static void setUp() throws InterruptedException {
        indexName = indexManager.getPodIndexName();
        indexDimension = indexManager.getDimension();
        IndexModel podIndex = indexManager.getPodIndexModel();
        indexPodType = podIndex.getSpec().getPod().getPodType();
    }

    @Test
    public void describeAndListIndex() {
        // Describe the index
        IndexModel indexModel = controlPlaneClient.describeIndex(indexName);
        assertNotNull(indexModel);
        assertEquals(indexDimension, indexModel.getDimension());
        assertEquals(indexName, indexModel.getName());
        assertEquals(IndexMetric.DOTPRODUCT, indexModel.getMetric());
        assertNotNull(indexModel.getSpec().getPod());
        assertEquals(indexPodType, indexModel.getSpec().getPod().getPodType());

        // List the index
        IndexList indexList = controlPlaneClient.listIndexes();
        assertNotNull(indexList.getIndexes());
        assertTrue(indexList.getIndexes().stream().anyMatch(index -> indexName.equals(index.getName())));
    }

    @Test
    public void createPodsIndexWithMinimumRequiredParams() {
        String podIndexName = RandomStringBuilder.build("create-pod-with-min", 8);
        Integer dimension = 3;
        String environment = "us-east-1-aws";
        String podType = "p1.x1";
        String metric = "cosine";
        IndexModel podsIndex = controlPlaneClient.createPodsIndex(podIndexName, dimension, environment, podType,
                metric);

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
        assertThrows(PineconeBadRequestException.class, () -> {
            controlPlaneClient.createPodsIndex(podIndexName, dimension, environment, podType);
        });

    }

}
