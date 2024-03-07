package io.pinecone.integration.dataPlane.serverless;

import com.google.protobuf.Struct;
import io.pinecone.clients.PineconeBlockingDataPlaneClient;
import io.pinecone.clients.PineconeFutureDataPlaneClient;
import io.pinecone.configs.PineconeConnection;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.DescribeIndexStatsResponse;
import io.pinecone.proto.UpsertResponse;
import io.pinecone.proto.VectorServiceGrpc;
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
    private static final Struct nullFilterStruct = null;

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException {
        connection = createIndexIfNotExistsDataPlane(dimension, IndexModelSpec.SERIALIZED_NAME_SERVERLESS);
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
}
