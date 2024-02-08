package io.pinecone.integration.dataplane;

import io.pinecone.*;
import io.pinecone.proto.*;
import org.junit.jupiter.api.*;
import org.openapitools.client.ApiException;
import org.openapitools.client.model.IndexModelSpec;

import static io.pinecone.helpers.BuildUpsertRequest.*;
import static io.pinecone.helpers.IndexManager.createIndexIfNotExistsDataPlane;
import static io.pinecone.helpers.AssertRetry.assertWithRetry;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class UpsertAndDescribeIndexStatsTest {
    private static VectorServiceGrpc.VectorServiceBlockingStub blockingStub;
    private static VectorServiceGrpc.VectorServiceFutureStub futureStub;
    private static final int dimension = 3;

    @BeforeAll
    public static void setUp() throws ApiException, InterruptedException {
        PineconeConnection connection = createIndexIfNotExistsDataPlane(dimension, IndexModelSpec.SERIALIZED_NAME_POD);
        blockingStub = connection.getBlockingStub();
        futureStub = connection.getFutureStub();
    }

    @Test
    public void UpsertRequiredVectorsAndDescribeIndexStatsSyncTest() throws ApiException, InterruptedException {
        // Get vector and namespace counts before upserting vectors with required parameters
        DescribeIndexStatsRequest describeIndexRequest = DescribeIndexStatsRequest.newBuilder().build();
        DescribeIndexStatsResponse describeIndexStatsResponse1 = blockingStub.describeIndexStats(describeIndexRequest);
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);
        int startVectorCount = describeIndexStatsResponse1.getTotalVectorCount();
        int startNamespaceCount = describeIndexStatsResponse1.getNamespacesCount();

        // upsert vectors with required parameters
        UpsertResponse upsertResponse = blockingStub.upsert(buildRequiredUpsertRequest());

        assertWithRetry(() -> {
            // call describeIndexStats to get updated counts
            DescribeIndexStatsResponse describeIndexStatsResponse2 = blockingStub.describeIndexStats(describeIndexRequest);

            // verify updated vector and namespace counts
            assertEquals(describeIndexStatsResponse2.getNamespacesCount(), startNamespaceCount + 1);
            assertEquals(describeIndexStatsResponse2.getTotalVectorCount(), startVectorCount + upsertResponse.getUpsertedCount());
        });

    }

    @Test
    public void UpsertOptionalVectorsAndDescribeIndexStatsSyncTest() throws ApiException, InterruptedException {
        // Get vector and namespace counts before upserting vectors with required parameters
        DescribeIndexStatsRequest describeIndexRequest = DescribeIndexStatsRequest.newBuilder().build();
        DescribeIndexStatsResponse describeIndexStatsResponse1 = blockingStub.describeIndexStats(describeIndexRequest);
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);
        int startVectorCount = describeIndexStatsResponse1.getTotalVectorCount();
        int startNamespaceCount = describeIndexStatsResponse1.getNamespacesCount();

        // upsert vectors with required parameters
        UpsertResponse upsertResponse = blockingStub.upsert(buildOptionalUpsertRequest());

        assertWithRetry(() -> {
            // call describeIndexStats to get updated counts
            DescribeIndexStatsResponse describeIndexStatsResponse2 = blockingStub.describeIndexStats(describeIndexRequest);

            // verify updated vector and namespace counts
            assertEquals(describeIndexStatsResponse2.getNamespacesCount(), startNamespaceCount + 1);
            assertEquals(describeIndexStatsResponse2.getTotalVectorCount(), startVectorCount + upsertResponse.getUpsertedCount());
        });
    }


    @Test
    public void UpsertRequiredVectorsAndDescribeIndexStatsFutureTest() throws ApiException, ExecutionException, InterruptedException {
        // Get vector and namespace counts before upserting vectors with required parameters
        DescribeIndexStatsRequest describeIndexRequest = DescribeIndexStatsRequest.newBuilder().build();
        DescribeIndexStatsResponse describeIndexStatsResponse1 = futureStub.describeIndexStats(describeIndexRequest).get();
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);
        int startVectorCount = describeIndexStatsResponse1.getTotalVectorCount();
        int startNamespaceCount = describeIndexStatsResponse1.getNamespacesCount();

        // upsert optional vectors
        UpsertResponse upsertResponse = futureStub.upsert(buildRequiredUpsertRequest()).get();

        assertWithRetry(() -> {
            // call describeIndexStats to get updated counts
            DescribeIndexStatsResponse describeIndexStatsResponse2 = futureStub.describeIndexStats(describeIndexRequest).get();

            // verify updated vector and namespace counts
            assertEquals(describeIndexStatsResponse2.getTotalVectorCount(), startVectorCount + upsertResponse.getUpsertedCount());
            assertEquals(describeIndexStatsResponse2.getNamespacesCount(), startNamespaceCount + 1);
        });
    }

    @Test
    public void UpsertOptionalVectorsAndDescribeIndexStatsFutureTest() throws ApiException, ExecutionException, InterruptedException {
        // Get vector and namespace counts before upserting vectors with required parameters
        DescribeIndexStatsRequest describeIndexRequest = DescribeIndexStatsRequest.newBuilder().build();
        DescribeIndexStatsResponse describeIndexStatsResponse1 = futureStub.describeIndexStats(describeIndexRequest).get();
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);
        int startVectorCount = describeIndexStatsResponse1.getTotalVectorCount();
        int startNamespaceCount = describeIndexStatsResponse1.getNamespacesCount();

        // upsert optional vectors
        UpsertResponse upsertResponse = futureStub.upsert(buildOptionalUpsertRequest()).get();

        assertWithRetry(() -> {
            // call describeIndexStats to get updated counts
            DescribeIndexStatsResponse describeIndexStatsResponse2 = futureStub.describeIndexStats(describeIndexRequest).get();

            // verify updated vector and namespace counts
            assertEquals(describeIndexStatsResponse2.getTotalVectorCount(), startVectorCount + upsertResponse.getUpsertedCount());
            assertEquals(describeIndexStatsResponse2.getNamespacesCount(), startNamespaceCount + 1);
        });
    }
}
