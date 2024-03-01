package io.pinecone.integration.dataPlane;

import io.grpc.StatusRuntimeException;
import io.pinecone.PineconeBlockingDataPlaneClient;
import io.pinecone.PineconeConnection;
import io.pinecone.PineconeFutureDataPlaneClient;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.IndexModelSpec;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.pinecone.helpers.BuildUpsertRequest.*;
import static io.pinecone.helpers.IndexManager.createIndexIfNotExistsDataPlane;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class QueryErrorTest {

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
    public void queryWithIncorrectVectorDimensionSync() {
        int numOfVectors = 3;

        String namespace = RandomStringBuilder.build("ns", 8);
        PineconeBlockingDataPlaneClient dataPlaneClient = new PineconeBlockingDataPlaneClient(blockingStub);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = dataPlaneClient.describeIndexStats(null);
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);

        // Query with incorrect dimensions
        try {
            List<Float> vector = Arrays.asList(100F);
            dataPlaneClient.query(5, vector, null, null, null, namespace, null, true, true);
        } catch (StatusRuntimeException statusRuntimeException) {
            assert (statusRuntimeException.getTrailers().toString().contains("grpc-status=3"));
            assert (statusRuntimeException.getTrailers().toString().contains("grpc-message=Query vector dimension 1 does not match the dimension of the index 3"));
        }
    }

    @Test
    public void QueryWithNullSparseIndicesNotNullSparseValuesSyncTest() {
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

    // ToDo: @Test public void queryWithIncorrectVectorDimensionFuture()
    @Test
    public void queryWithIncorrectVectorDimensionFuture() throws ExecutionException, InterruptedException {
        int numOfVectors = 3;

        String namespace = RandomStringBuilder.build("ns", 8);
        PineconeFutureDataPlaneClient dataPlaneClient = new PineconeFutureDataPlaneClient(futureStub);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = dataPlaneClient.describeIndexStats(null).get();
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);

        // Query with incorrect dimensions
        try {
            List<Float> vector = Arrays.asList(100F);
            dataPlaneClient.query(5, vector, null, null, null, namespace, null, true, true);
        } catch (StatusRuntimeException statusRuntimeException) {
            assert (statusRuntimeException.getTrailers().toString().contains("grpc-status=3"));
            assert (statusRuntimeException.getTrailers().toString().contains("grpc-message=Query vector dimension 1 does not match the dimension of the index 3"));
        }
    }

    @Test
    public void QueryWithNullSparseIndicesNotNullSparseValuesFutureTest() {
        PineconeFutureDataPlaneClient dataPlaneClient = new PineconeFutureDataPlaneClient(futureStub);
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
}
