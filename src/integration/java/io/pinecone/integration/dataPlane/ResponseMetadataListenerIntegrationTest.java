package io.pinecone.integration.dataPlane;

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.configs.ResponseMetadata;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.helpers.TestResourcesManager;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.pinecone.commons.IndexInterface.buildUpsertVectorWithUnsignedIndices;
import static io.pinecone.helpers.BuildUpsertRequest.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the ResponseMetadataListener feature.
 * Verifies that response metadata is correctly captured for all data plane operations.
 */
public class ResponseMetadataListenerIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(ResponseMetadataListenerIntegrationTest.class);
    private static final TestResourcesManager resourceManager = TestResourcesManager.getInstance();
    private static final String namespace = RandomStringBuilder.build("resp-meta-ns", 8);
    private static final CopyOnWriteArrayList<ResponseMetadata> capturedMetadata = new CopyOnWriteArrayList<>();

    private static String indexName;
    private static Index index;
    private static int dimension;

    @BeforeAll
    public static void setUp() throws InterruptedException {
        dimension = resourceManager.getDimension();
        indexName = resourceManager.getOrCreateServerlessIndex();

        Pinecone pineconeClient = new Pinecone.Builder(System.getenv("PINECONE_API_KEY"))
                .withSourceTag("pinecone_test")
                .withResponseMetadataListener(metadata -> {
                    logger.debug("Captured metadata: {}", metadata);
                    capturedMetadata.add(metadata);
                })
                .build();

        index = pineconeClient.getIndexConnection(indexName);
    }

    @AfterAll
    public static void cleanUp() {
        if (index != null) {
            index.close();
        }
    }

    @Test
    public void testUpsertCapturesMetadata() {
        capturedMetadata.clear();

        List<String> ids = getIdsList(1);
        List<Float> values = generateVectorValuesByDimension(dimension);
        VectorWithUnsignedIndices vector = buildUpsertVectorWithUnsignedIndices(
                ids.get(0), values, null, null, null);
        index.upsert(Collections.singletonList(vector), namespace);

        assertFalse(capturedMetadata.isEmpty(), "Should have captured at least one metadata entry");

        ResponseMetadata metadata = findMetadataForOperation("upsert");
        assertNotNull(metadata, "Should have captured metadata for upsert operation");

        assertEquals("upsert", metadata.getOperationName());
        assertEquals(indexName, metadata.getIndexName());
        assertEquals(namespace, metadata.getNamespace());
        assertNotNull(metadata.getServerAddress());
        assertTrue(metadata.getServerAddress().contains("pinecone.io"));
        assertTrue(metadata.getClientDurationMs() >= 0, "Client duration should be non-negative");
        assertEquals("success", metadata.getStatus());
        assertEquals("OK", metadata.getGrpcStatusCode());
        assertNull(metadata.getErrorType());
        assertTrue(metadata.isSuccess());

        logMetadata("Upsert", metadata);
    }

    @Test
    public void testQueryCapturesMetadata() throws InterruptedException {
        capturedMetadata.clear();

        List<String> ids = getIdsList(1);
        List<Float> values = generateVectorValuesByDimension(dimension);
        VectorWithUnsignedIndices vector = buildUpsertVectorWithUnsignedIndices(
                ids.get(0), values, null, null, null);
        index.upsert(Collections.singletonList(vector), namespace);

        Thread.sleep(1000);
        capturedMetadata.clear();

        index.query(5, values, null, null, null, namespace, null, false, false);

        ResponseMetadata metadata = findMetadataForOperation("query");
        assertNotNull(metadata, "Should have captured metadata for query operation");

        assertEquals("query", metadata.getOperationName());
        assertEquals(indexName, metadata.getIndexName());
        assertEquals(namespace, metadata.getNamespace());
        assertEquals("success", metadata.getStatus());
        assertTrue(metadata.getClientDurationMs() >= 0);

        logMetadata("Query", metadata);
    }

    @Test
    public void testFetchCapturesMetadata() throws InterruptedException {
        capturedMetadata.clear();

        String vectorId = RandomStringBuilder.build("fetch-test", 8);
        List<Float> values = generateVectorValuesByDimension(dimension);
        VectorWithUnsignedIndices vector = buildUpsertVectorWithUnsignedIndices(
                vectorId, values, null, null, null);
        index.upsert(Collections.singletonList(vector), namespace);

        Thread.sleep(1000);
        capturedMetadata.clear();

        index.fetch(Collections.singletonList(vectorId), namespace);

        ResponseMetadata metadata = findMetadataForOperation("fetch");
        assertNotNull(metadata, "Should have captured metadata for fetch operation");

        assertEquals("fetch", metadata.getOperationName());
        assertEquals(indexName, metadata.getIndexName());
        assertEquals(namespace, metadata.getNamespace());
        assertEquals("success", metadata.getStatus());
        assertTrue(metadata.getClientDurationMs() >= 0);

        logMetadata("Fetch", metadata);
    }

    @Test
    public void testUpdateCapturesMetadata() throws InterruptedException {
        capturedMetadata.clear();

        String vectorId = RandomStringBuilder.build("update-test", 8);
        List<Float> values = generateVectorValuesByDimension(dimension);
        VectorWithUnsignedIndices vector = buildUpsertVectorWithUnsignedIndices(
                vectorId, values, null, null, null);
        index.upsert(Collections.singletonList(vector), namespace);

        Thread.sleep(1000);
        capturedMetadata.clear();

        List<Float> updatedValues = generateVectorValuesByDimension(dimension);
        index.update(vectorId, updatedValues, namespace);

        ResponseMetadata metadata = findMetadataForOperation("update");
        assertNotNull(metadata, "Should have captured metadata for update operation");

        assertEquals("update", metadata.getOperationName());
        assertEquals(indexName, metadata.getIndexName());
        assertEquals(namespace, metadata.getNamespace());
        assertEquals("success", metadata.getStatus());
        assertTrue(metadata.getClientDurationMs() >= 0);

        logMetadata("Update", metadata);
    }

    @Test
    public void testDeleteCapturesMetadata() throws InterruptedException {
        capturedMetadata.clear();

        String vectorId = RandomStringBuilder.build("delete-test", 8);
        List<Float> values = generateVectorValuesByDimension(dimension);
        VectorWithUnsignedIndices vector = buildUpsertVectorWithUnsignedIndices(
                vectorId, values, null, null, null);
        index.upsert(Collections.singletonList(vector), namespace);

        Thread.sleep(1000);
        capturedMetadata.clear();

        index.deleteByIds(Collections.singletonList(vectorId), namespace);

        ResponseMetadata metadata = findMetadataForOperation("delete");
        assertNotNull(metadata, "Should have captured metadata for delete operation");

        assertEquals("delete", metadata.getOperationName());
        assertEquals(indexName, metadata.getIndexName());
        assertEquals(namespace, metadata.getNamespace());
        assertEquals("success", metadata.getStatus());
        assertTrue(metadata.getClientDurationMs() >= 0);

        logMetadata("Delete", metadata);
    }

    @Test
    public void testMultipleOperationsCaptureAllMetadata() throws InterruptedException {
        capturedMetadata.clear();

        String vectorId = RandomStringBuilder.build("multi-test", 8);
        List<Float> values = generateVectorValuesByDimension(dimension);

        VectorWithUnsignedIndices vector = buildUpsertVectorWithUnsignedIndices(
                vectorId, values, null, null, null);
        index.upsert(Collections.singletonList(vector), namespace);

        Thread.sleep(500);

        index.query(5, values, null, null, null, namespace, null, false, false);
        index.fetch(Collections.singletonList(vectorId), namespace);

        List<Float> updatedValues = generateVectorValuesByDimension(dimension);
        index.update(vectorId, updatedValues, namespace);
        index.deleteByIds(Collections.singletonList(vectorId), namespace);

        assertTrue(capturedMetadata.size() >= 5, "Should have captured metadata for at least 5 operations");

        assertNotNull(findMetadataForOperation("upsert"), "Should have upsert metadata");
        assertNotNull(findMetadataForOperation("query"), "Should have query metadata");
        assertNotNull(findMetadataForOperation("fetch"), "Should have fetch metadata");
        assertNotNull(findMetadataForOperation("update"), "Should have update metadata");
        assertNotNull(findMetadataForOperation("delete"), "Should have delete metadata");

        logger.info("Captured {} metadata entries for multiple operations", capturedMetadata.size());
    }

    @Test
    public void testDefaultNamespaceCapturedCorrectly() {
        capturedMetadata.clear();

        List<Float> values = generateVectorValuesByDimension(dimension);
        String vectorId = RandomStringBuilder.build("default-ns-test", 8);
        VectorWithUnsignedIndices vector = buildUpsertVectorWithUnsignedIndices(
                vectorId, values, null, null, null);

        index.upsert(Collections.singletonList(vector), "");

        ResponseMetadata metadata = findMetadataForOperation("upsert");
        assertNotNull(metadata, "Should have captured metadata");
        assertEquals("", metadata.getNamespace(), "Default namespace should be empty string");
    }

    @Test
    public void testServerAddressMatchesIndexHost() {
        capturedMetadata.clear();

        List<Float> values = generateVectorValuesByDimension(dimension);
        String vectorId = RandomStringBuilder.build("host-test", 8);
        VectorWithUnsignedIndices vector = buildUpsertVectorWithUnsignedIndices(
                vectorId, values, null, null, null);
        index.upsert(Collections.singletonList(vector), namespace);

        ResponseMetadata metadata = findMetadataForOperation("upsert");
        assertNotNull(metadata, "Should have captured metadata");
        assertNotNull(metadata.getServerAddress());
        assertTrue(metadata.getServerAddress().contains("pinecone.io"),
                "Server address should be a Pinecone host");
    }

    private void logMetadata(String operation, ResponseMetadata metadata) {
        if (metadata.getServerDurationMs() != null) {
            logger.info("{} - Client: {}ms, Server: {}ms, Network overhead: {}ms",
                    operation,
                    metadata.getClientDurationMs(),
                    metadata.getServerDurationMs(),
                    metadata.getNetworkOverheadMs());
        } else {
            logger.info("{} - Client: {}ms (server duration header not present)",
                    operation, metadata.getClientDurationMs());
        }
    }

    private ResponseMetadata findMetadataForOperation(String operationName) {
        for (int i = capturedMetadata.size() - 1; i >= 0; i--) {
            ResponseMetadata metadata = capturedMetadata.get(i);
            if (operationName.equals(metadata.getOperationName())) {
                return metadata;
            }
        }
        return null;
    }
}
