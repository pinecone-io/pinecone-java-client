package io.pinecone;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PineconeConfigs {
    private PineconeClientConfig clientConfig;
    private PineconeConnectionConfig connectionConfig;

    protected  PineconeConfigs(PineconeConfigs other) {
        clientConfig = other.clientConfig;
        connectionConfig = other.connectionConfig;
    }

    public PineconeConfigs(PineconeClientConfig clientConfig, PineconeConnectionConfig connectionConfig) {
        this.clientConfig = clientConfig;
        this.connectionConfig = connectionConfig;
    }

    public PineconeConfigs(String apiKey, String connectionUrl) {
        String pattern = "https://([a-zA-Z0-9-]+)-([a-fA-F0-9]+)\\.svc\\.([a-zA-Z0-9-]+)\\.pinecone\\.io";
        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(connectionUrl);

        if (matcher.matches()) {
            String indexName = matcher.group(1);
            String projectName = matcher.group(2);
            String environment = matcher.group(3);
            clientConfig = new PineconeClientConfig().withApiKey(apiKey).withProjectName(projectName).withEnvironment(environment);
            connectionConfig = new PineconeConnectionConfig().withIndexName(indexName).withConnectionUrl(connectionUrl);
        }
        else {
            throw new PineconeValidationException("Unable to parse connection url " + connectionUrl);
        }
    }

    public PineconeClientConfig getClientConfig() {
        return clientConfig;
    }

    public PineconeConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    public PineconeConfigs withClientConfig(PineconeClientConfig clientConfig) {
        PineconeConfigs configs = new PineconeConfigs(this);
        configs.clientConfig = clientConfig;
        return configs;
    }

    public PineconeConfigs withConnectionConfig(PineconeConnectionConfig connectionConfig) {
        PineconeConfigs configs = new PineconeConfigs(this);
        configs.connectionConfig = connectionConfig;
        return configs;
    }
}