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
 * The Inference class provides methods to interact with Pinecone's inference API through the Java SDK. It allows users
 * to send input data to generate embeddings or rank documents using a specified model.
 * <p>
 * This class utilizes the {@link InferenceApi} to make API calls to the Pinecone inference service.
 *
 */

public class Inference {

    private final InferenceApi inferenceApi;

    /**
     * Constructs an instance of {@link Inference} class using PineconeConfig object.
     * @param config The Pinecone configuration object for interacting with inference api.
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
     * Sends input data and parameters to the embedding model and returns a list of embeddings.
     *
     * @param model      The embedding model to use.
     * @param parameters A map containing model-specific parameters.
     * @param inputs     A list of input strings to generate embeddings for.
     * @return EmbeddingsList containing the embeddings for the provided inputs.
     * @throws ApiException If the API call fails, an ApiException is thrown.
     */
    public EmbeddingsList embed(String model, Map<String, Object> parameters, List<String> inputs) throws ApiException {
//        EmbedRequestParameters embedRequestParameters = new EmbedRequestParameters();
//        parameters.forEach(embedRequestParameters::putAdditionalProperty);

        EmbedRequest embedRequest = new EmbedRequest()
                .model(model)
                .parameters(parameters)
                .inputs(convertToEmbedInputs(inputs));

        return inferenceApi.embed(embedRequest);
    }

    /**
     * Reranks a list of documents based on the relevance to a query using the specified model. Since rest of the
     * parameters are optional, they are set to their default values.
     *
     * @param model      The model to be used for reranking the documents.
     * @param query      The query string to rank the documents against.
     * @param documents  A list of maps representing the documents to be ranked.
     *                   Each map should contain document attributes, such as "text".
     *
     * @return RerankResult containing the ranked documents and their scores.
     * @throws ApiException If the API call fails, an ApiException is thrown.
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
     * Reranks a list of documents based on the relevance to a query using the specified model with additional options.
     *
     * @param model            The model to be used for reranking the documents.
     * @param query            The query string to rank the documents against.
     * @param documents        A list of maps representing the documents to be ranked.
     *                         Each map should contain document attributes, such as "text".
     * @param rankFields       A list of fields in the documents to be used for ranking, typically "text".
     * @param topN             The number of top-ranked documents to return.
     * @param returnDocuments  Whether to return the documents along with the ranking scores.
     * @param parameters       A map containing additional model-specific parameters for reranking.
     * @return RerankResult containing the ranked documents and their scores.
     * @throws ApiException If the API call fails, an ApiException is thrown.
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

        return inferenceApi.rerank(rerankRequest);
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