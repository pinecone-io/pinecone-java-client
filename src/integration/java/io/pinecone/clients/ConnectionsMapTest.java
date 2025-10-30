package io.pinecone.clients;

import io.pinecone.configs.PineconeConfig;
import io.pinecone.configs.PineconeConnection;
import io.pinecone.exceptions.PineconeNotFoundException;
import io.pinecone.helpers.RandomStringBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
    static Pinecone pinecone1;
    static Pinecone pinecone2;

    @BeforeAll
    public static void setUp() throws InterruptedException {
        indexName1 = RandomStringBuilder.build("conn-map-index1", 5);
        indexName2 = RandomStringBuilder.build("conn-map-index2", 5);
        pinecone1 = new Pinecone
                .Builder(System.getenv("PINECONE_API_KEY"))
                .withSourceTag("pinecone_test")
                .build();

        pinecone2 = new Pinecone
                .Builder(System.getenv("PINECONE_API_KEY"))
                .withSourceTag("pinecone_test")
                .build();
    }

    @Test
    public void testMultipleIndexesWithMultipleClients() throws InterruptedException {
        Map<String, String> tags = new HashMap<>();
        tags.put("env", "test");

        // Create index-1
        pinecone1.createServerlessIndex(indexName1,
                null,
                3,
                "aws",
                "us-east-1",
                "disabled",
                tags);

        // Wait for index to be ready
        IndexModel indexModel1 = waitUntilIndexIsReady(pinecone1, indexName1);

        // Get index1's host
        String host1 = indexModel1.getHost();

        // Create config1 for getting index connection and set the host
        PineconeConfig config1 = new PineconeConfig(System.getenv("PINECONE_API_KEY"));
        config1.setHost(host1);

        // Create index-2
        pinecone1.createServerlessIndex(indexName2,
                null,
                3,
                "aws",
                "us-east-1",
                "disabled",
                tags);

        // Wait for index to be ready
        IndexModel indexModel2 = waitUntilIndexIsReady(pinecone1, indexName2);

        // Get index2's host
        String host2 = indexModel2.getHost();

        // Create config2 for getting index connection and set the host
        PineconeConfig config2 = new PineconeConfig(System.getenv("PINECONE_API_KEY"));
        config1.setHost(host2);


        // Establish grpc connection for index-1
        Index index1_1 = pinecone1.getIndexConnection(indexName1);
        // Get connections map
        ConcurrentHashMap<String, PineconeConnection> connectionsMap1_1 = pinecone1.getConnectionsMap();

        // Verify connectionsMap contains only one <indexName, connection> pair i.e. for index1 and its connection
        assertEquals(pinecone1.getConnectionsMap().size(), 1);
        // Verify the value for index1 by comparing its value with host1 in the connectionsMap
        assertEquals(host1, connectionsMap1_1.get(indexName1).toString());

        // Establish grpc connection for index-2
        Index index1_2 = pinecone1.getIndexConnection(indexName2);
        // Get connections map after establishing second connection
        ConcurrentHashMap<String, PineconeConnection> connectionsMap1_2 = pinecone1.getConnectionsMap();

        // Verify connectionsMap contains two <indexName, connection> pairs i.e. for index1 and index2
        assertEquals(connectionsMap1_2.size(), 2);
        // Verify the values by checking host for both indexName1 and indexName2
        assertEquals(host1, connectionsMap1_2.get(indexName1).toString());
        assertEquals(host2, connectionsMap1_2.get(indexName2).toString());

        // Establishing connections with index1 and index2 using another pinecone client
        pinecone2.getConnection(indexName1, config1);
        ConcurrentHashMap<String, PineconeConnection> connectionsMap2_1 = pinecone1.getConnectionsMap();
        // Verify the new connections map is pointing to the same reference
        assert connectionsMap2_1 == connectionsMap1_2;
        // Verify the size of connections map is still 2 since the connection for index2 was not closed
        assertEquals(2, connectionsMap2_1.size());
        // Verify the connection value for index1 is host1
        assertEquals(host1, connectionsMap2_1.get(indexName1).toString());

        pinecone2.getConnection(indexName2, config2);
        ConcurrentHashMap<String, PineconeConnection> connectionsMap2_2 = pinecone1.getConnectionsMap();
        // Verify the new connections map is pointing to the same reference
        assert connectionsMap2_1 == connectionsMap2_2;
        // Verify the size of connections map is still 2 since the connections are not closed
        assertEquals(2, connectionsMap2_2.size());
        // Verify the values by checking host for both indexName1 and indexName2
        assertEquals(host1, connectionsMap2_2.get(indexName1).toString());
        assertEquals(host2, connectionsMap2_2.get(indexName2).toString());

        // Close the connections
        index1_1.close();
        index1_2.close();

        // Verify the map size is now 0
        assertEquals(connectionsMap1_1.size(), 0);
        assertEquals(connectionsMap1_2.size(), 0);
        assertEquals(connectionsMap2_1.size(), 0);
        assertEquals(connectionsMap2_2.size(), 0);

        // Delete the indexes
        pinecone1.deleteIndex(indexName1);
        pinecone1.deleteIndex(indexName2);

        // Wait for indexes to be deleted
        Thread.sleep(5000);

        // Confirm the indexes are deleted by calling describe index which should return resource not found
        assertThrows(PineconeNotFoundException.class, () -> pinecone1.describeIndex(indexName1));
        assertThrows(PineconeNotFoundException.class, () -> pinecone1.describeIndex(indexName2));
    }
}
