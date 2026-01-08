package io.pinecone.configs;

/**
 * Listener interface for receiving response metadata from Pinecone data plane operations.
 *
 * <p>Implement this interface to capture timing metrics for observability purposes.
 * The listener is invoked after each data plane operation completes (success or failure).
 *
 * <p>Supported operations:
 * <ul>
 *   <li>upsert - Insert or update vectors</li>
 *   <li>query - Search for similar vectors</li>
 *   <li>fetch - Retrieve vectors by ID</li>
 *   <li>update - Update vector metadata</li>
 *   <li>delete - Delete vectors</li>
 * </ul>
 *
 * <p>Example - Simple logging:
 * <pre>{@code
 * Pinecone client = new Pinecone.Builder(apiKey)
 *     .withResponseMetadataListener(metadata -> {
 *         logger.info("Pinecone {} completed in {}ms (server: {}ms)",
 *             metadata.getOperationName(),
 *             metadata.getClientDurationMs(),
 *             metadata.getServerDurationMs());
 *     })
 *     .build();
 * }</pre>
 *
 * <p>Example - OpenTelemetry integration:
 * <pre>{@code
 * Meter meter = openTelemetry.getMeter("io.pinecone");
 * DoubleHistogram clientDuration = meter.histogramBuilder("db.client.operation.duration")
 *     .setUnit("ms").build();
 * DoubleHistogram serverDuration = meter.histogramBuilder("pinecone.server.processing.duration")
 *     .setUnit("ms").build();
 * LongCounter operationCount = meter.counterBuilder("db.client.operation.count").build();
 *
 * Pinecone client = new Pinecone.Builder(apiKey)
 *     .withResponseMetadataListener(metadata -> {
 *         Attributes attrs = Attributes.builder()
 *             .put("db.system", "pinecone")
 *             .put("db.operation.name", metadata.getOperationName())
 *             .put("db.namespace", metadata.getNamespace())
 *             .put("status", metadata.getStatus())
 *             .build();
 *
 *         clientDuration.record(metadata.getClientDurationMs(), attrs);
 *         if (metadata.getServerDurationMs() != null) {
 *             serverDuration.record(metadata.getServerDurationMs(), attrs);
 *         }
 *         operationCount.add(1, attrs);
 *     })
 *     .build();
 * }</pre>
 *
 * <p>Example - Micrometer/Prometheus:
 * <pre>{@code
 * Pinecone client = new Pinecone.Builder(apiKey)
 *     .withResponseMetadataListener(metadata -> {
 *         Timer.builder("pinecone.client.duration")
 *             .tag("operation", metadata.getOperationName())
 *             .tag("status", metadata.getStatus())
 *             .register(meterRegistry)
 *             .record(metadata.getClientDurationMs(), TimeUnit.MILLISECONDS);
 *     })
 *     .build();
 * }</pre>
 *
 * @see ResponseMetadata
 */
@FunctionalInterface
public interface ResponseMetadataListener {

    /**
     * Called after each data plane operation completes.
     *
     * <p>This method is called synchronously after the gRPC response is received.
     * Implementations should be lightweight and non-blocking to avoid impacting
     * request latency. For heavy processing, consider queuing the metadata for
     * async handling.
     *
     * <p>Exceptions thrown by this method are logged but do not affect the
     * operation result.
     *
     * @param metadata The response metadata containing timing and operation details
     */
    void onResponse(ResponseMetadata metadata);
}

