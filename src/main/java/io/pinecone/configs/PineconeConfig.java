package io.pinecone.configs;

import io.grpc.ManagedChannel;
import io.pinecone.exceptions.PineconeConfigurationException;
/**
 * The `PineconeConfig` class is responsible for managing the configuration settings
 * required to interact with the Pinecone API. It provides methods to set and retrieve
 * the necessary API key, host, source tag, and custom managed channel.
 */
public class PineconeConfig {

    // Required field
    private String apiKey;

    // Optional fields
    private String host;
    private String sourceTag;
    private ManagedChannel customManagedChannel;

    /**
     * Constructs a {@link PineconeConfig} instance with the specified API key.
     *
     * @param apiKey The API key required to authenticate with the Pinecone API.
     */
    public PineconeConfig(String apiKey) {
        this(apiKey, null);
    }

    /**
     * Constructs a {@link PineconeConfig} instance with the specified API key and source tag.
     *
     * @param apiKey      The API key required to authenticate with the Pinecone API.
     * @param sourceTag   An optional source tag to be included in the user agent.
     */
    public PineconeConfig(String apiKey, String sourceTag) {
        this.apiKey = apiKey;
        this.sourceTag = sourceTag;
    }

    /**
     * Returns the API key.
     *
     * @return The API key.
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Sets the API key.
     *
     * @param apiKey The new API key.
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Returns the host.
     *
     * @return The host.
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the host.
     *
     * @param host The new host.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Returns the source tag.
     *
     * @return The source tag.
     */
    public String getSourceTag() {
        return sourceTag;
    }

    /**
     * Sets the source tag. The source tag is normalized before being stored.
     *
     * @param sourceTag The new source tag.
     */
    public void setSourceTag(String sourceTag) {
        this.sourceTag = normalizeSourceTag(sourceTag);
    }

    /**
     * Returns the custom gRPC managed channel.
     *
     * @return The custom gRPC managed channel.
     */
    public ManagedChannel getCustomManagedChannel() {
        return this.customManagedChannel;
    }

    /**
     * Sets the custom gRPC managed channel if the user is not interested in using default gRPC channel initialized
     * and set in the Pinecone Builder class.
     *
     * @param customManagedChannel The new custom gRPC managed channel.
     */
    public void setCustomManagedChannel(ManagedChannel customManagedChannel) {
        this.customManagedChannel = customManagedChannel;
    }

    /**
     * Validates the configuration, ensuring that the API key is not null or empty.
     *
     * @throws PineconeConfigurationException if the API key is null or empty.
     */
    public void validate() {
        if (apiKey == null || apiKey.isEmpty())
            throw new PineconeConfigurationException("The API key is required and must not be empty or null");
    }

    /**
     * Builds the user agent string for the Pinecone client.
     *
     * @return The user agent string.
     */
    public String getUserAgent() {
        return buildUserAgent("pineconeClientVersion");
    }

    /**
     * Builds the user agent string for the Pinecone client's gRPC requests.
     *
     * @return The user agent string for gRPC requests.
     */
    public String getUserAgentGrpc() {
        return buildUserAgent("pineconeClientVersion[grpc]");
    }

    private String buildUserAgent(String clientId) {
        String userAgent = String.format("lang=java; %s=%s", clientId, "v0.8.0");
        if (this.getSourceTag() != null && !this.getSourceTag().isEmpty()) {
            userAgent += "; source_tag=" + this.getSourceTag();
        }
        return userAgent;
    }

    private String normalizeSourceTag(String input) {
        if (input == null) {
            return null;
        }

        /*
         * Normalize the source tag
         * 1. Lowercase
         * 2. Limit charset to [a-z0-9_ ]
         * 3. Trim left/right empty space
         * 4. Condense multiple spaces to one, and replace with underscore
         */
        return input.toLowerCase()
                .replaceAll("[^a-z0-9_ ]", "")
                .trim()
                .replaceAll("\\s+", "_");
    }
}