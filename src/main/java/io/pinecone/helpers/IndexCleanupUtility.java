package io.pinecone.helpers;

import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.CollectionList;
import org.openapitools.db_control.client.model.CollectionModel;
import org.openapitools.db_control.client.model.IndexList;
import org.openapitools.db_control.client.model.IndexModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for cleaning up Pinecone indexes and collections created by the integration tests.
 * <p>
 * The API does not report when an index or collection was created, so integration tests name their resources with
 * {@link #testResourcePrefix()}, which encodes the CI run and the creation time. This utility only ever deletes
 * resources whose names carry that prefix; anything else in the project is left alone.
 * <p>
 * Command-line arguments:
 * --run-id &lt;id&gt; - Delete only resources created by this CI run (GITHUB_RUN_ID), regardless of age
 * --age-threshold-days &lt;number&gt; - Without --run-id, minimum age in days for resources to be deleted (default: 1)
 * --dry-run - Preview deletions without actually deleting resources
 * <p>
 * Example usage:
 * <pre>{@code
 * java io.pinecone.helpers.IndexCleanupUtility --age-threshold-days 2 --dry-run
 * java io.pinecone.helpers.IndexCleanupUtility --run-id "$GITHUB_RUN_ID"
 * }</pre>
 */
public class IndexCleanupUtility {
    /** Set by GitHub Actions in every job; scopes test resource names to one workflow run. */
    public static final String RUN_ID_ENV = "GITHUB_RUN_ID";
    private static final String LOCAL_RUN_TAG = "local";
    private static final Pattern TEST_RESOURCE_NAME = Pattern.compile("^ci([0-9a-z]{1,12})-([0-9a-z]{1,12})-.+");

    private final Pinecone pinecone;
    private final int ageThresholdDays;
    private final String runTag;
    private final boolean dryRun;

    /**
     * Constructs a new IndexCleanupUtility that deletes test resources older than {@code ageThresholdDays}.
     *
     * @param pinecone The Pinecone client instance
     * @param ageThresholdDays Minimum age in days for resources to be deleted
     * @param dryRun If true, preview deletions without executing them
     */
    public IndexCleanupUtility(Pinecone pinecone, int ageThresholdDays, boolean dryRun) {
        this(pinecone, ageThresholdDays, null, dryRun);
    }

    /**
     * Constructs a new IndexCleanupUtility.
     *
     * @param pinecone The Pinecone client instance
     * @param ageThresholdDays Minimum age in days for resources to be deleted; ignored when {@code runId} is set
     * @param runId If non-null, delete only test resources created by this CI run, regardless of age
     * @param dryRun If true, preview deletions without executing them
     */
    public IndexCleanupUtility(Pinecone pinecone, int ageThresholdDays, String runId, boolean dryRun) {
        this.pinecone = pinecone;
        this.ageThresholdDays = ageThresholdDays;
        this.runTag = runId == null ? null : runTag(runId);
        this.dryRun = dryRun;
    }

    /**
     * Returns the prefix integration tests put on every index and collection name they create, so this utility can
     * tell which run created a resource and when. Reads the run id from {@value #RUN_ID_ENV}; when it is unset, as in
     * a local run, the resource is tagged as local and only age-based cleanup will remove it.
     *
     * @return a prefix such as {@code ci<run>-<minute>-}, 16 characters for current GitHub run ids
     */
    public static String testResourcePrefix() {
        return testResourcePrefix(System.getenv(RUN_ID_ENV), System.currentTimeMillis());
    }

    static String testResourcePrefix(String runId, long createdAtMillis) {
        return "ci" + runTag(runId) + "-" + Long.toString(TimeUnit.MILLISECONDS.toMinutes(createdAtMillis), 36) + "-";
    }

    // Base 36 keeps the prefix short enough to fit test names within the 45-character index name limit.
    static String runTag(String runId) {
        if (runId == null || runId.trim().isEmpty()) {
            return LOCAL_RUN_TAG;
        }
        try {
            return Long.toString(Long.parseLong(runId.trim()), 36);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Run id must be numeric: " + runId, e);
        }
    }

    /**
     * Decides whether a resource is in scope for this cleanup, logging the reason when it is not.
     */
    boolean isInScope(String name) {
        Matcher matcher = TEST_RESOURCE_NAME.matcher(name);
        if (!matcher.matches()) {
            logInfo("Skipping %s - not created by the integration tests", name);
            return false;
        }
        if (runTag != null) {
            if (!runTag.equals(matcher.group(1))) {
                logInfo("Skipping %s - created by another run", name);
                return false;
            }
            return true;
        }
        long createdAtMinutes = Long.parseLong(matcher.group(2), 36);
        long ageMinutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()) - createdAtMinutes;
        if (ageMinutes < TimeUnit.DAYS.toMinutes(ageThresholdDays)) {
            logInfo("Skipping %s - younger than %d days", name, ageThresholdDays);
            return false;
        }
        return true;
    }
    
    /**
     * Main entry point for the cleanup utility.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        try {
            ParsedArgs parsedArgs = parseArgs(args);

            logInfo("Starting Pinecone resource cleanup...");
            if (parsedArgs.runId != null) {
                logInfo("Run id: %s", parsedArgs.runId);
            } else {
                logInfo("Age threshold: %d days", parsedArgs.ageThresholdDays);
            }
            logInfo("Dry-run mode: %s", parsedArgs.dryRun);

            // Initialize Pinecone client
            String apiKey = System.getenv("PINECONE_API_KEY");
            if (apiKey == null || apiKey.isEmpty()) {
                logError("PINECONE_API_KEY environment variable is not set");
                System.exit(1);
            }

            Pinecone pinecone = new Pinecone.Builder(apiKey).build();
            IndexCleanupUtility utility = new IndexCleanupUtility(
                pinecone,
                parsedArgs.ageThresholdDays,
                parsedArgs.runId,
                parsedArgs.dryRun
            );

            // Execute cleanup
            CleanupResult result = utility.cleanup();

            // Log summary
            logInfo("=== Cleanup Summary ===");
            logInfo("Indexes processed: %d", result.getIndexesProcessed());
            logInfo("Indexes deleted: %d", result.getIndexesDeleted());
            logInfo("Indexes failed: %d", result.getIndexesFailed());
            logInfo("Collections processed: %d", result.getCollectionsProcessed());
            logInfo("Collections deleted: %d", result.getCollectionsDeleted());
            logInfo("Collections failed: %d", result.getCollectionsFailed());

            if (parsedArgs.dryRun) {
                logInfo("DRY-RUN MODE: No resources were actually deleted");
            }

            logInfo("Cleanup completed");

            // Exit with error code if any deletions failed
            if (result.getIndexesFailed() > 0 || result.getCollectionsFailed() > 0) {
                System.exit(1);
            }

        } catch (Exception e) {
            logError("Error during cleanup: " + e.getMessage(), e);
            System.exit(1);
        }
    }

    static ParsedArgs parseArgs(String[] args) {
        int ageThresholdDays = 1; // Default: 1 day
        String runId = null;
        boolean dryRun = false;

        for (int i = 0; i < args.length; i++) {
            if ("--age-threshold-days".equals(args[i])) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("--age-threshold-days requires a value");
                }
                String value = args[i + 1];
                try {
                    ageThresholdDays = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid value for --age-threshold-days: " + value, e);
                }
                if (ageThresholdDays < 0) {
                    throw new IllegalArgumentException("--age-threshold-days must be >= 0");
                }
                i++; // Skip the next argument since we've consumed it
            } else if ("--run-id".equals(args[i])) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("--run-id requires a value");
                }
                runId = args[i + 1];
                if (runId.trim().isEmpty()) {
                    throw new IllegalArgumentException("--run-id must not be blank");
                }
                runTag(runId);
                i++;
            } else if ("--dry-run".equals(args[i])) {
                dryRun = true;
            } else {
                logWarn("Unknown argument: %s", args[i]);
            }
        }

        return new ParsedArgs(ageThresholdDays, runId, dryRun);
    }

    private static void printUsage() {
        System.err.println("Usage: java io.pinecone.helpers.IndexCleanupUtility [OPTIONS]");
        System.err.println("Options:");
        System.err.println("  --run-id <id>                  Delete only resources created by this CI run, regardless of age");
        System.err.println("  --age-threshold-days <number>  Without --run-id, minimum age in days for resources to be deleted (default: 1)");
        System.err.println("  --dry-run                      Preview deletions without executing them");
    }

    /**
     * Executes the cleanup operation, deleting indexes and collections that match the criteria.
     *
     * @return A CleanupResult containing statistics about the cleanup operation
     * @throws Exception if an error occurs during cleanup
     */
    public CleanupResult cleanup() throws Exception {
        CleanupResult result = new CleanupResult();

        // Clean up indexes
        logInfo("Listing indexes...");
        IndexList indexList = pinecone.listIndexes();
        List<IndexModel> indexes = indexList != null ? indexList.getIndexes() : null;
        if (indexes == null) {
            indexes = new ArrayList<>();
        }
        logInfo("Found %d indexes", indexes.size());

        for (IndexModel index : indexes) {
            result.incrementIndexesProcessed();
            try {
                boolean deletionInitiated = cleanupIndex(index);
                if (deletionInitiated) {
                    result.incrementIndexesDeleted();
                }
            } catch (Exception e) {
                logError(String.format("Failed to delete index %s: %s", index.getName(), e.getMessage()), e);
                result.incrementIndexesFailed();
            }
        }

        // Clean up collections
        logInfo("Listing collections...");
        CollectionList collectionList = pinecone.listCollections();
        List<CollectionModel> collections = collectionList != null ? collectionList.getCollections() : null;
        if (collections == null) {
            collections = new ArrayList<>();
        }
        logInfo("Found %d collections", collections.size());

        for (CollectionModel collection : collections) {
            result.incrementCollectionsProcessed();
            try {
                boolean deletionInitiated = cleanupCollection(collection);
                if (deletionInitiated) {
                    result.incrementCollectionsDeleted();
                }
            } catch (Exception e) {
                logError(String.format("Failed to delete collection %s: %s", collection.getName(), e.getMessage()), e);
                result.incrementCollectionsFailed();
            }
        }

        return result;
    }

    /**
     * Cleans up a single index.
     *
     * @param index The index to clean up
     * @throws Exception if an error occurs during cleanup
     */
    private boolean cleanupIndex(IndexModel index) throws Exception {
        String indexName = index.getName();
        String status = index.getStatus() != null && index.getStatus().getState() != null 
            ? index.getStatus().getState() 
            : "unknown";

        logInfo("Processing index: %s (status: %s)", indexName, status);

        // Skip indexes that are already terminating
        if ("Terminating".equalsIgnoreCase(status)) {
            logInfo("Skipping index %s - already terminating", indexName);
            return false;
        }

        if (!isInScope(indexName)) {
            return false;
        }

        if (dryRun) {
            logInfo("DRY-RUN: Would delete index: %s", indexName);
            return false;
        }

        // Handle deletion protection
        if ("enabled".equals(index.getDeletionProtection())) {
            logInfo("Index %s has deletion protection enabled, disabling...", indexName);
            try {
                // Try pod-based configuration first
                index.getSpec().getIndexModelPodBased();
                pinecone.configurePodsIndex(indexName, "disabled");
            } catch (ClassCastException e) {
                // Not a pod-based index, try serverless
                pinecone.configureServerlessIndex(indexName, "disabled", null, null);
            }

            // Wait for configuration to take effect
            logInfo("Waiting 5 seconds for deletion protection to be disabled...");
            Thread.sleep(5000);
        }

        // Delete the index
        logInfo("Deleting index: %s", indexName);
        pinecone.deleteIndex(indexName);
        logInfo("Successfully initiated deletion of index: %s", indexName);

        // Add small delay to avoid rate limiting
        Thread.sleep(1000);
        return true;
    }

    /**
     * Cleans up a single collection.
     *
     * @param collection The collection to clean up
     * @throws Exception if an error occurs during cleanup
     */
    private boolean cleanupCollection(CollectionModel collection) throws Exception {
        String collectionName = collection.getName();
        String status = collection.getStatus() != null ? collection.getStatus() : "unknown";

        logInfo("Processing collection: %s (status: %s)", collectionName, status);

        // Skip collections that are already terminating
        if ("Terminating".equalsIgnoreCase(status)) {
            logInfo("Skipping collection %s - already terminating", collectionName);
            return false;
        }

        if (!isInScope(collectionName)) {
            return false;
        }

        if (dryRun) {
            logInfo("DRY-RUN: Would delete collection: %s", collectionName);
            return false;
        }

        // Delete the collection
        logInfo("Deleting collection: %s", collectionName);
        pinecone.deleteCollection(collectionName);
        logInfo("Successfully initiated deletion of collection: %s", collectionName);

        // Add small delay to avoid rate limiting
        Thread.sleep(1000);
        return true;
    }

    /**
     * Result of a cleanup operation, containing statistics about what was processed and deleted.
     */
    public static class CleanupResult {
        private int indexesProcessed = 0;
        private int indexesDeleted = 0;
        private int indexesFailed = 0;
        private int collectionsProcessed = 0;
        private int collectionsDeleted = 0;
        private int collectionsFailed = 0;
        
        public int getIndexesProcessed() { return indexesProcessed; }
        public int getIndexesDeleted() { return indexesDeleted; }
        public int getIndexesFailed() { return indexesFailed; }
        public int getCollectionsProcessed() { return collectionsProcessed; }
        public int getCollectionsDeleted() { return collectionsDeleted; }
        public int getCollectionsFailed() { return collectionsFailed; }
        
        void incrementIndexesProcessed() { indexesProcessed++; }
        void incrementIndexesDeleted() { indexesDeleted++; }
        void incrementIndexesFailed() { indexesFailed++; }
        void incrementCollectionsProcessed() { collectionsProcessed++; }
        void incrementCollectionsDeleted() { collectionsDeleted++; }
        void incrementCollectionsFailed() { collectionsFailed++; }
    }

    static final class ParsedArgs {
        final int ageThresholdDays;
        final String runId;
        final boolean dryRun;

        ParsedArgs(int ageThresholdDays, String runId, boolean dryRun) {
            this.ageThresholdDays = ageThresholdDays;
            this.runId = runId;
            this.dryRun = dryRun;
        }
    }

    private static void logInfo(String format, Object... args) {
        log("INFO", format, args);
    }

    private static void logWarn(String format, Object... args) {
        log("WARN", format, args);
    }

    private static void logError(String message) {
        System.err.println("[ERROR] " + message);
    }

    private static void logError(String message, Throwable t) {
        System.err.println("[ERROR] " + message);
        if (t != null) {
            t.printStackTrace(System.err);
        }
    }

    private static void log(String level, String format, Object... args) {
        String msg;
        try {
            msg = (args == null || args.length == 0) ? format : String.format(format, args);
        } catch (Exception e) {
            msg = format;
        }
        System.err.println("[" + level + "] " + msg);
    }
}
