package io.pinecone.helpers;

import io.pinecone.*;
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
    private static PineconeClientConfig config;
    private static final Logger logger = LoggerFactory.getLogger(IndexManager.class);

    public static PineconeConnection createIndexIfNotExistsDataPlane(int dimension, String indexType) throws IOException, InterruptedException {
        String apiKey = System.getenv("PINECONE_API_KEY");
        String environment = System.getenv("PINECONE_ENVIRONMENT");
        config = new PineconeClientConfig().withApiKey(apiKey).withEnvironment(environment);
        PineconeControlPlaneClient controlPlaneClient = new PineconeControlPlaneClient(apiKey);
        IndexList indexList = controlPlaneClient.listIndexes();

        String indexName = findIndexWithDimensionAndType(indexList, dimension, controlPlaneClient, indexType);
        if (indexName.isEmpty()) indexName = createNewIndex(controlPlaneClient, indexType, dimension);

        // Do not proceed until the newly created index is ready
        isIndexReady(indexName, controlPlaneClient);
        // ToDo: Update the constructor by removing dependency on PineconeClientConfig
        PineconeClient dataPlaneClient = new PineconeClient(config);
        String host = controlPlaneClient.describeIndex(indexName).getHost();

        return dataPlaneClient.connect(
                new PineconeConnectionConfig()
                        .withConnectionUrl("https://" + host));
    }

    public static String createIndexIfNotExistsControlPlane(PineconeControlPlaneClient controlPlaneClient, int dimension, String indexType) throws IOException, InterruptedException {
        IndexList indexList = controlPlaneClient.listIndexes();
        String indexName = findIndexWithDimensionAndType(indexList, dimension, controlPlaneClient, indexType);

        return (indexName.isEmpty()) ? createNewIndex(controlPlaneClient, indexType, dimension) : indexName;
    }

    private static String findIndexWithDimensionAndType(IndexList indexList, int dimension, PineconeControlPlaneClient controlPlaneClient, String indexType)
            throws InterruptedException {
        int i = 0;
        List<IndexModel> indexModels = indexList.getIndexes();
        while (i < indexModels.size()) {
            IndexModel indexModel = isIndexReady(indexModels.get(i).getName(), controlPlaneClient);
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
        return "";
    }

    private static String createNewIndex(PineconeControlPlaneClient controlPlaneClient, String indexType, int dimension) throws IOException {
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
                .metric(IndexMetric.EUCLIDEAN)
                .spec(createIndexRequestSpec);
        controlPlaneClient.createIndex(createIndexRequest);

        return indexName;
    }

    public static IndexModel waitUntilIndexIsReady(PineconeControlPlaneClient controlPlaneClient, String indexName, Integer totalMsToWait) throws InterruptedException {
        IndexModel index = controlPlaneClient.describeIndex(indexName);
        int waitedTimeMs = 0;
        int intervalMs = 1500;

        while (!index.getStatus().getReady()) {
            index = controlPlaneClient.describeIndex(indexName);
            if (waitedTimeMs >= totalMsToWait) {
                logger.info("Index " + indexName + " not ready after " + waitedTimeMs + "ms");
                break;
            }
            if (index.getStatus().getReady()) {
                logger.info("Index " + indexName + " is ready after " + waitedTimeMs + "ms");
                break;
            }
            Thread.sleep(intervalMs);
            waitedTimeMs += intervalMs;
        }
        return index;
    }

    public static IndexModel waitUntilIndexIsReady(PineconeControlPlaneClient controlPlaneClient, String indexName) throws InterruptedException {
        return waitUntilIndexIsReady(controlPlaneClient, indexName, 120000);
    }

    public static PineconeConnection createNewIndexAndConnect(PineconeControlPlaneClient controlPlaneClient, String indexName, int dimension, IndexMetric metric, CreateIndexRequestSpec spec) throws InterruptedException, PineconeException {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest().name(indexName).dimension(dimension).metric(metric).spec(spec);
        controlPlaneClient.createIndex(createIndexRequest);

        // Wait until index is ready
        waitUntilIndexIsReady(controlPlaneClient, indexName, 200000);
        // wait a bit more before we connect...
        Thread.sleep(15000);

        String host = controlPlaneClient.describeIndex(indexName).getHost();

        PineconeClientConfig specificConfig = new PineconeClientConfig().withApiKey(System.getenv("PINECONE_API_KEY"));
        PineconeClient dataPlaneClient = new PineconeClient(specificConfig);

        return dataPlaneClient.connect(
                new PineconeConnectionConfig()
                        .withConnectionUrl("https://" + host));
    }

    public static CollectionModel createCollection(PineconeControlPlaneClient controlPlaneClient, String collectionName, String indexName, boolean waitUntilReady) throws InterruptedException {
        CreateCollectionRequest createCollectionRequest = new CreateCollectionRequest().name(collectionName).source(indexName);
        CollectionModel collection = controlPlaneClient.createCollection(createCollectionRequest);

        assertEquals(collection.getStatus(), CollectionModel.StatusEnum.INITIALIZING);

        // Wait until collection is ready
        if (waitUntilReady) {
            int timeWaited = 0;
            CollectionModel.StatusEnum collectionReady = collection.getStatus();
            while (collectionReady != CollectionModel.StatusEnum.READY && timeWaited < 120000) {
                logger.info("Waiting for collection" + collectionName + " to be ready. Waited " + timeWaited + " milliseconds...");
                Thread.sleep(5000);
                timeWaited += 5000;
                collection = controlPlaneClient.describeCollection(collectionName);
                collectionReady = collection.getStatus();
            }

            if (timeWaited > 120000) {
                fail("Collection: " + collectionName + " is not ready after 120 seconds");
            }
        }

        return collection;
    }

    public static IndexModel isIndexReady(String indexName, PineconeControlPlaneClient controlPlaneClient)
            throws InterruptedException {
        final IndexModel[] indexModels = new IndexModel[1];
        assertWithRetry(() -> {
            indexModels[0] = controlPlaneClient.describeIndex(indexName);
            assert (indexModels[0].getStatus().getReady());
        }, 3);

        return indexModels[0];
    }
}
