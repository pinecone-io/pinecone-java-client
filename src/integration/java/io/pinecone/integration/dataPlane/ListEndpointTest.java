package io.pinecone.integration.dataPlane;

import io.pinecone.clients.AsyncIndex;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.helpers.TestIndexResourcesManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ListEndpointTest {
    private static final TestIndexResourcesManager indexManager = TestIndexResourcesManager.getInstance();
    private static String indexName ;
    private static Index indexConnection;

    @BeforeAll
    public static void setUp() throws InterruptedException {
        indexName = indexManager.getServerlessIndexName();
        indexConnection = indexManager.getIndexConnection();
//        asyncIndex = pinecone.getAsyncIndexConnection(indexName);
    }

    @AfterAll
    public static void cleanUp() {
        String apiKey = System.getenv("PINECONE_API_KEY");
        Pinecone pinecone = new Pinecone.Builder(apiKey).build();

        pinecone.deleteIndex(indexName);
        indexConnection.close();
//        asyncIndex.close();
    }

    @Test
    public void testListEndpoint() throws InterruptedException {
        Thread.sleep(10000); // wait for the index to be ready for operations

        String listResponse = indexConnection.list("example-namespace").toString();
        assertTrue(listResponse.contains("id1"));
        assertTrue(listResponse.contains("id2"));
        assertTrue(listResponse.contains("cidddd3"));

        String listResponseWithPrefix = indexConnection.list("example-namespace", "i").toString();
        assertTrue(listResponseWithPrefix.contains("id1"));
        assertTrue(listResponseWithPrefix.contains("id2"));
        assertFalse(listResponseWithPrefix.contains("cidddd3")); // should not be in response

        String listResponseWithLimit = indexConnection.list("example-namespace", 1).toString();
        assertTrue(listResponseWithLimit.contains("cidddd3")); // (IDs returned in LIFO order)
        assertFalse(listResponseWithLimit.contains("id2")); // should not be in response
        assertFalse(listResponseWithLimit.contains("id1")); // should not be in response
    }

}
