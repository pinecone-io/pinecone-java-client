package io.pinecone.helpers;

import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeNotFoundException;
import org.openapitools.db_control.client.model.IndexModel;
import org.openapitools.db_control.client.model.IndexModelPodBased;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Deletes test indexes and collections and waits until they are gone. A resource that is already gone (404) counts
 * as deleted, so teardown can run more than once or after a test deleted the resource itself. An index with deletion
 * protection enabled has it turned off first.
 */
public class ResourceCleanup {
    private static final Logger logger = LoggerFactory.getLogger(ResourceCleanup.class);
    private static final long DEFAULT_TIMEOUT_MS = 120000;
    private static final long DEFAULT_INTERVAL_MS = 2000;

    public interface Step {
        void run() throws Exception;
    }

    public static void deleteIndex(Pinecone pinecone, String indexName) throws InterruptedException {
        deleteIndex(pinecone, indexName, DEFAULT_TIMEOUT_MS, DEFAULT_INTERVAL_MS);
    }

    static void deleteIndex(Pinecone pinecone, String indexName, long timeoutMs, long intervalMs)
            throws InterruptedException {
        try {
            IndexModel index = pinecone.describeIndex(indexName);
            if ("enabled".equals(index.getDeletionProtection())) {
                disableDeletionProtection(pinecone, index);
            }
            pinecone.deleteIndex(indexName);
        } catch (PineconeNotFoundException e) {
            logger.info("Index {} was already deleted", indexName);
            return;
        }
        waitUntilIndexIsDeleted(pinecone, indexName, timeoutMs, intervalMs);
    }

    /** Polls describeIndex until it returns 404, failing after two minutes. */
    public static void waitUntilIndexIsDeleted(Pinecone pinecone, String indexName) throws InterruptedException {
        waitUntilIndexIsDeleted(pinecone, indexName, DEFAULT_TIMEOUT_MS, DEFAULT_INTERVAL_MS);
    }

    static void waitUntilIndexIsDeleted(Pinecone pinecone, String indexName, long timeoutMs, long intervalMs)
            throws InterruptedException {
        Poll.until("index " + indexName + " is deleted", timeoutMs, intervalMs, () -> {
            try {
                pinecone.describeIndex(indexName);
                return false;
            } catch (PineconeNotFoundException e) {
                return true;
            }
        }, gone -> gone);
    }

    private static void disableDeletionProtection(Pinecone pinecone, IndexModel index) {
        logger.info("Disabling deletion protection on index {}", index.getName());
        if (index.getSpec() != null && index.getSpec().getActualInstance() instanceof IndexModelPodBased) {
            pinecone.configurePodsIndex(index.getName(), "disabled");
        } else {
            pinecone.configureServerlessIndex(index.getName(), "disabled", null, null);
        }
    }

    public static void deleteCollection(Pinecone pinecone, String collectionName) throws InterruptedException {
        deleteCollection(pinecone, collectionName, DEFAULT_TIMEOUT_MS, DEFAULT_INTERVAL_MS);
    }

    static void deleteCollection(Pinecone pinecone, String collectionName, long timeoutMs, long intervalMs)
            throws InterruptedException {
        try {
            pinecone.deleteCollection(collectionName);
        } catch (PineconeNotFoundException e) {
            logger.info("Collection {} was already deleted", collectionName);
            return;
        }
        Poll.until("collection " + collectionName + " is deleted", timeoutMs, intervalMs, () -> {
            try {
                pinecone.describeCollection(collectionName);
                return false;
            } catch (PineconeNotFoundException e) {
                return true;
            }
        }, gone -> gone);
    }

    /**
     * Runs every step concurrently, so deletes wait on the backend in parallel. Every step runs even if others fail;
     * each failure is logged under its name. Returns the names of the failed steps, in the order given.
     */
    public static List<String> runAll(LinkedHashMap<String, Step> steps) {
        List<String> failed = new ArrayList<>();
        if (steps.isEmpty()) {
            return failed;
        }
        ExecutorService pool = Executors.newFixedThreadPool(steps.size());
        try {
            Map<String, Future<?>> running = new LinkedHashMap<>();
            for (Map.Entry<String, Step> step : steps.entrySet()) {
                running.put(step.getKey(), pool.submit(() -> {
                    step.getValue().run();
                    return null;
                }));
            }
            for (Map.Entry<String, Future<?>> step : running.entrySet()) {
                try {
                    step.getValue().get();
                } catch (ExecutionException e) {
                    failed.add(step.getKey());
                    logger.error("Cleanup step failed: {}", step.getKey(), e.getCause());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    failed.add(step.getKey());
                    logger.error("Interrupted while waiting for cleanup step: {}", step.getKey(), e);
                }
            }
        } finally {
            pool.shutdownNow();
        }
        if (!failed.isEmpty()) {
            logger.error("{} of {} cleanup steps failed: {}", failed.size(), steps.size(), failed);
        }
        return failed;
    }
}
