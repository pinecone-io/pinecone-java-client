package io.pinecone;

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

    private VectorServiceGrpc.VectorServiceStub asyncStub;

    public PineconeConnection(PineconeClientConfig clientConfig, PineconeConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
        this.clientConfig = clientConfig;
        validateConfigs();

        channel = connectionConfig.getCustomChannelBuilder() != null
                ? connectionConfig.getCustomChannelBuilder().apply(clientConfig, connectionConfig)
                : buildChannel(clientConfig, connectionConfig);
        channel.notifyWhenStateChanged(channel.getState(false), this::onConnectivityStateChanged);
        Metadata metadata = assembleMetadata(clientConfig, connectionConfig);
//        blockingStub = applyDefaultBlockingStubConfig(
//                MetadataUtils.attachHeaders(VectorServiceGrpc.newBlockingStub(channel), metadata));
        blockingStub = VectorServiceGrpc
                .newBlockingStub(channel)
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));
        asyncStub = VectorServiceGrpc
                .newStub(channel)
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));
        logger.debug("created new PineconeConnection for channel: {}", channel);
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

    public void setBlockingStub(VectorServiceGrpc.VectorServiceBlockingStub blockingStub) {
        this.blockingStub = blockingStub;
    }

    private void onConnectivityStateChanged() {
        logger.debug("channel connectivity state changed to {} for {}",
                channel.getState(false), channel);
    }

    public static ManagedChannel buildChannel(PineconeClientConfig clientConfig,
                                              PineconeConnectionConfig config) {
        String endpoint = getEndpoint(clientConfig, config);
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

    private static Metadata assembleMetadata(PineconeClientConfig clientConfig,
                                             PineconeConnectionConfig connectionConfig) {
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("api-key",
                Metadata.ASCII_STRING_MARSHALLER), clientConfig.getApiKey());
        return metadata;
    }

    private VectorServiceGrpc.VectorServiceBlockingStub applyDefaultBlockingStubConfig(VectorServiceGrpc.VectorServiceBlockingStub stub) {
        return stub
                .withMaxInboundMessageSize(DEFAULT_MAX_MESSAGE_SIZE)
                .withMaxOutboundMessageSize(DEFAULT_MAX_MESSAGE_SIZE);
    }

    static String getEndpoint(PineconeClientConfig clientConfig, PineconeConnectionConfig connectionConfig) {
        String endpoint = (connectionConfig.getConnectionUrl() != null) ?
                connectionConfig.getConnectionUrl().replaceFirst("https?://", "") :
                String.format("%s-%s.svc.%s.pinecone.io",
                        connectionConfig.getIndexName(),
                        clientConfig.getProjectName(),
                        clientConfig.getEnvironment());

        logger.debug("Pinecone endpoint is: " + endpoint);

        return endpoint;
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

        if (this.connectionConfig.getIndexName() != null) {
            if (this.clientConfig.getEnvironment() == null || this.clientConfig.getProjectName() == null) {
                throw new PineconeValidationException("Cannot connect with indexName "
                        + this.connectionConfig.getIndexName()
                        + " unless PineconeClientConfig contains projectName and environment");
            }
        }
    }
}
