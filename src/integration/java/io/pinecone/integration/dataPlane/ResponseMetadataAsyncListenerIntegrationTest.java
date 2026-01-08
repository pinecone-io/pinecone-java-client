package io.pinecone.integration.dataPlane;

import com.google.common.util.concurrent.ListenableFuture;
import io.pinecone.clients.AsyncIndex;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.pinecone.commons.IndexInterface.buildUpsertVectorWithUnsignedIndices;
import static io.pinecone.helpers.BuildUpsertRequest.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the ResponseMetadataListener feature with AsyncIndex.
 * Verifies that response metadata is correctly captured for async data plane operations.
 */
public class ResponseMetadataAsyncListenerIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(ResponseMetadataAsyncListenerIntegrationTest.class);
    private static final TestResourcesManager resourceManager = TestResourcesManager.getInstance();
    private static final String namespace = RandomStringBuilder.build("async-resp-meta-ns", 8);
    private static final CopyOnWriteArrayList<ResponseMetadata> capturedMetadata = new CopyOnWriteArrayList<>();

    private static String indexName;
    private static AsyncIndex asyncIndex;
    private static int dimension;

    @BeforeAll
    public static void setUp() throws InterruptedException {
        dimension = resourceManager.getDimension();
        indexName = resourceManager.getOrCreateServerlessIndex();

        Pinecone pineconeClient = new Pinecone.Builder(System.getenv("PINECONE_API_KEY"))
                .withSourceTag("pinecone_test")
                .withResponseMetadataListener(metadata -> {
                    logger.debug("Captured async metadata: {}", metadata);
                    capturedMetadata.add(metadata);
                })
                .build();

        asyncIndex = pineconeClient.getAsyncIndexConnection(indexName);
    }

    @AfterAll
    public static void cleanUp() {
        if (asyncIndex != null) {
            asyncIndex.close();
        }
    }

    @Test
    public void testAsyncUpsertCapturesMetadata() throws ExecutionException, InterruptedException, TimeoutException {
        capturedMetadata.clear();

        String vectorId = RandomStringBuilder.build("async-upsert", 8);
        List<Float> values = generateVectorValuesByDimension(dimension);
        VectorWithUnsignedIndices vector = buildUpsertVectorWithUnsignedIndices(
                vectorId, values, null, null, null);

        asyncIndex.upsert(Collections.singletonList(vector), namespace).get(30, TimeUnit.SECONDS);
        Thread.sleep(100);

        ResponseMetadata metadata = findMetadataForOperation("upsert");
        assertNotNull(metadata, "Should have captured metadata for async upsert operation");

        assertEquals("upsert", metadata.getOperationName());
        assertEquals(indexName, metadata.getIndexName());
        assertEquals(namespace, metadata.getNamespace());
        assertEquals("success", metadata.getStatus());
        assertTrue(metadata.getClientDurationMs() >= 0);

        logMetadata("Async Upsert", metadata);
    }

    @Test
    public void testAsyncQueryCapturesMetadata() throws ExecutionException, InterruptedException, TimeoutException {
        capturedMetadata.clear();

        String vectorId = RandomStringBuilder.build("async-query", 8);
        List<Float> values = generateVectorValuesByDimension(dimension);
        VectorWithUnsignedIndices vector = buildUpsertVectorWithUnsignedIndices(
                vectorId, values, null, null, null);
        asyncIndex.upsert(Collections.singletonList(vector), namespace).get(30, TimeUnit.SECONDS);

        Thread.sleep(1000);
        capturedMetadata.clear();

        asyncIndex.query(5, values, null, null, null, namespace, null, false, false).get(30, TimeUnit.SECONDS);
        Thread.sleep(100);

        ResponseMetadata metadata = findMetadataForOperation("query");
        assertNotNull(metadata, "Should have captured metadata for async query operation");

        assertEquals("query", metadata.getOperationName());
        assertEquals(indexName, metadata.getIndexName());
        assertEquals("success", metadata.getStatus());

        logMetadata("Async Query", metadata);
    }

    @Test
    public void testAsyncFetchCapturesMetadata() throws ExecutionException, InterruptedException, TimeoutException {
        capturedMetadata.clear();

        String vectorId = RandomStringBuilder.build("async-fetch", 8);
        List<Float> values = generateVectorValuesByDimension(dimension);
        VectorWithUnsignedIndices vector = buildUpsertVectorWithUnsignedIndices(
                vectorId, values, null, null, null);
        asyncIndex.upsert(Collections.singletonList(vector), namespace).get(30, TimeUnit.SECONDS);

        Thread.sleep(1000);
        capturedMetadata.clear();

        asyncIndex.fetch(Collections.singletonList(vectorId), namespace).get(30, TimeUnit.SECONDS);
        Thread.sleep(100);

        ResponseMetadata metadata = findMetadataForOperation("fetch");
        assertNotNull(metadata, "Should have captured metadata for async fetch operation");

        assertEquals("fetch", metadata.getOperationName());
        assertEquals(indexName, metadata.getIndexName());
        assertEquals("success", metadata.getStatus());

        logMetadata("Async Fetch", metadata);
    }

    @Test
    public void testAsyncUpdateCapturesMetadata() throws ExecutionException, InterruptedException, TimeoutException {
        capturedMetadata.clear();

        String vectorId = RandomStringBuilder.build("async-update", 8);
        List<Float> values = generateVectorValuesByDimension(dimension);
        VectorWithUnsignedIndices vector = buildUpsertVectorWithUnsignedIndices(
                vectorId, values, null, null, null);
        asyncIndex.upsert(Collections.singletonList(vector), namespace).get(30, TimeUnit.SECONDS);

        Thread.sleep(1000);
        capturedMetadata.clear();

        List<Float> updatedValues = generateVectorValuesByDimension(dimension);
        asyncIndex.update(vectorId, updatedValues, namespace).get(30, TimeUnit.SECONDS);
        Thread.sleep(100);

        ResponseMetadata metadata = findMetadataForOperation("update");
        assertNotNull(metadata, "Should have captured metadata for async update operation");

        assertEquals("update", metadata.getOperationName());
        assertEquals(indexName, metadata.getIndexName());
        assertEquals("success", metadata.getStatus());

        logMetadata("Async Update", metadata);
    }

    @Test
    public void testAsyncDeleteCapturesMetadata() throws ExecutionException, InterruptedException, TimeoutException {
        capturedMetadata.clear();

        String vectorId = RandomStringBuilder.build("async-delete", 8);
        List<Float> values = generateVectorValuesByDimension(dimension);
        VectorWithUnsignedIndices vector = buildUpsertVectorWithUnsignedIndices(
                vectorId, values, null, null, null);
        asyncIndex.upsert(Collections.singletonList(vector), namespace).get(30, TimeUnit.SECONDS);

        Thread.sleep(1000);
        capturedMetadata.clear();

        asyncIndex.deleteByIds(Collections.singletonList(vectorId), namespace).get(30, TimeUnit.SECONDS);
        Thread.sleep(100);

        ResponseMetadata metadata = findMetadataForOperation("delete");
        assertNotNull(metadata, "Should have captured metadata for async delete operation");

        assertEquals("delete", metadata.getOperationName());
        assertEquals(indexName, metadata.getIndexName());
        assertEquals("success", metadata.getStatus());

        logMetadata("Async Delete", metadata);
    }

    @Test
    public void testAsyncOperationsThreadSafety() throws ExecutionException, InterruptedException, TimeoutException {
        capturedMetadata.clear();

        String vectorId1 = RandomStringBuilder.build("async-ts-1", 8);
        String vectorId2 = RandomStringBuilder.build("async-ts-2", 8);
        String vectorId3 = RandomStringBuilder.build("async-ts-3", 8);
        List<Float> values = generateVectorValuesByDimension(dimension);

        ListenableFuture<?> future1 = asyncIndex.upsert(
                Collections.singletonList(buildUpsertVectorWithUnsignedIndices(vectorId1, values, null, null, null)),
                namespace);
        ListenableFuture<?> future2 = asyncIndex.upsert(
                Collections.singletonList(buildUpsertVectorWithUnsignedIndices(vectorId2, values, null, null, null)),
                namespace);
        ListenableFuture<?> future3 = asyncIndex.upsert(
                Collections.singletonList(buildUpsertVectorWithUnsignedIndices(vectorId3, values, null, null, null)),
                namespace);

        future1.get(30, TimeUnit.SECONDS);
        future2.get(30, TimeUnit.SECONDS);
        future3.get(30, TimeUnit.SECONDS);
        Thread.sleep(100);

        int upsertCount = 0;
        for (ResponseMetadata m : capturedMetadata) {
            if ("upsert".equals(m.getOperationName())) {
                upsertCount++;
            }
        }

        assertTrue(upsertCount >= 3, "Should have captured metadata for all 3 concurrent upserts, got: " + upsertCount);

        for (ResponseMetadata metadata : capturedMetadata) {
            assertNotNull(metadata.getOperationName());
            assertNotNull(metadata.getIndexName());
            assertNotNull(metadata.getStatus());
            assertTrue(metadata.getClientDurationMs() >= 0);
        }

        logger.info("Captured {} metadata entries for concurrent async operations", capturedMetadata.size());
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
