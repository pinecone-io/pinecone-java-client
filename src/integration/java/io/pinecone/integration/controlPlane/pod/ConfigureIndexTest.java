package io.pinecone.integration.controlPlane.pod;

import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeException;
import io.pinecone.exceptions.PineconeForbiddenException;
import io.pinecone.exceptions.PineconeBadRequestException;
import io.pinecone.exceptions.PineconeNotFoundException;
import org.junit.jupiter.api.*;
import org.openapitools.client.model.*;

import java.io.IOException;

import static io.pinecone.helpers.AssertRetry.assertWithRetry;
import static io.pinecone.helpers.IndexManager.createIndexIfNotExistsControlPlane;
import static io.pinecone.helpers.IndexManager.waitUntilIndexIsReady;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigureIndexTest {
    private static Pinecone controlPlaneClient;
    private static String indexName;

    @BeforeAll
    public static void setUp() throws InterruptedException, IOException {
        controlPlaneClient = new Pinecone(System.getenv("PINECONE_API_KEY"));
        indexName = createIndexIfNotExistsControlPlane(controlPlaneClient, 5, IndexModelSpec.SERIALIZED_NAME_POD);
    }

    @AfterAll
    public static void cleanUp() throws InterruptedException {
        Thread.sleep(2500);
        controlPlaneClient.deleteIndex(indexName);
        Thread.sleep(2500);
    }

    @Test
    public void configureIndexWithInvalidIndexName() {
        ConfigureIndexRequestSpecPod pod = new ConfigureIndexRequestSpecPod();
        ConfigureIndexRequestSpec spec = new ConfigureIndexRequestSpec().pod(pod);
        ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest().spec(spec);

        try {
            controlPlaneClient.configureIndex("non-existent-index", configureIndexRequest);
        } catch (PineconeNotFoundException notFoundException) {
            assert (notFoundException.getLocalizedMessage().toLowerCase().contains("not found"));
        }
    }

    @Test
    public void configureIndexExceedingQuota() {
        ConfigureIndexRequestSpecPod pod = new ConfigureIndexRequestSpecPod().replicas(40);
        ConfigureIndexRequestSpec spec = new ConfigureIndexRequestSpec().pod(pod);
        ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest().spec(spec);
        try {
            waitUntilIndexIsReady(controlPlaneClient, indexName);
            controlPlaneClient.configureIndex(indexName, configureIndexRequest);
        } catch (PineconeForbiddenException forbiddenException) {
            assert (forbiddenException.getLocalizedMessage().contains("Increase your quota or upgrade to create more indexes."));
        } catch (Exception exception) {
            throw new PineconeException("Expected PineconeForbiddenException but the test threw: " + exception.getLocalizedMessage());
        }
    }

    @Test
    public void scaleUpAndDown() {
        try {
            // Verify the starting state
            IndexModel indexModel = waitUntilIndexIsReady(controlPlaneClient, indexName);
            assert indexModel.getSpec().getPod() != null;
            assertEquals(1, indexModel.getSpec().getPod().getReplicas());

            // Scale up for the test
            ConfigureIndexRequestSpecPod pod = new ConfigureIndexRequestSpecPod().replicas(3);
            ConfigureIndexRequestSpec spec = new ConfigureIndexRequestSpec().pod(pod);
            ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest().spec(spec);
            controlPlaneClient.configureIndex(indexName, configureIndexRequest);

            // Verify the scaled up replicas
            assertWithRetry(() -> {
                PodSpec podSpec = controlPlaneClient.describeIndex(indexName).getSpec().getPod();
                assert (podSpec != null);
                assertEquals(podSpec.getReplicas(), 3);
            });

            // Scaling down
            pod = new ConfigureIndexRequestSpecPod().replicas(1);
            spec = new ConfigureIndexRequestSpec().pod(pod);
            configureIndexRequest = new ConfigureIndexRequest().spec(spec);
            controlPlaneClient.configureIndex(indexName, configureIndexRequest);

            // Verify replicas were scaled down
            assertWithRetry(() -> {
                PodSpec podSpec = controlPlaneClient.describeIndex(indexName).getSpec().getPod();
                assert (podSpec != null);
                assertEquals(podSpec.getReplicas(), 1);
            });
        } catch (Exception exception) {
            throw new PineconeException("Test failed: " + exception.getLocalizedMessage());
        }
    }

    @Test
    public void changingBasePodType() {
        try {
            // Verify the starting state
            IndexModel indexModel = waitUntilIndexIsReady(controlPlaneClient, indexName);
            assert indexModel.getSpec().getPod() != null;
            assertEquals(1, indexModel.getSpec().getPod().getReplicas());

            // Try to change the base pod type
            ConfigureIndexRequestSpecPod pod = new ConfigureIndexRequestSpecPod().podType("p2.x2");
            ConfigureIndexRequestSpec spec = new ConfigureIndexRequestSpec().pod(pod);
            ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest().spec(spec);
            controlPlaneClient.configureIndex(indexName, configureIndexRequest);
        } catch (PineconeBadRequestException badRequestException) {
            assert(badRequestException.getMessage().contains("Bad request: Cannot change pod type."));
        } catch (Exception exception) {
            throw new PineconeException("Test failed: " + exception.getLocalizedMessage());
        }
    }

    @Test
    public void sizeIncrease() {
        try {
            // Verify the starting state
            IndexModel indexModel = waitUntilIndexIsReady(controlPlaneClient, indexName);
            assert indexModel.getSpec().getPod() != null;
            assertEquals("p1.x1", indexModel.getSpec().getPod().getPodType());

            // Change the pod type to a larger one
            ConfigureIndexRequestSpecPod pod = new ConfigureIndexRequestSpecPod().podType("p1.x2");
            ConfigureIndexRequestSpec spec = new ConfigureIndexRequestSpec().pod(pod);
            ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest().spec(spec);
            controlPlaneClient.configureIndex(indexName, configureIndexRequest);

            // Get the index description to verify the new pod type
            assertWithRetry(() -> {
                PodSpec podSpec = controlPlaneClient.describeIndex(indexName).getSpec().getPod();
                assert (podSpec != null);
                assertEquals(podSpec.getPodType(), "p1.x2");
            });
        } catch (Exception exception) {
            throw new PineconeException("Test failed: " + exception.getLocalizedMessage());
        }
    }
}
