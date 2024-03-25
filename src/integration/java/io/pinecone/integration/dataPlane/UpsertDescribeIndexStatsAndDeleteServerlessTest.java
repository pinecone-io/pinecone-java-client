package io.pinecone.integration.dataPlane;

import com.google.protobuf.Struct;
import io.pinecone.clients.AsyncIndex;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.DescribeIndexStatsResponse;
import io.pinecone.proto.UpsertResponse;
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
import static io.pinecone.helpers.IndexManager.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UpsertDescribeIndexStatsAndDeleteServerlessTest {

    private static Index index;
    private static AsyncIndex asyncIndex;

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException {
        String apiKey = System.getenv("PINECONE_API_KEY");
        String indexType = IndexModelSpec.SERIALIZED_NAME_SERVERLESS;
        int dimension = 3;
        Pinecone pinecone = new Pinecone.Builder(apiKey).build();

        String indexName = findIndexWithDimensionAndType(pinecone, dimension, indexType);
        if (indexName.isEmpty()) indexName = createNewIndex(pinecone, dimension, indexType, true);
        index = pinecone.createIndexConnection(indexName);
        asyncIndex = pinecone.createAsyncIndexConnection(indexName);
    }

    @AfterAll
    public static void cleanUp() {
        index.close();
        asyncIndex.close();
    }

    @Test
    public void upsertVectorsAndDeleteByIdSyncTest() throws InterruptedException {
        // Upsert vectors with required parameters
        int dimension = 3;
        Struct emptyFilterStruct = null;
        int numOfVectors = 3;

        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        int vectorCount = 0;
        for (String id : upsertIds) {
            UpsertResponse upsertResponse = index.upsert(id,
                    generateVectorValuesByDimension(dimension),
                    namespace);
            vectorCount += upsertResponse.getUpsertedCount();
        }

        int actualVectorCount = vectorCount;

        assertWithRetry(() -> {
            // call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = index.describeIndexStats(null);

            // verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), actualVectorCount);
        });

        // Delete 1 vector
        List<String> idsToDelete = new ArrayList<>(3);
        idsToDelete.add(upsertIds.get(0));
        index.delete(idsToDelete, false, namespace, emptyFilterStruct);
        vectorCount -= idsToDelete.size();
        int testSingleDeletedVectorCount = vectorCount;

        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = index.describeIndexStats(emptyFilterStruct);
            // Verify the updated vector count should be 1 less than previous vector count since number of vectors deleted = 1
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), testSingleDeletedVectorCount);
        });

        // vector id at index 0 is already deleted
        idsToDelete.remove(0);
        // Add ids to delete multiple vectors
        idsToDelete.add(upsertIds.get(1));
        idsToDelete.add(upsertIds.get(2));

        // Delete multiple vectors
        index.delete(idsToDelete, false, namespace, null);

        // Update startVectorCount
        vectorCount -= idsToDelete.size();
        int testMultipleDeletedVectorCount = vectorCount;

        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = index.describeIndexStats(emptyFilterStruct);
            // Verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), testMultipleDeletedVectorCount);
        });
    }

    @Test
    public void upsertVectorsAndDeleteByIdFutureTest() throws InterruptedException, ExecutionException {
        // Upsert vectors with required parameters
        int dimension = 3;
        Struct emptyFilterStruct = null;
        int numOfVectors = 3;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        int vectorCount = 0;
        for (String id : upsertIds) {
            UpsertResponse upsertResponse = asyncIndex.upsert(id,
                    generateVectorValuesByDimension(dimension),
                    namespace).get();
            vectorCount += upsertResponse.getUpsertedCount();
        }

        int actualVectorCount = vectorCount;

        assertWithRetry(() -> {
            // call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = asyncIndex.describeIndexStats(null).get();

            // verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), actualVectorCount);
        });

        // Delete 1 vector
        List<String> idsToDelete = new ArrayList<>(3);
        idsToDelete.add(upsertIds.get(0));
        asyncIndex.delete(idsToDelete, false, namespace, emptyFilterStruct);
        vectorCount -= idsToDelete.size();
        int testSingleDeletedVectorCount = vectorCount;

        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = asyncIndex.describeIndexStats(emptyFilterStruct).get();
            // Verify the updated vector count should be 1 less than previous vector count since number of vectors deleted = 1
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), testSingleDeletedVectorCount);
        });

        // vector id at index 0 is already deleted
        idsToDelete.remove(0);
        // Add ids to delete multiple vectors
        idsToDelete.add(upsertIds.get(1));
        idsToDelete.add(upsertIds.get(2));

        // Delete multiple vectors
        asyncIndex.delete(idsToDelete, false, namespace, null);

        // Update startVectorCount
        vectorCount -= idsToDelete.size();
        int testMultipleDeletedVectorCount = vectorCount;

        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = asyncIndex.describeIndexStats(emptyFilterStruct).get();
            // Verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), testMultipleDeletedVectorCount);
        });
    }
}
