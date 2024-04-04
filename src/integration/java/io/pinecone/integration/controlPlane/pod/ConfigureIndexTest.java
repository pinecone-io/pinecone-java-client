package io.pinecone.integration.controlPlane.pod;

import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeForbiddenException;
import io.pinecone.exceptions.PineconeBadRequestException;
import io.pinecone.exceptions.PineconeNotFoundException;
import io.pinecone.helpers.RandomStringBuilder;
import org.junit.jupiter.api.*;
import org.openapitools.client.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.pinecone.helpers.AssertRetry.assertWithRetry;
import static io.pinecone.helpers.IndexManager.waitUntilIndexIsReady;
import static org.junit.jupiter.api.Assertions.*;

public class ConfigureIndexTest {
    private static final Logger logger = LoggerFactory.getLogger(CollectionTest.class);
    private static Pinecone controlPlaneClient;
    private static final String indexName = RandomStringBuilder.build("configure-index", 8);;

    @BeforeAll
    public static void setUp() throws InterruptedException {
        controlPlaneClient = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();

        // Create index to work with
        CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().pods(1).podType("p1.x1").replicas(1).environment("us-east4-gcp");
        CreateIndexRequestSpec spec = new CreateIndexRequestSpec().pod(podSpec);
        CreateIndexRequest request = new CreateIndexRequest().name(indexName).dimension(5).metric(IndexMetric.COSINE).spec(spec);
        controlPlaneClient.createIndex(request);
        waitUntilIndexIsReady(controlPlaneClient, indexName);
    }

    @BeforeEach
    public void beforeEach() throws InterruptedException {
        waitUntilIndexStateIsReady(indexName);
    }

    @AfterAll
    public static void cleanUp() throws InterruptedException  {
        waitUntilIndexStateIsReady(indexName);
        assertWithRetry(() -> controlPlaneClient.deleteIndex(indexName));
    }

    @Test
    public void configureIndexWithInvalidIndexName() {
        ConfigureIndexRequestSpecPod pod = new ConfigureIndexRequestSpecPod();
        ConfigureIndexRequestSpec spec = new ConfigureIndexRequestSpec().pod(pod);
        ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest().spec(spec);

        try {
            controlPlaneClient.configureIndex("non-existent-index", configureIndexRequest);

            fail("Expected to throw PineconeNotFoundException");
        } catch (PineconeNotFoundException expected) {
            assertTrue(expected.getLocalizedMessage().toLowerCase().contains("not found"));
        }
    }

    @Test
    public void configureIndexExceedingQuota() {
        ConfigureIndexRequestSpecPod pod = new ConfigureIndexRequestSpecPod().replicas(400);
        ConfigureIndexRequestSpec spec = new ConfigureIndexRequestSpec().pod(pod);
        ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest().spec(spec);
        try {
            controlPlaneClient.configureIndex(indexName, configureIndexRequest);

            fail("Expected to throw PineconeForbiddenException");
        } catch (PineconeForbiddenException expected) {
            assertTrue(expected.getLocalizedMessage().contains("quota"));
        }
    }

    @Test
    public void scaleUpAndDown() throws InterruptedException {
        // Verify the starting state
        IndexModel indexModel = controlPlaneClient.describeIndex(indexName);
        assertNotNull(indexModel.getSpec().getPod());
        assertEquals(1, indexModel.getSpec().getPod().getReplicas());

        // Scale up for the test
        ConfigureIndexRequestSpecPod upPod = new ConfigureIndexRequestSpecPod().replicas(3);
        ConfigureIndexRequestSpec upSpec = new ConfigureIndexRequestSpec().pod(upPod);
        ConfigureIndexRequest upConfigureIndexRequest = new ConfigureIndexRequest().spec(upSpec);

        // Verify the scaled up replicas
        assertWithRetry(() -> {
            controlPlaneClient.configureIndex(indexName, upConfigureIndexRequest);
            PodSpec podSpec = controlPlaneClient.describeIndex(indexName).getSpec().getPod();
            assertNotNull(podSpec);
            assertEquals(podSpec.getReplicas(), 3);
        });

        waitUntilIndexStateIsReady(indexName);

        // Scaling down
        ConfigureIndexRequestSpecPod downPod = new ConfigureIndexRequestSpecPod().replicas(1);
        ConfigureIndexRequestSpec downSpec = new ConfigureIndexRequestSpec().pod(downPod);
        ConfigureIndexRequest downConfigureIndexRequest = new ConfigureIndexRequest().spec(downSpec);

        // Verify replicas were scaled down
        assertWithRetry(() -> {
            controlPlaneClient.configureIndex(indexName, downConfigureIndexRequest);
            PodSpec podSpec = controlPlaneClient.describeIndex(indexName).getSpec().getPod();
            assertNotNull(podSpec);
            assertEquals(podSpec.getReplicas(), 1);
        });
    }

    @Test
    public void changingBasePodType() throws InterruptedException {
        try {
            // Verify the starting state
            IndexModel indexModel = controlPlaneClient.describeIndex(indexName);
            assertNotNull(indexModel.getSpec().getPod());
            assertEquals(1, indexModel.getSpec().getPod().getReplicas());

            // Try to change the base pod type
            ConfigureIndexRequestSpecPod pod = new ConfigureIndexRequestSpecPod().podType("p2.x2");
            ConfigureIndexRequestSpec spec = new ConfigureIndexRequestSpec().pod(pod);
            ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest().spec(spec);
            controlPlaneClient.configureIndex(indexName, configureIndexRequest);

            fail("Expected to throw PineconeBadRequestException");
        } catch (PineconeBadRequestException expected) {
            assertTrue(expected.getMessage().contains("change pod"));
        }
    }

    @Test
    public void sizeIncrease() throws InterruptedException {
        // Verify the starting state
        IndexModel indexModel = controlPlaneClient.describeIndex(indexName);
        assertNotNull(indexModel.getSpec().getPod());
        assertEquals("p1.x1", indexModel.getSpec().getPod().getPodType());

        // Change the pod type to a larger one
        ConfigureIndexRequestSpecPod pod = new ConfigureIndexRequestSpecPod().podType("p1.x2");
        ConfigureIndexRequestSpec spec = new ConfigureIndexRequestSpec().pod(pod);
        ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest().spec(spec);

        // Get the index description to verify the new pod type
        assertWithRetry(() -> {
            controlPlaneClient.configureIndex(indexName, configureIndexRequest);
            PodSpec podSpec = controlPlaneClient.describeIndex(indexName).getSpec().getPod();
            assertNotNull(podSpec);
            assertEquals(podSpec.getPodType(), "p1.x2");
        });
    }

    private static void waitUntilIndexStateIsReady(String indexName) throws InterruptedException {
        int timeToWaitMs = 30000;
        int timeWaited = 0;
        IndexModel index = controlPlaneClient.describeIndex(indexName);

        while (index.getStatus().getState() != IndexModelStatus.StateEnum.READY && timeWaited <= timeToWaitMs) {
            Thread.sleep(2000);
            timeWaited += 2000;
            logger.info("waited 2000ms for index to upgrade, time left: " + timeToWaitMs);
            logger.info("System model state: " + index.getStatus());
            index = controlPlaneClient.describeIndex(indexName);
        }
        if (!index.getStatus().getReady()) {
            fail("Index " + indexName + " did not finish upgrading after " + timeWaited + "ms");
        }
    }
}
