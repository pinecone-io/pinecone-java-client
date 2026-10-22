package io.pinecone.clients;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.pinecone.helpers.MockPinecone;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openapitools.db_data.client.ApiException;
import org.openapitools.db_data.client.model.Hit;
import org.openapitools.db_data.client.model.SearchRecordsRequestQuery;
import org.openapitools.db_data.client.model.SearchRecordsRequestRerank;
import org.openapitools.db_data.client.model.SearchRecordsResponse;
import org.openapitools.db_data.client.model.SearchRecordsVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Drives the REST records data plane (upsertRecords and the searchRecords* methods) through the public client against
 * {@link MockPinecone}, checking the wire request and response parsing with no API key.
 */
public class MockedRecordsTest {
    @RegisterExtension
    final MockPinecone mock = new MockPinecone();

    private static final String SEARCH_PATH = "/records/namespaces/ns-1/search";
    private static final String SEARCH_RESPONSE = "{"
            + "\"result\":{\"hits\":["
            + "{\"_id\":\"rec-1\",\"_score\":0.91,\"fields\":{\"chunk_text\":\"apples\",\"category\":\"fruit\"}},"
            + "{\"_id\":\"rec-2\",\"_score\":0.42,\"fields\":{\"chunk_text\":\"carrots\"}}"
            + "]},"
            + "\"usage\":{\"read_units\":6,\"embed_total_tokens\":12,\"rerank_units\":1}"
            + "}";

