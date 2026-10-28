package io.pinecone.configs;

import io.grpc.HttpConnectProxiedSocketAddress;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.ProxyDetector;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.pinecone.exceptions.PineconeException;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.proto.VectorServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * The gRPC channel and stubs that an {@link io.pinecone.clients.Index} or {@link io.pinecone.clients.AsyncIndex} uses
 * to reach one index. Safe to share across threads.
 * <p>
 * {@link io.pinecone.clients.Pinecone#getIndexConnection(String)} creates and caches one per index name, so most
 * callers never construct one. Construct it yourself, from a {@link PineconeConfig} that has the index host, when you
 * want to skip the describe-index call, connect to a local emulator, or supply your own gRPC channel. Close it when
 * you are done: it holds threads and network connections.
 * <pre>{@code
 * PineconeConfig config = new PineconeConfig(System.getenv("PINECONE_API_KEY"));
 * config.setHost("product-catalog-abc1234.svc.aped-4627-b74a.pinecone.io");
 *
 * try (PineconeConnection connection = new PineconeConnection(config, "product-catalog")) {
 *     Index index = new Index(config, connection, "product-catalog");
 *     DescribeIndexStatsResponse stats = index.describeIndexStats();
 *     System.out.println(stats.getTotalVectorCount());
 * }
 * }</pre>
 */
public class PineconeConnection implements AutoCloseable {

    private static final int DEFAULT_MAX_MESSAGE_SIZE = 64 * 1000 * 1000;

    private static final Logger logger = LoggerFactory.getLogger(PineconeConnection.class);
    private final PineconeConfig config;
    private final String indexName;
    final ManagedChannel channel;

    /**
     * The gRPC stub used for sending requests to Pinecone. Blocks until response.
     */
    private VectorServiceGrpc.VectorServiceBlockingStub blockingStub;

    /**
     * The gRPC async stub to allow clients to do ListenableFuture-style rpc calls to Pinecone.
     */
    private VectorServiceGrpc.VectorServiceFutureStub asyncStub;

    /**
     * Opens a connection without an index name. Same as {@link #PineconeConnection(PineconeConfig, String)}, except
     * that {@link ResponseMetadata#getIndexName()} is empty for calls on this connection.
     *
     * @param config the API key, index host and connection settings
     * @throws PineconeValidationException if the config has no custom channel and no host
     */
    public PineconeConnection(PineconeConfig config) {
        this(config, null);
    }

    /**
     * Opens a connection to the index host in {@code config}, or uses the channel from
     * {@link PineconeConfig#setCustomManagedChannel(ManagedChannel)} if one is set. The channel is built with the
     * config's TLS and proxy settings and connects lazily, so an unreachable host surfaces on the first call, not
     * here. The API key and response metadata listener are read now; later changes to the config do not affect this
     * connection.
     *
     * @param config    the API key, index host and connection settings
     * @param indexName the index name reported in {@link ResponseMetadata#getIndexName()}, such as
     *                  {@code "product-catalog"}; null reports an empty name
     * @throws PineconeValidationException if the config has no custom channel and no host
     */
    public PineconeConnection(PineconeConfig config, String indexName) {
        this.config = config;
        this.indexName = indexName;
        if (config.getCustomManagedChannel() != null) {
            channel = config.getCustomManagedChannel();
        } else {
            if (config.getHost() == null || config.getHost().isEmpty()) {
                throw new PineconeValidationException("Index-name or host is required for data plane operations");
            }
            channel = buildChannel();
        }
        initialize();
    }

    private void initialize() {
        channel.notifyWhenStateChanged(channel.getState(false), this::onConnectivityStateChanged);
        Metadata metadata = assembleMetadata(config);
        blockingStub = generateBlockingStub(metadata);
        asyncStub = generateAsyncStub(metadata);
        logger.debug("created new PineconeConnection for channel: {}", channel);
    }

    private VectorServiceGrpc.VectorServiceBlockingStub generateBlockingStub(Metadata metadata) {
        VectorServiceGrpc.VectorServiceBlockingStub stub = VectorServiceGrpc
                .newBlockingStub(channel)
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withMaxInboundMessageSize(DEFAULT_MAX_MESSAGE_SIZE)
                .withMaxOutboundMessageSize(DEFAULT_MAX_MESSAGE_SIZE);

        // Add response metadata interceptor if listener is configured
        if (config.getResponseMetadataListener() != null) {
            stub = stub.withInterceptors(
                    new ResponseMetadataInterceptor(
                            config.getResponseMetadataListener(),
                            indexName != null ? indexName : "",
                            config.getHost() != null ? config.getHost() : ""));
        }

        return stub;
    }

    private VectorServiceGrpc.VectorServiceFutureStub generateAsyncStub(Metadata metadata) {
        VectorServiceGrpc.VectorServiceFutureStub stub = VectorServiceGrpc
                .newFutureStub(channel)
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withMaxInboundMessageSize(DEFAULT_MAX_MESSAGE_SIZE)
                .withMaxOutboundMessageSize(DEFAULT_MAX_MESSAGE_SIZE);

        // Add response metadata interceptor if listener is configured
        if (config.getResponseMetadataListener() != null) {
            stub = stub.withInterceptors(
                    new ResponseMetadataInterceptor(
                            config.getResponseMetadataListener(),
                            indexName != null ? indexName : "",
                            config.getHost() != null ? config.getHost() : ""));
        }

        return stub;
    }

    /**
     * The gRPC channel this connection sends calls on, either built from the config or the custom one supplied.
     *
     * @return the channel
     */
    public ManagedChannel getChannel() {
        return channel;
    }

    /**
     * The blocking stub {@link io.pinecone.clients.Index} uses. It already sends the API key and, if a listener is set,
     * reports response metadata, so use it for a call the {@link io.pinecone.clients.Index} methods don't cover.
     *
     * @return the blocking stub
     */
    public VectorServiceGrpc.VectorServiceBlockingStub getBlockingStub() {
        return blockingStub;
    }

    /**
     * The future-returning stub {@link io.pinecone.clients.AsyncIndex} uses. It already sends the API key and, if a
     * listener is set, reports response metadata.
     *
     * @return the async stub
     */
    public VectorServiceGrpc.VectorServiceFutureStub getAsyncStub() {
        return asyncStub;
    }

    private void onConnectivityStateChanged() {
        logger.debug("channel connectivity state changed to {} for {}",
                channel.getState(false), channel);
    }

    private ManagedChannel buildChannel() {
        String endpoint = formatEndpoint(config.getHost());
        NettyChannelBuilder builder = NettyChannelBuilder
                .forTarget(endpoint)
                .userAgent(config.getUserAgent());

        try {
            if(config.isTLSEnabled()) {
                builder = builder
                        .overrideAuthority(endpoint)
                        .negotiationType(NegotiationType.TLS)
                        .sslContext(GrpcSslContexts.forClient().build());
            }
            else {
                builder = builder
                        .negotiationType(NegotiationType.PLAINTEXT);
            }

            if(config.getProxyConfig() != null) {
                ProxyDetector proxyDetector = getProxyDetector();
                builder.proxyDetector(proxyDetector);
            }
        } catch (SSLException e) {
            throw new PineconeException("SSL error opening gRPC channel", e);
        }

        return builder.build();
    }

    private ProxyDetector getProxyDetector() {
        ProxyConfig proxyConfig = config.getProxyConfig();
        return (targetServerAddress) -> {
            SocketAddress proxyAddress = new InetSocketAddress(proxyConfig.getHost(), proxyConfig.getPort());

            return HttpConnectProxiedSocketAddress.newBuilder()
                    .setTargetAddress((InetSocketAddress) targetServerAddress)
                    .setProxyAddress(proxyAddress)
                    .build();
        };
    }

    private static Metadata assembleMetadata(PineconeConfig config) {
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("api-key", Metadata.ASCII_STRING_MARSHALLER), config.getApiKey());
        metadata.put(Metadata.Key.of("X-Pinecone-api-version", Metadata.ASCII_STRING_MARSHALLER), "2025-10");
        return metadata;
    }

    /**
     * Removes a leading {@code http://} or {@code https://} from a host so it can be used as a gRPC target.
     *
     * @param host a host with or without a scheme, such as
     *             {@code "https://product-catalog-abc1234.svc.aped-4627-b74a.pinecone.io"}
     * @return the host without the scheme
     * @throws PineconeValidationException if {@code host} is null or empty
     */
    public static String formatEndpoint(String host) {
        if (host != null && !host.isEmpty()) {
            return host.replaceFirst("https?://", "");
        } else {
            throw new PineconeValidationException("Index host cannot be null or empty");
        }
    }

    /**
     * Shuts down the channel, including a custom one, cancels calls still in flight, and waits up to 5 seconds for it
     * to terminate. Close a connection once nothing will use it again; any {@link io.pinecone.clients.Index} or
     * {@link io.pinecone.clients.AsyncIndex} built on it stops working.
     */
    @Override
    public void close() {
        try {
            logger.debug("closing channel");
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn("Channel shutdown interrupted before termination confirmed");
        }
    }

    @Override
    public String toString() {
        return config.getHost();
    }
}
