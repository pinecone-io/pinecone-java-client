package io.pinecone.integration.dataPlane;

import com.google.common.util.concurrent.ListenableFuture;
import io.pinecone.clients.AsyncIndex;
import io.pinecone.clients.Index;
import io.pinecone.helpers.TestResourcesManager;
import io.pinecone.integration.controlPlane.pod.ConfigureIndexTest;
import io.pinecone.proto.ListResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;


public class ListEndpointTest {
    private static final TestResourcesManager indexManager = TestResourcesManager.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(ConfigureIndexTest.class);
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
        Index.ListRequestBuilder listResponseNoArgs = indexConnection.list();
        assertEquals(listResponseNoArgs.build().getVectorsList().size(), 4);
        assertTrue(listResponseNoArgs.build().getVectorsList().toString().contains("def-id1"));
        assertTrue(listResponseNoArgs.build().getVectorsList().toString().contains("def-id2"));
        assertTrue(listResponseNoArgs.build().getVectorsList().toString().contains("def-prefix-id3"));
        assertTrue(listResponseNoArgs.build().getVectorsList().toString().contains("def-prefix-id4"));

        // Confirm all vector IDs from custom namespace are returned when pass customNamespace
        Index.ListRequestBuilder listResponseCustomNamespace = indexConnection.list().setNamespace(customNamespace);
        assertTrue(listResponseCustomNamespace.build().getVectorsList().toString().contains("cus-id1"));
        assertTrue(listResponseCustomNamespace.build().getVectorsList().toString().contains("cus-id2"));
        assertTrue(listResponseCustomNamespace.build().getVectorsList().toString().contains("cus-prefix-id3"));
        assertTrue(listResponseCustomNamespace.build().getVectorsList().toString().contains("cus-prefix-id4"));

        // Confirm all vector IDs from custom namespace are returned, filtered by given prefix
        Index.ListRequestBuilder listResponseCustomNamespaceWithPrefix =
                indexConnection.list().setNamespace(customNamespace).setPrefix("cus-prefix-");
        assertEquals(listResponseCustomNamespaceWithPrefix.build().getVectorsList().size(), 2);
        assertTrue(listResponseCustomNamespaceWithPrefix.build().getVectorsList().toString().contains("cus-prefix-id3"));
        assertTrue(listResponseCustomNamespaceWithPrefix.build().getVectorsList().toString().contains("cus-prefix-id4"));

        // Confirm all vector IDs from custom namespace are returned when limit is specified
        Index.ListRequestBuilder listResponseWithLimit = indexConnection.list().setNamespace(customNamespace).setLimit(1);
        assertEquals(1, listResponseWithLimit.build().getVectorsList().size());
    }

    @Test
    public void testAsyncListEndpoint() {
        // Confirm default vector IDs are returned when no namespace is specified
        ListenableFuture<ListResponse> futureResponseNoArgs = asyncIndexConnection.list().build();
        futureResponseNoArgs.addListener(() -> {
            try {
                ListResponse response = futureResponseNoArgs.get();
                assertEquals(response.getVectorsList().size(), 4);
                assertTrue(response.getVectorsList().toString().contains("def-id1"));
                assertTrue(response.getVectorsList().toString().contains("def-id2"));
                assertTrue(response.getVectorsList().toString().contains("def-prefix-id3"));
                assertTrue(response.getVectorsList().toString().contains("def-prefix-id4"));
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error occurred while fetching list response", e);
            }
        }, Executors.newSingleThreadExecutor());

        // Confirm all vector IDs from custom namespace are returned when pass customNamespace
        ListenableFuture<ListResponse> futureResponseCustomNamespace = asyncIndexConnection.list().setNamespace(customNamespace).build();
        futureResponseCustomNamespace.addListener(() -> {
            try {
                ListResponse response = futureResponseCustomNamespace.get();
                assertEquals(response.getVectorsList().size(), 4);
                assertTrue(response.getVectorsList().toString().contains("cus-id1"));
                assertTrue(response.getVectorsList().toString().contains("cus-id2"));
                assertTrue(response.getVectorsList().toString().contains("cus-prefix-id3"));
                assertTrue(response.getVectorsList().toString().contains("cus-prefix-id4"));
                assertTrue(response.getNamespace().contains("example-namespace"));
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error occurred while fetching list response", e);
            }
        }, Executors.newSingleThreadExecutor());

        // Confirm all vector IDs from custom namespace are returned, filtered by given prefix
        ListenableFuture<ListResponse> futureResponseCustomNamespaceWithPrefix =
                asyncIndexConnection.list().setNamespace(customNamespace).setPrefix("cus-prefix-").build();
        futureResponseCustomNamespaceWithPrefix.addListener(() -> {
            try {
                ListResponse response = futureResponseCustomNamespaceWithPrefix.get();
                assertEquals(response.getVectorsList().size(), 2);
                assertTrue(response.getVectorsList().toString().contains("cus-prefix-id3"));
                assertTrue(response.getVectorsList().toString().contains("cus-prefix-id4"));
                assertTrue(response.getNamespace().contains("example-namespace"));
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error occurred while fetching list response", e);
            }
        }, Executors.newSingleThreadExecutor());

        // Confirm only 1 vector ID from custom namespace is returned when limit is 1
        ListenableFuture<ListResponse> futureResponseWithLimit =
                asyncIndexConnection.list().setNamespace(customNamespace).setLimit(1).build();
        futureResponseWithLimit.addListener(() -> {
            try {
                ListResponse response = futureResponseWithLimit.get();
                assertEquals(response.getVectorsList().size(), 1);
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error occurred while fetching list response", e);
            }
        }, Executors.newSingleThreadExecutor());

    }

}
