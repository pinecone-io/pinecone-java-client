package io.pinecone.integration.dataPlane;

import com.google.protobuf.Struct;
import io.pinecone.clients.AsyncIndex;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.helpers.TestResourcesManager;
import io.pinecone.proto.DescribeIndexStatsResponse;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.pinecone.helpers.AssertRetry.assertWithRetry;
import static io.pinecone.helpers.BuildUpsertRequest.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UpsertDescribeIndexStatsAndDeleteServerlessTest {
    private static final TestResourcesManager indexManager = TestResourcesManager.getInstance();
    private static Index index;
    private static AsyncIndex asyncIndex;
    private static int dimension;
    private static final String namespace = RandomStringBuilder.build("ns", 8);
    private static int namespaceVectorCount = 0;

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException {
        dimension = indexManager.getDimension();
        index = indexManager.getServerlessIndexConnection();
        asyncIndex = indexManager.getServerlessAsyncIndexConnection();
    }

    @AfterAll
    public static void cleanUp() {
        index.close();
        asyncIndex.close();
    }

    @Test
    public void upsertVectorsAndDeleteByIdSyncTest() throws InterruptedException {
        // Upsert vectors with required parameters
        int numOfVectors = 3;

        List<String> upsertIds = getIdsList(numOfVectors);
        List<VectorWithUnsignedIndices> vectorsToUpsert = new ArrayList<>(numOfVectors);

        for (String id : upsertIds) {
            VectorWithUnsignedIndices vector =
            new VectorWithUnsignedIndices(id, generateVectorValuesByDimension(dimension));
            vectorsToUpsert.add(vector);
        }

        index.upsert(vectorsToUpsert, namespace);
        namespaceVectorCount += numOfVectors;

        assertWithRetry(() -> {
            // call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = index.describeIndexStats();

            // verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), namespaceVectorCount);
        }, 3);

        // Delete vectors
        index.deleteByIds(upsertIds, namespace);
        namespaceVectorCount -= numOfVectors;

        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = index.describeIndexStats();
            // Verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), namespaceVectorCount);
        }, 3);
    }

    @Test
    public void upsertVectorsAndDeleteByIdFutureTest() throws InterruptedException, ExecutionException {
        // Upsert vectors with required parameters
        Struct emptyFilterStruct = null;
        int numOfVectors = 3;
        List<String> upsertIds = getIdsList(numOfVectors);
        List<VectorWithUnsignedIndices> vectorsToUpsert = new ArrayList<>(numOfVectors);

        for (String id : upsertIds) {
            VectorWithUnsignedIndices vector =
            new VectorWithUnsignedIndices(id, generateVectorValuesByDimension(dimension));
            vectorsToUpsert.add(vector);
        }

        asyncIndex.upsert(vectorsToUpsert, namespace).get();
        namespaceVectorCount += numOfVectors;

        assertWithRetry(() -> {
            // call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = asyncIndex.describeIndexStats().get();

            // verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), namespaceVectorCount);
        }, 3);

        // Delete vectors
        asyncIndex.deleteByIds(upsertIds, namespace);
        namespaceVectorCount -= numOfVectors;

        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = asyncIndex.describeIndexStats().get();
            // Verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), namespaceVectorCount);
        }, 3);
    }
}
