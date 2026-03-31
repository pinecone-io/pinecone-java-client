package io.pinecone.integration.dataPlane;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.pinecone.clients.AsyncIndex;
import io.pinecone.clients.Index;
import io.pinecone.helpers.TestResourcesManager;
import io.pinecone.proto.ListResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ListEndpointTest {
    private static final TestResourcesManager indexManager = TestResourcesManager.getInstance();
    private static Index indexConnection;
    private static AsyncIndex asyncIndexConnection;
    private static String customNamespace;

    // ID prefixes used when seeding the shared index in TestResourcesManager.
    // Filtering by these prefixes makes assertions immune to concurrent test writes.
    private static final String DEFAULT_NS_PREFIX = "def-";
    private static final String CUSTOM_NS_PREFIX = "cus-";
    private static final String CUSTOM_NS_SUBPREFIX = "cus-prefix-";

    @BeforeAll
    public static void setUp() throws Exception {
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
    public void testSyncListEndpoint() throws Exception {
        // Use prefix filtering so concurrent writes to these namespaces don't affect counts
        ListResponse listDefaultNs = indexConnection.list("", DEFAULT_NS_PREFIX);
        assertEquals(4, listDefaultNs.getVectorsList().size());
        assertTrue(listDefaultNs.getVectorsList().toString().contains("def-id1"));
        assertTrue(listDefaultNs.getVectorsList().toString().contains("def-id2"));
        assertTrue(listDefaultNs.getVectorsList().toString().contains("def-prefix-id3"));
        assertTrue(listDefaultNs.getVectorsList().toString().contains("def-prefix-id4"));

        ListResponse listCustomNs = indexConnection.list(customNamespace, CUSTOM_NS_PREFIX);
        assertEquals(4, listCustomNs.getVectorsList().size());
        assertTrue(listCustomNs.getVectorsList().toString().contains("cus-id1"));
        assertTrue(listCustomNs.getVectorsList().toString().contains("cus-id2"));
        assertTrue(listCustomNs.getVectorsList().toString().contains("cus-prefix-id3"));
        assertTrue(listCustomNs.getVectorsList().toString().contains("cus-prefix-id4"));

        // Sub-prefix filter
        ListResponse listCustomNsSubPrefix = indexConnection.list(customNamespace, CUSTOM_NS_SUBPREFIX);
        assertEquals(2, listCustomNsSubPrefix.getVectorsList().size());
        assertTrue(listCustomNsSubPrefix.getVectorsList().toString().contains("cus-prefix-id3"));
        assertTrue(listCustomNsSubPrefix.getVectorsList().toString().contains("cus-prefix-id4"));

        // Limit
        ListResponse listWithLimit = indexConnection.list(customNamespace, CUSTOM_NS_PREFIX, 1);
        assertEquals(1, listWithLimit.getVectorsList().size());

        // Pagination over exactly the 4 pre-seeded cus-* vectors
        ListResponse page1 = indexConnection.list(customNamespace, CUSTOM_NS_PREFIX, 2);
        assertEquals(2, page1.getVectorsList().size());
        ListResponse page2 = indexConnection.list(
                customNamespace, CUSTOM_NS_PREFIX, page1.getPagination().getNext(), 2);
        assertEquals(2, page2.getVectorsList().size());
    }

    @Test
    public void testAsyncListEndpoint() throws Exception {
        ListResponse asyncListDefaultNs = Futures.getUnchecked(
                asyncIndexConnection.list("", DEFAULT_NS_PREFIX));
        assertEquals(4, asyncListDefaultNs.getVectorsList().size());
        assertTrue(asyncListDefaultNs.getVectorsList().toString().contains("def-id1"));
        assertTrue(asyncListDefaultNs.getVectorsList().toString().contains("def-id2"));
        assertTrue(asyncListDefaultNs.getVectorsList().toString().contains("def-prefix-id3"));
        assertTrue(asyncListDefaultNs.getVectorsList().toString().contains("def-prefix-id4"));

        ListResponse asyncListCustomNs = Futures.getUnchecked(
                asyncIndexConnection.list(customNamespace, CUSTOM_NS_PREFIX));
        assertEquals(4, asyncListCustomNs.getVectorsList().size());
        assertTrue(asyncListCustomNs.getVectorsList().toString().contains("cus-id1"));
        assertTrue(asyncListCustomNs.getVectorsList().toString().contains("cus-id2"));
        assertTrue(asyncListCustomNs.getVectorsList().toString().contains("cus-prefix-id3"));
        assertTrue(asyncListCustomNs.getVectorsList().toString().contains("cus-prefix-id4"));

        // Sub-prefix filter
        ListResponse asyncListSubPrefix = Futures.getUnchecked(
                asyncIndexConnection.list(customNamespace, CUSTOM_NS_SUBPREFIX));
        assertEquals(2, asyncListSubPrefix.getVectorsList().size());
        assertTrue(asyncListSubPrefix.getVectorsList().toString().contains("cus-prefix-id3"));
        assertTrue(asyncListSubPrefix.getVectorsList().toString().contains("cus-prefix-id4"));

        // Limit
        ListResponse asyncListWithLimit = Futures.getUnchecked(
                asyncIndexConnection.list(customNamespace, CUSTOM_NS_PREFIX, 1));
        assertEquals(1, asyncListWithLimit.getVectorsList().size());

        // Pagination over exactly the 4 pre-seeded cus-* vectors
        ListResponse asyncPage1 = Futures.getUnchecked(
                asyncIndexConnection.list(customNamespace, CUSTOM_NS_PREFIX, 2));
        assertEquals(2, asyncPage1.getVectorsList().size());
        ListResponse asyncPage2 = Futures.getUnchecked(asyncIndexConnection.list(
                customNamespace, CUSTOM_NS_PREFIX, asyncPage1.getPagination().getNext(), 2));
        assertEquals(2, asyncPage2.getVectorsList().size());
    }
}
