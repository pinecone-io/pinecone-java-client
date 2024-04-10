package io.pinecone.helpers;

import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeException;
import org.openapitools.client.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class IndexManager {
    private static final Logger logger = LoggerFactory.getLogger(IndexManager.class);

    public static AbstractMap.SimpleEntry<String, Pinecone> createIndexIfNotExistsDataPlane(int dimension, String indexType) throws IOException, InterruptedException {
        String apiKey = System.getenv("PINECONE_API_KEY");
        Pinecone pinecone = new Pinecone.Builder(apiKey).build();

        String indexName = findIndexWithDimensionAndType(pinecone, dimension, indexType);
        if (indexName.isEmpty()) indexName = createNewIndex(pinecone, dimension, indexType, true);

        return new AbstractMap.SimpleEntry<>(indexName, pinecone);
    }

    public static String findIndexWithDimensionAndType(Pinecone pinecone, int dimension, String indexType) {
        String indexName = "";
        List<IndexModel> indexModels = pinecone.listIndexes().getIndexes();
        if(indexModels == null) {
            return indexName;
        }

        for (IndexModel indexModel : indexModels) {
            boolean typePod = indexType.equalsIgnoreCase(IndexModelSpec.SERIALIZED_NAME_POD) && indexModel.getSpec().getPod() != null;
            boolean typeServerless = indexType.equalsIgnoreCase(IndexModelSpec.SERIALIZED_NAME_SERVERLESS) && indexModel.getSpec().getServerless() != null;

            if (indexModel.getDimension() == dimension && indexModel.getMetric() == IndexMetric.DOTPRODUCT) {
                if (typePod || typeServerless) {
                    return indexModel.getName();
                }
            }
        }
        return indexName;
    }

    public static String createNewIndex(Pinecone pinecone, int dimension, String indexType, boolean waitUntilIndexIsReady) throws InterruptedException {
        String indexName = RandomStringBuilder.build("index-name", 8);
        String environment = System.getenv("PINECONE_ENVIRONMENT");
        CreateIndexRequestSpec createIndexRequestSpec;

        if (indexType.equalsIgnoreCase(IndexModelSpec.SERIALIZED_NAME_POD)) {
            pinecone.createPodsIndex(indexName, dimension, environment, "p1.x1");
            if (waitUntilIndexIsReady) {
                waitUntilIndexIsReady(pinecone, indexName);
            }
            return indexName;
        } else {
            pinecone.createServerlessIndex(indexName, "cosine", dimension, ServerlessSpec.CloudEnum.AWS.toString(), "us-west-2");
            if (waitUntilIndexIsReady) {
                waitUntilIndexIsReady(pinecone, indexName);
            }
            return indexName;
        }
    }

    public static Pinecone createNewIndex(Pinecone pinecone, String indexName, int dimension,
     String metric, boolean waitUntilIndexIsReady) throws InterruptedException,
            PineconeException {
//        CreateIndexRequest createIndexRequest = new CreateIndexRequest().name(indexName).dimension(dimension).metric(metric).spec(spec);
        pinecone.createPodsIndex(indexName, dimension, "us-east4-gcp", "p1.x1", metric);

        if (waitUntilIndexIsReady) {
            waitUntilIndexIsReady(pinecone, indexName);
        }
        return pinecone;
    }

    public static IndexModel waitUntilIndexIsReady(Pinecone pinecone, String indexName, Integer totalMsToWait) throws InterruptedException {
        IndexModel index = pinecone.describeIndex(indexName);
        int waitedTimeMs = 0;
        int intervalMs = 2000;

        while (index.getStatus().getState() != IndexModelStatus.StateEnum.READY) {
            index = pinecone.describeIndex(indexName);
            if (waitedTimeMs >= totalMsToWait) {
                logger.info("WARNING: Index " + indexName + " not ready after " + waitedTimeMs + "ms");
                break;
            }
            if (index.getStatus().getReady()) {
                logger.info("Index " + indexName + " is ready after " + waitedTimeMs + "ms");
                // Wait one final time before we start connecting and operating on the index
                Thread.sleep(10000);
                break;
            }
            Thread.sleep(intervalMs);
            logger.info("Waited " + waitedTimeMs + "ms for " + indexName + " to get ready");
            waitedTimeMs += intervalMs;
        }
        return index;
    }

    public static IndexModel waitUntilIndexIsReady(Pinecone pinecone, String indexName) throws InterruptedException {
        return waitUntilIndexIsReady(pinecone, indexName, 200000);
    }

    public static CollectionModel createCollection(Pinecone pinecone, String collectionName, String indexName, boolean waitUntilReady) throws InterruptedException {
        CollectionModel collection = pinecone.createCollection(collectionName, indexName);

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
                collection = pinecone.describeCollection(collectionName);
                collectionReady = collection.getStatus();
            }

            if (timeWaited > 120000) {
                fail("Collection: " + collectionName + " is not ready after 120 seconds");
            }
        }

        return collection;
    }
}
