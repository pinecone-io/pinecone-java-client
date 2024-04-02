package io.pinecone.clients;

import io.pinecone.configs.PineconeConnection;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class ConnectionsMapTest {

    @Test
    public void testConnectionsMapConcurrency() throws InterruptedException {
        final int numThreads = 10;
        final ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        final Pinecone pinecone = new Pinecone.Builder("testApiKey").build();

        for (int i = 0; i < numThreads; i++) {
            executorService.submit(() -> {
                String indexName = "testIndex";
                Index index = pinecone.createIndexConnection(indexName);
                PineconeConnection retrievedConnection = pinecone.getConnectionsMap().get(indexName);
                assertEquals(index.getConnection(), retrievedConnection);
                index.close();
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(7, TimeUnit.SECONDS);
    }

    @Test
    public void testMultiplePineconeInstances() {
        Pinecone pinecone1 = mock(Pinecone.class);
        Pinecone pinecone2 = mock(Pinecone.class);

        assertNotSame(pinecone1, pinecone2);

        // Tests for blocking indexes
        Index index1 = mock(Index.class);
        Index index2 = mock(Index.class);

        when(pinecone1.createIndexConnection("index1")).thenReturn(index1);
        when(pinecone2.createIndexConnection("index2")).thenReturn(index2);

        Index resultIndex1 = pinecone1.createIndexConnection("index1");
        Index resultIndex2 = pinecone2.createIndexConnection("index2");

        verify(pinecone1).createIndexConnection("index1");
        verify(pinecone2).createIndexConnection("index2");
        assertNotSame(resultIndex1, resultIndex2);

        // Tests for asyncIndexes
        AsyncIndex asyncIndex1 = mock(AsyncIndex.class);
        AsyncIndex asyncIndex2 = mock(AsyncIndex.class);

        when(pinecone1.createAsyncIndexConnection("index1")).thenReturn(asyncIndex1);
        when(pinecone2.createAsyncIndexConnection("index2")).thenReturn(asyncIndex2);

        AsyncIndex resultAsyncIndex1 = pinecone1.createAsyncIndexConnection("index1");
        AsyncIndex resultAsyncIndex2 = pinecone2.createAsyncIndexConnection("index2");

        // Verify that the correct methods were called and the returned Index instances are not the same
        verify(pinecone1).createAsyncIndexConnection("index1");
        verify(pinecone2).createAsyncIndexConnection("index2");
        assertNotSame(resultAsyncIndex1, resultAsyncIndex2);
    }

    // ToDo: add more tests
    // verify the connection is removed when index.close() and asyncIndex.close() are called
}
