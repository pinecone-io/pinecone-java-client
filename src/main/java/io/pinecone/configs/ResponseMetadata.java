package io.pinecone.configs;

/**
 * Captures response metadata from Pinecone data plane operations.
 * Contains timing information for observability and monitoring.
 *
 * <p>This class provides:
 * <ul>
 *   <li>{@link #getClientDurationMs()} - Total round-trip time measured by the client</li>
 *   <li>{@link #getServerDurationMs()} - Server processing time from x-pinecone-response-duration-ms header</li>
 *   <li>{@link #getNetworkOverheadMs()} - Computed network + serialization overhead</li>
 * </ul>
 *
 * <p>Example usage with a listener:
 * <pre>{@code
 * Pinecone client = new Pinecone.Builder(apiKey)
 *     .withResponseMetadataListener(metadata -> {
 *         System.out.println("Operation: " + metadata.getOperationName());
 *         System.out.println("Server duration: " + metadata.getServerDurationMs() + "ms");
 *         System.out.println("Client duration: " + metadata.getClientDurationMs() + "ms");
 *         System.out.println("Network overhead: " + metadata.getNetworkOverheadMs() + "ms");
 *     })
 *     .build();
 * }</pre>
 */
public class ResponseMetadata {

    private final String operationName;
    private final String indexName;
    private final String namespace;
    private final String serverAddress;
    private final long clientDurationMs;
    private final Long serverDurationMs;
    private final String status;
    private final String grpcStatusCode;
    private final String errorType;

    private ResponseMetadata(Builder builder) {
        this.operationName = builder.operationName;
        this.indexName = builder.indexName;
        this.namespace = builder.namespace;
        this.serverAddress = builder.serverAddress;
        this.clientDurationMs = builder.clientDurationMs;
        this.serverDurationMs = builder.serverDurationMs;
        this.status = builder.status;
        this.grpcStatusCode = builder.grpcStatusCode;
        this.errorType = builder.errorType;
    }

    /**
     * Returns the operation name (e.g., "upsert", "query", "fetch", "update", "delete").
     * Corresponds to OTel attribute: db.operation.name
     */
    public String getOperationName() {
        return operationName;
    }

    /**
     * Returns the Pinecone index name.
     * Corresponds to OTel attribute: pinecone.index_name
     */
    public String getIndexName() {
        return indexName;
    }

    /**
     * Returns the Pinecone namespace (empty string if default namespace).
     * Corresponds to OTel attribute: db.namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Returns the server address/host.
     * Corresponds to OTel attribute: server.address
     */
    public String getServerAddress() {
        return serverAddress;
    }

    /**
     * Returns the total client-side duration in milliseconds.
     * Measured from request initiation to response completion.
     * Corresponds to metric: db.client.operation.duration
     */
    public long getClientDurationMs() {
        return clientDurationMs;
    }

    /**
     * Returns the server processing duration in milliseconds, or null if the
     * x-pinecone-response-duration-ms header was not present.
     * Corresponds to metric: pinecone.server.processing.duration
     */
    public Long getServerDurationMs() {
        return serverDurationMs;
    }

    /**
     * Returns the computed network overhead in milliseconds (client duration minus server duration),
     * or null if server duration is not available.
     * This includes network latency, serialization, and deserialization time.
     */
    public Long getNetworkOverheadMs() {
        if (serverDurationMs == null) {
            return null;
        }
        return clientDurationMs - serverDurationMs;
    }

    /**
     * Returns the operation status: "success" or "error".
     * Corresponds to OTel attribute: status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Returns the raw gRPC status code (e.g., "OK", "UNAVAILABLE", "DEADLINE_EXCEEDED").
     * Corresponds to OTel attribute: db.response.status_code
     */
    public String getGrpcStatusCode() {
        return grpcStatusCode;
    }

    /**
     * Returns the error type category, or null if status is "success".
     * Possible values: "validation", "connection", "server", "rate_limit", "timeout", "auth", "not_found"
     * Corresponds to OTel attribute: error.type
     */
    public String getErrorType() {
        return errorType;
    }

    /**
     * Returns true if the operation was successful.
     */
    public boolean isSuccess() {
        return "success".equals(status);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ResponseMetadata{");
        sb.append("operation=").append(operationName);
        sb.append(", index=").append(indexName);
        if (namespace != null && !namespace.isEmpty()) {
            sb.append(", namespace=").append(namespace);
        }
        sb.append(", clientDurationMs=").append(clientDurationMs);
        if (serverDurationMs != null) {
            sb.append(", serverDurationMs=").append(serverDurationMs);
            sb.append(", networkOverheadMs=").append(getNetworkOverheadMs());
        }
        sb.append(", status=").append(status);
        if (errorType != null) {
            sb.append(", errorType=").append(errorType);
        }
        sb.append("}");
        return sb.toString();
    }

    public static class Builder {
        private String operationName;
        private String indexName;
        private String namespace = "";
        private String serverAddress;
        private long clientDurationMs;
        private Long serverDurationMs;
        private String status = "success";
        private String grpcStatusCode = "OK";
        private String errorType;

        public Builder operationName(String operationName) {
            this.operationName = operationName;
            return this;
        }

        public Builder indexName(String indexName) {
            this.indexName = indexName;
            return this;
        }

        public Builder namespace(String namespace) {
            this.namespace = namespace != null ? namespace : "";
            return this;
        }

        public Builder serverAddress(String serverAddress) {
            this.serverAddress = serverAddress;
            return this;
        }

        public Builder clientDurationMs(long clientDurationMs) {
            this.clientDurationMs = clientDurationMs;
            return this;
        }

        public Builder serverDurationMs(Long serverDurationMs) {
            this.serverDurationMs = serverDurationMs;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder grpcStatusCode(String grpcStatusCode) {
            this.grpcStatusCode = grpcStatusCode;
            return this;
        }

        public Builder errorType(String errorType) {
            this.errorType = errorType;
            return this;
        }

        public ResponseMetadata build() {
            return new ResponseMetadata(this);
        }
    }
}

