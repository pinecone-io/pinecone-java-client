package io.pinecone.integration.inference;

import io.pinecone.clients.Inference;
import io.pinecone.clients.Pinecone;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openapitools.inference.client.ApiException;
import org.openapitools.inference.client.model.EmbeddingsList;

import java.util.*;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EmbedTest {

    private static final Pinecone pinecone = new Pinecone
            .Builder(System.getenv("PINECONE_API_KEY"))
            .withSourceTag("pinecone_test")
            .build();
    private static final Inference inference = pinecone.getInferenceClient();

    @Test
    public void testGenerateEmbeddings() throws ApiException {
        List<String> inputs = new ArrayList<>(1);
        inputs.add("The quick brown fox jumps over the lazy dog.");
        inputs.add("Lorem ipsum");

        String embeddingModel = "multilingual-e5-large";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("input_type", "query");
        parameters.put("truncate", "END");
        EmbeddingsList embeddings = inference.embed(embeddingModel, parameters, inputs);

        assertNotNull(embeddings, "Expected embedding to be not null");
        Assertions.assertEquals(embeddingModel, embeddings.getModel());
        Assertions.assertEquals(1024, embeddings.getData().get(0).getValues().size());
        Assertions.assertEquals(2, embeddings.getData().size());
    }

    @Test
    public void testGenerateEmbeddingsInvalidInputs() throws ApiException {
        String embeddingModel = "multilingual-e5-large";
        List<String> inputs = new ArrayList<>();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("input_type", "query");
        parameters.put("truncate", "END");

        Exception exception = assertThrows(Exception.class, () -> {
            inference.embed(embeddingModel, parameters, inputs);
        });

        Assertions.assertTrue(exception.getMessage().contains("Must specify at least one input"));
    }
}
