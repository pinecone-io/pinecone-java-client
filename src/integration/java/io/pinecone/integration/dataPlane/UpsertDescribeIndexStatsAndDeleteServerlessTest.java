package io.pinecone.integration.dataPlane;

import com.google.protobuf.Struct;
import io.pinecone.clients.AsyncIndex;
import io.pinecone.clients.Index;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.helpers.TestResourcesManager;
import io.pinecone.proto.DescribeIndexStatsResponse;
import io.pinecone.proto.NamespaceSummary;
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

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException {
        dimension = indexManager.getDimension();
        index = indexManager.getOrCreateServerlessIndexConnection();
        asyncIndex = indexManager.getOrCreateServerlessAsyncIndexConnection();
    }

    @AfterAll
    public static void cleanUp() {
        index.close();
        asyncIndex.close();
    }

    @Test
    public void upsertVectorsAndDeleteByIdSyncTest() throws Exception {
        String namespace = RandomStringBuilder.build("ns", 8);
        int numOfVectors = 3;
        List<String> upsertIds = getIdsList(numOfVectors);
        List<VectorWithUnsignedIndices> vectorsToUpsert = new ArrayList<>(numOfVectors);

        for (String id : upsertIds) {
            vectorsToUpsert.add(new VectorWithUnsignedIndices(id, generateVectorValuesByDimension(dimension)));
        }

        index.upsert(vectorsToUpsert, namespace);

        assertWithRetry(() -> {
            DescribeIndexStatsResponse response = index.describeIndexStats();
            NamespaceSummary ns = response.getNamespacesMap().get(namespace);
            assertEquals(numOfVectors, ns == null ? 0 : ns.getVectorCount());
        }, 3);

        index.deleteByIds(upsertIds, namespace);

        assertWithRetry(() -> {
            DescribeIndexStatsResponse response = index.describeIndexStats();
            NamespaceSummary ns = response.getNamespacesMap().get(namespace);
            assertEquals(0, ns == null ? 0 : ns.getVectorCount());
        }, 3);
    }

    @Test
    public void upsertVectorsAndDeleteByIdFutureTest() throws Exception {
        String namespace = RandomStringBuilder.build("ns", 8);
        int numOfVectors = 3;
        List<String> upsertIds = getIdsList(numOfVectors);
        List<VectorWithUnsignedIndices> vectorsToUpsert = new ArrayList<>(numOfVectors);

        for (String id : upsertIds) {
            vectorsToUpsert.add(new VectorWithUnsignedIndices(id, generateVectorValuesByDimension(dimension)));
        }

        asyncIndex.upsert(vectorsToUpsert, namespace).get();

        assertWithRetry(() -> {
            DescribeIndexStatsResponse response = asyncIndex.describeIndexStats().get();
            NamespaceSummary ns = response.getNamespacesMap().get(namespace);
            assertEquals(numOfVectors, ns == null ? 0 : ns.getVectorCount());
        }, 3);

        asyncIndex.deleteByIds(upsertIds, namespace);

        assertWithRetry(() -> {
            DescribeIndexStatsResponse response = asyncIndex.describeIndexStats().get();
            NamespaceSummary ns = response.getNamespacesMap().get(namespace);
            assertEquals(0, ns == null ? 0 : ns.getVectorCount());
        }, 3);
    }
}
