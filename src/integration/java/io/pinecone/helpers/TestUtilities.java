package io.pinecone.helpers;

import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestUtilities {
    private static final Logger logger = LoggerFactory.getLogger(TestUtilities.class);

    /**
     * Polls describeIndex until the index reports Ready.
     *
     * @throws AssertionError if the index is not Ready within {@code totalMsToWait}.
     */
    public static IndexModel waitUntilIndexIsReady(Pinecone pineconeClient, String indexName, Integer totalMsToWait) throws InterruptedException {
        return Poll.until("index " + indexName + " is Ready", totalMsToWait, 2000,
                () -> pineconeClient.describeIndex(indexName),
                index -> index.getStatus() != null && "Ready".equals(index.getStatus().getState()));
    }

    public static IndexModel waitUntilIndexIsReady(Pinecone pineconeClient, String indexName) throws InterruptedException {
        return waitUntilIndexIsReady(pineconeClient, indexName, 200000);
    }

    public static CollectionModel createCollection(Pinecone pineconeClient, String collectionName, String indexName, boolean waitUntilReady) throws InterruptedException {
        CollectionModel collection = pineconeClient.createCollection(collectionName, indexName);

        assertEquals("Initializing", collection.getStatus());

        if (waitUntilReady) {
            collection = waitUntilCollectionIsReady(pineconeClient, collectionName);
        }

        return collection;
    }

    /**
     * Polls describeCollection until the collection reports Ready.
     *
     * @throws AssertionError if the collection is not Ready within 120 seconds.
     */
    public static CollectionModel waitUntilCollectionIsReady(Pinecone pineconeClient, String collectionName) throws InterruptedException {
        return Poll.until("collection " + collectionName + " is Ready", 120000, 5000,
                () -> pineconeClient.describeCollection(collectionName),
                collection -> "ready".equalsIgnoreCase(collection.getStatus()));
    }
}
