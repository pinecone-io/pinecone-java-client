package io.pinecone.integration.dataplane;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.pinecone.PineconeConnection;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.IndexModelSpec;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.pinecone.helpers.BuildUpsertRequest.*;
import static io.pinecone.helpers.IndexManager.createIndexIfNotExistsDataPlane;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UpsertAndDeleteTest {
    private static VectorServiceGrpc.VectorServiceBlockingStub blockingStub;
    private static VectorServiceGrpc.VectorServiceFutureStub futureStub;
    private static final int dimension = 3;

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException {
        PineconeConnection connection = createIndexIfNotExistsDataPlane(dimension, IndexModelSpec.SERIALIZED_NAME_POD);
        blockingStub = connection.getBlockingStub();
        futureStub = connection.getFutureStub();
    }

    @Test
    public void UpsertVectorsAndDeleteByIdSyncTest() throws InterruptedException {
        // Upsert vectors with required parameters
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
        UpsertResponse upsertResponse = blockingStub.upsert(buildRequiredUpsertRequest(upsertIds, namespace));
        Thread.sleep(3500);

        // Get vector count before deleting vectors
        DescribeIndexStatsRequest describeIndexRequest = DescribeIndexStatsRequest.newBuilder().build();
        DescribeIndexStatsResponse describeIndexStatsResponse = blockingStub.describeIndexStats(describeIndexRequest);
        int startVectorCount = describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount();
        assertEquals(startVectorCount, upsertResponse.getUpsertedCount());

        // Delete 1 vector
        String[] idsToDelete = new String[]{upsertIds.get(0)};
        DeleteRequest deleteRequest = DeleteRequest.newBuilder()
                .setNamespace(namespace)
                .addAllIds(Arrays.asList(idsToDelete))
                .setDeleteAll(false)
                .build();
        blockingStub.delete(deleteRequest);
        Thread.sleep(3500);

        // Call describeIndexStats to get updated counts
        describeIndexStatsResponse = blockingStub.describeIndexStats(describeIndexRequest);
        // Verify updated vector count
        assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), startVectorCount - idsToDelete.length);
        startVectorCount -= idsToDelete.length;

        // Delete multiple vectors
        idsToDelete = new String[]{upsertIds.get(1), upsertIds.get(2)};
        deleteRequest = DeleteRequest.newBuilder()
                .setNamespace(namespace)
                .addAllIds(Arrays.asList(idsToDelete))
                .setDeleteAll(false)
                .build();
        blockingStub.delete(deleteRequest);
        Thread.sleep(3500);

        // Call describeIndexStats to get updated counts
        describeIndexStatsResponse = blockingStub.describeIndexStats(describeIndexRequest);
        // Verify the updated vector count
        assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), startVectorCount - idsToDelete.length);
    }

    @Test
    public void UpsertVectorsAndDeleteByNamespaceSyncTest() throws InterruptedException {
        // Upsert vectors with optional parameters
        String namespace = RandomStringBuilder.build("ns", 8);
        UpsertResponse upsertResponse = blockingStub.upsert(buildOptionalUpsertRequest(namespace));
        Thread.sleep(3500);
        // Get vector count before deleting vectors
        DescribeIndexStatsRequest describeIndexRequest = DescribeIndexStatsRequest.newBuilder().build();
        DescribeIndexStatsResponse describeIndexStatsResponse = blockingStub.describeIndexStats(describeIndexRequest);
        int startVectorCount = describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount();
        assertEquals(startVectorCount, upsertResponse.getUpsertedCount());

        // Delete all vectors in the namespace
        DeleteRequest deleteRequest = DeleteRequest.newBuilder()
                .setNamespace(namespace)
                .setDeleteAll(true)
                .build();
        blockingStub.delete(deleteRequest);
        Thread.sleep(3500);

        // Call describeIndexStats to get updated counts
        NamespaceSummary namespaceSummary = blockingStub.describeIndexStats(describeIndexRequest).getNamespacesMap().get(namespace);
        // Verify the namespace is deleted
        assertThrows(NullPointerException.class, () -> namespaceSummary.getVectorCount());
    }

    @Test
    public void UpsertVectorsAndDeleteByFilterSyncTest() throws InterruptedException {
        // Upsert vectors with optional and custom metadata parameters
        String namespace = RandomStringBuilder.build("ns", 8);
        UpsertResponse upsertResponse = blockingStub.upsert(buildOptionalUpsertRequest(namespace));
        Thread.sleep(3500);
        String fieldToDelete = metadataFields[0];
        String valueToDelete = createAndGetMetadataMap().get(fieldToDelete).get(0);

        // Get vector count before deleting vectors
        DescribeIndexStatsRequest describeIndexRequest = DescribeIndexStatsRequest.newBuilder().build();
        DescribeIndexStatsResponse describeIndexStatsResponse = blockingStub.describeIndexStats(describeIndexRequest);
        int startVectorCount = describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount();
        assertEquals(upsertResponse.getUpsertedCount(), startVectorCount);

        // Delete by filtering
        DeleteRequest deleteRequest = DeleteRequest.newBuilder()
                .setNamespace(namespace)
                .setDeleteAll(false)
                .setFilter(Struct.newBuilder()
                        .putFields(fieldToDelete, Value.newBuilder()
                                .setStructValue(Struct.newBuilder()
                                        .putFields("$eq", Value.newBuilder()
                                                .setStringValue(valueToDelete)
                                                .build()))
                                .build())
                        .build())
                .build();
        blockingStub.delete(deleteRequest);
        Thread.sleep(3500);

        // Call describeIndexStats to get updated counts
        NamespaceSummary namespaceSummary = blockingStub.describeIndexStats(describeIndexRequest).getNamespacesMap().get(namespace);
        // Verify the updated vector count
        assertEquals(namespaceSummary.getVectorCount(), upsertResponse.getUpsertedCount() - 1);
    }

    @Test
    public void UpsertVectorsAndDeleteByIdFutureTest() throws ExecutionException, InterruptedException {
        // Upsert vectors with required parameters
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
        UpsertResponse upsertResponse = futureStub.upsert(buildRequiredUpsertRequest(upsertIds, namespace)).get();
        Thread.sleep(3500);

        // Get vector count before deleting vectors
        DescribeIndexStatsRequest describeIndexRequest = DescribeIndexStatsRequest.newBuilder().build();
        DescribeIndexStatsResponse describeIndexStatsResponse = blockingStub.describeIndexStats(describeIndexRequest);
        int startVectorCount = describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount();
        assertEquals(startVectorCount, upsertResponse.getUpsertedCount());

        // Delete 1 vector
        String[] idsToDelete = new String[]{upsertIds.get(0)};
        DeleteRequest deleteRequest = DeleteRequest.newBuilder()
                .setNamespace(namespace)
                .addAllIds(Arrays.asList(idsToDelete))
                .setDeleteAll(false)
                .build();
        blockingStub.delete(deleteRequest);
        Thread.sleep(3500);

        // Call describeIndexStats to get updated counts
        describeIndexStatsResponse = futureStub.describeIndexStats(describeIndexRequest).get();
        assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), startVectorCount - idsToDelete.length);
        startVectorCount -= idsToDelete.length;

        // Delete multiple vectors
        idsToDelete = new String[]{upsertIds.get(1), upsertIds.get(2)};
        deleteRequest = DeleteRequest.newBuilder()
                .setNamespace(namespace)
                .addAllIds(Arrays.asList(idsToDelete))
                .setDeleteAll(false)
                .build();
        blockingStub.delete(deleteRequest);
        Thread.sleep(3500);

        // Call describeIndexStats to get updated counts
        describeIndexStatsResponse = futureStub.describeIndexStats(describeIndexRequest).get();
        // Verify the updated vector count
        assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), startVectorCount - idsToDelete.length);
    }

    @Test
    public void UpsertVectorsAndDeleteByNamespaceFutureTest() throws ExecutionException, InterruptedException {
        // Upsert vectors with optional parameters
        String namespace = RandomStringBuilder.build("ns", 8);
        UpsertResponse upsertResponse = futureStub.upsert(buildOptionalUpsertRequest(namespace)).get();
        Thread.sleep(3500);

        // Get vector count before deleting vectors
        DescribeIndexStatsRequest describeIndexRequest = DescribeIndexStatsRequest.newBuilder().build();
        DescribeIndexStatsResponse describeIndexStatsResponse = futureStub.describeIndexStats(describeIndexRequest).get();
        int startVectorCount = describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount();
        assertEquals(startVectorCount, upsertResponse.getUpsertedCount());

        // Delete all vectors in the namespace
        DeleteRequest deleteRequest = DeleteRequest.newBuilder()
                .setNamespace(namespace)
                .setDeleteAll(true)
                .build();
        futureStub.delete(deleteRequest);
        Thread.sleep(3500);

        // Call describeIndexStats to get updated counts
        NamespaceSummary namespaceSummary = futureStub.describeIndexStats(describeIndexRequest).get().getNamespacesMap().get(namespace);
        // Verify the namespace is deleted
        assertThrows(NullPointerException.class, () -> namespaceSummary.getVectorCount());
    }

    @Test
    public void UpsertVectorsAndDeleteByFilterFutureTest() throws InterruptedException, ExecutionException {
        // Upsert vectors with optional and custom metadata parameters
        String namespace = RandomStringBuilder.build("ns", 8);
        String fieldToDelete = metadataFields[0];
        String valueToDelete = createAndGetMetadataMap().get(fieldToDelete).get(0);
        UpsertResponse upsertResponse = futureStub.upsert(buildOptionalUpsertRequest(namespace)).get();
        Thread.sleep(3500);

        // Get vector count before deleting vectors
        DescribeIndexStatsRequest describeIndexRequest = DescribeIndexStatsRequest.newBuilder().build();
        DescribeIndexStatsResponse describeIndexStatsResponse = futureStub.describeIndexStats(describeIndexRequest).get();
        int startVectorCount = describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount();
        assertEquals(upsertResponse.getUpsertedCount(), startVectorCount);

        // Delete by filtering
        DeleteRequest deleteRequest = DeleteRequest.newBuilder()
                .setNamespace(namespace)
                .setDeleteAll(false)
                .setFilter(Struct.newBuilder()
                        .putFields(metadataFields[0], Value.newBuilder()
                                .setStructValue(Struct.newBuilder()
                                        .putFields("$eq", Value.newBuilder()
                                                .setStringValue(valueToDelete)
                                                .build()))
                                .build())
                        .build())
                .build();
        futureStub.delete(deleteRequest);
        Thread.sleep(3500);

        // Call describeIndexStats to get updated counts
        NamespaceSummary namespaceSummary = futureStub.describeIndexStats(describeIndexRequest).get().getNamespacesMap().get(namespace);
        // Verify the updated vector count
        assertEquals(namespaceSummary.getVectorCount(), upsertResponse.getUpsertedCount() - 1);
    }
}
