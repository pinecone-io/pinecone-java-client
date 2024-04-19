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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ListEndpointTest {
    private static final TestIndexResourcesManager indexManager = TestIndexResourcesManager.getInstance();
    private static Index indexConnection;
    private static AsyncIndex asyncIndexConnection;
    private static String customNamespace;
    private static List<String> defaultVectorIDs;
    private static List<String> customVectorIDs;
    private static List<String> allVectorIds;

    @BeforeAll
    public static void setUp() throws InterruptedException {
        indexManager.getServerlessIndexName(); // creates serverless index
        indexConnection = indexManager.getServerlessIndexConnection();
        asyncIndexConnection = indexManager.getServerlessAsyncIndexConnection();
        customNamespace = indexManager.getCustomNamespace();

        // Grab IDs for testing
        defaultVectorIDs = indexManager.getVectorIdsFromDefaultNamespace();
        customVectorIDs = indexManager.getVectorIdsFromCustomNamespace();
        allVectorIds = new ArrayList<>();
        allVectorIds.addAll(customVectorIDs);
        allVectorIds.addAll(defaultVectorIDs);
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

        assertEquals(listResponseNoArgs.getVectorsList().size(), defaultVectorIDs.size());

        assertTrue(listResponseNoArgs.getVectorsList().toString().contains(defaultVectorIDs.get(0)));
        assertTrue(listResponseNoArgs.getVectorsList().toString().contains(defaultVectorIDs.get(1)));
        assertTrue(listResponseNoArgs.getVectorsList().toString().contains(defaultVectorIDs.get(2)));

        // Confirm all vector IDs from custom namespace are returned when pass customNamespace
        ListResponse listResponseCustomNamespace = indexConnection.list(customNamespace);

        assertTrue(listResponseCustomNamespace.getVectorsList().toString().contains(customVectorIDs.get(0)));
        assertTrue(listResponseCustomNamespace.getVectorsList().toString().contains(customVectorIDs.get(1)));
        assertTrue(listResponseCustomNamespace.getVectorsList().toString().contains(customVectorIDs.get(2)));

        // Confirm all vector IDs from custom namespace are returned, filtered by given prefix
        ListResponse listResponseCustomNamespaceWithPrefix = indexConnection.list(customNamespace, "cus-prefix-");
        List<String> filteredCustomVectorIDs = new ArrayList<>();
        for (String vectorID : customVectorIDs) {
            if (vectorID.startsWith("cus-prefix-")) {
                filteredCustomVectorIDs.add(vectorID);
            }
        }
        assertEquals(listResponseCustomNamespaceWithPrefix.getVectorsList().size(), filteredCustomVectorIDs.size());

        assertTrue(listResponseCustomNamespaceWithPrefix.getVectorsList().toString().contains(customVectorIDs.get(2)));
        assertTrue(listResponseCustomNamespaceWithPrefix.getVectorsList().toString().contains(customVectorIDs.get(3)));

        // Confirm all vector IDs from custom namespace are returned when limit is specified
        ListResponse listResponseWithLimit = indexConnection.list(customNamespace, 1);

        assertEquals(1, listResponseWithLimit.getVectorsList().size());
    }

    @Test
    public void testAsyncListEndpoint() throws InterruptedException {
        // Confirm default vector IDs are returned when no namespace is specified
        ListenableFuture<ListResponse> futureResponseNoArgs = asyncIndexConnection.list();
        ListResponse asyncListResponseNoArgs = Futures.getUnchecked(futureResponseNoArgs);

        assertEquals(asyncListResponseNoArgs.getVectorsList().size(), defaultVectorIDs.size());

        assertTrue(asyncListResponseNoArgs.getVectorsList().toString().contains(defaultVectorIDs.get(0)));
        assertTrue(asyncListResponseNoArgs.getVectorsList().toString().contains(defaultVectorIDs.get(1)));
        assertTrue(asyncListResponseNoArgs.getVectorsList().toString().contains(defaultVectorIDs.get(2)));

        // Confirm all vector IDs from custom namespace are returned when pass customNamespace
        ListenableFuture<ListResponse> futureResponseCustomNamespace = asyncIndexConnection.list(customNamespace);
        ListResponse asyncListResponseCustomNamespace = Futures.getUnchecked(futureResponseCustomNamespace);

        assertTrue(asyncListResponseCustomNamespace.getVectorsList().toString().contains(customVectorIDs.get(0)));
        assertTrue(asyncListResponseCustomNamespace.getVectorsList().toString().contains(customVectorIDs.get(1)));
        assertTrue(asyncListResponseCustomNamespace.getVectorsList().toString().contains(customVectorIDs.get(2)));

        // Confirm all vector IDs from custom namespace are returned, filtered by given prefix
        ListenableFuture<ListResponse> futureResponseCustomNamespaceWithPrefix =
                asyncIndexConnection.list(customNamespace, "cus-prefix-");
        ListResponse asyncListResponseCustomNamespaceWithPrefix = Futures.getUnchecked(futureResponseCustomNamespaceWithPrefix);

        List<String> filteredCustomVectorIDs = new ArrayList<>();
        for (String vectorID : customVectorIDs) {
            if (vectorID.startsWith("cus-prefix-")) {
                filteredCustomVectorIDs.add(vectorID);
            }
        }
        assertEquals(asyncListResponseCustomNamespaceWithPrefix.getVectorsList().size(), filteredCustomVectorIDs.size());

        assertTrue(asyncListResponseCustomNamespaceWithPrefix.getVectorsList().toString().contains(customVectorIDs.get(2)));
        assertTrue(asyncListResponseCustomNamespaceWithPrefix.getVectorsList().toString().contains(customVectorIDs.get(3)));

        // Confirm all vector IDs from custom namespace are returned when limit is specified
        ListenableFuture<ListResponse> futureResponseWithLimit = asyncIndexConnection.list(customNamespace, 1);
        ListResponse asyncListResponseWithLimit = Futures.getUnchecked(futureResponseWithLimit);

        assertEquals(1, asyncListResponseWithLimit.getVectorsList().size());
    }

}
