package io.pinecone.helpers;

import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.IndexModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexCleanupUtility {
    private static final Logger logger = LoggerFactory.getLogger(IndexCleanupUtility.class);
    
    public static void main(String[] args) {
        try {
            logger.info("Starting Pinecone index cleanup...");
            Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
            
            for(IndexModel model : pinecone.listIndexes().getIndexes()) {
                String indexName = model.getName();
                if(model.getDeletionProtection().equals("enabled")) {
                    if(model.getSpec().getPod() != null) {
                        pinecone.configurePodsIndex(indexName, "disabled");
                    }
                    pinecone.configureServerlessIndex(indexName, "disabled", null, null);
                }
                Thread.sleep(5000);
                pinecone.deleteIndex(indexName);
            }
            
            logger.info("Index cleanup completed");
            
        } catch (Exception e) {
            logger.error("Error during cleanup: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}
