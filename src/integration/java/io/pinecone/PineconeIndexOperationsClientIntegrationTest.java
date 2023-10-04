package io.pinecone.integration;

import io.pinecone.PineconeClient;
import io.pinecone.PineconeClientConfig;
import io.pinecone.PineconeClientLiveIntegTest;
import io.pinecone.PineconeIndexOperationClient;
import io.pinecone.model.CreateIndexRequest;
import io.pinecone.model.IndexMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PineconeIndexOperationsClientIntegrationTest {
    private PineconeIndexOperationClient pinecone;

    @BeforeEach
    public void setUp() throws Exception {
        PineconeClientConfig config = new PineconeClientConfig()
                .withApiKey(System.getenv("PINECONE_API_KEY"))
                .withEnvironment(System.getenv("PINECONE_ENVIRONMENT"))
                .withServerSideTimeoutSec(10);
        pinecone = new PineconeIndexOperationClient(config);
    }

    @Test
    public void createAndDelete() throws IOException {
        String indexName = "banana";

        // Create an index
        CreateIndexRequest request = new CreateIndexRequest()
                .withIndexName(indexName)
                .withDimension(10)
                .withMetric("euclidean");
        pinecone.createIndex(request);

        // Get the index description
        IndexMeta indexMeta = pinecone.describeIndex(indexName);
        assertNotNull(indexMeta);
        assertEquals(10, indexMeta.getDatabase().getDimension());
        assertEquals(indexName, indexMeta.getDatabase().getName());
        assertEquals("euclidean", indexMeta.getDatabase().getMetric());

        // Cleanup
        pinecone.deleteIndex(indexName);
    }
}
