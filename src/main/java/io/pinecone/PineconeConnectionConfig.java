package io.pinecone;

import io.grpc.ManagedChannel;

import java.util.function.BiFunction;

/**
 * This class contains the connection-level configuration options for the Pinecone client.
 */
public class PineconeConnectionConfig {

    private BiFunction<PineconeClientConfig, PineconeConnectionConfig, ManagedChannel> customChannelBuilder;

    /**
     * Optional flag for whether to use SSL for communication with the service. Default: true.
     */
    private boolean secure = true;

    /**
     * <p>
     * Required URL authority of the Pinecone service or router to access
     * (e.g. "10.1.2.3:3456"). If not specified it will be constructed from other config values.
     * </p>
     * <p>
     * You can get the serviceAuthority from the host and port values returned by the python client's
     * {@code pinecone.service.describe(service_name)}. For example:
     * </p>
     * <pre>
     * pinecone.service.deploy(service_name='my-service', graph=pinecone.graph.IndexGraph())
     * pinecone.service.describe(service_name='my-service')
     * </pre>
     */
    private String serviceAuthority;

    /**
     * Required service or router name to connect to.
     */
    private String serviceName;

    /**
     * Creates a new default config.
     */
    public PineconeConnectionConfig() {}

    protected PineconeConnectionConfig(PineconeConnectionConfig other) {
        serviceAuthority = other.serviceAuthority;
        secure = other.secure;
        serviceName = other.serviceName;
        customChannelBuilder = other.customChannelBuilder;
    }

    /**
     * @return See {@link PineconeConnectionConfig#serviceAuthority}.
     */
    public String getServiceAuthority() {
        return serviceAuthority;
    }

    /**
     * @return A copy of this object with a new value for {@link PineconeConnectionConfig#serviceAuthority}.
     */
    public PineconeConnectionConfig withServiceAuthority(String serviceAuthority) {
        PineconeConnectionConfig config = new PineconeConnectionConfig(this);
        config.serviceAuthority = serviceAuthority;
        return config;
    }

    /**
     * @return See {@link PineconeConnectionConfig#secure}.
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * @return A copy of this object with a new value for {@link PineconeConnectionConfig#secure}.
     */
    public PineconeConnectionConfig withSecure(boolean secure) {
        PineconeConnectionConfig config = new PineconeConnectionConfig(this);
        config.secure = secure;
        return config;
    }

    /**
     * @return See {@link PineconeConnectionConfig#serviceName}.
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * @return A copy of this object with a new value for {@link PineconeConnectionConfig#serviceName}.
     */
    public PineconeConnectionConfig withServiceName(String serviceName) {
        PineconeConnectionConfig config = new PineconeConnectionConfig(this);
        config.serviceName = serviceName;
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
        if (serviceName == null)
            throw new PineconeValidationException(messagePrefix + "serviceName must be specified");
        if (serviceAuthority == null)
            throw new PineconeValidationException(messagePrefix + "serviceAuthority must be specified");
    }

    @Override
    public String toString() {
        return "PineconeConnectionConfig("
                + "customChannelBuilder=" + getCustomChannelBuilder()
                + ", secure=" + isSecure()
                + ", serviceAuthority=" + getServiceAuthority()
                + ", serviceName=" + getServiceName()
                + ")";
    }
}
