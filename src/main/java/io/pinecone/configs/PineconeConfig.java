package io.pinecone.configs;

import io.grpc.ManagedChannel;
import io.pinecone.exceptions.PineconeConfigurationException;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.pinecone.commons.Constants.pineconeClientVersion;

/**
 * Settings the client uses to reach Pinecone: API key, source tag, proxy, custom HTTP client, TLS, index host and
 * response metadata listener.
 * <p>
 * Most callers never construct one: {@link io.pinecone.clients.Pinecone.Builder} builds it from the {@code with...}
 * methods. Construct one yourself when you already know an index's host and want to open a data-plane connection
 * without the describe-index call that {@link io.pinecone.clients.Pinecone#getIndexConnection(String)} makes, for
 * example against a local emulator. In that case pass it to both {@link PineconeConnection} and the
 * {@link io.pinecone.clients.Index} constructor. A config built this way applies its proxy to the gRPC connection,
 * and its proxy or custom {@link OkHttpClient} to the index's HTTP calls; index connections opened through
 * {@link io.pinecone.clients.Pinecone} use neither.
 * <pre>{@code
 * PineconeConfig config = new PineconeConfig(System.getenv("PINECONE_API_KEY"));
 * config.setHost("localhost:5081");
 * config.setTLSEnabled(false);
 *
 * PineconeConnection connection = new PineconeConnection(config, "product-catalog");
 * Index index = new Index(config, connection, "product-catalog");
 * index.upsert("item-1", Arrays.asList(0.12f, 0.45f, 0.33f));
 * }</pre>
 */
public class PineconeConfig {

    private static final Logger logger = LoggerFactory.getLogger(PineconeConfig.class);

    // Required field
    private String apiKey;

    // Optional fields
    private String host;
    private String sourceTag;
    private ProxyConfig proxyConfig;
    private OkHttpClient customOkHttpClient;
    private ManagedChannel customManagedChannel;
    private boolean enableTLS = true;
    private ResponseMetadataListener responseMetadataListener;

    /**
     * Creates a config with only an API key; TLS is on and every other setting is unset.
     *
     * @param apiKey the Pinecone API key sent with every request; {@link #validate()} checks and trims it
     */
    public PineconeConfig(String apiKey) {
        this(apiKey, null);
    }

    /**
     * Creates a config with an API key and a source tag. The tag is stored as given; use
     * {@link #setSourceTag(String)} if you want it normalized.
     *
     * @param apiKey    the Pinecone API key sent with every request; {@link #validate()} checks and trims it
     * @param sourceTag a tag added to the User-Agent header that identifies the application or integration making
     *                  the calls, such as {@code "support_bot"}; null leaves it out
     */
    public PineconeConfig(String apiKey, String sourceTag) {
        this(apiKey, sourceTag, null, null);
    }

    /**
     * Creates a config with an API key, source tag, proxy and custom HTTP client. The source tag is stored as given.
     *
     * @param apiKey             the Pinecone API key sent with every request; {@link #validate()} checks and
     *                           trims it
     * @param sourceTag          a tag added to the User-Agent header, such as {@code "support_bot"}; null leaves it out
     * @param proxyConfig        the HTTP proxy to send requests through; null connects directly
     * @param customOkHttpClient the {@link OkHttpClient} to use for HTTP requests, for example one with longer
     *                           timeouts; null lets the client build its own
     */
    public PineconeConfig(String apiKey, String sourceTag, ProxyConfig proxyConfig, OkHttpClient customOkHttpClient) {
        this.apiKey = apiKey;
        this.sourceTag = sourceTag;
        this.proxyConfig = proxyConfig;
        this.customOkHttpClient = customOkHttpClient;
    }

