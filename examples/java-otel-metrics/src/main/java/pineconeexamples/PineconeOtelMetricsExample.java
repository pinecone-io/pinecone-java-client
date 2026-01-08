package pineconeexamples;

import com.google.common.primitives.Floats;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributeKey;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.UpsertResponse;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Example demonstrating how to integrate OpenTelemetry metrics with the Pinecone Java SDK.
 * 
 * <p>This example shows:
 * <ul>
 *   <li>Setting up OpenTelemetry SDK with MeterProvider</li>
 *   <li>Configuring console and OTLP exporters</li>
 *   <li>Using PineconeMetricsRecorder to capture operation metrics</li>
 *   <li>Performing sample Pinecone operations with metrics recording</li>
 * </ul>
 * 
 * <p>Environment variables:
 * <ul>
 *   <li>PINECONE_API_KEY (required) - Your Pinecone API key</li>
 *   <li>PINECONE_INDEX (required) - Name of your Pinecone index</li>
 *   <li>OTEL_EXPORTER_OTLP_ENDPOINT (optional) - OTLP endpoint (e.g., http://localhost:4317)</li>
 * </ul>
 * 
 * <p>Run with:
 * <pre>
 * mvn package exec:java -Dexec.mainClass="pineconeexamples.PineconeOtelMetricsExample"
 * </pre>
 */
public class PineconeOtelMetricsExample {

    private static final String SERVICE_NAME = "pinecone-otel-example";

    public static void main(String[] args) {
        // Read configuration from environment
        String apiKey = getRequiredEnv("PINECONE_API_KEY");
        String indexName = getRequiredEnv("PINECONE_INDEX");
        String otlpEndpoint = System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT");

        System.out.println("============================================================");
        System.out.println("Pinecone OpenTelemetry Metrics Example");
        System.out.println("============================================================");
        System.out.println("Index: " + indexName);
        System.out.println("OTLP Endpoint: " + (otlpEndpoint != null ? otlpEndpoint : "(not configured - console only)"));
        System.out.println();

        // Initialize OpenTelemetry
        OpenTelemetrySdk openTelemetry = initializeOpenTelemetry(otlpEndpoint);
        
        try {
            // Get a Meter for creating instruments
            Meter meter = openTelemetry.getMeter("pinecone.client");

            // Create the metrics recorder
            PineconeMetricsRecorder metricsRecorder = new PineconeMetricsRecorder(meter);

            // Build Pinecone client with the metrics recorder as listener
            Pinecone pinecone = new Pinecone.Builder(apiKey)
                    .withResponseMetadataListener(metricsRecorder)
                    .build();

            // Get index connection
            Index index = pinecone.getIndexConnection(indexName);

            System.out.println("Performing Pinecone operations...");
            System.out.println();

            // Perform sample operations
            performSampleOperations(index);

            // Close the index connection
            index.close();

            System.out.println();
            System.out.println("Operations complete. Flushing metrics...");
            System.out.println();

            // Give metrics time to export (periodic reader exports every 10 seconds by default)
            // Force a flush by waiting a bit
            Thread.sleep(2000);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Shutdown OpenTelemetry SDK
            openTelemetry.shutdown();
            System.out.println("OpenTelemetry SDK shutdown complete.");
        }
    }

    /**
     * Initialize OpenTelemetry SDK with console and optional OTLP exporters.
     */
    private static OpenTelemetrySdk initializeOpenTelemetry(String otlpEndpoint) {
        // Create resource with service name
        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(
                        AttributeKey.stringKey("service.name"), SERVICE_NAME
                )));

        // Create console exporter (logs metrics to stdout)
        MetricReader consoleReader = PeriodicMetricReader.builder(LoggingMetricExporter.create())
                .setInterval(Duration.ofSeconds(10))
                .build();

        // Define custom histogram buckets optimized for Pinecone latencies (in milliseconds)
        // Fine granularity across the typical latency range (5ms - 500ms)
        List<Double> latencyBuckets = Arrays.asList(
                5.0, 10.0, 15.0, 20.0, 25.0, 30.0, 40.0, 50.0, 60.0, 75.0, 
                100.0, 125.0, 150.0, 175.0, 200.0, 250.0, 300.0, 400.0, 500.0,
                750.0, 1000.0, 2000.0, 5000.0
        );

        // Create a View to apply custom buckets to all histograms from pinecone.client
        View latencyHistogramView = View.builder()
                .setAggregation(Aggregation.explicitBucketHistogram(latencyBuckets))
                .build();

        SdkMeterProviderBuilder meterProviderBuilder = SdkMeterProvider.builder()
                .setResource(resource)
                .registerMetricReader(consoleReader)
                // Apply custom histogram buckets to duration metrics
                .registerView(
                        InstrumentSelector.builder()
                                .setType(InstrumentType.HISTOGRAM)
                                .setMeterName("pinecone.client")
                                .build(),
                        latencyHistogramView
                );

        // Add OTLP exporter if endpoint is configured
        if (otlpEndpoint != null && !otlpEndpoint.isEmpty()) {
            OtlpGrpcMetricExporter otlpExporter = OtlpGrpcMetricExporter.builder()
                    .setEndpoint(otlpEndpoint)
                    .build();

            MetricReader otlpReader = PeriodicMetricReader.builder(otlpExporter)
                    .setInterval(Duration.ofSeconds(10))
                    .build();

            meterProviderBuilder.registerMetricReader(otlpReader);
            System.out.println("OTLP exporter configured for: " + otlpEndpoint);
        }

        SdkMeterProvider meterProvider = meterProviderBuilder.build();

        return OpenTelemetrySdk.builder()
                .setMeterProvider(meterProvider)
                .build();
    }

    /**
     * Perform sample Pinecone operations to generate metrics.
     * Runs a good number of operations to produce meaningful histogram data.
     */
    private static void performSampleOperations(Index index) {
        String namespace = "otel-example-ns";
        Random random = new Random();

        // Generate sample vectors (assuming 3-dimensional index for simplicity)
        // Adjust dimension based on your index configuration
        int dimension = 3;

        // Number of operations to run (increase for richer metrics data)
        int numWarmup = 5;
        int numUpserts = 25;
        int numQueries = 20;
        int numFetches = 15;
        int numUpdates = 10;

        try {
            // 0. Warmup phase - establish connections (metrics not representative)
            System.out.println("0. Warming up connections (first few requests are slower)...");
            for (int i = 0; i < numWarmup; i++) {
                String vectorId = "warmup-vec-" + i;
                List<Float> values = generateRandomVector(dimension, random);
                index.upsert(vectorId, values, namespace);
            }
            // Query to warm up read path
            index.query(1, generateRandomVector(dimension, random), null, null, null, namespace, null, false, false);
            // Clean up warmup vectors
            List<String> warmupIds = new java.util.ArrayList<>();
            for (int i = 0; i < numWarmup; i++) {
                warmupIds.add("warmup-vec-" + i);
            }
            index.deleteByIds(warmupIds, namespace);
            System.out.println("   Warmup complete (connection established)");
            System.out.println();

            // 1. Upsert operations
            System.out.println("1. Upserting " + numUpserts + " vectors...");
            for (int i = 0; i < numUpserts; i++) {
                String vectorId = "otel-vec-" + i;
                List<Float> values = generateRandomVector(dimension, random);
                UpsertResponse response = index.upsert(vectorId, values, namespace);
                if (i % 5 == 0) {
                    System.out.println("   Upserted " + (i + 1) + "/" + numUpserts);
                }
            }
            System.out.println("   Completed " + numUpserts + " upserts");

            // Wait for eventual consistency
            Thread.sleep(2000);

            // 2. Query operations
            System.out.println("2. Running " + numQueries + " queries...");
            for (int i = 0; i < numQueries; i++) {
                List<Float> queryVector = generateRandomVector(dimension, random);
                QueryResponseWithUnsignedIndices response = index.query(
                        5, queryVector, null, null, null, namespace, null, false, false);
                if (i % 5 == 0) {
                    System.out.println("   Query " + (i + 1) + "/" + numQueries + " returned " + 
                            response.getMatchesList().size() + " matches");
                }
            }
            System.out.println("   Completed " + numQueries + " queries");

            // 3. Fetch operations
            System.out.println("3. Running " + numFetches + " fetches...");
            for (int i = 0; i < numFetches; i++) {
                String vectorId = "otel-vec-" + (i % numUpserts);
                index.fetch(Arrays.asList(vectorId), namespace);
            }
            System.out.println("   Completed " + numFetches + " fetches");

            // 4. Update operations
            System.out.println("4. Running " + numUpdates + " updates...");
            for (int i = 0; i < numUpdates; i++) {
                String vectorId = "otel-vec-" + i;
                List<Float> newValues = generateRandomVector(dimension, random);
                index.update(vectorId, newValues, namespace);
            }
            System.out.println("   Completed " + numUpdates + " updates");

            // 5. Delete operations (cleanup)
            System.out.println("5. Deleting vectors...");
            for (int i = 0; i < numUpserts; i += 5) {
                List<String> idsToDelete = Arrays.asList(
                        "otel-vec-" + i,
                        "otel-vec-" + (i + 1),
                        "otel-vec-" + (i + 2),
                        "otel-vec-" + (i + 3),
                        "otel-vec-" + (i + 4)
                );
                index.deleteByIds(idsToDelete, namespace);
            }
            System.out.println("   Completed " + (numUpserts / 5) + " delete batches");

            System.out.println();
            System.out.println("Total operations: " + (numUpserts + numQueries + numFetches + numUpdates + numUpserts/5));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Operation interrupted");
        }
    }

    /**
     * Generate a random vector of the specified dimension.
     */
    private static List<Float> generateRandomVector(int dimension, Random random) {
        Float[] values = new Float[dimension];
        for (int i = 0; i < dimension; i++) {
            values[i] = random.nextFloat();
        }
        return Arrays.asList(values);
    }

    /**
     * Get a required environment variable or exit with an error.
     */
    private static String getRequiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isEmpty()) {
            System.err.println("Error: Required environment variable " + name + " is not set.");
            System.err.println();
            System.err.println("Usage:");
            System.err.println("  export PINECONE_API_KEY=your-api-key");
            System.err.println("  export PINECONE_INDEX=your-index-name");
            System.err.println("  export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317  # optional");
            System.err.println();
            System.err.println("  mvn package exec:java -Dexec.mainClass=\"pineconeexamples.PineconeOtelMetricsExample\"");
            System.exit(1);
        }
        return value;
    }
}

