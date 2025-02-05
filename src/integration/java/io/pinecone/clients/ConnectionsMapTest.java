package io.pinecone.clients;

import io.pinecone.configs.PineconeConnection;
import io.pinecone.exceptions.PineconeNotFoundException;
import io.pinecone.helpers.RandomStringBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openapitools.db_control.client.model.DeletionProtection;
import org.openapitools.db_control.client.model.IndexModel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.pinecone.helpers.TestUtilities.waitUntilIndexIsReady;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConnectionsMapTest {
    static String indexName1;
    static String indexName2;
    static Pinecone pinecone;

    @BeforeAll
    public static void setUp() throws InterruptedException {
        indexName1 = RandomStringBuilder.build("conn-map-index1", 5);
        indexName2 = RandomStringBuilder.build("conn-map-index2", 5);
        pinecone = new Pinecone
                .Builder(System.getenv("PINECONE_API_KEY"))
                .withSourceTag("pinecone_test")
                .build();
    }

    @Test
    public void testMultipleIndexesForSameClient() throws InterruptedException {
        Map<String, String> tags = new HashMap<>();
        tags.put("env", "test");

        // Create index-1
        pinecone.createServerlessIndex(indexName1,
                null,
                3,
                "aws",
                "us-east-1",
                DeletionProtection.DISABLED,
                tags);

        // Wait for index to be ready
        IndexModel indexModel1 = waitUntilIndexIsReady(pinecone, indexName1);

        // Get index1's host
        String host1 = indexModel1.getHost();

        // Create index-2
        pinecone.createServerlessIndex(indexName2,
                null,
                3,
                "aws",
                "us-east-1",
                DeletionProtection.DISABLED,
                tags);

        // Wait for index to be ready
        IndexModel indexModel2 = waitUntilIndexIsReady(pinecone, indexName2);

        // Get index2's host
        String host2 = indexModel2.getHost();

        // Establish grpc connection for index-1
        pinecone.getIndexConnection(indexName1);
        // Get connections map
        ConcurrentHashMap<String, PineconeConnection> connectionsMap1 = pinecone.getConnectionsMap();

        // Verify connectionsMap contains only one <indexName, connection> pair i.e. for index1 and its connection
        assertEquals(pinecone.getConnectionsMap().size(), 1);
        // Verify the value for index1 by comparing its value with host1 in the connectionsMap
        assertEquals(host1, connectionsMap1.get(indexName1).toString());

        // Establish grpc connection for index-2
        pinecone.getIndexConnection(indexName2);
        // Get connections map after establishing second connection
        ConcurrentHashMap<String, PineconeConnection> connectionsMap2 = pinecone.getConnectionsMap();

        // Verify connectionsMap contains two <indexName, connection> pairs i.e. for index1 and index2
        assertEquals(connectionsMap2.size(), 2);
        // Verify the values by checking host for both indexName1 and indexName2
        assertEquals(host1, connectionsMap2.get(indexName1).toString());
        assertEquals(host2, connectionsMap2.get(indexName2).toString());

        pinecone.deleteIndex(indexName1);
        pinecone.deleteIndex(indexName2);

        // Wait for indexes to be deleted
        Thread.sleep(5000);

        // Confirm the indexes are deleted by calling describe index which should return resource not found
        assertThrows(PineconeNotFoundException.class, () -> pinecone.describeIndex(indexName1));
        assertThrows(PineconeNotFoundException.class, () -> pinecone.describeIndex(indexName2));
    }
}
