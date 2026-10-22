package io.pinecone.clients;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.pinecone.helpers.MockPinecone;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openapitools.inference.client.ApiException;
import org.openapitools.inference.client.model.EmbeddingsList;
import org.openapitools.inference.client.model.ModelInfo;
import org.openapitools.inference.client.model.ModelInfoList;
import org.openapitools.inference.client.model.ModelInfoSupportedParameter;
import org.openapitools.inference.client.model.RankedDocument;
import org.openapitools.inference.client.model.RerankResult;
import org.openapitools.inference.client.model.SparseEmbedding;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Offline wire-level coverage of the inference client: rerank, listModels, describeModel, sparse embed and error
 * surfacing.
 */
public class MockedInferenceTest {
    @RegisterExtension
    final MockPinecone mock = new MockPinecone();

    private Inference inference;

    private static final String RERANK_RESPONSE = "{"
            + "\"model\":\"bge-reranker-v2-m3\","
            + "\"data\":[{\"index\":1,\"score\":0.9,\"document\":{\"id\":\"b\",\"text\":\"apples are red\"}},"
            + "{\"index\":0,\"score\":0.1}],"
            + "\"usage\":{\"rerank_units\":1}"
            + "}";

    private static final String MODEL_JSON = "{"
            + "\"model\":\"multilingual-e5-large\","
            + "\"short_description\":\"A multilingual model\","
            + "\"type\":\"embed\","
            + "\"vector_type\":\"dense\","
            + "\"default_dimension\":1024,"
            + "\"modality\":\"text\","
            + "\"max_sequence_length\":507,"
            + "\"max_batch_size\":96,"
            + "\"provider_name\":\"Microsoft\","
            + "\"supported_dimensions\":[1024],"
            + "\"supported_metrics\":[\"cosine\",\"euclidean\"],"
            + "\"supported_parameters\":["
            + "{\"parameter\":\"input_type\",\"type\":\"one_of\",\"value_type\":\"string\",\"required\":true,"
            + "\"allowed_values\":[\"query\",\"passage\"]},"
            + "{\"parameter\":\"truncate\",\"type\":\"one_of\",\"value_type\":\"string\",\"required\":false,"
            + "\"allowed_values\":[\"END\",\"NONE\"],\"default\":\"END\"}"
            + "]}";

