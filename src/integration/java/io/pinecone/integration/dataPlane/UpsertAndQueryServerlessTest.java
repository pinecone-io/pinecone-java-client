package io.pinecone.integration.dataPlane;

import com.google.protobuf.Struct;
import io.pinecone.clients.AsyncIndex;
import io.pinecone.clients.Index;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.helpers.TestResourcesManager;
import io.pinecone.proto.DescribeIndexStatsResponse;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.pinecone.commons.IndexInterface.buildUpsertVectorWithUnsignedIndices;
import static io.pinecone.helpers.AssertRetry.assertWithRetry;
import static io.pinecone.helpers.BuildUpsertRequest.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("smoke")
public class UpsertAndQueryServerlessTest {
    private static final TestResourcesManager indexManager = TestResourcesManager.getInstance();
    private static Index index;
    private static AsyncIndex asyncIndex;
    private static int dimension;
    private static final String namespace = RandomStringBuilder.build("ns", 8);

    @BeforeAll
    public static void setUp() throws Exception {
        dimension = indexManager.getDimension();
        index = indexManager.getOrCreateServerlessIndexConnection();
        asyncIndex = indexManager.getOrCreateServerlessAsyncIndexConnection();
    }

    @Test
    public void upsertOptionalVectorsAndQueryIndexSyncTest() throws Exception {
        int numOfVectors = 5;
        int topK = 5;

        Struct emptyFilterStruct = Struct.newBuilder().build();
        DescribeIndexStatsResponse describeIndexStatsResponse1 = index.describeIndexStats(emptyFilterStruct);
        // Confirm the starting state by verifying the dimension of the index
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);

        // upsert vectors with required + optional parameters
        List<String> upsertIds = getIdsList(numOfVectors);
        List<Float> values = generateVectorValuesByDimension(dimension);
        List<Long> sparseIndices = generateSparseIndicesByDimension(dimension);
        List<Float> sparseValues = generateVectorValuesByDimension(dimension);
        Struct metadataStruct = generateMetadataStruct();

        List<VectorWithUnsignedIndices> vectorsToUpsert = new ArrayList<>(numOfVectors);

        for (String id : upsertIds) {
            vectorsToUpsert.add(buildUpsertVectorWithUnsignedIndices(id, values, sparseIndices, sparseValues, metadataStruct));
        }

        index.upsert(vectorsToUpsert, namespace);

        // Query by vector to verify
        assertWithRetry(() -> {
            QueryResponseWithUnsignedIndices queryResponse = index.query(
                    topK,
                    values,
                    sparseIndices,
                    sparseValues,
                    null,
                    namespace,
                    null,
                    true,
                    true);

            ScoredVectorWithUnsignedIndices scoredVectorV1 = null;

            // if the sizes are not equal, let the following assertions fail and retry again
            if (queryResponse.getMatchesList().size() == upsertIds.size()) {
                for (ScoredVectorWithUnsignedIndices indexModel : queryResponse.getMatchesList()) {
                    if (upsertIds.get(0).equals(indexModel.getId())) {
                        scoredVectorV1 = indexModel;
                    }
                }
            }

            // Verify the correct vector id was updated
            assertNotNull(scoredVectorV1);
            assertEquals(scoredVectorV1.getId(), upsertIds.get(0));

            // Verify the updated values
            assertEquals(values, scoredVectorV1.getValuesList());

            // Verify the updated metadata
            assertEquals(scoredVectorV1.getMetadata(), metadataStruct);

            // Verify the initial sparse values set for upsert operation
            List<Long> unsignedIndicesList = new ArrayList<>(scoredVectorV1.getSparseValuesWithUnsignedIndices().getIndicesWithUnsigned32IntList());
            Collections.sort(unsignedIndicesList);
            Collections.sort(sparseIndices);
            assertEquals(unsignedIndicesList, sparseIndices);

            // Verify the initial sparse values set for upsert operation
            List<Float> expectedSparseValues = new ArrayList<>(scoredVectorV1.getSparseValuesWithUnsignedIndices().getValuesList());
            Collections.sort(expectedSparseValues);
            Collections.sort(sparseValues);
            assertEquals(expectedSparseValues, sparseValues);
        }, 3);
    }

    @Test
    public void upsertOptionalVectorsAndQueryIndexFutureTest() throws Exception {
        int numOfVectors = 5;
        int topK = 5;

        Struct emptyFilterStruct = Struct.newBuilder().build();

        DescribeIndexStatsResponse describeIndexStatsResponse1 = asyncIndex.describeIndexStats(emptyFilterStruct).get();
        // Confirm the starting state by verifying the dimension of the index
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);

        // upsert vectors with required + optional parameters
        List<String> upsertIds = getIdsList(numOfVectors);
        List<Float> values = generateVectorValuesByDimension(dimension);
        List<Long> sparseIndices = generateSparseIndicesByDimension(dimension);
        List<Float> sparseValues = generateVectorValuesByDimension(dimension);
        Struct metadataStruct = generateMetadataStruct();

        List<VectorWithUnsignedIndices> vectorsToUpsert = new ArrayList<>(numOfVectors);

        for (String id : upsertIds) {
            vectorsToUpsert.add(buildUpsertVectorWithUnsignedIndices(id, values, sparseIndices, sparseValues, metadataStruct));
        }
        asyncIndex.upsert(vectorsToUpsert, namespace).get();

        // Query by vector to verify
        assertWithRetry(() -> {
            QueryResponseWithUnsignedIndices queryResponse = asyncIndex.query(
                    topK,
                    values,
                    sparseIndices,
                    sparseValues,
                    null,
                    namespace,
                    null,
                    true,
                    true).get();

            ScoredVectorWithUnsignedIndices scoredVectorV1 = null;
            // if the sizes are not equal, let the following assertions fail and retry again
            if (queryResponse.getMatchesList().size() == upsertIds.size()) {
                for (ScoredVectorWithUnsignedIndices indexModel : queryResponse.getMatchesList()) {
                    if (upsertIds.get(0).equals(indexModel.getId())) {
                        scoredVectorV1 = indexModel;
                    }
                }
            }

            // Verify the correct vector id was updated
            assertNotNull(scoredVectorV1);
            assertEquals(scoredVectorV1.getId(), upsertIds.get(0));

            // Verify the updated values
            assertEquals(values, scoredVectorV1.getValuesList());

            // Verify the updated metadata
            assertEquals(scoredVectorV1.getMetadata(), metadataStruct);

            // Verify the initial sparse values set for upsert operation
            List<Long> unsignedIndicesList = new ArrayList<>(scoredVectorV1.getSparseValuesWithUnsignedIndices().getIndicesWithUnsigned32IntList());
            Collections.sort(unsignedIndicesList);
            Collections.sort(sparseIndices);
            assertEquals(unsignedIndicesList, sparseIndices);

            // Verify the initial sparse values set for upsert operation
            List<Float> expectedSparseValues = new ArrayList<>(scoredVectorV1.getSparseValuesWithUnsignedIndices().getValuesList());
            Collections.sort(expectedSparseValues);
            Collections.sort(sparseValues);
            assertEquals(expectedSparseValues, sparseValues);
        }, 3);
    }
}
