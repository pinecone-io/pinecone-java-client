package io.pinecone.integration.dataPlane;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.grpc.StatusRuntimeException;
import io.pinecone.clients.Index;
import io.pinecone.clients.AsyncIndex;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.helpers.TestResourcesManager;
import io.pinecone.proto.*;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.SparseValuesWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static io.pinecone.helpers.AssertRetry.assertWithRetry;
import static io.pinecone.helpers.BuildUpsertRequest.*;
import static org.junit.jupiter.api.Assertions.*;

public class  UpdateFetchAndQueryPodTest {
    private static final TestResourcesManager indexManager = TestResourcesManager.getInstance();
    private static Index index;
    private static AsyncIndex asyncIndex;
    private static final String namespace = RandomStringBuilder.build("ns", 8);
    private static List<String> upsertIds;
    private static List<String> sparseUpsertIds;
    private static List<Float> sparseValuesList;
    private static int dimension;

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException {
        dimension = indexManager.getDimension();
        index = indexManager.getOrCreatePodIndexConnection();
        asyncIndex = indexManager.getOrCreatePodAsyncIndexConnection();

        // Upsert vectors only once
        int numOfVectors = 3;
        int numOfSparseVectors = 2;
        upsertIds = getIdsList(numOfVectors);
        sparseUpsertIds = getIdsList(numOfSparseVectors);
        ArrayList<Long> sparseIndices = generateSparseIndicesByDimension(dimension);
        sparseValuesList = generateVectorValuesByDimension(dimension);
        List<VectorWithUnsignedIndices> vectorsToUpsert = new ArrayList<>(numOfVectors + numOfSparseVectors);

        // upsert vectors with optional + required parameters
        for (int i = 0; i < numOfSparseVectors; i++) {
            VectorWithUnsignedIndices vector = new VectorWithUnsignedIndices(sparseUpsertIds.get(i),
                    generateVectorValuesByDimension(dimension),
                    generateMetadataStruct(i, i),
                    new SparseValuesWithUnsignedIndices(sparseIndices, sparseValuesList)
            );
            vectorsToUpsert.add(vector);
        }

        for (int j = 0; j < numOfVectors; j++) {
            VectorWithUnsignedIndices vector =
            new VectorWithUnsignedIndices(upsertIds.get(j), generateVectorValuesByDimension(dimension));
            vectorsToUpsert.add(vector);
        }

        index.upsert(vectorsToUpsert, namespace);
    }

    @AfterAll
    public static void cleanUp() {
        index.close();
        asyncIndex.close();
    }

    @Test
    public void updateAllParamsFetchAndQuerySyncTest() throws InterruptedException {
        // Verify the upserted vector count with fetch
        assertWithRetry(() -> {
            FetchResponse fetchResponse = index.fetch(upsertIds, namespace);
            assertEquals(fetchResponse.getVectorsCount(), upsertIds.size());
            for (String key : upsertIds) {
                assertTrue(fetchResponse.containsVectors(key));
            }
        }, 3);

        String idToUpdate = sparseUpsertIds.get(0);
        List<Float> valuesToUpdate = Arrays.asList(201F, 202F, 203F, 204F);
        HashMap<String, List<String>> metadataMap = createAndGetMetadataMap();
        Struct metadataToUpdate = Struct.newBuilder()
                .putFields(metadataFields[0],
                        Value.newBuilder().setStringValue(metadataMap.get(metadataFields[0]).get(0)).build())
                .putFields(metadataFields[1],
                        Value.newBuilder().setStringValue(metadataMap.get(metadataFields[1]).get(0)).build())
                .build();

        // Update required+optional fields
        index.update(idToUpdate, valuesToUpdate, metadataToUpdate, namespace, null, null);

        // Query by vector to verify
        assertWithRetry(() -> {
            QueryResponseWithUnsignedIndices queryResponse = index.query(
                    5,
                    valuesToUpdate,
                    null,
                    null,
                    null,
                    namespace,
                    null,
                    true,
                    true);
            List<ScoredVectorWithUnsignedIndices> queryResults = queryResponse.getMatchesList();
            ScoredVectorWithUnsignedIndices scoredVectorV1 = null;

            for (ScoredVectorWithUnsignedIndices vector : queryResults) {
                if (idToUpdate.equals(vector.getId())) {
                    scoredVectorV1 = vector;
                }
            }

            // Verify the correct vector id was updated
            assertNotNull(scoredVectorV1);
            assertEquals(scoredVectorV1.getId(), idToUpdate);

            // Verify the updated values
            List<Float> valuesList = scoredVectorV1.getValuesList();
            assertEquals(valuesToUpdate, valuesList);

            // Verify the updated metadata
            assertEquals(scoredVectorV1.getMetadata(), metadataToUpdate);

            // Verify the initial sparse values set for upsert operation
            List<Float> expectedSparseValues = sparseValuesList;
            List<Float> actualSparseValues = scoredVectorV1.getSparseValuesWithUnsignedIndices().getValuesList();
            assertEquals(expectedSparseValues, actualSparseValues);
        }, 3);
    }

