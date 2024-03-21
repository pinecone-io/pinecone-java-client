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