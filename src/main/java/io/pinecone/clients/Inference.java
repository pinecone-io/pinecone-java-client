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
        EmbedRequestParameters embedRequestParameters = new EmbedRequestParameters();
        parameters.forEach(embedRequestParameters::putAdditionalProperty);

        EmbedRequest embedRequest = new EmbedRequest()
                .model(model)
                .parameters(embedRequestParameters)
                .inputs(convertToEmbedInputs(inputs));

        return inferenceApi.embed(embedRequest);
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