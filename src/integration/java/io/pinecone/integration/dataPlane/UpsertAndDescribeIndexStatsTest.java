package io.pinecone.integration.dataPlane;

import io.pinecone.*;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.model.CreateIndexRequest;
import io.pinecone.model.IndexMeta;
import io.pinecone.proto.*;
import org.junit.jupiter.api.*;

import static io.pinecone.helpers.BuildUpsertRequest.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class UpsertAndDescribeIndexStatsTest {
    private static VectorServiceGrpc.VectorServiceBlockingStub blockingStub;
    private static VectorServiceGrpc.VectorServiceFutureStub futureStub;
    private static final int dimension = 3;


    @BeforeAll
    public static void setUp() throws IOException {
        PineconeClientConfig config = new PineconeClientConfig()
                .withApiKey(System.getenv("PINECONE_API_KEY"))
                .withEnvironment(System.getenv("PINECONE_ENVIRONMENT"));

        PineconeIndexOperationClient controlPlaneClient = new PineconeIndexOperationClient(config);

        List<String> indexList = controlPlaneClient.listIndexes();
        String requiredParamsIndexName;
        if(!indexList.isEmpty()) {
            requiredParamsIndexName = indexList.get(0);
        }
        else {
            requiredParamsIndexName = RandomStringBuilder.build("index-name", 8);

            CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                    .withIndexName(requiredParamsIndexName)
                    .withDimension(dimension)
                    .withMetric("euclidean");
            controlPlaneClient.createIndex(createIndexRequest);
        }
        PineconeClient dataPlaneClient = new PineconeClient(config);
        IndexMeta indexMeta = controlPlaneClient.describeIndex(requiredParamsIndexName);
        String host = indexMeta.getStatus().getHost();
        PineconeConnection connection = dataPlaneClient.connect(
                new PineconeConnectionConfig()
                        .withConnectionUrl("https://" + host));
        blockingStub = connection.getBlockingStub();
        futureStub = connection.getFutureStub();
    }

    @Test
    public void UpsertRequiredVectorsAndDescribeIndexStatsTestSync() {
        // Get vector and namespace counts before upserting vectors with required parameters
        DescribeIndexStatsRequest describeIndexRequest = DescribeIndexStatsRequest.newBuilder().build();
        DescribeIndexStatsResponse describeIndexStatsResponse = blockingStub.describeIndexStats(describeIndexRequest);
        assertEquals(describeIndexStatsResponse.getDimension(), dimension);
        int startVectorCount = describeIndexStatsResponse.getTotalVectorCount();
        int startNamespaceCount = describeIndexStatsResponse.getNamespacesCount();

        // upsert vectors with required parameters
        UpsertResponse upsertResponse = blockingStub.upsert(buildRequiredUpsertRequest());

        // call describeIndexStats to get updated counts
        describeIndexStatsResponse = blockingStub.describeIndexStats(describeIndexRequest);

        // verify updated vector and namespace counts
        assertEquals(describeIndexStatsResponse.getNamespacesCount(), startNamespaceCount + 1);
        assertEquals(describeIndexStatsResponse.getTotalVectorCount(), startVectorCount + upsertResponse.getUpsertedCount());
    }

    @Test
    public void UpsertOptionalVectorsAndDescribeIndexStatsTestSync() {
        // Get vector and namespace counts before upserting vectors with required parameters
        DescribeIndexStatsRequest describeIndexRequest = DescribeIndexStatsRequest.newBuilder().build();
        DescribeIndexStatsResponse describeIndexStatsResponse = blockingStub.describeIndexStats(describeIndexRequest);
        assertEquals(describeIndexStatsResponse.getDimension(), dimension);
        int startVectorCount = describeIndexStatsResponse.getTotalVectorCount();
        int startNamespaceCount = describeIndexStatsResponse.getNamespacesCount();

        // upsert vectors with required parameters
        UpsertResponse upsertResponse = blockingStub.upsert(buildOptionalUpsertRequest());

        // call describeIndexStats to get updated counts
        describeIndexStatsResponse = blockingStub.describeIndexStats(describeIndexRequest);

        // verify updated vector and namespace counts
        assertEquals(describeIndexStatsResponse.getNamespacesCount(), startNamespaceCount + 1);
        assertEquals(describeIndexStatsResponse.getTotalVectorCount(), startVectorCount + upsertResponse.getUpsertedCount());
    }


    @Test
    public void UpsertRequiredVectorsAndDescribeIndexStatsTestFuture() throws ExecutionException, InterruptedException {
        // Get vector and namespace counts before upserting vectors with required parameters
        DescribeIndexStatsRequest describeIndexRequest = DescribeIndexStatsRequest.newBuilder().build();
        DescribeIndexStatsResponse describeIndexStatsResponse = futureStub.describeIndexStats(describeIndexRequest).get();
        assertEquals(describeIndexStatsResponse.getDimension(), dimension);
        int startVectorCount = describeIndexStatsResponse.getTotalVectorCount();
        int startNamespaceCount = describeIndexStatsResponse.getNamespacesCount();

        // upsert optional vectors
        UpsertResponse upsertResponse = futureStub.upsert(buildRequiredUpsertRequest()).get();

        // call describeIndexStats to get updated counts
        describeIndexStatsResponse = futureStub.describeIndexStats(describeIndexRequest).get();

        // verify updated vector and namespace counts
        assertEquals(describeIndexStatsResponse.getTotalVectorCount(), startVectorCount + upsertResponse.getUpsertedCount());
        assertEquals(describeIndexStatsResponse.getNamespacesCount(), startNamespaceCount + 1);
    }

    @Test
    public void UpsertOptionalVectorsAndDescribeIndexStatsTestFuture() throws ExecutionException, InterruptedException {
        // Get vector and namespace counts before upserting vectors with required parameters
        DescribeIndexStatsRequest describeIndexRequest = DescribeIndexStatsRequest.newBuilder().build();
        DescribeIndexStatsResponse describeIndexStatsResponse = futureStub.describeIndexStats(describeIndexRequest).get();
        assertEquals(describeIndexStatsResponse.getDimension(), dimension);
        int startVectorCount = describeIndexStatsResponse.getTotalVectorCount();
        int startNamespaceCount = describeIndexStatsResponse.getNamespacesCount();

        // upsert optional vectors
        UpsertResponse upsertResponse = futureStub.upsert(buildOptionalUpsertRequest()).get();

        // call describeIndexStats to get updated counts
        describeIndexStatsResponse = futureStub.describeIndexStats(describeIndexRequest).get();

        // verify updated vector and namespace counts
        assertEquals(describeIndexStatsResponse.getTotalVectorCount(), startVectorCount + upsertResponse.getUpsertedCount());
        assertEquals(describeIndexStatsResponse.getNamespacesCount(), startNamespaceCount + 1);
    }
}