    /**
     * The Pinecone API key sent with every request.
     *
     * @return the API key, or null if none was set
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Replaces the API key. Connections already built from this config keep the key they were built with.
     *
     * @param apiKey the Pinecone API key
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * The index host a {@link PineconeConnection} built from this config connects to.
     *
     * @return the host, or null if none was set
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the index host that {@link PineconeConnection} and {@link io.pinecone.clients.Index} connect to. Pass the
     * host without a scheme, as {@code describeIndex(...).getHost()} returns it; {@link #setTLSEnabled(boolean)}
     * decides between HTTPS and plain HTTP.
     *
     * @param host the index host, such as {@code "product-catalog-abc1234.svc.aped-4627-b74a.pinecone.io"}, or
     *             {@code "localhost:5081"} for a local emulator
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * The tag added to the User-Agent header to identify the application or integration making the calls.
     *
     * @return the source tag, or null if none was set
     */
    public String getSourceTag() {
        return sourceTag;
    }

    /**
     * Sets the source tag after normalizing it: lowercased, characters other than letters, digits, underscores,
     * colons and spaces removed, surrounding spaces trimmed, and each run of spaces replaced with one underscore. So
     * {@code "Support Bot!"} is stored as {@code "support_bot"}.
     *
     * @param sourceTag the tag identifying your application or integration; null clears it
     */
    public void setSourceTag(String sourceTag) {
        this.sourceTag = normalizeSourceTag(sourceTag);
    }

    /**
     * The HTTP proxy requests are sent through.
     *
     * @return the proxy, or null to connect directly
     */
    public ProxyConfig getProxyConfig() {
        return proxyConfig;
    }

    /**
     * Sets the HTTP proxy to send requests through. A {@link PineconeConnection} built from this config sends its gRPC
     * traffic through the proxy, and an {@link io.pinecone.clients.Index} built from it uses the proxy for its HTTP
     * calls unless a custom {@link OkHttpClient} is also set. Index connections opened with
     * {@link io.pinecone.clients.Pinecone#getIndexConnection(String)} do not use the proxy set on the
     * {@link io.pinecone.clients.Pinecone} client.
     *
     * @param proxyConfig the proxy host and port, or null to connect directly
     */
    public void setProxyConfig(ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

    /**
     * The gRPC channel a {@link PineconeConnection} uses instead of building its own.
     *
     * @return the channel, or null to let the connection build one from the host
     */
    public ManagedChannel getCustomManagedChannel() {
        return this.customManagedChannel;
    }

    /**
     * The {@link OkHttpClient} used for HTTP requests instead of one the client builds.
     *
     * @return the custom HTTP client, or null if none was set
     */
    public OkHttpClient getCustomOkHttpClient() {
        return customOkHttpClient;
    }

    /**
     * Sets the {@link OkHttpClient} for HTTP requests, for example one with longer timeouts. An
     * {@link io.pinecone.clients.Index} or {@link io.pinecone.clients.Inference} client built from this config uses it
     * for its HTTP calls, in place of the proxy setting. It does not affect gRPC calls, which go through
     * {@link PineconeConnection}.
     *
     * @param customOkHttpClient the HTTP client to use, or null to let the client build its own
     */
    public void setCustomOkHttpClient(OkHttpClient customOkHttpClient) {
        this.customOkHttpClient = customOkHttpClient;
    }

    /**
     * Sets a gRPC channel for {@link PineconeConnection} to use instead of building its own, when you need channel
     * options the client does not expose, such as keepalive settings. The connection uses the channel as given, so
     * configure TLS, the target address and any proxy on it yourself; {@link PineconeConnection#close()} shuts it down.
     * Set the host as well if you call the record methods on {@link io.pinecone.clients.Index}, which send HTTP
     * requests to the host rather than using the channel.
     * <pre>{@code
     * ManagedChannel channel = NettyChannelBuilder
     *         .forTarget("product-catalog-abc1234.svc.aped-4627-b74a.pinecone.io")
     *         .keepAliveTime(30, TimeUnit.SECONDS)
     *         .build();
     * config.setCustomManagedChannel(channel);
     * }</pre>
     *
     * @param customManagedChannel the channel to use, or null to let the connection build one from the host
     */
    public void setCustomManagedChannel(ManagedChannel customManagedChannel) {
        this.customManagedChannel = customManagedChannel;
    }

    /**
     * A factory for a gRPC channel. The client never calls it; to supply your own channel, pass it to
     * {@link #setCustomManagedChannel(ManagedChannel)}.
     */
    public interface CustomChannelBuilder {
        /**
         * Builds the channel.
         *
         * @return a new gRPC channel
         */
        ManagedChannel buildChannel();
    }

    /**
     * Checks that the API key is set and can be sent in a header and, if a proxy is set, that it has a host and a
     * positive port. Leading and trailing whitespace and control characters (everything up to and including the
     * space, 0x20) are removed from the key, as {@link String#trim()} does, and {@link #getApiKey()} returns the
     * result; a key read from a file with a trailing newline therefore works. When that removes anything, a warning is
     * logged. What remains may contain only printable ASCII characters (0x20-0x7E). Neither error messages nor the
     * warning include the key. {@link io.pinecone.clients.Pinecone.Builder#build()} calls this;
     * {@link PineconeConnection} does not, so call it yourself on a config you construct.
     *
     * @throws PineconeConfigurationException if the API key is null, empty, only whitespace or control characters,
     *                                        or contains a character other than printable ASCII after trimming, or
     *                                        the proxy host or port is invalid
     */
    public void validate() {
        if (apiKey == null || apiKey.isEmpty())
            throw new PineconeConfigurationException("The API key is required and must not be empty or null");
        String trimmed = apiKey.trim();
        if (trimmed.isEmpty())
            throw new PineconeConfigurationException(
                    "The API key must not be only whitespace or control characters");
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c < 0x20 || c > 0x7e) {
                throw new PineconeConfigurationException(String.format(
                        "The API key contains an invalid character (U+%04X) at index %d of the trimmed key. It may "
                                + "contain only printable ASCII characters.", (int) c, i));
            }
        }
        if (trimmed.length() != apiKey.length()) {
            logger.warn("Removed leading/trailing whitespace or control characters from the Pinecone API key.");
            apiKey = trimmed;
        }