    @Test
    public void addIncorrectDimensionalValuesSyncTest() throws InterruptedException {
        // Update required fields only but with incorrect values dimension
        String idToUpdate = upsertIds.get(0);
        List<Float> updatedValues = Arrays.asList(101F);

        try {
            index.update(idToUpdate, updatedValues, namespace);

            fail("Expected to throw statusRuntimeException");
        } catch (StatusRuntimeException statusRuntimeException) {
            assertTrue(statusRuntimeException.toString().contains("Vector dimension 1 does not match the dimension of the index " + dimension));
        }
    }

    @Test
    public void updateNullSparseIndicesNotNullSparseValuesSyncTest() {
        String id = RandomStringBuilder.build(3);

        try {
            index.update(id,
                    generateVectorValuesByDimension(dimension),
                    null,
                    null,
                    null,
                    generateVectorValuesByDimension(dimension));

            fail("Expected to throw PineconeValidationException");
        } catch (PineconeValidationException validationException) {
            assertTrue(validationException.toString().contains("Invalid upsert request. Please ensure that both sparse indices and values are present."));
        }
    }

    @Test
    public void queryWithFiltersSyncTest() throws InterruptedException {
        String fieldToQuery = metadataFields[0];
        String valueToQuery = createAndGetMetadataMap().get(fieldToQuery).get(0);

        Struct filter = Struct.newBuilder()
                .putFields(metadataFields[0], Value.newBuilder()
                        .setStructValue(Struct.newBuilder()
                                .putFields("$eq", Value.newBuilder()
                                        .setStringValue(valueToQuery)
                                        .build()))
                        .build())
                .build();

        assertWithRetry(() -> {
            QueryResponseWithUnsignedIndices queryResponse = index.queryByVectorId(3,
                    upsertIds.get(0),
                    namespace,
                    filter,
                    true,
                    true);

            // Verify the metadata field is correctly filtered in the query response
            assertTrue(queryResponse.getMatches(0).getMetadata().getFieldsMap().get(fieldToQuery).toString().contains(valueToQuery));
        }, 3);
    }

