package io.pinecone.integration.dataPlane.pod;

import com.google.protobuf.Struct;
import io.pinecone.clients.Index;
import io.pinecone.clients.AsyncIndex;
import io.pinecone.configs.PineconeConnection;
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
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class UpsertAndDescribeIndexStatsTest {
    private static PineconeConnection connection;
    private static final int dimension = 3;
    private static final Struct emptyFilterStruct = Struct.newBuilder().build();

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException {
        connection = createIndexIfNotExistsDataPlane(dimension, IndexModelSpec.SERIALIZED_NAME_POD);}

    @AfterAll
    public static void cleanUp() {
        connection.close();
    }

    @Test
    public void upsertOptionalVectorsAndQueryIndexSyncTest() throws InterruptedException {
        int numOfVectors = 5;
        Index dataPlaneClient = new Index(connection);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = dataPlaneClient.describeIndexStats(emptyFilterStruct);
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
            UpsertResponse upsertResponse = dataPlaneClient.upsert(id,
                    values,
                    sparseIndices,
                    sparseValues,
                    metadataStruct,
                    namespace);
        }

        // Query by vector to verify
        assertWithRetry(() -> {
            QueryResponseWithUnsignedIndices queryResponse = dataPlaneClient.query(
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
        Index dataPlaneClient = new Index(connection);
        String id = RandomStringBuilder.build(3);
        try {
            dataPlaneClient.upsert(id,
                    generateVectorValuesByDimension(dimension),
                    null,
                    generateVectorValuesByDimension(dimension),
                    null,
                    null);
        } catch (PineconeValidationException validationException) {
            assertEquals(validationException.getLocalizedMessage(), "Invalid upsert request. Please ensure that both sparse indices and values are present.");
        }
    }

    @Test
    public void upsertOptionalVectorsAndQueryIndexFutureTest() throws InterruptedException, ExecutionException {
        int numOfVectors = 5;
        AsyncIndex dataPlaneClient = new AsyncIndex(connection);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = dataPlaneClient.describeIndexStats(emptyFilterStruct).get();
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
            UpsertResponse upsertResponse = dataPlaneClient.upsert(id,
                    values,
                    sparseIndices,
                    sparseValues,
                    metadataStruct,
                    namespace).get();
        }

        // Query by vector to verify
        assertWithRetry(() -> {
            QueryResponseWithUnsignedIndices queryResponse = dataPlaneClient.query(
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
    public void upsertNullSparseIndicesNotNullSparseValuesFutureTest() {
        AsyncIndex dataPlaneClient = new AsyncIndex(connection);
        String id = RandomStringBuilder.build(3);
        try {
            dataPlaneClient.upsert(id,
                    generateVectorValuesByDimension(dimension),
                    null,
                    generateVectorValuesByDimension(dimension),
                    null,
                    null).get();
        } catch (PineconeValidationException validationException) {
            assertEquals(validationException.getLocalizedMessage(), "Invalid upsert request. Please ensure that both sparse indices and values are present.");
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
