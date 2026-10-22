package io.pinecone.clients;

import io.pinecone.exceptions.*;
import io.pinecone.helpers.MockPinecone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openapitools.db_control.client.ApiException;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks that control-plane HTTP errors reach callers as the documented {@link PineconeException} subclass, with the
 * response body as the message and the generated client's {@link ApiException} as the cause.
 */
public class MockedErrorMappingTest {
    @RegisterExtension
    final MockPinecone mock = new MockPinecone();

    private static String errorBody(int status) {
        return "{\"error\":{\"code\":\"E" + status + "\",\"message\":\"status " + status + " from mock\"},"
                + "\"status\":" + status + "}";
    }

    private static <T extends PineconeException> T assertMapped(Class<T> expected, int status, Runnable call) {
        T thrown = assertThrows(expected, call::run);
        assertEquals(expected, thrown.getClass());
        assertEquals(errorBody(status), thrown.getMessage());
        ApiException cause = assertInstanceOf(ApiException.class, thrown.getCause());
        assertEquals(status, cause.getCode());
        return thrown;
    }

    @Test
    public void describeIndexMapsEachStatusToItsException() {
        Map<Integer, Class<? extends PineconeException>> expected = new LinkedHashMap<>();
        expected.put(400, PineconeBadRequestException.class);
        expected.put(401, PineconeAuthorizationException.class);
        expected.put(403, PineconeForbiddenException.class);
        expected.put(404, PineconeNotFoundException.class);
        expected.put(409, PineconeAlreadyExistsException.class);
        expected.put(500, PineconeInternalServerException.class);
        expected.put(422, PineconeUnmappedHttpException.class);
        expected.put(429, PineconeUnmappedHttpException.class);
        expected.put(503, PineconeUnmappedHttpException.class);

        Pinecone client = mock.client();
        for (Map.Entry<Integer, Class<? extends PineconeException>> entry : expected.entrySet()) {
            int status = entry.getKey();
            String indexName = "index-" + status;
            mock.stub("GET", "/indexes/" + indexName, status, errorBody(status));
            assertMapped(entry.getValue(), status, () -> client.describeIndex(indexName));
        }
        assertEquals(expected.size(), mock.restRequests().size());
    }

    @Test
    public void createExistingIndexThrowsAlreadyExists() {
        mock.stub("POST", "/indexes", 409, errorBody(409));

        assertMapped(PineconeAlreadyExistsException.class, 409, () -> mock.client().createServerlessIndex(
                "taken", "cosine", 3, "aws", "us-east-1", "disabled", null));
    }

    @Test
    public void deleteMissingIndexThrowsNotFound() {
        mock.stub("DELETE", "/indexes/missing", 404, errorBody(404));

        assertMapped(PineconeNotFoundException.class, 404, () -> mock.client().deleteIndex("missing"));
    }

    @Test
    public void listIndexesWithBadKeyThrowsAuthorization() {
        mock.stub("GET", "/indexes", 401, errorBody(401));

        assertMapped(PineconeAuthorizationException.class, 401, () -> mock.client().listIndexes());
    }
}
