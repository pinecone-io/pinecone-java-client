package io.pinecone.configs;

import io.grpc.ManagedChannel;
import io.pinecone.exceptions.PineconeValidationException;

public class PineconeConfig {

    private String apiKey;
    private String host;
    private String integrationId;
    private ManagedChannel customManagedChannel;

    public PineconeConfig(String apiKey) {
        this.apiKey = apiKey;
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

    public String getIntegrationId() {
        return integrationId;
    }

    public void setIntegrationId(String integrationId) {
        this.integrationId = integrationId;
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

    void validate() {
        if (apiKey == null)
            throw new PineconeValidationException("Invalid PineconeConfig: missing apiKey");
    }

    public String getUserAgent() {
        String userAgentLanguage = "lang=java; pineconeClientVersion = v0.8.0";
        if (this.getIntegrationId() == null) {
            return userAgentLanguage;
        } else {
            return userAgentLanguage + "; usageContext=" + this.getIntegrationId();
        }
    }
}