package io.pinecone.integration.dataPlane;

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.helpers.RandomStringBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openapitools.db_control.client.model.CreateIndexForModelRequest;
import org.openapitools.db_control.client.model.CreateIndexForModelRequestEmbed;
import org.openapitools.db_control.client.model.DeletionProtection;
import org.openapitools.db_data.client.ApiException;
import org.openapitools.db_data.client.model.SearchRecordsRequestQuery;
import org.openapitools.db_data.client.model.SearchRecordsRequestRerank;
import org.openapitools.db_data.client.model.SearchRecordsResponse;

import java.util.*;

public class UpsertAndSearchRecordsTest {
    @Test
    public void upsertAndSearchRecordsTest() throws ApiException, org.openapitools.db_control.client.ApiException, InterruptedException {
        Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
        String indexName = RandomStringBuilder.build("inf", 8);
        HashMap<String, String> fieldMap = new HashMap<>();
        fieldMap.put("text", "chunk_text");
        CreateIndexForModelRequestEmbed embed = new CreateIndexForModelRequestEmbed()
                .model("multilingual-e5-large")
                .fieldMap(fieldMap);
        pinecone.createIndexForModel(indexName, CreateIndexForModelRequest.CloudEnum.AWS, "us-west-2", embed, DeletionProtection.DISABLED, new HashMap<>());

        // Wait for index to be created
        Thread.sleep(10000);

        Index index = pinecone.getIndexConnection(indexName);
        ArrayList<Map<String, String>> upsertRecords = new ArrayList<>();

        HashMap<String, String> record1 = new HashMap<>();
        record1.put("_id", "rec1");
        record1.put("category", "digestive system");
        record1.put("chunk_text", "Apples are a great source of dietary fiber, which supports digestion and helps maintain a healthy gut.");

        HashMap<String, String> record2 = new HashMap<>();
        record2.put("_id", "rec2");
        record2.put("category", "cultivation");
        record2.put("chunk_text", "Apples originated in Central Asia and have been cultivated for thousands of years, with over 7,500 varieties available today.");

        HashMap<String, String> record3 = new HashMap<>();
        record3.put("_id", "rec3");
        record3.put("category", "immune system");
        record3.put("chunk_text", "Rich in vitamin C and other antioxidants, apples contribute to immune health and may reduce the risk of chronic diseases.");

        HashMap<String, String> record4 = new HashMap<>();
        record4.put("_id", "rec4");
        record4.put("category", "endocrine system");
        record4.put("chunk_text", "The high fiber content in apples can also help regulate blood sugar levels, making them a favorable snack for people with diabetes.");

        upsertRecords.add(record1);
        upsertRecords.add(record2);
        upsertRecords.add(record3);
        upsertRecords.add(record4);

        index.upsertRecords("example-namespace", upsertRecords);

        String namespace = "example-namespace";
        HashMap<String, String> inputsMap = new HashMap<>();
        inputsMap.put("text", "Disease prevention");
        SearchRecordsRequestQuery query = new SearchRecordsRequestQuery()
                .topK(4)
                .inputs(inputsMap);

        List<String> fields = new ArrayList<>();
        fields.add("category");
        fields.add("chunk_text");

        // Wait for vectors to be upserted
        Thread.sleep(7500);

        SearchRecordsResponse recordsResponse = index.searchRecordsById(record1.get("_id"), namespace, fields, 1, null, null);
        Assertions.assertEquals(1, recordsResponse.getResult().getHits().size());
        Assertions.assertEquals(record1.get("_id"), recordsResponse.getResult().getHits().get(0).getId());

        SearchRecordsRequestRerank rerank = new SearchRecordsRequestRerank()
                .model("bge-reranker-v2-m3")
                .topN(2)
                .rankFields(Arrays.asList("chunk_text"));

        recordsResponse = index.searchRecordsByText("Disease prevention", namespace, fields, 4, null, rerank);
        Assertions.assertEquals(record3.get("_id"), recordsResponse.getResult().getHits().get(0).getId());

        pinecone.deleteIndex(indexName);
    }
}
