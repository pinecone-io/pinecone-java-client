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

            // create requiredParamsIndex
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
    public void UpsertVectorsAndDescribeIndexStatsTestSync() {
        blockingStub.upsert(buildRequiredUpsertRequest());
        DescribeIndexStatsRequest describeIndexRequest = DescribeIndexStatsRequest.newBuilder().build();
        DescribeIndexStatsResponse describeIndexStatsResponse = blockingStub.describeIndexStats(describeIndexRequest);
        assertEquals(describeIndexStatsResponse.getDimension(), dimension);
        assert describeIndexStatsResponse.getNamespacesCount()>0;
        assert describeIndexStatsResponse.getTotalVectorCount()>0;
    }

    @Test
    public void UpsertVectorsAndDescribeIndexStatsTestFuture() throws ExecutionException, InterruptedException {
        futureStub.upsert(buildRequiredUpsertRequest());
        DescribeIndexStatsRequest describeIndexRequest = DescribeIndexStatsRequest.newBuilder().build();
        DescribeIndexStatsResponse describeIndexStatsResponse = futureStub.describeIndexStats(describeIndexRequest).get();
        assertEquals(describeIndexStatsResponse.getDimension(), dimension);
        assert describeIndexStatsResponse.getNamespacesCount()>0;
        assert describeIndexStatsResponse.getTotalVectorCount()>0;
    }
}
