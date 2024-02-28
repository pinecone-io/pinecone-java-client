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
import java.util.List;

public class UpsertAndDescribeIndexStatsTest {
    private static PineconeConnection connection;
    private static VectorServiceGrpc.VectorServiceBlockingStub blockingStub;
    private static VectorServiceGrpc.VectorServiceFutureStub futureStub;
    private static final int dimension = 3;
    private static final Struct emptyFilterStruct = Struct.newBuilder().build();

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
        // Confirm the starting state by verifying the dimension of the index
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);
        int vectorCount = describeIndexStatsResponse1.getTotalVectorCount();

        // Upsert vectors with required parameters
        List<String> upsertIds = getIdsList(numOfVectors);
        for (String id : upsertIds) {
            UpsertResponse upsertResponse = dataPlaneClient.upsert(id, generateVectorValuesByDimension(dimension));
            vectorCount += upsertResponse.getUpsertedCount();
        }
        int actualVectorCount = vectorCount;

        assertWithRetry(() -> {
            // call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse2 = dataPlaneClient.describeIndexStats(emptyFilterStruct);

            // verify the updated vector count
            assertEquals(describeIndexStatsResponse2.getTotalVectorCount(), actualVectorCount);
        });
    }

    @Test
    public void UpsertOptionalVectorsAndDescribeIndexStatsSyncTest() throws InterruptedException {
        int numOfVectors = 5;
        PineconeDataPlaneClient dataPlaneClient = new PineconeDataPlaneClient(blockingStub);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = dataPlaneClient.describeIndexStats(emptyFilterStruct);
        // Confirm the starting state by verifying the dimension of the index
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);
        int vectorCount = describeIndexStatsResponse1.getTotalVectorCount();

        // upsert vectors with required + optional parameters
        List<String> upsertIds = getIdsList(numOfVectors);
        String namespace = RandomStringBuilder.build("ns", 8);
        for (String id : upsertIds) {
            UpsertResponse upsertResponse = dataPlaneClient.upsert(id,
                    generateVectorValuesByDimension(dimension),
                    generateSparseIndicesByDimension(dimension),
                    generateVectorValuesByDimension(dimension),
                    generateMetadataStruct(),
                    namespace);
            vectorCount += upsertResponse.getUpsertedCount();
        }
        int actualVectorCount = vectorCount;
        assertWithRetry(() -> {
            // call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse2 = dataPlaneClient.describeIndexStats(emptyFilterStruct);

            // verify updated vector count
            assertEquals(describeIndexStatsResponse2.getTotalVectorCount(), actualVectorCount);
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
