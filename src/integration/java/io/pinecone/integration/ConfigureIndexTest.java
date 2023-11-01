package io.pinecone.integration;

import io.pinecone.PineconeClientConfig;
import io.pinecone.PineconeClientLiveIntegTest;
import io.pinecone.PineconeIndexOperationClient;
import io.pinecone.exceptions.PineconeBadRequestException;
import io.pinecone.exceptions.PineconeNotFoundException;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.model.ConfigureIndexRequest;
import io.pinecone.model.CreateIndexRequest;
import io.pinecone.model.IndexMeta;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigureIndexTest {
    private PineconeIndexOperationClient pinecone;
    private String indexName;
    private static final Logger logger = LoggerFactory.getLogger(PineconeClientLiveIntegTest.class);

    @BeforeEach
    public void setUp() throws IOException {
        indexName = RandomStringBuilder.build("index-name", 8);
        PineconeClientConfig config = new PineconeClientConfig()
                .withApiKey(System.getenv("PINECONE_API_KEY"))
                .withEnvironment(System.getenv("PINECONE_ENVIRONMENT"));
        pinecone = new PineconeIndexOperationClient(config);

        // Create an index
        CreateIndexRequest request = new CreateIndexRequest()
                .withIndexName(indexName)
                .withDimension(5)
                .withMetric("euclidean");
        pinecone.createIndex(request);
    }

    @AfterEach
    public void cleanUp() throws IOException {
        pinecone.deleteIndex(indexName);
    }

    @Test
    public void configureIndexWithInvalidIndexName() {
        ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest()
                .withReplicas(2);
        try {
            checkIndexStatusReady(indexName);
            pinecone.configureIndex("non-existent-index", configureIndexRequest);
        } catch (PineconeNotFoundException exception) {
            assert (exception.getLocalizedMessage().contains("404: Not Found"));
        } catch (IOException | InterruptedException exception) {
            logger.error(exception.toString());
        }
    }

    @Test
    public void configureIndexExceedingQuota() {
        ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest()
                .withReplicas(20);
        try {
            checkIndexStatusReady(indexName);
            pinecone.configureIndex(indexName, configureIndexRequest);
        } catch (PineconeBadRequestException exception) {
            assert (exception.getLocalizedMessage().contains("The index exceeds the project " +
                    "quota of 5 pods by 16 pods."));
            assert (exception.getLocalizedMessage().contains("Upgrade your account or change" +
                    " the project settings to increase the quota."));
        } catch (Exception exception) {
            logger.error(exception.toString());
        }
    }

    @Test
    public void scaleUp() {
        try{
            // Verify the starting state
            IndexMeta indexMeta = pinecone.describeIndex(indexName);
            assertEquals(1, indexMeta.getDatabase().getReplicas());

            // Configure the index
            ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest()
                    .withReplicas(2);
            checkIndexStatusReady(indexName);
            pinecone.configureIndex(indexName, configureIndexRequest);

            // Verify replicas were scaled up
            indexMeta = pinecone.describeIndex(indexName);
            assertEquals(2, indexMeta.getDatabase().getReplicas());
        } catch (Exception exception) {
            logger.error(exception.toString());
        }
    }

    @Test
    public void scaleDown() {
        try {
            // Verify the starting state
            IndexMeta indexMeta = pinecone.describeIndex(indexName);
            assertEquals(1, indexMeta.getDatabase().getReplicas());

            // Scale up for the test
            ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest()
                    .withReplicas(3);
            checkIndexStatusReady(indexName);
            pinecone.configureIndex(indexName, configureIndexRequest);

            // Verify the scaled up replicas
            indexMeta = pinecone.describeIndex(indexName);
            assertEquals(3, indexMeta.getDatabase().getReplicas());

            // Scaling down
            configureIndexRequest = new ConfigureIndexRequest()
                    .withReplicas(1);
            checkIndexStatusReady(indexName);
            pinecone.configureIndex(indexName, configureIndexRequest);

            // Verify replicas were scaled down
            indexMeta = pinecone.describeIndex(indexName);
            assertEquals(1, indexMeta.getDatabase().getReplicas());
        } catch (Exception exception) {
            logger.error(exception.toString());
        }
    }

    @Test
    public void changingBasePodType() {
        try {
            // Verify the starting state
            IndexMeta indexMeta = pinecone.describeIndex(indexName);
            assertEquals("p1.x1", indexMeta.getDatabase().getPodType());

            // Try to change the base pod type
            ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest()
                    .withPodType("p2.x1");

            checkIndexStatusReady(indexName);
            pinecone.configureIndex(indexName, configureIndexRequest);
        } catch (PineconeBadRequestException pineconeBadRequestException) {
            assertEquals(pineconeBadRequestException.getMessage(), "updating base pod type is not supported");
        } catch (Exception exception) {
            logger.error(exception.getLocalizedMessage());
        }
    }

    @Test
    public void sizeIncrease() {
        try {
            // Verify the starting state
            IndexMeta indexMeta = pinecone.describeIndex(indexName);
            assertEquals("p1.x1", indexMeta.getDatabase().getPodType());

            // Change the pod type to a larger one
            ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest()
                    .withPodType("p1.x2");
            checkIndexStatusReady(indexName);
            pinecone.configureIndex(indexName, configureIndexRequest);

            // Get the index description to verify the new pod type
            indexMeta = pinecone.describeIndex(indexName);
            assertEquals("p1.x2", indexMeta.getDatabase().getPodType());
        } catch (Exception exception) {
            logger.error(exception.getLocalizedMessage());
        }
    }

    @Test
    public void sizeDown() {
        try {
            // Verify the starting state
            IndexMeta indexMeta = pinecone.describeIndex(indexName);
            assertEquals("p1.x1", indexMeta.getDatabase().getPodType());

            // Increase the pod type
            ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest()
                    .withPodType("p1.x2");
            checkIndexStatusReady(indexName);
            pinecone.configureIndex(indexName, configureIndexRequest);

            // Get the index description to verify the new pod type
            indexMeta = pinecone.describeIndex(indexName);
            assertEquals("p1.x2", indexMeta.getDatabase().getPodType());

            // Attempt to scale down
            configureIndexRequest = new ConfigureIndexRequest()
                    .withPodType("p1.x1");
            pinecone.configureIndex(indexName, configureIndexRequest);
        } catch (Exception exception) {
            assertEquals(exception.getClass(), PineconeBadRequestException.class);
            assertEquals(exception.getMessage(), "scaling down pod type is not supported");
        }
    }

    private void checkIndexStatusReady(String indexName) throws IOException, InterruptedException {
        while (true) {
            IndexMeta indexMeta = pinecone.describeIndex(indexName);
            if (indexMeta.getStatus().getState().equalsIgnoreCase("ready")) {
                break;
            }

            Thread.sleep(500);
        }
    }
}
