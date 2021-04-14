package io.pinecone;

/**
 * This class contains the user-level configuration options for the Pinecone client.
 *
 * Currently these values must be explicitly set; ~/.pinecone is not consulted for values.
 */
public class PineconeClientConfig {

    /**
     * Required API Key used to access Pinecone.
     */
    private String apiKey;

    /**
     * Optional server-side timeout in seconds for all operations. Default: 20 seconds.
     */
    private int serverSideTimeoutSec = 20;

    private PineconeTranslator translator = new PineconeTranslator();

    /**
     * Creates a new default config.
     */
    public PineconeClientConfig() {}

    protected PineconeClientConfig(PineconeClientConfig other) {
        apiKey = other.apiKey;
        serverSideTimeoutSec = other.serverSideTimeoutSec;
        translator = other.translator;
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

    public PineconeTranslator getTranslator() {
        return translator;
    }

    public PineconeClientConfig withTranslator(PineconeTranslator translator) {
        PineconeClientConfig config = new PineconeClientConfig(this);
        config.translator = translator;
        return config;
    }

    void validate() {
        if(apiKey == null)
            throw new PineconeValidationException("Invalid Pinecone config: missing apiKey");
    }

    @Override
    public String toString() {
        return "PineconeConnectionConfig("
                + "apiKey=" + getApiKey()
                + ", serverSideTimeoutSec=" + getServerSideTimeoutSec()
                + ", translator=" + getTranslator()
                + ")";
    }
}