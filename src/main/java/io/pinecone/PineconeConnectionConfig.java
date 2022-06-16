
//Copyright (c) 2020-2021 Pinecone Systems Inc. All right reserved.


package io.pinecone;

import io.grpc.ManagedChannel;

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

    /**
     * Creates a new default config.
     */
    public PineconeConnectionConfig() {}

    protected PineconeConnectionConfig(PineconeConnectionConfig other) {
        indexName = other.indexName;
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

    public BiFunction<PineconeClientConfig, PineconeConnectionConfig, ManagedChannel> getCustomChannelBuilder() {
        return customChannelBuilder;
    }

    public PineconeConnectionConfig withCustomChannelBuilder(BiFunction<PineconeClientConfig, PineconeConnectionConfig, ManagedChannel> customChannelBuilder) {
        PineconeConnectionConfig config = new PineconeConnectionConfig(this);
        config.customChannelBuilder = customChannelBuilder;
        return config;
    }

    void validate() {
        String messagePrefix = "Invalid Pinecone config: ";
        if (indexName == null)
            throw new PineconeValidationException(messagePrefix + "indexName must be specified");
    }

    @Override
    public String toString() {
        return "PineconeConnectionConfig("
                + "customChannelBuilder=" + getCustomChannelBuilder()
                + ", indexName=" + getIndexName()
                + ")";
    }
}
