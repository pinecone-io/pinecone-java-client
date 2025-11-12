package io.pinecone.integration.dataPlane;

import io.pinecone.clients.AsyncIndex;
import io.pinecone.clients.Index;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.helpers.TestResourcesManager;
import io.pinecone.proto.ListNamespacesResponse;
import io.pinecone.proto.NamespaceDescription;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static io.pinecone.helpers.BuildUpsertRequest.generateVectorValuesByDimension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NamespacesTest {
    private static final TestResourcesManager indexManager = TestResourcesManager.getInstance();
    private static Index index;
    private static AsyncIndex asyncIndex;
    private static final int dimension = indexManager.getDimension();

    @AfterAll
    public static void cleanUp() {
        index.close();
        asyncIndex.close();
    }

    @Test
    public void namespacesSyncTest() throws InterruptedException {
        String[] namespaces = new String[4];
        index = indexManager.getOrCreateServerlessIndexConnection();
        ListNamespacesResponse listNamespacesResponse = index.listNamespaces();
        int namespaceCount = listNamespacesResponse.getNamespacesCount();

        // Create namespace explicitly using createNamespace
        namespaces[0] = RandomStringBuilder.build("namespace-", 3);
        NamespaceDescription createdNamespace = index.createNamespace(namespaces[0]);
        assertNotNull(createdNamespace);
        assertEquals(namespaces[0], createdNamespace.getName());

        // Create namespaces implicitly by upserting vectors
        for(int i=1; i<4; i++) {
            namespaces[i] = RandomStringBuilder.build("namespace-", 3);
            index.upsert("v"+i, generateVectorValuesByDimension(dimension), namespaces[i]);
        }

        // wait for vectors to be upserted
        Thread.sleep(5000);
        listNamespacesResponse = index.listNamespaces();
        assertEquals(listNamespacesResponse.getNamespacesCount(), namespaceCount + 4);

        index.describeNamespace(namespaces[0]);
        index.deleteNamespace(namespaces[0]);

        // wait for namespace to be deleted
        Thread.sleep(3000);
        listNamespacesResponse = index.listNamespaces();
        assertEquals(listNamespacesResponse.getNamespacesCount(), namespaceCount + 3);
    }

    @Test
    public void namespacesAsyncTest() throws InterruptedException, ExecutionException {
        String[] namespaces = new String[4];
        asyncIndex = indexManager.getOrCreateServerlessAsyncIndexConnection();

        ListNamespacesResponse listNamespacesResponse = asyncIndex.listNamespaces().get();
        int namespaceCount = listNamespacesResponse.getNamespacesCount();

        // Create namespace explicitly using createNamespace
        namespaces[0] = RandomStringBuilder.build("namespace-", 3);
        NamespaceDescription createdNamespace = asyncIndex.createNamespace(namespaces[0]).get();
        assertNotNull(createdNamespace);
        assertEquals(namespaces[0], createdNamespace.getName());

        // Create namespaces implicitly by upserting vectors
        for(int i=1; i<4; i++) {
            namespaces[i] = RandomStringBuilder.build("namespace-", 3);
            asyncIndex.upsert("v"+i, generateVectorValuesByDimension(dimension), namespaces[i]);
        }

        // wait for vectors to be upserted
        Thread.sleep(5000);
        listNamespacesResponse = asyncIndex.listNamespaces().get();
        assertEquals(listNamespacesResponse.getNamespacesCount(), namespaceCount + 4);

        asyncIndex.describeNamespace(namespaces[0]);
        asyncIndex.deleteNamespace(namespaces[0]);

        // wait for namespace to be deleted
        Thread.sleep(3000);
        listNamespacesResponse = asyncIndex.listNamespaces().get();
        assertEquals(listNamespacesResponse.getNamespacesCount(), namespaceCount + 3);
    }
}
