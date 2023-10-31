package io.pinecone.integration;

import io.pinecone.PineconeClientConfig;
import io.pinecone.PineconeIndexOperationClient;
import io.pinecone.exceptions.PineconeBadRequestException;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.model.ConfigureIndexRequest;
import io.pinecone.model.CreateIndexRequest;
import io.pinecone.model.IndexMeta;
import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigureIndexTest {
    private PineconeIndexOperationClient pinecone;
    private String indexName = RandomStringBuilder.build("index-name", 8);

    @BeforeEach
    public void setUp() throws IOException{
        PineconeClientConfig config = new PineconeClientConfig()
                .withApiKey(System.getenv("PINECONE_API_KEY"))
                .withEnvironment(System.getenv("PINECONE_ENVIRONMENT"))
                .withServerSideTimeoutSec(10);
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

    @Nested
    class scalingReplicas {
        @Test
        public void scaleUp() throws IOException {
            // Verify the starting state
            IndexMeta indexMeta = pinecone.describeIndex(indexName);
            assertEquals(1, indexMeta.getDatabase().getReplicas());

            // Configure the index
            ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest()
                    .withReplicas(2);
            pinecone.configureIndex(indexName, configureIndexRequest);

            // Verify replicas were scaled up
            indexMeta = pinecone.describeIndex(indexName);
            assertEquals(2, indexMeta.getDatabase().getReplicas());
        }

        @Test
        public void scaleDown() throws IOException {
            // Verify the starting state
            IndexMeta indexMeta = pinecone.describeIndex(indexName);
            assertEquals(1, indexMeta.getDatabase().getReplicas());

            // Scale up for the test
            ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest()
                    .withReplicas(3);
            pinecone.configureIndex(indexName, configureIndexRequest);

            // Verify the scaled up replicas
            indexMeta = pinecone.describeIndex(indexName);
            assertEquals(3, indexMeta.getDatabase().getReplicas());

            // Scaling down
            configureIndexRequest = new ConfigureIndexRequest()
                    .withReplicas(2);
            pinecone.configureIndex(indexName, configureIndexRequest);

            // Verify replicas were scaled down
            indexMeta = pinecone.describeIndex(indexName);
            assertEquals(2, indexMeta.getDatabase().getReplicas());
        }
    }

    @Nested
    class ScalingPodTypeTests {
        @Test
        public void changingBasePodType() throws IOException {
            // Verify the starting state
            IndexMeta indexMeta = pinecone.describeIndex(indexName);
            assertEquals("p1.x1", indexMeta.getDatabase().getPodType());

            // Try to change the base pod type
            ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest()
                    .withPodType("p2.x1");
            try {
                pinecone.configureIndex(indexName, configureIndexRequest);
            } catch (Exception exception) {
                assertEquals(exception.getClass(), PineconeBadRequestException.class);
                assertEquals(exception.getMessage(), "updating base pod type is not supported");
            }
        }

        @Test
        public void sizeIncrease() throws IOException {
            // Verify the starting state
            IndexMeta indexMeta = pinecone.describeIndex(indexName);
            assertEquals("p1.x1", indexMeta.getDatabase().getPodType());

            // Change the pod type to a larger one
            ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest()
                    .withPodType("p1.x2");
            pinecone.configureIndex(indexName, configureIndexRequest);

            // Get the index description to verify the new pod type
            indexMeta = pinecone.describeIndex(indexName);
            assertEquals("p1.x2", indexMeta.getDatabase().getPodType());
        }

        @Test
        public void sizeDown() throws IOException {
            // Verify the starting state
            IndexMeta indexMeta = pinecone.describeIndex(indexName);
            assertEquals("p1.x1", indexMeta.getDatabase().getPodType());

            // Increase the pod type
            ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest()
                    .withPodType("p1.x2");
            pinecone.configureIndex(indexName, configureIndexRequest);

            // Get the index description to verify the new pod type
            indexMeta = pinecone.describeIndex(indexName);
            assertEquals("p1.x2", indexMeta.getDatabase().getPodType());

            // Attempt to scale down
            configureIndexRequest = new ConfigureIndexRequest()
                    .withPodType("p1.x1");
            try {
                pinecone.configureIndex(indexName, configureIndexRequest);
            } catch (Exception exception) {
                assertEquals(exception.getClass(), PineconeBadRequestException.class);
                assertEquals(exception.getMessage(), "scaling down pod type is not supported");
            }

            // Get the index description to verify the pod type remains unchanged
            indexMeta = pinecone.describeIndex(indexName);
            assertEquals("p1.x2", indexMeta.getDatabase().getPodType());
        }
    }
}
