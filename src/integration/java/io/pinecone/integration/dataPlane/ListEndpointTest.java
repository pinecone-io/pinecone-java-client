package io.pinecone.integration.dataPlane;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.pinecone.clients.AsyncIndex;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.helpers.TestIndexResourcesManager;
import io.pinecone.proto.ListResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ListEndpointTest {
    private static final TestIndexResourcesManager indexManager = TestIndexResourcesManager.getInstance();
    private static String indexName ;
    private static Index indexConnection;
    private static AsyncIndex asyncIndexConnection;

    @BeforeAll
    public static void setUp() throws InterruptedException {
        indexName = indexManager.getServerlessIndexName();
        indexConnection = indexManager.getServerlessIndexConnection();
        asyncIndexConnection = indexManager.getServerlessAsyncIndexConnection();
    }

    @AfterAll
    public static void cleanUp() {
        String apiKey = System.getenv("PINECONE_API_KEY");
        Pinecone pinecone = new Pinecone.Builder(apiKey).build();
        pinecone.deleteIndex(indexName);
        indexConnection.close();
        asyncIndexConnection.close();
    }

    @Test
    public void testListEndpoint() throws InterruptedException {
        Thread.sleep(10000); // wait for the index to be ready for operations

        // Sync
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

        //Async
        ListenableFuture<ListResponse> futureResponse = asyncIndexConnection.list("example-namespace");
        ListResponse asyncListResponse = Futures.getUnchecked(futureResponse);
        assertTrue(asyncListResponse.toString().contains("id1"));
        assertTrue(asyncListResponse.toString().contains("id2"));
        assertTrue(asyncListResponse.toString().contains("cidddd3"));

        ListenableFuture<ListResponse> futureResponseWithPrefix = asyncIndexConnection.list("example-namespace", "i");
        ListResponse asyncListResponseWithPrefix = Futures.getUnchecked(futureResponseWithPrefix);
        assertTrue(asyncListResponseWithPrefix.toString().contains("id1"));
        assertTrue(asyncListResponseWithPrefix.toString().contains("id2"));
        assertFalse(asyncListResponseWithPrefix.toString().contains("cidddd3"));

        ListenableFuture<ListResponse> futureResponseWithLimit = asyncIndexConnection.list("example-namespace", 1);
        ListResponse asyncListResponseWithLimit = Futures.getUnchecked(futureResponseWithLimit);
        assertTrue(asyncListResponseWithLimit.toString().contains("cidddd3"));
        assertFalse(asyncListResponseWithLimit.toString().contains("id2"));
        assertFalse(asyncListResponseWithLimit.toString().contains("id1"));
    }

}
