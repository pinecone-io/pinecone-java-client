package io.pinecone.clients;

import io.pinecone.configs.PineconeConfig;
import io.pinecone.configs.PineconeConnection;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openapitools.client.ApiException;
import org.openapitools.client.api.ManageIndexesApi;
import org.openapitools.client.model.IndexModel;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class ConnectionsMapTest {

    @Test
    public void testMultiplePineconeInstancesForIndexes() {
        String host = "some-host";
        ManageIndexesApi manageIndexesApi = Mockito.mock(ManageIndexesApi.class);
        IndexModel indexModel = new IndexModel();
        indexModel.setHost(host);
        PineconeConfig config = new PineconeConfig("testApiKey");
        config.setHost(host);

        // Create two pinecone objects
        Pinecone pinecone1 = new Pinecone(config, manageIndexesApi);
        Pinecone pinecone2 = new Pinecone(config, manageIndexesApi);

        // We don't want to hit the servers for describeIndex()
        when(pinecone1.describeIndex("index1")).thenReturn(indexModel);
        when(pinecone1.describeIndex("index2")).thenReturn(indexModel);
        when(pinecone2.describeIndex("index1")).thenReturn(indexModel);
        when(pinecone2.describeIndex("index2")).thenReturn(indexModel);

        // Create two pinecone objects with 2 index connections per pinecone object
        Index index1Pinecone1 = pinecone1.getIndexConnection("index1");
        Index index1Pinecone2 = pinecone1.getIndexConnection("index2");
        Index index2Pinecone1 = pinecone2.getIndexConnection("index1");
        Index index2Pinecone2 = pinecone2.getIndexConnection("index2");

        ConcurrentHashMap<String, PineconeConnection> concurrentHashMap1 = pinecone1.getConnectionsMap();
        ConcurrentHashMap<String, PineconeConnection> concurrentHashMap2 = pinecone1.getConnectionsMap();

        // Verify that both pinecone objects are of size 2
        assertEquals(concurrentHashMap1.size(), 2);
        assertEquals(concurrentHashMap2.size(), 2);

        // Verify that the contents and refer to the same object instance
        assertEquals(concurrentHashMap1, concurrentHashMap2);
        assertSame(concurrentHashMap1, concurrentHashMap2);

        index1Pinecone1.close();
        index2Pinecone1.close();
        index1Pinecone2.close();
        index2Pinecone2.close();
    }

    @Test
    public void testMultiplePineconeInstancesForAsyncIndexes() {
        String host = "some-host";
        ManageIndexesApi manageIndexesApi = Mockito.mock(ManageIndexesApi.class);
        IndexModel indexModel = new IndexModel();
        indexModel.setHost(host);
        PineconeConfig config = new PineconeConfig("testApiKey");
        config.setHost(host);

        // Create two pinecone objects
        Pinecone pinecone1 = new Pinecone(config, manageIndexesApi);
        Pinecone pinecone2 = new Pinecone(config, manageIndexesApi);

        // We don't want to hit the servers for describeIndex()
        when(pinecone1.describeIndex("index1")).thenReturn(indexModel);
        when(pinecone1.describeIndex("index2")).thenReturn(indexModel);
        when(pinecone1.describeIndex("index1")).thenReturn(indexModel);
        when(pinecone1.describeIndex("index2")).thenReturn(indexModel);

        // Create two pinecone objects with 2 index connections per pinecone object
        AsyncIndex asyncIndex1Pinecone1 = pinecone1.getAsyncIndexConnection("index1");
        AsyncIndex asyncIndex2Pinecone1 = pinecone1.getAsyncIndexConnection("index2");
        AsyncIndex asyncIndex1Pinecone2 = pinecone2.getAsyncIndexConnection("index1");
        AsyncIndex asyncIndex2Pinecone2 = pinecone2.getAsyncIndexConnection("index2");

        ConcurrentHashMap<String, PineconeConnection> concurrentHashMap1 = pinecone1.getConnectionsMap();
        ConcurrentHashMap<String, PineconeConnection> concurrentHashMap2 = pinecone1.getConnectionsMap();

        // Verify that both pinecone objects are of size 2
        assertEquals(concurrentHashMap1.size(), 2);
        assertEquals(concurrentHashMap2.size(), 2);

        // Verify that the contents and refer to the same object instance
        assertEquals(concurrentHashMap1, concurrentHashMap2);
        assertSame(concurrentHashMap1, concurrentHashMap2);

        asyncIndex1Pinecone1.close();
        asyncIndex2Pinecone1.close();
        asyncIndex1Pinecone2.close();
        asyncIndex2Pinecone2.close();
    }

    @Test
    public void testConnectionRemovedOnIndexClose() throws ApiException {
        String host = "some-host";
        String indexName = "test-index";

        ManageIndexesApi manageIndexesApi = Mockito.mock(ManageIndexesApi.class);
        IndexModel indexModel = new IndexModel();
        indexModel.setHost(host);

        PineconeConfig config = new PineconeConfig("testApiKey");
        config.setHost(host);
        Pinecone pinecone = new Pinecone(config, manageIndexesApi);

        when(manageIndexesApi.describeIndex(indexName)).thenReturn(indexModel);

        Index index = pinecone.getIndexConnection(indexName);

        // Verify that the connection is present in the connections map
        assertTrue(pinecone.getConnectionsMap().containsKey(indexName));

        // Verify that the connections map size is 1
        assertEquals(pinecone.getConnectionsMap().size(), 1);

        // Connection should be removed from the connections map after closing it
        index.close();

        // Verify that the connection is removed from the connections map
        assertFalse(pinecone.getConnectionsMap().containsKey(indexName));
    }

    @Test
    public void testConnectionRemovedOnAsyncIndexClose() throws ApiException {
        String host = "some-host";
        String indexName = "test-index";
        ManageIndexesApi manageIndexesApi = Mockito.mock(ManageIndexesApi.class);
        IndexModel indexModel = new IndexModel();
        indexModel.setHost(host);

        PineconeConfig config = new PineconeConfig("testApiKey");
        config.setHost(host);
        Pinecone pinecone = new Pinecone(config, manageIndexesApi);

        when(manageIndexesApi.describeIndex(indexName)).thenReturn(indexModel);

        AsyncIndex asyncIndex = pinecone.getAsyncIndexConnection(indexName);

        // Verify that the connection is present in the connections map
        assertTrue(pinecone.getConnectionsMap().containsKey(indexName));

        // Verify that the connections map size is 1
        assertEquals(pinecone.getConnectionsMap().size(), 1);

        // Connection should be removed from the connections map after closing it
        asyncIndex.close();

        // Verify that the connection is removed from the connections map
        assertFalse(pinecone.getConnectionsMap().containsKey(indexName));
    }
}
