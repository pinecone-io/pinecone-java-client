package io.pinecone.integration.dataPlane;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.grpc.StatusRuntimeException;
import io.pinecone.clients.AsyncIndex;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeException;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.*;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.IndexModelSpec;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static io.pinecone.helpers.AssertRetry.assertWithRetry;
import static io.pinecone.helpers.BuildUpsertRequest.*;
import static io.pinecone.helpers.IndexManager.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class UpdateFetchAndQueryServerlessTest {

    private static Index index;
    private static AsyncIndex asyncIndex;

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException {
        final int dimension = 3;
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
    public void updateRequiredParamsFetchAndQuerySyncTest() throws InterruptedException {
        // Upsert vectors with required parameters
        int numOfVectors = 3;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        for (String id : upsertIds) {
            index.upsert(id, generateVectorValuesByDimension(3), namespace);
        }

        Thread.sleep(10000);

        // Verify the upserted vector count with fetch
        assertWithRetry(() -> {
            FetchResponse fetchResponse = index.fetch(upsertIds, namespace);
            assertEquals(fetchResponse.getVectorsCount(), upsertIds.size());
            for (String key : upsertIds) {
                assert (fetchResponse.containsVectors(key));
            }
        });

        // Update required fields only
        String idToUpdate = upsertIds.get(0);
        List<Float> updatedValues = Arrays.asList(101F, 102F, 103F);
        index.update(idToUpdate, updatedValues, null, namespace, null, null);

        // Query by ID to verify
        assertWithRetry(() -> {
            QueryResponseWithUnsignedIndices queryResponse = index.query(1,
                    null,
                    null,
                    null,
                    idToUpdate,
                    namespace,
                    null,
                    true,
                    false);

            List<Float> queryResponseValues = queryResponse.getMatches(0).getValuesList();
            assertEquals(updatedValues, queryResponseValues);
        });
    }

    @Test
    public void updateAllParamsFetchAndQuerySyncTest() throws InterruptedException {
        int dimension = 3;
        int numOfVectors = 3;
        int numOfSparseVectors = 2;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = index.describeIndexStats(null);
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);
        List<List<Long>> sparseIndicesList = getSparseIndicesList(numOfSparseVectors, dimension);
        List<List<Float>> sparseValuesList = getValuesListLists(numOfSparseVectors, dimension);

        // upsert vectors with required + optional parameters
        int sparseVectorCount = 0;
        for (int i = sparseVectorCount; i < numOfSparseVectors; i++) {
            index.upsert(upsertIds.get(i),
                    generateVectorValuesByDimension(dimension),
                    sparseIndicesList.get(i),
                    sparseValuesList.get(i),
                    generateMetadataStruct(),
                    namespace);
            sparseVectorCount++;
        }

        for (int j = sparseVectorCount; j < numOfVectors; j++) {
            index.upsert(
                    upsertIds.get(j),
                    generateVectorValuesByDimension(dimension),
                    namespace);
        }

        // Verify the upserted vector count with fetch
        assertWithRetry(() -> {
            FetchResponse fetchResponse = index.fetch(upsertIds, namespace);
            assertEquals(fetchResponse.getVectorsCount(), upsertIds.size());
            for (String key : upsertIds) {
                assert (fetchResponse.containsVectors(key));
            }
        });

        Thread.sleep(10000);

        String idToUpdate = upsertIds.get(0);
        List<Float> valuesToUpdate = Arrays.asList(101F, 102F, 103F);
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
        });
    }

    @Test
    public void addIncorrectDimensionalValuesSyncTest() throws InterruptedException {
        // Upsert vectors with required parameters
        int dimension = 3;
        int numOfVectors = 3;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);

        for (String id : upsertIds) {
            index.upsert(id,
                    generateVectorValuesByDimension(dimension),
                    namespace);
        }

        Thread.sleep(10000);

        // Verify the upserted vector count with fetch
        assertWithRetry(() -> {
            FetchResponse fetchResponse = index.fetch(upsertIds, namespace);
            assertEquals(fetchResponse.getVectorsCount(), upsertIds.size());
            for (String key : upsertIds) {
                assert (fetchResponse.containsVectors(key));
            }
        });

        // Update required fields only but with incorrect values dimension
        String idToUpdate = upsertIds.get(0);
        List<Float> updatedValues = Arrays.asList(101F); // should be of size 3

        // Should fail since only 1 value is added for the vector of dimension 3
        try {
            index.update(idToUpdate, updatedValues, null, namespace, null, null);

            fail("Expected to throw statusRuntimeException");
        } catch (StatusRuntimeException statusRuntimeException) {
            assert (statusRuntimeException.toString().contains("Vector dimension 1 does not match the dimension of the index 3"));
        }
    }

    @Test
    public void queryWithFilersSyncTest() {
        // Upsert vectors with all parameters
        int dimension = 3;
        String fieldToQuery = metadataFields[0];
        String valueToQuery = createAndGetMetadataMap().get(fieldToQuery).get(0);

        int numOfVectors = 3;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = index.describeIndexStats();
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);

        try {
            for (int i = 0; i < upsertIds.size(); i++) {
                index.upsert(upsertIds.get(0),
                        generateVectorValuesByDimension(dimension),
                        generateSparseIndicesByDimension(dimension),
                        generateVectorValuesByDimension(dimension),
                        generateMetadataStruct(0, 0),
                        namespace);
            }

            Thread.sleep(10000);

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
            });
        } catch (Exception e) {
            throw new PineconeException(e.getLocalizedMessage());
        }
    }

    @Test
    public void updateNullSparseIndicesNotNullSparseValuesSyncTest() {
        int dimension = 3;
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
    public void updateRequiredParamsFetchAndQueryFutureTest() throws InterruptedException, ExecutionException {
        // Upsert vectors with required parameters
        int dimension = 3;
        int numOfVectors = 3;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        for (String id : upsertIds) {
            asyncIndex.upsert(id, generateVectorValuesByDimension(dimension), namespace).get();
        }

        Thread.sleep(10000);

        // Verify the upserted vector count with fetch
        assertWithRetry(() -> {
            FetchResponse fetchResponse = asyncIndex.fetch(upsertIds, namespace).get();
            assertEquals(fetchResponse.getVectorsCount(), upsertIds.size());
            for (String key : upsertIds) {
                assert (fetchResponse.containsVectors(key));
            }
        });

        // Update required fields only
        String idToUpdate = upsertIds.get(0);
        List<Float> updatedValues = Arrays.asList(101F, 102F, 103F);
        asyncIndex.update(idToUpdate, updatedValues, null, namespace, null, null).get();

        Thread.sleep(10000);

        // Query by ID to verify
        assertWithRetry(() -> {
            QueryResponseWithUnsignedIndices queryResponse = asyncIndex.query(1,
                    null,
                    null,
                    null,
                    idToUpdate,
                    namespace,
                    null,
                    true,
                    false).get();

            List<Float> queryResponseValues = queryResponse.getMatches(0).getValuesList();
            assertEquals(updatedValues, queryResponseValues);
        });
    }

    @Test
    public void updateAllParamsFetchAndQueryFutureTest() throws InterruptedException, ExecutionException {
        int dimension = 3;
        int numOfVectors = 3;
        int numOfSparseVectors = 2;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = asyncIndex.describeIndexStats(null).get();
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);
        List<List<Long>> sparseIndicesList = getSparseIndicesList(numOfSparseVectors, dimension);
        List<List<Float>> sparseValuesList = getValuesListLists(numOfSparseVectors, dimension);

        // upsert vectors with required + optional parameters
        int sparseVectorCount = 0;
        for (int i = sparseVectorCount; i < numOfSparseVectors; i++) {
            asyncIndex.upsert(upsertIds.get(i),
                    generateVectorValuesByDimension(dimension),
                    sparseIndicesList.get(i),
                    sparseValuesList.get(i),
                    generateMetadataStruct(),
                    namespace).get();
            sparseVectorCount++;
        }

        for (int j = sparseVectorCount; j < numOfVectors; j++) {
            asyncIndex.upsert(
                    upsertIds.get(j),
                    generateVectorValuesByDimension(dimension),
                    namespace).get();
        }

        Thread.sleep(10000);

        // Verify the upserted vector count with fetch
        assertWithRetry(() -> {
            FetchResponse fetchResponse = asyncIndex.fetch(upsertIds, namespace).get();
            assertEquals(fetchResponse.getVectorsCount(), upsertIds.size());
            for (String key : upsertIds) {
                assert (fetchResponse.containsVectors(key));
            }
        });

        String idToUpdate = upsertIds.get(0);
        List<Float> valuesToUpdate = Arrays.asList(101F, 102F, 103F);
        HashMap<String, List<String>> metadataMap = createAndGetMetadataMap();
        Struct metadataToUpdate = Struct.newBuilder()
                .putFields(metadataFields[0],
                        Value.newBuilder().setStringValue(metadataMap.get(metadataFields[0]).get(0)).build())
                .putFields(metadataFields[1],
                        Value.newBuilder().setStringValue(metadataMap.get(metadataFields[1]).get(0)).build())
                .build();

        // Update required+optional fields
        asyncIndex.update(idToUpdate, valuesToUpdate, metadataToUpdate, namespace, null, null).get();

        Thread.sleep(10000);

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
        });
    }

    @Test
    public void addIncorrectDimensionalValuesFutureTest() throws InterruptedException, ExecutionException {
        // Upsert vectors with required parameters
        int dimension = 3;
        int numOfVectors = 3;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);

        for (String id : upsertIds) {
            asyncIndex.upsert(id,
                    generateVectorValuesByDimension(dimension),
                    namespace).get();
        }

        Thread.sleep(10000);

        // Verify the upserted vector count with fetch
        assertWithRetry(() -> {
            FetchResponse fetchResponse = asyncIndex.fetch(upsertIds, namespace).get();
            assertEquals(fetchResponse.getVectorsCount(), upsertIds.size());
            for (String key : upsertIds) {
                assert (fetchResponse.containsVectors(key));
            }
        });

        // Update required fields only but with incorrect values dimension
        String idToUpdate = upsertIds.get(0);
        List<Float> updatedValues = Arrays.asList(101F); // should be of size 3

        // Should fail since only 1 value is added for the vector of dimension 3
        try {
            asyncIndex.update(idToUpdate, updatedValues, null, namespace, null, null).get();

            fail("Expected to throw statusRuntimeException");
        } catch (ExecutionException executionException) {
            assert (executionException.toString().contains("Vector dimension 1 does not match the dimension of the index 3"));
        }
    }

    @Test
    public void queryWithFilersFutureTest() throws ExecutionException, InterruptedException {
        // Upsert vectors with all parameters
        int dimension = 3;
        String fieldToQuery = metadataFields[0];
        String valueToQuery = createAndGetMetadataMap().get(fieldToQuery).get(0);

        int numOfVectors = 3;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = asyncIndex.describeIndexStats(null).get();
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);

        try {
            for (int i = 0; i < upsertIds.size(); i++) {
                asyncIndex.upsert(upsertIds.get(0),
                        generateVectorValuesByDimension(dimension),
                        generateSparseIndicesByDimension(dimension),
                        generateVectorValuesByDimension(dimension),
                        generateMetadataStruct(0, 0),
                        namespace).get();
            }

            Struct filter = Struct.newBuilder()
                    .putFields(metadataFields[0], Value.newBuilder()
                            .setStructValue(Struct.newBuilder()
                                    .putFields("$eq", Value.newBuilder()
                                            .setStringValue(valueToQuery)
                                            .build()))
                            .build())
                    .build();

            Thread.sleep(10000);

            assertWithRetry(() -> {
                QueryResponseWithUnsignedIndices queryResponse = asyncIndex.queryByVectorId(3,
                        upsertIds.get(0),
                        namespace,
                        filter,
                        true,
                        true).get();

                // Verify the metadata field is correctly filtered in the query response
                assert (queryResponse.getMatches(0).getMetadata().getFieldsMap().get(fieldToQuery).toString().contains(valueToQuery));
            });
        } catch (Exception exception) {
            throw exception;
        }
    }

    @Test
    public void updateNullSparseIndicesNotNullSparseValuesFutureTest() throws InterruptedException, ExecutionException {
        int dimension = 3;
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
