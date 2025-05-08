package io.pinecone.integration.dataPlane;

import io.pinecone.clients.AsyncIndex;
import io.pinecone.clients.Index;
import io.pinecone.helpers.TestResourcesManager;
import io.pinecone.proto.ListNamespacesResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NamespacesTest {
    private static final TestResourcesManager indexManager = TestResourcesManager.getInstance();
    private static Index index;
    private static AsyncIndex asyncIndex;

    @AfterAll
    public static void cleanUp() {
        index.close();
        asyncIndex.close();
    }

    @Test
    public void testNamespaces() throws InterruptedException {
        index = indexManager.getOrCreateServerlessIndexConnection();
        ListNamespacesResponse listNamespacesResponse = index.listNamespaces();
        int namespaceCount = listNamespacesResponse.getNamespacesCount();

        String namespace1 = "namespace-1";
        String namespace2 = "namespace-2";
        String namespace3 = "namespace-3";

        index.upsert("v1", Arrays.asList(1F, 2F, 3F), namespace1);
        index.upsert("v1", Arrays.asList(1F, 2F, 3F), namespace2);
        index.upsert("v1", Arrays.asList(1F, 2F, 3F), namespace3);

        // wait for vectors to be upserted
        Thread.sleep(5000);
        listNamespacesResponse = index.listNamespaces();
        assert(listNamespacesResponse.getNamespacesCount() == namespaceCount + 3);

        index.describeNamespace("namespace-1");
        index.deleteNamespace("namespace-1");
        listNamespacesResponse = index.listNamespaces();
        assertEquals(listNamespacesResponse.getNamespacesCount(), namespaceCount + 2);
    }

    @Test
    public void testNamespacesAsync() throws InterruptedException, ExecutionException {
        asyncIndex = indexManager.getOrCreateServerlessAsyncIndexConnection();

        ListNamespacesResponse listNamespacesResponse = asyncIndex.listNamespaces().get();
        int namespaceCount = listNamespacesResponse.getNamespacesCount();

        String namespace1 = "namespace-1";
        String namespace2 = "namespace-2";
        String namespace3 = "namespace-3";

        asyncIndex.upsert("v1", Arrays.asList(1F, 2F, 3F), namespace1);
        asyncIndex.upsert("v1", Arrays.asList(1F, 2F, 3F), namespace2);
        asyncIndex.upsert("v1", Arrays.asList(1F, 2F, 3F), namespace3);

        asyncIndex.upsert("v1", Arrays.asList(1F, 2F, 3F), namespace1);
        asyncIndex.upsert("v1", Arrays.asList(1F, 2F, 3F), namespace2);
        asyncIndex.upsert("v1", Arrays.asList(1F, 2F, 3F), namespace3);

        // wait for vectors to be upserted
        Thread.sleep(5000);
        listNamespacesResponse = asyncIndex.listNamespaces().get();
        assert(listNamespacesResponse.getNamespacesCount() == namespaceCount + 3);

        asyncIndex.describeNamespace(namespace1);
        asyncIndex.deleteNamespace(namespace1);
        listNamespacesResponse = asyncIndex.listNamespaces().get();
        assertEquals(listNamespacesResponse.getNamespacesCount(), namespaceCount + 2);
    }
}
