package io.pinecone.integration.dataPlane;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.StatusRuntimeException;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.clients.AsyncIndex;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.*;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.IndexModelSpec;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.pinecone.helpers.BuildUpsertRequest.*;
import static io.pinecone.helpers.IndexManager.createIndexIfNotExistsDataPlane;
import static org.junit.jupiter.api.Assertions.*;

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

            fail("queryWithIncorrectVectorDimensionSync should have thrown StatusRuntimeException");
        } catch (StatusRuntimeException expected) {
            assertTrue(expected.getLocalizedMessage().contains("grpc-status=3"));
            assertTrue(expected.getLocalizedMessage().contains("grpc-message=Query vector dimension 1 does not match the dimension of the index 3"));
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

            fail("QueryWithNullSparseIndicesNotNullSparseValuesSyncTest should have thrown PineconeValidationException");
        } catch (PineconeValidationException expected) {
            assertTrue(expected.getLocalizedMessage().contains( "ensure that both sparse indices and values are present"));
        }
    }

    @Test
    public void queryWithIncorrectVectorDimensionFuture() throws ExecutionException, InterruptedException, TimeoutException {
        String namespace = RandomStringBuilder.build("ns", 8);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = asyncIndexClient.describeIndexStats(null).get();
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);

        // Query with incorrect dimensions
        try {
            List<Float> vector = Arrays.asList(100F);
            ListenableFuture<QueryResponseWithUnsignedIndices> queryFuture = asyncIndexClient.query(5, vector, null, null, null, namespace, null, true, true);
            queryFuture.get(10, TimeUnit.SECONDS);

            fail("queryWithIncorrectVectorDimensionFuture should have thrown ExecutionException");
        } catch (ExecutionException expected) {
            assertTrue(expected.getLocalizedMessage().contains("grpc-status=3"));
            assertTrue(expected.getLocalizedMessage().contains("grpc-message=Query vector dimension 1 does not match the dimension of the index 3"));
        }
    }

    @Test
    public void QueryWithNullSparseIndicesNotNullSparseValuesFutureTest() throws ExecutionException, InterruptedException, TimeoutException {
        String id = RandomStringBuilder.build(3);

        try {
            ListenableFuture<UpdateResponse> updateFuture = asyncIndexClient.update(id,
                    generateVectorValuesByDimension(dimension),
                    null,
                    null,
                    null,
                    generateVectorValuesByDimension(dimension));

            updateFuture.get(10, TimeUnit.SECONDS);

            fail("QueryWithNullSparseIndicesNotNullSparseValuesFutureTest should have thrown PineconeValidationException");
        } catch (PineconeValidationException expected) {
            assertTrue(expected.getLocalizedMessage().contains("ensure that both sparse indices and values are present"));
        }
    }
}
