package io.pinecone;

import io.pinecone.clients.Index;
import io.pinecone.configs.PineconeConfig;
import io.pinecone.configs.PineconeConnection;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.proto.VectorServiceGrpc;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ListEndpointValidationTest {

    private Index index;

    @BeforeAll
    public void setUp() {
        String indexName = "test-index";

        // Mock sync Pinecone connection
        PineconeConfig config = mock(PineconeConfig.class);
        PineconeConnection connectionMock = mock(PineconeConnection.class);
        VectorServiceGrpc.VectorServiceBlockingStub stubMock = mock(VectorServiceGrpc.VectorServiceBlockingStub.class);
        when(connectionMock.getBlockingStub()).thenReturn(stubMock);

        index = new Index(config, connectionMock, indexName);
    }

    @Test
    public void testValidateListNamespace() throws IOException {
        PineconeValidationException thrownNullNamespace = assertThrows(PineconeValidationException.class, () -> {
            index.validateListEndpointParameters(null, null, null, null, true, true, true, true);
        });
        assertEquals("Namespace cannot be null", thrownNullNamespace.getMessage());
    }

    @Test
    public void testValidateListPrefix() throws IOException {
        PineconeValidationException thrownNullPrefix = assertThrows(PineconeValidationException.class, () -> {
            index.validateListEndpointParameters("test-namespace", null, null, null, true, true, true, true);
        });
        assertEquals("Prefix cannot be null or empty", thrownNullPrefix.getMessage());

        PineconeValidationException thrownEmptyPrefix = assertThrows(PineconeValidationException.class, () -> {
            index.validateListEndpointParameters("test-namespace", "", null, null, true, true, true, true);
        });
        assertEquals("Prefix cannot be null or empty", thrownEmptyPrefix.getMessage());

        // Confirm can pass null prefix if prefixRequired=false
        index.validateListEndpointParameters("test-namespace", null, "someToken", 1, false, false, true, true);
    }

    @Test
    public void testValidateListPagToken() throws IOException {
        PineconeValidationException thrownNullPagToken = assertThrows(PineconeValidationException.class, () -> {
            index.validateListEndpointParameters("test-namespace", "", null, null, false, false, true, true);
        });
        assertEquals("Pagination token cannot be null or empty", thrownNullPagToken.getMessage());

        PineconeValidationException thrownEmptyPagToken = assertThrows(PineconeValidationException.class, () -> {
            index.validateListEndpointParameters("test-namespace", "", "", null, false, false, true, true);
        });
        assertEquals("Pagination token cannot be null or empty", thrownEmptyPagToken.getMessage());

        // Confirm can pass null paginationToken if paginationToken=false
        index.validateListEndpointParameters("test-namespace", "somePrefix", null, 1, true, true, false, true);
    }

    @Test
    public void testValidateListLimit() throws IOException {
        PineconeValidationException thrownNegativeLimit = assertThrows(PineconeValidationException.class, () -> {
            index.validateListEndpointParameters("test-namespace", "", "", -1, false, false, false, true);
        });
        assertEquals("Limit must be a positive integer", thrownNegativeLimit.getMessage());

        PineconeValidationException thrownNullLimit = assertThrows(PineconeValidationException.class, () -> {
            index.validateListEndpointParameters("test-namespace", "", "", null, false, false, false, true);
        });
        assertEquals("Limit must be a positive integer", thrownNullLimit.getMessage());

        // Confirm can pass null limit if limit=false
        index.validateListEndpointParameters("test-namespace", "somePrefix", "someToken", null, true, true, true,
                false);
    }

}
