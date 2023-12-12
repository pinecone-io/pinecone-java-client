package io.pinecone.integration.dataplane;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.grpc.StatusRuntimeException;
import io.pinecone.PineconeConnection;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.pinecone.helpers.BuildUpsertRequest.*;
import static io.pinecone.helpers.IndexManager.createIndexIfNotExistsDataPlane;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UpdateAndQueryTest {
    private static VectorServiceGrpc.VectorServiceBlockingStub blockingStub;
    private static VectorServiceGrpc.VectorServiceFutureStub futureStub;
    private static final int dimension = 3;

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException {
        PineconeConnection connection = createIndexIfNotExistsDataPlane(dimension);
        blockingStub = connection.getBlockingStub();
        futureStub = connection.getFutureStub();
    }

    @Test
    public void UpdateRequiredParamsFetchAndQuerySync() throws InterruptedException {
        // Upsert vectors with required parameters
        List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
        String namespace = RandomStringBuilder.build("ns", 8);
        blockingStub.upsert(buildRequiredUpsertRequest(upsertIds, namespace));
        Thread.sleep(3500);

        // Verify the upserted vector count with fetch
        FetchRequest fetchRequest = FetchRequest.newBuilder().addAllIds(upsertIds).setNamespace(namespace).build();
        FetchResponse fetchResponse = blockingStub.fetch(fetchRequest);
        assertEquals(fetchResponse.getVectorsCount(), upsertIds.size());
        for (String key : upsertIds) {
            assert (fetchResponse.containsVectors(key));
        }

        List<Float> updatedValues = Arrays.asList(101F, 102F, 103F);
        // Update required fields only
        UpdateRequest updateRequest = UpdateRequest.newBuilder()
                .setId(upsertIds.get(0))
                .setNamespace(namespace)
                .addAllValues(updatedValues)
                .build();

        blockingStub.update(updateRequest);

        // Query by ID to verify
        QueryRequest queryRequest = QueryRequest.newBuilder()
                .setId(upsertIds.get(0))
                .setNamespace(namespace)
                .setTopK(1)
                .setIncludeValues(true)
                .build();
        QueryResponse queryResponse = blockingStub.query(queryRequest);
        List<Float> queryResponseValues = queryResponse.getMatches(0).getValuesList();

        assertEquals(updatedValues.size(), queryResponseValues.size());
        int expectedValueSum = 0, actualValueSum = 0;
        for (int i = 0; i < updatedValues.size(); i++) {
            expectedValueSum += updatedValues.get(i);
            actualValueSum += queryResponseValues.get(i);
        }
        assertEquals(expectedValueSum, actualValueSum);
    }

    @Test
    public void UpdateAllParamsFetchAndQuerySync() throws InterruptedException {
        // Upsert vectors with required parameters
        List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
        String expectedId = upsertIds.get(0);
        String namespace = RandomStringBuilder.build("ns", 8);
        blockingStub.upsert(buildRequiredUpsertRequest(upsertIds, namespace));
        Thread.sleep(3500);

        // Verify the upserted vector count with fetch
        FetchRequest fetchRequest = FetchRequest.newBuilder().addAllIds(upsertIds).setNamespace(namespace).build();
        FetchResponse fetchResponse = blockingStub.fetch(fetchRequest);
        assertEquals(fetchResponse.getVectorsCount(), upsertIds.size());
        for (String key : upsertIds) {
            assert (fetchResponse.containsVectors(key));
        }

        List<Float> updatedValues = Arrays.asList(101F, 102F, 103F);
        HashMap<String, List<String>> metadataMap = createAndGetMetadataMap();
        Struct metadata = Struct.newBuilder()
                .putFields(metadataFields[0],
                        Value.newBuilder().setStringValue(metadataMap.get(metadataFields[0]).get(0)).build())
                .putFields(metadataFields[1],
                        Value.newBuilder().setStringValue(metadataMap.get(metadataFields[1]).get(0)).build())
                .build();

        SparseValues sparseVector = SparseValues
                .newBuilder()
                .addAllIndices(sparseIndices)
                .addAllValues(sparseValues)
                .build();

        // Update required fields only
        UpdateRequest updateRequest = UpdateRequest.newBuilder()
                .setId(expectedId)
                .setNamespace(namespace)
                .addValues(updatedValues.get(0))
                .addValues(updatedValues.get(1))
                .addValues(updatedValues.get(2))
                .setSparseValues(sparseVector)
                .setSetMetadata(metadata)
                .build();

        blockingStub.update(updateRequest);

        // Query by vector to verify
        QueryRequest queryRequest = QueryRequest.newBuilder()
                .setNamespace(namespace)
                .setTopK(5)
                .setIncludeValues(true)
                .setIncludeMetadata(true)
                .addAllVector(updatedValues)
                .build();
        QueryResponse queryResponse = blockingStub.query(queryRequest);
        // Returns the vector v1 at 0th index since its queryRequest's values are set to updatedValues ArrayList
        ScoredVector scoredVectorV1 = queryResponse.getMatches(0);
        assertEquals(scoredVectorV1.getId(), expectedId);

        // Verify the values are updated correctly
        List<Float> queryResponseValues = scoredVectorV1.getValuesList();
        assert (updatedValues.equals(queryResponseValues));

        // Verify the updated metadata
        assert (scoredVectorV1.getMetadata().equals(metadata));

        // Verify the updated sparse vector
        assert (scoredVectorV1.getSparseValues().equals(sparseVector));
    }

    @Test
    public void addIncorrectDimensionalValuesSync() throws InterruptedException {
        // Upsert vectors with required parameters
        List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
        String namespace = RandomStringBuilder.build("ns", 8);
        blockingStub.upsert(buildRequiredUpsertRequest(upsertIds, namespace));
        Thread.sleep(3500);

        // Verify the upserted vector count with fetch
        FetchRequest fetchRequest = FetchRequest.newBuilder().addAllIds(upsertIds).setNamespace(namespace).build();
        FetchResponse fetchResponse = blockingStub.fetch(fetchRequest);
        assertEquals(fetchResponse.getVectorsCount(), upsertIds.size());
        for (String key : upsertIds) {
            assert (fetchResponse.containsVectors(key));
        }

        // Update required fields only
        UpdateRequest updateRequest = UpdateRequest.newBuilder()
                .setId(upsertIds.get(0))
                .setNamespace(namespace)
                .addValues(100F)
                .build();

        // Should fail since only 1 value is added for the vector of dimension 3
        try {
            blockingStub.update(updateRequest);
        } catch (StatusRuntimeException runtimeException) {
            assert (runtimeException.getTrailers().toString().contains("status=200"));
            assert (runtimeException.getTrailers().toString().contains("Vector dimension 1 does not match the dimension of the index 3"));
        }
    }

    @Test
    public void UpdateRequiredParamsFetchAndQueryFuture() throws InterruptedException, ExecutionException {
        // Upsert vectors with required parameters
        List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
        String namespace = RandomStringBuilder.build("ns", 8);
        futureStub.upsert(buildRequiredUpsertRequest(upsertIds, namespace));
        Thread.sleep(3500);

        // Verify the upserted vector count with fetch
        FetchRequest fetchRequest = FetchRequest.newBuilder().addAllIds(upsertIds).setNamespace(namespace).build();
        FetchResponse fetchResponse = futureStub.fetch(fetchRequest).get();
        assertEquals(fetchResponse.getVectorsCount(), upsertIds.size());
        for (String key : upsertIds) {
            assert (fetchResponse.containsVectors(key));
        }

        List<Float> updatedValues = Arrays.asList(101F, 102F, 103F);
        // Update required fields only
        UpdateRequest updateRequest = UpdateRequest.newBuilder()
                .setId(upsertIds.get(0))
                .setNamespace(namespace)
                .addAllValues(updatedValues)
                .build();

        futureStub.update(updateRequest);

        // Query to verify
        QueryRequest queryRequest = QueryRequest.newBuilder()
                .setId(upsertIds.get(0))
                .setNamespace(namespace)
                .setTopK(1)
                .setIncludeValues(true)
                .build();
        QueryResponse queryResponse = futureStub.query(queryRequest).get();
        List<Float> queryResponseValues = queryResponse.getMatches(0).getValuesList();

        assertEquals(updatedValues.size(), queryResponseValues.size());
        int expectedValueSum = 0, actualValueSum = 0;
        for (int i = 0; i < updatedValues.size(); i++) {
            expectedValueSum += updatedValues.get(i);
            actualValueSum += queryResponseValues.get(i);
        }
        assertEquals(expectedValueSum, actualValueSum);
    }

    @Test
    public void addIncorrectDimensionalValuesFuture() throws InterruptedException, ExecutionException {
        // Upsert vectors with required parameters
        List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
        String namespace = RandomStringBuilder.build("ns", 8);
        futureStub.upsert(buildRequiredUpsertRequest(upsertIds, namespace));
        Thread.sleep(3500);

        // Verify the upserted vector count with fetch
        FetchRequest fetchRequest = FetchRequest.newBuilder().addAllIds(upsertIds).setNamespace(namespace).build();
        FetchResponse fetchResponse = futureStub.fetch(fetchRequest).get();
        assertEquals(fetchResponse.getVectorsCount(), upsertIds.size());
        for (String key : upsertIds) {
            assert (fetchResponse.containsVectors(key));
        }

        // Update required fields only
        UpdateRequest updateRequest = UpdateRequest.newBuilder()
                .setId(upsertIds.get(0))
                .setNamespace(namespace)
                .addValues(100F)
                .build();

        // Should fail since only 1 value is added for the vector of dimension 3
        try {
            futureStub.update(updateRequest);
        } catch (StatusRuntimeException runtimeException) {
            assert (runtimeException.getTrailers().toString().contains("status=200"));
            assert (runtimeException.getTrailers().toString().contains("Vector dimension 1 does not match the dimension of the index 3"));
        }
    }

    @Test
    public void UpdateAllParamsFetchAndQueryFuture() throws InterruptedException, ExecutionException {
        // Upsert vectors with required parameters
        List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
        String expectedId = upsertIds.get(0);
        String namespace = RandomStringBuilder.build("ns", 8);
        futureStub.upsert(buildRequiredUpsertRequest(upsertIds, namespace));
        Thread.sleep(3500);

        // Verify the upserted vector count with fetch
        FetchRequest fetchRequest = FetchRequest.newBuilder().addAllIds(upsertIds).setNamespace(namespace).build();
        FetchResponse fetchResponse = futureStub.fetch(fetchRequest).get();
        assertEquals(fetchResponse.getVectorsCount(), upsertIds.size());
        for (String key : upsertIds) {
            assert (fetchResponse.containsVectors(key));
        }

        List<Float> updatedValues = Arrays.asList(101F, 102F, 103F);
        HashMap<String, List<String>> metadataMap = createAndGetMetadataMap();
        Struct metadata = Struct.newBuilder()
                .putFields(metadataFields[0],
                        Value.newBuilder().setStringValue(metadataMap.get(metadataFields[0]).get(0)).build())
                .putFields(metadataFields[1],
                        Value.newBuilder().setStringValue(metadataMap.get(metadataFields[1]).get(0)).build())
                .build();

        SparseValues sparseVector = SparseValues
                .newBuilder()
                .addAllIndices(sparseIndices)
                .addAllValues(sparseValues)
                .build();

        // Update required fields only
        UpdateRequest updateRequest = UpdateRequest.newBuilder()
                .setId(expectedId)
                .setNamespace(namespace)
                .addValues(updatedValues.get(0))
                .addValues(updatedValues.get(1))
                .addValues(updatedValues.get(2))
                .setSparseValues(sparseVector)
                .setSetMetadata(metadata)
                .build();

        futureStub.update(updateRequest);

        // Query by vector to verify
        QueryRequest queryRequest = QueryRequest.newBuilder()
                .setNamespace(namespace)
                .setTopK(5)
                .setIncludeValues(true)
                .setIncludeMetadata(true)
                .addAllVector(updatedValues)
                .build();
        QueryResponse queryResponse = futureStub.query(queryRequest).get();
        // Returns the vector v1 at 0th index since its queryRequest's values are set to updatedValues ArrayList
        ScoredVector scoredVectorV1 = queryResponse.getMatches(0);
        assertEquals(scoredVectorV1.getId(), expectedId);

        // Verify the values are updated correctly
        List<Float> queryResponseValues = scoredVectorV1.getValuesList();
        assert (updatedValues.equals(queryResponseValues));

        // Verify the updated metadata
        assert (scoredVectorV1.getMetadata().equals(metadata));

        // Verify the updated sparse vector
        assert (scoredVectorV1.getSparseValues().equals(sparseVector));
    }


    // ToDo: Order the test
    @Test
    public void UpsertRequiredParamsVectorAndQueryByIncorrectVectorDimensionSync() throws InterruptedException {
        // Upsert vectors with required parameters
        List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
        String namespace = RandomStringBuilder.build("ns", 8);
        blockingStub.upsert(buildRequiredUpsertRequest(upsertIds, namespace));
        Thread.sleep(3500);

        QueryRequest queryRequest = QueryRequest.newBuilder()
                .setNamespace(namespace)
                .setTopK(5)
                .setIncludeValues(true)
                .setIncludeMetadata(true)
                .addVector(100F)
                .build();

        try {
            blockingStub.query(queryRequest);
        } catch (Exception e) {
            // ToDo: Verify the exception
        }
    }

    @Test
    public void UpsertRequiredParamsVectorAndQueryByIncorrectVectorDimensionFuture() throws InterruptedException {
        // Upsert vectors with required parameters
        List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
        String namespace = RandomStringBuilder.build("ns", 8);
        futureStub.upsert(buildRequiredUpsertRequest(upsertIds, namespace));
        Thread.sleep(3500);

        QueryRequest queryRequest = QueryRequest.newBuilder()
                .setNamespace(namespace)
                .setTopK(5)
                .setIncludeValues(true)
                .setIncludeMetadata(true)
                .addVector(100F)
                .build();

        try {
            futureStub.query(queryRequest);
        } catch (Exception e) {
            // ToDo: Verify the exception
        }
    }

    // 1.
    // Upsert and update required fields only:
    // Upsert with id, namespace, and values
    // Update the values with id and namespace
    // query with topK=upsertedVectorCount to confirm the values with id and namespace

    // 2.
    // Upsert with required values and update req+optional fields test:
    // Upsert with id, namespace, and values
    // Update the optional values by adding metadata, sparse vectors, and all optional values
    // query with topK=upsertedVectorCount to confirm the updated fields

    // 3.
    // Required + Optional values:
    // Upsert with id, namespace, values, metadata, sparse vectors, and all optional values
    // Update the all values
    // query with topK=upsertedVectorCount to confirm the values with id and namespace

    // 4.
    // ToDo: query with filter

    // Failing tests
}
