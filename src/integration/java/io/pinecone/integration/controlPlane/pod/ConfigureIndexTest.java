package io.pinecone.integration.controlPlane.pod;

import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeBadRequestException;
import io.pinecone.exceptions.PineconeForbiddenException;
import io.pinecone.exceptions.PineconeNotFoundException;
import io.pinecone.helpers.TestResourcesManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openapitools.db_control.client.model.DeletionProtection;
import org.openapitools.db_control.client.model.IndexModel;
import org.openapitools.db_control.client.model.IndexModelStatus;
import org.openapitools.db_control.client.model.PodSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.pinecone.helpers.AssertRetry.assertWithRetry;
import static org.junit.jupiter.api.Assertions.*;

public class ConfigureIndexTest {
    private static final Logger logger = LoggerFactory.getLogger(ConfigureIndexTest.class);
    private static final TestResourcesManager indexManager = TestResourcesManager.getInstance();
    private static final Pinecone controlPlaneClient = new Pinecone
            .Builder(System.getenv("PINECONE_API_KEY"))
            .withSourceTag("pinecone_test")
            .build();
    private static String indexName;

    @BeforeAll
    public static void setUp() throws InterruptedException {
        indexName = indexManager.getOrCreatePodIndex();
    }

    private static void waitUntilIndexStateIsReady(String indexName) throws InterruptedException {
        int timeToWaitMs = 100000;
        int timeWaited = 0;
        IndexModel index = controlPlaneClient.describeIndex(indexName);

        while (index.getStatus().getState() != IndexModelStatus.StateEnum.READY && timeWaited <= timeToWaitMs) {
            Thread.sleep(2000);
            timeWaited += 2000;
            logger.info("waited 2000ms for index to upgrade, time waited: " + timeWaited);
            index = controlPlaneClient.describeIndex(indexName);
        }
        if (!index.getStatus().getReady()) {
            fail("Index " + indexName + " did not finish upgrading after " + timeWaited + "ms");
        }
    }

    @AfterEach
    public void afterEach() throws InterruptedException {
        waitUntilIndexStateIsReady(indexName);
    }

    @Test
    public void configureIndexWithInvalidIndexName() {
        try {
            controlPlaneClient.configurePodsIndex("non-existent-index", 3, DeletionProtection.DISABLED);

            fail("Expected to throw PineconeNotFoundException");
        } catch (PineconeNotFoundException expected) {
            assertTrue(expected.getLocalizedMessage().toLowerCase().contains("not found"));
        }
    }

    @Test
    public void configureIndexExceedingQuota() {
        try {
            controlPlaneClient.configurePodsIndex(indexName, 30, DeletionProtection.DISABLED);
            fail("Expected to throw PineconeForbiddenException");
        } catch (PineconeForbiddenException expected) {
            assertTrue(expected.getLocalizedMessage().contains("reached the max pods allowed"));
        }
    }

    @Test
    public void scaleUpAndDown() throws InterruptedException {
        IndexModel indexModel = controlPlaneClient.describeIndex(indexName);
        assertNotNull(indexModel.getSpec().getPod());
        assertEquals(1, indexModel.getSpec().getPod().getReplicas());

        // Verify the scaled up replicas
        assertWithRetry(() -> {
            controlPlaneClient.configurePodsIndex(indexName, 3, DeletionProtection.DISABLED);
            PodSpec podSpec = controlPlaneClient.describeIndex(indexName).getSpec().getPod();
            assertNotNull(podSpec);
            assertEquals(podSpec.getReplicas(), 3);
        });

        waitUntilIndexStateIsReady(indexName);

        // Verify replicas were scaled down
        assertWithRetry(() -> {
            controlPlaneClient.configurePodsIndex(indexName, 1, DeletionProtection.DISABLED);
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
            controlPlaneClient.configurePodsIndex(indexName, "p2.x2");

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
        // Get the index description to verify the new pod type
        assertWithRetry(() -> {
            controlPlaneClient.configurePodsIndex(indexName, "p1.x2");
            PodSpec podSpec = controlPlaneClient.describeIndex(indexName).getSpec().getPod();
            assertNotNull(podSpec);
            assertEquals(podSpec.getPodType(), "p1.x2");
        });
    }
}
