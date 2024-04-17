package io.pinecone.integration.dataPlane;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.pinecone.clients.AsyncIndex;
import io.pinecone.clients.Index;
import io.pinecone.helpers.TestIndexResourcesManager;
import io.pinecone.proto.ListResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ListEndpointTest {
    private static final TestIndexResourcesManager indexManager = TestIndexResourcesManager.getInstance();
    private static Index indexConnection;
    private static AsyncIndex asyncIndexConnection;

    @BeforeAll
    public static void setUp() throws InterruptedException {
        indexManager.getServerlessIndexName(); // create serverless index
        indexConnection = indexManager.getServerlessIndexConnection();
        asyncIndexConnection = indexManager.getServerlessAsyncIndexConnection();
    }

    @AfterAll
    public static void cleanUp() {
        indexConnection.close();
        asyncIndexConnection.close();
    }

    @Test
    public void testSyncListEndpoint() throws InterruptedException {
        // Confirm all vector IDs from namespace are returned
        String listResponse = indexConnection.list("example-namespace").toString();
        assertTrue(listResponse.contains("id1"));
        assertTrue(listResponse.contains("id2"));
        assertTrue(listResponse.contains("prefix-id3"));

        // Confirm all vector IDs from namespace are returned
        String listResponseWithPrefix = indexConnection.list("example-namespace", "i").toString();
        assertTrue(listResponseWithPrefix.contains("id1"));
        assertTrue(listResponseWithPrefix.contains("id2"));
        assertFalse(listResponseWithPrefix.contains("prefix-id3")); // should not be in response

        // Confirm all vector IDs from namespace are returned, with a limit of 1
        ListResponse listResponseWithLimit = indexConnection.list("example-namespace", 1);
        assertEquals(1, listResponseWithLimit.getVectorsList().size());
    }

    @Test
    public void testAsyncListEndpoint() throws InterruptedException {
        // Confirm all vector IDs from namespace are returned
        ListenableFuture<ListResponse> futureResponse = asyncIndexConnection.list("example-namespace");
        ListResponse asyncListResponse = Futures.getUnchecked(futureResponse);
        assertTrue(asyncListResponse.toString().contains("id1"));
        assertTrue(asyncListResponse.toString().contains("id2"));
        assertTrue(asyncListResponse.toString().contains("prefix-id3"));

        // Confirm all vector IDs from namespace are returned, filtered by given prefix
        ListenableFuture<ListResponse> futureResponseWithPrefix = asyncIndexConnection.list("example-namespace", "i");
        ListResponse asyncListResponseWithPrefix = Futures.getUnchecked(futureResponseWithPrefix);
        assertTrue(asyncListResponseWithPrefix.toString().contains("id1"));
        assertTrue(asyncListResponseWithPrefix.toString().contains("id2"));
        assertFalse(asyncListResponseWithPrefix.toString().contains("prefix-id3"));

        // Confirm all vector IDs from namespace are returned, with a limit of 1
        ListenableFuture<ListResponse> futureResponseWithLimit = asyncIndexConnection.list("example-namespace", 1);
        ListResponse asyncListResponseWithLimit = Futures.getUnchecked(futureResponseWithLimit);
        assertEquals(1, asyncListResponseWithLimit.getVectorsList().size());
    }

}
