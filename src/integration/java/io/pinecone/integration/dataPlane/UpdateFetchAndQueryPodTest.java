package io.pinecone.integration.dataPlane;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.grpc.StatusRuntimeException;
import io.pinecone.clients.Pinecone;
import io.pinecone.clients.Index;
import io.pinecone.clients.AsyncIndex;
import io.pinecone.exceptions.PineconeException;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.helpers.TestIndexResourcesManager;
import io.pinecone.proto.*;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static io.pinecone.helpers.AssertRetry.assertWithRetry;
import static io.pinecone.helpers.BuildUpsertRequest.*;
import static org.junit.jupiter.api.Assertions.*;

public class UpdateFetchAndQueryPodTest {
    private static final TestIndexResourcesManager indexManager = TestIndexResourcesManager.getInstance();
    private static Index index;
    private static AsyncIndex asyncIndex;
    private static final String namespace = RandomStringBuilder.build("ns", 8);
    private static List<String> upsertIds;
    private static List<List<Float>> sparseValuesList;
    private static int dimension;

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException {
        Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();

        String indexName = indexManager.getPodIndexName();
        dimension = indexManager.getDimension();
        index = pinecone.getIndexConnection(indexName);
        asyncIndex = pinecone.getAsyncIndexConnection(indexName);

        // Upsert vectors only once
        int numOfVectors = 3;
        int numOfSparseVectors = 2;
        upsertIds = getIdsList(numOfVectors);
        List<List<Long>> sparseIndicesList = getSparseIndicesList(numOfSparseVectors, dimension);
        sparseValuesList = getValuesListLists(numOfSparseVectors, dimension);

        // upsert vectors with required + optional parameters
        int sparseVectorCount = 0;
        for (int i = sparseVectorCount; i < numOfSparseVectors; i++) {
            index.upsert(upsertIds.get(i),
                    generateVectorValuesByDimension(dimension),
                    sparseIndicesList.get(i),
                    sparseValuesList.get(i),
                    generateMetadataStruct(i,i),
                    namespace);
            sparseVectorCount++;
        }

        for (int j = sparseVectorCount; j < numOfVectors; j++) {
            index.upsert(
                    upsertIds.get(j),
                    generateVectorValuesByDimension(dimension),
                    namespace);
        }
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
                assert (fetchResponse.containsVectors(key));
            }
        });

        String idToUpdate = upsertIds.get(0);
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

            ScoredVectorWithUnsignedIndices scoredVectorV1 = queryResponse.getMatches(0);
            // Verify the correct vector id was updated
            assertEquals(scoredVectorV1.getId(), idToUpdate);

            // Verify the updated values
            List<Float> valuesList = new ArrayList<>(scoredVectorV1.getValuesList());
            Collections.sort(valuesList);
            Collections.sort(valuesToUpdate);
            assertEquals(valuesToUpdate, valuesList);

            // Verify the updated metadata
            assertEquals(scoredVectorV1.getMetadata(), metadataToUpdate);

            // Verify the initial sparse values set for upsert operation
            List<Float> expectedSparseValues = new ArrayList<>(scoredVectorV1.getSparseValuesWithUnsignedIndices().getValuesList());
            List<Float> actualSparseValues = new ArrayList<>(sparseValuesList.get(0));
            Collections.sort(actualSparseValues);
            Collections.sort(expectedSparseValues);

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
            assert (statusRuntimeException.toString().contains("Vector dimension 1 does not match the dimension of the index " + dimension));
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
            assert(validationException.toString().contains("Invalid upsert request. Please ensure that both sparse indices and values are present."));
        }
    }

    @Test
    public void queryWithFilersSyncTest() {
        String fieldToQuery = metadataFields[0];
        String valueToQuery = createAndGetMetadataMap().get(fieldToQuery).get(0);

        try {
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
                assert (queryResponse.getMatches(0).getMetadata().getFieldsMap().get(fieldToQuery).toString().contains(valueToQuery));
            }, 3);
        } catch (Exception e) {
            throw new PineconeException(e.getLocalizedMessage());
        }
    }

    @Test
    public void updateAllParamsFetchAndQueryFutureTest() throws InterruptedException, ExecutionException {
        // Verify the upserted vector count with fetch
        assertWithRetry(() -> {
            FetchResponse fetchResponse = asyncIndex.fetch(upsertIds, namespace).get();
            assertEquals(fetchResponse.getVectorsCount(), upsertIds.size());
            for (String key : upsertIds) {
                assert (fetchResponse.containsVectors(key));
            }
        });

        String idToUpdate = upsertIds.get(0);
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

            ScoredVectorWithUnsignedIndices scoredVectorV1 = queryResponse.getMatches(0);
            // Verify the correct vector id was updated
            assertEquals(scoredVectorV1.getId(), idToUpdate);

            // Verify the updated values
            List<Float> valuesList = new ArrayList<>(scoredVectorV1.getValuesList());
            Collections.sort(valuesList);
            Collections.sort(valuesToUpdate);
            assertEquals(valuesToUpdate, valuesList);

            // Verify the updated metadata
            assertEquals(scoredVectorV1.getMetadata(), metadataToUpdate);

            // Verify the initial sparse values set for upsert operation
            List<Float> expectedSparseValues = new ArrayList<>(scoredVectorV1.getSparseValuesWithUnsignedIndices().getValuesList());
            List<Float> actualSparseValues = new ArrayList<>(sparseValuesList.get(0));
            Collections.sort(actualSparseValues);
            Collections.sort(expectedSparseValues);
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
            assert (executionException.toString().contains("Vector dimension 1 does not match the dimension of the index " + dimension));
        }
    }

    @Test
    public void queryWithFilersFutureTest() throws ExecutionException, InterruptedException {
        String fieldToQuery = metadataFields[0];
        String valueToQuery = createAndGetMetadataMap().get(fieldToQuery).get(0);

        try {
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
                        upsertIds.get(0),
                        namespace,
                        filter,
                        true,
                        true).get();

                // Verify the metadata field is correctly filtered in the query response
                assert (queryResponse.getMatches(0).getMetadata().getFieldsMap().get(fieldToQuery).toString().contains(valueToQuery));
            }, 3);
        } catch (Exception exception) {
            throw exception;
        }
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
            assert(validationException.toString().contains("Invalid upsert request. Please ensure that both sparse indices and values are present."));
        }
    }
}
