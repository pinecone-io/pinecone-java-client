package io.pinecone.configs;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.pinecone.clients.PineconeControlPlaneClient;
import io.pinecone.exceptions.PineconeException;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.proto.VectorServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.util.concurrent.TimeUnit;

/**
 * Handles communication with a Pinecone service or router. One PineconeConnection can be shared
 * and used concurrently by multiple threads.
 */
public class PineconeConnection implements AutoCloseable {

    private static final int DEFAULT_MAX_MESSAGE_SIZE = 64 * 1000 * 1000;

    private static final Logger logger = LoggerFactory.getLogger(PineconeConnection.class);
    private final PineconeConfig config;
    final ManagedChannel channel;

    /**
     * The gRPC stub used for sending requests to Pinecone. Blocks until response. Access to this
     * field is not thread-safe; modifications should be synchronized by callers.
     */
    private VectorServiceGrpc.VectorServiceBlockingStub blockingStub;

    private VectorServiceGrpc.VectorServiceFutureStub futureStub;

    public PineconeConnection(String apiKey, String indexName) {
        this.config = new PineconeConfig(apiKey);
        String host = getHost(apiKey, indexName);
        channel = buildChannel(host);
        initialize();
    }

    public PineconeConnection(PineconeConfig config, String indexName) {
        this.config = config;
        if (config.getCustomChannelBuilder() != null) {
            channel = config.getCustomChannelBuilder();
        } else {
            if (config.getHost() == null || config.getHost().isEmpty()) {
                config.setHost(getHost(config.getApiKey(), indexName));
            }
            channel = buildChannel(config.getHost());
        }
        initialize();
    }

    private void initialize() {
        channel.notifyWhenStateChanged(channel.getState(false), this::onConnectivityStateChanged);
        Metadata metadata = assembleMetadata(config);
        blockingStub = generateBlockingStub(metadata);
        futureStub = generateFutureStub(metadata);
        logger.debug("created new PineconeConnection for channel: {}", channel);
    }

    private VectorServiceGrpc.VectorServiceBlockingStub generateBlockingStub(Metadata metadata) {
        return VectorServiceGrpc
                .newBlockingStub(channel)
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withMaxInboundMessageSize(DEFAULT_MAX_MESSAGE_SIZE)
                .withMaxOutboundMessageSize(DEFAULT_MAX_MESSAGE_SIZE);
    }

    private VectorServiceGrpc.VectorServiceFutureStub generateFutureStub(Metadata metadata) {
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

    public ManagedChannel getChannel() {
        return channel;
    }

    public VectorServiceGrpc.VectorServiceBlockingStub getBlockingStub() {
        return blockingStub;
    }

    public VectorServiceGrpc.VectorServiceFutureStub getFutureStub() {
        return futureStub;
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
        metadata.put(Metadata.Key.of("api-key",
                Metadata.ASCII_STRING_MARSHALLER), config.getApiKey());
        metadata.put(Metadata.Key.of("User-Agent", Metadata.ASCII_STRING_MARSHALLER), config.getUserAgent());
        return metadata;
    }

    public static String formatEndpoint(String host) {
        if (host != null && !host.isEmpty()) {
            return host.replaceFirst("https?://", "");
        } else {
            throw new PineconeValidationException("Index host cannot be null or empty");
        }
    }

    private static String getHost(String apiKey, String indexName) {
        PineconeControlPlaneClient controlPlaneClient = new PineconeControlPlaneClient(apiKey);
        return controlPlaneClient.describeIndex(indexName).getHost();
    }
}
