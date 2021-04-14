package io.pinecone;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import io.pinecone.proto.Core;
import io.pinecone.proto.RPCClientGrpc;
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
    private RPCClientGrpc.RPCClientBlockingStub blockingStub;

    private RPCClientGrpc.RPCClientStub asyncStub;

    public PineconeConnection(PineconeClientConfig clientConfig, PineconeConnectionConfig connectionConfig) {
        connectionConfig.validate();
        this.connectionConfig = connectionConfig;
        this.clientConfig = clientConfig;
        channel = connectionConfig.getCustomChannelBuilder() != null
                ? connectionConfig.getCustomChannelBuilder().apply(clientConfig, connectionConfig)
                : buildChannel(clientConfig, connectionConfig);
        channel.notifyWhenStateChanged(channel.getState(false), this::onConnectivityStateChanged);
        Metadata metadata = assembleMetadata(clientConfig, connectionConfig);
        blockingStub = applyDefaultBlockingStubConfig(
                MetadataUtils.attachHeaders(RPCClientGrpc.newBlockingStub(channel), metadata));
        asyncStub = MetadataUtils.attachHeaders(RPCClientGrpc.newStub(channel), metadata);
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

    /**
     * Send a request over the connection, blocking until a response is received. Throws
     * {@link PineconeException} in case of errors.
     * @param request
     * @return
     */
    public UpsertResponse send(UpsertRequest request) {
        return UpsertResponse.from(
                sendRequest(request.toRequest()));
    }

    /**
     * Send a request over the connection, blocking until a response is received. Throws
     * {@link PineconeException} in case of errors.
     * @param request
     * @return
     */
    public QueryResponse send(QueryRequest request) {
        return QueryResponse.from(
                sendRequest(request.toRequest()),
                clientConfig.getTranslator());
    }

    public ManagedChannel getChannel() {
        return channel;
    }

    public RPCClientGrpc.RPCClientBlockingStub getBlockingStub() {
        return blockingStub;
    }

    public void setBlockingStub(RPCClientGrpc.RPCClientBlockingStub blockingStub) {
        this.blockingStub = blockingStub;
    }

    private void onConnectivityStateChanged() {
        logger.debug("channel connectivity state changed to {} for {}",
                channel.getState(false), channel);
    }

    private Core.Request sendRequest(Core.Request req) {
        try {
            Core.Request response = blockingStub.callUnary(req);
            Core.Status.StatusCode statusCode = response.getStatus().getCode();
            if(Core.Status.StatusCode.SUCCESS != statusCode) {
                throw new PineconeException(
                        String.format("Server returned error: %s; details: %s",
                                response.getStatus().getDescription(),
                                response.getStatus().getDetailsList()));
            }
            return response;
        } catch (StatusRuntimeException e) {
            String requestId = req == null ? null : Long.toUnsignedString(req.getRequestId());
            throw new PineconeException(
                    String.format("Error sending request id %s", requestId), e);
        }
    }

    // wrap in observer that converts to and from internal Core.Request format
    private StreamObserver<Core.Request> streamRequests(StreamObserver<Core.Request> requestStream) {
        try {
            return asyncStub.call(requestStream);
        } catch (StatusRuntimeException e) {
            logger.error("Error streaming requests.", e);
        }
        return null;
    }

    public static ManagedChannel buildChannel(PineconeClientConfig clientConfig,
                                              PineconeConnectionConfig config) {
        String serviceTarget = config.getServiceAuthority();
        NettyChannelBuilder builder = NettyChannelBuilder.forTarget(serviceTarget);
        if (!config.isSecure()) {
            builder.usePlaintext();
        }
        else {
            try {
                builder = builder.overrideAuthority(serviceTarget)
                        .negotiationType(NegotiationType.TLS)
                        .sslContext(GrpcSslContexts.forClient().build());
            } catch (SSLException e) {
                throw new PineconeException("SSL error opening gRPC channel", e);
            }
        }
        return builder.build();
    }

    private static Metadata assembleMetadata(PineconeClientConfig clientConfig,
                                             PineconeConnectionConfig connectionConfig) {
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("service-name",
                Metadata.ASCII_STRING_MARSHALLER), connectionConfig.getServiceName());
        metadata.put(Metadata.Key.of("api-key",
                Metadata.ASCII_STRING_MARSHALLER), clientConfig.getApiKey());
        return metadata;
    }

    private RPCClientGrpc.RPCClientBlockingStub applyDefaultBlockingStubConfig(RPCClientGrpc.RPCClientBlockingStub stub) {
        return stub
                .withMaxInboundMessageSize(DEFAULT_MAX_MESSAGE_SIZE)
                .withMaxOutboundMessageSize(DEFAULT_MAX_MESSAGE_SIZE);
    }
}
