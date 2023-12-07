package io.pinecone;

import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.model.CreateIndexRequest;
import io.pinecone.model.IndexMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PineconeIndexOperationsClientIntegrationTest {
    private PineconeIndexOperationClient pinecone;

    @BeforeEach
    public void setUp() {
        PineconeClientConfig config = new PineconeClientConfig()
                .withApiKey(System.getenv("PINECONE_API_KEY"))
                .withEnvironment(System.getenv("PINECONE_ENVIRONMENT"))
                .withServerSideTimeoutSec(10);
        pinecone = new PineconeIndexOperationClient(config);
    }

    @Test
    public void createAndDelete() throws IOException, InterruptedException {
        String indexName = RandomStringBuilder.build("index-name", 8);

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

        // List the index
        List<String> indexList = pinecone.listIndexes();
        assert !indexList.isEmpty();

        // Cleanup
        pinecone.deleteIndex(indexName);
        Thread.sleep(3500);
    }
}
