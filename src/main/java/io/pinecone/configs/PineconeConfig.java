package io.pinecone.configs;

import io.grpc.ManagedChannel;
import io.pinecone.exceptions.PineconeConfigurationException;
import io.pinecone.exceptions.PineconeValidationException;

public class PineconeConfig {

    private String apiKey;
    private String host;
    private String sourceTag;
    private ManagedChannel customManagedChannel;

    public PineconeConfig(String apiKey) {
        this(apiKey, null);
    }

    public PineconeConfig(String apiKey, String sourceTag) {
        this.apiKey = apiKey;
        this.sourceTag = sourceTag;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSourceTag() {
        return sourceTag;
    }

    public void setSourceTag(String sourceTag) {
        this.sourceTag = normalizeSourceTag(sourceTag);
    }

    public ManagedChannel getCustomManagedChannel() {
        return this.customManagedChannel;
    }

    public void setCustomManagedChannel(ManagedChannel customManagedChannel) {
        this.customManagedChannel = customManagedChannel;
    }

    public interface CustomChannelBuilder {
        ManagedChannel buildChannel();
    }

    public void validate() {
        if (apiKey == null || apiKey.isEmpty())
            throw new PineconeConfigurationException("The API key is required and must not be empty or null");
    }

    public String getUserAgent() {
        return buildUserAgent("pineconeClientVersion");
    }

    public String getUserAgentGrpc() {
        return buildUserAgent("pineconeClientVersion[grpc]");
    }

    private String buildUserAgent(String clientId) {
        String userAgent = String.format("lang=java; %s=%s", clientId, "v1.0.0");
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