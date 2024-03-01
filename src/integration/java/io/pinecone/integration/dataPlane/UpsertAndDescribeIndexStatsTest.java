package io.pinecone.integration.dataPlane;

import com.google.protobuf.Struct;
import io.pinecone.*;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.*;
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
    private static VectorServiceGrpc.VectorServiceBlockingStub blockingStub;
    private static VectorServiceGrpc.VectorServiceFutureStub futureStub;
    private static final int dimension = 3;
    private static final Struct emptyFilterStruct = Struct.newBuilder().build();

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
    public void upsertRequiredVectorsAndDescribeIndexStatsSyncTest() throws InterruptedException {
        // Get vector count before upserting vectors with required parameters
        int numOfVectors = 3;
        PineconeBlockingDataPlaneClient dataPlaneClient = new PineconeBlockingDataPlaneClient(blockingStub);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = dataPlaneClient.describeIndexStats(emptyFilterStruct);
        // Confirm the starting state by verifying the dimension of the index
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);
        int vectorCount = describeIndexStatsResponse1.getTotalVectorCount();

        // Upsert vectors with required parameters
        List<String> upsertIds = getIdsList(numOfVectors);
        for (String id : upsertIds) {
            UpsertResponse upsertResponse = dataPlaneClient.upsert(id, generateVectorValuesByDimension(dimension));
            vectorCount += upsertResponse.getUpsertedCount();
        }
        int actualVectorCount = vectorCount;

        assertWithRetry(() -> {
            // call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse2 = dataPlaneClient.describeIndexStats(emptyFilterStruct);

            // verify the updated vector count
            assertEquals(describeIndexStatsResponse2.getTotalVectorCount(), actualVectorCount);
        });
    }

    @Test
    public void upsertOptionalVectorsAndDescribeIndexStatsSyncTest() throws InterruptedException {
        int numOfVectors = 5;
        PineconeBlockingDataPlaneClient dataPlaneClient = new PineconeBlockingDataPlaneClient(blockingStub);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = dataPlaneClient.describeIndexStats(emptyFilterStruct);
        // Confirm the starting state by verifying the dimension of the index
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);
        int vectorCount = describeIndexStatsResponse1.getTotalVectorCount();

        // upsert vectors with required + optional parameters
        List<String> upsertIds = getIdsList(numOfVectors);
        String namespace = RandomStringBuilder.build("ns", 8);
        for (String id : upsertIds) {
            UpsertResponse upsertResponse = dataPlaneClient.upsert(id,
                    generateVectorValuesByDimension(dimension),
                    generateSparseIndicesByDimension(dimension),
                    generateVectorValuesByDimension(dimension),
                    generateMetadataStruct(),
                    namespace);
            vectorCount += upsertResponse.getUpsertedCount();
        }
        int actualVectorCount = vectorCount;
        assertWithRetry(() -> {
            // call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse2 = dataPlaneClient.describeIndexStats(emptyFilterStruct);

            // verify updated vector count
            assertEquals(describeIndexStatsResponse2.getTotalVectorCount(), actualVectorCount);
        });
    }

    @Test
    public void upsertNullSparseIndicesNotNullSparseValuesSyncTest() {
        PineconeBlockingDataPlaneClient dataPlaneClient = new PineconeBlockingDataPlaneClient(blockingStub);
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
    public void upsertRequiredVectorsAndDescribeIndexStatsFutureTest() throws InterruptedException, ExecutionException {
        // Get vector count before upserting vectors with required parameters
        int numOfVectors = 3;
        PineconeFutureDataPlaneClient dataPlaneClient = new PineconeFutureDataPlaneClient(futureStub);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = dataPlaneClient.describeIndexStats(emptyFilterStruct).get();
        // Confirm the starting state by verifying the dimension of the index
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);
        int vectorCount = describeIndexStatsResponse1.getTotalVectorCount();

        // Upsert vectors with required parameters
        List<String> upsertIds = getIdsList(numOfVectors);
        for (String id : upsertIds) {
            UpsertResponse upsertResponse = dataPlaneClient.upsert(id, generateVectorValuesByDimension(dimension)).get();
            vectorCount += upsertResponse.getUpsertedCount();
        }
        int actualVectorCount = vectorCount;

        assertWithRetry(() -> {
            // call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse2 = dataPlaneClient.describeIndexStats(emptyFilterStruct).get();

            // verify the updated vector count
            assertEquals(describeIndexStatsResponse2.getTotalVectorCount(), actualVectorCount);
        });
    }

    @Test
    public void UpsertOptionalVectorsAndDescribeIndexStatsFutureTest() throws InterruptedException {
        int numOfVectors = 5;
        PineconeBlockingDataPlaneClient dataPlaneClient = new PineconeBlockingDataPlaneClient(blockingStub);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = dataPlaneClient.describeIndexStats(emptyFilterStruct);
        // Confirm the starting state by verifying the dimension of the index
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);
        int vectorCount = describeIndexStatsResponse1.getTotalVectorCount();

        // upsert vectors with required + optional parameters
        List<String> upsertIds = getIdsList(numOfVectors);
        String namespace = RandomStringBuilder.build("ns", 8);
        for (String id : upsertIds) {
            UpsertResponse upsertResponse = dataPlaneClient.upsert(id,
                    generateVectorValuesByDimension(dimension),
                    generateSparseIndicesByDimension(dimension),
                    generateVectorValuesByDimension(dimension),
                    generateMetadataStruct(),
                    namespace);
            vectorCount += upsertResponse.getUpsertedCount();
        }
        int actualVectorCount = vectorCount;
        assertWithRetry(() -> {
            // call describeIndexStats to get updated vector count
            DescribeIndexStatsResponse describeIndexStatsResponse2 = dataPlaneClient.describeIndexStats(emptyFilterStruct);

            // verify updated vector count
            assertEquals(describeIndexStatsResponse2.getTotalVectorCount(), actualVectorCount);
        });
    }


    @Test
    public void upsertNullSparseIndicesNotNullSparseValuesFutureTest() {
        PineconeFutureDataPlaneClient dataPlaneClient = new PineconeFutureDataPlaneClient(futureStub);
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
