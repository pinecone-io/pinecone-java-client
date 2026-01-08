package pineconeexamples;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.pinecone.configs.ResponseMetadata;
import io.pinecone.configs.ResponseMetadataListener;

/**
 * A reusable OpenTelemetry metrics recorder for Pinecone operations.
 * 
 * <p>This class implements {@link ResponseMetadataListener} to capture response metadata
 * from Pinecone data plane operations and record them as OpenTelemetry metrics.
 * 
 * <p>Metrics recorded:
 * <ul>
 *   <li><b>db.client.operation.duration</b> - Histogram of client-measured round-trip time (ms)</li>
 *   <li><b>pinecone.server.processing.duration</b> - Histogram of server processing time (ms)</li>
 *   <li><b>db.client.operation.count</b> - Counter of operations by status</li>
 * </ul>
 * 
 * <p>Attributes follow OpenTelemetry semantic conventions for database clients:
 * <ul>
 *   <li>db.system = "pinecone"</li>
 *   <li>db.operation.name = operation name (upsert, query, fetch, update, delete)</li>
 *   <li>db.namespace = Pinecone namespace</li>
 *   <li>pinecone.index_name = index name</li>
 *   <li>server.address = Pinecone host</li>
 *   <li>status = "success" or "error"</li>
 *   <li>error.type = error category (only when status="error")</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>{@code
 * Meter meter = meterProvider.get("pinecone.client");
 * PineconeMetricsRecorder recorder = new PineconeMetricsRecorder(meter);
 * 
 * Pinecone client = new Pinecone.Builder(apiKey)
 *     .withResponseMetadataListener(recorder)
 *     .build();
 * }</pre>
 * 
 * <p>You can copy this class into your project and customize it as needed.
 */
public class PineconeMetricsRecorder implements ResponseMetadataListener {

    // Attribute keys following OTel semantic conventions
    private static final AttributeKey<String> DB_SYSTEM = AttributeKey.stringKey("db.system");
    private static final AttributeKey<String> DB_OPERATION_NAME = AttributeKey.stringKey("db.operation.name");
    private static final AttributeKey<String> DB_NAMESPACE = AttributeKey.stringKey("db.namespace");
    private static final AttributeKey<String> PINECONE_INDEX_NAME = AttributeKey.stringKey("pinecone.index_name");
    private static final AttributeKey<String> SERVER_ADDRESS = AttributeKey.stringKey("server.address");
    private static final AttributeKey<String> STATUS = AttributeKey.stringKey("status");
    private static final AttributeKey<String> ERROR_TYPE = AttributeKey.stringKey("error.type");

    private final LongHistogram clientDurationHistogram;
    private final LongHistogram serverDurationHistogram;
    private final LongCounter operationCounter;

    /**
     * Creates a new PineconeMetricsRecorder with the given OpenTelemetry Meter.
     *
     * @param meter the OpenTelemetry Meter to use for creating instruments
     */
    public PineconeMetricsRecorder(Meter meter) {
        // Client-side operation duration histogram
        this.clientDurationHistogram = meter.histogramBuilder("db.client.operation.duration")
                .setDescription("Duration of Pinecone operations from client perspective")
                .setUnit("ms")
                .ofLongs()
                .build();

        // Server-side processing duration histogram
        this.serverDurationHistogram = meter.histogramBuilder("pinecone.server.processing.duration")
                .setDescription("Server processing time from x-pinecone-response-duration-ms header")
                .setUnit("ms")
                .ofLongs()
                .build();

        // Operation counter
        this.operationCounter = meter.counterBuilder("db.client.operation.count")
                .setDescription("Total number of Pinecone operations")
                .setUnit("{operation}")
                .build();
    }

    @Override
    public void onResponse(ResponseMetadata metadata) {
        // Build common attributes
        AttributesBuilder attributesBuilder = Attributes.builder()
                .put(DB_SYSTEM, "pinecone")
                .put(DB_OPERATION_NAME, metadata.getOperationName())
                .put(PINECONE_INDEX_NAME, metadata.getIndexName())
                .put(SERVER_ADDRESS, metadata.getServerAddress())
                .put(STATUS, metadata.getStatus());

        // Add namespace if present
        String namespace = metadata.getNamespace();
        if (namespace != null && !namespace.isEmpty()) {
            attributesBuilder.put(DB_NAMESPACE, namespace);
        }

        // Add error type if this was an error
        if (!metadata.isSuccess() && metadata.getErrorType() != null) {
            attributesBuilder.put(ERROR_TYPE, metadata.getErrorType());
        }

        Attributes attributes = attributesBuilder.build();

        // Record client duration (always available)
        clientDurationHistogram.record(metadata.getClientDurationMs(), attributes);

        // Record server duration (if available from header)
        Long serverDuration = metadata.getServerDurationMs();
        if (serverDuration != null) {
            serverDurationHistogram.record(serverDuration, attributes);
        }

        // Increment operation counter
        operationCounter.add(1, attributes);
    }
}

