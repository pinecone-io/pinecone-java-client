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
        Pinecone pinecone1 = mock(Pinecone.class);
        Pinecone pinecone2 = mock(Pinecone.class);

        assertNotSame(pinecone1, pinecone2);

        // Tests for blocking indexes
        Index index1 = mock(Index.class);
        Index index2 = mock(Index.class);

        when(pinecone1.getIndexConnection("index1")).thenReturn(index1);
        when(pinecone2.getIndexConnection("index2")).thenReturn(index2);

        Index resultIndex1 = pinecone1.getIndexConnection("index1");
        Index resultIndex2 = pinecone2.getIndexConnection("index2");

        verify(pinecone1).getIndexConnection("index1");
        verify(pinecone2).getIndexConnection("index2");
        assertNotSame(resultIndex1, resultIndex2);
    }

    @Test
    public void testMultiplePineconeInstancesForAsyncIndexes() {
        Pinecone pinecone1 = mock(Pinecone.class);
        Pinecone pinecone2 = mock(Pinecone.class);

        assertNotSame(pinecone1, pinecone2);

        // Tests for asyncIndexes
        AsyncIndex asyncIndex1 = mock(AsyncIndex.class);
        AsyncIndex asyncIndex2 = mock(AsyncIndex.class);

        when(pinecone1.getAsyncIndexConnection("index1")).thenReturn(asyncIndex1);
        when(pinecone2.getAsyncIndexConnection("index2")).thenReturn(asyncIndex2);

        AsyncIndex resultAsyncIndex1 = pinecone1.getAsyncIndexConnection("index1");
        AsyncIndex resultAsyncIndex2 = pinecone2.getAsyncIndexConnection("index2");

        // Verify that the correct methods were called and the returned Index instances are not the same
        verify(pinecone1).getAsyncIndexConnection("index1");
        verify(pinecone2).getAsyncIndexConnection("index2");
        assertNotSame(resultAsyncIndex1, resultAsyncIndex2);
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
