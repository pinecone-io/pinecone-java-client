package io.pinecone.integration.dataPlane;

import io.grpc.StatusRuntimeException;
import io.pinecone.clients.Index;
import io.pinecone.configs.PineconeConnection;
import io.pinecone.clients.AsyncIndex;
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

public class QueryErrorPodTest {

    private static PineconeConnection connection;
    private static final int dimension = 3;

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException {
        connection = createIndexIfNotExistsDataPlane(dimension, IndexModelSpec.SERIALIZED_NAME_POD);
    }

    @AfterAll
    public static void cleanUp() {
        connection.close();
    }

    @Test
    public void queryWithIncorrectVectorDimensionSync() {
        String namespace = RandomStringBuilder.build("ns", 8);
        Index index = new Index(connection);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = index.describeIndexStats(null);
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);

        // Query with incorrect dimensions
        try {
            List<Float> vector = Arrays.asList(100F);
            index.query(5, vector, null, null, null, namespace, null, true, true);
        } catch (StatusRuntimeException statusRuntimeException) {
            assert (statusRuntimeException.getLocalizedMessage().contains("grpc-status=3"));
            assert (statusRuntimeException.getLocalizedMessage().contains("grpc-message=Query vector dimension 1 does not match the dimension of the index 3"));
        }
    }

    @Test
    public void QueryWithNullSparseIndicesNotNullSparseValuesSyncTest() {
        Index index = new Index(connection);
        String id = RandomStringBuilder.build(3);

        try {
            index.update(id,
                    generateVectorValuesByDimension(dimension),
                    null,
                    null,
                    null,
                    generateVectorValuesByDimension(dimension));
        } catch (PineconeValidationException validationException) {
            assertEquals(validationException.getLocalizedMessage(), "Invalid upsert request. Please ensure that both sparse indices and values are present.");
        }
    }

    @Test
    public void queryWithIncorrectVectorDimensionFuture() throws ExecutionException, InterruptedException {
        String namespace = RandomStringBuilder.build("ns", 8);
        AsyncIndex asyncIndex = new AsyncIndex(connection);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = asyncIndex.describeIndexStats(null).get();
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);

        // Query with incorrect dimensions
        try {
            List<Float> vector = Arrays.asList(100F);
            asyncIndex.query(5, vector, null, null, null, namespace, null, true, true).get();
        } catch (ExecutionException executionException) {
            assert (executionException.getLocalizedMessage().contains("grpc-status=3"));
            assert (executionException.getLocalizedMessage().contains("grpc-message=Query vector dimension 1 does not match the dimension of the index 3"));
        }
    }

    @Test
    public void QueryWithNullSparseIndicesNotNullSparseValuesFutureTest() throws ExecutionException, InterruptedException {
        AsyncIndex asyncIndex = new AsyncIndex(connection);
        String id = RandomStringBuilder.build(3);

        try {
            asyncIndex.update(id,
                    generateVectorValuesByDimension(dimension),
                    null,
                    null,
                    null,
                    generateVectorValuesByDimension(dimension)).get();
        } catch (PineconeValidationException validationException) {
            assertEquals(validationException.getLocalizedMessage(), "Invalid upsert request. Please ensure that both sparse indices and values are present.");
        }
    }
}
