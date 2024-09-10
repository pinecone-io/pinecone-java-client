package io.pinecone.configs;

import io.grpc.ManagedChannel;
import io.pinecone.exceptions.PineconeConfigurationException;

import static io.pinecone.commons.Constants.pineconeClientVersion;

/**
 * The {@link PineconeConfig} class is responsible for managing the configuration settings
 * required to interact with the Pinecone API. It provides methods to set and retrieve
 * the necessary API key, host, source tag, proxyConfig, and custom managed channel.
 * <pre>{@code
 *
 *     import io.grpc.ManagedChannel;
 *     import io.grpc.netty.GrpcSslContexts;
 *     import io.grpc.netty.NegotiationType;
 *     import io.grpc.netty.NettyChannelBuilder;
 *     import io.pinecone.configs.PineconeConfig;
 *     import io.pinecone.exceptions.PineconeException;
 *
 *     import javax.net.ssl.SSLException;
 *     import java.util.concurrent.TimeUnit;
 * ...
 *
 *
 *     PineconeConfig config = new PineconeConfig("apikey");
 *     String endpoint = "some-endpoint";
 *     NettyChannelBuilder builder = NettyChannelBuilder.forTarget(endpoint);
 *
 *     // Custom channel with timeouts
 *     try {
 *         builder = builder.overrideAuthority(endpoint)
 *             .negotiationType(NegotiationType.TLS)
 *             .keepAliveTimeout(5, TimeUnit.SECONDS)
 *             .sslContext(GrpcSslContexts.forClient().build());
 *     } catch (SSLException e) {
 *         throw new PineconeException("SSL error opening gRPC channel", e);
 *     }
 *
 *     // Build the managed channel with the configured options
 *     ManagedChannel channel = builder.build();
 *     config.setCustomManagedChannel(channel);
 * }</pre>
 */
public class PineconeConfig {

    // Required field
    private String apiKey;

    // Optional fields
    private String host;
    private String sourceTag;
    private ProxyConfig proxyConfig;
    private ManagedChannel customManagedChannel;
    private boolean enableTLS = true;

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
     * @param apiKey    The API key required to authenticate with the Pinecone API.
     * @param sourceTag An optional source tag to be included in the user agent.
     */
    public PineconeConfig(String apiKey, String sourceTag) {
        this(apiKey, sourceTag, null);
    }

    /**
     * Constructs a {@link PineconeConfig} instance with the specified API key, source tag, control plane proxy
     * configuration, and data plane proxy configuration.
     *
     * @param apiKey The API key required to authenticate with the Pinecone API.
     * @param sourceTag An optional source tag to be included in the user agent.
     * @param proxyConfig The proxy configuration for control and data plane requests. Can be null if not set.
     */
    public PineconeConfig(String apiKey, String sourceTag, ProxyConfig proxyConfig) {
        this.apiKey = apiKey;
        this.sourceTag = sourceTag;
        this.proxyConfig = proxyConfig;
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
     * Returns the proxy configuration for control and data plane requests.
     *
     * @return The proxy configuration for control and data plane requests, or null if not set.
     */
    public ProxyConfig getProxyConfig() {
        return proxyConfig;
    }

    /**
     * Sets the proxy configuration for control and data plane requests.
     *
     * @param proxyConfig The new proxy configuration for control and data plane requests.
     */
    public void setProxyConfig(ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
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

    public interface CustomChannelBuilder {
        ManagedChannel buildChannel();
    }

    /**
     * Validates the configuration settings of the Pinecone client.
     * This method ensures that the API key is not null or empty, and validates the proxy configurations if set.
     * Throws a PineconeConfigurationException if the API key is null or empty, or if any of the proxy configurations are invalid.
     *
     * @throws PineconeConfigurationException If the API key is null or empty, or if any of the proxy configurations are invalid.
     */
    public void validate() {
        if (apiKey == null || apiKey.isEmpty())
            throw new PineconeConfigurationException("The API key is required and must not be empty or null");

        // proxyConfig is set to null by default indicating the user is not interested in configuring the proxy
        if(proxyConfig != null) {
            proxyConfig.validate();
        }
    }

    /**
     * Builds the user agent string for the Pinecone client.
     *
     * @return The user agent string.
     */
    public String getUserAgent() {
        return buildUserAgent();
    }

    /**
     * Returns true if TLS is enabled or false otherwise.
     *
     * @return enableTls
     */
    public boolean isTLSEnabled() {
        return enableTLS;
    }

    /**
     * Sets whether TLS is enabled.
     *
     * @param enableTLS true to enable TLS, false to disable it.
     */
    public void setEnableTLS(boolean enableTLS) {
        this.enableTLS = enableTLS;
    }

    private String buildUserAgent() {
        String userAgent = String.format("lang=java; %s=%s", "pineconeClientVersion", pineconeClientVersion);
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
         * 2. Limit charset to [a-z0-9_: ]
         * 3. Trim left/right empty space
         * 4. Condense multiple spaces to one, and replace with underscore
         */
        return input.toLowerCase()
                .replaceAll("[^a-z0-9_: ]", "")
                .trim()
                .replaceAll("\\s+", "_");
    }
}