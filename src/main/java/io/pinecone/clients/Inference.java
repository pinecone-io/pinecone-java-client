package io.pinecone.clients;

import io.pinecone.configs.PineconeConfig;
import io.pinecone.configs.ProxyConfig;
import io.pinecone.exceptions.PineconeConfigurationException;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.openapitools.inference.client.ApiClient;
import org.openapitools.inference.client.Configuration;
import org.openapitools.inference.client.ApiException;
import org.openapitools.inference.client.api.InferenceApi;
import org.openapitools.inference.client.model.EmbedRequest;
import org.openapitools.inference.client.model.EmbedRequestInputsInner;
import org.openapitools.inference.client.model.EmbedRequestParameters;
import org.openapitools.inference.client.model.EmbeddingsList;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The Inference class provides methods to interact with Pinecone's inference API through the Java SDK. It allows users
 * to send input data to generate embeddings using a specified model.
 * <p>
 * This class utilizes the {@link InferenceApi} to make API calls to the Pinecone inference service.
 *
 */

public class Inference {

    private final InferenceApi inferenceApi;

    /**
     * Constructs an instance of {@link Inference} class.
     * ToDo: add params list
     */
    public Inference(PineconeConfig config, OkHttpClient customOkHttpClient) {
        OkHttpClient.Builder builder = getBuilder(config, customOkHttpClient);
        ApiClient apiClient = (customOkHttpClient != null) ? new ApiClient(customOkHttpClient) : new ApiClient(builder.build());
        apiClient.setApiKey(config.getApiKey());
        apiClient.setUserAgent(config.getUserAgent());
        apiClient.addDefaultHeader("X-Pinecone-Api-Version", Configuration.VERSION);

        if (Boolean.parseBoolean(System.getenv("PINECONE_DEBUG"))) {
            apiClient.setDebugging(true);
        }

        inferenceApi = new InferenceApi(apiClient);
    }

    @NotNull
    private static OkHttpClient.Builder getBuilder(PineconeConfig config, OkHttpClient customOkHttpClient) {
        ProxyConfig proxyConfig = config.getProxyConfig();
        if (proxyConfig != null && customOkHttpClient != null) {
            throw new PineconeConfigurationException("Invalid configuration: Both Custom OkHttpClient and Proxy are set. Please configure only one of these options.");
        }
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if(proxyConfig != null) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyConfig.getHost(), proxyConfig.getPort()));
            builder.proxy(proxy);
        }
        return builder;
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