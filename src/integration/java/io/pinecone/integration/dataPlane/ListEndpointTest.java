package io.pinecone.integration.dataPlane;

import io.pinecone.clients.AsyncIndex;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.IndexModelSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.pinecone.commons.IndexInterface.buildUpsertVectorWithUnsignedIndices;
import static io.pinecone.helpers.IndexManager.*;
import static org.junit.jupiter.api.Assertions.*;

public class ListEndpointTest {
    private static Index index;
    private static AsyncIndex asyncIndex;

    @BeforeAll
    public static void setUp() throws InterruptedException {
        int dimension = 3;
        String apiKey = System.getenv("PINECONE_API_KEY");
        String indexType = IndexModelSpec.SERIALIZED_NAME_SERVERLESS;
        Pinecone pinecone = new Pinecone.Builder(apiKey).build();

        String indexName = findIndexWithDimensionAndType(pinecone, dimension, indexType);
        if (indexName.isEmpty()) indexName = createNewIndex(pinecone, dimension, indexType, true);
        index = pinecone.getIndexConnection(indexName);
        asyncIndex = pinecone.getAsyncIndexConnection(indexName);

        List<String> upsertIds = Arrays.asList("v1", "v2", "c3");

        List<List<Float>> values = new ArrayList<>();
        values.add(Arrays.asList(1.0f, 2.0f, 3.0f));
        values.add(Arrays.asList(4.0f, 5.0f, 6.0f));
        values.add(Arrays.asList(7.0f, 8.0f, 9.0f));

        List<VectorWithUnsignedIndices> vectors = new ArrayList<>(3);
        for (int i=0; i<upsertIds.size(); i++) {
            VectorWithUnsignedIndices vector = buildUpsertVectorWithUnsignedIndices(upsertIds.get(i), values.get(i), null, null, null);
            vectors.add(vector);
        }

        // Upsert data
        index.upsert(vectors, "example-namespace");

//        asyncIndex = pinecone.getAsyncIndexConnection(indexName);
    }

    @AfterAll
    public static void cleanUp() {
        String apiKey = System.getenv("PINECONE_API_KEY");
        String indexType = IndexModelSpec.SERIALIZED_NAME_SERVERLESS;
        Pinecone pinecone = new Pinecone.Builder(apiKey).build();
        String indexName = findIndexWithDimensionAndType(pinecone, 3, indexType);

        pinecone.deleteIndex(indexName);

        index.close();
//        asyncIndex.close();
    }

    @Test
    public void testListEndpoint() throws InterruptedException {
        Thread.sleep(10000); // wait for the index to be ready for operations

        String listResponse = index.list("example-namespace").toString();
        assertTrue(listResponse.contains("v1"));
        assertTrue(listResponse.contains("v2"));
        assertTrue(listResponse.contains("c3"));

        String listResponseWithPrefix = index.list("example-namespace", "v").toString();
        assertTrue(listResponseWithPrefix.contains("v1"));
        assertTrue(listResponseWithPrefix.contains("v2"));
        assertFalse(listResponseWithPrefix.contains("c3")); // should not be in response

        String listResponseWithLimit = index.list("example-namespace", 1).toString();
        assertTrue(listResponseWithLimit.contains("v1"));
        assertFalse(listResponseWithLimit.contains("v2")); // should not be in response
        assertFalse(listResponseWithLimit.contains("c3")); // should not be in response
    }




}
