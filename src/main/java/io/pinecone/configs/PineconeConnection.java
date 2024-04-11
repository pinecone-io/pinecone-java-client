package io.pinecone.configs;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
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
import java.util.concurrent.TimeUnit;

/**
 * The {@link PineconeConnection} class handles communication with a Pinecone service or router. One PineconeConnection
 * can be shared and used concurrently by multiple threads.
 */
public class PineconeConnection implements AutoCloseable {

    private static final int DEFAULT_MAX_MESSAGE_SIZE = 64 * 1000 * 1000;

    private static final Logger logger = LoggerFactory.getLogger(PineconeConnection.class);
    private final PineconeConfig config;
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
     * Constructs a {@link PineconeConnection} instance with the specified {@link PineconeConfig}.
     * If a custom gRPC ManagedChannel is provided in the {@link PineconeConfig}, it will be used.
     * Otherwise, a new gRPC ManagedChannel will be built using the host specified in the {@link PineconeConfig}.
     * <p>
     *
     * @param config The {@link PineconeConfig} containing configuration settings for the PineconeConnection.
     * @throws PineconeValidationException If index name or host is not provided for data plane operations.
     */
    public PineconeConnection(PineconeConfig config) {
        this.config = config;
        if (config.getCustomManagedChannel() != null) {
            channel = config.getCustomManagedChannel();
        } else {
            if (config.getHost() == null || config.getHost().isEmpty()) {
                throw new PineconeValidationException("Index-name or host is required for data plane operations");
            }
            channel = buildChannel(config.getHost());
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
        return VectorServiceGrpc
                .newBlockingStub(channel)
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withMaxInboundMessageSize(DEFAULT_MAX_MESSAGE_SIZE)
                .withMaxOutboundMessageSize(DEFAULT_MAX_MESSAGE_SIZE);
    }

    private VectorServiceGrpc.VectorServiceFutureStub generateAsyncStub(Metadata metadata) {
        return VectorServiceGrpc
                .newFutureStub(channel)
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withMaxInboundMessageSize(DEFAULT_MAX_MESSAGE_SIZE)
                .withMaxOutboundMessageSize(DEFAULT_MAX_MESSAGE_SIZE);
    }

    /**
     * Close the connection and release all resources. A PineconeConnection's underlying gRPC components use resources
     * like threads and TCP connections. To prevent leaking these resources the connection should be closed when it
     * will no longer be used. If it may be used again leave it running.
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

    /**
     * Returns the gRPC channel.
     */
    public ManagedChannel getChannel() {
        return channel;
    }

    /**
     * Return the gRPC stub used for sending requests to Pinecone.
     */
    public VectorServiceGrpc.VectorServiceBlockingStub getBlockingStub() {
        return blockingStub;
    }

    /**
     * Return the gRPC async stub to allow clients to do ListenableFuture-style rpc calls to Pinecone.
     */
    public VectorServiceGrpc.VectorServiceFutureStub getAsyncStub() {
        return asyncStub;
    }

    private void onConnectivityStateChanged() {
        logger.debug("channel connectivity state changed to {} for {}",
                channel.getState(false), channel);
    }

    public static ManagedChannel buildChannel(String host) {
        String endpoint = formatEndpoint(host);
        NettyChannelBuilder builder = NettyChannelBuilder.forTarget(endpoint);

        try {
            builder = builder.overrideAuthority(endpoint)
                    .negotiationType(NegotiationType.TLS)
                    .sslContext(GrpcSslContexts.forClient().build());
        } catch (SSLException e) {
            throw new PineconeException("SSL error opening gRPC channel", e);
        }

        return builder.build();
    }

    private static Metadata assembleMetadata(PineconeConfig config) {
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("api-key", Metadata.ASCII_STRING_MARSHALLER), config.getApiKey());
        metadata.put(Metadata.Key.of("User-Agent", Metadata.ASCII_STRING_MARSHALLER), config.getUserAgentGrpc());
        return metadata;
    }

    public static String formatEndpoint(String host) {
        if (host != null && !host.isEmpty()) {
            return host.replaceFirst("https?://", "");
        } else {
            throw new PineconeValidationException("Index host cannot be null or empty");
        }
    }
}
