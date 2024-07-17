package io.pinecone.helpers;

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.DescribeIndexStatsResponse;
import io.pinecone.proto.NamespaceSummary;
import org.openapitools.control.client.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class TestUtilities {
    private static final Logger logger = LoggerFactory.getLogger(TestUtilities.class);

    public static IndexModel waitUntilIndexIsReady(Pinecone pineconeClient, String indexName, Integer totalMsToWait) throws InterruptedException {
        IndexModel index = pineconeClient.describeIndex(indexName);
        int waitedTimeMs = 0;
        int intervalMs = 2000;

        while (index.getStatus().getState() != IndexModelStatus.StateEnum.READY) {
            index = pineconeClient.describeIndex(indexName);
            if (waitedTimeMs >= totalMsToWait) {
                logger.info("WARNING: Index " + indexName + " not ready after " + waitedTimeMs + "ms");
                break;
            }
            if (index.getStatus().getState() == IndexModelStatus.StateEnum.READY) {
                logger.info("Index " + indexName + " is ready after " + waitedTimeMs + "ms");
                Thread.sleep(20000);
                break;
            }
            Thread.sleep(intervalMs);
            logger.info("Waited " + waitedTimeMs + "ms for " + indexName + " to get ready");
            waitedTimeMs += intervalMs;
        }
        return index;
    }

    public static IndexModel waitUntilIndexIsReady(Pinecone pineconeClient, String indexName) throws InterruptedException {
        return waitUntilIndexIsReady(pineconeClient, indexName, 200000);
    }

    public static CollectionModel createCollection(Pinecone pineconeClient, String collectionName, String indexName, boolean waitUntilReady) throws InterruptedException {
        CollectionModel collection = pineconeClient.createCollection(collectionName, indexName);

        assertEquals(collection.getStatus(), CollectionModel.StatusEnum.INITIALIZING);

        // Wait until collection is ready
        if (waitUntilReady) {
            int timeWaited = 0;
            CollectionModel.StatusEnum collectionReady = collection.getStatus();
            while (collectionReady != CollectionModel.StatusEnum.READY && timeWaited < 120000) {
                logger.info("Waiting for collection " + collectionName + " to be ready. Waited " + timeWaited + " " +
                        "milliseconds...");
                Thread.sleep(5000);
                timeWaited += 5000;
                collection = pineconeClient.describeCollection(collectionName);
                collectionReady = collection.getStatus();
            }

            if (timeWaited > 120000) {
                fail("Collection: " + collectionName + " is not ready after 120 seconds");
            }
        }

        return collection;
    }
}
