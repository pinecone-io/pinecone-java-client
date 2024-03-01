package io.pinecone.integration.dataPlane;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.pinecone.PineconeConnection;
import io.pinecone.PineconeBlockingDataPlaneClient;
import io.pinecone.PineconeFutureDataPlaneClient;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.IndexModelSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.pinecone.helpers.AssertRetry.assertWithRetry;
import static io.pinecone.helpers.BuildUpsertRequest.*;
import static io.pinecone.helpers.IndexManager.createIndexIfNotExistsDataPlane;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UpsertDescribeIndexStatsAndDeleteTest {
    private static PineconeConnection connection;
    private static VectorServiceGrpc.VectorServiceBlockingStub blockingStub;
    private static VectorServiceGrpc.VectorServiceFutureStub futureStub;
    private static final int dimension = 3;
    private static final List emptyList = new ArrayList<>();
    private static final Struct nullFilterStruct = null;

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
    public void upsertVectorsAndDeleteByIdSyncTest() throws InterruptedException {
        // Upsert vectors with required parameters
        int numOfVectors = 3;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        PineconeBlockingDataPlaneClient dataPlaneClient = new PineconeBlockingDataPlaneClient(blockingStub);
        int vectorCount = 0;
        for (String id : upsertIds) {
            UpsertResponse upsertResponse = dataPlaneClient.upsert(id,
                    generateVectorValuesByDimension(dimension),
                    namespace);
            vectorCount += upsertResponse.getUpsertedCount();
        }

        int actualVectorCount = vectorCount;

        assertWithRetry(() -> {
            // call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = dataPlaneClient.describeIndexStats(null);

            // verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), actualVectorCount);
        });

        // Delete 1 vector
        List<String> idsToDelete = new ArrayList<>(3);
        idsToDelete.add(upsertIds.get(0));
        dataPlaneClient.delete(idsToDelete, false, namespace, nullFilterStruct);
        vectorCount -= idsToDelete.size();
        int testSingleDeletedVectorCount = vectorCount;

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
        vectorCount -= idsToDelete.size();
        int testMultipleDeletedVectorCount = vectorCount;

        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = dataPlaneClient.describeIndexStats(nullFilterStruct);
            // Verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), testMultipleDeletedVectorCount);
        });
    }

    @Test
    public void upsertVectorsAndDeleteByFilterSyncTest() throws InterruptedException {
        int numOfVectors = 3;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        PineconeBlockingDataPlaneClient dataPlaneClient = new PineconeBlockingDataPlaneClient(blockingStub);

        int vectorCount = 0;
        // Upsert vectors with required + optional and custom metadata parameters
        for (int i=0; i<upsertIds.size(); i++) {
            UpsertResponse upsertResponse = dataPlaneClient.upsert(upsertIds.get(i),
                    generateVectorValuesByDimension(dimension),
                    generateSparseIndicesByDimension(dimension),
                    generateVectorValuesByDimension(dimension),
                    generateMetadataStruct(i, i),
                    namespace);
            vectorCount += upsertResponse.getUpsertedCount();
        }

        int actualVectorCount = vectorCount;
        // Verify the vectors are upserts
        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = dataPlaneClient.describeIndexStats(nullFilterStruct);
            // Verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), actualVectorCount);
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
        int updatedVectorCount = actualVectorCount - 1;

        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = dataPlaneClient.describeIndexStats(nullFilterStruct);
            // Verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), updatedVectorCount);
        });
    }

    // ToDo: Update when future stub changes are in
    @Test
    public void upsertVectorsAndDeleteByIdFutureTest() throws InterruptedException, ExecutionException {
        // Upsert vectors with required parameters
        int numOfVectors = 3;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        PineconeFutureDataPlaneClient dataPlaneClient = new PineconeFutureDataPlaneClient(futureStub);
        int vectorCount = 0;
        for (String id : upsertIds) {
            UpsertResponse upsertResponse = dataPlaneClient.upsert(id,
                    generateVectorValuesByDimension(dimension),
                    namespace).get();
            vectorCount += upsertResponse.getUpsertedCount();
        }

        int actualVectorCount = vectorCount;

        assertWithRetry(() -> {
            // call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = dataPlaneClient.describeIndexStats(null).get();

            // verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), actualVectorCount);
        });

        // Delete 1 vector
        List<String> idsToDelete = new ArrayList<>(3);
        idsToDelete.add(upsertIds.get(0));
        dataPlaneClient.delete(idsToDelete, false, namespace, nullFilterStruct);
        vectorCount -= idsToDelete.size();
        int testSingleDeletedVectorCount = vectorCount;

        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = dataPlaneClient.describeIndexStats(nullFilterStruct).get();
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
        vectorCount -= idsToDelete.size();
        int testMultipleDeletedVectorCount = vectorCount;

        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = dataPlaneClient.describeIndexStats(nullFilterStruct).get();
            // Verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), testMultipleDeletedVectorCount);
        });
    }

    @Test
    public void upsertVectorsAndDeleteByFilterFutureTest() throws InterruptedException, ExecutionException {
        int numOfVectors = 3;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        PineconeFutureDataPlaneClient dataPlaneClient = new PineconeFutureDataPlaneClient(futureStub);

        int vectorCount = 0;
        // Upsert vectors with required + optional and custom metadata parameters
        for (int i=0; i<upsertIds.size(); i++) {
            UpsertResponse upsertResponse = dataPlaneClient.upsert(upsertIds.get(i),
                    generateVectorValuesByDimension(dimension),
                    generateSparseIndicesByDimension(dimension),
                    generateVectorValuesByDimension(dimension),
                    generateMetadataStruct(i, i),
                    namespace).get();
            vectorCount += upsertResponse.getUpsertedCount();
        }

        int actualVectorCount = vectorCount;
        // Verify the vectors are upserts
        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = dataPlaneClient.describeIndexStats(nullFilterStruct).get();
            // Verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), actualVectorCount);
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
        dataPlaneClient.delete(emptyList, false, namespace, filterStruct).get();

        // Update startVectorCount
        int updatedVectorCount = actualVectorCount - 1;

        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = dataPlaneClient.describeIndexStats(nullFilterStruct).get();
            // Verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), updatedVectorCount);
        });
    }
}
