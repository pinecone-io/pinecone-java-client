package io.pinecone;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class contains the user-level configuration options for the Pinecone client.
 * <p>
 * Currently, these values must be explicitly set; ~/.pinecone is not consulted for values.
 */
public class PineconeClientConfig {

    /**
     * Required API Key used to access Pinecone.
     */
    private String apiKey;

    /**
     * Required project name
     */
    private String projectName;

    private String environment;

    /**
     * Optional server-side timeout in seconds for all operations. Default: 20 seconds.
     */
    private int serverSideTimeoutSec = 20;

    /**
     * Creates a new default config.
     */
    public PineconeClientConfig() {
    }

    protected PineconeClientConfig(PineconeClientConfig other) {
        apiKey = other.apiKey;
        projectName = other.projectName;
        environment = other.environment;
        serverSideTimeoutSec = other.serverSideTimeoutSec;
    }

    /**
     * @return See {@link PineconeClientConfig#apiKey}.
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * @return A copy of this object with a new value for {@link PineconeClientConfig#apiKey}.
     */
    public PineconeClientConfig withApiKey(String apiKey) {
        PineconeClientConfig config = new PineconeClientConfig(this);
        config.apiKey = apiKey;
        return config;
    }

    /**
     * @return See {@link PineconeClientConfig#projectName}.
     */
    public String getProjectName() {
        return projectName;
    }

    public PineconeClientConfig withProjectName(String projectName) {
        PineconeClientConfig config = new PineconeClientConfig(this);
        config.projectName = projectName;
        return config;
    }

    public String getEnvironment() {
        return environment;
    }

    public PineconeClientConfig withEnvironment(String environment) {
        PineconeClientConfig config = new PineconeClientConfig(this);
        config.environment = environment;
        return config;
    }

    /**
     * @return See {@link PineconeClientConfig#serverSideTimeoutSec}.
     */
    public int getServerSideTimeoutSec() {
        return serverSideTimeoutSec;
    }

    /**
     * @return A copy of this object with a new value for {@link PineconeClientConfig#serverSideTimeoutSec}.
     */
    public PineconeClientConfig withServerSideTimeoutSec(int serverSideTimeout) {
        PineconeClientConfig config = new PineconeClientConfig(this);
        config.serverSideTimeoutSec = serverSideTimeout;
        return config;
    }

    void validate() {
        if (apiKey == null)
            throw new PineconeValidationException("Invalid Pinecone config: missing apiKey");
    }

    @Override
    public String toString() {
        return "PineconeConnectionConfig("
                + "apiKey=" + maskedApiKey()
                + ", projectName=" + projectName
                + ", environment=" + environment
                + ", serverSideTimeoutSec=" + getServerSideTimeoutSec()
                + ")";
    }

    private String maskedApiKey() {
        if (apiKey == null) {
            return "NULL";
        } else {
            List<String> splits = Arrays.stream(apiKey.split("-"))
                    .map(split -> String.join("", Collections.nCopies(split.length(), "*")))
                    .collect(Collectors.toList());
            return String.join("-", splits);
        }
    }
}