    @Test
    public void updateAllParamsFetchAndQueryFutureTest() throws InterruptedException, ExecutionException {
        // Verify the upserted vector count with fetch
        assertWithRetry(() -> {
            FetchResponse fetchResponse = asyncIndex.fetch(upsertIds, namespace).get();
            assertEquals(fetchResponse.getVectorsCount(), upsertIds.size());
            for (String key : upsertIds) {
                assertTrue(fetchResponse.containsVectors(key));
            }
        }, 3);

        String idToUpdate = sparseUpsertIds.get(0);
        List<Float> valuesToUpdate = Arrays.asList(301F, 302F, 303F, 304F);
        HashMap<String, List<String>> metadataMap = createAndGetMetadataMap();
        Struct metadataToUpdate = Struct.newBuilder()
                .putFields(metadataFields[0],
                        Value.newBuilder().setStringValue(metadataMap.get(metadataFields[0]).get(0)).build())
                .putFields(metadataFields[1],
                        Value.newBuilder().setStringValue(metadataMap.get(metadataFields[1]).get(0)).build())
                .build();

        // Update required+optional fields
        asyncIndex.update(idToUpdate, valuesToUpdate, metadataToUpdate, namespace, null, null).get();

        // Query by vector to verify
        assertWithRetry(() -> {
            QueryResponseWithUnsignedIndices queryResponse = asyncIndex.query(
                    5,
                    valuesToUpdate,
                    null,
                    null,
                    null,
                    namespace,
                    null,
                    true,
                    true).get();

            List<ScoredVectorWithUnsignedIndices> queryResults = queryResponse.getMatchesList();
            ScoredVectorWithUnsignedIndices scoredVectorV1 = null;

            for (ScoredVectorWithUnsignedIndices vector : queryResults) {
                if (idToUpdate.equals(vector.getId())) {
                    scoredVectorV1 = vector;
                }
            }

            // Verify the correct vector id was updated
            assertNotNull(scoredVectorV1);
            assertEquals(scoredVectorV1.getId(), idToUpdate);

            // Verify the updated values
            List<Float> valuesList = new ArrayList<>(scoredVectorV1.getValuesList());
            assertEquals(valuesToUpdate, valuesList);

            // Verify the metadata was updated
            assertEquals(scoredVectorV1.getMetadata(), metadataToUpdate);

            // Verify the initial sparse values set for upsert operation were not overwritten
            List<Float> expectedSparseValues = sparseValuesList;
            List<Float> actualSparseValues = scoredVectorV1.getSparseValuesWithUnsignedIndices().getValuesList();
            assertEquals(expectedSparseValues, actualSparseValues);
        }, 3);
    }

    @Test
    public void addIncorrectDimensionalValuesFutureTest() throws InterruptedException {
        // Update required fields only but with incorrect values dimension
        String idToUpdate = upsertIds.get(0);
        List<Float> updatedValues = Arrays.asList(101F);

        try {
            asyncIndex.update(idToUpdate, updatedValues, null, namespace, null, null).get();

            fail("Expected to throw statusRuntimeException");
        } catch (ExecutionException executionException) {
            assertTrue(executionException.toString().contains("Vector dimension 1 does not match the dimension of the index " + dimension));
        }
    }

    @Test
    public void queryWithFiltersFutureTest() throws ExecutionException, InterruptedException {
        String fieldToQuery = metadataFields[0];
        String valueToQuery = createAndGetMetadataMap().get(fieldToQuery).get(0);

        Struct filter = Struct.newBuilder()
                .putFields(metadataFields[0], Value.newBuilder()
                        .setStructValue(Struct.newBuilder()
                                .putFields("$eq", Value.newBuilder()
                                        .setStringValue(valueToQuery)
                                        .build()))
                        .build())
                .build();

        assertWithRetry(() -> {
            QueryResponseWithUnsignedIndices queryResponse = asyncIndex.queryByVectorId(3,
                    sparseUpsertIds.get(0),
                    namespace,
                    filter,
                    true,
                    true).get();

            List<ScoredVectorWithUnsignedIndices> queryResults = queryResponse.getMatchesList();
            ScoredVectorWithUnsignedIndices scoredVectorV1 = null;

            for (ScoredVectorWithUnsignedIndices vector : queryResults) {
                if (sparseUpsertIds.get(0).equals(vector.getId())) {
                    scoredVectorV1 = vector;
                }
            }

            // Verify the metadata field is correctly filtered in the query response
            assertNotNull(scoredVectorV1);
            assertTrue(scoredVectorV1.getMetadata().getFieldsMap().get(fieldToQuery).toString().contains(valueToQuery));
        }, 3);
    }

    @Test
    public void updateNullSparseIndicesNotNullSparseValuesFutureTest() throws InterruptedException, ExecutionException {
        String id = RandomStringBuilder.build(3);

        try {
            asyncIndex.update(id,
                    generateVectorValuesByDimension(dimension),
                    null,
                    null,
                    null,
                    generateVectorValuesByDimension(dimension)).get();
            fail("Expected to throw PineconeValidationException");
        } catch (PineconeValidationException validationException) {
            assertTrue(validationException.toString().contains("Invalid upsert request. Please ensure that both sparse indices and values are present."));
        }
    }
}
