package io.pinecone.integration.controlPlane.pod;

import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeBadRequestException;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.helpers.TestResourcesManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openapitools.db_control.client.model.*;

import static org.junit.jupiter.api.Assertions.*;

public class CreateDescribeListAndDeleteIndexTest {

    private static final TestResourcesManager indexManager = TestResourcesManager.getInstance();
    private static final Pinecone controlPlaneClient = new Pinecone
            .Builder(System.getenv("PINECONE_API_KEY"))
            .withSourceTag("pinecone_test")
            .build();
    private static String indexName;
    private static int indexDimension;
    private static String indexPodType;

    @BeforeAll
    public static void setUp() throws InterruptedException {
        indexName = indexManager.getOrCreatePodIndex();
        indexDimension = indexManager.getDimension();
        IndexModel podIndex = indexManager.getOrCreatePodIndexModel();
        indexPodType = podIndex.getSpec().getIndexModelPodBased().getPod().getPodType();
    }

    @Test
    public void describeAndListIndex() {
        // Describe the index
        IndexModel indexModel = controlPlaneClient.describeIndex(indexName);
        assertNotNull(indexModel);
        assertEquals(indexDimension, indexModel.getDimension());
        assertEquals(indexName, indexModel.getName());
        assertEquals("dotproduct", indexModel.getMetric());
        assertNotNull(indexModel.getSpec().getIndexModelPodBased().getPod());
        assertEquals(indexPodType, indexModel.getSpec().getIndexModelPodBased().getPod().getPodType());

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
        PodSpecMetadataConfig metadataConfig = null;
        IndexModel podsIndex = controlPlaneClient.createPodsIndex(podIndexName, dimension, environment, podType,
                metric, metadataConfig);

        assertEquals(podIndexName, podsIndex.getName());
        assertEquals(dimension, podsIndex.getDimension());
        assertEquals(environment, podsIndex.getSpec().getIndexModelPodBased().getPod().getEnvironment());
        assertEquals(metric, podsIndex.getMetric().toString());
        assertEquals(podType, podsIndex.getSpec().getIndexModelPodBased().getPod().getPodType());

        // Confirm defaults are put in by the backend when not supplied by the user
        assertEquals("cosine", podsIndex.getMetric());
        assertEquals(1, podsIndex.getSpec().getIndexModelPodBased().getPod().getPods());
        assertEquals(1, podsIndex.getSpec().getIndexModelPodBased().getPod().getReplicas());
        assertEquals(1, podsIndex.getSpec().getIndexModelPodBased().getPod().getShards());
        assertNull(podsIndex.getSpec().getIndexModelPodBased().getPod().getMetadataConfig());
        assertNull(podsIndex.getSpec().getIndexModelPodBased().getPod().getSourceCollection());

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
