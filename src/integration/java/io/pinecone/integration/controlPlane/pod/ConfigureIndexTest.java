package io.pinecone.integration.controlPlane.pod;

import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeForbiddenException;
import io.pinecone.exceptions.PineconeBadRequestException;
import io.pinecone.exceptions.PineconeNotFoundException;
import io.pinecone.helpers.RandomStringBuilder;
import org.junit.jupiter.api.*;
import org.openapitools.client.model.*;

import java.io.IOException;

import static io.pinecone.helpers.AssertRetry.assertWithRetry;
import static io.pinecone.helpers.IndexManager.waitUntilIndexIsReady;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigureIndexTest {
    private static Pinecone controlPlaneClient;
    private static final String indexName = RandomStringBuilder.build("configure-index", 8);;

    @BeforeAll
    public static void setUp() throws InterruptedException {
        controlPlaneClient = new Pinecone(System.getenv("PINECONE_API_KEY"));

        // Create index to work with
        CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().pods(1).podType("p1.x1").replicas(1).environment("us-east4-gcp");
        CreateIndexRequestSpec spec = new CreateIndexRequestSpec().pod(podSpec);
        CreateIndexRequest request = new CreateIndexRequest().name(indexName).dimension(5).metric(IndexMetric.COSINE).spec(spec);
        controlPlaneClient.createIndex(request);
        waitUntilIndexIsReady(controlPlaneClient, indexName);
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
            controlPlaneClient.configureIndex(indexName, configureIndexRequest);
        } catch (PineconeForbiddenException forbiddenException) {
            assert (forbiddenException.getLocalizedMessage().contains("Increase your quota or upgrade to create more indexes."));
        }
    }

    @Test
    public void scaleUpAndDown() throws InterruptedException {
        // Verify the starting state
        IndexModel indexModel = waitUntilIndexIsReady(controlPlaneClient, indexName);
        assert indexModel.getSpec().getPod() != null;
        assertEquals(1, indexModel.getSpec().getPod().getReplicas());

        // Scale up for the test
        ConfigureIndexRequestSpecPod upPod = new ConfigureIndexRequestSpecPod().replicas(3);
        ConfigureIndexRequestSpec upSpec = new ConfigureIndexRequestSpec().pod(upPod);
        ConfigureIndexRequest upConfigureIndexRequest = new ConfigureIndexRequest().spec(upSpec);

        // Verify the scaled up replicas
        assertWithRetry(() -> {
            controlPlaneClient.configureIndex(indexName, upConfigureIndexRequest);
            PodSpec podSpec = controlPlaneClient.describeIndex(indexName).getSpec().getPod();
            assert (podSpec != null);
            assertEquals(podSpec.getReplicas(), 3);
        });

        // Scaling down
        ConfigureIndexRequestSpecPod downPod = new ConfigureIndexRequestSpecPod().replicas(1);
        ConfigureIndexRequestSpec downSpec = new ConfigureIndexRequestSpec().pod(downPod);
        ConfigureIndexRequest downConfigureIndexRequest = new ConfigureIndexRequest().spec(downSpec);

        // Verify replicas were scaled down
        assertWithRetry(() -> {
            controlPlaneClient.configureIndex(indexName, downConfigureIndexRequest);
            PodSpec podSpec = controlPlaneClient.describeIndex(indexName).getSpec().getPod();
            assert (podSpec != null);
            assertEquals(podSpec.getReplicas(), 1);
        });
    }

    @Test
    public void changingBasePodType() throws InterruptedException {
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
        }
    }

    @Test
    public void sizeIncrease() throws InterruptedException {
        // Verify the starting state
        IndexModel indexModel = waitUntilIndexIsReady(controlPlaneClient, indexName);
        assert indexModel.getSpec().getPod() != null;
        assertEquals("p1.x1", indexModel.getSpec().getPod().getPodType());

        // Change the pod type to a larger one
        ConfigureIndexRequestSpecPod pod = new ConfigureIndexRequestSpecPod().podType("p1.x2");
        ConfigureIndexRequestSpec spec = new ConfigureIndexRequestSpec().pod(pod);
        ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest().spec(spec);

        // Get the index description to verify the new pod type
        assertWithRetry(() -> {
            controlPlaneClient.configureIndex(indexName, configureIndexRequest);
            PodSpec podSpec = controlPlaneClient.describeIndex(indexName).getSpec().getPod();
            assert (podSpec != null);
            assertEquals(podSpec.getPodType(), "p1.x2");
        });
    }
}
