package io.pinecone.integration.dataPlane;

import com.google.protobuf.Struct;
import io.pinecone.clients.AsyncIndex;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.helpers.TestIndexResourcesManager;
import io.pinecone.proto.DescribeIndexStatsResponse;
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
    private static final TestIndexResourcesManager indexManager = TestIndexResourcesManager.getInstance();
    private static Index index;
    private static AsyncIndex asyncIndex;
    private static int dimension;

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException {
        Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();

        String indexName = indexManager.getServerlessIndexName();
        dimension = indexManager.getDimension();
        index = pinecone.getIndexConnection(indexName);
        asyncIndex = pinecone.getAsyncIndexConnection(indexName);
    }

    @AfterAll
    public static void cleanUp() {
        index.close();
        asyncIndex.close();
    }

    @Test
    public void upsertVectorsAndDeleteByIdSyncTest() throws InterruptedException {
        // Upsert vectors with required parameters
        Struct emptyFilterStruct = null;
        int numOfVectors = 3;

        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        for (String id : upsertIds) {
            index.upsert(id,
                    generateVectorValuesByDimension(dimension),
                    namespace);
        }

        int actualVectorCount = numOfVectors;

        assertWithRetry(() -> {
            // call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = index.describeIndexStats();

            // verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), actualVectorCount);
        });

        // Delete 1 vector
        List<String> idsToDelete = new ArrayList<>(3);
        String vectorIdToDelete = upsertIds.get(0);
        idsToDelete.add(vectorIdToDelete);
        index.delete(idsToDelete, false, namespace, emptyFilterStruct);
        numOfVectors -= idsToDelete.size();
        int testSingleDeletedVectorCount = numOfVectors;

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
        numOfVectors -= idsToDelete.size();
        int testMultipleDeletedVectorCount = numOfVectors;

        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = index.describeIndexStats(emptyFilterStruct);
            // Verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), testMultipleDeletedVectorCount);
        }, 4);
    }

    @Test
    public void upsertVectorsAndDeleteByIdFutureTest() throws InterruptedException, ExecutionException {
        // Upsert vectors with required parameters
        Struct emptyFilterStruct = null;
        int numOfVectors = 3;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        for (String id : upsertIds) {
            asyncIndex.upsert(id,
                    generateVectorValuesByDimension(dimension),
                    namespace).get();
        }

        int actualVectorCount = numOfVectors;

        assertWithRetry(() -> {
            // call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = asyncIndex.describeIndexStats().get();

            // verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), actualVectorCount);
        });

        // Delete 1 vector
        List<String> idsToDelete = new ArrayList<>(3);
        String vectorIdToDelete = upsertIds.get(0);
        idsToDelete.add(vectorIdToDelete);
        asyncIndex.delete(idsToDelete, false, namespace, emptyFilterStruct);
        numOfVectors -= idsToDelete.size();
        int testSingleDeletedVectorCount = numOfVectors;

        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = asyncIndex.describeIndexStats().get();
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
        numOfVectors -= idsToDelete.size();
        int testMultipleDeletedVectorCount = numOfVectors;

        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = asyncIndex.describeIndexStats().get();
            // Verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), testMultipleDeletedVectorCount);
        }, 4);
    }
}
