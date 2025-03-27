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
import org.openapitools.db_data.client.model.SearchRecordsResponse;
import org.openapitools.db_data.client.model.UpsertRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        UpsertRecord record1 = new UpsertRecord();
        record1.id("rec1");
        record1.putAdditionalProperty("category", "digestive system");
        record1.putAdditionalProperty("chunk_text", "Apples are a great source of dietary fiber, which supports digestion and helps maintain a healthy gut.");
        ArrayList<UpsertRecord> records = new ArrayList<>();

        UpsertRecord record2 = new UpsertRecord();
        record2.id("rec2");
        record2.putAdditionalProperty("category", "cultivation");
        record2.putAdditionalProperty("chunk_text", "Apples originated in Central Asia and have been cultivated for thousands of years, with over 7,500 varieties available today.");

        UpsertRecord record3 = new UpsertRecord();
        record3.id("rec3");
        record3.putAdditionalProperty("category", "immune system");
        record3.putAdditionalProperty("chunk_text", "Rich in vitamin C and other antioxidants, apples contribute to immune health and may reduce the risk of chronic diseases.");

        UpsertRecord record4 = new UpsertRecord();
        record4.id("rec4");
        record4.putAdditionalProperty("category", "endocrine system");
        record4.putAdditionalProperty("chunk_text", "The high fiber content in apples can also help regulate blood sugar levels, making them a favorable snack for people with diabetes.");

        records.add(record1);
        records.add(record2);
        records.add(record3);
        records.add(record4);

        index.upsertRecords("example-namespace", records);
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
        Thread.sleep(5000);

        SearchRecordsResponse recordsResponse = index.searchRecords(namespace, query, fields, null);
        Assertions.assertEquals(records.size(), recordsResponse.getResult().getHits().size());
        Assertions.assertEquals(record3.getId(), recordsResponse.getResult().getHits().get(0).getId());

        pinecone.deleteIndex(indexName);
    }
}
