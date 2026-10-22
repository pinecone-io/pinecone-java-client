package io.pinecone.clients;

import io.pinecone.configs.PineconeConfig;
import io.pinecone.configs.PineconeConnection;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.VectorServiceGrpc;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.pinecone.helpers.BuildUpsertRequest.generateVectorValuesByDimension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UpdateErrorTest {

    private static Index index;
    private static AsyncIndex asyncIndex;

    @BeforeAll
    public static void setUp() {
        PineconeConfig config = mock(PineconeConfig.class);
        PineconeConnection connectionMock = mock(PineconeConnection.class);

        VectorServiceGrpc.VectorServiceBlockingStub stubMock = mock(VectorServiceGrpc.VectorServiceBlockingStub.class);
        VectorServiceGrpc.VectorServiceFutureStub asyncStubMock = mock(VectorServiceGrpc.VectorServiceFutureStub.class);

        when(connectionMock.getBlockingStub()).thenReturn(stubMock);
        when(connectionMock.getAsyncStub()).thenReturn(asyncStubMock);

        index = new Index(config, connectionMock, "some-index-name");
        asyncIndex = new AsyncIndex(config, connectionMock, "some-index-name");
    }

    @Test
    public void updateNullSparseIndicesNotNullSparseValuesSyncTest() {
        String id = RandomStringBuilder.build(3);
        int dimension = 3;

        try {
            index.update(id,
                    generateVectorValuesByDimension(dimension),
                    null,
                    null,
                    null,
                    generateVectorValuesByDimension(dimension));

            fail("Expected to throw PineconeValidationException");
        } catch (PineconeValidationException validationException) {
            assertTrue(validationException.toString().contains("Invalid upsert request. Please ensure that both sparse indices and values are present."));
        }
    }

    @Test
    public void updateNullSparseIndicesNotNullSparseValuesFutureTest() {
        String id = RandomStringBuilder.build(3);
        int dimension = 3;

        try {
            asyncIndex.update(id,
                    generateVectorValuesByDimension(dimension),
                    null,
                    null,
                    null,
                    generateVectorValuesByDimension(dimension));

            fail("Expected to throw PineconeValidationException");
        } catch (PineconeValidationException validationException) {
            assertTrue(validationException.toString().contains("Invalid upsert request. Please ensure that both sparse indices and values are present."));
        }
    }
}
