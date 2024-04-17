package io.pinecone.integration.dataPlane;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.pinecone.clients.Pinecone;
import io.pinecone.clients.Index;
import io.pinecone.clients.AsyncIndex;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.helpers.TestIndexResourcesManager;
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
    private static final TestIndexResourcesManager indexManager = TestIndexResourcesManager.getInstance();
    private static Pinecone pineconeClient = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
    private static Index index;
    private static AsyncIndex asyncIndex;
    private static String indexName;
    private static int dimension;
    private static final Struct nullFilterStruct = null;
    private static final String namespace = RandomStringBuilder.build("ns", 8);
    private static int namespaceVectorCount = 0;

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException {
        indexName = indexManager.getPodIndexName();
        dimension = indexManager.getDimension();
        index = pineconeClient.getIndexConnection(indexName);
        asyncIndex = pineconeClient.getAsyncIndexConnection(indexName);
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
        index.delete(upsertIds, false, namespace, nullFilterStruct);
        namespaceVectorCount -= numOfVectors;

        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = index.describeIndexStats(nullFilterStruct);
            // Verify the vectors have been deleted
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), namespaceVectorCount);
        }, 3);
    }

    @Test
    public void upsertVectorsAndDeleteByFilterSyncTest() throws InterruptedException {
        int numOfVectors = 3;
        List<String> upsertIds = getIdsList(numOfVectors);
        List<VectorWithUnsignedIndices> vectorsToUpsert = new ArrayList<>(numOfVectors);

        // Upsert vectors with required + optional and custom metadata parameters
        for (int i=0; i<upsertIds.size(); i++) {
            VectorWithUnsignedIndices vector =
            new VectorWithUnsignedIndices(upsertIds.get(i),
                    generateVectorValuesByDimension(dimension),
                    generateMetadataStruct(i, i),
                    new SparseValuesWithUnsignedIndices(generateSparseIndicesByDimension(dimension),
                            generateVectorValuesByDimension(dimension)
                    )
            );
            vectorsToUpsert.add(vector);
        }

        index.upsert(vectorsToUpsert, namespace);
        namespaceVectorCount += numOfVectors;

        // Verify the vectors are upserted
        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = index.describeIndexStats(nullFilterStruct);
            // Verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), namespaceVectorCount);
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

        // Delete by filtering
        index.delete(null, false, namespace, filterStruct);
        namespaceVectorCount = namespaceVectorCount - 1;

        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = index.describeIndexStats(nullFilterStruct);
            // Verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), namespaceVectorCount);
        }, 3);
    }

    @Test
    public void upsertVectorsAndDeleteByIdFutureTest() throws InterruptedException, ExecutionException {
        // Upsert vectors with required parameters
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
        asyncIndex.delete(upsertIds, false, namespace, null).get();
        namespaceVectorCount -= numOfVectors;

        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = asyncIndex.describeIndexStats().get();
            // Verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), namespaceVectorCount);
        }, 3);
    }

    @Test
    public void upsertVectorsAndDeleteByFilterFutureTest() throws InterruptedException, ExecutionException {
        int numOfVectors = 3;
        List<String> upsertIds = getIdsList(numOfVectors);
        List<VectorWithUnsignedIndices> vectorsToUpsert = new ArrayList<>(numOfVectors);

        // Upsert vectors with required + optional and custom metadata parameters
        for (int i=0; i<upsertIds.size(); i++) {
            VectorWithUnsignedIndices vector =
            new VectorWithUnsignedIndices(upsertIds.get(i),
                    generateVectorValuesByDimension(dimension),
                    generateMetadataStruct(i, i),
                    new SparseValuesWithUnsignedIndices(generateSparseIndicesByDimension(dimension),
                            generateVectorValuesByDimension(dimension)
                    )
            );
            vectorsToUpsert.add(vector);
        }
        asyncIndex.upsert(vectorsToUpsert, namespace).get();
        namespaceVectorCount += numOfVectors;


        // Verify the vectors are upserts
        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = asyncIndex.describeIndexStats(nullFilterStruct).get();
            // Verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), namespaceVectorCount);
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

        // Delete by filtering
        asyncIndex.delete(new ArrayList<>(), false, namespace, filterStruct).get();
        namespaceVectorCount -= 1;

        assertWithRetry(() -> {
            // Call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse = asyncIndex.describeIndexStats(nullFilterStruct).get();
            // Verify the updated vector count
            assertEquals(describeIndexStatsResponse.getNamespacesMap().get(namespace).getVectorCount(), namespaceVectorCount);
        }, 3);
    }
}
