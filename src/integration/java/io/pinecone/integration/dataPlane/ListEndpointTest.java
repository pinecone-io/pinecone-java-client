package io.pinecone.integration.dataPlane;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.pinecone.clients.AsyncIndex;
import io.pinecone.clients.Index;
import io.pinecone.helpers.TestResourcesManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ListEndpointTest {
    private static final TestResourcesManager indexManager = TestResourcesManager.getInstance();
    private static Index indexConnection;
    private static AsyncIndex asyncIndexConnection;
    private static String customNamespace;

    @BeforeAll
    public static void setUp() throws InterruptedException {
        indexManager.getOrCreateServerlessIndex();
        indexConnection = indexManager.getOrCreateServerlessIndexConnection();
        asyncIndexConnection = indexManager.getOrCreateServerlessAsyncIndexConnection();
        customNamespace = indexManager.getCustomNamespace();
    }

    @AfterAll
    public static void cleanUp() {
        indexConnection.close();
        asyncIndexConnection.close();
    }

    @Test
    public void testSyncListEndpoint() throws InterruptedException {
        // Confirm default vector IDs are returned when no namespace is specified
        ListResponse listResponseNoArgs = indexConnection.list();
        assertEquals(listResponseNoArgs.getVectorsList().size(), 4);
        assertTrue(listResponseNoArgs.getVectorsList().toString().contains("def-id1"));
        assertTrue(listResponseNoArgs.getVectorsList().toString().contains("def-id2"));
        assertTrue(listResponseNoArgs.getVectorsList().toString().contains("def-prefix-id3"));
        assertTrue(listResponseNoArgs.getVectorsList().toString().contains("def-prefix-id4"));

        // Confirm all vector IDs from custom namespace are returned when pass customNamespace
        ListResponse listResponseCustomNamespace = indexConnection.list(customNamespace);
        assertEquals(listResponseCustomNamespace.getVectorsList().size(), 4);
        assertTrue(listResponseCustomNamespace.getVectorsList().toString().contains("cus-id1"));
        assertTrue(listResponseCustomNamespace.getVectorsList().toString().contains("cus-id2"));
        assertTrue(listResponseCustomNamespace.getVectorsList().toString().contains("cus-prefix-id3"));
        assertTrue(listResponseCustomNamespace.getVectorsList().toString().contains("cus-prefix-id4"));

        // Confirm all vector IDs from custom namespace are returned, filtered by given prefix
        ListResponse listResponseCustomNamespaceWithPrefix = indexConnection.list(customNamespace, "cus-prefix-");
        assertEquals(listResponseCustomNamespaceWithPrefix.getVectorsList().size(), 2);
        assertTrue(listResponseCustomNamespaceWithPrefix.getVectorsList().toString().contains("cus-prefix-id3"));
        assertTrue(listResponseCustomNamespaceWithPrefix.getVectorsList().toString().contains("cus-prefix-id4"));

        // Confirm all vector IDs from custom namespace are returned when limit is specified
        ListResponse listResponseWithLimit = indexConnection.list(customNamespace, 1);
        assertEquals(1, listResponseWithLimit.getVectorsList().size());

        // Confirm all vector IDs from custom namespace are returned using pagination
        ListResponse listResponseWithPaginationNoPrefix1 = indexConnection.list(customNamespace, 2);
        assertEquals(listResponseWithPaginationNoPrefix1.getVectorsList().size(), 2);
        ListResponse listResponseWithPaginationNoPrefix2 = indexConnection.list(
                customNamespace,
                2,
                listResponseWithPaginationNoPrefix1.getPagination().getNext()
        );
        assertEquals(listResponseWithPaginationNoPrefix2.getVectorsList().size(), 2);
    }

    @Test
    public void testAsyncListEndpoint() throws InterruptedException {
        // Confirm default vector IDs are returned when no namespace is specified
        ListenableFuture<ListResponse> futureResponseNoArgs = asyncIndexConnection.list();
        ListResponse asyncListResponseNoArgs = Futures.getUnchecked(futureResponseNoArgs);
        assertEquals(asyncListResponseNoArgs.getVectorsList().size(), 4);
        assertTrue(asyncListResponseNoArgs.getVectorsList().toString().contains("def-id1"));
        assertTrue(asyncListResponseNoArgs.getVectorsList().toString().contains("def-id2"));
        assertTrue(asyncListResponseNoArgs.getVectorsList().toString().contains("def-prefix-id3"));
        assertTrue(asyncListResponseNoArgs.getVectorsList().toString().contains("def-prefix-id4"));

        // Confirm all vector IDs from custom namespace are returned when pass customNamespace
        ListenableFuture<ListResponse> futureResponseCustomNamespace = asyncIndexConnection.list(customNamespace);
        ListResponse asyncListResponseCustomNamespace = Futures.getUnchecked(futureResponseCustomNamespace);
        assertTrue(asyncListResponseCustomNamespace.getVectorsList().toString().contains("cus-id1"));
        assertTrue(asyncListResponseCustomNamespace.getVectorsList().toString().contains("cus-id2"));
        assertTrue(asyncListResponseCustomNamespace.getVectorsList().toString().contains("cus-prefix-id3"));
        assertTrue(asyncListResponseCustomNamespace.getVectorsList().toString().contains("cus-prefix-id4"));

        // Confirm all vector IDs from custom namespace are returned, filtered by given prefix
        ListenableFuture<ListResponse> futureResponseCustomNamespaceWithPrefix =
                asyncIndexConnection.list(customNamespace, "cus-prefix-");
        ListResponse asyncListResponseCustomNamespaceWithPrefix = Futures.getUnchecked(futureResponseCustomNamespaceWithPrefix);
        assertEquals(asyncListResponseCustomNamespaceWithPrefix.getVectorsList().size(), 2);
        assertTrue(asyncListResponseCustomNamespaceWithPrefix.getVectorsList().toString().contains("cus-prefix-id3"));
        assertTrue(asyncListResponseCustomNamespaceWithPrefix.getVectorsList().toString().contains("cus-prefix-id4"));

        // Confirm all vector IDs from custom namespace are returned when limit is specified
        ListenableFuture<ListResponse> futureResponseWithLimit = asyncIndexConnection.list(customNamespace, 1);
        ListResponse asyncListResponseWithLimit = Futures.getUnchecked(futureResponseWithLimit);
        assertEquals(1, asyncListResponseWithLimit.getVectorsList().size());

        // Confirm all vector IDs from custom namespace are returned using pagination
        ListenableFuture<ListResponse> futureResponseWithPaginationNoPrefix1 = asyncIndexConnection.list(customNamespace, 2);
        ListResponse asyncListResponseWithPaginationNoPrefix1 = Futures.getUnchecked(futureResponseWithPaginationNoPrefix1);
        assertEquals(asyncListResponseWithPaginationNoPrefix1.getVectorsList().size(), 2);
        ListenableFuture<ListResponse> futureResponseWithPaginationNoPrefix2 = asyncIndexConnection.list(
                customNamespace,
                2,
                asyncListResponseWithPaginationNoPrefix1.getPagination().getNext()
        );
        ListResponse asyncListResponseWithPaginationNoPrefix2 = Futures.getUnchecked(futureResponseWithPaginationNoPrefix2);
        assertEquals(asyncListResponseWithPaginationNoPrefix2.getVectorsList().size(), 2);
    }
}
