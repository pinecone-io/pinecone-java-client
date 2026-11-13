package io.pinecone.configs;

/**
 * Timing, outcome and usage details for one data-plane call, passed to a {@link ResponseMetadataListener}.
 *
 * <p>The client creates one for each upsert, query, fetch, fetch-by-metadata, update, delete and list call made
 * through an {@link io.pinecone.clients.Index} or {@link io.pinecone.clients.AsyncIndex}, whether the call succeeds or
 * fails, when a listener is set with {@link io.pinecone.clients.Pinecone.Builder#withResponseMetadataListener
 * Pinecone.Builder.withResponseMetadataListener}. You read
 * these values in the listener; you don't construct this class. All durations are whole milliseconds.
 *
 * <pre>{@code
 * Pinecone pc = new Pinecone.Builder(System.getenv("PINECONE_API_KEY"))
 *     .withResponseMetadataListener(metadata -> {
 *         if (!metadata.isSuccess()) {
 *             System.err.println(metadata.getOperationName() + " on " + metadata.getIndexName()
 *                 + " failed: " + metadata.getGrpcStatusCode());
 *         } else if (metadata.getServerDurationMs() != null) {
 *             System.out.println(metadata.getOperationName() + " took " + metadata.getClientDurationMs()
 *                 + " ms, " + metadata.getNetworkOverheadMs() + " ms outside the server");
 *         }
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
    private final Integer readUnits;
    private final Integer upsertedCount;
    private final Integer matchedRecords;

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
        this.readUnits = builder.readUnits;
        this.upsertedCount = builder.upsertedCount;
        this.matchedRecords = builder.matchedRecords;
    }

    /**
     * The kind of call, in lower case: "upsert", "query", "fetch", "fetchbymetadata", "update",
     * "delete" or "list".
     *
     * @return the operation name; matches the OpenTelemetry {@code db.operation.name} attribute
     */
    public String getOperationName() {
        return operationName;
    }

    /**
     * The name of the index the call went to, as passed to {@code getIndexConnection}.
     *
     * @return the index name
     */
    public String getIndexName() {
        return indexName;
    }

    /**
     * The namespace named in the request.
     *
     * @return the namespace, or an empty string when the request used the default namespace; never null
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * The index host the call was sent to.
     *
     * @return the host, or an empty string when the connection was built without one
     */
    public String getServerAddress() {
        return serverAddress;
    }

    /**
     * Wall-clock time the client spent on the call, from starting the request to receiving the final status,
     * including network and serialization time.
     *
     * @return the duration in milliseconds
     */
    public long getClientDurationMs() {
        return clientDurationMs;
    }

    /**
     * Time the server reports it spent processing the request, read from the
     * {@code x-pinecone-response-duration-ms} response header.
     *
     * @return the duration in milliseconds, or null when the response did not include that header
     */
    public Long getServerDurationMs() {
        return serverDurationMs;
    }

    /**
     * Time spent outside the server: {@link #getClientDurationMs()} minus {@link #getServerDurationMs()}. A large
     * value points at network latency or serialization rather than server work.
     *
     * @return the overhead in milliseconds, or null when the server duration is null
     */
    public Long getNetworkOverheadMs() {
        if (serverDurationMs == null) {
            return null;
        }
        return clientDurationMs - serverDurationMs;
    }

    /**
     * Whether the call succeeded, as a string suited to metric labels.
     *
     * @return "success" or "error"
     */
    public String getStatus() {
        return status;
    }

    /**
     * The gRPC status code the call ended with, such as "OK", "NOT_FOUND" or "UNAVAILABLE".
     *
     * @return the name of the {@link io.grpc.Status.Code}
     */
    public String getGrpcStatusCode() {
        return grpcStatusCode;
    }

    /**
     * A coarse category for a failed call, derived from the gRPC status code, for grouping errors in metrics:
     * "validation" (bad request), "auth", "not_found" (also used for ALREADY_EXISTS), "rate_limit", "timeout"
     * (also used for a cancelled call), "connection", "server" or "unknown".
     *
     * @return the category, or null when the call succeeded
     */
    public String getErrorType() {
        return errorType;
    }

    /**
     * Read units the server charged for a query, fetch, fetch-by-metadata or list call.
     *
     * @return the read units, or null for other operations and when the response reported no usage
     */
    public Integer getReadUnits() {
        return readUnits;
    }

    /**
     * Number of records the server accepted in an upsert call.
     *
     * @return the count, or null for other operations and for a failed upsert
     */
    public Integer getUpsertedCount() {
        return upsertedCount;
    }

    /**
     * Number of records an update call matched.
     *
     * @return the count, or null for other operations and when the server did not report it
     */
    public Integer getMatchedRecords() {
        return matchedRecords;
    }

    /**
     * Whether the call ended with gRPC status OK.
     *
     * @return true when {@link #getStatus()} is "success"
     */
    public boolean isSuccess() {
        return "success".equals(status);
    }

    /**
     * Starts a builder. The client uses it to create instances; callers normally don't need it, except to create
     * test values for their own listener.
     *
     * <pre>{@code
     * ResponseMetadata metadata = ResponseMetadata.builder()
     *     .operationName("query")
     *     .indexName("product-catalog")
     *     .clientDurationMs(48)
     *     .serverDurationMs(31L)
     *     .build();
     * }</pre>
     *
     * @return a builder with status "success", gRPC status "OK" and an empty namespace
     */
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
        if (readUnits != null) {
            sb.append(", readUnits=").append(readUnits);
        }
        if (upsertedCount != null) {
            sb.append(", upsertedCount=").append(upsertedCount);
        }
        if (matchedRecords != null) {
            sb.append(", matchedRecords=").append(matchedRecords);
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Builds {@link ResponseMetadata} values. Each setter sets the field read by the matching getter on
     * {@link ResponseMetadata}; unset counts and the server duration stay null.
     *
     * <pre>{@code
     * ResponseMetadata failed = ResponseMetadata.builder()
     *     .operationName("upsert")
     *     .indexName("product-catalog")
     *     .status("error")
     *     .grpcStatusCode("UNAVAILABLE")
     *     .errorType("connection")
     *     .build();
     * }</pre>
     */
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
        private Integer readUnits;
        private Integer upsertedCount;
        private Integer matchedRecords;

        /**
         * Sets the value returned by {@link ResponseMetadata#getOperationName()}.
         *
         * @param operationName the lower-case operation name, such as "query"
         * @return this builder
         */
        public Builder operationName(String operationName) {
            this.operationName = operationName;
            return this;
        }

        /**
         * Sets the value returned by {@link ResponseMetadata#getIndexName()}.
         *
         * @param indexName the index name
         * @return this builder
         */
        public Builder indexName(String indexName) {
            this.indexName = indexName;
            return this;
        }

        /**
         * Sets the value returned by {@link ResponseMetadata#getNamespace()}.
         *
         * @param namespace the namespace; null is stored as an empty string
         * @return this builder
         */
        public Builder namespace(String namespace) {
            this.namespace = namespace != null ? namespace : "";
            return this;
        }

        /**
         * Sets the value returned by {@link ResponseMetadata#getServerAddress()}.
         *
         * @param serverAddress the index host
         * @return this builder
         */
        public Builder serverAddress(String serverAddress) {
            this.serverAddress = serverAddress;
            return this;
        }

        /**
         * Sets the value returned by {@link ResponseMetadata#getClientDurationMs()}.
         *
         * @param clientDurationMs the client-measured duration in milliseconds
         * @return this builder
         */
        public Builder clientDurationMs(long clientDurationMs) {
            this.clientDurationMs = clientDurationMs;
            return this;
        }

        /**
         * Sets the value returned by {@link ResponseMetadata#getServerDurationMs()}.
         *
         * @param serverDurationMs the server-reported duration in milliseconds, or null if unknown
         * @return this builder
         */
        public Builder serverDurationMs(Long serverDurationMs) {
            this.serverDurationMs = serverDurationMs;
            return this;
        }

        /**
         * Sets the value returned by {@link ResponseMetadata#getStatus()}.
         *
         * @param status "success" or "error"
         * @return this builder
         */
        public Builder status(String status) {
            this.status = status;
            return this;
        }

        /**
         * Sets the value returned by {@link ResponseMetadata#getGrpcStatusCode()}.
         *
         * @param grpcStatusCode the gRPC status code name, such as "OK"
         * @return this builder
         */
        public Builder grpcStatusCode(String grpcStatusCode) {
            this.grpcStatusCode = grpcStatusCode;
            return this;
        }

        /**
         * Sets the value returned by {@link ResponseMetadata#getErrorType()}.
         *
         * @param errorType the error category, or null for a successful call
         * @return this builder
         */
        public Builder errorType(String errorType) {
            this.errorType = errorType;
            return this;
        }

        /**
         * Sets the value returned by {@link ResponseMetadata#getReadUnits()}.
         *
         * @param readUnits the read units charged, or null
         * @return this builder
         */
        public Builder readUnits(Integer readUnits) {
            this.readUnits = readUnits;
            return this;
        }

        /**
         * Sets the value returned by {@link ResponseMetadata#getUpsertedCount()}.
         *
         * @param upsertedCount the number of records upserted, or null
         * @return this builder
         */
        public Builder upsertedCount(Integer upsertedCount) {
            this.upsertedCount = upsertedCount;
            return this;
        }

        /**
         * Sets the value returned by {@link ResponseMetadata#getMatchedRecords()}.
         *
         * @param matchedRecords the number of records matched, or null
         * @return this builder
         */
        public Builder matchedRecords(Integer matchedRecords) {
            this.matchedRecords = matchedRecords;
            return this;
        }

        /**
         * Creates the {@link ResponseMetadata}.
         *
         * @return a new instance with the values set on this builder
         */
        public ResponseMetadata build() {
            return new ResponseMetadata(this);
        }
    }
}