        // proxyConfig is set to null by default indicating the user is not interested in configuring the proxy
        if(proxyConfig != null) {
            proxyConfig.validate();
        }
    }

    /**
     * The User-Agent value the client sends: the language, the client version and, if set, the source tag.
     *
     * @return a value such as {@code "lang=java; pineconeClientVersion=v6.3.0; source_tag=support_bot"}
     */
    public String getUserAgent() {
        return buildUserAgent();
    }

    /**
     * Whether data-plane connections use TLS. On by default.
     *
     * @return true for TLS (HTTPS), false for plain-text connections
     */
    public boolean isTLSEnabled() {
        return enableTLS;
    }

    /**
     * Turns TLS on or off for data-plane connections. Turn it off only to reach a local emulator that serves plain
     * text, never for a Pinecone index.
     *
     * @param enableTLS true for TLS (the default), false for plain-text gRPC and HTTP
     */
    public void setTLSEnabled(boolean enableTLS) {
        this.enableTLS = enableTLS;
    }

    /**
     * The listener that receives timing and status details for data-plane calls.
     *
     * @return the listener, or null if none was set
     */
    public ResponseMetadataListener getResponseMetadataListener() {
        return responseMetadataListener;
    }

    /**
     * Sets the listener that receives a {@link ResponseMetadata} after each upsert, query, fetch, fetch-by-metadata,
     * update, delete or list call. Set it before constructing the {@link PineconeConnection}; a connection built
     * earlier does not report to it.
     *
     * @param responseMetadataListener the listener, or null for none
     */
    public void setResponseMetadataListener(ResponseMetadataListener responseMetadataListener) {
        this.responseMetadataListener = responseMetadataListener;
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