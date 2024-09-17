package io.pinecone.integration.inference;

import io.pinecone.clients.Inference;
import io.pinecone.clients.Pinecone;
import org.openapitools.control.client.ApiException;
import org.openapitools.control.client.model.EmbeddingsList;

import java.util.ArrayList;
import java.util.List;

public class EmbedTest {
    public static void main(String[] args) throws ApiException {
        Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
        Inference inference = pinecone.getInferenceClient();
        List<String> inputs = new ArrayList<>(1);
        inputs.add("The quick brown fox jumps over the lazy dog.");
        // passing null for truncate should default to END
        EmbeddingsList list = inference.embed("model", null, "someInputType", inputs);
    }
}
