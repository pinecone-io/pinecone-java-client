package io.pinecone.integration.dataPlane;

import io.grpc.StatusRuntimeException;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
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
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.pinecone.helpers.BuildUpsertRequest.*;
import static io.pinecone.helpers.IndexManager.createIndexIfNotExistsDataPlane;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class QueryErrorPodTest {

    private static Pinecone pineconeClient;
    private static String indexName;
    private static Index indexClient;
    private static AsyncIndex asyncIndexClient;

    private static final int dimension = 3;

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException {
        AbstractMap.SimpleEntry<String, Pinecone> indexAndClient = createIndexIfNotExistsDataPlane(dimension, IndexModelSpec.SERIALIZED_NAME_POD);
        indexName = indexAndClient.getKey();
        pineconeClient = indexAndClient.getValue();
        indexClient = pineconeClient.createIndexConnection(indexName);
        asyncIndexClient = pineconeClient.createAsyncIndexConnection(indexName);
    }

    @Test
    public void queryWithIncorrectVectorDimensionSync() {
        String namespace = RandomStringBuilder.build("ns", 8);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = indexClient.describeIndexStats(null);
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);

        // Query with incorrect dimensions
        try {
            List<Float> vector = Arrays.asList(100F);
            indexClient.query(5, vector, null, null, null, namespace, null, true, true);
        } catch (StatusRuntimeException statusRuntimeException) {
            assert (statusRuntimeException.getLocalizedMessage().contains("grpc-status=3"));
            assert (statusRuntimeException.getLocalizedMessage().contains("grpc-message=Query vector dimension 1 does not match the dimension of the index 3"));
        }
    }

    @Test
    public void QueryWithNullSparseIndicesNotNullSparseValuesSyncTest() {
        String id = RandomStringBuilder.build(3);

        try {
            indexClient.update(id,
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
        DescribeIndexStatsResponse describeIndexStatsResponse1 = asyncIndexClient.describeIndexStats(null).get();
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);

        // Query with incorrect dimensions
        try {
            List<Float> vector = Arrays.asList(100F);
            asyncIndexClient.query(5, vector, null, null, null, namespace, null, true, true).get();
        } catch (ExecutionException executionException) {
            assert (executionException.getLocalizedMessage().contains("grpc-status=3"));
            assert (executionException.getLocalizedMessage().contains("grpc-message=Query vector dimension 1 does not match the dimension of the index 3"));
        }
    }

    @Test
    public void QueryWithNullSparseIndicesNotNullSparseValuesFutureTest() throws ExecutionException, InterruptedException {
        String id = RandomStringBuilder.build(3);

        try {
            asyncIndexClient.update(id,
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
