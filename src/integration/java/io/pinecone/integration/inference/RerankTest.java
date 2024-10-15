package io.pinecone.integration.inference;

import io.pinecone.clients.Inference;
import io.pinecone.clients.Pinecone;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openapitools.inference.client.ApiException;
import org.openapitools.inference.client.model.RerankResult;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RerankTest {
    private static final Pinecone pinecone = new Pinecone
            .Builder(System.getenv("PINECONE_API_KEY"))
            .withSourceTag("pinecone_test")
            .build();
    private static final Inference inference = pinecone.getInferenceClient();

    @Test
    public void testRerank() throws ApiException {
        String model = "bge-reranker-v2-m3";
        String query = "The tech company Apple is known for its innovative products like the iPhone.";
        List<Map<String, String>> documents = new ArrayList<>();

        Map<String, String> doc1 = new HashMap<>();
        doc1.put("id", "vec1");
        doc1.put("my_field", "Apple is a popular fruit known for its sweetness and crisp texture.");
        documents.add(doc1);

        Map<String, String> doc2 = new HashMap<>();
        doc2.put("id", "vec2");
        doc2.put("my_field", "Many people enjoy eating apples as a healthy snack.");
        documents.add(doc2);

        Map<String, String> doc3 = new HashMap<>();
        doc3.put("id", "vec3");
        doc3.put("my_field", "Apple Inc. has revolutionized the tech industry with its sleek designs and user-friendly interfaces.");
        documents.add(doc3);

        Map<String, String> doc4 = new HashMap<>();
        doc4.put("id", "vec4");
        doc4.put("my_field", "An apple a day keeps the doctor away, as the saying goes.");
        documents.add(doc4);

        List<String> rankFields = Arrays.asList("my_field");
        int topN = 2;
        boolean returnDocuments = true;
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("truncate", "END");

        RerankResult result = inference.rerank(model, query, documents, rankFields, topN, returnDocuments, parameters);

        assertNotNull(result);
        Assertions.assertEquals(result.getData().size(), topN);
        Assertions.assertEquals(result.getData().get(0).getIndex(), 2);
        Assertions.assertEquals(result.getData().get(0).getDocument().get("my_field"), doc3.get("my_field"));
        Assertions.assertEquals(result.getData().size(), 2);
    }

    @Test
    public void testRerankWithRequiredParameters() throws ApiException {
        String model = "bge-reranker-v2-m3";
        String query = "The tech company Apple is known for its innovative products like the iPhone.";
        List<Map<String, String>> documents = new ArrayList<>();

        Map<String, String> doc1 = new HashMap<>();
        doc1.put("id", "vec1");
        doc1.put("text", "Apple is a popular fruit known for its sweetness and crisp texture.");
        documents.add(doc1);

        Map<String, String> doc2 = new HashMap<>();
        doc2.put("id", "vec2");
        doc2.put("text", "Many people enjoy eating apples as a healthy snack.");
        documents.add(doc2);

        Map<String, String> doc3 = new HashMap<>();
        doc3.put("id", "vec3");
        doc3.put("text", "Apple Inc. has revolutionized the tech industry with its sleek designs and user-friendly interfaces.");
        documents.add(doc3);

        Map<String, String> doc4 = new HashMap<>();
        doc4.put("id", "vec4");
        doc4.put("text", "An apple a day keeps the doctor away, as the saying goes.");
        documents.add(doc4);

        RerankResult result = inference.rerank(model, query, documents);

        assertNotNull(result);
        Assertions.assertEquals(result.getData().size(), documents.size());
        Assertions.assertEquals(result.getData().get(0).getIndex(), 2);
        Assertions.assertEquals(result.getData().get(0).getDocument().get("text"), doc3.get("text"));
    }
}
