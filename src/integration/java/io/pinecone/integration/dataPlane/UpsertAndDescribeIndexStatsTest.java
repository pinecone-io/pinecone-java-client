package io.pinecone.integration.dataPlane;

import com.google.protobuf.Struct;
import io.pinecone.*;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.*;
import org.junit.jupiter.api.*;
import org.openapitools.client.model.IndexModelSpec;

import static io.pinecone.helpers.BuildUpsertRequest.*;
import static io.pinecone.helpers.IndexManager.createIndexIfNotExistsDataPlane;
import static io.pinecone.helpers.AssertRetry.assertWithRetry;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class UpsertAndDescribeIndexStatsTest {
    private static PineconeConnection connection;
    private static VectorServiceGrpc.VectorServiceBlockingStub blockingStub;
    private static VectorServiceGrpc.VectorServiceFutureStub futureStub;
    private static final int dimension = 3;
    private static final List emptyList = new ArrayList<>();
    private static final Struct emptyFilterStruct = Struct.newBuilder().build();
    private static final String emptyNamespace = "";

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException {
        connection = createIndexIfNotExistsDataPlane(dimension, IndexModelSpec.SERIALIZED_NAME_POD);
        blockingStub = connection.getBlockingStub();
        futureStub = connection.getFutureStub();
    }

    @AfterAll
    public static void cleanUp() {
        connection.close();
    }

    @Test
    public void UpsertRequiredVectorsAndDescribeIndexStatsSyncTest() throws InterruptedException {
        // Get vector count before upserting vectors with required parameters
        int numOfVectors = 3;
        PineconeDataPlaneClient dataPlaneClient = new PineconeDataPlaneClient(blockingStub);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = dataPlaneClient.describeIndexStats(emptyFilterStruct);
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);
        int startVectorCount = describeIndexStatsResponse1.getTotalVectorCount();

        // upsert vectors with required parameters
        UpsertResponse upsertResponse = dataPlaneClient.batchUpsert(getIdsList(numOfVectors),
                getValuesList(numOfVectors, dimension),
                emptyList,
                emptyList,
                emptyList,
                emptyNamespace);

        assertWithRetry(() -> {
            // call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse2 = dataPlaneClient.describeIndexStats(emptyFilterStruct);

            // verify the updated vector count
            assertEquals(describeIndexStatsResponse2.getTotalVectorCount(), startVectorCount + upsertResponse.getUpsertedCount());
        });
    }

    @Test
    public void UpsertOptionalVectorsAndDescribeIndexStatsSyncTest() throws InterruptedException {
        int numOfVectors = 5;
        // Checks if number of sparse vectors < total number of vectors
        int numOfSparseVectors = 2;
        // Checks if number of metadata structs are < total number of vectors
        int numOfMetadataStructs = 3;
        PineconeDataPlaneClient dataPlaneClient = new PineconeDataPlaneClient(blockingStub);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = dataPlaneClient.describeIndexStats(emptyFilterStruct);
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);
        int startVectorCount = describeIndexStatsResponse1.getTotalVectorCount();

        // upsert vectors with required + optional parameters
        UpsertResponse upsertResponse = dataPlaneClient.batchUpsert(
                getIdsList(numOfVectors),
                getValuesList(numOfVectors, dimension),
                getSparseIndicesList(numOfSparseVectors, dimension),
                getValuesList(numOfSparseVectors, dimension),
                getMetadataStruct(numOfMetadataStructs),
                RandomStringBuilder.build("ns", 8));

        assertWithRetry(() -> {
            // call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse2 = dataPlaneClient.describeIndexStats(emptyFilterStruct);

            // verify updated vector count
            assertEquals(describeIndexStatsResponse2.getTotalVectorCount(), startVectorCount + upsertResponse.getUpsertedCount());
        });
    }

    // ToDo: Update when future stub is added
//    @Test
//    public void UpsertRequiredVectorsAndDescribeIndexStatsFutureTest() throws ExecutionException, InterruptedException {
//        // Get vector and namespace counts before upserting vectors with required parameters
//        DescribeIndexStatsRequest describeIndexRequest = DescribeIndexStatsRequest.newBuilder().build();
//        DescribeIndexStatsResponse describeIndexStatsResponse1 = futureStub.describeIndexStats(describeIndexRequest).get();
//        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);
//        int startVectorCount = describeIndexStatsResponse1.getTotalVectorCount();
//        int startNamespaceCount = describeIndexStatsResponse1.getNamespacesCount();
//
//        // upsert optional vectors
//        UpsertResponse upsertResponse = futureStub.upsert(buildRequiredUpsertRequest()).get();
//
//        assertWithRetry(() -> {
//            // call describeIndexStats to get updated counts
//            DescribeIndexStatsResponse describeIndexStatsResponse2 = futureStub.describeIndexStats(describeIndexRequest).get();
//
//            // verify updated vector and namespace counts
//            assertEquals(describeIndexStatsResponse2.getTotalVectorCount(), startVectorCount + upsertResponse.getUpsertedCount());
//            assertEquals(describeIndexStatsResponse2.getNamespacesCount(), startNamespaceCount + 1);
//        });
//    }
//
//    @Test
//    public void UpsertOptionalVectorsAndDescribeIndexStatsFutureTest() throws ExecutionException, InterruptedException {
//        // Get vector and namespace counts before upserting vectors with required parameters
//        DescribeIndexStatsRequest describeIndexRequest = DescribeIndexStatsRequest.newBuilder().build();
//        DescribeIndexStatsResponse describeIndexStatsResponse1 = futureStub.describeIndexStats(describeIndexRequest).get();
//        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);
//        int startVectorCount = describeIndexStatsResponse1.getTotalVectorCount();
//        int startNamespaceCount = describeIndexStatsResponse1.getNamespacesCount();
//
//        // upsert optional vectors
//        UpsertResponse upsertResponse = futureStub.upsert(buildOptionalUpsertRequest()).get();
//
//        assertWithRetry(() -> {
//            // call describeIndexStats to get updated counts
//            DescribeIndexStatsResponse describeIndexStatsResponse2 = futureStub.describeIndexStats(describeIndexRequest).get();
//
//            // verify updated vector and namespace counts
//            assertEquals(describeIndexStatsResponse2.getTotalVectorCount(), startVectorCount + upsertResponse.getUpsertedCount());
//            assertEquals(describeIndexStatsResponse2.getNamespacesCount(), startNamespaceCount + 1);
//        });
//    }
}
