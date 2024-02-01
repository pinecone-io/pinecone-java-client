package io.pinecone.integration.controlPlane.index;

import io.pinecone.PineconeClientConfig;
import io.pinecone.integration.dataplane.PineconeClientLiveIntegTest;
import io.pinecone.PineconeIndexOperationClient;
import io.pinecone.exceptions.PineconeBadRequestException;
import io.pinecone.exceptions.PineconeNotFoundException;
import io.pinecone.model.ConfigureIndexRequest;
import io.pinecone.model.IndexMeta;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static io.pinecone.helpers.IndexManager.createIndexIfNotExistsControlPlane;
import static io.pinecone.helpers.IndexManager.isIndexReady;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigureIndexTest {
    private static PineconeClientConfig config;
    private PineconeIndexOperationClient indexOperationClient;
    private String indexName;
    private static final Logger logger = LoggerFactory.getLogger(PineconeClientLiveIntegTest.class);

    @BeforeAll
    public static void defineConfig() {
        config = new PineconeClientConfig()
                .withApiKey(System.getenv("PINECONE_API_KEY"))
                .withEnvironment(System.getenv("PINECONE_ENVIRONMENT"));
        logger.info(String.format("CONFIG: %s", config));
    }

    @BeforeEach
    public void setUp() throws IOException, InterruptedException {
        indexName = createIndexIfNotExistsControlPlane(config, 5);
        indexOperationClient = new PineconeIndexOperationClient(config);
    }

    @Test
    public void configureIndexWithInvalidIndexName() {
        ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest()
                .withReplicas(2);
        try {
            isIndexReady(indexName, indexOperationClient);
            indexOperationClient.configureIndex("non-existent-index", configureIndexRequest);
        } catch (PineconeNotFoundException notFoundException) {
            assert (notFoundException.getLocalizedMessage().toLowerCase().contains("not found"));
        } catch (IOException | InterruptedException exception) {
            logger.error(exception.toString());
        }
    }

    @Test
    public void configureIndexExceedingQuota() {
        ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest()
                .withReplicas(20);
        try {
            isIndexReady(indexName, indexOperationClient);
            indexOperationClient.configureIndex(indexName, configureIndexRequest);
        } catch (PineconeBadRequestException badRequestException) {
            assert (badRequestException.getLocalizedMessage().contains("Capacity Reached. Increase your quota or upgrade to create more indexes."));
        } catch (Exception exception) {
            logger.error(exception.toString());
        }
    }

    @Test
    public void scaleUpAndDown() {
        try {
            // Verify the starting state
            IndexMeta indexMeta = isIndexReady(indexName, indexOperationClient);
            assertEquals(1, indexMeta.getDatabase().getReplicas());

            // Scale up for the test
            ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest()
                    .withReplicas(3);
            isIndexReady(indexName, indexOperationClient);
            indexOperationClient.configureIndex(indexName, configureIndexRequest);

            // Verify the scaled up replicas
            indexMeta = indexOperationClient.describeIndex(indexName);
            assertEquals(3, indexMeta.getDatabase().getReplicas());

            // Scaling down
            configureIndexRequest = new ConfigureIndexRequest()
                    .withReplicas(1);
            isIndexReady(indexName, indexOperationClient);
            indexOperationClient.configureIndex(indexName, configureIndexRequest);

            // Verify replicas were scaled down
            indexMeta = indexOperationClient.describeIndex(indexName);
            assertEquals(1, indexMeta.getDatabase().getReplicas());
        } catch (Exception exception) {
            logger.error(exception.toString());
        }
    }

    @Test
    public void changingBasePodType() {
        try {
            // Verify the starting state
            IndexMeta indexMeta = isIndexReady(indexName, indexOperationClient);
            assertEquals("p1.x1", indexMeta.getDatabase().getPodType());

            // Try to change the base pod type
            ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest()
                    .withPodType("p2.x1");

            isIndexReady(indexName, indexOperationClient);
            indexOperationClient.configureIndex(indexName, configureIndexRequest);
        } catch (PineconeBadRequestException badRequestException) {
            assertEquals(badRequestException.getMessage(), "Bad request: Cannot change pod type.");
        } catch (Exception exception) {
            logger.error(exception.getLocalizedMessage());
        }
    }

    @Test
    public void sizeIncrease() {
        try {
            // Verify the starting state
            IndexMeta indexMeta = isIndexReady(indexName, indexOperationClient);
            assertEquals("p1.x1", indexMeta.getDatabase().getPodType());

            // Change the pod type to a larger one
            ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest()
                    .withPodType("p1.x2");
            isIndexReady(indexName, indexOperationClient);
            indexOperationClient.configureIndex(indexName, configureIndexRequest);

            // Get the index description to verify the new pod type
            indexMeta = indexOperationClient.describeIndex(indexName);
            assertEquals("p1.x2", indexMeta.getDatabase().getPodType());

            // Delete this index since it'll be unused for future tests
            indexOperationClient.deleteIndex(indexName);
            Thread.sleep(3500);
        } catch (Exception exception) {
            logger.error(exception.getLocalizedMessage());
        }
    }

    @Test
    public void sizeDown() throws IOException, InterruptedException {
        try {
            // Verify the starting state
            IndexMeta indexMeta = isIndexReady(indexName, indexOperationClient);
            assertEquals("p1.x1", indexMeta.getDatabase().getPodType());

            // Increase the pod type
            ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest()
                    .withPodType("p1.x2");
            isIndexReady(indexName, indexOperationClient);
            indexOperationClient.configureIndex(indexName, configureIndexRequest);

            // Get the index description to verify the new pod type
            indexMeta = indexOperationClient.describeIndex(indexName);
            assertEquals("p1.x2", indexMeta.getDatabase().getPodType());

            // Attempt to scale down
            configureIndexRequest = new ConfigureIndexRequest()
                    .withPodType("p1.x1");
            indexOperationClient.configureIndex(indexName, configureIndexRequest);
            Thread.sleep(3500);
        } catch (Exception exception) {
            assertEquals(exception.getClass(), PineconeBadRequestException.class);
            assert(exception.getMessage().contains("Bad request: Cannot scale down an index, only up."));
        } finally {
            // Delete this index since it'll be unused for other tests
            indexOperationClient.deleteIndex(indexName);
            Thread.sleep(3500);
        }
    }
}
