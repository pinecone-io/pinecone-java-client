package io.pinecone.integration.inference;

import io.pinecone.clients.Inference;
import io.pinecone.clients.Pinecone;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openapitools.control.client.ApiException;
import org.openapitools.control.client.model.EmbeddingsList;

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
        EmbeddingsList embeddings = inference.embed(embeddingModel, "query", "END", inputs);
        try {
            assertNotNull(embeddings, "Expected embedding to be not null");
            Assertions.assertEquals(embeddingModel, embeddings.getModel());
            System.out.println(embeddings);
            Assertions.assertEquals(1024, embeddings.getData().get(0).getValues().size());
            Assertions.assertEquals(2, embeddings.getData().size());
        } catch (Exception e) {
            Assertions.fail("Embedding request should not have thrown an exception: " + e.getMessage());
        }
    }

    @Test
    public void testGenerateEmbeddingsInvalidInputs() throws ApiException {
        String embeddingModel = "multilingual-e5-large";
        List<String> inputs = new ArrayList<>();

        Exception exception = assertThrows(Exception.class, () -> {
            inference.embed(embeddingModel, "query", "END", inputs);
        });

        Assertions.assertTrue(exception.getMessage().contains("Must specify at least one input"));
    }
}
