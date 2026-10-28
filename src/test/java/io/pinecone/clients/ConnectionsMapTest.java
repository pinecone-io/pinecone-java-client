package io.pinecone.clients;

import io.pinecone.configs.PineconeConfig;
import io.pinecone.configs.PineconeConnection;
import io.pinecone.configs.ResponseMetadataListener;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.openapitools.db_control.client.ApiException;
import org.openapitools.db_control.client.Configuration;
import org.openapitools.db_control.client.api.ManageIndexesApi;
import org.openapitools.db_control.client.model.IndexModel;
import org.openapitools.db_data.client.api.BulkOperationsApi;
import org.openapitools.db_data.client.api.VectorOperationsApi;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConnectionsMapTest {

    private static ManageIndexesApi describing(String indexName, String host) throws ApiException {
        ManageIndexesApi manageIndexesApi = Mockito.mock(ManageIndexesApi.class);
        IndexModel indexModel = new IndexModel();
        indexModel.setHost(host);
        when(manageIndexesApi.describeIndex(Configuration.VERSION, indexName)).thenReturn(indexModel);
        return manageIndexesApi;
    }

    private static List<PineconeConnection> connectionsTo(String host) {
        return Pinecone.openConnections().stream()
                .filter(connection -> host.equals(connection.toString()))
                .collect(Collectors.toList());
    }

    @Test
    public void testClientsWithSameSettingsShareConnection() throws ApiException {
        String host = "shared-host-" + System.nanoTime();
        ManageIndexesApi manageIndexesApi = describing("index1", host);
        Pinecone pinecone1 = new Pinecone(new PineconeConfig("testApiKey"), manageIndexesApi);
        Pinecone pinecone2 = new Pinecone(new PineconeConfig("testApiKey"), manageIndexesApi);

        Index index1 = pinecone1.getIndexConnection("index1");
        Index index2 = pinecone2.getIndexConnection("index1");
        AsyncIndex asyncIndex = pinecone2.getAsyncIndexConnection("index1");
        try {
            assertEquals(1, connectionsTo(host).size());
        } finally {
            index1.close();
            index2.close();
            asyncIndex.close();
        }
        assertEquals(0, connectionsTo(host).size());
    }

    @Test
    public void testClientsWithDifferentApiKeysGetDistinctConnections() throws ApiException {
        String host = "same-name-host-" + System.nanoTime();
        ManageIndexesApi manageIndexesApi = describing("index1", host);
        Pinecone pinecone1 = new Pinecone(new PineconeConfig("apiKeyA"), manageIndexesApi);
        Pinecone pinecone2 = new Pinecone(new PineconeConfig("apiKeyB"), manageIndexesApi);

        Index index1 = pinecone1.getIndexConnection("index1");
        Index index2 = pinecone2.getIndexConnection("index1");
        try {
            assertEquals(2, connectionsTo(host).size());
        } finally {
            index1.close();
            index2.close();
        }
        assertEquals(0, connectionsTo(host).size());
    }

    @Test
    public void testClientsWithDifferentTlsOrListenerGetDistinctConnections() throws ApiException {
        String host = "settings-host-" + System.nanoTime();
        ManageIndexesApi manageIndexesApi = describing("index1", host);
        PineconeConfig plaintext = new PineconeConfig("testApiKey");
        plaintext.setTLSEnabled(false);
        PineconeConfig listening = new PineconeConfig("testApiKey");
        listening.setResponseMetadataListener(metadata -> { });

        Index base = new Pinecone(new PineconeConfig("testApiKey"), manageIndexesApi).getIndexConnection("index1");
        Index noTls = new Pinecone(plaintext, manageIndexesApi).getIndexConnection("index1");
        Index withListener = new Pinecone(listening, manageIndexesApi).getIndexConnection("index1");
        try {
            assertEquals(3, connectionsTo(host).size());
        } finally {
            base.close();
            noTls.close();
            withListener.close();
        }
        assertEquals(0, connectionsTo(host).size());
    }

    @Test
    public void testCloseKeepsConnectionOpenWhileAnotherIndexUsesIt() throws ApiException {
        String host = "close-host-" + System.nanoTime();
        ManageIndexesApi manageIndexesApi = describing("index1", host);
        Pinecone pinecone1 = new Pinecone(new PineconeConfig("testApiKey"), manageIndexesApi);
        Pinecone pinecone2 = new Pinecone(new PineconeConfig("testApiKey"), manageIndexesApi);

        Index index = pinecone1.getIndexConnection("index1");
        AsyncIndex asyncIndex = pinecone2.getAsyncIndexConnection("index1");
        PineconeConnection connection = connectionsTo(host).get(0);

        index.close();
        index.close();
        assertSame(connection, connectionsTo(host).get(0));
        assertFalse(connection.getChannel().isShutdown());

        asyncIndex.close();
        assertTrue(connectionsTo(host).isEmpty());
        assertTrue(connection.getChannel().isShutdown());
    }

    @Test
    public void testCloseShutsDownConnectionNotFromClient() {
        PineconeConfig config = new PineconeConfig("testApiKey");
        config.setHost("standalone-host");
        PineconeConnection connection = new PineconeConnection(config);

        new Index(config, connection, "index1").close();

        assertTrue(connection.getChannel().isShutdown());
    }

    @Test
    public void testClientsWithDifferentSourceTagsGetDistinctConnections() throws ApiException {
        String host = "source-tag-host-" + System.nanoTime();
        ManageIndexesApi manageIndexesApi = describing("index1", host);

        Index tagA = new Pinecone(new PineconeConfig("testApiKey", "tag_a"), manageIndexesApi).getIndexConnection("index1");
        Index tagB = new Pinecone(new PineconeConfig("testApiKey", "tag_b"), manageIndexesApi).getIndexConnection("index1");
        try {
            assertEquals(2, connectionsTo(host).size());
        } finally {
            tagA.close();
            tagB.close();
        }
        assertEquals(0, connectionsTo(host).size());
    }

    @Test
    public void testClientsSharingOneListenerInstanceShareConnection() throws ApiException {
        String host = "shared-listener-host-" + System.nanoTime();
        ManageIndexesApi manageIndexesApi = describing("index1", host);
        ResponseMetadataListener listener = metadata -> { };
        PineconeConfig config1 = new PineconeConfig("testApiKey");
        config1.setResponseMetadataListener(listener);
        PineconeConfig config2 = new PineconeConfig("testApiKey");
        config2.setResponseMetadataListener(listener);

        Index index1 = new Pinecone(config1, manageIndexesApi).getIndexConnection("index1");
        Index index2 = new Pinecone(config2, manageIndexesApi).getIndexConnection("index1");
        try {
            assertEquals(1, connectionsTo(host).size());
        } finally {
            index1.close();
            index2.close();
        }
        assertEquals(0, connectionsTo(host).size());
    }

    @Test
    public void testReconnectAfterLastCloseOpensNewConnection() throws ApiException {
        String host = "reconnect-host-" + System.nanoTime();
        Pinecone pinecone = new Pinecone(new PineconeConfig("testApiKey"), describing("index1", host));

        Index first = pinecone.getIndexConnection("index1");
        PineconeConnection firstConnection = connectionsTo(host).get(0);
        first.close();
        assertTrue(firstConnection.getChannel().isShutdown());

        Index second = pinecone.getIndexConnection("index1");
        try {
            PineconeConnection secondConnection = connectionsTo(host).get(0);
            assertNotSame(firstConnection, secondConnection);
            assertFalse(secondConnection.getChannel().isShutdown());
        } finally {
            second.close();
        }
        assertEquals(0, connectionsTo(host).size());
    }

    @Test
    public void testAsyncIndexCloseTwiceReleasesOnce() throws ApiException {
        String host = "async-close-host-" + System.nanoTime();
        Pinecone pinecone = new Pinecone(new PineconeConfig("testApiKey"), describing("index1", host));

        AsyncIndex asyncIndex = pinecone.getAsyncIndexConnection("index1");
        Index index = pinecone.getIndexConnection("index1");
        PineconeConnection connection = connectionsTo(host).get(0);
        try {
            asyncIndex.close();
            asyncIndex.close();
            assertSame(connection, connectionsTo(host).get(0));
            assertFalse(connection.getChannel().isShutdown());
        } finally {
            index.close();
        }
        assertTrue(connectionsTo(host).isEmpty());
        assertTrue(connection.getChannel().isShutdown());
    }

    @Test
    public void testFailedIndexConstructionReleasesConnection() throws ApiException {
        String host = "failed-index-host-" + System.nanoTime();
        Pinecone pinecone = new Pinecone(new PineconeConfig("testApiKey"), describing("index1", host));

        try (MockedConstruction<VectorOperationsApi> ignored = mockConstruction(VectorOperationsApi.class,
                (mock, context) -> { throw new IllegalStateException("boom"); })) {
            assertThrows(RuntimeException.class, () -> pinecone.getIndexConnection("index1"));
        }
        assertTrue(connectionsTo(host).isEmpty());
    }

    @Test
    public void testFailedAsyncIndexConstructionReleasesConnection() throws ApiException {
        String host = "failed-async-index-host-" + System.nanoTime();
        Pinecone pinecone = new Pinecone(new PineconeConfig("testApiKey"), describing("index1", host));

        try (MockedConstruction<BulkOperationsApi> ignored = mockConstruction(BulkOperationsApi.class,
                (mock, context) -> { throw new IllegalStateException("boom"); })) {
            assertThrows(RuntimeException.class, () -> pinecone.getAsyncIndexConnection("index1"));
        }
        assertTrue(connectionsTo(host).isEmpty());
    }
}
