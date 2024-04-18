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
    private static String customNamespace;
    private static String defaultNamespace;

    @BeforeAll
    public static void setUp() throws InterruptedException {
        indexManager.getServerlessIndexName(); // creates serverless index
        indexConnection = indexManager.getServerlessIndexConnection();
        asyncIndexConnection = indexManager.getServerlessAsyncIndexConnection();
        customNamespace = indexManager.getCustomNamespace();
        defaultNamespace = indexManager.getDefaultNamespace();
    }

    @AfterAll
    public static void cleanUp() {
        indexConnection.close();
        asyncIndexConnection.close();
    }

    @Test
    public void testSyncListEndpoint() throws InterruptedException {
        // Confirm all vector IDs from index are returned when do not pass namespace
        String listResponseNoArgs = indexConnection.list().toString();
        assertTrue(listResponseNoArgs.contains("id1"));
        assertTrue(listResponseNoArgs.contains("id2"));
        assertTrue(listResponseNoArgs.contains("prefix-id3"));
        assertEquals(defaultNamespace, indexConnection.list().getNamespace()); // namespace should be defaultNamespace

        // Confirm all vector IDs from namespace are returned
        String listResponse = indexConnection.list(customNamespace).toString();
        assertTrue(listResponse.contains("id1"));
        assertTrue(listResponse.contains("id2"));
        assertTrue(listResponse.contains("prefix-id3"));

        // Confirm all vector IDs from namespace are returned
        String listResponseWithPrefix = indexConnection.list(customNamespace, "i").toString();
        assertTrue(listResponseWithPrefix.contains("id1"));
        assertTrue(listResponseWithPrefix.contains("id2"));
        assertFalse(listResponseWithPrefix.contains("prefix-id3")); // should not be in response

        // Confirm all vector IDs from namespace are returned, with a limit of 1
        ListResponse listResponseWithLimit = indexConnection.list(customNamespace, 1);
        assertEquals(1, listResponseWithLimit.getVectorsList().size());
    }

    @Test
    public void testAsyncListEndpoint() throws InterruptedException {
        // Confirm all vector IDs from index are returned when do not pass namespace
        ListenableFuture<ListResponse> futureResponseNoArgs = asyncIndexConnection.list();
        ListResponse asyncListResponseNoArgs = Futures.getUnchecked(futureResponseNoArgs);
        assertTrue(asyncListResponseNoArgs.toString().contains("id1"));
        assertTrue(asyncListResponseNoArgs.toString().contains("id2"));
        assertTrue(asyncListResponseNoArgs.toString().contains("prefix-id3"));
        assertEquals(defaultNamespace, asyncListResponseNoArgs.getNamespace()); // namespace should be defaultNamespace

        // Confirm all vector IDs from namespace are returned
        ListenableFuture<ListResponse> futureResponse = asyncIndexConnection.list(customNamespace);
        ListResponse asyncListResponse = Futures.getUnchecked(futureResponse);
        assertTrue(asyncListResponse.toString().contains("id1"));
        assertTrue(asyncListResponse.toString().contains("id2"));
        assertTrue(asyncListResponse.toString().contains("prefix-id3"));

        // Confirm all vector IDs from namespace are returned, filtered by given prefix
        ListenableFuture<ListResponse> futureResponseWithPrefix = asyncIndexConnection.list(customNamespace, "i");
        ListResponse asyncListResponseWithPrefix = Futures.getUnchecked(futureResponseWithPrefix);
        assertTrue(asyncListResponseWithPrefix.toString().contains("id1"));
        assertTrue(asyncListResponseWithPrefix.toString().contains("id2"));
        assertFalse(asyncListResponseWithPrefix.toString().contains("prefix-id3"));

        // Confirm all vector IDs from namespace are returned, with a limit of 1
        ListenableFuture<ListResponse> futureResponseWithLimit = asyncIndexConnection.list(customNamespace, 1);
        ListResponse asyncListResponseWithLimit = Futures.getUnchecked(futureResponseWithLimit);
        assertEquals(1, asyncListResponseWithLimit.getVectorsList().size());
    }

}
