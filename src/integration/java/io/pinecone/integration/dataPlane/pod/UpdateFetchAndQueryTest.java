package io.pinecone.integration.dataPlane.pod;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.grpc.StatusRuntimeException;
import io.pinecone.configs.PineconeConnection;
import io.pinecone.clients.PineconeBlockingDataPlaneClient;
import io.pinecone.clients.PineconeFutureDataPlaneClient;
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
import static io.pinecone.helpers.IndexManager.createIndexIfNotExistsDataPlane;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UpdateFetchAndQueryTest {
    private static PineconeConnection connection;
    private static VectorServiceGrpc.VectorServiceBlockingStub blockingStub;
    private static VectorServiceGrpc.VectorServiceFutureStub futureStub;
    private static final int dimension = 3;

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException {
        connection = createIndexIfNotExistsDataPlane(dimension, IndexModelSpec.SERIALIZED_NAME_POD);
        blockingStub = connection.getBlockingStub();
        futureStub = connection.getFutureStub();
    }

    @AfterAll
    public static void cleanUp() {
        connection.close();
    }

    @Test
    public void updateRequiredParamsFetchAndQuerySyncTest() throws InterruptedException {
        // Upsert vectors with required parameters
        int numOfVectors = 3;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        PineconeBlockingDataPlaneClient dataPlaneClient = new PineconeBlockingDataPlaneClient(blockingStub);
        for (String id : upsertIds) {
            dataPlaneClient.upsert(id, generateVectorValuesByDimension(dimension), namespace);
        }

        // Verify the upserted vector count with fetch
        assertWithRetry(() -> {
            FetchResponse fetchResponse = dataPlaneClient.fetch(upsertIds, namespace);
            assertEquals(fetchResponse.getVectorsCount(), upsertIds.size());
            for (String key : upsertIds) {
                assert (fetchResponse.containsVectors(key));
            }
        });

        // Update required fields only
        String idToUpdate = upsertIds.get(0);
        List<Float> updatedValues = Arrays.asList(101F, 102F, 103F);
        dataPlaneClient.update(idToUpdate, updatedValues, null, namespace, null, null);

        // Query by ID to verify
        assertWithRetry(() -> {
            QueryResponseWithUnsignedIndices queryResponse = dataPlaneClient.query(1,
                    null,
                    null,
                    null,
                    idToUpdate,
                    namespace,
                    null,
                    true,
                    false);

            List<Float> queryResponseValues = queryResponse.getMatches(0).getValuesList();
            assert (updatedValues.equals(queryResponseValues));
        });
    }

    @Test
    public void updateAllParamsFetchAndQuerySyncTest() throws InterruptedException {
        int numOfVectors = 3;
        int numOfSparseVectors = 2;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        PineconeBlockingDataPlaneClient dataPlaneClient = new PineconeBlockingDataPlaneClient(blockingStub);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = dataPlaneClient.describeIndexStats(null);
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);
        List<List<Long>> sparseIndicesList = getSparseIndicesList(numOfSparseVectors, dimension);
        List<List<Float>> sparseValuesList = getValuesListLists(numOfSparseVectors, dimension);

        // upsert vectors with required + optional parameters
        int index = 0;
        for (int i = index; i < numOfSparseVectors; i++) {
            dataPlaneClient.upsert(upsertIds.get(i),
                    generateVectorValuesByDimension(dimension),
                    sparseIndicesList.get(i),
                    sparseValuesList.get(i),
                    generateMetadataStruct(),
                    namespace);
            index++;
        }

        for (int j = index; j < numOfVectors; j++) {
            dataPlaneClient.upsert(
                    upsertIds.get(j),
                    generateVectorValuesByDimension(dimension),
                    namespace);
        }

        // Verify the upserted vector count with fetch
        assertWithRetry(() -> {
            FetchResponse fetchResponse = dataPlaneClient.fetch(upsertIds, namespace);
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
        dataPlaneClient.update(idToUpdate, valuesToUpdate, metadataToUpdate, namespace, null, null);

        // Query by vector to verify
        assertWithRetry(() -> {
            QueryResponseWithUnsignedIndices queryResponse = dataPlaneClient.query(
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
            assertEquals(scoredVectorV1.getSparseValuesWithUnsignedIndices().getValuesList(), sparseValuesList.get(0));
        });
    }

    @Test
    public void addIncorrectDimensionalValuesSyncTest() throws InterruptedException {
        // Upsert vectors with required parameters
        int numOfVectors = 3;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        PineconeBlockingDataPlaneClient dataPlaneClient = new PineconeBlockingDataPlaneClient(blockingStub);

        for (String id : upsertIds) {
            dataPlaneClient.upsert(id,
                    generateVectorValuesByDimension(dimension),
                    namespace);
        }

        // Verify the upserted vector count with fetch
        assertWithRetry(() -> {
            FetchResponse fetchResponse = dataPlaneClient.fetch(upsertIds, namespace);
            assertEquals(fetchResponse.getVectorsCount(), upsertIds.size());
            for (String key : upsertIds) {
                assert (fetchResponse.containsVectors(key));
            }
        });

        // Update required fields only but with incorrect values dimension
        String idToUpdate = upsertIds.get(0);
        List<Float> updatedValues = Arrays.asList(101F); // should be of size 3

        // Should fail since only 1 value is added for the vector of dimension 3
        StringBuilder exceptionMessage = new StringBuilder();
        try {
            dataPlaneClient.update(idToUpdate, updatedValues, null, namespace, null, null);
        } catch (StatusRuntimeException statusRuntimeException) {
            exceptionMessage.append(statusRuntimeException.getLocalizedMessage());
        } finally {
            assert (exceptionMessage.toString().contains("grpc-status=3"));
            assert (exceptionMessage.toString().contains("Vector dimension 1 does not match the dimension of the index 3"));
        }
    }

    @Test
    public void queryWithFilersSyncTest() {
        // Upsert vectors with all parameters
        String fieldToQuery = metadataFields[0];
        String valueToQuery = createAndGetMetadataMap().get(fieldToQuery).get(0);

        int numOfVectors = 3;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        PineconeBlockingDataPlaneClient dataPlaneClient = new PineconeBlockingDataPlaneClient(blockingStub);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = dataPlaneClient.describeIndexStats(null);
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);

        try {
            for (int i = 0; i < upsertIds.size(); i++) {
                dataPlaneClient.upsert(upsertIds.get(0),
                        generateVectorValuesByDimension(dimension),
                        generateSparseIndicesByDimension(dimension),
                        generateVectorValuesByDimension(dimension),
                        generateMetadataStruct(0, 0),
                        namespace);
            }

            Struct filter = Struct.newBuilder()
                    .putFields(metadataFields[0], Value.newBuilder()
                            .setStructValue(Struct.newBuilder()
                                    .putFields("$eq", Value.newBuilder()
                                            .setStringValue(valueToQuery)
                                            .build()))
                            .build())
                    .build();

            assertWithRetry(() -> {
                QueryResponseWithUnsignedIndices queryResponse = dataPlaneClient.queryByVectorId(3,
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
        PineconeBlockingDataPlaneClient dataPlaneClient = new PineconeBlockingDataPlaneClient(blockingStub);
        String id = RandomStringBuilder.build(3);
        StringBuilder exceptionMessage = new StringBuilder();
        try {
            dataPlaneClient.update(id,
                    generateVectorValuesByDimension(dimension),
                    null,
                    null,
                    null,
                    generateVectorValuesByDimension(dimension));
        } catch (PineconeValidationException validationException) {
            exceptionMessage.append(validationException.getLocalizedMessage());
        } finally {
            assertEquals(exceptionMessage.toString(), "Invalid upsert request. Please ensure that both sparse indices and values are present.");
        }
    }

    @Test
    public void updateRequiredParamsFetchAndQueryFutureTest() throws InterruptedException, ExecutionException {
        // Upsert vectors with required parameters
        int numOfVectors = 3;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        PineconeFutureDataPlaneClient dataPlaneClient = new PineconeFutureDataPlaneClient(futureStub);
        for (String id : upsertIds) {
            dataPlaneClient.upsert(id, generateVectorValuesByDimension(dimension), namespace);
        }

        // Verify the upserted vector count with fetch
        assertWithRetry(() -> {
            FetchResponse fetchResponse = dataPlaneClient.fetch(upsertIds, namespace).get();
            assertEquals(fetchResponse.getVectorsCount(), upsertIds.size());
            for (String key : upsertIds) {
                assert (fetchResponse.containsVectors(key));
            }
        });

        // Update required fields only
        String idToUpdate = upsertIds.get(0);
        List<Float> updatedValues = Arrays.asList(101F, 102F, 103F);
        dataPlaneClient.update(idToUpdate, updatedValues, null, namespace, null, null).get();

        // Query by ID to verify
        assertWithRetry(() -> {
            QueryResponseWithUnsignedIndices queryResponse = dataPlaneClient.query(1,
                    null,
                    null,
                    null,
                    idToUpdate,
                    namespace,
                    null,
                    true,
                    false).get();

            List<Float> queryResponseValues = queryResponse.getMatches(0).getValuesList();
            assert (updatedValues.equals(queryResponseValues));
        });
    }

    @Test
    public void updateAllParamsFetchAndQueryFutureTest() throws InterruptedException, ExecutionException {
        int numOfVectors = 3;
        int numOfSparseVectors = 2;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        PineconeFutureDataPlaneClient dataPlaneClient = new PineconeFutureDataPlaneClient(futureStub);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = dataPlaneClient.describeIndexStats(null).get();
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);
        List<List<Long>> sparseIndicesList = getSparseIndicesList(numOfSparseVectors, dimension);
        List<List<Float>> sparseValuesList = getValuesListLists(numOfSparseVectors, dimension);

        // upsert vectors with required + optional parameters
        int index = 0;
        for (int i = index; i < numOfSparseVectors; i++) {
            dataPlaneClient.upsert(upsertIds.get(i),
                    generateVectorValuesByDimension(dimension),
                    sparseIndicesList.get(i),
                    sparseValuesList.get(i),
                    generateMetadataStruct(),
                    namespace).get();
            index++;
        }

        for (int j = index; j < numOfVectors; j++) {
            dataPlaneClient.upsert(
                    upsertIds.get(j),
                    generateVectorValuesByDimension(dimension),
                    namespace).get();
        }

        // Verify the upserted vector count with fetch
        assertWithRetry(() -> {
            FetchResponse fetchResponse = dataPlaneClient.fetch(upsertIds, namespace).get();
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
        dataPlaneClient.update(idToUpdate, valuesToUpdate, metadataToUpdate, namespace, null, null).get();

        // Query by vector to verify
        assertWithRetry(() -> {
            QueryResponseWithUnsignedIndices queryResponse = dataPlaneClient.query(
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
            assertEquals(scoredVectorV1.getSparseValuesWithUnsignedIndices().getValuesList(), sparseValuesList.get(0));
        });
    }

    @Test
    public void addIncorrectDimensionalValuesFutureTest() throws InterruptedException {
        // Upsert vectors with required parameters
        int numOfVectors = 3;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        PineconeFutureDataPlaneClient dataPlaneClient = new PineconeFutureDataPlaneClient(futureStub);

        for (String id : upsertIds) {
            dataPlaneClient.upsert(id,
                    generateVectorValuesByDimension(dimension),
                    namespace);
        }

        // Verify the upserted vector count with fetch
        assertWithRetry(() -> {
            FetchResponse fetchResponse = dataPlaneClient.fetch(upsertIds, namespace).get();
            assertEquals(fetchResponse.getVectorsCount(), upsertIds.size());
            for (String key : upsertIds) {
                assert (fetchResponse.containsVectors(key));
            }
        });

        // Update required fields only but with incorrect values dimension
        String idToUpdate = upsertIds.get(0);
        List<Float> updatedValues = Arrays.asList(101F); // should be of size 3

        // Should fail since only 1 value is added for the vector of dimension 3
        StringBuilder exceptionMessage = new StringBuilder();
        try {
            dataPlaneClient.update(idToUpdate, updatedValues, null, namespace, null, null).get();
        } catch (ExecutionException executionException) {
            exceptionMessage.append(executionException.getLocalizedMessage());
        } finally {
            assert (exceptionMessage.toString().contains("grpc-status=3"));
            assert (exceptionMessage.toString().contains("Vector dimension 1 does not match the dimension of the index 3"));
        }
    }

    @Test
    public void queryWithFilersFutureTest() throws ExecutionException, InterruptedException {
        // Upsert vectors with all parameters
        String fieldToQuery = metadataFields[0];
        String valueToQuery = createAndGetMetadataMap().get(fieldToQuery).get(0);

        int numOfVectors = 3;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        PineconeFutureDataPlaneClient dataPlaneClient = new PineconeFutureDataPlaneClient(futureStub);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = dataPlaneClient.describeIndexStats(null).get();
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);

        try {
            for (int i = 0; i < upsertIds.size(); i++) {
                dataPlaneClient.upsert(upsertIds.get(0),
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

            assertWithRetry(() -> {
                QueryResponseWithUnsignedIndices queryResponse = dataPlaneClient.queryByVectorId(3,
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
        PineconeFutureDataPlaneClient dataPlaneClient = new PineconeFutureDataPlaneClient(futureStub);
        String id = RandomStringBuilder.build(3);
        StringBuilder exceptionMessage = new StringBuilder();
        try {
            dataPlaneClient.update(id,
                    generateVectorValuesByDimension(dimension),
                    null,
                    null,
                    null,
                    generateVectorValuesByDimension(dimension)).get();
        } catch (PineconeValidationException validationException) {
            exceptionMessage.append(validationException.getLocalizedMessage());
        } finally {
            assertEquals(exceptionMessage.toString(), "Invalid upsert request. Please ensure that both sparse indices and values are present.");
        }
    }
}
