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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
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
        config2.setHost(host2);


        // Establish grpc connection for index-1
        Index index1_1 = pinecone1.getIndexConnection(indexName1);
        // Get connections map
        ConcurrentHashMap<String, PineconeConnection> connectionsMap = pinecone1.getConnectionsMap();

        // Verify indexName1 is in the map with the correct host
        assertEquals(host1, connectionsMap.get(indexName1).toString());

        // Establish grpc connection for index-2
        Index index1_2 = pinecone1.getIndexConnection(indexName2);

        // Verify both indexes are now in the map with the correct hosts
        assertEquals(host1, connectionsMap.get(indexName1).toString());
        assertEquals(host2, connectionsMap.get(indexName2).toString());

        // Connecting a second client to the same indexes should reuse the existing map entries
        int sizeBeforeSecondClient = connectionsMap.size();
        pinecone2.getConnection(indexName1, config1);
        // Verify the map is the same reference and the size did not grow
        assertSame(connectionsMap, pinecone2.getConnectionsMap());
        assertEquals(sizeBeforeSecondClient, connectionsMap.size());
        assertEquals(host1, connectionsMap.get(indexName1).toString());

        pinecone2.getConnection(indexName2, config2);
        assertEquals(sizeBeforeSecondClient, connectionsMap.size());
        assertEquals(host1, connectionsMap.get(indexName1).toString());
        assertEquals(host2, connectionsMap.get(indexName2).toString());

        // Close the connections
        index1_1.close();
        index1_2.close();

        // Verify the specific entries for this test's indexes were removed
        assertFalse(connectionsMap.containsKey(indexName1));
        assertFalse(connectionsMap.containsKey(indexName2));

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