    @BeforeEach
    public void setUp() {
        // Inference ignores withHost, so a custom client redirects its api.pinecone.io calls to the mock.
        HttpUrl mockUrl = HttpUrl.get(mock.restUrl());
        OkHttpClient redirecting = new OkHttpClient.Builder()
                .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
                        .url(chain.request().url().newBuilder()
                                .scheme(mockUrl.scheme()).host(mockUrl.host()).port(mockUrl.port()).build())
                        .build()))
                .build();
        inference = new Pinecone.Builder(MockPinecone.API_KEY)
                .withOkHttpClient(redirecting)
                .build()
                .getInferenceClient();
    }

    private RecordedRequest onlyRequest() {
        List<RecordedRequest> requests = mock.restRequests();
        assertEquals(1, requests.size());
        RecordedRequest request = requests.get(0);
        assertEquals("2025-10", request.getHeader("X-Pinecone-Api-Version"));
        assertEquals(MockPinecone.API_KEY, request.getHeader("Api-Key"));
        return request;
    }

    private static JsonObject body(RecordedRequest request) {
        return JsonParser.parseString(request.getBody().readUtf8()).getAsJsonObject();
    }

    private static List<Map<String, Object>> documents() {
        Map<String, Object> first = new HashMap<>();
        first.put("id", "a");
        first.put("text", "bananas are yellow");
        Map<String, Object> second = new HashMap<>();
        second.put("id", "b");
        second.put("text", "apples are red");
        return Arrays.asList(first, second);
    }

    @Test
    public void rerankWithDefaultsSendsTextFieldAllDocumentsAndReturnDocuments() throws Exception {
        mock.stub("POST", "/rerank", 200, RERANK_RESPONSE);

        RerankResult result = inference.rerank("bge-reranker-v2-m3", "red fruit", documents());

        RecordedRequest request = onlyRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/rerank", request.getPath());
        JsonObject sent = body(request);
        assertEquals("bge-reranker-v2-m3", sent.get("model").getAsString());
        assertEquals("red fruit", sent.get("query").getAsString());
        assertEquals(2, sent.get("top_n").getAsInt());
        assertTrue(sent.get("return_documents").getAsBoolean());
        JsonArray rankFields = sent.getAsJsonArray("rank_fields");
        assertEquals(1, rankFields.size());
        assertEquals("text", rankFields.get(0).getAsString());
        assertEquals(0, sent.getAsJsonObject("parameters").size());
        JsonArray sentDocuments = sent.getAsJsonArray("documents");
        assertEquals(2, sentDocuments.size());
        assertEquals("a", sentDocuments.get(0).getAsJsonObject().get("id").getAsString());
        assertEquals("apples are red", sentDocuments.get(1).getAsJsonObject().get("text").getAsString());

        assertEquals("bge-reranker-v2-m3", result.getModel());
        assertEquals(1, result.getUsage().getRerankUnits());
        assertEquals(2, result.getData().size());
        RankedDocument top = result.getData().get(0);
        assertEquals(1, top.getIndex());
        assertEquals(new java.math.BigDecimal("0.9"), top.getScore());
        assertEquals("b", top.getDocument().get("id"));
        assertEquals("apples are red", top.getDocument().get("text"));
        RankedDocument bottom = result.getData().get(1);
        assertEquals(0, bottom.getIndex());
        // The generated model initialises document to an empty map when the response omits it.
        assertTrue(bottom.getDocument().isEmpty());
    }

    @Test
    public void rerankWithOptionsSendsEveryOption() throws Exception {
        mock.stub("POST", "/rerank", 200, RERANK_RESPONSE);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("truncate", "END");

        inference.rerank("bge-reranker-v2-m3", "red fruit", documents(),
                Arrays.asList("text", "id"), 1, false, parameters);

        JsonObject sent = body(onlyRequest());
        assertEquals(1, sent.get("top_n").getAsInt());
        assertFalse(sent.get("return_documents").getAsBoolean());
        JsonArray rankFields = sent.getAsJsonArray("rank_fields");
        assertEquals(2, rankFields.size());
        assertEquals("id", rankFields.get(1).getAsString());
        assertEquals("END", sent.getAsJsonObject("parameters").get("truncate").getAsString());
    }

    @Test
    public void listModelsSendsOnlyTheGivenFilters() throws Exception {
        mock.stub("GET", "/models", 200, "{\"models\":[" + MODEL_JSON + "]}");

        ModelInfoList all = inference.listModels();
        inference.listModels("rerank");
        inference.listModels("embed", "sparse");

        List<RecordedRequest> requests = mock.restRequests();
        assertEquals(3, requests.size());
        assertEquals("/models", requests.get(0).getPath());
        assertEquals("/models?type=rerank", requests.get(1).getPath());
        assertEquals("/models?type=embed&vector_type=sparse", requests.get(2).getPath());
        for (RecordedRequest request : requests) {
            assertEquals("GET", request.getMethod());
            assertEquals("2025-10", request.getHeader("X-Pinecone-Api-Version"));
            assertEquals(MockPinecone.API_KEY, request.getHeader("Api-Key"));
        }
        assertEquals(1, all.getModels().size());
        assertEquals("multilingual-e5-large", all.getModels().get(0).getModel());
    }

    @Test
    public void describeModelParsesEveryField() throws Exception {
        mock.stub("GET", "/models/multilingual-e5-large", 200, MODEL_JSON);

        ModelInfo model = inference.describeModel("multilingual-e5-large");

        RecordedRequest request = onlyRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/models/multilingual-e5-large", request.getPath());
        assertEquals("multilingual-e5-large", model.getModel());
        assertEquals("A multilingual model", model.getShortDescription());
        assertEquals("embed", model.getType());
        assertEquals("dense", model.getVectorType());
        assertEquals(1024, model.getDefaultDimension());
        assertEquals("text", model.getModality());
        assertEquals(507, model.getMaxSequenceLength());
        assertEquals(96, model.getMaxBatchSize());
        assertEquals("Microsoft", model.getProviderName());
        assertEquals(Collections.singletonList(1024), model.getSupportedDimensions());
        assertEquals(Arrays.asList("cosine", "euclidean"), model.getSupportedMetrics());
        assertEquals(2, model.getSupportedParameters().size());
        ModelInfoSupportedParameter inputType = model.getSupportedParameters().get(0);
        assertEquals("input_type", inputType.getParameter());
        assertEquals("one_of", inputType.getType());
        assertEquals("string", inputType.getValueType());
        assertTrue(inputType.getRequired());
        assertEquals(2, inputType.getAllowedValues().size());
        assertEquals("passage", inputType.getAllowedValues().get(1).getActualInstance());
        assertNull(inputType.getDefault());
        ModelInfoSupportedParameter truncate = model.getSupportedParameters().get(1);
        assertFalse(truncate.getRequired());
        assertEquals("END", truncate.getDefault().getActualInstance());
    }

    @Test
    public void sparseEmbedResponseIsParsedAsSparseEmbedding() throws Exception {
        mock.stub("POST", "/embed", 200, "{"
                + "\"model\":\"pinecone-sparse-english-v0\","
                + "\"vector_type\":\"sparse\","
                + "\"data\":[{\"sparse_values\":[0.5,0.25],\"sparse_indices\":[10,42],"
                + "\"sparse_tokens\":[\"red\",\"fruit\"],\"vector_type\":\"sparse\"}],"
                + "\"usage\":{\"total_tokens\":2}"
                + "}");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("input_type", "passage");
        parameters.put("return_tokens", true);

        EmbeddingsList embeddings = inference.embed("pinecone-sparse-english-v0", parameters,
                Arrays.asList("red", "fruit"));

        JsonObject sent = body(onlyRequest());
        assertTrue(sent.getAsJsonObject("parameters").get("return_tokens").getAsBoolean());
        assertEquals(2, sent.getAsJsonArray("inputs").size());
        assertEquals("fruit", sent.getAsJsonArray("inputs").get(1).getAsJsonObject().get("text").getAsString());

        assertEquals("sparse", embeddings.getVectorType());
        assertEquals(1, embeddings.getData().size());
        SparseEmbedding sparse = embeddings.getData().get(0).getSparseEmbedding();
        assertEquals(Arrays.asList(0.5f, 0.25f), sparse.getSparseValues());
        assertEquals(Arrays.asList(10L, 42L), sparse.getSparseIndices());
        assertEquals(Arrays.asList("red", "fruit"), sparse.getSparseTokens());
    }

    @Test
    public void errorResponseSurfacesAsApiExceptionWithCodeAndBody() {
        String error = "{\"error\":{\"code\":\"INVALID_ARGUMENT\",\"message\":\"Model not found\"},\"status\":400}";
        mock.stub("POST", "/rerank", 400, error);

        ApiException thrown = assertThrows(ApiException.class,
                () -> inference.rerank("no-such-model", "q", documents()));

        assertEquals(400, thrown.getCode());
        assertEquals(error, thrown.getResponseBody());
        onlyRequest();
    }
}
