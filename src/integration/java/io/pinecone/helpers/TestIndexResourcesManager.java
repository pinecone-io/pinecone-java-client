package io.pinecone.helpers;

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
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

public class TestIndexResourcesManager {
    private static final Logger logger = LoggerFactory.getLogger(IndexManager.class);
    private static TestIndexResourcesManager instance;
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
    private Pinecone pineconeClient;
    private String podIndexName;
    private IndexModel podIndexModel;
    private String serverlessIndexName;
    private IndexModel serverlessIndexModel;
    private String collectionName;
    private CollectionModel collectionModel;
    private final List<String> podIndexVectorIds = Arrays.asList("v1", "v2", "v3");

    private TestIndexResourcesManager() {
        pineconeClient = new Pinecone.Builder(apiKey).build();
    }

    public static TestIndexResourcesManager getInstance() {
        if (instance == null) {
            instance = new TestIndexResourcesManager();
        }
        return instance;
    }

    public String getPodIndexName() throws InterruptedException, PineconeException {
        return createOrGetPodIndex();
    }

    public String getServerlessIndexName() throws InterruptedException, PineconeException {
        return createOrGetServerlessIndex();
    }

    public String getCollectionName() throws InterruptedException {
        return createOrGetCollection();
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
        podIndexModel = pineconeClient.describeIndex(createOrGetPodIndex());
        return podIndexModel;
    }

    public IndexModel getServerlessIndexModel() throws InterruptedException {
        serverlessIndexModel = pineconeClient.describeIndex(createOrGetServerlessIndex());
        return serverlessIndexModel;
    }

    public CollectionModel getCollectionModel() throws InterruptedException {
        collectionModel = pineconeClient.describeCollection(createOrGetCollection());
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

    private String createOrGetPodIndex() throws InterruptedException, PineconeException {
        if (podIndexName != null) {
            return podIndexName;
        }

        String indexName = RandomStringBuilder.build("pod-index", 8);

        // Create index
        CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod()
                .podType("p1.x1")
                .environment(environment);
        CreateIndexRequestSpec spec = new CreateIndexRequestSpec().pod(podSpec);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest().name(indexName).dimension(dimension).metric(metric).spec(spec);
        podIndexModel = pineconeClient.createIndex(createIndexRequest);
        waitUntilIndexIsReady(pineconeClient, indexName);

        // Explicitly wait after ready to avoid the "no healthy upstream" issue
        Thread.sleep(10000);

        // Seed data
        seedIndex(podIndexVectorIds, indexName);

        this.podIndexName = indexName;
        return indexName;
    }

    private String createOrGetServerlessIndex() throws InterruptedException, PineconeException {
        if (this.serverlessIndexName != null) {
            return this.serverlessIndexName;
        }

        String indexName = RandomStringBuilder.build("serverless-index", 8);

        // Create index
        ServerlessSpec serverlessSpec = new ServerlessSpec().cloud(ServerlessSpec.CloudEnum.AWS).region("us-west-2");
        CreateIndexRequestSpec spec = new CreateIndexRequestSpec().serverless(serverlessSpec);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest().name(indexName).dimension(dimension).metric(metric).spec(spec);
        serverlessIndexModel = pineconeClient.createIndex(createIndexRequest);

        serverlessIndexName = indexName;
        return indexName;
    }

    private String createOrGetCollection() throws InterruptedException {
        if (collectionName != null) {
            return collectionName;
        }

        // Create index if not exists
        String sourceIndexName = createOrGetPodIndex();

        collectionName = RandomStringBuilder.build("collection", 8);
        CreateCollectionRequest createCollectionRequest = new CreateCollectionRequest().name(collectionName).source(sourceIndexName);
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
