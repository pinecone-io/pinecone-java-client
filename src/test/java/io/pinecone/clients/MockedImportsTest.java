package io.pinecone.clients;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.pinecone.helpers.MockPinecone;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openapitools.db_data.client.ApiException;
import org.openapitools.db_data.client.model.ImportModel;
import org.openapitools.db_data.client.model.ListImportsResponse;
import org.openapitools.db_data.client.model.StartImportResponse;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Drives the bulk import methods on {@link AsyncIndex} through the public client against {@link MockPinecone}, checking
 * the wire request (method, path, query, body, headers) and response parsing with no API key.
 */
public class MockedImportsTest {
    @RegisterExtension
    final MockPinecone mock = new MockPinecone();

    private static final String IMPORT_JSON = "{"
            + "\"id\":\"101\","
            + "\"uri\":\"s3://bucket/prefix/\","
            + "\"status\":\"Completed\","
            + "\"createdAt\":\"2025-01-02T03:04:05Z\","
            + "\"finishedAt\":\"2025-01-02T04:05:06Z\","
            + "\"percentComplete\":100.0,"
            + "\"recordsImported\":12345,"
            + "\"error\":\"partial failure\""
            + "}";

    private AsyncIndex index(String indexName) {
        mock.stubDescribeIndex(indexName, 3, mock.restHost());
        return mock.client().getAsyncIndexConnection(indexName);
    }

    private RecordedRequest onlyDataPlaneRequest(String method, String path) {
        List<RecordedRequest> dataPlane = new ArrayList<>();
        for (RecordedRequest request : mock.restRequests()) {
            if (!request.getPath().startsWith("/indexes/")) {
                dataPlane.add(request);
            }
        }
        assertEquals(1, dataPlane.size());
        RecordedRequest request = dataPlane.get(0);
        assertEquals(method, request.getMethod());
        assertEquals(path, request.getPath());
        assertEquals("2025-10", request.getHeader("X-Pinecone-Api-Version"));
        assertEquals(MockPinecone.API_KEY, request.getHeader("Api-Key"));
        return request;
    }

    private static JsonObject jsonBody(RecordedRequest request) {
        assertTrue(request.getHeader("Content-Type").startsWith("application/json"), request.getHeader("Content-Type"));
        return JsonParser.parseString(request.getBody().readUtf8()).getAsJsonObject();
    }

    private static void assertImport(ImportModel model) {
        assertEquals("101", model.getId());
        assertEquals("s3://bucket/prefix/", model.getUri());
        assertEquals("Completed", model.getStatus());
        assertEquals(OffsetDateTime.parse("2025-01-02T03:04:05Z"), model.getCreatedAt());
        assertEquals(OffsetDateTime.parse("2025-01-02T04:05:06Z"), model.getFinishedAt());
        assertEquals(100.0f, model.getPercentComplete(), 1e-6);
        assertEquals(12345L, model.getRecordsImported());
        assertEquals("partial failure", model.getError());
    }

    @Test
    public void startImportSendsUriIntegrationIdAndErrorMode() throws ApiException {
        mock.stub("POST", "/bulk/imports", 200, "{\"id\":\"101\"}");

        StartImportResponse response = index("imports-start")
                .startImport("s3://bucket/prefix/", "integration-123", "continue");

        assertEquals("101", response.getId());
        JsonObject body = jsonBody(onlyDataPlaneRequest("POST", "/bulk/imports"));
        assertEquals("s3://bucket/prefix/", body.get("uri").getAsString());
        assertEquals("integration-123", body.get("integrationId").getAsString());
        assertEquals("continue", body.getAsJsonObject("errorMode").get("onError").getAsString());
        assertEquals(3, body.size(), body.toString());
    }

    @Test
    public void startImportOmitsEmptyIntegrationIdAndNullErrorMode() throws ApiException {
        mock.stub("POST", "/bulk/imports", 200, "{\"id\":\"102\"}");

        index("imports-start-minimal").startImport("s3://bucket/prefix/", "", null);

        JsonObject body = jsonBody(onlyDataPlaneRequest("POST", "/bulk/imports"));
        assertEquals("s3://bucket/prefix/", body.get("uri").getAsString());
        assertEquals(1, body.size(), body.toString());
    }

    @Test
    public void startImportErrorSurfacesAsApiException() {
        mock.stub("POST", "/bulk/imports", 400, "{\"error\":{\"message\":\"invalid uri\"}}");

        ApiException e = assertThrows(ApiException.class,
                () -> index("imports-start-error").startImport("not-a-uri", null, null));

        assertEquals(400, e.getCode());
        assertTrue(e.getResponseBody().contains("invalid uri"), e.getResponseBody());
    }

    @Test
    public void listImportsDefaultsToLimit100AndParsesPagination() throws ApiException {
        mock.stub("GET", "/bulk/imports", 200,
                "{\"data\":[" + IMPORT_JSON + ",{\"id\":\"102\",\"uri\":\"s3://bucket/other/\",\"status\":\"InProgress\"}],"
                        + "\"pagination\":{\"next\":\"token-2\"}}");

        ListImportsResponse response = index("imports-list").listImports();

        onlyDataPlaneRequest("GET", "/bulk/imports?limit=100");
        assertEquals(2, response.getData().size());
        assertImport(response.getData().get(0));
        assertEquals("102", response.getData().get(1).getId());
        assertEquals("InProgress", response.getData().get(1).getStatus());
        assertNull(response.getData().get(1).getFinishedAt());
        assertEquals("token-2", response.getPagination().getNext());
    }

    @Test
    public void listImportsSendsLimitAndPaginationToken() throws ApiException {
        mock.stub("GET", "/bulk/imports", 200, "{\"data\":[]}");

        ListImportsResponse response = index("imports-list-page").listImports(5, "token-2");

        onlyDataPlaneRequest("GET", "/bulk/imports?limit=5&paginationToken=token-2");
        assertTrue(response.getData().isEmpty());
        assertNull(response.getPagination());
    }

    @Test
    public void describeImportParsesEveryField() throws ApiException {
        mock.stub("GET", "/bulk/imports/101", 200, IMPORT_JSON);

        ImportModel model = index("imports-describe").describeImport("101");

        onlyDataPlaneRequest("GET", "/bulk/imports/101");
        assertImport(model);
    }

    @Test
    public void cancelImportSendsDelete() throws ApiException {
        mock.stub("DELETE", "/bulk/imports/101", 200, "{}");

        index("imports-cancel").cancelImport("101");

        onlyDataPlaneRequest("DELETE", "/bulk/imports/101");
    }

    @Test
    public void cancelImportNotFoundSurfacesAsApiException() {
        mock.stub("DELETE", "/bulk/imports/missing", 404, "{\"error\":{\"message\":\"import not found\"}}");

        ApiException e = assertThrows(ApiException.class, () -> index("imports-cancel-missing").cancelImport("missing"));

        assertEquals(404, e.getCode());
        assertTrue(e.getResponseBody().contains("import not found"), e.getResponseBody());
    }
}
