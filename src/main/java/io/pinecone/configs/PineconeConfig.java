package io.pinecone.configs;

import io.grpc.ManagedChannel;
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
            throw new PineconeValidationException("The API key is required and must not be empty or null");
    }

    public String getUserAgent() {
        String userAgentLanguage = "lang=java; pineconeClientVersion=v0.8.0";
        if (this.getSourceTag() == null) {
            return userAgentLanguage;
        } else {
            return userAgentLanguage + "; source_tag=" + this.getSourceTag();
        }
    }

    private String normalizeSourceTag(String input) {
        if (input == null) {
            return null;
        }

        String normalizedTag = input.toLowerCase();
        normalizedTag = normalizedTag.trim();
        normalizedTag = normalizedTag.replaceAll("\\s+", " ");
        normalizedTag = normalizedTag.replaceAll(" ", "_");
        normalizedTag = normalizedTag.replaceAll("[^a-z0-9_]", "");
        return normalizedTag;
    }
}