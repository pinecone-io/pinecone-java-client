package io.pinecone.configs;

import io.grpc.ManagedChannel;
import io.pinecone.exceptions.PineconeValidationException;

import java.util.function.BiFunction;

/**
 * This class contains the connection-level configuration options for the Pinecone client.
 */
public class PineconeConnectionConfig {

    private BiFunction<PineconeClientConfig, PineconeConnectionConfig, ManagedChannel> customChannelBuilder;

    /**
     * Required service or router name to connect to.
     */
    private String indexName;

    private String connectionUrl;

    /**
     * Creates a new default config.
     */
    public PineconeConnectionConfig() {}

    protected PineconeConnectionConfig(PineconeConnectionConfig other) {
        indexName = other.indexName;
        connectionUrl = other.connectionUrl;
        customChannelBuilder = other.customChannelBuilder;
    }

    /**
     * @return See {@link PineconeConnectionConfig#indexName}.
     */
    public String getIndexName() {
        return indexName;
    }

    /**
     * @return A copy of this object with a new value for {@link PineconeConnectionConfig#indexName}.
     */
    public PineconeConnectionConfig withIndexName(String indexName) {
        PineconeConnectionConfig config = new PineconeConnectionConfig(this);
        config.indexName = indexName;
        return config;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public PineconeConnectionConfig withConnectionUrl(String connectionUrl) {
        PineconeConnectionConfig config = new PineconeConnectionConfig(this);
        config.connectionUrl = connectionUrl;
        return config;
    }

    public BiFunction<PineconeClientConfig, PineconeConnectionConfig, ManagedChannel> getCustomChannelBuilder() {
        return customChannelBuilder;
    }

    public PineconeConnectionConfig withCustomChannelBuilder(BiFunction<PineconeClientConfig, PineconeConnectionConfig, ManagedChannel> customChannelBuilder) {
        PineconeConnectionConfig config = new PineconeConnectionConfig(this);
        config.customChannelBuilder = customChannelBuilder;
        return config;
    }

    void validate() {
        if (indexName == null && connectionUrl == null)
            throw new PineconeValidationException("Invalid PineconeConnectionConfig, indexName or connection url must be specified.");
    }

    @Override
    public String toString() {
        return "PineconeConnectionConfig("
                + "customChannelBuilder=" + getCustomChannelBuilder()
                + ", indexName=" + getIndexName()
                + ", connectionUrl=" + getConnectionUrl()
                + ")";
    }
}
