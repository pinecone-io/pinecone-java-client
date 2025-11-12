package io.pinecone.integration.dataPlane;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.pinecone.clients.AsyncIndex;
import io.pinecone.clients.Index;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.helpers.TestResourcesManager;
import io.pinecone.proto.FetchByMetadataResponse;
import io.pinecone.proto.UpdateResponse;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.pinecone.helpers.AssertRetry.assertWithRetry;
import static io.pinecone.helpers.BuildUpsertRequest.*;
import static org.junit.jupiter.api.Assertions.*;

public class FetchAndUpdateByMetadataTest {

    private static final TestResourcesManager indexManager = TestResourcesManager.getInstance();
    private static Index index;
    private static AsyncIndex asyncIndex;
    private static final String namespace = RandomStringBuilder.build("ns", 8);

    @BeforeAll
    public static void setUp() throws InterruptedException {
        int dimension = indexManager.getDimension();
        index = indexManager.getOrCreateServerlessIndexConnection();
        asyncIndex = indexManager.getOrCreateServerlessAsyncIndexConnection();

        // Upsert vectors with metadata for testing
        int numOfVectors = 5;
        List<String> upsertIds = getIdsList(numOfVectors);
        List<VectorWithUnsignedIndices> vectorsToUpsert = new ArrayList<>(numOfVectors);

        // Upsert vectors with different metadata values
        for (int i = 0; i < numOfVectors; i++) {
            Struct metadata = generateMetadataStruct(i % 3, (i + 1) % 3);
            VectorWithUnsignedIndices vector = new VectorWithUnsignedIndices(
                    upsertIds.get(i),
                    generateVectorValuesByDimension(dimension),
                    metadata,
                    null
            );
            vectorsToUpsert.add(vector);
        }

        index.upsert(vectorsToUpsert, namespace);
        
        // Wait for vectors to be indexed
        Thread.sleep(5000);
    }

    @AfterAll
    public static void cleanUp() {
        index.close();
        asyncIndex.close();
    }

    @Test
    public void fetchByMetadataSyncTest() throws InterruptedException {
        HashMap<String, List<String>> metadataMap = createAndGetMetadataMap();
        String filterValue = metadataMap.get(metadataFields[0]).get(0);
        
        Struct filter = Struct.newBuilder()
                .putFields(metadataFields[0], Value.newBuilder()
                        .setStructValue(Struct.newBuilder()
                                .putFields("$eq", Value.newBuilder()
                                        .setStringValue(filterValue)
                                        .build()))
                        .build())
                .build();

        assertWithRetry(() -> {
            FetchByMetadataResponse response = index.fetchByMetadata(namespace, filter, 10, null);
            assertNotNull(response);
            assertTrue(response.getVectorsCount() > 0);
        }, 3);
    }

    @Test
    public void updateByMetadataSyncTest() throws InterruptedException {
        HashMap<String, List<String>> metadataMap = createAndGetMetadataMap();
        String filterValue = metadataMap.get(metadataFields[0]).get(0);
        
        Struct filter = Struct.newBuilder()
                .putFields(metadataFields[0], Value.newBuilder()
                        .setStructValue(Struct.newBuilder()
                                .putFields("$eq", Value.newBuilder()
                                        .setStringValue(filterValue)
                                        .build()))
                        .build())
                .build();

        Struct newMetadata = Struct.newBuilder()
                .putFields("updated", Value.newBuilder().setStringValue("true").build())
                .build();

        assertWithRetry(() -> {
            UpdateResponse response = index.updateByMetadata(filter, newMetadata, namespace, false);
            assertNotNull(response);
            assertTrue(response.getMatchedRecords() > 0);
        }, 3);
    }

    @Test
    public void fetchByMetadataAsyncTest() throws InterruptedException, ExecutionException {
        HashMap<String, List<String>> metadataMap = createAndGetMetadataMap();
        String filterValue = metadataMap.get(metadataFields[1]).get(0);
        
        Struct filter = Struct.newBuilder()
                .putFields(metadataFields[1], Value.newBuilder()
                        .setStructValue(Struct.newBuilder()
                                .putFields("$eq", Value.newBuilder()
                                        .setStringValue(filterValue)
                                        .build()))
                        .build())
                .build();

        assertWithRetry(() -> {
            FetchByMetadataResponse response = asyncIndex.fetchByMetadata(namespace, filter, 10, null).get();
            assertNotNull(response);
            assertTrue(response.getVectorsCount() > 0);
        }, 3);
    }

    @Test
    public void updateByMetadataAsyncTest() throws InterruptedException, ExecutionException {
        HashMap<String, List<String>> metadataMap = createAndGetMetadataMap();
        String filterValue = metadataMap.get(metadataFields[1]).get(0);
        
        Struct filter = Struct.newBuilder()
                .putFields(metadataFields[1], Value.newBuilder()
                        .setStructValue(Struct.newBuilder()
                                .putFields("$eq", Value.newBuilder()
                                        .setStringValue(filterValue)
                                        .build()))
                        .build())
                .build();

        Struct newMetadata = Struct.newBuilder()
                .putFields("async_updated", Value.newBuilder().setStringValue("true").build())
                .build();

        assertWithRetry(() -> {
            UpdateResponse response = asyncIndex.updateByMetadata(filter, newMetadata, namespace, false).get();
            assertNotNull(response);
            assertTrue(response.getMatchedRecords() > 0);
        }, 3);
    }
}
