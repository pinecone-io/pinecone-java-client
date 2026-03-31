package io.pinecone.integration.dataPlane;

import io.pinecone.clients.AsyncIndex;
import io.pinecone.clients.Index;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.helpers.TestResourcesManager;
import io.pinecone.proto.ListNamespacesResponse;
import io.pinecone.proto.NamespaceDescription;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static io.pinecone.helpers.AssertRetry.assertWithRetry;
import static io.pinecone.helpers.BuildUpsertRequest.generateVectorValuesByDimension;
import static org.junit.jupiter.api.Assertions.*;

public class NamespacesTest {
    private static final TestResourcesManager indexManager = TestResourcesManager.getInstance();
    private static Index index;
    private static AsyncIndex asyncIndex;
    private static final int dimension = indexManager.getDimension();

    @Test
    public void namespacesSyncTest() throws Exception {
        // Use a unique prefix so this test's namespaces can be counted independently
        String prefix = "sync-ns-" + RandomStringBuilder.build("", 4) + "-";
        String[] namespaces = new String[4];
        index = indexManager.getOrCreateServerlessIndexConnection();

        // Create namespace explicitly
        namespaces[0] = prefix + "explicit";
        NamespaceDescription createdNamespace = index.createNamespace(namespaces[0]);
        assertNotNull(createdNamespace);
        assertEquals(namespaces[0], createdNamespace.getName());

        // Create namespaces implicitly by upserting vectors
        for (int i = 1; i < 4; i++) {
            namespaces[i] = prefix + i;
            index.upsert("v" + i, generateVectorValuesByDimension(dimension), namespaces[i]);
        }

        // Wait for all 4 namespaces to appear under this test's unique prefix
        assertWithRetry(() -> {
            ListNamespacesResponse resp = index.listNamespaces(prefix, null, 100);
            assertEquals(4, resp.getNamespacesCount(),
                    "Expected 4 namespaces with prefix '" + prefix + "'");
        }, 3);

        index.describeNamespace(namespaces[0]);
        index.deleteNamespace(namespaces[0]);

        // Wait for the deleted namespace to disappear
        assertWithRetry(() -> {
            ListNamespacesResponse resp = index.listNamespaces(prefix, null, 100);
            assertEquals(3, resp.getNamespacesCount(),
                    "Expected 3 namespaces with prefix '" + prefix + "' after deletion");
        }, 3);

        // Verify all remaining namespaces start with the prefix
        ListNamespacesResponse prefixResponse = index.listNamespaces(prefix, null, 100);
        assertNotNull(prefixResponse);
        assertTrue(prefixResponse.getTotalCount() >= 3, "totalCount should be at least 3");
        assertTrue(prefixResponse.getNamespacesCount() >= 3, "Should return at least 3 namespaces with prefix");
        for (int i = 0; i < prefixResponse.getNamespacesCount(); i++) {
            String namespaceName = prefixResponse.getNamespaces(i).getName();
            assertTrue(namespaceName.startsWith(prefix),
                    "Namespace " + namespaceName + " should start with prefix " + prefix);
        }
        assertTrue(prefixResponse.getTotalCount() >= prefixResponse.getNamespacesCount(),
                "totalCount should be at least equal to the number of namespaces returned");
    }

    @Test
    public void namespacesAsyncTest() throws Exception {
        // Use a unique prefix so this test's namespaces can be counted independently
        String prefix = "async-ns-" + RandomStringBuilder.build("", 4) + "-";
        String[] namespaces = new String[4];
        asyncIndex = indexManager.getOrCreateServerlessAsyncIndexConnection();

        // Create namespace explicitly
        namespaces[0] = prefix + "explicit";
        NamespaceDescription createdNamespace = asyncIndex.createNamespace(namespaces[0]).get();
        assertNotNull(createdNamespace);
        assertEquals(namespaces[0], createdNamespace.getName());

        // Create namespaces implicitly by upserting vectors; await each to ensure they're registered
        for (int i = 1; i < 4; i++) {
            namespaces[i] = prefix + i;
            asyncIndex.upsert("v" + i, generateVectorValuesByDimension(dimension), namespaces[i]).get();
        }

        // Wait for all 4 namespaces to appear under this test's unique prefix
        assertWithRetry(() -> {
            ListNamespacesResponse resp = asyncIndex.listNamespaces(prefix, null, 100).get();
            assertEquals(4, resp.getNamespacesCount(),
                    "Expected 4 namespaces with prefix '" + prefix + "'");
        }, 3);

        asyncIndex.describeNamespace(namespaces[0]);
        asyncIndex.deleteNamespace(namespaces[0]);

        // Wait for the deleted namespace to disappear
        assertWithRetry(() -> {
            ListNamespacesResponse resp = asyncIndex.listNamespaces(prefix, null, 100).get();
            assertEquals(3, resp.getNamespacesCount(),
                    "Expected 3 namespaces with prefix '" + prefix + "' after deletion");
        }, 3);

        // Test prefix filtering and total count
        ListNamespacesResponse prefixResponse = asyncIndex.listNamespaces(prefix, null, 100).get();
        assertNotNull(prefixResponse);
        assertTrue(prefixResponse.getTotalCount() >= 3, "totalCount should be at least 3");
        assertTrue(prefixResponse.getNamespacesCount() >= 3, "Should return at least 3 namespaces with prefix");
        for (int i = 0; i < prefixResponse.getNamespacesCount(); i++) {
            String namespaceName = prefixResponse.getNamespaces(i).getName();
            assertTrue(namespaceName.startsWith(prefix),
                    "Namespace " + namespaceName + " should start with prefix " + prefix);
        }
        assertTrue(prefixResponse.getTotalCount() >= prefixResponse.getNamespacesCount(),
                "totalCount should be at least equal to the number of namespaces returned");
    }
}