    private Index index(String indexName) {
        mock.stubDescribeIndex(indexName, 3, mock.restHost());
        return mock.client().getIndexConnection(indexName);
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

    private static JsonObject searchQuery(RecordedRequest request) {
        assertTrue(request.getHeader("Content-Type").startsWith("application/json"), request.getHeader("Content-Type"));
        return JsonParser.parseString(request.getBody().readUtf8()).getAsJsonObject();
    }

    private static void assertSearchResponse(SearchRecordsResponse response) {
        List<Hit> hits = response.getResult().getHits();
        assertEquals(2, hits.size());
        assertEquals("rec-1", hits.get(0).getId());
        assertEquals(0.91f, hits.get(0).getScore(), 1e-6);
        Map<?, ?> fields = (Map<?, ?>) hits.get(0).getFields();
        assertEquals("apples", fields.get("chunk_text"));
        assertEquals("fruit", fields.get("category"));
        assertEquals("rec-2", hits.get(1).getId());
        assertEquals(6, response.getUsage().getReadUnits());
        assertEquals(12, response.getUsage().getEmbedTotalTokens());
        assertEquals(1, response.getUsage().getRerankUnits());
    }

    private static List<String> strings(JsonArray array) {
        List<String> values = new ArrayList<>();
        array.forEach(element -> values.add(element.getAsString()));
        return values;
    }

    @Test
    public void upsertRecordsSendsNdjsonWithIdAndFields() throws ApiException {
        mock.stub("POST", "/records/namespaces/ns-1/upsert", 201, "");
        Map<String, String> first = new LinkedHashMap<>();
        first.put("_id", "rec-1");
        first.put("chunk_text", "apples");
        first.put("category", "fruit");
        Map<String, String> second = new LinkedHashMap<>();
        second.put("_id", "rec-2");
        second.put("chunk_text", "carrots");

        index("records-upsert").upsertRecords("ns-1", Arrays.asList(first, second));

        RecordedRequest request = onlyDataPlaneRequest("POST", "/records/namespaces/ns-1/upsert");
        assertTrue(request.getHeader("Content-Type").startsWith("application/x-ndjson"), request.getHeader("Content-Type"));
        String[] lines = request.getBody().readUtf8().trim().split("\n");
        assertEquals(2, lines.length);
        JsonObject line1 = JsonParser.parseString(lines[0]).getAsJsonObject();
        assertEquals("rec-1", line1.get("_id").getAsString());
        assertEquals("apples", line1.get("chunk_text").getAsString());
        assertEquals("fruit", line1.get("category").getAsString());
        assertEquals(3, line1.size(), line1.toString());
        JsonObject line2 = JsonParser.parseString(lines[1]).getAsJsonObject();
        assertEquals("rec-2", line2.get("_id").getAsString());
        assertEquals("carrots", line2.get("chunk_text").getAsString());
        assertEquals(2, line2.size(), line2.toString());
    }

    @Test
    public void upsertRecordsErrorSurfacesAsApiException() {
        mock.stub("POST", "/records/namespaces/ns-1/upsert", 400, "{\"error\":{\"message\":\"missing _id\"}}");
        Map<String, String> record = Collections.singletonMap("chunk_text", "no id");

        ApiException e = assertThrows(ApiException.class,
                () -> index("records-upsert-error").upsertRecords("ns-1", Collections.singletonList(record)));

        assertEquals(400, e.getCode());
        assertTrue(e.getResponseBody().contains("missing _id"), e.getResponseBody());
    }

    @Test
    public void searchRecordsSendsQueryFieldsAndRerank() throws ApiException {
        mock.stub("POST", SEARCH_PATH, 200, SEARCH_RESPONSE);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("text", "fruit");
        SearchRecordsRequestQuery query = new SearchRecordsRequestQuery().topK(5).inputs(inputs);
        SearchRecordsRequestRerank rerank = new SearchRecordsRequestRerank()
                .model("bge-reranker-v2-m3")
                .rankFields(Collections.singletonList("chunk_text"))
                .topN(2);

        SearchRecordsResponse response = index("records-search")
                .searchRecords("ns-1", query, Arrays.asList("chunk_text", "category"), rerank);

        assertSearchResponse(response);
        JsonObject sent = searchQuery(onlyDataPlaneRequest("POST", SEARCH_PATH));
        JsonObject sentQuery = sent.getAsJsonObject("query");
        assertEquals(5, sentQuery.get("top_k").getAsInt());
        assertEquals("fruit", sentQuery.getAsJsonObject("inputs").get("text").getAsString());
        assertEquals(Arrays.asList("chunk_text", "category"), strings(sent.getAsJsonArray("fields")));
        JsonObject sentRerank = sent.getAsJsonObject("rerank");
        assertEquals("bge-reranker-v2-m3", sentRerank.get("model").getAsString());
        assertEquals(Collections.singletonList("chunk_text"), strings(sentRerank.getAsJsonArray("rank_fields")));
        assertEquals(2, sentRerank.get("top_n").getAsInt());
    }

    @Test
    public void searchRecordsByIdSendsIdTopKAndFilter() throws ApiException {
        mock.stub("POST", SEARCH_PATH, 200, SEARCH_RESPONSE);
        Map<String, Object> filter = Collections.singletonMap("category", Collections.singletonMap("$eq", "fruit"));

        SearchRecordsResponse response = index("records-search-id")
                .searchRecordsById("rec-1", "ns-1", Collections.singletonList("chunk_text"), 3, filter, null);

        assertSearchResponse(response);
        JsonObject sent = searchQuery(onlyDataPlaneRequest("POST", SEARCH_PATH));
        JsonObject sentQuery = sent.getAsJsonObject("query");
        assertEquals("rec-1", sentQuery.get("id").getAsString());
        assertEquals(3, sentQuery.get("top_k").getAsInt());
        assertEquals("fruit", sentQuery.getAsJsonObject("filter").getAsJsonObject("category").get("$eq").getAsString());
        assertFalse(sentQuery.has("inputs"), sentQuery.toString());
        assertFalse(sentQuery.has("vector"), sentQuery.toString());
        assertFalse(sent.has("rerank"), sent.toString());
    }

    @Test
    public void searchRecordsByVectorSendsDenseAndSparseValues() throws ApiException {
        mock.stub("POST", SEARCH_PATH, 200, SEARCH_RESPONSE);
        SearchRecordsVector vector = new SearchRecordsVector()
                .values(Arrays.asList(0.1f, 0.2f, 0.3f))
                .sparseIndices(Arrays.asList(7, 42))
                .sparseValues(Arrays.asList(0.5f, 0.25f));

        SearchRecordsResponse response = index("records-search-vector")
                .searchRecordsByVector(vector, "ns-1", Collections.singletonList("chunk_text"), 4, null, null);

        assertSearchResponse(response);
        JsonObject sentQuery = searchQuery(onlyDataPlaneRequest("POST", SEARCH_PATH)).getAsJsonObject("query");
        assertEquals(4, sentQuery.get("top_k").getAsInt());
        JsonObject sentVector = sentQuery.getAsJsonObject("vector");
        assertEquals(3, sentVector.getAsJsonArray("values").size());
        assertEquals(0.2f, sentVector.getAsJsonArray("values").get(1).getAsFloat(), 1e-6);
        assertEquals(42, sentVector.getAsJsonArray("sparse_indices").get(1).getAsInt());
        assertEquals(0.25f, sentVector.getAsJsonArray("sparse_values").get(1).getAsFloat(), 1e-6);
        assertFalse(sentQuery.has("filter"), sentQuery.toString());
        assertFalse(sentQuery.has("id"), sentQuery.toString());
    }

    @Test
    public void searchRecordsByTextSendsTextInput() throws ApiException {
        mock.stub("POST", SEARCH_PATH, 200, SEARCH_RESPONSE);

        SearchRecordsResponse response = index("records-search-text")
                .searchRecordsByText("crunchy snacks", "ns-1", Collections.singletonList("chunk_text"), 2, null, null);

        assertSearchResponse(response);
        JsonObject sentQuery = searchQuery(onlyDataPlaneRequest("POST", SEARCH_PATH)).getAsJsonObject("query");
        assertEquals(2, sentQuery.get("top_k").getAsInt());
        JsonObject inputs = sentQuery.getAsJsonObject("inputs");
        assertEquals("crunchy snacks", inputs.get("text").getAsString());
        assertEquals(1, inputs.size(), inputs.toString());
    }
}
