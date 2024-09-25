package io.pinecone.clients;

import org.openapitools.control.client.ApiClient;
import org.openapitools.control.client.ApiException;
import org.openapitools.control.client.api.InferenceApi;
import org.openapitools.control.client.model.EmbedRequest;
import org.openapitools.control.client.model.EmbedRequestInputsInner;
import org.openapitools.control.client.model.EmbedRequestParameters;
import org.openapitools.control.client.model.EmbeddingsList;

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
     *
     * @param apiClient The ApiClient object used to configure the API connection.
     */
    public Inference(ApiClient apiClient) {
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