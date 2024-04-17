package io.pinecone.integration.dataPlane;

import com.google.protobuf.Struct;
import io.pinecone.clients.AsyncIndex;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.helpers.TestIndexResourcesManager;
import io.pinecone.proto.DescribeIndexStatsResponse;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.pinecone.commons.IndexInterface.buildUpsertVectorWithUnsignedIndices;
import static io.pinecone.helpers.AssertRetry.assertWithRetry;
import static io.pinecone.helpers.BuildUpsertRequest.*;
import static org.junit.jupiter.api.Assertions.*;

public class UpsertAndQueryServerlessTest {
    private static final TestIndexResourcesManager indexManager = TestIndexResourcesManager.getInstance();
    private static Index index;
    private static AsyncIndex asyncIndex;
    private static int dimension;
    private static final String namespace = RandomStringBuilder.build("ns", 8);

    @BeforeAll
    public static void setUp() throws InterruptedException {
        Pinecone pineconeClient = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();

        String indexName = indexManager.getServerlessIndexName();
        dimension = indexManager.getDimension();
        index = pineconeClient.getIndexConnection(indexName);
        asyncIndex = pineconeClient.getAsyncIndexConnection(indexName);
    }

    @AfterAll
    public static void cleanUp() {
        index.close();
        asyncIndex.close();
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
    public void upsertNullSparseIndicesNotNullSparseValuesSyncTest() {
        String id = RandomStringBuilder.build(3);

        try {
            index.upsert(id,
                    generateVectorValuesByDimension(dimension),
                    null,
                    generateVectorValuesByDimension(dimension),
                    null,
                    null);

            fail("Expected to throw PineconeValidationException");
        } catch (PineconeValidationException expected) {
            assertTrue(expected.getLocalizedMessage().contains("ensure that both sparse indices and values are present"));
        }
    }

    @Test
    public void upsertOptionalVectorsAndQueryIndexFutureTest() throws InterruptedException, ExecutionException {
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

    @Test
    public void upsertNullSparseIndicesNotNullSparseValuesFutureTest() throws ExecutionException, InterruptedException {
        String id = RandomStringBuilder.build(3);

        try {
            asyncIndex.upsert(id,
                    generateVectorValuesByDimension(dimension),
                    null,
                    generateVectorValuesByDimension(dimension),
                    null,
                    null).get();

            fail("Expected to throw PineconeValidationException");
        } catch (PineconeValidationException expected) {
            assertTrue(expected.getLocalizedMessage().contains("ensure that both sparse indices and values are present"));
        }
    }
}
