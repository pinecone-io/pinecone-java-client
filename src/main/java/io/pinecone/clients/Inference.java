package io.pinecone.clients;

import io.pinecone.configs.PineconeConfig;
import okhttp3.OkHttpClient;
import org.openapitools.inference.client.ApiClient;
import org.openapitools.inference.client.Configuration;
import org.openapitools.inference.client.ApiException;
import org.openapitools.inference.client.api.InferenceApi;
import org.openapitools.inference.client.model.*;

import java.util.*;
import java.util.stream.Collectors;

import static io.pinecone.clients.Pinecone.buildOkHttpClient;

/**
 * Client for Pinecone's hosted models: generate embeddings from text, rerank documents against a query, and list or
 * describe the available models.
 * <p>
 * Get one from {@link Pinecone#getInferenceClient()}; it uses the same API key, proxy and HTTP client settings as the
 * {@link Pinecone} instance that created it. Use it when you want vectors or relevance scores for text you then store
 * or query yourself; an index with integrated embedding does this for you on upsert and search.
 *
 * <pre>{@code
 * Pinecone pc = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
 * Inference inference = pc.getInferenceClient();
 *
 * Map<String, Object> parameters = new HashMap<>();
 * parameters.put("input_type", "passage");
 * EmbeddingsList embeddings = inference.embed("multilingual-e5-large", parameters,
 *         Arrays.asList("Apple released a new iPhone today.", "The orchard harvested ten tons of apples."));
 * List<Float> firstVector = embeddings.getData().get(0).getDenseEmbedding().getValues();
 * }</pre>
 */

public class Inference {

    private final InferenceApi inferenceApi;

    /**
     * Creates an inference client from a configuration. Most callers use {@link Pinecone#getInferenceClient()} instead,
     * which passes the configuration of an existing {@link Pinecone} instance.
     *
     * @param config the API key, user agent, and proxy or custom {@code OkHttpClient} to use for inference requests.
     */
    public Inference(PineconeConfig config) {
        OkHttpClient customOkHttpClient = config.getCustomOkHttpClient();
        ApiClient apiClient = (customOkHttpClient != null) ? new ApiClient(customOkHttpClient) : new ApiClient(buildOkHttpClient(config.getProxyConfig()));
        apiClient.setApiKey(config.getApiKey());
        apiClient.setUserAgent(config.getUserAgent());
        apiClient.addDefaultHeader("X-Pinecone-Api-Version", Configuration.VERSION);

        inferenceApi = new InferenceApi(apiClient);
    }

    /**
     * Generates one embedding per input text using a hosted embedding model.
     * <p>
     * The result's {@code getData()} holds one {@code Embedding} per input. Dense models return a
     * {@link DenseEmbedding} (read it with {@code getDenseEmbedding().getValues()}); sparse models return a
     * {@link SparseEmbedding} with indices and values. {@code getUsage().getTotalTokens()} reports the tokens consumed.
     *
     * <pre>{@code
     * Map<String, Object> parameters = new HashMap<>();
     * parameters.put("input_type", "query");
     * EmbeddingsList result = inference.embed("multilingual-e5-large", parameters,
     *         Collections.singletonList("Which fruit has the most vitamin C?"));
     * List<Float> queryVector = result.getData().get(0).getDenseEmbedding().getValues();
     * }</pre>
     *
     * @param model the name of the embedding model, for example {@code "multilingual-e5-large"}. Use
     *              {@link #listModels(String)} with {@code "embed"} to see the available names.
     * @param parameters model-specific options such as {@code "input_type"} ({@code "query"} or {@code "passage"} for
     *                   models that embed queries and documents differently), {@code "truncate"} or
     *                   {@code "dimension"}. Pass {@code null} to use the model's defaults. The server rejects
     *                   parameter names it does not recognize; {@link #describeModel(String)} lists the parameters a
     *                   model supports.
     * @param inputs the texts to embed, for example document passages before upsert or a search query.
     * @return the embeddings, the model name, the vector type ({@code "dense"} or {@code "sparse"}) and token usage.
     * @throws ApiException if the server rejects the request, for example for an unknown model, an empty
     *                      {@code inputs} list, or an unsupported parameter value.
     */
    public EmbeddingsList embed(String model, Map<String, Object> parameters, List<String> inputs) throws ApiException {
        EmbedRequest embedRequest = new EmbedRequest()
                .model(model)
                .parameters(parameters)
                .inputs(convertToEmbedInputs(inputs));

        return inferenceApi.embed(Configuration.VERSION, embedRequest);
    }

