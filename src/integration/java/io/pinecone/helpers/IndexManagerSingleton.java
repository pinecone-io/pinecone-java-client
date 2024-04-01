package io.pinecone.helpers;

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.configs.PineconeConfig;
import io.pinecone.exceptions.PineconeException;
import io.pinecone.proto.DescribeIndexStatsResponse;
import org.openapitools.client.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static io.pinecone.helpers.BuildUpsertRequest.buildRequiredUpsertRequestByDimension;
import static io.pinecone.helpers.IndexManager.*;
import static org.junit.jupiter.api.Assertions.fail;

public class IndexManagerSingleton {
    private static final Logger logger = LoggerFactory.getLogger(IndexManager.class);
    private static IndexManagerSingleton instance;
    private static final String apiKey = System.getenv("PINECONE_API_KEY");
    private final int dimension = System.getenv("DIMENSION") == null
            ? 4
            : Integer.parseInt(System.getenv("DIMENSION"));

    private final String environment = System.getenv("PINECONE_ENVIRONMENT") == null
            ? "us-east4-gcp"
            : System.getenv("PINECONE_ENVIRONMENT");

    private static final IndexMetric metric = System.getenv("METRIC") == null
            ? IndexMetric.COSINE
            : IndexMetric.valueOf(System.getenv("METRIC"));

    private static final PineconeConfig pineconeConfig = new PineconeConfig(apiKey);
    private static Pinecone pineconeClient;
    private String podIndexName;
    private IndexModel podIndexModel;
    private String serverlessIndexName;
    private IndexModel serverlessIndexModel;
    private String collectionName;
    private CollectionModel collectionModel;
    private final List<String> podIndexVectorIds = Arrays.asList("v1", "v2", "v3");

    private IndexManagerSingleton() {
        pineconeClient = new Pinecone.Builder(apiKey).build();
    }

    public static IndexManagerSingleton getInstance() {
        if (instance == null) {
            instance = new IndexManagerSingleton();
        }
        return instance;
    }

    public String getPodIndexName() throws InterruptedException, PineconeException {
        if (podIndexName == null) {
            podIndexName = createPodIndex();
        }
        return podIndexName;
    }
    public String getServerlessIndexName() throws InterruptedException, PineconeException {
        if (serverlessIndexName == null) {
            serverlessIndexName = createServerlessIndex();
        }
        return serverlessIndexName;
    }
    public String getCollectionName() throws InterruptedException {
        if (collectionName == null) {
            collectionName = createCollection();
        }
        return collectionName;
    }
    public Pinecone getPineconeClient() {
        return pineconeClient;
    }

    public int getDimension() {
        return dimension;
    }

    public IndexMetric getMetric() {
        return metric;
    }

    public String getEnvironment() {
        return environment;
    }

    public List<String> getPodIndexVectorIds() {
        return podIndexVectorIds;
    }

    public IndexModel getPodIndexModel() throws InterruptedException {
        if (podIndexName == null) {
            podIndexName = createPodIndex();
        }
        podIndexModel = pineconeClient.describeIndex(podIndexName);
        return podIndexModel;
    }

    public IndexModel getServerlessIndexModel() throws InterruptedException {
        if (serverlessIndexName == null) {
            serverlessIndexName = createServerlessIndex();
        }
        serverlessIndexModel = pineconeClient.describeIndex(serverlessIndexName);
        return serverlessIndexModel;
    }

    public CollectionModel getCollectionModel() throws InterruptedException {
        if (collectionName == null) {
            collectionName = createCollection();
        }
        collectionModel = pineconeClient.describeCollection(collectionName);
        return collectionModel;
    }

    public void cleanupResources() {
        if (podIndexName != null) {
            pineconeClient.deleteIndex(podIndexName);
        }

        if (serverlessIndexName != null) {
            pineconeClient.deleteIndex(serverlessIndexName);
        }

        if (collectionName != null) {
            pineconeClient.deleteCollection(collectionName);
        }
    }

    private String createPodIndex() throws InterruptedException, PineconeException {
        String indexName = RandomStringBuilder.build("pod-index", 8);

        // Create index
        CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod()
                .podType("p1.x1")
                .environment(environment);
        CreateIndexRequestSpec spec = new CreateIndexRequestSpec().pod(podSpec);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest().name(indexName).dimension(dimension).metric(metric).spec(spec);
        podIndexModel = pineconeClient.createIndex(createIndexRequest);
        waitUntilIndexIsReady(pineconeClient, indexName);

        // Seed data
        seedIndex(podIndexVectorIds, indexName);

        return indexName;
    }

    private String createServerlessIndex() throws InterruptedException, PineconeException {
        String indexName = RandomStringBuilder.build("serverless-index", 8);

        // Create index
        ServerlessSpec serverlessSpec = new ServerlessSpec().cloud(ServerlessSpec.CloudEnum.AWS).region("us-west-2");
        CreateIndexRequestSpec spec = new CreateIndexRequestSpec().serverless(serverlessSpec);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest().name(indexName).dimension(dimension).metric(metric).spec(spec);
        serverlessIndexModel = pineconeClient.createIndex(createIndexRequest);

        return indexName;
    }

    private String createCollection() throws InterruptedException {
        // Create index if not exists
        if (podIndexName == null || podIndexName.isEmpty()) {
            podIndexName = createPodIndex();
        }
        collectionName = RandomStringBuilder.build("collection", 8);
        CreateCollectionRequest createCollectionRequest = new CreateCollectionRequest().name(collectionName).source(podIndexName);
        collectionModel = pineconeClient.createCollection(createCollectionRequest);

        // Wait until collection is ready
        int timeWaited = 0;
        CollectionModel.StatusEnum collectionReady = collectionModel.getStatus();
        while (collectionReady != CollectionModel.StatusEnum.READY && timeWaited < 120000) {
            logger.info("Waiting for collection " + collectionName + " to be ready. Waited " + timeWaited + " " +
                    "milliseconds...");
            Thread.sleep(5000);
            timeWaited += 5000;
            collectionModel = pineconeClient.describeCollection(collectionName);
            collectionReady = collectionModel.getStatus();
        }

        if (timeWaited > 120000) {
            fail("Collection: " + collectionName + " is not ready after 120 seconds");
        }

        return collectionName;
    }

    private void seedIndex(List<String> vectorIds, String indexName) throws InterruptedException {
        // Build upsert request
        Index indexClient = pineconeClient.createIndexConnection(indexName);
        indexClient.upsert(buildRequiredUpsertRequestByDimension(vectorIds, dimension), "");

        // Wait for record freshness
        DescribeIndexStatsResponse indexStats = indexClient.describeIndexStats();

        int totalTimeWaitedForVectors = 0;
        while (indexStats.getTotalVectorCount() == 0 || totalTimeWaitedForVectors <= 60000) {
            Thread.sleep(2000);
            totalTimeWaitedForVectors += 2000;
            indexStats = indexClient.describeIndexStats();
        }
        if (indexStats.getTotalVectorCount() == 0 && totalTimeWaitedForVectors >= 60000) {
            throw new PineconeException("Failed to seed index " + indexName + "with vectors");
        }
    }
}
