package io.pinecone.integration.dataPlane;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.grpc.StatusRuntimeException;
import io.pinecone.PineconeConnection;
import io.pinecone.PineconeDataPlaneClient;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.IndexModelSpec;

import java.io.IOException;
import java.util.ArrayList;
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
    private static final List emptyList = new ArrayList<>();

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
    public void UpdateRequiredParamsFetchAndQuerySync() throws InterruptedException {
        // Upsert vectors with required parameters
        int numOfVectors = 3;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        PineconeDataPlaneClient dataPlaneClient = new PineconeDataPlaneClient(blockingStub);

        dataPlaneClient.batchUpsert(
                upsertIds,
                getValuesList(numOfVectors, dimension),
                emptyList,
                emptyList,
                emptyList,
                namespace);

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
            QueryResponse queryResponse = dataPlaneClient.query(idToUpdate,
                    namespace,
                    1,
                    null,
                    true,
                    false,
                    null,
                    null,
                    null);
            List<Float> queryResponseValues = queryResponse.getMatches(0).getValuesList();
            assert (updatedValues.equals(queryResponseValues));
        });
    }

    @Test
    public void UpdateAllParamsFetchAndQuerySync() throws InterruptedException {
        int numOfVectors = 3;
        int numOfSparseVectors = 2;
        int numOfMetadataStructs = 3;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        PineconeDataPlaneClient dataPlaneClient = new PineconeDataPlaneClient(blockingStub);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = dataPlaneClient.describeIndexStats(null);
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);
        List<List<Long>> sparseIndicesList = getSparseIndicesList(numOfSparseVectors, dimension);
        List<List<Float>> sparseValuesList = getValuesList(numOfSparseVectors, dimension);
        // upsert vectors with required + optional parameters
        dataPlaneClient.batchUpsert(
                upsertIds,
                getValuesList(numOfVectors, dimension),
                sparseIndicesList,
                sparseValuesList,
                getMetadataStruct(numOfMetadataStructs),
                namespace);

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
                    null,
                    namespace,
                    5,
                    null,
                    true,
                    true,
                    valuesToUpdate,
                    null,
                    null);
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
        PineconeDataPlaneClient dataPlaneClient = new PineconeDataPlaneClient(blockingStub);

        dataPlaneClient.batchUpsert(
                upsertIds,
                getValuesList(numOfVectors, dimension),
                emptyList,
                emptyList,
                emptyList,
                namespace);

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
    public void UpsertRequiredParamsVectorAndQueryByIncorrectVectorDimensionSync() throws InterruptedException {
        int numOfVectors = 3;
        int numOfSparseVectors = 2;
        int numOfMetadataStructs = 3;
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = getIdsList(numOfVectors);
        PineconeDataPlaneClient dataPlaneClient = new PineconeDataPlaneClient(blockingStub);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = dataPlaneClient.describeIndexStats(null);
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);

        // upsert vectors with required+optional parameters
        dataPlaneClient.batchUpsert(
                upsertIds,
                getValuesList(numOfVectors, dimension),
                getSparseIndicesList(numOfSparseVectors, dimension),
                getValuesList(numOfSparseVectors, dimension),
                getMetadataStruct(numOfMetadataStructs),
                namespace);

        // Verify the upserted vector count with fetch
        assertWithRetry(() -> {
            FetchResponse fetchResponse = dataPlaneClient.fetch(upsertIds, namespace);
            assertEquals(fetchResponse.getVectorsCount(), upsertIds.size());
            for (String key : upsertIds) {
                assert (fetchResponse.containsVectors(key));
            }
        });

        // Query with incorrect dimensions
        try {
            List<Float> vectors = Arrays.asList(100F);
            QueryResponse queryResponse = dataPlaneClient.query(
                    null,
                    namespace,
                    5,
                    null,
                    true,
                    true,
                    vectors,
                    null,
                    null);
        } catch (StatusRuntimeException statusRuntimeException) {
            assert (statusRuntimeException.getTrailers().toString().contains("grpc-status=3"));
            assert (statusRuntimeException.getTrailers().toString().contains("grpc-message=Query vector dimension 1 does not match the dimension of the index 3"));
        }
    }

    @Test
    public void QueryWithFilersSync() {
        // Upsert vectors with all parameters
        String fieldToQuery = metadataFields[0];
        String valueToQuery = createAndGetMetadataMap().get(fieldToQuery).get(0);

        List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
        String namespace = RandomStringBuilder.build("ns", 8);
        try {
            blockingStub.upsert(buildOptionalUpsertRequest(upsertIds, namespace));
            Thread.sleep(3500);

            QueryRequest queryRequest = QueryRequest.newBuilder()
                    .setId(upsertIds.get(0))
                    .setNamespace(namespace)
                    .setTopK(3)
                    .setIncludeValues(true)
                    .setIncludeMetadata(true)
                    .setFilter(Struct.newBuilder()
                            .putFields(metadataFields[0], Value.newBuilder()
                                    .setStructValue(Struct.newBuilder()
                                            .putFields("$eq", Value.newBuilder()
                                                    .setStringValue(valueToQuery)
                                                    .build()))
                                    .build())
                            .build())
                    .build();
            QueryResponse queryResponse = blockingStub.query(queryRequest);
            // Verify the metadata field is correctly filtered in the query response
            assert (queryResponse.getMatches(0).getMetadata().getFieldsMap().get(fieldToQuery).toString().contains(valueToQuery));
        } catch (InterruptedException e) {
            // ignore
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
}
