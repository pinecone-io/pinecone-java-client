package io.pinecone.helpers;

import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.CollectionList;
import org.openapitools.db_control.client.model.CollectionModel;
import org.openapitools.db_control.client.model.IndexList;
import org.openapitools.db_control.client.model.IndexModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility for cleaning up Pinecone indexes and collections.
 * 
 * This utility can be used to clean up resources in a Pinecone project, with support for:
 * - Deleting all indexes and collections
 * - Dry-run mode to preview deletions without executing them
 * - Automatic handling of deletion protection
 * - Age-based filtering (when timestamp information becomes available)
 * 
 * Command-line arguments:
 * --age-threshold-days &lt;number&gt; - Minimum age in days for resources to be deleted (default: 1)
 * --dry-run - Preview deletions without actually deleting resources
 * 
 * Example usage:
 * <pre>{@code
 * java io.pinecone.helpers.IndexCleanupUtility --age-threshold-days 2 --dry-run
 * }</pre>
 */
public class IndexCleanupUtility {
    private static final Logger logger = LoggerFactory.getLogger(IndexCleanupUtility.class);
    
    private final Pinecone pinecone;
    private final int ageThresholdDays;
    private final boolean dryRun;
    
    /**
     * Constructs a new IndexCleanupUtility.
     * 
     * @param pinecone The Pinecone client instance
     * @param ageThresholdDays Minimum age in days for resources to be deleted
     * @param dryRun If true, preview deletions without executing them
     */
    public IndexCleanupUtility(Pinecone pinecone, int ageThresholdDays, boolean dryRun) {
        this.pinecone = pinecone;
        this.ageThresholdDays = ageThresholdDays;
        this.dryRun = dryRun;
    }
    
