package io.pinecone.integration.dataPlane;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.pinecone.PineconeConnection;
import io.pinecone.PineconeDataPlaneClient;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.IndexModelSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.pinecone.helpers.AssertRetry.assertWithRetry;
import static io.pinecone.helpers.BuildUpsertRequest.*;
import static io.pinecone.helpers.IndexManager.createIndexIfNotExistsDataPlane;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UpsertDescribeIndexStatsAndDeleteTest {
    private static PineconeConnection connection;
    private static VectorServiceGrpc.VectorServiceBlockingStub blockingStub;
    private static VectorServiceGrpc.VectorServiceFutureStub futureStub;
    private static final int dimension = 3;
    private static final List emptyList = new ArrayList<>();
    private static final Struct nullFilterStruct = null;
    private static String emptyNamespace = "";

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
    public void UpsertVectorsAndDeleteByIdSyncTest() throws InterruptedException {
        // Upsert vectors with required parameters
        int numOfVectors = 3;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        PineconeDataPlaneClient dataPlaneClient = new PineconeDataPlaneClient(blockingStub);
        UpsertResponse upsertResponse = dataPlaneClient.batchUpsert(
                upsertIds,
                getValuesList(numOfVectors, dimension),
                emptyList,
                emptyList,
                emptyList,
                namespace);

        // Get vector count before deleting the vector
        int startVectorCount = upsertResponse.getUpsertedCount();
        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = dataPlaneClient.describeIndexStats(nullFilterStruct);
            // Verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), upsertResponse.getUpsertedCount());
        });

        // Delete 1 vector
        List<String> idsToDelete = new ArrayList<>(3);
        idsToDelete.add(upsertIds.get(0));
        dataPlaneClient.delete(idsToDelete, false, namespace, nullFilterStruct);
        startVectorCount -= idsToDelete.size();
        int testSingleDeletedVectorCount = startVectorCount;

        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = dataPlaneClient.describeIndexStats(nullFilterStruct);
            // Verify the updated vector count should be 1 less than previous vector count since number of vectors deleted = 1
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), testSingleDeletedVectorCount);
        });

        // vector id at index 0 is already deleted
        idsToDelete.remove(0);
        // Add ids to delete multiple vectors
        idsToDelete.add(upsertIds.get(1));
        idsToDelete.add(upsertIds.get(2));

        // Delete multiple vectors
        dataPlaneClient.delete(idsToDelete, false, namespace, null);

        // Update startVectorCount
        startVectorCount -= idsToDelete.size();
        int testMultipleDeletedVectorCount = startVectorCount;

        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = dataPlaneClient.describeIndexStats(nullFilterStruct);
            // Verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), testMultipleDeletedVectorCount);
        });
    }

    @Test
    public void UpsertVectorsAndDeleteByNamespaceSyncTest() throws InterruptedException {
        int numOfVectors = 3;
        int numOfSparseVectors = 2;
        int numOfMetadataStructs = 3;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        PineconeDataPlaneClient dataPlaneClient = new PineconeDataPlaneClient(blockingStub);

        // Upsert vectors with optional parameters
        UpsertResponse upsertResponse = dataPlaneClient.batchUpsert(
                upsertIds,
                getValuesList(numOfVectors, dimension),
                getSparseIndicesList(numOfSparseVectors, dimension),
                getValuesList(numOfSparseVectors, dimension),
                getMetadataStruct(numOfMetadataStructs),
                namespace);

        // Verify the vectors are upserts
        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = dataPlaneClient.describeIndexStats(nullFilterStruct);
            // Verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), upsertResponse.getUpsertedCount());
        });

        // Delete all vectors in the namespace by setting deleteAll = true and without setting IDs and filters
        dataPlaneClient.delete(emptyList, true, namespace, null);

        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = dataPlaneClient.describeIndexStats(nullFilterStruct);
            NamespaceSummary namespaceSummary = describeIndexStatsResponse.getNamespacesMap().get(namespace);
            // Verify the namespace is deleted
            assertThrows(NullPointerException.class, () -> namespaceSummary.getVectorCount());
        });
    }

    @Test
    public void UpsertVectorsAndDeleteByFilterSyncTest() throws InterruptedException {
        int numOfVectors = 3;
        int numOfSparseVectors = 2;
        int numOfMetadataStructs = 3;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        PineconeDataPlaneClient dataPlaneClient = new PineconeDataPlaneClient(blockingStub);

        // Upsert vectors with required + optional and custom metadata parameters
        UpsertResponse upsertResponse = dataPlaneClient.batchUpsert(
                upsertIds,
                getValuesList(numOfVectors, dimension),
                getSparseIndicesList(numOfSparseVectors, dimension),
                getValuesList(numOfSparseVectors, dimension),
                getMetadataStruct(numOfMetadataStructs),
                namespace);

        // Verify the vectors are upserts
        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = dataPlaneClient.describeIndexStats(nullFilterStruct);
            // Verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), upsertResponse.getUpsertedCount());
        });

        String fieldToDelete = metadataFields[0];
        String valueToDelete = createAndGetMetadataMap().get(fieldToDelete).get(0);
        Struct filterStruct = Struct.newBuilder()
                .putFields(fieldToDelete, Value.newBuilder()
                        .setStructValue(Struct.newBuilder()
                                .putFields("$eq", Value.newBuilder()
                                        .setStringValue(valueToDelete)
                                        .build()))
                        .build())
                .build();

        // Delete by filtering
        dataPlaneClient.delete(emptyList, false, namespace, filterStruct);

        // Update startVectorCount
        int expectedVectorCount = upsertResponse.getUpsertedCount() - 1;

        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = dataPlaneClient.describeIndexStats(nullFilterStruct);
            // Verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), expectedVectorCount);
        });
    }

    // ToDo: Update when future stub changes are in
