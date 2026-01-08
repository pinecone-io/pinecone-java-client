# Pinecone Java SDK - OpenTelemetry Metrics Example

This example demonstrates how to integrate OpenTelemetry metrics with the Pinecone Java SDK using the `ResponseMetadataListener` feature. It captures latency metrics for all data plane operations and exports them to Prometheus/Grafana for visualization.

## What This Example Does

- Captures **client-side latency** (total round-trip time) for Pinecone operations
- Captures **server-side latency** from the `x-pinecone-response-duration-ms` header
- Calculates **network overhead** (client - server duration)
- Exports metrics to OpenTelemetry-compatible backends (Prometheus, Grafana, Datadog, etc.)

## Metrics Recorded

| Metric | Type | Description |
|--------|------|-------------|
| `db.client.operation.duration` | Histogram | Client-measured round-trip time (ms) |
| `pinecone.server.processing.duration` | Histogram | Server processing time from header (ms) |
| `db.client.operation.count` | Counter | Total number of operations |

### Attributes

| Attribute | Description |
|-----------|-------------|
| `db.system` | Always "pinecone" |
| `db.operation.name` | Operation type (upsert, query, fetch, update, delete) |
| `db.namespace` | Pinecone namespace |
| `pinecone.index_name` | Index name |
| `server.address` | Pinecone host |
| `status` | "success" or "error" |

## Prerequisites

- Java 8+
- Maven 3.6+
- Docker and Docker Compose
- A Pinecone account with an API key and index

## Project Structure

```
java-otel-metrics/
├── pom.xml                           # Maven dependencies
├── README.md                         # This file
├── observability/                    # Local observability stack
│   ├── docker-compose.yml            # Prometheus + Grafana + OTel Collector
│   ├── otel-collector-config.yaml    # OTel Collector configuration
│   └── prometheus.yml                # Prometheus scrape config
└── src/main/java/pineconeexamples/
    ├── PineconeOtelMetricsExample.java   # Main example
    └── PineconeMetricsRecorder.java      # Reusable metrics recorder
```

## Quick Start

### 1. Start the Observability Stack

```bash
cd examples/java-otel-metrics/observability
docker-compose up -d
```

This starts:
- **OpenTelemetry Collector** (port 4317) - receives metrics via OTLP
- **Prometheus** (port 9090) - stores metrics
- **Grafana** (port 3000) - visualizes metrics

### 2. Run the Example

```bash
cd examples/java-otel-metrics

export PINECONE_API_KEY=your-api-key
export PINECONE_INDEX=your-index-name
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317

mvn package exec:java -Dexec.mainClass="pineconeexamples.PineconeOtelMetricsExample"
```

### 3. View Metrics in Grafana

1. Open http://localhost:3000
2. Login with `admin` / `admin`
3. Go to **Connections** → **Data sources** → **Add data source**
4. Select **Prometheus**, set URL to `http://prometheus:9090`, click **Save & test**
5. Go to **Dashboards** → **New** → **New Dashboard** → **Add visualization**

### 4. Sample Grafana Queries

**P50 Client vs Server Latency:**
```promql
histogram_quantile(0.5, sum(rate(db_client_operation_duration_milliseconds_bucket[5m])) by (le))
histogram_quantile(0.5, sum(rate(pinecone_server_processing_duration_milliseconds_bucket[5m])) by (le))
```

**P95 Latency by Operation:**
```promql
histogram_quantile(0.95, sum(rate(db_client_operation_duration_milliseconds_bucket[5m])) by (le, db_operation_name))
```

**Operation Count by Type:**
```promql
sum by (db_operation_name) (db_client_operation_count_total)
```

## Understanding the Metrics

### Percentiles Explained

| Percentile | Meaning |
|------------|---------|
| P50 | Median - typical latency |
| P90 | 90% of requests are faster |
| P95 | Tail latency - good for SLAs |
| P99 | Worst-case for most users |

### Network Overhead

The difference between client and server duration shows network overhead:

```
Network Overhead = Client Duration - Server Duration
```

This helps identify whether latency issues are:
- **Server-side** (high server duration)
- **Network-side** (high network overhead)

## Cleanup

```bash
cd examples/java-otel-metrics/observability
docker-compose down
```

## Using in Your Project

Copy `PineconeMetricsRecorder.java` into your project:

```java
Meter meter = meterProvider.get("pinecone.client");
PineconeMetricsRecorder recorder = new PineconeMetricsRecorder(meter);

Pinecone client = new Pinecone.Builder(apiKey)
    .withResponseMetadataListener(recorder)
    .build();

// All operations now emit metrics automatically
Index index = client.getIndexConnection(indexName);
index.upsert(...);  // Metrics recorded!
index.query(...);   // Metrics recorded!
```
