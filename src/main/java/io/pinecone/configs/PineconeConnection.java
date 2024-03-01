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
    private final PineconeConnectionConfig connectionConfig;
    private final PineconeClientConfig clientConfig;
    final ManagedChannel channel;

    /**
     * The gRPC stub used for sending requests to Pinecone. Blocks until response. Access to this
     * field is not thread-safe; modifications should be synchronized by callers.
     */
    private VectorServiceGrpc.VectorServiceBlockingStub blockingStub;

    private VectorServiceGrpc.VectorServiceFutureStub futureStub;

    public PineconeConnection(String apiKey, String indexName) {
        this.clientConfig = new PineconeClientConfig().withApiKey(apiKey);
        String host = getHost(apiKey, indexName);
        this.connectionConfig = new PineconeConnectionConfig().withConnectionUrl(host);
        channel = buildChannel(host);
        initialize();
    }

    public PineconeConnection(PineconeClientConfig clientConfig, PineconeConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
        this.clientConfig = clientConfig;
        validateConfigs();

        channel = connectionConfig.getCustomChannelBuilder() != null
                ? connectionConfig.getCustomChannelBuilder().apply(clientConfig, connectionConfig)
                : buildChannel(connectionConfig.getConnectionUrl());
        initialize();
    }

    private void initialize() {
        channel.notifyWhenStateChanged(channel.getState(false), this::onConnectivityStateChanged);
        Metadata metadata = assembleMetadata(clientConfig);
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
        String endpoint = getEndpoint(host);
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

    private static Metadata assembleMetadata(PineconeClientConfig clientConfig) {
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("api-key",
                Metadata.ASCII_STRING_MARSHALLER), clientConfig.getApiKey());
        metadata.put(Metadata.Key.of("User-Agent", Metadata.ASCII_STRING_MARSHALLER), clientConfig.getUserAgent());
        return metadata;
    }

    public static String getEndpoint(String host) {
        if(host != null && !host.isEmpty()) {
            return host.replaceFirst("https?://", "");
        }
        else {
            throw new PineconeValidationException("Index host cannot be null or empty");
        }
    }

    private static String getHost(String apiKey, String indexName) {
        PineconeControlPlaneClient controlPlaneClient = new PineconeControlPlaneClient(apiKey);
        return controlPlaneClient.describeIndex(indexName).getHost();
    }

    void validateConfigs() throws PineconeValidationException {
        if (this.clientConfig == null) {
            throw new PineconeValidationException("PineconeClientConfiguration may not be null");
        }

        if (this.connectionConfig == null) {
            throw new PineconeValidationException("PineconeConnectionConfig may not be null");
        }

        this.clientConfig.validate();
        this.connectionConfig.validate();

        if(connectionConfig.getIndexName() != null && !connectionConfig.getIndexName().isEmpty()
                && (connectionConfig.getConnectionUrl() == null || connectionConfig.getConnectionUrl().isEmpty())) {
            String host = getHost(clientConfig.getApiKey(), connectionConfig.getIndexName());
            connectionConfig.withConnectionUrl(host);
        }
    }
}
