package io.pinecone.helpers;

import io.pinecone.PineconeClientConfig;
import io.pinecone.PineconeConnection;
import io.pinecone.PineconeIndexOperationClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class CleanUp {
    private static final Logger logger = LoggerFactory.getLogger(PineconeConnection.class);

    public static void main(String[] args) {
        String pineconeEnvironment = System.getenv("PINECONE_ENVIRONMENT");
        String pineconeApiKey = System.getenv("PINECONE_API_KEY");

        PineconeClientConfig config = new PineconeClientConfig()
                .withApiKey(System.getenv("PINECONE_API_KEY"))
                .withEnvironment(System.getenv("PINECONE_ENVIRONMENT"))
                .withServerSideTimeoutSec(10);
        PineconeIndexOperationClient pinecone = new PineconeIndexOperationClient(config);

        checkEnvironmentVariable("PINECONE_ENVIRONMENT", pineconeEnvironment);
        checkEnvironmentVariable("PINECONE_API_KEY", pineconeApiKey);

        try {
            List<String> indexes = pinecone.listIndexes();
            for (String index : indexes) {
                logger.info("Deleting index: " + index);
                pinecone.deleteIndex(index);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void checkEnvironmentVariable(String envVar, String value) {
        if (value == null || value.isEmpty()) {
            logger.error("WARNING: Missing environment variable " + envVar);
        } else {
            logger.info("INFO: Found environment variable " + envVar);
        }
    }
}
