package io.pinecone.integration.inference;

import io.pinecone.clients.Inference;
import io.pinecone.clients.Pinecone;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openapitools.inference.client.ApiException;
import org.openapitools.inference.client.model.ModelInfo;
import org.openapitools.inference.client.model.ModelInfoList;

public class ModelsTest {
    private static final Pinecone pinecone = new Pinecone
            .Builder(System.getenv("PINECONE_API_KEY"))
            .withSourceTag("pinecone_test")
            .build();

    private static final Inference inference = pinecone.getInferenceClient();

    @Test
    public void testListAndDescribeModels() throws ApiException {
        ModelInfoList models = inference.listModels();
        Assertions.assertNotNull(models.getModels());

        models = inference.listModels("rerank");
        Assertions.assertNotNull(models.getModels());

        models = inference.listModels("embed", "dense");
        Assertions.assertNotNull(models.getModels());

        ModelInfo modelInfo = inference.describeModel("llama-text-embed-v2");
        Assertions.assertNotNull(modelInfo);
    }
}
