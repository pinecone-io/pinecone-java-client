package io.pinecone.integration.dataPlane;

import com.google.protobuf.Struct;
import io.pinecone.clients.AsyncIndex;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.DescribeIndexStatsResponse;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.IndexModelSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.pinecone.commons.IndexInterface.buildUpsertVectorWithUnsignedIndices;
import static io.pinecone.helpers.AssertRetry.assertWithRetry;
import static io.pinecone.helpers.BuildUpsertRequest.*;
import static io.pinecone.helpers.IndexManager.*;
import static org.junit.jupiter.api.Assertions.*;

public class UpsertAndQueryServerlessTest {

    private static Index index;
    private static AsyncIndex asyncIndex;

    @BeforeAll
    public static void setUp() throws InterruptedException {
        int dimension = 3;
        String apiKey = System.getenv("PINECONE_API_KEY");
        String indexType = IndexModelSpec.SERIALIZED_NAME_SERVERLESS;
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
    public void upsertOptionalVectorsAndQueryIndexSyncTest() throws Exception {
        int numOfVectors = 5;
        int dimension = 3;
        Struct emptyFilterStruct = Struct.newBuilder().build();
        DescribeIndexStatsResponse describeIndexStatsResponse1 = index.describeIndexStats(emptyFilterStruct);
        // Confirm the starting state by verifying the dimension of the index
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);

        // upsert vectors with required + optional parameters
        List<String> upsertIds = getIdsList(numOfVectors);
        int topK = 5;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<Float> values = generateVectorValuesByDimension(dimension);
        List<Long> sparseIndices = generateSparseIndicesByDimension(dimension);
        List<Float> sparseValues = generateVectorValuesByDimension(dimension);
        Struct metadataStruct = generateMetadataStruct();

        List<VectorWithUnsignedIndices> vectors = new ArrayList<VectorWithUnsignedIndices>(numOfVectors);

        for (String id : upsertIds) {
            vectors.add(buildUpsertVectorWithUnsignedIndices(id, values, sparseIndices, sparseValues, metadataStruct));
        }
        index.upsert(vectors, namespace);

        // wait sometime for the vectors to be upserted
        Thread.sleep(90000);

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
                for (int i = 0; i < topK; i++) {
                    if (upsertIds.get(0).equals(queryResponse.getMatches(i).getId())) {
                        scoredVectorV1 = queryResponse.getMatches(i);
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
        }, 4);
    }

    @Test
    public void upsertNullSparseIndicesNotNullSparseValuesSyncTest() {
        String id = RandomStringBuilder.build(3);
        int dimension = 3;

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
        int dimension = 3;
        Struct emptyFilterStruct = Struct.newBuilder().build();

        DescribeIndexStatsResponse describeIndexStatsResponse1 = asyncIndex.describeIndexStats(emptyFilterStruct).get();
        // Confirm the starting state by verifying the dimension of the index
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);

        // upsert vectors with required + optional parameters
        List<String> upsertIds = getIdsList(numOfVectors);
        int topK = 5;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<Float> values = generateVectorValuesByDimension(dimension);
        List<Long> sparseIndices = generateSparseIndicesByDimension(dimension);
        List<Float> sparseValues = generateVectorValuesByDimension(dimension);
        Struct metadataStruct = generateMetadataStruct();
        List<VectorWithUnsignedIndices> vectors = new ArrayList<VectorWithUnsignedIndices>(numOfVectors);

        for (String id : upsertIds) {
            vectors.add(buildUpsertVectorWithUnsignedIndices(id, values, sparseIndices, sparseValues, metadataStruct));
        }
        asyncIndex.upsert(vectors, namespace).get();

        // wait sometime for the vectors to be upserted
        Thread.sleep(90000);

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
                for (int i = 0; i < topK; i++) {
                    if (upsertIds.get(0).equals(queryResponse.getMatches(i).getId())) {
                        scoredVectorV1 = queryResponse.getMatches(i);
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
        }, 4);
    }

    @Test
    public void upsertNullSparseIndicesNotNullSparseValuesFutureTest() throws ExecutionException, InterruptedException {
        String id = RandomStringBuilder.build(3);
        int dimension = 3;

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
