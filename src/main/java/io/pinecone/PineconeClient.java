package io.pinecone;

import io.pinecone.exceptions.PineconeValidationException;

/**
 * Top-level client for connecting and making calls to Pinecone services. One instance can
 * be used to connect to multiple services and shared across threads.
 */
public final class PineconeClient {

    private final PineconeClientConfig config;

    /**
     * Create a new instance. Throws {@link PineconeValidationException} if configuration is invalid.
     * @param config User-level configuration for the client.
     */
    public PineconeClient(PineconeClientConfig config) {
        config.validate();
        this.config = config;
    }

    /**
     * Create a new connection to the specified Pinecone service or router. Throws {@link PineconeValidationException} if configuration is invalid.
     *
     * @param indexName The name of your pinecone Index.
     * @return A {@link PineconeConnection} for the service or router.
     */
    public PineconeConnection connect(String indexName) {
        return connect(new PineconeConnectionConfig()
                .withIndexName(indexName));
    }

    /**
     * Create a new connection to the Pinecone service or router specified in the config. Throws {@link PineconeValidationException} if configuration is invalid.
     * @param connectionConfig Config for the connection to be opened.
     * @return A {@link PineconeConnection} for the service or router.
     */
    public PineconeConnection connect(PineconeConnectionConfig connectionConfig) {
        return new PineconeConnection(config, connectionConfig);
    }

    public PineconeConnection connectWithUrl(String connectionUrl) {
        return connect(new PineconeConnectionConfig()
                .withConnectionUrl(connectionUrl));
    }
}