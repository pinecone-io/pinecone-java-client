package io.pinecone.integration.dataPlane;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.StatusRuntimeException;
import io.pinecone.clients.AsyncIndex;
import io.pinecone.clients.Index;
import io.pinecone.configs.PineconeConnection;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.DescribeIndexStatsResponse;
import io.pinecone.proto.VectorServiceGrpc;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.pinecone.helpers.BuildUpsertRequest.generateSparseIndicesByDimension;
import static io.pinecone.helpers.BuildUpsertRequest.generateVectorValuesByDimension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QueryErrorTest {

    private static Index index;
    private static AsyncIndex asyncIndex;
    private static final int dimension = 3;

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException {
        PineconeConnection connectionMock = mock(PineconeConnection.class);

        VectorServiceGrpc.VectorServiceBlockingStub stubMock = mock(VectorServiceGrpc.VectorServiceBlockingStub.class);
        VectorServiceGrpc.VectorServiceFutureStub asyncStubMock = mock(VectorServiceGrpc.VectorServiceFutureStub.class);

        when(connectionMock.getBlockingStub()).thenReturn(stubMock);
        when(connectionMock.getFutureStub()).thenReturn(asyncStubMock);

        index = new Index(connectionMock);
        asyncIndex = new AsyncIndex(connectionMock);
    }

    @Test
    public void queryWithVectorAndIdSyncTest() {
        List<Float> values = generateVectorValuesByDimension(3);
        try {
            index.query(5, values, null, null, "some_vector_id", "namespace", null, true, true);

            fail("Expected to throw PineconeValidationException");
        } catch (PineconeValidationException expected) {
            assertTrue(expected.getLocalizedMessage().contains("Cannot query with both vector id and vector values."));
        }
    }

    @Disabled("disable server-side validations only")
    @Test
    public void queryWithIncorrectVectorDimensionSync() {
        String namespace = RandomStringBuilder.build("ns", 8);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = index.describeIndexStats(null);
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);

        // Query with incorrect dimensions
        try {
            List<Float> vector = Arrays.asList(100F);
            index.query(5, vector, null, null, null, namespace, null, true, true);

            fail("Expected to throw StatusRuntimeException");
        } catch (StatusRuntimeException expected) {
            assertTrue(expected.getLocalizedMessage().contains("grpc-status=3"));
            assertTrue(expected.getLocalizedMessage().contains("grpc-message=Query vector dimension 1 does not match the dimension of the index 3"));
        }
    }

    @Test
    public void queryWithNullSparseIndicesNotNullSparseValuesSyncTest() {
        List<Float> sparseValues = generateVectorValuesByDimension(3);

        try {
            index.query(5, null, null, sparseValues, "some_vector_id", "namespace", null, false, false);

            fail("Expected to throw PineconeValidationException");
        } catch (PineconeValidationException expected) {
            assertTrue(expected.getLocalizedMessage().contains("ensure that both sparse indices and values are present"));
        }
    }

    @Test
    public void queryWithNotNullSparseIndicesNullSparseValuesSyncTest() {
        List<Long> sparseIndices = generateSparseIndicesByDimension(3);

        try {
            index.query(5, null, sparseIndices, null, "some_vector_id", "namespace", null, false, false);

            fail("Expected to throw PineconeValidationException");
        } catch (PineconeValidationException expected) {
            assertTrue(expected.getLocalizedMessage().contains("ensure that both sparse indices and values are present"));
        }
    }

    @Test
    public void queryWithVectorAndIdFutureTest() {
        List<Float> values = generateVectorValuesByDimension(3);
        try {
            index.query(5, values, null, null, "some_vector_id", "namespace", null, true, true);

            fail("Expected to throw PineconeValidationException");
        } catch (PineconeValidationException expected) {
            assertTrue(expected.getLocalizedMessage().contains("Cannot query with both vector id and vector values."));
        }
    }

    @Disabled("disable server-side validations")
    @Test
    public void queryWithIncorrectVectorDimensionFutureTest() throws ExecutionException, InterruptedException, TimeoutException {
        String namespace = RandomStringBuilder.build("ns", 8);
        DescribeIndexStatsResponse describeIndexStatsResponse1 = asyncIndex.describeIndexStats(null).get();
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);

        // Query with incorrect dimensions
        try {
            List<Float> vector = Arrays.asList(100F);
            ListenableFuture<QueryResponseWithUnsignedIndices> queryFuture = asyncIndex.query(5, vector, null, null, null, namespace, null, true, true);
            queryFuture.get(10, TimeUnit.SECONDS);

            fail("Expected to throw ExecutionException");
        } catch (ExecutionException expected) {
            assertTrue(expected.getLocalizedMessage().contains("grpc-status=3"));
            assertTrue(expected.getLocalizedMessage().contains("grpc-message=Query vector dimension 1 does not match the dimension of the index 3"));
        }
    }

    @Test
    public void queryWithNullSparseIndicesNotNullSparseValuesFutureTest() {
        List<Float> sparseValues = generateVectorValuesByDimension(3);

        try {
            asyncIndex.query(5, null, null, sparseValues, "some_vector_id", "namespace", null, false, false);

            fail("Expected to throw PineconeValidationException");
        } catch (PineconeValidationException expected) {
            assertTrue(expected.getLocalizedMessage().contains("ensure that both sparse indices and values are present"));
        }
    }

    @Test
    public void queryWithNotNullSparseIndicesNullSparseValuesFutureTest() {
        List<Long> sparseIndices = generateSparseIndicesByDimension(3);

        try {
            asyncIndex.query(5, null, sparseIndices, null, "some_vector_id", "namespace", null, false, false);

            fail("Expected to throw PineconeValidationException");
        } catch (PineconeValidationException expected) {
            assertTrue(expected.getLocalizedMessage().contains("ensure that both sparse indices and values are present"));
        }
    }
}