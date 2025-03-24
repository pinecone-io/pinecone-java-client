package io.pinecone.integration.dataPlane;

import io.pinecone.clients.AsyncIndex;
import io.pinecone.clients.Index;
import io.pinecone.configs.PineconeConfig;
import io.pinecone.configs.PineconeConnection;
import io.pinecone.exceptions.PineconeException;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.VectorServiceGrpc;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static io.pinecone.helpers.BuildUpsertRequest.generateSparseIndicesByDimension;
import static io.pinecone.helpers.BuildUpsertRequest.generateVectorValuesByDimension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UpsertErrorTest {

    private static Index index;
    private static AsyncIndex asyncIndex;

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException {
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
    public void upsertWithApiKeyMissingSyncTest() {
        List<Float> values = Arrays.asList(1f, 2f, 3f);
        try {
            index.upsert(null, values);
            fail("Expecting invalid upsert request exception");
        } catch (PineconeException expected) {
            assertEquals(expected.getMessage(), "Invalid upsert request. Please ensure that id is provided.");
        }
    }

    @Test
    public void upsertWithNullSparseIndicesNotNullSparseValuesSyncTest() {
        String id = RandomStringBuilder.build(3);
        int dimension = 3;

        try {
            index.upsert(id,
                    generateVectorValuesByDimension(dimension),
                    null,
                    generateVectorValuesByDimension(dimension),
                    null,
                    "namespace");

            fail("Expected to throw PineconeValidationException");
        } catch (PineconeValidationException expected) {
            assertTrue(expected.getLocalizedMessage().contains("ensure that both sparse indices and values are present"));
        }
    }

    @Test
    public void upsertWithNotNullSparseIndicesNullSparseValuesSyncTest() {
        String id = RandomStringBuilder.build(3);
        int dimension = 3;

        try {
            index.upsert(id,
                    generateVectorValuesByDimension(dimension),
                    generateSparseIndicesByDimension(dimension),
                    null,
                    null,
                    "namespace");

            fail("Expected to throw PineconeValidationException");
        } catch (PineconeValidationException expected) {
            assertTrue(expected.getLocalizedMessage().contains("ensure that both sparse indices and values are present"));
        }
    }

    @Test
    public void upsertWithIncorrectDimensionOfSparseIndicesAndValuesSyncTest() {
        String id = RandomStringBuilder.build(3);
        int dimension = 3;

        try {
            index.upsert(id,
                    generateVectorValuesByDimension(dimension),
                    generateSparseIndicesByDimension(dimension),
                    generateVectorValuesByDimension(dimension + 1),
                    null,
                    "namespace");

            fail("Expected to throw PineconeValidationException");
        } catch (PineconeValidationException expected) {
            assertTrue(expected.getMessage().contains("ensure that both sparse indices and values are of the same length."));
        }
    }

    @Test
    public void upsertWithApiKeyMissingFutureTest() {
        List<Float> values = Arrays.asList(1f, 2f, 3f);
        try {
            asyncIndex.upsert(null, values);
            fail("Expecting invalid upsert request exception");
        } catch (PineconeException expected) {
            assertTrue(expected.getMessage().contains("ensure that id is provided."));
        }
    }

    @Test
    public void upsertWithNullSparseIndicesNotNullSparseValuesFutureTest() {
        String id = RandomStringBuilder.build(3);
        int dimension = 3;

        try {
            asyncIndex.upsert(id,
                    generateVectorValuesByDimension(dimension),
                    null,
                    generateVectorValuesByDimension(dimension),
                    null,
                    "namespace");

            fail("Expected to throw PineconeValidationException");
        } catch (PineconeValidationException expected) {
            assertTrue(expected.getLocalizedMessage().contains("ensure that both sparse indices and values are present"));
        }
    }

    @Test
    public void upsertWithNotNullSparseIndicesNullSparseValuesFutureTest() {
        String id = RandomStringBuilder.build(3);
        int dimension = 3;

        try {
            asyncIndex.upsert(id,
                    generateVectorValuesByDimension(dimension),
                    generateSparseIndicesByDimension(dimension),
                    null,
                    null,
                    "namespace");

            fail("Expected to throw PineconeValidationException");
        } catch (PineconeValidationException expected) {
            assertTrue(expected.getLocalizedMessage().contains("ensure that both sparse indices and values are present"));
        }
    }

    @Test
    public void upsertWithIncorrectDimensionOfSparseIndicesAndValuesFutureTest() {
        String id = RandomStringBuilder.build(3);
        int dimension = 3;

        try {
            index.upsert(id,
                    generateVectorValuesByDimension(dimension),
                    generateSparseIndicesByDimension(dimension),
                    generateVectorValuesByDimension(dimension + 1),
                    null,
                    "namespace");

            fail("Expected to throw PineconeValidationException");
        } catch (PineconeValidationException expected) {
            assertTrue(expected.getMessage().contains("ensure that both sparse indices and values are of the same length."));
        }
    }
}
