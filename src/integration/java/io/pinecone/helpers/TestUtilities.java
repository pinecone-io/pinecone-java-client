package io.pinecone.helpers;

import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class TestUtilities {
    private static final Logger logger = LoggerFactory.getLogger(TestUtilities.class);

    public static IndexModel waitUntilIndexIsReady(Pinecone pineconeClient, String indexName, Integer totalMsToWait) throws InterruptedException {
        IndexModel index = pineconeClient.describeIndex(indexName);
        int waitedTimeMs = 0;
        int intervalMs = 2000;

        while (!index.getStatus().getState().equals("Ready")) {
            index = pineconeClient.describeIndex(indexName);
            if (waitedTimeMs >= totalMsToWait) {
                logger.info("WARNING: Index " + indexName + " not ready after " + waitedTimeMs + "ms");
                break;
            }
            if (index.getStatus().getState().equals("Ready")) {
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

    /**
     * Waits until the read capacity status is Ready for a serverless index.
     * This is needed before configuring read capacity, as the API requires read capacity to be Ready before updates.
     *
     * @param pineconeClient The Pinecone client instance
     * @param indexName The name of the index
     * @param totalMsToWait Maximum time to wait in milliseconds
     * @return The IndexModel with read capacity status Ready
     * @throws InterruptedException if the thread is interrupted
     */
    public static IndexModel waitUntilReadCapacityIsReady(Pinecone pineconeClient, String indexName, Integer totalMsToWait) throws InterruptedException {
        IndexModel index = pineconeClient.describeIndex(indexName);
        int waitedTimeMs = 0;
        int intervalMs = 2000;

        while (true) {
            // Check if index has serverless spec with read capacity
            if (index.getSpec() != null && index.getSpec().getIndexModelServerless() != null) {
                ServerlessSpecResponse serverless = index.getSpec().getIndexModelServerless().getServerless();
                if (serverless != null && serverless.getReadCapacity() != null) {
                    ReadCapacityResponse readCapacityResponse = serverless.getReadCapacity();
                    ReadCapacityStatus status = null;

                    // Get status from the appropriate response type
                    try {
                        ReadCapacityDedicatedSpecResponse dedicatedResponse = readCapacityResponse.getReadCapacityDedicatedSpecResponse();
                        status = dedicatedResponse.getStatus();
                    } catch (ClassCastException e) {
                        try {
                            ReadCapacityOnDemandSpecResponse onDemandResponse = readCapacityResponse.getReadCapacityOnDemandSpecResponse();
                            status = onDemandResponse.getStatus();
                        } catch (ClassCastException e2) {
                            logger.warn("Unknown read capacity response type for index " + indexName);
                        }
                    }

                    if (status != null && "Ready".equals(status.getState())) {
                        logger.info("Read capacity for index " + indexName + " is ready after " + waitedTimeMs + "ms");
                        break;
                    }
                } else {
                    // If no read capacity is configured (OnDemand by default), consider it ready
                    logger.info("Index " + indexName + " has no read capacity configured (defaults to OnDemand), considering ready");
                    break;
                }
            } else {
                // Not a serverless index or spec not available yet
                logger.info("Index " + indexName + " spec not available yet, waiting...");
            }

            if (waitedTimeMs >= totalMsToWait) {
                logger.info("WARNING: Read capacity for index " + indexName + " not ready after " + waitedTimeMs + "ms");
                break;
            }

            Thread.sleep(intervalMs);
            waitedTimeMs += intervalMs;
            logger.info("Waited " + waitedTimeMs + "ms for read capacity of " + indexName + " to get ready");
            index = pineconeClient.describeIndex(indexName);
        }
        return index;
    }

    /**
     * Waits until the read capacity status is Ready for a serverless index (default timeout: 200 seconds).
     *
     * @param pineconeClient The Pinecone client instance
     * @param indexName The name of the index
     * @return The IndexModel with read capacity status Ready
     * @throws InterruptedException if the thread is interrupted
     */
    public static IndexModel waitUntilReadCapacityIsReady(Pinecone pineconeClient, String indexName) throws InterruptedException {
        return waitUntilReadCapacityIsReady(pineconeClient, indexName, 200000);
    }

    public static CollectionModel createCollection(Pinecone pineconeClient, String collectionName, String indexName, boolean waitUntilReady) throws InterruptedException {
        CollectionModel collection = pineconeClient.createCollection(collectionName, indexName);

        assertEquals("Initializing", collection.getStatus());

        // Wait until collection is ready
        if (waitUntilReady) {
            int timeWaited = 0;
            String collectionReady = collection.getStatus();
            while (!collectionReady.equals("Ready") && timeWaited < 120000) {
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
