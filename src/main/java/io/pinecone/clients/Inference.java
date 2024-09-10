package io.pinecone.clients;

import org.openapitools.control.client.ApiException;
import org.openapitools.control.client.api.InferenceApi;
import org.openapitools.control.client.model.EmbedRequest;
import org.openapitools.control.client.model.EmbedRequestInputsInner;
import org.openapitools.control.client.model.EmbedRequestParameters;
import org.openapitools.control.client.model.EmbeddingsList;

import java.util.ArrayList;
import java.util.List;

public class Inference {
    InferenceApi inferenceApi;

    public Inference() {
        inferenceApi = new InferenceApi();
    }

    public EmbeddingsList embed(String model, String truncate, String inputType, List<String> inputs) throws ApiException {
        EmbedRequestParameters embedRequestParameters = new EmbedRequestParameters().inputType(inputType);
        if(truncate != null && !truncate.isEmpty())
            embedRequestParameters.truncate(truncate);
        List<EmbedRequestInputsInner> EmbedRequestInputsInnerList = convertInputStringToEmbedRequestInputsInner(inputs);
        EmbedRequest embedRequest = new EmbedRequest()
                .model(model)
                .parameters(embedRequestParameters)
                .inputs(EmbedRequestInputsInnerList);

        return inferenceApi.embed(embedRequest);
    }

    private List<EmbedRequestInputsInner> convertInputStringToEmbedRequestInputsInner(List<String> inputs) {
        List<EmbedRequestInputsInner> embedRequestInputsInnerList = new ArrayList<EmbedRequestInputsInner>();
        for(String input:inputs) {
            embedRequestInputsInnerList.add(new EmbedRequestInputsInner().text(input));
        }
        return embedRequestInputsInnerList;
    }
}
