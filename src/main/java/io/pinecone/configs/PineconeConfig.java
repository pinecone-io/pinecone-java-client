package io.pinecone.configs;

import io.grpc.ManagedChannel;
import io.pinecone.exceptions.PineconeValidationException;

public class PineconeConfig {

    private String apiKey;
    private String host;
    private String usageContext;
    private ManagedChannel customChannelBuilder;

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

    public String getUsageContext() {
        return usageContext;
    }

    public void setUsageContext(String usageContext) {
        this.usageContext = usageContext;
    }

    public ManagedChannel getCustomChannelBuilder() {
        return this.customChannelBuilder;
    }

    public void setCustomChannelBuilder(ManagedChannel customChannelBuilder) {
        this.customChannelBuilder = customChannelBuilder;
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
        return (this.getUsageContext() != null) ?
                userAgentLanguage + "; usageContext=" + this.getUsageContext() : userAgentLanguage;
    }
}