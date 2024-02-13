package io.pinecone.helpers;

import io.pinecone.*;
import io.pinecone.exceptions.PineconeException;
import org.openapitools.client.ApiException;
import org.openapitools.client.model.*;

import java.io.IOException;
import java.util.List;

import static io.pinecone.helpers.AssertRetry.assertWithRetry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class IndexManager {
    private static PineconeClientConfig config;

    public static PineconeConnection createIndexIfNotExistsDataPlane(int dimension, String indexType) throws InterruptedException, PineconeException {
        String indexName = RandomStringBuilder.build("index-name", 8);
        String apiKey = System.getenv("PINECONE_API_KEY");
        String environment = System.getenv("PINECONE_ENVIRONMENT");
        config = new PineconeClientConfig().withApiKey(apiKey).withEnvironment(environment);
        PineconeControlPlaneClient controlPlaneClient = new PineconeControlPlaneClient(apiKey);

        // Check if index already exists
        IndexModel index = findIndex(controlPlaneClient, indexName);
        // Create new index if needed, do not proceed until index is ready
        if (index == null) index = createNewIndex(controlPlaneClient, indexName, indexType, dimension, true);

        // ToDo: Update the constructor by removing dependency on PineconeClientConfig
        PineconeClient dataPlaneClient = new PineconeClient(config);
        String host = controlPlaneClient.describeIndex(indexName).getHost();

        return dataPlaneClient.connect(
                new PineconeConnectionConfig()
                        .withConnectionUrl("https://" + host));
    }

    public static IndexModel createIndexIfNotExistsControlPlane(PineconeControlPlaneClient controlPlaneClient, int dimension, String indexType, boolean waitUntilReady) throws InterruptedException, PineconeException {
        String indexName = RandomStringBuilder.build("index-name", 8);
        IndexModel index = findIndex(controlPlaneClient, indexName);

        return (index == null) ? createNewIndex(controlPlaneClient, indexName, indexType, dimension, waitUntilReady) : index;
    }

    private static String findIndexWithDimensionAndType(IndexList indexList, int dimension, PineconeControlPlaneClient controlPlaneClient, String indexType)
            throws InterruptedException, PineconeException {
        int i = 0;
        List<IndexModel> indexModels = indexList.getIndexes();
        while (i < indexModels.size()) {
            IndexModel indexModel = waitUntilIndexIsReady(controlPlaneClient, indexModels.get(i).getName());
            // ToDo: add pod type support
            if (indexModel.getDimension() == dimension
                    && ((indexType.equalsIgnoreCase(IndexModelSpec.SERIALIZED_NAME_POD) && indexModel.getSpec().getPod() != null && indexModel.getSpec().getPod().getReplicas() == 1 && indexModel.getSpec().getPod().getPodType().equalsIgnoreCase("p1.x1"))
                    || (indexType.equalsIgnoreCase(IndexModelSpec.SERIALIZED_NAME_SERVERLESS)))) {
                return indexModel.getName();
            }
            i++;
        }
        return "";
    }

    private static IndexModel findIndex(PineconeControlPlaneClient controlPlaneClient, String indexName) throws PineconeException {
        IndexList indexList = controlPlaneClient.listIndexes();
        for (IndexModel index : indexList.getIndexes()) {
            if (index.getName().equals(indexName)) {
                return index;
            }
        }
        return null;
    }

    private static IndexModel createNewIndex(PineconeControlPlaneClient controlPlaneClient, String indexName, String indexType, int dimension, Boolean waitUntilReady) throws InterruptedException, PineconeException {
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
        IndexModel index = controlPlaneClient.createIndex(createIndexRequest);

        if (waitUntilReady) {
            waitUntilIndexIsReady(controlPlaneClient, indexName, 200000);
        }

        return index;
    }

    public static PineconeConnection createNewIndexAndConnect(PineconeControlPlaneClient controlPlaneClient, String indexName, int dimension, IndexMetric metric, CreateIndexRequestSpec spec) throws InterruptedException, PineconeException {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest().name(indexName).dimension(dimension).metric(metric).spec(spec);
        controlPlaneClient.createIndex(createIndexRequest);

        // Wait until index is ready
        waitUntilIndexIsReady(controlPlaneClient, indexName, 200000);
        String host = controlPlaneClient.describeIndex(indexName).getHost();

        PineconeClientConfig specificConfig = new PineconeClientConfig().withApiKey(System.getenv("PINECONE_API_KEY")).withEnvironment(System.getenv("PINECONE_ENVIRONMENT"));
        PineconeClient dataPlaneClient = new PineconeClient(specificConfig);

        return dataPlaneClient.connect(
                new PineconeConnectionConfig()
                        .withConnectionUrl("https://" + host));
    }

    public static IndexModel isIndexReady(String indexName, PineconeControlPlaneClient controlPlaneClient)
            throws InterruptedException, PineconeException {
        final IndexModel[] indexModels = new IndexModel[1];
        assertWithRetry(() -> {
            indexModels[0] = controlPlaneClient.describeIndex(indexName);
            assert (indexModels[0].getStatus().getReady());
        }, 3);

        return indexModels[0];
    }

    public static IndexModel waitUntilIndexIsReady(PineconeControlPlaneClient controlPlaneClient, String indexName, Integer totalMsToWait) throws InterruptedException {
        IndexModel index = controlPlaneClient.describeIndex(indexName);
        int waitedTimeMs = 0;
        int intervalMs = 1500;

        while (!index.getStatus().getReady()) {
            index = controlPlaneClient.describeIndex(indexName);
            if (waitedTimeMs >= totalMsToWait) {
                System.out.println("Index " + indexName + " not ready after " + waitedTimeMs + "ms");
                break;
            }
            if (index.getStatus().getReady()) {
                Thread.sleep(10000);
                System.out.println("Index " + indexName + " is ready after " + waitedTimeMs + 10000 + "ms");
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

    public static CollectionModel createCollection(PineconeControlPlaneClient controlPlaneClient, String collectionName, String indexName, boolean waitUntilReady) throws InterruptedException {
        CreateCollectionRequest createCollectionRequest = new CreateCollectionRequest().name(collectionName).source(indexName);
        CollectionModel collection = controlPlaneClient.createCollection(createCollectionRequest);

        assertEquals(collection.getStatus(), CollectionModel.StatusEnum.INITIALIZING);

        // Wait until collection is ready
        int timeWaited = 0;
        CollectionModel.StatusEnum collectionReady = collection.getStatus();
        while (collectionReady != CollectionModel.StatusEnum.READY && timeWaited < 120000) {
            System.out.println("Waiting for collection" + collectionName + " to be ready. Waited " + timeWaited + " milliseconds...");
            Thread.sleep(5000);
            timeWaited += 5000;
            collection = controlPlaneClient.describeCollection(collectionName);
            collectionReady = collection.getStatus();
            System.out.println("Collection Ready? " + collectionReady);
        }

        if (timeWaited > 120000) {
            fail("Collection: " + collectionName + " is not ready after 120 seconds");
        }

        return collection;
    }
}
