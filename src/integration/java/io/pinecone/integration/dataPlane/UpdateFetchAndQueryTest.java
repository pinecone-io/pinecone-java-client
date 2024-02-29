package io.pinecone.integration.dataPlane;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.grpc.StatusRuntimeException;
import io.pinecone.PineconeConnection;
import io.pinecone.PineconeBlockingDataPlaneClient;
import io.pinecone.exceptions.PineconeException;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.IndexModelSpec;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
    public void updateRequiredParamsFetchAndQuerySync() throws InterruptedException {
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
            QueryResponse queryResponse = dataPlaneClient.query(1,
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
    public void updateAllParamsFetchAndQuerySync() throws InterruptedException {
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
        for (int i=index; i<numOfSparseVectors; i++) {
            dataPlaneClient.upsert(upsertIds.get(i),
                    generateVectorValuesByDimension(dimension),
                    sparseIndicesList.get(i),
                    sparseValuesList.get(i),
                    generateMetadataStruct(),
                    namespace);
            index++;
        }

        for(int j=index; j<numOfVectors; j++) {
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
            QueryResponse queryResponse = dataPlaneClient.query(
                    5,
                    valuesToUpdate,
                    null,
                    null,
                    null,
                    namespace,
                    null,
                    true,
                    true);

            ScoredVector scoredVectorV1 = queryResponse.getMatches(0);
            // Verify the correct vector id was updated
            assertEquals(scoredVectorV1.getId(), idToUpdate);

            // Verify the updated values
            assertEquals(valuesToUpdate, scoredVectorV1.getValuesList());

            // Verify the updated metadata
            assertEquals (scoredVectorV1.getMetadata(),metadataToUpdate);

            // Verify the initial sparse values set for upsert operation
            assertEquals (scoredVectorV1.getSparseValues().getValuesList(), sparseValuesList.get(0));
        });
    }

    @Test
    public void addIncorrectDimensionalValuesSync() throws InterruptedException {
        // Upsert vectors with required parameters
        int numOfVectors = 3;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        PineconeBlockingDataPlaneClient dataPlaneClient = new PineconeBlockingDataPlaneClient(blockingStub);

        for(String id:upsertIds) {
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
        try {
            dataPlaneClient.update(idToUpdate, updatedValues, null, namespace, null, null);
        } catch (StatusRuntimeException statusRuntimeException) {
            assert (statusRuntimeException.getTrailers().toString().contains("grpc-status=3"));
            assert (statusRuntimeException.getTrailers().toString().contains("Vector dimension 1 does not match the dimension of the index 3"));
        }
    }

    @Test
    public void queryWithFilersSync() {
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
            for(int i=0; i<upsertIds.size(); i++) {
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

            QueryResponse queryResponse = dataPlaneClient.queryByVectorId(3,
                    upsertIds.get(0),
                    namespace,
                    filter,
                    true,
                    true);

            // Verify the metadata field is correctly filtered in the query response
            assert (queryResponse.getMatches(0).getMetadata().getFieldsMap().get(fieldToQuery).toString().contains(valueToQuery));
        } catch (Exception e) {
            throw new PineconeException(e.getLocalizedMessage());
        }
    }

    @Test
    public void upsertNullSparseIndicesNotNullSparseValuesSyncTest() {
        PineconeBlockingDataPlaneClient dataPlaneClient = new PineconeBlockingDataPlaneClient(blockingStub);
        String id = RandomStringBuilder.build(3);

        try {
            dataPlaneClient.update(id,
                    generateVectorValuesByDimension(dimension),
                    null,
                    null,
                    null,
                    generateVectorValuesByDimension(dimension));
        } catch (PineconeValidationException validationException) {
            assertEquals(validationException.getLocalizedMessage(), "Invalid upsert request. Please ensure that both sparse indices and values are present.");
        }
    }

    // ToDo: Update when future stub changes are in
//    @Test
//    public void UpdateRequiredParamsFetchAndQueryFuture() throws InterruptedException, ExecutionException {
//        // Upsert vectors with required parameters
//        List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
//        String namespace = RandomStringBuilder.build("ns", 8);
//        futureStub.upsert(buildRequiredUpsertRequest(upsertIds, namespace));
//        Thread.sleep(3500);
//
//        // Verify the upserted vector count with fetch
//        FetchRequest fetchRequest = FetchRequest.newBuilder().addAllIds(upsertIds).setNamespace(namespace).build();
//        FetchResponse fetchResponse = futureStub.fetch(fetchRequest).get();
//        assertEquals(fetchResponse.getVectorsCount(), upsertIds.size());
//        for (String key : upsertIds) {
//            assert (fetchResponse.containsVectors(key));
//        }
//
//        List<Float> updatedValues = Arrays.asList(101F, 102F, 103F);
//        // Update required fields only
//        UpdateRequest updateRequest = UpdateRequest.newBuilder()
//                .setId(upsertIds.get(0))
//                .setNamespace(namespace)
//                .addAllValues(updatedValues)
//                .build();
//
//        futureStub.update(updateRequest).get();
//        Thread.sleep(3500);
//
//        // Query to verify
//        QueryRequest queryRequest = QueryRequest.newBuilder()
//                .setId(upsertIds.get(0))
//                .setNamespace(namespace)
//                .setTopK(1)
//                .setIncludeValues(true)
//                .build();
//        QueryResponse queryResponse = futureStub.query(queryRequest).get();
//        List<Float> queryResponseValues = queryResponse.getMatches(0).getValuesList();
//        assert(updatedValues.equals(queryResponseValues));
//    }
//
//    @Test
//    public void addIncorrectDimensionalValuesFuture() throws InterruptedException, ExecutionException {
//        // Upsert vectors with required parameters
//        List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
//        String namespace = RandomStringBuilder.build("ns", 8);
//        futureStub.upsert(buildRequiredUpsertRequest(upsertIds, namespace));
//        Thread.sleep(3500);
//
//        // Verify the upserted vector count with fetch
//        FetchRequest fetchRequest = FetchRequest.newBuilder().addAllIds(upsertIds).setNamespace(namespace).build();
//        FetchResponse fetchResponse = futureStub.fetch(fetchRequest).get();
//        assertEquals(fetchResponse.getVectorsCount(), upsertIds.size());
//        for (String key : upsertIds) {
//            assert (fetchResponse.containsVectors(key));
//        }
//
//        // Update required fields only
//        UpdateRequest updateRequest = UpdateRequest.newBuilder()
//                .setId(upsertIds.get(0))
//                .setNamespace(namespace)
//                .addValues(100F)
//                .build();
//
//        // Should fail since only 1 value is added for the vector of dimension 3
//        try {
//            futureStub.update(updateRequest);
//        } catch (StatusRuntimeException statusRuntimeException) {
//            assert (statusRuntimeException.getTrailers().toString().contains("grpc-status=3"));
//            assert (statusRuntimeException.getTrailers().toString().contains("Vector dimension 1 does not match the dimension of the index 3"));
//        }
//    }
//
//    @Test
//    public void UpdateAllParamsFetchAndQueryFuture() throws InterruptedException, ExecutionException {
//        // Upsert vectors with required parameters
//        List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
//        String expectedId = upsertIds.get(0);
//        String namespace = RandomStringBuilder.build("ns", 8);
//        futureStub.upsert(buildRequiredUpsertRequest(upsertIds, namespace));
//        Thread.sleep(3500);
//
//        // Verify the upserted vector count with fetch
//        FetchRequest fetchRequest = FetchRequest.newBuilder().addAllIds(upsertIds).setNamespace(namespace).build();
//        FetchResponse fetchResponse = futureStub.fetch(fetchRequest).get();
//        assertEquals(fetchResponse.getVectorsCount(), upsertIds.size());
//        for (String key : upsertIds) {
//            assert (fetchResponse.containsVectors(key));
//        }
//
//        List<Float> updatedValues = Arrays.asList(101F, 102F, 103F);
//        HashMap<String, List<String>> metadataMap = createAndGetMetadataMap();
//        Struct metadata = Struct.newBuilder()
//                .putFields(metadataFields[0],
//                        Value.newBuilder().setStringValue(metadataMap.get(metadataFields[0]).get(0)).build())
//                .putFields(metadataFields[1],
//                        Value.newBuilder().setStringValue(metadataMap.get(metadataFields[1]).get(0)).build())
//                .build();
//
//        SparseValues sparseVector = SparseValues
//                .newBuilder()
//                .addAllIndices(sparseIndices)
//                .addAllValues(sparseValues)
//                .build();
//
//        // Update required fields only
//        UpdateRequest updateRequest = UpdateRequest.newBuilder()
//                .setId(expectedId)
//                .setNamespace(namespace)
//                .addValues(updatedValues.get(0))
//                .addValues(updatedValues.get(1))
//                .addValues(updatedValues.get(2))
//                .setSparseValues(sparseVector)
//                .setSetMetadata(metadata)
//                .build();
//
//        futureStub.update(updateRequest).get();
//        Thread.sleep(3500);
//
//        // Query by vector to verify
//        QueryRequest queryRequest = QueryRequest.newBuilder()
//                .setNamespace(namespace)
//                .setTopK(5)
//                .setIncludeValues(true)
//                .setIncludeMetadata(true)
//                .addAllVector(updatedValues)
//                .build();
//        QueryResponse queryResponse = futureStub.query(queryRequest).get();
//        // Returns the vector v1 at 0th index since its queryRequest's values are set to updatedValues ArrayList
//        ScoredVector scoredVectorV1 = queryResponse.getMatches(0);
//        assertEquals(scoredVectorV1.getId(), expectedId);
//
//        // Verify the values are updated correctly
//        List<Float> queryResponseValues = scoredVectorV1.getValuesList();
//        assert (updatedValues.equals(queryResponseValues));
//
//        // Verify the updated metadata
//        assert (scoredVectorV1.getMetadata().equals(metadata));
//
//        // Verify the updated sparse vector
//        assert (scoredVectorV1.getSparseValues().equals(sparseVector));
//    }
//
//    @Test
//    public void UpsertRequiredParamsVectorAndQueryByIncorrectVectorDimensionFuture() {
//        // Upsert vectors with required parameters
//        List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
//        String namespace = RandomStringBuilder.build("ns", 8);
//        try {
//            futureStub.upsert(buildRequiredUpsertRequest(upsertIds, namespace)).get();
//            Thread.sleep(3500);
//
//            QueryRequest queryRequest = QueryRequest.newBuilder()
//                    .setNamespace(namespace)
//                    .setTopK(5)
//                    .setIncludeValues(true)
//                    .setIncludeMetadata(true)
//                    .addVector(100F)
//                    .build();
//
//            futureStub.query(queryRequest).get();
//        } catch (ExecutionException executionException) {
//            assert(executionException.getMessage().contains("grpc-status=3"));
//            assert(executionException.getMessage().contains("grpc-message=Query vector dimension 1 does not match the dimension of the index 3"));
//        } catch (InterruptedException interruptedException) {
//            // ignore;
//        }
//    }
//
//    @Test
//    public void QueryWithFilersFuture() {
//        // Upsert vectors with all parameters
//        String fieldToQuery = metadataFields[0];
//        String valueToQuery = createAndGetMetadataMap().get(fieldToQuery).get(0);
//
//        List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
//        String namespace = RandomStringBuilder.build("ns", 8);
//        try {
//            futureStub.upsert(buildOptionalUpsertRequest(upsertIds, namespace)).get();
//            Thread.sleep(3500);
//
//            QueryRequest queryRequest = QueryRequest.newBuilder()
//                    .setId(upsertIds.get(0))
//                    .setNamespace(namespace)
//                    .setTopK(3)
//                    .setIncludeValues(true)
//                    .setIncludeMetadata(true)
//                    .setFilter(Struct.newBuilder()
//                            .putFields(metadataFields[0], Value.newBuilder()
//                                    .setStructValue(Struct.newBuilder()
//                                            .putFields("$eq", Value.newBuilder()
//                                                    .setStringValue(valueToQuery)
//                                                    .build()))
//                                    .build())
//                            .build())
//                    .build();
//            QueryResponse queryResponse = futureStub.query(queryRequest).get();
//            // Verify the metadata field is correctly filtered in the query response
//            assert (queryResponse.getMatches(0).getMetadata().getFieldsMap().get(fieldToQuery).toString().contains(valueToQuery));
//        } catch (ExecutionException | InterruptedException e) {
//            // ignore
//        }
//    }

    // ToDo: upsertNullSparseIndicesNotNullSparseValuesFutureTest()
}
