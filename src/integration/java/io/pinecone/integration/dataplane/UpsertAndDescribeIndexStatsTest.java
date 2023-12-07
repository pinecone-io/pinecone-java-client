package io.pinecone.integration.dataplane;

import io.pinecone.*;
import io.pinecone.proto.*;
import org.junit.jupiter.api.*;

import static io.pinecone.helpers.BuildUpsertRequest.*;
import static io.pinecone.helpers.IndexManager.createIndexIfNotExistsDataPlane;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class UpsertAndDescribeIndexStatsTest {
    private static VectorServiceGrpc.VectorServiceBlockingStub blockingStub;
    private static VectorServiceGrpc.VectorServiceFutureStub futureStub;
    private static final int dimension = 3;

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException {
        PineconeConnection connection = createIndexIfNotExistsDataPlane(dimension);
        blockingStub = connection.getBlockingStub();
        futureStub = connection.getFutureStub();
    }

    @Test
    public void UpsertRequiredVectorsAndDescribeIndexStatsSyncTest() {
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
    public void UpsertOptionalVectorsAndDescribeIndexStatsSyncTest() {
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
    public void UpsertRequiredVectorsAndDescribeIndexStatsFutureTest() throws ExecutionException, InterruptedException {
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
    public void UpsertOptionalVectorsAndDescribeIndexStatsFutureTest() throws ExecutionException, InterruptedException {
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
