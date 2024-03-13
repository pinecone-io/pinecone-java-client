package io.pinecone.helpers;

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.configs.PineconeConfig;
import io.pinecone.configs.PineconeConnection;
import io.pinecone.exceptions.PineconeException;
import org.openapitools.client.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static io.pinecone.helpers.AssertRetry.assertWithRetry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class IndexManager {
    private static final Logger logger = LoggerFactory.getLogger(IndexManager.class);

    public static PineconeConnection createIndexIfNotExistsDataPlane(int dimension, String indexType) throws IOException, InterruptedException {
        String apiKey = System.getenv("PINECONE_API_KEY");
        Pinecone pinecone = new Pinecone(apiKey);

        String indexName = findIndexWithDimensionAndType(pinecone, dimension, indexType);
        if (indexName.isEmpty()) indexName = createNewIndex(pinecone, dimension, indexType);

        // Do not proceed until the newly created index is ready
        waitUntilIndexIsReady(pinecone, indexName);

        // Adding to test PineconeConnection(pineconeConfig, host) constructor
        String host = pinecone.describeIndex(indexName).getHost();
        PineconeConfig config = new PineconeConfig(apiKey);
        config.setHost(host);
        return new PineconeConnection(config);
    }

    public static String createIndexIfNotExistsControlPlane(Pinecone pinecone, int dimension, String indexType) throws IOException, InterruptedException {
        String indexName = findIndexWithDimensionAndType(pinecone, dimension, indexType);

        return (indexName.isEmpty()) ? createNewIndex(pinecone, dimension, indexType) : indexName;
    }

    public static String findIndexWithDimensionAndType(Pinecone pinecone, int dimension, String indexType)
            throws InterruptedException {
        String indexName = "";
        int i = 0;
        List<IndexModel> indexModels = pinecone.listIndexes().getIndexes();
        if(indexModels == null) {
            return indexName;
        }
        while (i < indexModels.size()) {
            IndexModel indexModel = waitUntilIndexIsReady(pinecone, indexModels.get(i).getName());
            if (indexModel.getDimension() == dimension
                    && ((indexType.equalsIgnoreCase(IndexModelSpec.SERIALIZED_NAME_POD)
                        && indexModel.getSpec().getPod() != null
                        && indexModel.getSpec().getPod().getReplicas() == 1
                        && indexModel.getSpec().getPod().getPodType().equalsIgnoreCase("p1.x1"))
                    || (indexType.equalsIgnoreCase(IndexModelSpec.SERIALIZED_NAME_SERVERLESS)))) {
                return indexModel.getName();
            }
            i++;
        }
        return indexName;
    }

    public static String createNewIndex(Pinecone pinecone, int dimension, String indexType) {
        String indexName = RandomStringBuilder.build("index-name", 8);
        String environment = System.getenv("PINECONE_ENVIRONMENT");
        CreateIndexRequestSpec createIndexRequestSpec;

        if (indexType.equalsIgnoreCase(IndexModelSpec.SERIALIZED_NAME_POD)) {
            CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().environment(environment).podType("p1.x1");
            createIndexRequestSpec = new CreateIndexRequestSpec().pod(podSpec);
        } else {
            ServerlessSpec serverlessSpec = new ServerlessSpec().cloud(ServerlessSpec.CloudEnum.AWS).region(environment);
            createIndexRequestSpec = new CreateIndexRequestSpec().serverless(serverlessSpec);
        }

        CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                .name(indexName)
                .dimension(dimension)
                .metric(IndexMetric.DOTPRODUCT)
                .spec(createIndexRequestSpec);
        pinecone.createIndex(createIndexRequest);

        return indexName;
    }

    public static IndexModel waitUntilIndexIsReady(Pinecone pinecone, String indexName, Integer totalMsToWait) throws InterruptedException {
        IndexModel index = pinecone.describeIndex(indexName);
        int waitedTimeMs = 0;
        int intervalMs = 1500;

        while (!index.getStatus().getReady()) {
            index = pinecone.describeIndex(indexName);
            if (index.getStatus().getReady()) {
                logger.info("Index " + indexName + " is ready after " + waitedTimeMs + "ms");
                break;
            }
            if (waitedTimeMs >= totalMsToWait) {
                throw new PineconeException("Index " + indexName + " not ready after " + waitedTimeMs + "ms");
            }
            Thread.sleep(intervalMs);
            waitedTimeMs += intervalMs;
        }
        return index;
    }

    public static IndexModel waitUntilIndexIsReady(Pinecone pinecone, String indexName) throws InterruptedException {
        return waitUntilIndexIsReady(pinecone, indexName, 120000);
    }

    public static PineconeConnection createNewIndexAndConnect(Pinecone pinecone, String indexName, int dimension, IndexMetric metric, CreateIndexRequestSpec spec) throws InterruptedException, PineconeException {
        String apiKey = System.getenv("PINECONE_API_KEY");
        CreateIndexRequest createIndexRequest = new CreateIndexRequest().name(indexName).dimension(dimension).metric(metric).spec(spec);
        pinecone.createIndex(createIndexRequest);

        // Wait until index is ready
        waitUntilIndexIsReady(pinecone, indexName, 200000);
        // wait a bit more before we connect...
        Thread.sleep(15000);

        PineconeConfig config = new PineconeConfig(apiKey);
        return new PineconeConnection(config, indexName);
    }

    public static CollectionModel createCollection(Pinecone pinecone, String collectionName, String indexName, boolean waitUntilReady) throws InterruptedException {
        CreateCollectionRequest createCollectionRequest = new CreateCollectionRequest().name(collectionName).source(indexName);
        CollectionModel collection = pinecone.createCollection(createCollectionRequest);

        assertEquals(collection.getStatus(), CollectionModel.StatusEnum.INITIALIZING);

        // Wait until collection is ready
        if (waitUntilReady) {
            int timeWaited = 0;
            CollectionModel.StatusEnum collectionReady = collection.getStatus();
            while (collectionReady != CollectionModel.StatusEnum.READY && timeWaited < 120000) {
                logger.info("Waiting for collection " + collectionName + " to be ready. Waited " + timeWaited + " milliseconds...");
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