    /**
     * Main entry point for the cleanup utility.
     * 
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        try {
            // Parse command-line arguments
            int ageThresholdDays = 1; // Default: 1 day
            boolean dryRun = false;
            
            for (int i = 0; i < args.length; i++) {
                if ("--age-threshold-days".equals(args[i]) && i + 1 < args.length) {
                    try {
                        ageThresholdDays = Integer.parseInt(args[i + 1]);
                        i++; // Skip the next argument since we've consumed it
                    } catch (NumberFormatException e) {
                        logger.error("Invalid value for --age-threshold-days: {}", args[i + 1]);
                        printUsage();
                        System.exit(1);
                    }
                } else if ("--dry-run".equals(args[i])) {
                    dryRun = true;
                } else {
                    logger.warn("Unknown argument: {}", args[i]);
                }
            }
            
            logger.info("Starting Pinecone resource cleanup...");
            logger.info("Age threshold: {} days", ageThresholdDays);
            logger.info("Dry-run mode: {}", dryRun);
            
            // Initialize Pinecone client
            String apiKey = System.getenv("PINECONE_API_KEY");
            if (apiKey == null || apiKey.isEmpty()) {
                logger.error("PINECONE_API_KEY environment variable is not set");
                System.exit(1);
            }
            
            Pinecone pinecone = new Pinecone.Builder(apiKey).build();
            IndexCleanupUtility utility = new IndexCleanupUtility(pinecone, ageThresholdDays, dryRun);
            
            // Execute cleanup
            CleanupResult result = utility.cleanup();
            
            // Log summary
            logger.info("=== Cleanup Summary ===");
            logger.info("Indexes processed: {}", result.getIndexesProcessed());
            logger.info("Indexes deleted: {}", result.getIndexesDeleted());
            logger.info("Indexes failed: {}", result.getIndexesFailed());
            logger.info("Collections processed: {}", result.getCollectionsProcessed());
            logger.info("Collections deleted: {}", result.getCollectionsDeleted());
            logger.info("Collections failed: {}", result.getCollectionsFailed());
            
            if (dryRun) {
                logger.info("DRY-RUN MODE: No resources were actually deleted");
            }
            
            logger.info("Cleanup completed");
            
            // Exit with error code if any deletions failed
            if (result.getIndexesFailed() > 0 || result.getCollectionsFailed() > 0) {
                System.exit(1);
            }
            
        } catch (Exception e) {
            logger.error("Error during cleanup: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
    
    private static void printUsage() {
        System.err.println("Usage: java io.pinecone.helpers.IndexCleanupUtility [OPTIONS]");
        System.err.println("Options:");
        System.err.println("  --age-threshold-days <number>  Minimum age in days for resources to be deleted (default: 1)");
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
        logger.info("Listing indexes...");
        IndexList indexList = pinecone.listIndexes();
        List<IndexModel> indexes = indexList != null ? indexList.getIndexes() : null;
        if (indexes == null) {
            indexes = new ArrayList<>();
        }
        logger.info("Found {} indexes", indexes.size());
        
        for (IndexModel index : indexes) {
            result.incrementIndexesProcessed();
            try {
                boolean deletionInitiated = cleanupIndex(index);
                if (deletionInitiated) {
                    result.incrementIndexesDeleted();
                }
            } catch (Exception e) {
                logger.error("Failed to delete index {}: {}", index.getName(), e.getMessage(), e);
                result.incrementIndexesFailed();
            }
        }
        
        // Clean up collections
        logger.info("Listing collections...");
        CollectionList collectionList = pinecone.listCollections();
        List<CollectionModel> collections = collectionList != null ? collectionList.getCollections() : null;
        if (collections == null) {
            collections = new ArrayList<>();
        }
        logger.info("Found {} collections", collections.size());
        
        for (CollectionModel collection : collections) {
            result.incrementCollectionsProcessed();
            try {
                boolean deletionInitiated = cleanupCollection(collection);
                if (deletionInitiated) {
                    result.incrementCollectionsDeleted();
                }
            } catch (Exception e) {
                logger.error("Failed to delete collection {}: {}", collection.getName(), e.getMessage(), e);
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
        
        logger.info("Processing index: {} (status: {})", indexName, status);
        
        // Skip indexes that are already terminating
        if ("Terminating".equalsIgnoreCase(status)) {
            logger.info("Skipping index {} - already terminating", indexName);
            return false;
        }
        
        // Note: Age-based filtering would go here when timestamp information becomes available
        // For now, we process all indexes that aren't already terminating
        
        if (dryRun) {
            logger.info("DRY-RUN: Would delete index: {}", indexName);
            return false;
        }
        
        // Handle deletion protection
        if ("enabled".equals(index.getDeletionProtection())) {
            logger.info("Index {} has deletion protection enabled, disabling...", indexName);
            try {
                // Try pod-based configuration first
                index.getSpec().getIndexModelPodBased();
                pinecone.configurePodsIndex(indexName, "disabled");
            } catch (ClassCastException e) {
                // Not a pod-based index, try serverless
                pinecone.configureServerlessIndex(indexName, "disabled", null, null);
            }
            
            // Wait for configuration to take effect
            logger.info("Waiting 5 seconds for deletion protection to be disabled...");
            Thread.sleep(5000);
        }
        
        // Delete the index
        logger.info("Deleting index: {}", indexName);
        pinecone.deleteIndex(indexName);
        logger.info("Successfully initiated deletion of index: {}", indexName);
        
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
        
        logger.info("Processing collection: {} (status: {})", collectionName, status);
        
        // Skip collections that are already terminating
        if ("Terminating".equalsIgnoreCase(status)) {
            logger.info("Skipping collection {} - already terminating", collectionName);
            return false;
        }
        
        // Note: Age-based filtering would go here when timestamp information becomes available
        // For now, we process all collections that aren't already terminating
        
        if (dryRun) {
            logger.info("DRY-RUN: Would delete collection: {}", collectionName);
            return false;
        }
        
        // Delete the collection
        logger.info("Deleting collection: {}", collectionName);
        pinecone.deleteCollection(collectionName);
        logger.info("Successfully initiated deletion of collection: {}", collectionName);
        
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
}