    /**
     * Scores each document's {@code "text"} field for relevance to a query and returns all documents, most relevant
     * first.
     * <p>
     * Use this to reorder the results of a vector search before showing them. It ranks on the {@code "text"} field,
     * returns every document with its score, and includes the documents in the result. To rank on a different field,
     * return only the best matches, or pass model parameters, use
     * {@link #rerank(String, String, List, List, int, boolean, Map)}.
     *
     * <pre>{@code
     * List<Map<String, Object>> documents = new ArrayList<>();
     * Map<String, Object> doc1 = new HashMap<>();
     * doc1.put("id", "article-17");
     * doc1.put("text", "Oranges are a good source of vitamin C.");
     * documents.add(doc1);
     * Map<String, Object> doc2 = new HashMap<>();
     * doc2.put("id", "article-42");
     * doc2.put("text", "Bananas are rich in potassium.");
     * documents.add(doc2);
     *
     * RerankResult result = inference.rerank("bge-reranker-v2-m3",
     *         "Which fruit has the most vitamin C?", documents);
     * RankedDocument best = result.getData().get(0);
     * System.out.println(best.getDocument().get("id") + " " + best.getScore());
     * }</pre>
     *
     * @param model the name of the reranking model, for example {@code "bge-reranker-v2-m3"}. Use
     *              {@link #listModels(String)} with {@code "rerank"} to see the available names.
     * @param query the text to rank the documents against, such as the user's search query.
     * @param documents the documents to rank. Each map must contain a {@code "text"} entry; other entries, such as an
     *                  ID, are returned unchanged with the ranked document.
     * @return the ranked documents, each with its position in {@code documents}, its score and the document itself,
     *         plus the rerank units consumed.
     * @throws ApiException if the server rejects the request, for example for an unknown model, an empty query or
     *                      document list, or a document without a {@code "text"} entry.
     */
    public RerankResult rerank(String model,
                               String query,
                               List<Map<String, Object>> documents) throws ApiException {
        return rerank(model,
                query,
                documents,
                Arrays.asList("text"),
                documents.size(),
                true,
                new HashMap<>());
    }

    /**
     * Scores documents for relevance to a query and returns the {@code topN} most relevant, with control over which
     * fields are ranked and whether the documents are echoed back.
     * <p>
     * Results are ordered from most to least relevant. Each result's {@code getIndex()} is the document's position in
     * {@code documents}, so you can map scores back to your own objects when {@code returnDocuments} is
     * {@code false}.
     *
     * <pre>{@code
     * List<Map<String, Object>> documents = new ArrayList<>();
     * Map<String, Object> doc1 = new HashMap<>();
     * doc1.put("title", "Citrus and your health");
     * doc1.put("body", "Oranges are a good source of vitamin C.");
     * documents.add(doc1);
     * Map<String, Object> doc2 = new HashMap<>();
     * doc2.put("title", "Potassium-rich foods");
     * doc2.put("body", "Bananas are rich in potassium.");
     * documents.add(doc2);
     *
     * Map<String, Object> parameters = new HashMap<>();
     * parameters.put("truncate", "END");
     * RerankResult result = inference.rerank("bge-reranker-v2-m3", "Which fruit has the most vitamin C?",
     *         documents, Collections.singletonList("body"), 1, false, parameters);
     * int bestPosition = result.getData().get(0).getIndex();
     * }</pre>
     *
     * @param model the name of the reranking model, for example {@code "bge-reranker-v2-m3"}.
     * @param query the text to rank the documents against, such as the user's search query.
     * @param documents the documents to rank, as maps of field name to value. Every document must contain each field
     *                  named in {@code rankFields}.
     * @param rankFields the document fields to rank on, for example {@code Collections.singletonList("body")}. Some
     *                   models accept only one field and reject the request otherwise.
     * @param topN how many of the highest-scoring documents to return. A value larger than the number of documents
     *             returns all of them.
     * @param returnDocuments {@code true} to include each document in the result; {@code false} to return only
     *                        positions and scores, which keeps the response small.
     * @param parameters model-specific options, such as {@code "truncate"} set to {@code "END"} to shorten documents
     *                   that exceed the model's token limit instead of failing. Pass an empty map or {@code null} to
     *                   use the model's defaults. The server rejects parameter names it does not recognize.
     * @return the ranked documents, each with its position in {@code documents}, its score and (if requested) the
     *         document, plus the rerank units consumed.
     * @throws ApiException if the server rejects the request, for example for an unknown model, a document missing
     *                      a rank field, or more rank fields than the model supports.
     */
    public RerankResult rerank(String model,
                               String query,
                               List<Map<String, Object>> documents,
                               List<String> rankFields,
                               int topN,
                               boolean returnDocuments,
                               Map<String, Object> parameters) throws ApiException {
        RerankRequest rerankRequest = new RerankRequest();

        rerankRequest
                .model(model)
                .query(query)
                .documents(documents)
                .rankFields(rankFields)
                .topN(topN)
                .returnDocuments(returnDocuments)
                .parameters(parameters);

        return inferenceApi.rerank(Configuration.VERSION, rerankRequest);
    }

