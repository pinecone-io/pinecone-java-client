package io.pinecone;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PineconeConfigs {
    private PineconeClientConfig clientConfig;
    private PineconeConnectionConfig connectionConfig;

    public PineconeConfigs() {
        clientConfig = new PineconeClientConfig();
        connectionConfig = new PineconeConnectionConfig();
    }

    public PineconeConfigs(PineconeClientConfig clientConfig, PineconeConnectionConfig connectionConfig) {
        this.clientConfig = clientConfig;
        this.connectionConfig = connectionConfig;
    }

    public PineconeConfigs(String apiKey, String connectionURL) {
        this();
        String pattern = "https://([a-zA-Z0-9-]{1,45})-([a-fA-F0-9]+)\\.svc\\.([a-zA-Z0-9]+)\\.pinecone\\.io";
        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(connectionURL);

        if (matcher.matches()) {
            String indexName = matcher.group(1);
            String projectName = matcher.group(2);
            String environment = matcher.group(3);
            clientConfig = clientConfig.withApiKey(apiKey).withProjectName(projectName).withEnvironment(environment);
            connectionConfig = connectionConfig.withIndexName(indexName);
        }
        else {
            throw new PineconeValidationException("Unable to parse connection url");
        }
    }

    public PineconeClientConfig getClientConfig() {
        return clientConfig;
    }

    public PineconeConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    public PineconeConfigs withClientConfig(PineconeClientConfig clientConfig) {
        return new PineconeConfigs(clientConfig, new PineconeConnectionConfig());
    }

    public PineconeConfigs withConnectionConfig(PineconeConnectionConfig connectionConfig) {
        return new PineconeConfigs(new PineconeClientConfig(), connectionConfig);
    }
}