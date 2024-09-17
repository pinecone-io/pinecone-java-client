package io.pinecone.clients;

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

    public Inference() {
        inferenceApi = new InferenceApi();
    }

    // ToDo: Add serialization unit test for checking if truncate and input_type are correctly serialized when passed via map
    // Cond1: when input_type and truncate are set -> both should have the set values especially truncate's default value END should be overwritten
    // Cond2: when only input_type is set, truncate should be by default set to END
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
