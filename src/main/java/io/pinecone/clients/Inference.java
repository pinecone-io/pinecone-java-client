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

public class Inference {

    InferenceApi inferenceApi;

    public Inference(ApiClient apiClient) {
        inferenceApi = new InferenceApi(apiClient);
    }

    public EmbeddingsList embed(String model, Map<String, Object> parameters, List<String> inputs) throws ApiException {
        EmbedRequestParameters embedRequestParameters = new EmbedRequestParameters();
        parameters.forEach(embedRequestParameters::putAdditionalProperty);

        List<EmbedRequestInputsInner> EmbedRequestInputsInnerList = convertInputStringToEmbedRequestInputsInner(inputs);
        EmbedRequest embedRequest = new EmbedRequest()
                .model(model)
                .parameters(embedRequestParameters)
                .inputs(EmbedRequestInputsInnerList);

        return inferenceApi.embed(embedRequest);
    }

    private List<EmbedRequestInputsInner> convertInputStringToEmbedRequestInputsInner(List<String> inputs) {
        return inputs.stream()
                .map(input -> new EmbedRequestInputsInner().text(input))
                .collect(Collectors.toList());
    }
}