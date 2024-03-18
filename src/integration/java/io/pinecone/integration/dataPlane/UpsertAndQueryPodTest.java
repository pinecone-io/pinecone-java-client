package io.pinecone.integration.dataPlane;

import com.google.protobuf.Struct;
import io.pinecone.clients.Index;
import io.pinecone.clients.AsyncIndex;
import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.*;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;
import org.junit.jupiter.api.*;
import org.openapitools.client.model.IndexModelSpec;

import static io.pinecone.helpers.BuildUpsertRequest.*;
import static io.pinecone.helpers.IndexManager.createIndexIfNotExistsDataPlane;
import static io.pinecone.helpers.AssertRetry.assertWithRetry;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class UpsertAndQueryPodTest {
    private static Pinecone pineconeClient;
    private static String indexName;
    private static Index indexClient;
    private static AsyncIndex asyncIndexClient;
    private static final int dimension = 3;
    private static final Struct emptyFilterStruct = Struct.newBuilder().build();

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException {
        AbstractMap.SimpleEntry<String, Pinecone> indexAndClient = createIndexIfNotExistsDataPlane(dimension, IndexModelSpec.SERIALIZED_NAME_POD);
        indexName = indexAndClient.getKey();
        pineconeClient = indexAndClient.getValue();
        indexClient = pineconeClient.createIndexConnection(indexName);
        asyncIndexClient = pineconeClient.createAsyncIndexConnection(indexName);
    }

    @Test
    public void upsertOptionalVectorsAndQueryIndexSyncTest() throws InterruptedException {
        int numOfVectors = 5;
        DescribeIndexStatsResponse describeIndexStatsResponse1 = indexClient.describeIndexStats(emptyFilterStruct);
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
        for (String id : upsertIds) {
            UpsertResponse upsertResponse = indexClient.upsert(id,
                    values,
                    sparseIndices,
                    sparseValues,
                    metadataStruct,
                    namespace);
        }

        // Query by vector to verify
        assertWithRetry(() -> {
            QueryResponseWithUnsignedIndices queryResponse = indexClient.query(
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
            for (int i = 0; i < topK; i++) {
                if (upsertIds.get(0).equals(queryResponse.getMatches(i).getId())) {
                    scoredVectorV1 = queryResponse.getMatches(i);
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
            assertEquals(scoredVectorV1.getSparseValuesWithUnsignedIndices().getIndicesWithUnsigned32IntList(), sparseIndices);

            // Verify the initial sparse values set for upsert operation
            assertEquals(scoredVectorV1.getSparseValuesWithUnsignedIndices().getValuesList(), sparseValues);
        });
    }

    @Test
    public void upsertNullSparseIndicesNotNullSparseValuesSyncTest() {
        String id = RandomStringBuilder.build(3);

        try {
            indexClient.upsert(id,
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
        DescribeIndexStatsResponse describeIndexStatsResponse1 = asyncIndexClient.describeIndexStats(emptyFilterStruct).get();
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
        for (String id : upsertIds) {
            UpsertResponse upsertResponse = asyncIndexClient.upsert(id,
                    values,
                    sparseIndices,
                    sparseValues,
                    metadataStruct,
                    namespace).get();
        }

        // Query by vector to verify
        assertWithRetry(() -> {
            QueryResponseWithUnsignedIndices queryResponse = asyncIndexClient.query(
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
            if(queryResponse.getMatchesList().size() == upsertIds.size()) {
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
            assertEquals(scoredVectorV1.getSparseValuesWithUnsignedIndices().getIndicesWithUnsigned32IntList(), sparseIndices);

            // Verify the initial sparse values set for upsert operation
            assertEquals(scoredVectorV1.getSparseValuesWithUnsignedIndices().getValuesList(), sparseValues);
        });
    }

    @Test
    public void upsertNullSparseIndicesNotNullSparseValuesFutureTest() throws ExecutionException, InterruptedException {
        String id = RandomStringBuilder.build(3);
        try {
            asyncIndexClient.upsert(id,
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
