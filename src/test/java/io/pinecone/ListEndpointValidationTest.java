package io.pinecone;

import io.pinecone.clients.Index;
import io.pinecone.exceptions.PineconeValidationException;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

public class ListEndpointValidationTest {
    @Test
    public void testValidateListEndpointParameters() {

        PineconeValidationException thrownNullNamespace =assertThrows(PineconeValidationException.class, () -> {
            Index.validateListEndpointParameters(null, null, null, null, true, true, true);
        });
        assertEquals("Namespace cannot be null or empty", thrownNullNamespace.getMessage());

        PineconeValidationException thrownEmptyNamespace =assertThrows(PineconeValidationException.class, () -> {
            Index.validateListEndpointParameters("", null, null, null, true, true, true);
        });
        assertEquals("Namespace cannot be null or empty", thrownEmptyNamespace.getMessage());

        PineconeValidationException thrownNullPrefix =assertThrows(PineconeValidationException.class, () -> {
            Index.validateListEndpointParameters("test-namespace", null, null, null, true, true, true);
        });
        assertEquals("Prefix cannot be null or empty", thrownNullPrefix.getMessage());

        PineconeValidationException thrownEmptyPrefix =assertThrows(PineconeValidationException.class, () -> {
            Index.validateListEndpointParameters("test-namespace", "", null, null, true, true, true);
        });
        assertEquals("Prefix cannot be null or empty", thrownEmptyPrefix.getMessage());

        // Confirm can pass null prefix if prefixRequired=false
        Index.validateListEndpointParameters("test-namespace", null, "someToken", 1, false, true, true);

        PineconeValidationException thrownNullPagToken =assertThrows(PineconeValidationException.class, () -> {
            Index.validateListEndpointParameters("test-namespace", "", null, null, false, true, true);
        });
        assertEquals("Pagination token cannot be null or empty", thrownNullPagToken.getMessage());

        PineconeValidationException thrownEmptyPagToken =assertThrows(PineconeValidationException.class, () -> {
            Index.validateListEndpointParameters("test-namespace", "", "", null, false, true, true);
        });
        assertEquals("Pagination token cannot be null or empty", thrownEmptyPagToken.getMessage());

        // Confirm can pass null paginationToken if paginationToken=false
        Index.validateListEndpointParameters("test-namespace", "somePrefix", null, 1, true, false, true);

        PineconeValidationException thrownNegativeLimit =assertThrows(PineconeValidationException.class, () -> {
            Index.validateListEndpointParameters("test-namespace", "", "", -1, false, false, true);
        });
        assertEquals("Limit must be a positive integer", thrownNegativeLimit.getMessage());

        PineconeValidationException thrownNullLimit =assertThrows(PineconeValidationException.class, () -> {
            Index.validateListEndpointParameters("test-namespace", "", "", null, false, false, true);
        });
        assertEquals("Limit must be a positive integer", thrownNullLimit.getMessage());

        // Confirm can pass null limit if limit=false
        Index.validateListEndpointParameters("test-namespace", "somePrefix", "someToken", null, true, true, false);
    }

}