//    @Test
//    public void UpsertVectorsAndDeleteByIdFutureTest() throws ExecutionException, InterruptedException {
//        // Upsert vectors with required parameters
//        String namespace = RandomStringBuilder.build("ns", 8);
//        List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
//        UpsertResponse upsertResponse = futureStub.upsert(buildRequiredUpsertRequest(upsertIds, namespace)).get();
//        Thread.sleep(3500);
//
//        // Get vector count before deleting vectors
//        DescribeIndexStatsRequest describeIndexRequest = DescribeIndexStatsRequest.newBuilder().build();
//        DescribeIndexStatsResponse describeIndexStatsResponse = blockingStub.describeIndexStats(describeIndexRequest);
//        int startVectorCount = describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount();
//        assertEquals(startVectorCount, upsertResponse.getUpsertedCount());
//
//        // Delete 1 vector
//        String[] idsToDelete = new String[]{upsertIds.get(0)};
//        DeleteRequest deleteRequest = DeleteRequest.newBuilder()
//                .setNamespace(namespace)
//                .addAllIds(Arrays.asList(idsToDelete))
//                .setDeleteAll(false)
//                .build();
//        blockingStub.delete(deleteRequest);
//        Thread.sleep(3500);
//
//        // Call describeIndexStats to get updated counts
//        describeIndexStatsResponse = futureStub.describeIndexStats(describeIndexRequest).get();
//        assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), startVectorCount - idsToDelete.length);
//        startVectorCount -= idsToDelete.length;
//
//        // Delete multiple vectors
//        idsToDelete = new String[]{upsertIds.get(1), upsertIds.get(2)};
//        deleteRequest = DeleteRequest.newBuilder()
//                .setNamespace(namespace)
//                .addAllIds(Arrays.asList(idsToDelete))
//                .setDeleteAll(false)
//                .build();
//        blockingStub.delete(deleteRequest);
//        Thread.sleep(3500);
//
//        // Call describeIndexStats to get updated counts
//        describeIndexStatsResponse = futureStub.describeIndexStats(describeIndexRequest).get();
//        // Verify the updated vector count
//        assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), startVectorCount - idsToDelete.length);
//    }
//
//    @Test
//    public void UpsertVectorsAndDeleteByNamespaceFutureTest() throws ExecutionException, InterruptedException {
//        // Upsert vectors with optional parameters
//        String namespace = RandomStringBuilder.build("ns", 8);
//        UpsertResponse upsertResponse = futureStub.upsert(buildOptionalUpsertRequest(namespace)).get();
//        Thread.sleep(3500);
//
//        // Get vector count before deleting vectors
//        DescribeIndexStatsRequest describeIndexRequest = DescribeIndexStatsRequest.newBuilder().build();
//        DescribeIndexStatsResponse describeIndexStatsResponse = futureStub.describeIndexStats(describeIndexRequest).get();
//        int startVectorCount = describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount();
//        assertEquals(startVectorCount, upsertResponse.getUpsertedCount());
//
//        // Delete all vectors in the namespace
//        DeleteRequest deleteRequest = DeleteRequest.newBuilder()
//                .setNamespace(namespace)
//                .setDeleteAll(true)
//                .build();
//        futureStub.delete(deleteRequest);
//        Thread.sleep(3500);
//
//        // Call describeIndexStats to get updated counts
//        NamespaceSummary namespaceSummary = futureStub.describeIndexStats(describeIndexRequest).get().getNamespacesMap().get(namespace);
//        // Verify the namespace is deleted
//        assertThrows(NullPointerException.class, () -> namespaceSummary.getVectorCount());
//    }
//
//    @Test
//    public void UpsertVectorsAndDeleteByFilterFutureTest() throws InterruptedException, ExecutionException {
//        // Upsert vectors with optional and custom metadata parameters
//        String namespace = RandomStringBuilder.build("ns", 8);
//        String fieldToDelete = metadataFields[0];
//        String valueToDelete = createAndGetMetadataMap().get(fieldToDelete).get(0);
//        UpsertResponse upsertResponse = futureStub.upsert(buildOptionalUpsertRequest(namespace)).get();
//        Thread.sleep(3500);
//
//        // Get vector count before deleting vectors
//        DescribeIndexStatsRequest describeIndexRequest = DescribeIndexStatsRequest.newBuilder().build();
//        DescribeIndexStatsResponse describeIndexStatsResponse = futureStub.describeIndexStats(describeIndexRequest).get();
//        int startVectorCount = describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount();
//        assertEquals(upsertResponse.getUpsertedCount(), startVectorCount);
//
//        // Delete by filtering
//        DeleteRequest deleteRequest = DeleteRequest.newBuilder()
//                .setNamespace(namespace)
//                .setDeleteAll(false)
//                .setFilter(Struct.newBuilder()
//                        .putFields(metadataFields[0], Value.newBuilder()
//                                .setStructValue(Struct.newBuilder()
//                                        .putFields("$eq", Value.newBuilder()
//                                                .setStringValue(valueToDelete)
//                                                .build()))
//                                .build())
//                        .build())
//                .build();
//        futureStub.delete(deleteRequest);
//        Thread.sleep(3500);
//
//        // Call describeIndexStats to get updated counts
//        NamespaceSummary namespaceSummary = futureStub.describeIndexStats(describeIndexRequest).get().getNamespacesMap().get(namespace);
//        // Verify the updated vector count
//        assertEquals(namespaceSummary.getVectorCount(), upsertResponse.getUpsertedCount() - 1);
//    }
}