    /**
     * Lists every hosted embedding and reranking model.
     *
     * <pre>{@code
     * for (ModelInfo model : inference.listModels().getModels()) {
     *     System.out.println(model.getModel() + " (" + model.getType() + "): " + model.getShortDescription());
     * }
     * }</pre>
     *
     * @return the models, each with its name, type and capabilities such as supported dimensions and parameters.
     * @throws ApiException if the request fails.
     */
    public ModelInfoList listModels() throws ApiException {
        return inferenceApi.listModels(Configuration.VERSION, null, null);
    }

    /**
     * Lists the hosted models of one type: embedding models or reranking models.
     *
     * <pre>{@code
     * ModelInfoList rerankModels = inference.listModels("rerank");
     * }</pre>
     *
     * @param type {@code "embed"} for embedding models or {@code "rerank"} for reranking models; {@code null} lists
     *             both.
     * @return the matching models, each with its name, type and capabilities.
     * @throws ApiException if the server rejects {@code type} as an unknown model type.
     */
    public ModelInfoList listModels(String type) throws ApiException {
        return inferenceApi.listModels(Configuration.VERSION, type, null);
    }

    /**
     * Lists the hosted models of one type, narrowed to embedding models that produce dense or sparse vectors.
     * <p>
     * Use this to find a model that matches your index: a dense index needs a dense embedding model, a sparse index a
     * sparse one. Setting {@code vectorType} returns only embedding models.
     *
     * <pre>{@code
     * ModelInfoList sparseModels = inference.listModels("embed", "sparse");
     * }</pre>
     *
     * @param type {@code "embed"} or {@code "rerank"}; {@code null} for no type filter.
     * @param vectorType {@code "dense"} or {@code "sparse"}; {@code null} for no vector type filter. The server
     *                   rejects a vector type combined with {@code type} {@code "rerank"}.
     * @return the matching models, each with its name, type and capabilities.
     * @throws ApiException if the server rejects an unknown {@code type} or {@code vectorType}, or the combination
     *                      of {@code "rerank"} with a vector type.
     */
    public ModelInfoList listModels(String type, String vectorType) throws ApiException {
        return inferenceApi.listModels(Configuration.VERSION, type, vectorType);
    }

    /**
     * Describes one hosted model: its type, vector type, supported dimensions and metrics, limits and the parameters
     * it accepts.
     * <p>
     * Check a model here before creating an index for its embeddings, so the index dimension and metric match.
     *
     * <pre>{@code
     * ModelInfo model = inference.describeModel("multilingual-e5-large");
     * Integer dimension = model.getDefaultDimension();
     * List<String> metrics = model.getSupportedMetrics();
     * }</pre>
     *
     * @param modelName the model name, for example {@code "multilingual-e5-large"}.
     * @return the model's details.
     * @throws ApiException if no model has this name (HTTP 404) or the request fails.
     */
    public ModelInfo describeModel(String modelName) throws ApiException {
        return inferenceApi.getModel(Configuration.VERSION, modelName);
    }

    /**
     * Converts a list of input strings to EmbedRequestInputsInner objects.
     *
     * @param inputs A list of input strings.
     * @return A list of EmbedRequestInputsInner objects containing the input data.
     */
    private List<EmbedRequestInputsInner> convertToEmbedInputs(List<String> inputs) {
        return inputs.stream()
                .map(input -> new EmbedRequestInputsInner().text(input))
                .collect(Collectors.toList());
    }
}