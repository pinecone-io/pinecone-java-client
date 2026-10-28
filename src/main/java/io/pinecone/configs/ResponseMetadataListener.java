package io.pinecone.configs;

/**
 * Callback that receives a {@link ResponseMetadata} after each upsert, query, fetch, update, delete and list call
 * on an index, for logging, metrics or tracing.
 *
 * <p>Set it with {@link io.pinecone.clients.Pinecone.Builder#withResponseMetadataListener
 * Pinecone.Builder.withResponseMetadataListener}; it then applies to the {@link io.pinecone.clients.Index} and
 * {@link io.pinecone.clients.AsyncIndex} connections that client opens. Connections are shared by index name across
 * all {@code Pinecone} instances in the JVM, so if another instance opened the index first, that connection keeps
 * the other instance's listener (or none). Other index calls (such as describing index stats or managing
 * namespaces), control-plane calls and {@link io.pinecone.clients.Inference} calls don't report metadata. The
 * listener runs before the call returns to your code, so keep it fast.
 *
 * <pre>{@code
 * Pinecone pc = new Pinecone.Builder(System.getenv("PINECONE_API_KEY"))
 *     .withResponseMetadataListener(metadata ->
 *         System.out.println(metadata.getOperationName() + " " + metadata.getStatus()
 *             + " in " + metadata.getClientDurationMs() + " ms"))
 *     .build();
 * Index index = pc.getIndexConnection("product-catalog");
 * }</pre>
 *
 * @see ResponseMetadata
 */
@FunctionalInterface
public interface ResponseMetadataListener {

    /**
     * Called once when a tracked call finishes, whether it succeeded or failed.
     *
     * <p>For {@link io.pinecone.clients.Index} calls it runs on the calling thread before the method returns; for
     * {@link io.pinecone.clients.AsyncIndex} calls it runs on a gRPC thread before the future completes. It can run
     * on several threads at once, so make it thread-safe, and hand slow work such as network export to another
     * thread. The client logs and ignores any exception it throws, so the call's result is unaffected.
     *
     * <pre>{@code
     * ResponseMetadataListener slowCalls = metadata -> {
     *     if (metadata.getClientDurationMs() > 500) {
     *         System.err.println("Slow " + metadata.getOperationName() + " on " + metadata.getIndexName());
     *     }
     * };
     * }</pre>
     *
     * @param metadata timing, outcome and usage details for the call that finished
     */
    void onResponse(ResponseMetadata metadata);
}

