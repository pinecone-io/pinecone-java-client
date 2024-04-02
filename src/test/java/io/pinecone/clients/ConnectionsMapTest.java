package io.pinecone.clients;

import io.pinecone.configs.PineconeConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openapitools.client.ApiException;
import org.openapitools.client.api.ManageIndexesApi;
import org.openapitools.client.model.IndexModel;

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
        Pinecone pinecone1 = new Pinecone(config, manageIndexesApi);
        Pinecone pinecone2 = new Pinecone(config, manageIndexesApi);

        when(pinecone1.describeIndex("index1")).thenReturn(indexModel);
        when(pinecone1.describeIndex("index2")).thenReturn(indexModel);
        when(pinecone2.describeIndex("index1")).thenReturn(indexModel);
        when(pinecone2.describeIndex("index2")).thenReturn(indexModel);

        Index index1Pinecone1 = pinecone1.getIndexConnection("index1");
        Index index1Pinecone2 = pinecone1.getIndexConnection("index2");
        Index index2Pinecone1 = pinecone2.getIndexConnection("index1");
        Index index2Pinecone2 = pinecone2.getIndexConnection("index2");
        assertEquals(pinecone1.getConnectionsMap().size(), 2);
        assertEquals(pinecone2.getConnectionsMap().size(), 2);

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
        Pinecone pinecone1 = new Pinecone(config, manageIndexesApi);
        Pinecone pinecone2 = new Pinecone(config, manageIndexesApi);

        when(pinecone1.describeIndex("index1")).thenReturn(indexModel);
        when(pinecone1.describeIndex("index2")).thenReturn(indexModel);
        when(pinecone1.describeIndex("index1")).thenReturn(indexModel);
        when(pinecone1.describeIndex("index2")).thenReturn(indexModel);

        AsyncIndex asyncIndex1Pinecone1 = pinecone1.getAsyncIndexConnection("index1");
        AsyncIndex asyncIndex2Pinecone1 = pinecone1.getAsyncIndexConnection("index2");
        AsyncIndex asyncIndex1Pinecone2 = pinecone2.getAsyncIndexConnection("index1");
        AsyncIndex asyncIndex2Pinecone2 = pinecone2.getAsyncIndexConnection("index2");
        assertEquals(pinecone2.getConnectionsMap().size(), 2);
        assertEquals(pinecone2.getConnectionsMap().size(), 2);

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
