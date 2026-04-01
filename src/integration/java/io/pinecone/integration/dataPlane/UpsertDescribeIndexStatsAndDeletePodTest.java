package io.pinecone.integration.dataPlane;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.pinecone.clients.Index;
import io.pinecone.clients.AsyncIndex;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.helpers.TestResourcesManager;
import io.pinecone.proto.*;
import io.pinecone.unsigned_indices_model.SparseValuesWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.pinecone.helpers.AssertRetry.assertWithRetry;
import static io.pinecone.helpers.BuildUpsertRequest.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UpsertDescribeIndexStatsAndDeletePodTest {
    private static final TestResourcesManager indexManager = TestResourcesManager.getInstance();
    private static Index index;
    private static AsyncIndex asyncIndex;
    private static int dimension;
    private static final Struct nullFilterStruct = null;

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException {
        dimension = indexManager.getDimension();
        index = indexManager.getOrCreatePodIndexConnection();
        asyncIndex = indexManager.getOrCreatePodAsyncIndexConnection();
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

        index.delete(upsertIds, false, namespace, nullFilterStruct);

        assertWithRetry(() -> {
            DescribeIndexStatsResponse response = index.describeIndexStats(nullFilterStruct);
            NamespaceSummary ns = response.getNamespacesMap().get(namespace);
            assertEquals(0, ns == null ? 0 : ns.getVectorCount());
        }, 3);
    }

    @Test
    public void upsertVectorsAndDeleteByFilterSyncTest() throws Exception {
        String namespace = RandomStringBuilder.build("ns", 8);
        int numOfVectors = 3;
        List<String> upsertIds = getIdsList(numOfVectors);
        List<VectorWithUnsignedIndices> vectorsToUpsert = new ArrayList<>(numOfVectors);

        for (int i = 0; i < upsertIds.size(); i++) {
            vectorsToUpsert.add(new VectorWithUnsignedIndices(
                    upsertIds.get(i),
                    generateVectorValuesByDimension(dimension),
                    generateMetadataStruct(i, i),
                    new SparseValuesWithUnsignedIndices(
                            generateSparseIndicesByDimension(dimension),
                            generateVectorValuesByDimension(dimension)
                    )
            ));
        }

        index.upsert(vectorsToUpsert, namespace);

        assertWithRetry(() -> {
            DescribeIndexStatsResponse response = index.describeIndexStats(nullFilterStruct);
            NamespaceSummary ns = response.getNamespacesMap().get(namespace);
            assertEquals(numOfVectors, ns == null ? 0 : ns.getVectorCount());
        }, 3);

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

        index.delete(null, false, namespace, filterStruct);
        int expectedAfterDelete = numOfVectors - 1;

        assertWithRetry(() -> {
            DescribeIndexStatsResponse response = index.describeIndexStats(nullFilterStruct);
            NamespaceSummary ns = response.getNamespacesMap().get(namespace);
            assertEquals(expectedAfterDelete, ns == null ? 0 : ns.getVectorCount());
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

        asyncIndex.delete(upsertIds, false, namespace, null).get();

        assertWithRetry(() -> {
            DescribeIndexStatsResponse response = asyncIndex.describeIndexStats().get();
            NamespaceSummary ns = response.getNamespacesMap().get(namespace);
            assertEquals(0, ns == null ? 0 : ns.getVectorCount());
        }, 3);
    }

    @Test
    public void upsertVectorsAndDeleteByFilterFutureTest() throws Exception {
        String namespace = RandomStringBuilder.build("ns", 8);
        int numOfVectors = 3;
        List<String> upsertIds = getIdsList(numOfVectors);
        List<VectorWithUnsignedIndices> vectorsToUpsert = new ArrayList<>(numOfVectors);

        for (int i = 0; i < upsertIds.size(); i++) {
            vectorsToUpsert.add(new VectorWithUnsignedIndices(
                    upsertIds.get(i),
                    generateVectorValuesByDimension(dimension),
                    generateMetadataStruct(i, i),
                    new SparseValuesWithUnsignedIndices(
                            generateSparseIndicesByDimension(dimension),
                            generateVectorValuesByDimension(dimension)
                    )
            ));
        }

        asyncIndex.upsert(vectorsToUpsert, namespace).get();

        assertWithRetry(() -> {
            DescribeIndexStatsResponse response = asyncIndex.describeIndexStats(nullFilterStruct).get();
            NamespaceSummary ns = response.getNamespacesMap().get(namespace);
            assertEquals(numOfVectors, ns == null ? 0 : ns.getVectorCount());
        }, 3);

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

        asyncIndex.delete(new ArrayList<>(), false, namespace, filterStruct).get();
        int expectedAfterDelete = numOfVectors - 1;

        assertWithRetry(() -> {
            DescribeIndexStatsResponse response = asyncIndex.describeIndexStats(nullFilterStruct).get();
            NamespaceSummary ns = response.getNamespacesMap().get(namespace);
            assertEquals(expectedAfterDelete, ns == null ? 0 : ns.getVectorCount());
        }, 3);
    }
}
