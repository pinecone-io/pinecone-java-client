package io.pinecone.configs;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * gRPC ClientInterceptor that captures response metadata from Pinecone data plane operations.
 * Extracts timing information from the x-pinecone-response-duration-ms trailing header.
 */
public class ResponseMetadataInterceptor implements ClientInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ResponseMetadataInterceptor.class);

    private static final Metadata.Key<String> RESPONSE_DURATION_KEY =
            Metadata.Key.of("x-pinecone-response-duration-ms", Metadata.ASCII_STRING_MARSHALLER);

    // Operations to track (matches VectorService RPC method names)
    private static final Set<String> TRACKED_OPERATIONS = new HashSet<>(Arrays.asList(
            "Upsert", "Query", "Fetch", "Update", "Delete"
    ));

    private final ResponseMetadataListener listener;
    private final String indexName;
    private final String serverAddress;

    /**
     * Creates a new interceptor.
     *
     * @param listener      The listener to receive response metadata
     * @param indexName     The name of the Pinecone index
     * @param serverAddress The server address/host
     */
    public ResponseMetadataInterceptor(ResponseMetadataListener listener, String indexName, String serverAddress) {
        this.listener = listener;
        this.indexName = indexName;
        this.serverAddress = serverAddress;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        String methodName = method.getBareMethodName();

        // Only intercept tracked operations
        if (!TRACKED_OPERATIONS.contains(methodName)) {
            return next.newCall(method, callOptions);
        }

        final long startTimeNanos = System.nanoTime();
        final String operationName = methodName.toLowerCase();

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                next.newCall(method, callOptions)) {

            private volatile String namespace = "";

            @Override
            public void sendMessage(ReqT message) {
                // Extract namespace from request for metrics context
                namespace = extractNamespace(message);
                super.sendMessage(message);
            }

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(
                        responseListener) {

                    private Metadata initialHeaders;

                    @Override
                    public void onHeaders(Metadata headers) {
                        // Capture initial headers (some servers send custom headers here)
                        this.initialHeaders = headers;
                        if (logger.isDebugEnabled()) {
                            logger.debug("Initial headers for {}: {}", operationName, headers.keys());
                        }
                        super.onHeaders(headers);
                    }

                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        try {
                            long clientDurationMs = (System.nanoTime() - startTimeNanos) / 1_000_000;

                            if (logger.isDebugEnabled()) {
                                logger.debug("Trailing metadata for {}: {}", operationName, trailers.keys());
                            }

                            // Try to extract server duration from trailing metadata first, then initial headers
                            Long serverDurationMs = extractServerDuration(trailers);
                            if (serverDurationMs == null && initialHeaders != null) {
                                serverDurationMs = extractServerDuration(initialHeaders);
                            }

                            // Determine status and error type
                            String statusStr = status.isOk() ? "success" : "error";
                            String errorType = status.isOk() ? null : mapGrpcStatusToErrorType(status.getCode());

                            // Build and emit metadata
                            ResponseMetadata metadata = ResponseMetadata.builder()
                                    .operationName(operationName)
                                    .indexName(indexName)
                                    .namespace(namespace)
                                    .serverAddress(serverAddress)
                                    .clientDurationMs(clientDurationMs)
                                    .serverDurationMs(serverDurationMs)
                                    .status(statusStr)
                                    .grpcStatusCode(status.getCode().name())
                                    .errorType(errorType)
                                    .build();

                            invokeListener(metadata);

                        } catch (Exception e) {
                            logger.warn("Error capturing response metadata", e);
                        }

                        super.onClose(status, trailers);
                    }
                }, headers);
            }
        };
    }

    private Long extractServerDuration(Metadata trailers) {
        String durationHeader = trailers.get(RESPONSE_DURATION_KEY);
        if (durationHeader != null) {
            try {
                return Long.parseLong(durationHeader.trim());
            } catch (NumberFormatException e) {
                logger.warn("Invalid x-pinecone-response-duration-ms header value: {}", durationHeader);
            }
        }
        return null;
    }

    private <T> String extractNamespace(T message) {
        // Use reflection to extract namespace from request objects
        // All tracked request types have a getNamespace() method
        try {
            java.lang.reflect.Method getNamespace = message.getClass().getMethod("getNamespace");
            Object result = getNamespace.invoke(message);
            return result != null ? result.toString() : "";
        } catch (NoSuchMethodException e) {
            // Some requests may not have namespace
            return "";
        } catch (Exception e) {
            logger.debug("Could not extract namespace from request", e);
            return "";
        }
    }

    private String mapGrpcStatusToErrorType(Status.Code code) {
        switch (code) {
            case INVALID_ARGUMENT:
            case FAILED_PRECONDITION:
            case OUT_OF_RANGE:
                return "validation";

            case UNAVAILABLE:
            case UNKNOWN:
            case ABORTED:
                return "connection";

            case INTERNAL:
            case DATA_LOSS:
            case UNIMPLEMENTED:
                return "server";

            case RESOURCE_EXHAUSTED:
                return "rate_limit";

            case DEADLINE_EXCEEDED:
            case CANCELLED:
                return "timeout";

            case UNAUTHENTICATED:
            case PERMISSION_DENIED:
                return "auth";

            case NOT_FOUND:
            case ALREADY_EXISTS:
                return "not_found";

            default:
                return "unknown";
        }
    }

    private void invokeListener(ResponseMetadata metadata) {
        try {
            listener.onResponse(metadata);
        } catch (Exception e) {
            logger.error("Exception in ResponseMetadataListener.onResponse()", e);
        }
    }
}

