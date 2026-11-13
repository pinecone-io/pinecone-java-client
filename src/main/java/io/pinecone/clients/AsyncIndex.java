package io.pinecone.clients;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.Struct;
import io.pinecone.commons.IndexInterface;
import io.pinecone.configs.PineconeConfig;
import io.pinecone.configs.PineconeConnection;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.proto.*;
import io.pinecone.proto.CreateNamespaceRequest;
import io.pinecone.proto.DeleteRequest;
import io.pinecone.proto.DescribeIndexStatsRequest;
import io.pinecone.proto.FetchResponse;
import io.pinecone.proto.FetchByMetadataRequest;
import io.pinecone.proto.FetchByMetadataResponse;
import io.pinecone.proto.ListNamespacesResponse;
import io.pinecone.proto.ListResponse;
import io.pinecone.proto.MetadataSchema;
import io.pinecone.proto.NamespaceDescription;
import io.pinecone.proto.QueryRequest;
import io.pinecone.proto.QueryResponse;
import io.pinecone.proto.UpdateRequest;
import io.pinecone.proto.UpdateResponse;
import io.pinecone.proto.UpsertRequest;
import io.pinecone.proto.UpsertResponse;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
import okhttp3.OkHttpClient;
import org.openapitools.db_data.client.ApiClient;
import org.openapitools.db_data.client.ApiException;
import org.openapitools.db_data.client.Configuration;
import org.openapitools.db_data.client.api.BulkOperationsApi;
import org.openapitools.db_data.client.model.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.pinecone.clients.Pinecone.buildOkHttpClient;

/**
 * A non-blocking client for reading and writing the records in one Pinecone index.
 *
 * <p>Get one from {@link Pinecone#getAsyncIndexConnection(String)}, which looks up the index host for you. Vector
 * operations (upsert, query, fetch, update, delete, list, stats and namespaces) go over gRPC and return a
 * {@code ListenableFuture}; the bulk import operations ({@code startImport} and related methods) go over HTTP and
 * block. {@link Index} has the same vector operations as blocking calls, and is where the integrated inference
 * operations ({@code upsertRecords}, {@code searchRecords}) live. An {@code AsyncIndex} is safe to share across
 * threads. Every {@code Index} and {@code AsyncIndex} that one {@link Pinecone} client opens for the same index
 * shares one gRPC channel, so get one, reuse it, and call {@link #close()} when you are done with it.
 *
 * <pre>{@code
 * Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
 * AsyncIndex asyncIndex = pinecone.getAsyncIndexConnection("product-catalog");
 *
 * ListenableFuture<QueryResponseWithUnsignedIndices> future =
 *         asyncIndex.queryByVector(3, Arrays.asList(0.12f, 0.48f, 0.91f), "articles-en");
 * QueryResponseWithUnsignedIndices response = future.get();
 * for (ScoredVectorWithUnsignedIndices match : response.getMatchesList()) {
 *     System.out.println(match.getId() + " " + match.getScore());
 * }
 * }</pre>
 */
public class AsyncIndex implements IndexInterface<ListenableFuture<UpsertResponse>,
        ListenableFuture<QueryResponseWithUnsignedIndices>,
        ListenableFuture<FetchResponse>,
        ListenableFuture<UpdateResponse>,
        ListenableFuture<DeleteResponse>,
        ListenableFuture<DescribeIndexStatsResponse>,
        ListenableFuture<ListResponse>,
        ListenableFuture<ListNamespacesResponse>,
        ListenableFuture<NamespaceDescription>>{

    private final PineconeConnection connection;
    private final VectorServiceGrpc.VectorServiceFutureStub asyncStub;
    private final AtomicBoolean closed = new AtomicBoolean();
    BulkOperationsApi bulkOperations;

    /**
     * Creates an {@code AsyncIndex} from an existing connection.
     *
     * <p>Most code calls {@link Pinecone#getAsyncIndexConnection(String)} instead, which resolves the index host and
     * reuses the shared connection for you. The constructor does not look anything up: the HTTP operations use the
     * host and TLS setting in {@code config}, and the gRPC operations use {@code connection}.
     *
     * @param config the API key, index host and TLS setting for the HTTP operations; the host is the index host
     *               (for example, from {@link Pinecone#describeIndex(String)}), not the control-plane host
     * @param connection the gRPC connection to the index; must not be null
     * @param indexName the name of the index, such as {@code "product-catalog"}
     * @throws PineconeValidationException if {@code connection} is null
     */
    public AsyncIndex(PineconeConfig config, PineconeConnection connection, String indexName) {
        if (connection == null) {
            throw new PineconeValidationException("Pinecone connection object cannot be null.");
        }

        this.connection = connection;
        this.asyncStub = connection.getAsyncStub();

        OkHttpClient customOkHttpClient = config.getCustomOkHttpClient();
        ApiClient apiClient = (customOkHttpClient != null) ? new ApiClient(customOkHttpClient) : new ApiClient(buildOkHttpClient(config.getProxyConfig()));
        apiClient.setApiKey(config.getApiKey());
        apiClient.setUserAgent(config.getUserAgent());
        apiClient.addDefaultHeader("X-Pinecone-Api-Version", Configuration.VERSION);

        this.bulkOperations = new BulkOperationsApi(apiClient);
        String protocol = config.isTLSEnabled() ? "https://" : "http://";
        bulkOperations.setCustomBaseUrl(protocol + config.getHost());
    }

    /**
     * Writes a batch of vectors to a namespace, replacing any existing vector with the same id.
     *
     * <p>Use this overload to load data: one request carries many vectors. The server rejects a request that is too
     * large, so split big loads into several calls. To write a single vector, the other {@code upsert} overloads are
     * shorter. A namespace that does not exist yet is created. Writes are not visible instantly: a query or stats call
     * made right after an upsert may not reflect it yet.
     *
     * <pre>{@code
     * Struct metadata = Struct.newBuilder()
     *         .putFields("category", Value.newBuilder().setStringValue("news").build())
     *         .build();
     * List<VectorWithUnsignedIndices> vectors = Arrays.asList(
     *         IndexInterface.buildUpsertVectorWithUnsignedIndices(
     *                 "doc-1", Arrays.asList(0.12f, 0.48f, 0.91f), null, null, metadata),
     *         IndexInterface.buildUpsertVectorWithUnsignedIndices(
     *                 "doc-2", Arrays.asList(0.33f, 0.05f, 0.72f), null, null, metadata));
     *
     * UpsertResponse response = asyncIndex.upsert(vectors, "articles-en").get();
     * System.out.println(response.getUpsertedCount());
     * }</pre>
     *
     * @param vectorList the vectors to write, for example built with
     *                   {@link IndexInterface#buildUpsertVectorWithUnsignedIndices}; each needs an id and dense values
     *                   whose length matches the index dimension (an empty list, not null, for a sparse index)
     * @param namespace the namespace to write to, such as {@code "articles-en"}; {@code ""} writes to the default
     *                  namespace. Must not be null.
     * @return a future that completes with the server's response;
     *         {@code getUpsertedCount()} is the number of vectors written
     * @throws PineconeValidationException if a vector has no id, has sparse indices without sparse values or the
     *         reverse, has sparse lists of different lengths, or has a sparse index outside the unsigned 32-bit range;
     *         thrown by this call, not through the future
     */
    @Override
    public ListenableFuture<UpsertResponse> upsert(List<VectorWithUnsignedIndices> vectorList,
                                                   String namespace) {
        UpsertRequest upsertRequest = validateUpsertRequest(vectorList, namespace);
        return asyncStub.upsert(upsertRequest);
    }

    /**
     * Writes one dense vector to the default namespace, replacing any existing vector with the same id.
     *
     * <p>To write to a named namespace, use {@link #upsert(String, List, String)}; to write many vectors, use
     * {@link #upsert(List, String)}. A query made right after an upsert may not reflect it yet.
     *
     * <pre>{@code
     * UpsertResponse response = asyncIndex.upsert("doc-1", Arrays.asList(0.12f, 0.48f, 0.91f)).get();
     * }</pre>
     *
     * @param id the vector id, such as {@code "doc-1"}; must not be null or empty
     * @param values the dense values; the length must match the index dimension. Must not be null.
     * @return a future that completes with the server's response;
     *         {@code getUpsertedCount()} is 1 when the vector was written
     * @throws PineconeValidationException if {@code id} is null or empty; thrown by this call, not through the future
     */
    @Override
    public ListenableFuture<UpsertResponse> upsert(String id,
                                                   List<Float> values) {
        return upsert(id, values, null, null, null, null);
    }

    /**
     * Writes one dense vector to a namespace, replacing any existing vector with the same id.
     *
     * <p>A namespace that does not exist yet is created. To add sparse values or metadata, use
     * {@link #upsert(String, List, List, List, Struct, String)}; to write many vectors, use
     * {@link #upsert(List, String)}. A query made right after an upsert may not reflect it yet.
     *
     * <pre>{@code
     * UpsertResponse response = asyncIndex.upsert("doc-1", Arrays.asList(0.12f, 0.48f, 0.91f), "articles-en").get();
     * }</pre>
     *
     * @param id the vector id, such as {@code "doc-1"}; must not be null or empty
     * @param values the dense values; the length must match the index dimension. Must not be null.
     * @param namespace the namespace to write to, such as {@code "articles-en"}; null or {@code ""} writes to the
     *                  default namespace
     * @return a future that completes with the server's response;
     *         {@code getUpsertedCount()} is 1 when the vector was written
     * @throws PineconeValidationException if {@code id} is null or empty; thrown by this call, not through the future
     */
    @Override
    public ListenableFuture<UpsertResponse> upsert(String id,
                                                   List<Float> values,
                                                   String namespace) {
        return upsert(id, values, null, null, null, namespace);
    }

    /**
     * Writes one vector, with optional sparse values and metadata, to a namespace, replacing any existing vector
     * with the same id.
     *
     * <p>Use this overload when the vector has sparse values or metadata to filter on; pass null for the parts it
     * does not have. A namespace that does not exist yet is created. To write many vectors, use
     * {@link #upsert(List, String)}. A query made right after an upsert may not reflect it yet.
     *
     * <pre>{@code
     * Struct metadata = Struct.newBuilder()
     *         .putFields("category", Value.newBuilder().setStringValue("news").build())
     *         .putFields("year", Value.newBuilder().setNumberValue(2024).build())
     *         .build();
     *
     * UpsertResponse response = asyncIndex.upsert("doc-1", Arrays.asList(0.12f, 0.48f, 0.91f),
     *         null, null, metadata, "articles-en").get();
     * }</pre>
     *
     * @param id the vector id, such as {@code "doc-1"}; must not be null or empty
     * @param values the dense values; the length must match the index dimension. For a sparse index, pass an empty
     *               list. Must not be null.
     * @param sparseIndices the positions of the sparse values as unsigned 32-bit integers, such as
     *                      {@code Arrays.asList(10L, 45L)}; null if the vector has no sparse values
     * @param sparseValues the sparse values, one per entry in {@code sparseIndices}; null if the vector has no sparse
     *                     values
     * @param metadata fields to store with the vector and filter on in queries, such as a category or a year; null
     *                 for none
     * @param namespace the namespace to write to, such as {@code "articles-en"}; null or {@code ""} writes to the
     *                  default namespace
     * @return a future that completes with the server's response;
     *         {@code getUpsertedCount()} is 1 when the vector was written
     * @throws PineconeValidationException if {@code id} is null or empty, only one of {@code sparseIndices} and
     *         {@code sparseValues} is given, they differ in length, or a sparse index is outside the unsigned 32-bit
     *         range;
     *         thrown by this call, not through the future
     */
    @Override
    public ListenableFuture<UpsertResponse> upsert(String id,
                                                   List<Float> values,
                                                   List<Long> sparseIndices,
                                                   List<Float> sparseValues,
                                                   com.google.protobuf.Struct metadata,
                                                   String namespace) {
        UpsertRequest upsertRequest = validateUpsertRequest(id, values, sparseIndices, sparseValues, metadata,
                namespace);

        return asyncStub.upsert(upsertRequest);
    }

    /**
     * Searches a namespace for the vectors most similar to a query given as dense values, sparse values, both, or the
     * id of a stored vector.
     *
     * <p>Use this overload for sparse indexes and for hybrid queries that combine dense and sparse values; for a
     * dense-only query, {@link #queryByVector(int, List, String, Struct, boolean, boolean)} and
     * {@link #queryByVectorId(int, String, String, Struct, boolean, boolean)} are shorter. Give either values or an id,
     * not both. A dense index needs dense values (sparse values alone are rejected) and accepts sparse values only if
     * its metric is dotproduct; a sparse index needs sparse values and rejects dense ones. The server checks this, not
     * the client. A query made right after an upsert may not reflect it yet.
     *
     * <pre>{@code
     * List<Float> queryVector = Arrays.asList(0.12f, 0.48f, 0.91f);
     * Struct filter = Struct.newBuilder()
     *         .putFields("category", Value.newBuilder()
     *                 .setStructValue(Struct.newBuilder()
     *                         .putFields("$eq", Value.newBuilder().setStringValue("news").build()))
     *                 .build())
     *         .build();
     * QueryResponseWithUnsignedIndices response = asyncIndex.query(10, queryVector,
     *         Arrays.asList(10L, 45L), Arrays.asList(0.5f, 0.3f),
     *         null, "articles-en", filter, false, true).get();
     * for (ScoredVectorWithUnsignedIndices match : response.getMatchesList()) {
     *     System.out.println(match.getId() + " " + match.getScore() + " " + match.getMetadata());
     * }
     * }</pre>
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param vector the dense query values, such as {@code Arrays.asList(0.12f, 0.48f, 0.91f)}; their length
     *               must match the index dimension. Null or empty to query by sparse values only or by {@code id}.
     * @param sparseIndices the positions of the sparse query values as unsigned 32-bit integers, such as
     *                      {@code Arrays.asList(10L, 45L)}; null for no sparse values
     * @param sparseValues the sparse query values, one per entry in {@code sparseIndices}; null for no sparse values
     * @param id the id of a stored vector to search from instead of passing values, such as {@code "doc-1"}; null
     *           or empty to search with the values given. If no vector has this id, the response has no matches.
     * @param namespace the namespace to search, such as {@code "articles-en"}; null or {@code ""} searches the
     *                  default namespace
     * @param filter a metadata filter in Pinecone's filter syntax, such as {@code {"category": {"$eq": "news"}}}
     *               built as a {@code Struct}; only vectors whose metadata matches are returned. Null for no
     *               filter.
     * @param includeValues true to return each match's vector values; false to leave them out
     * @param includeMetadata true to return each match's metadata; false to leave it out
     * @return a future that completes with the matches; {@code getMatchesList()} holds up to {@code topK} matches,
     *         each with {@code getId()}, {@code getScore()} and, when requested, {@code getValuesList()} and {@code
     *         getMetadata()}
     * @throws PineconeValidationException if both {@code id} and {@code vector} are given, only one of
     *         {@code sparseIndices} and {@code sparseValues} is given, they differ in length, or a sparse index is
     *         outside the unsigned 32-bit range;
     *         thrown by this call, not through the future
     */
    @Override
    public ListenableFuture<QueryResponseWithUnsignedIndices> query(int topK,
                                                                    List<Float> vector,
                                                                    List<Long> sparseIndices,
                                                                    List<Float> sparseValues,
                                                                    String id,
                                                                    String namespace,
                                                                    Struct filter,
                                                                    boolean includeValues,
                                                                    boolean includeMetadata) {
        QueryRequest queryRequest = validateQueryRequest(topK, vector, sparseIndices, sparseValues, id, namespace,
                filter, includeValues, includeMetadata);

        ListenableFuture<QueryResponse> queryResponseFuture = asyncStub.query(queryRequest);

        return Futures.transform(queryResponseFuture,
                QueryResponseWithUnsignedIndices::new, MoreExecutors.directExecutor());
    }

    /**
     * Searches a namespace for the vectors most similar to a vector already stored in it, keeping only those whose
     * metadata matches a filter.
     *
     * <p>Use this to find items similar to one you already have, such as related articles, without sending its values;
     * the server uses the stored values of the vector with that id. Pass a null filter to search every vector in the
     * namespace. If no vector has the id, the response has no matches rather than an error. To search with values you
     * supply, use {@link #queryByVector(int, List, String, Struct, boolean, boolean)}.
     *
     * <pre>{@code
     * Struct filter = Struct.newBuilder()
     *         .putFields("category", Value.newBuilder()
     *                 .setStructValue(Struct.newBuilder()
     *                         .putFields("$eq", Value.newBuilder().setStringValue("news").build()))
     *                 .build())
     *         .build();
     * QueryResponseWithUnsignedIndices response = asyncIndex.queryByVectorId(10, "doc-1", "articles-en",
     *         filter, false, true).get();
     * for (ScoredVectorWithUnsignedIndices match : response.getMatchesList()) {
     *     System.out.println(match.getId() + " " + match.getScore() + " " + match.getMetadata());
     * }
     * }</pre>
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param id the id of a stored vector to search from, such as {@code "doc-1"}; if no vector has this id,
     *           the response has no matches
     * @param namespace the namespace to search, such as {@code "articles-en"}; null or {@code ""} searches the
     *                  default namespace
     * @param filter a metadata filter in Pinecone's filter syntax, such as {@code {"category": {"$eq": "news"}}}
     *               built as a {@code Struct}; only vectors whose metadata matches are returned. Null for no
     *               filter.
     * @param includeValues true to return each match's vector values; false to leave them out
     * @param includeMetadata true to return each match's metadata; false to leave it out
     * @return a future that completes with the matches; {@code getMatchesList()} holds up to {@code topK} matches,
     *         each with {@code getId()}, {@code getScore()} and, when requested, {@code getValuesList()} and {@code
     *         getMetadata()}
     */
    @Override
    public ListenableFuture<QueryResponseWithUnsignedIndices> queryByVectorId(int topK,
                                                                              String id,
                                                                              String namespace,
                                                                              Struct filter,
                                                                              boolean includeValues,
                                                                              boolean includeMetadata) {
        return query(topK, null, null, null, id, namespace, filter, includeValues, includeMetadata);
    }

    /**
     * Searches a namespace for the vectors most similar to a vector already stored in it, keeping only those whose
     * metadata matches a filter, and returns their ids and scores.
     *
     * <p>If no vector has the id, the response has no matches. To also get each match's metadata or values, use
     * {@link #queryByVectorId(int, String, String, Struct, boolean, boolean)}.
     *
     * <pre>{@code
     * Struct filter = Struct.newBuilder()
     *         .putFields("category", Value.newBuilder()
     *                 .setStructValue(Struct.newBuilder()
     *                         .putFields("$eq", Value.newBuilder().setStringValue("news").build()))
     *                 .build())
     *         .build();
     * QueryResponseWithUnsignedIndices response =
     *         asyncIndex.queryByVectorId(10, "doc-1", "articles-en", filter).get();
     * for (ScoredVectorWithUnsignedIndices match : response.getMatchesList()) {
     *     System.out.println(match.getId() + " " + match.getScore());
     * }
     * }</pre>
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param id the id of a stored vector to search from, such as {@code "doc-1"}; if no vector has this id,
     *           the response has no matches
     * @param namespace the namespace to search, such as {@code "articles-en"}; null or {@code ""} searches the
     *                  default namespace
     * @param filter a metadata filter in Pinecone's filter syntax, such as {@code {"category": {"$eq": "news"}}}
     *               built as a {@code Struct}; only vectors whose metadata matches are returned. Null for no
     *               filter.
     * @return a future that completes with the matches; {@code getMatchesList()} holds up to {@code topK} matches,
     *         each with {@code getId()} and {@code getScore()}
     */
    @Override
    public ListenableFuture<QueryResponseWithUnsignedIndices> queryByVectorId(int topK,
                                                                              String id,
                                                                              String namespace,
                                                                              Struct filter) {
        return query(topK, null, null, null, id, namespace, filter, false, false);
    }

    /**
     * Searches a namespace for the vectors most similar to a vector already stored in it.
     *
     * <p>If no vector has the id, the response has no matches. To filter on metadata, use
     * {@link #queryByVectorId(int, String, String, Struct, boolean, boolean)}.
     *
     * <pre>{@code
     * QueryResponseWithUnsignedIndices response =
     *         asyncIndex.queryByVectorId(10, "doc-1", "articles-en", false, true).get();
     * for (ScoredVectorWithUnsignedIndices match : response.getMatchesList()) {
     *     System.out.println(match.getId() + " " + match.getScore() + " " + match.getMetadata());
     * }
     * }</pre>
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param id the id of a stored vector to search from, such as {@code "doc-1"}; if no vector has this id,
     *           the response has no matches
     * @param namespace the namespace to search, such as {@code "articles-en"}; null or {@code ""} searches the
     *                  default namespace
     * @param includeValues true to return each match's vector values; false to leave them out
     * @param includeMetadata true to return each match's metadata; false to leave it out
     * @return a future that completes with the matches; {@code getMatchesList()} holds up to {@code topK} matches,
     *         each with {@code getId()}, {@code getScore()} and, when requested, {@code getValuesList()} and {@code
     *         getMetadata()}
     */
    @Override
    public ListenableFuture<QueryResponseWithUnsignedIndices> queryByVectorId(int topK,
                                                                              String id,
                                                                              String namespace,
                                                                              boolean includeValues,
                                                                              boolean includeMetadata) {
        return query(topK, null, null, null, id, namespace, null, includeValues, includeMetadata);
    }

    /**
     * Searches a namespace for the vectors most similar to a vector already stored in it and returns their ids and
     * scores.
     *
     * <p>If no vector has the id, the response has no matches. To filter on metadata or get each match's metadata or
     * values, use {@link #queryByVectorId(int, String, String, Struct, boolean, boolean)}.
     *
     * <pre>{@code
     * QueryResponseWithUnsignedIndices response = asyncIndex.queryByVectorId(10, "doc-1", "articles-en").get();
     * for (ScoredVectorWithUnsignedIndices match : response.getMatchesList()) {
     *     System.out.println(match.getId() + " " + match.getScore());
     * }
     * }</pre>
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param id the id of a stored vector to search from, such as {@code "doc-1"}; if no vector has this id,
     *           the response has no matches
     * @param namespace the namespace to search, such as {@code "articles-en"}; null or {@code ""} searches the
     *                  default namespace
     * @return a future that completes with the matches; {@code getMatchesList()} holds up to {@code topK} matches,
     *         each with {@code getId()} and {@code getScore()}
     */
    @Override
    public ListenableFuture<QueryResponseWithUnsignedIndices> queryByVectorId(int topK,
                                                                              String id,
                                                                              String namespace) {
        return query(topK, null, null, null, id, namespace, null, false, false);
    }

    /**
     * Searches the default namespace for the vectors most similar to a vector already stored in it.
     *
     * <p>If no vector has the id, the response has no matches. To search a named namespace or filter on metadata, use
     * {@link #queryByVectorId(int, String, String, Struct, boolean, boolean)}.
     *
     * <pre>{@code
     * QueryResponseWithUnsignedIndices response = asyncIndex.queryByVectorId(10, "doc-1", false, true).get();
     * for (ScoredVectorWithUnsignedIndices match : response.getMatchesList()) {
     *     System.out.println(match.getId() + " " + match.getScore() + " " + match.getMetadata());
     * }
     * }</pre>
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param id the id of a stored vector to search from, such as {@code "doc-1"}; if no vector has this id,
     *           the response has no matches
     * @param includeValues true to return each match's vector values; false to leave them out
     * @param includeMetadata true to return each match's metadata; false to leave it out
     * @return a future that completes with the matches; {@code getMatchesList()} holds up to {@code topK} matches,
     *         each with {@code getId()}, {@code getScore()} and, when requested, {@code getValuesList()} and {@code
     *         getMetadata()}
     */
    @Override
    public ListenableFuture<QueryResponseWithUnsignedIndices> queryByVectorId(int topK,
                                                                              String id,
                                                                              boolean includeValues,
                                                                              boolean includeMetadata) {
        return query(topK, null, null, null, id, null, null, includeValues, includeMetadata);
    }

    /**
     * Searches the default namespace for the vectors most similar to a vector already stored in it and returns their
     * ids and scores.
     *
     * <p>If no vector has the id, the response has no matches. To search a named namespace, filter on metadata, or get
     * each match's metadata, use {@link #queryByVectorId(int, String, String, Struct, boolean, boolean)}.
     *
     * <pre>{@code
     * QueryResponseWithUnsignedIndices response = asyncIndex.queryByVectorId(10, "doc-1").get();
     * for (ScoredVectorWithUnsignedIndices match : response.getMatchesList()) {
     *     System.out.println(match.getId() + " " + match.getScore());
     * }
     * }</pre>
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param id the id of a stored vector to search from, such as {@code "doc-1"}; if no vector has this id,
     *           the response has no matches
     * @return a future that completes with the matches; {@code getMatchesList()} holds up to {@code topK} matches,
     *         each with {@code getId()} and {@code getScore()}
     */
    @Override
    public ListenableFuture<QueryResponseWithUnsignedIndices> queryByVectorId(int topK,
                                                                              String id) {
        return query(topK, null, null, null, id, null, null, false, false);
    }

    /**
     * Searches a namespace for the vectors most similar to a dense query vector, keeping only those whose metadata
     * matches a filter.
     *
     * <p>This is the overload for most dense searches; pass a null filter to search every vector in the namespace. To
     * search from a vector already stored in the index, use
     * {@link #queryByVectorId(int, String, String, Struct, boolean, boolean)}; for sparse or hybrid queries, use
     * {@link #query(int, List, List, List, String, String, Struct, boolean, boolean)}. A query made right after an
     * upsert may not reflect it yet.
     *
     * <pre>{@code
     * List<Float> queryVector = Arrays.asList(0.12f, 0.48f, 0.91f);
     * Struct filter = Struct.newBuilder()
     *         .putFields("category", Value.newBuilder()
     *                 .setStructValue(Struct.newBuilder()
     *                         .putFields("$eq", Value.newBuilder().setStringValue("news").build()))
     *                 .build())
     *         .build();
     * QueryResponseWithUnsignedIndices response = asyncIndex.queryByVector(10, queryVector, "articles-en",
     *         filter, false, true).get();
     * for (ScoredVectorWithUnsignedIndices match : response.getMatchesList()) {
     *     System.out.println(match.getId() + " " + match.getScore() + " " + match.getMetadata());
     * }
     * }</pre>
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param vector the dense query vector, such as {@code Arrays.asList(0.12f, 0.48f, 0.91f)}; the server rejects
     *               a vector whose length does not match the index dimension
     * @param namespace the namespace to search, such as {@code "articles-en"}; null or {@code ""} searches the
     *                  default namespace
     * @param filter a metadata filter in Pinecone's filter syntax, such as {@code {"category": {"$eq": "news"}}}
     *               built as a {@code Struct}; only vectors whose metadata matches are returned. Null for no
     *               filter.
     * @param includeValues true to return each match's vector values; false to leave them out
     * @param includeMetadata true to return each match's metadata; false to leave it out
     * @return a future that completes with the matches; {@code getMatchesList()} holds up to {@code topK} matches,
     *         each with {@code getId()}, {@code getScore()} and, when requested, {@code getValuesList()} and {@code
     *         getMetadata()}
     */
    @Override
    public ListenableFuture<QueryResponseWithUnsignedIndices> queryByVector(int topK,
                                                                            List<Float> vector,
                                                                            String namespace,
                                                                            Struct filter,
                                                                            boolean includeValues,
                                                                            boolean includeMetadata) {
        return query(topK, vector, null, null, null, namespace, filter, includeValues, includeMetadata);
    }

    /**
     * Searches a namespace for the vectors most similar to a dense query vector, keeping only those whose metadata
     * matches a filter, and returns their ids and scores.
     *
     * <p>To also get each match's metadata or values, use
     * {@link #queryByVector(int, List, String, Struct, boolean, boolean)}.
     *
     * <pre>{@code
     * List<Float> queryVector = Arrays.asList(0.12f, 0.48f, 0.91f);
     * Struct filter = Struct.newBuilder()
     *         .putFields("category", Value.newBuilder()
     *                 .setStructValue(Struct.newBuilder()
     *                         .putFields("$eq", Value.newBuilder().setStringValue("news").build()))
     *                 .build())
     *         .build();
     * QueryResponseWithUnsignedIndices response =
     *         asyncIndex.queryByVector(10, queryVector, "articles-en", filter).get();
     * for (ScoredVectorWithUnsignedIndices match : response.getMatchesList()) {
     *     System.out.println(match.getId() + " " + match.getScore());
     * }
     * }</pre>
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param vector the dense query vector, such as {@code Arrays.asList(0.12f, 0.48f, 0.91f)}; the server rejects
     *               a vector whose length does not match the index dimension
     * @param namespace the namespace to search, such as {@code "articles-en"}; null or {@code ""} searches the
     *                  default namespace
     * @param filter a metadata filter in Pinecone's filter syntax, such as {@code {"category": {"$eq": "news"}}}
     *               built as a {@code Struct}; only vectors whose metadata matches are returned. Null for no
     *               filter.
     * @return a future that completes with the matches; {@code getMatchesList()} holds up to {@code topK} matches,
     *         each with {@code getId()} and {@code getScore()}
     */
    @Override
    public ListenableFuture<QueryResponseWithUnsignedIndices> queryByVector(int topK,
                                                                            List<Float> vector,
                                                                            String namespace,
                                                                            Struct filter) {
        return query(topK, vector, null, null, null, namespace, filter, false, false);
    }

    /**
     * Searches a namespace for the vectors most similar to a dense query vector.
     *
     * <p>To keep only matches whose metadata matches a filter, use
     * {@link #queryByVector(int, List, String, Struct, boolean, boolean)}.
     *
     * <pre>{@code
     * List<Float> queryVector = Arrays.asList(0.12f, 0.48f, 0.91f);
     * QueryResponseWithUnsignedIndices response =
     *         asyncIndex.queryByVector(10, queryVector, "articles-en", false, true).get();
     * for (ScoredVectorWithUnsignedIndices match : response.getMatchesList()) {
     *     System.out.println(match.getId() + " " + match.getScore() + " " + match.getMetadata());
     * }
     * }</pre>
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param vector the dense query vector, such as {@code Arrays.asList(0.12f, 0.48f, 0.91f)}; the server rejects
     *               a vector whose length does not match the index dimension
     * @param namespace the namespace to search, such as {@code "articles-en"}; null or {@code ""} searches the
     *                  default namespace
     * @param includeValues true to return each match's vector values; false to leave them out
     * @param includeMetadata true to return each match's metadata; false to leave it out
     * @return a future that completes with the matches; {@code getMatchesList()} holds up to {@code topK} matches,
     *         each with {@code getId()}, {@code getScore()} and, when requested, {@code getValuesList()} and {@code
     *         getMetadata()}
     */
    @Override
    public ListenableFuture<QueryResponseWithUnsignedIndices> queryByVector(int topK,
                                                                            List<Float> vector,
                                                                            String namespace,
                                                                            boolean includeValues,
                                                                            boolean includeMetadata) {
        return query(topK, vector, null, null, null, namespace, null, includeValues, includeMetadata);
    }

    /**
     * Searches a namespace for the vectors most similar to a dense query vector and returns their ids and scores.
     *
     * <p>To filter on metadata or get each match's metadata or values, use
     * {@link #queryByVector(int, List, String, Struct, boolean, boolean)}.
     *
     * <pre>{@code
     * List<Float> queryVector = Arrays.asList(0.12f, 0.48f, 0.91f);
     * QueryResponseWithUnsignedIndices response = asyncIndex.queryByVector(10, queryVector, "articles-en").get();
     * for (ScoredVectorWithUnsignedIndices match : response.getMatchesList()) {
     *     System.out.println(match.getId() + " " + match.getScore());
     * }
     * }</pre>
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param vector the dense query vector, such as {@code Arrays.asList(0.12f, 0.48f, 0.91f)}; the server rejects
     *               a vector whose length does not match the index dimension
     * @param namespace the namespace to search, such as {@code "articles-en"}; null or {@code ""} searches the
     *                  default namespace
     * @return a future that completes with the matches; {@code getMatchesList()} holds up to {@code topK} matches,
     *         each with {@code getId()} and {@code getScore()}
     */
    @Override
    public ListenableFuture<QueryResponseWithUnsignedIndices> queryByVector(int topK,
                                                                            List<Float> vector,
                                                                            String namespace) {
        return query(topK, vector, null, null, null, namespace, null, false, false);
    }

    /**
     * Searches the default namespace for the vectors most similar to a dense query vector.
     *
     * <p>To search a named namespace or filter on metadata, use
     * {@link #queryByVector(int, List, String, Struct, boolean, boolean)}.
     *
     * <pre>{@code
     * List<Float> queryVector = Arrays.asList(0.12f, 0.48f, 0.91f);
     * QueryResponseWithUnsignedIndices response = asyncIndex.queryByVector(10, queryVector, false, true).get();
     * for (ScoredVectorWithUnsignedIndices match : response.getMatchesList()) {
     *     System.out.println(match.getId() + " " + match.getScore() + " " + match.getMetadata());
     * }
     * }</pre>
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param vector the dense query vector, such as {@code Arrays.asList(0.12f, 0.48f, 0.91f)}; the server rejects
     *               a vector whose length does not match the index dimension
     * @param includeValues true to return each match's vector values; false to leave them out
     * @param includeMetadata true to return each match's metadata; false to leave it out
     * @return a future that completes with the matches; {@code getMatchesList()} holds up to {@code topK} matches,
     *         each with {@code getId()}, {@code getScore()} and, when requested, {@code getValuesList()} and {@code
     *         getMetadata()}
     */
    @Override
    public ListenableFuture<QueryResponseWithUnsignedIndices> queryByVector(int topK,
                                                                            List<Float> vector,
                                                                            boolean includeValues,
                                                                            boolean includeMetadata) {
        return query(topK, vector, null, null, null, null, null, includeValues, includeMetadata);
    }

    /**
     * Searches the default namespace for the vectors most similar to a dense query vector and returns their ids and
     * scores.
     *
     * <p>To search a named namespace, filter on metadata, or get each match's metadata, use
     * {@link #queryByVector(int, List, String, Struct, boolean, boolean)}.
     *
     * <pre>{@code
     * List<Float> queryVector = Arrays.asList(0.12f, 0.48f, 0.91f);
     * QueryResponseWithUnsignedIndices response = asyncIndex.queryByVector(10, queryVector).get();
     * for (ScoredVectorWithUnsignedIndices match : response.getMatchesList()) {
     *     System.out.println(match.getId() + " " + match.getScore());
     * }
     * }</pre>
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param vector the dense query vector, such as {@code Arrays.asList(0.12f, 0.48f, 0.91f)}; the server rejects
     *               a vector whose length does not match the index dimension
     * @return a future that completes with the matches; {@code getMatchesList()} holds up to {@code topK} matches,
     *         each with {@code getId()} and {@code getScore()}
     */
    @Override
    public ListenableFuture<QueryResponseWithUnsignedIndices> queryByVector(int topK,
                                                                            List<Float> vector) {
        return query(topK, vector, null, null, null, null, null, false, false);
    }

    /**
     * Fetches vectors by id from the default namespace, with their values and metadata.
     *
     * <p>To fetch from a named namespace, use {@link #fetch(List, String)}. Ids that are not in the namespace are
     * left out of the result rather than reported as an error, so check the returned map for each id you need.
     *
     * <pre>{@code
     * FetchResponse response = asyncIndex.fetch(Arrays.asList("doc-1", "doc-2")).get();
     * Vector vector = response.getVectorsMap().get("doc-1");
     * if (vector != null) {
     *     System.out.println(vector.getValuesList() + " " + vector.getMetadata());
     * }
     * }</pre>
     *
     * @param ids the ids of the vectors to fetch, such as {@code Arrays.asList("doc-1", "doc-2")}; must not be null
     *            or empty, and the server rejects a request with too many ids
     * @return a future that completes with the fetched vectors; {@code getVectorsMap()} maps each id that was
     *         found to its {@code Vector}, which holds {@code getValuesList()}, {@code getSparseValues()} and
     *         {@code getMetadata()}
     * @throws PineconeValidationException if {@code ids} is null or empty; thrown by this call, not through the
     *         future
     */
    @Override
    public ListenableFuture<FetchResponse> fetch(List<String> ids) {
        return fetch(ids, null);
    }

    /**
     * Fetches vectors by id from a namespace, with their values and metadata.
     *
     * <p>Use this when you know the ids; to find vectors by their metadata instead, use
     * {@link #fetchByMetadata(String, Struct)}. Ids that are not in the namespace are left out of the result rather
     * than reported as an error, so check the returned map for each id you need. A fetch made right after an upsert
     * may not reflect it yet.
     *
     * <pre>{@code
     * FetchResponse response = asyncIndex.fetch(Arrays.asList("doc-1", "doc-2"), "articles-en").get();
     * Vector vector = response.getVectorsMap().get("doc-1");
     * if (vector != null) {
     *     System.out.println(vector.getValuesList() + " " + vector.getMetadata());
     * }
     * }</pre>
     *
     * @param ids the ids of the vectors to fetch, such as {@code Arrays.asList("doc-1", "doc-2")}; must not be null
     *            or empty, and the server rejects a request with too many ids
     * @param namespace the namespace to fetch from, such as {@code "articles-en"}; null or {@code ""} means the
     *                  default namespace
     * @return a future that completes with the fetched vectors; {@code getVectorsMap()} maps each id that was
     *         found to its {@code Vector}, which holds {@code getValuesList()}, {@code getSparseValues()} and
     *         {@code getMetadata()}
     * @throws PineconeValidationException if {@code ids} is null or empty; thrown by this call, not through the
     *         future
     */
    @Override
    public ListenableFuture<FetchResponse> fetch(List<String> ids,
                                                 String namespace) {
        FetchRequest fetchRequest = validateFetchRequest(ids, namespace);

        return asyncStub.fetch(fetchRequest);
    }

    /**
     * Fetches the vectors in a namespace whose metadata matches a filter, with their values and metadata.
     *
     * <p>Use this to find vectors when you don't know their ids; to fetch by id, use {@link #fetch(List, String)}.
     * It returns one page of matches; to set the page size or read the next page, use
     * {@link #fetchByMetadata(String, Struct, Integer, String)}. A fetch made right after a write may not reflect it
     * yet.
     *
     * <pre>{@code
     * Struct filter = Struct.newBuilder()
     *         .putFields("category", Value.newBuilder()
     *                 .setStructValue(Struct.newBuilder()
     *                         .putFields("$eq", Value.newBuilder().setStringValue("news").build()))
     *                 .build())
     *         .build();
     *
     * FetchByMetadataResponse response = asyncIndex.fetchByMetadata("articles-en", filter).get();
     * for (Map.Entry<String, Vector> entry : response.getVectorsMap().entrySet()) {
     *     System.out.println(entry.getKey() + " " + entry.getValue().getMetadata());
     * }
     * }</pre>
     *
     * @param namespace the namespace to fetch from, such as {@code "articles-en"}; must not be null or empty, so pass
     *                  {@code "__default__"} for the default namespace
     * @param filter the metadata filter the vectors must match, such as {@code {"category": {"$eq": "news"}}}
     *               built as a {@code Struct}; the server rejects a null or empty filter
     * @return a future that completes with one page of matching vectors: {@code getVectorsMap()} maps each id to its {@code Vector}, and
     *         {@code hasPagination()} is true when more matches follow
     * @throws PineconeValidationException if {@code namespace} is null or empty; thrown by this call, not
     *         through the future
     */
    public ListenableFuture<FetchByMetadataResponse> fetchByMetadata(String namespace, Struct filter) {
        return fetchByMetadata(namespace, filter, null, null);
    }

    /**
     * Fetches one page of the vectors in a namespace whose metadata matches a filter.
     *
     * <p>Each call returns at most {@code limit} vectors. When {@code hasPagination()} is true on the response, pass
     * {@code getPagination().getNext()} as {@code paginationToken}, with the same namespace and filter, to get the
     * next page. A fetch made right after a write may not reflect it yet.
     *
     * <pre>{@code
     * Struct filter = Struct.newBuilder()
     *         .putFields("category", Value.newBuilder()
     *                 .setStructValue(Struct.newBuilder()
     *                         .putFields("$eq", Value.newBuilder().setStringValue("news").build()))
     *                 .build())
     *         .build();
     *
     * FetchByMetadataResponse page = asyncIndex.fetchByMetadata("articles-en", filter, 50, null).get();
     * while (true) {
     *     for (String id : page.getVectorsMap().keySet()) {
     *         System.out.println(id);
     *     }
     *     if (!page.hasPagination()) {
     *         break;
     *     }
     *     page = asyncIndex.fetchByMetadata("articles-en", filter, 50, page.getPagination().getNext()).get();
     * }
     * }</pre>
     *
     * @param namespace the namespace to fetch from, such as {@code "articles-en"}; must not be null or empty, so pass
     *                  {@code "__default__"} for the default namespace
     * @param filter the metadata filter the vectors must match, such as {@code {"category": {"$eq": "news"}}}
     *               built as a {@code Struct}; the server rejects a null or empty filter
     * @param limit the most vectors to return in this page, such as {@code 50}; null, zero or a negative value uses
     *              the server's default, and the server rejects a limit above its maximum
     * @param paginationToken the token for the next page, from {@code getPagination().getNext()} on the previous
     *                        response; null or empty gets the first page
     * @return a future that completes with the page of matching vectors: {@code getVectorsMap()} maps each id to its {@code Vector}, and
     *         {@code getPagination().getNext()} is the token for the next page when {@code hasPagination()} is true
     * @throws PineconeValidationException if {@code namespace} is null or empty; thrown by this call, not
     *         through the future
     */
    public ListenableFuture<FetchByMetadataResponse> fetchByMetadata(String namespace, Struct filter, Integer limit, String paginationToken) {
        FetchByMetadataRequest fetchByMetadataRequest = validateFetchByMetadataRequest(namespace, filter, limit, paginationToken);

        return asyncStub.fetchByMetadata(fetchByMetadataRequest);
    }

    /**
     * Replaces the dense values of one existing vector in the default namespace, keeping its sparse values and
     * metadata.
     *
     * <p>To update a vector in a named namespace, use {@link #update(String, List, String)}; to change
     * metadata or sparse values, use {@link #update(String, List, Struct, String, List, List)}.
     * If no vector has that id, nothing changes; use upsert to create vectors. A query made right after an
     * update may not reflect it yet.
     *
     * <pre>{@code
     * asyncIndex.update("doc-1", Arrays.asList(0.12f, 0.48f, 0.91f)).get();
     * }</pre>
     *
     * @param id the id of the vector to update, such as {@code "doc-1"}; must not be null, and the server rejects
     *           an empty id
     * @param values the new dense values, such as {@code Arrays.asList(0.12f, 0.48f, 0.91f)}; the length must
     *               match the index dimension
     * @return a future that completes when the server accepts the update
     * @throws PineconeValidationException if {@code id} is null; thrown by this call, not through the future
     */
    @Override
    public ListenableFuture<UpdateResponse> update(String id,
                                                   List<Float> values) {
        return update(id, values, null, null, null, null);
    }

    /**
     * Replaces the dense values of one existing vector in a namespace, keeping its sparse values and metadata.
     *
     * <p>To change metadata or sparse values, use {@link #update(String, List, Struct, String, List, List)}.
     * If no vector has that id in the namespace, nothing changes; use upsert to create vectors. A query made
     * right after an update may not reflect it yet.
     *
     * <pre>{@code
     * asyncIndex.update("doc-1", Arrays.asList(0.12f, 0.48f, 0.91f), "articles-en").get();
     * }</pre>
     *
     * @param id the id of the vector to update, such as {@code "doc-1"}; must not be null, and the server rejects
     *           an empty id
     * @param values the new dense values, such as {@code Arrays.asList(0.12f, 0.48f, 0.91f)}; the length must
     *               match the index dimension
     * @param namespace the namespace that holds the vector, such as {@code "articles-en"}; null or
     *                  {@code ""} means the default namespace
     * @return a future that completes when the server accepts the update
     * @throws PineconeValidationException if {@code id} is null; thrown by this call, not through the future
     */
    @Override
    public ListenableFuture<UpdateResponse> update(String id,
                                                   List<Float> values,
                                                   String namespace) {
        return update(id, values, null, namespace, null, null);
    }

    /**
     * Changes the dense values, sparse values or metadata of one existing vector in a namespace, keeping the
     * parts you pass null for.
     *
     * <p>Metadata is merged: each field you pass is set, and the vector's other fields are kept. To replace all of
     * a vector's metadata, upsert the vector again. If no vector has that id in the namespace, nothing changes;
     * use upsert to create vectors. To change every vector that matches a metadata filter, use
     * {@link #updateByMetadata(Struct, Struct, String)}. A query made right after an update may not reflect it
     * yet.
     *
     * <pre>{@code
     * Struct metadata = Struct.newBuilder()
     *         .putFields("category", Value.newBuilder().setStringValue("archived").build())
     *         .build();
     *
     * asyncIndex.update("doc-1", null, metadata, "articles-en", null, null).get();
     * }</pre>
     *
     * @param id the id of the vector to update, such as {@code "doc-1"}; must not be null, and the server rejects
     *           an empty id
     * @param values the new dense values, such as {@code Arrays.asList(0.12f, 0.48f, 0.91f)}; the length must
     *               match the index dimension. Null or an empty list keeps the current values; pass null for a
     *               sparse index.
     * @param metadata the metadata fields to set, such as a new category; null keeps the current metadata
     * @param namespace the namespace that holds the vector, such as {@code "articles-en"}; null or {@code ""}
     *                  means the default namespace
     * @param sparseIndices the positions of the new sparse values as unsigned 32-bit integers, such as
     *                      {@code Arrays.asList(10L, 45L)}; null keeps the current sparse values
     * @param sparseValues the new sparse values, one per entry in {@code sparseIndices}; null keeps the current
     *                     sparse values
     * @return a future that completes when the server accepts the update
     * @throws PineconeValidationException if {@code id} is null, only one of {@code sparseIndices} and
     *         {@code sparseValues} is given, they differ in length, or a sparse index is outside the unsigned
     *         32-bit range;
     *         thrown by this call, not through the future
     */
    @Override
    public ListenableFuture<UpdateResponse> update(String id,
                                                   List<Float> values,
                                                   Struct metadata,
                                                   String namespace,
                                                   List<Long> sparseIndices,
                                                   List<Float> sparseValues) {
        UpdateRequest updateRequest = validateUpdateRequest(id, values, metadata, namespace, sparseIndices,
                sparseValues);

        return asyncStub.update(updateRequest);
    }

    /**
     * Sets metadata fields on every vector in a namespace whose metadata matches a filter.
     *
     * <p>Metadata is merged: each field you pass is set on every matching vector, and their other fields are kept.
     * Vector values can't be changed this way; to change one vector's values, use
     * {@link #update(String, List, Struct, String, List, List)}. To count the matches without changing anything, use
     * {@link #updateByMetadata(Struct, Struct, String, boolean)} with {@code dryRun} set to true. A query made right
     * after an update may not reflect it yet.
     *
     * <pre>{@code
     * Struct filter = Struct.newBuilder()
     *         .putFields("category", Value.newBuilder()
     *                 .setStructValue(Struct.newBuilder()
     *                         .putFields("$eq", Value.newBuilder().setStringValue("news").build()))
     *                 .build())
     *         .build();
     * Struct metadata = Struct.newBuilder()
     *         .putFields("status", Value.newBuilder().setStringValue("archived").build())
     *         .build();
     *
     * UpdateResponse response = asyncIndex.updateByMetadata(filter, metadata, "articles-en").get();
     * System.out.println("Updated " + response.getMatchedRecords() + " vectors");
     * }</pre>
     *
     * @param filter the metadata filter that selects the vectors to update, such as
     *               {@code {"category": {"$eq": "news"}}} built as a {@code Struct}; must not be null, and the server
     *               rejects an empty filter
     * @param metadata the metadata fields to set on each matching vector, such as {@code {"status": "archived"}}
     * @param namespace the namespace that holds the vectors, such as {@code "articles-en"}; null or {@code ""} means
     *                  the default namespace
     * @return a future that completes with the server's response; {@code getMatchedRecords()} is the number
     *         of vectors the filter matched, and {@code hasMatchedRecords()} is false if the server could not
     *         report it
     * @throws PineconeValidationException if {@code filter} is null; thrown by this call, not through the
     *         future
     */
    @Override
    public ListenableFuture<UpdateResponse> updateByMetadata(Struct filter,
                                                            Struct metadata,
                                                            String namespace) {
        return updateByMetadata(filter, metadata, namespace, false);
    }

    /**
     * Sets metadata fields on every vector in a namespace whose metadata matches a filter, or only counts the
     * matches.
     *
     * <p>Run it with {@code dryRun} set to true first to see how many vectors the filter matches, then with false to
     * apply the change. Metadata is merged: each field you pass is set on every matching vector, and their other
     * fields are kept. Vector values can't be changed this way. A query made right after an update may not reflect it
     * yet.
     *
     * <pre>{@code
     * Struct filter = Struct.newBuilder()
     *         .putFields("category", Value.newBuilder()
     *                 .setStructValue(Struct.newBuilder()
     *                         .putFields("$eq", Value.newBuilder().setStringValue("news").build()))
     *                 .build())
     *         .build();
     * Struct metadata = Struct.newBuilder()
     *         .putFields("status", Value.newBuilder().setStringValue("archived").build())
     *         .build();
     *
     * UpdateResponse preview = asyncIndex.updateByMetadata(filter, metadata, "articles-en", true).get();
     * System.out.println("Would update " + preview.getMatchedRecords() + " vectors");
     *
     * asyncIndex.updateByMetadata(filter, metadata, "articles-en", false).get();
     * }</pre>
     *
     * @param filter the metadata filter that selects the vectors to update, such as
     *               {@code {"category": {"$eq": "news"}}} built as a {@code Struct}; must not be null, and the server
     *               rejects an empty filter
     * @param metadata the metadata fields to set on each matching vector, such as {@code {"status": "archived"}}
     * @param namespace the namespace that holds the vectors, such as {@code "articles-en"}; null or {@code ""} means
     *                  the default namespace
     * @param dryRun true to only count the vectors the filter matches, without changing them; false to update them
     * @return a future that completes with the server's response; {@code getMatchedRecords()} is the number
     *         of vectors the filter matched, and {@code hasMatchedRecords()} is false if the server could not
     *         report it
     * @throws PineconeValidationException if {@code filter} is null; thrown by this call, not through the
     *         future
     */
    @Override
    public ListenableFuture<UpdateResponse> updateByMetadata(Struct filter,
                                                            Struct metadata,
                                                            String namespace,
                                                            boolean dryRun) {
        UpdateRequest updateRequest = validateUpdateByMetadataRequest(filter, metadata, namespace, dryRun);

        return asyncStub.update(updateRequest);
    }

    /**
     * Deletes vectors by id from a namespace.
     *
     * <p>Ids that are not in the namespace are ignored rather than reported as an error. To delete vectors by metadata
     * instead, use {@link #deleteByFilter(Struct, String)}. A query made right after a delete may not reflect it yet.
     *
     * <pre>{@code
     * asyncIndex.deleteByIds(Arrays.asList("doc-1", "doc-2"), "articles-en").get();
     * }</pre>
     *
     * @param ids the ids of the vectors to delete, such as {@code Arrays.asList("doc-1", "doc-2")}; the server
     *            rejects a null or empty list and a request with too many ids
     * @param namespace the namespace to delete from, such as {@code "articles-en"}; null or {@code ""} means the
     *                  default namespace
     * @return a future that completes when the server accepts the delete
     */
    @Override
    public ListenableFuture<DeleteResponse> deleteByIds(List<String> ids, String namespace) {
        return delete(ids, false, namespace, null);
    }

    /**
     * Deletes vectors by id from the default namespace.
     *
     * <p>To delete from a named namespace, use {@link #deleteByIds(List, String)}. Ids that are not in the namespace
     * are ignored rather than reported as an error. A query made right after a delete may not reflect it yet.
     *
     * <pre>{@code
     * asyncIndex.deleteByIds(Arrays.asList("doc-1", "doc-2")).get();
     * }</pre>
     *
     * @param ids the ids of the vectors to delete, such as {@code Arrays.asList("doc-1", "doc-2")}; the server
     *            rejects a null or empty list and a request with too many ids
     * @return a future that completes when the server accepts the delete
     */
    @Override
    public ListenableFuture<DeleteResponse> deleteByIds(List<String> ids) {
        return delete(ids, false, null, null);
    }

    /**
     * Deletes every vector in a namespace whose metadata matches a filter.
     *
     * <p>The delete can't be undone, and a filter broader than you meant deletes more than you meant, so try the filter
     * in a query first. To delete specific vectors, use {@link #deleteByIds(List, String)}. A query made right after a
     * delete may not reflect it yet.
     *
     * <pre>{@code
     * Struct filter = Struct.newBuilder()
     *         .putFields("category", Value.newBuilder()
     *                 .setStructValue(Struct.newBuilder()
     *                         .putFields("$eq", Value.newBuilder().setStringValue("archived").build()))
     *                 .build())
     *         .build();
     *
     * asyncIndex.deleteByFilter(filter, "articles-en").get();
     * }</pre>
     *
     * @param filter a metadata filter in Pinecone's filter syntax, such as {@code {"category": {"$eq": "archived"}}}
     *               built as a {@code Struct}; every vector whose metadata matches is deleted. The server rejects
     *               a null or empty filter.
     * @param namespace the namespace to delete from, such as {@code "articles-en"}; null or {@code ""} means the
     *                  default namespace
     * @return a future that completes when the server accepts the delete
     */
    @Override
    public ListenableFuture<DeleteResponse> deleteByFilter(Struct filter, String namespace) {
        return delete(null, false, namespace, filter);
    }

    /**
     * Deletes every vector in the default namespace whose metadata matches a filter.
     *
     * <p>To delete from a named namespace, use {@link #deleteByFilter(Struct, String)}. The delete can't be undone, and
     * a filter broader than you meant deletes more than you meant, so try the filter in a query first. A query made
     * right after a delete may not reflect it yet.
     *
     * <pre>{@code
     * Struct filter = Struct.newBuilder()
     *         .putFields("category", Value.newBuilder()
     *                 .setStructValue(Struct.newBuilder()
     *                         .putFields("$eq", Value.newBuilder().setStringValue("archived").build()))
     *                 .build())
     *         .build();
     *
     * asyncIndex.deleteByFilter(filter).get();
     * }</pre>
     *
     * @param filter a metadata filter in Pinecone's filter syntax, such as {@code {"category": {"$eq": "archived"}}}
     *               built as a {@code Struct}; every vector whose metadata matches is deleted. The server rejects
     *               a null or empty filter.
     * @return a future that completes when the server accepts the delete
     */
    @Override
    public ListenableFuture<DeleteResponse> deleteByFilter(Struct filter) {
        return delete(null, false, null, filter);
    }

    /**
     * Deletes every vector in a namespace.
     *
     * <p>The delete can't be undone. A null or empty namespace deletes every vector in the default namespace, so check
     * the argument before you call this. To delete only some vectors, use {@link #deleteByIds(List, String)} or
     * {@link #deleteByFilter(Struct, String)}. A query made right after a delete may not reflect it yet.
     *
     * <pre>{@code
     * asyncIndex.deleteAll("articles-en").get();
     * }</pre>
     *
     * @param namespace the namespace to empty, such as {@code "articles-en"}; null or {@code ""} means the
     *                  default namespace
     * @return a future that completes when the server accepts the delete
     */
    @Override
    public ListenableFuture<DeleteResponse> deleteAll(String namespace) {
        return delete(null, true, namespace, null);
    }

    /**
     * Deletes vectors by id, by metadata filter, or all vectors in a namespace, depending on the arguments.
     *
     * <p>{@link #deleteByIds(List, String)}, {@link #deleteByFilter(Struct, String)} and {@link #deleteAll(String)} are
     * clearer; use this method when the kind of delete is decided at run time. When {@code deleteAll} is true, pass
     * null for {@code ids} and {@code filter}: the server rejects a request that combines them. When a filter is given,
     * the server deletes by the filter and ignores {@code ids}. A query made right after a delete may not reflect it
     * yet.
     *
     * <pre>{@code
     * asyncIndex.delete(Arrays.asList("doc-1", "doc-2"), false, "articles-en", null).get();
     * }</pre>
     *
     * @param ids the ids of the vectors to delete, such as {@code Arrays.asList("doc-1", "doc-2")}; null or
     *            empty when deleting by filter or deleting all. The server rejects a request with too many ids.
     * @param deleteAll true to delete every vector in the namespace, which can't be undone; false to delete by
     *                  id or filter
     * @param namespace the namespace to delete from, such as {@code "articles-en"}; null or {@code ""} means the
     *                  default namespace
     * @param filter a metadata filter in Pinecone's filter syntax, such as {@code {"category": {"$eq": "archived"}}}
     *               built as a {@code Struct}; every vector whose metadata matches is deleted. Null when deleting
     *               by id or deleting all; the server rejects an empty filter.
     * @return a future that completes when the server accepts the delete
     */
    @Override
    public ListenableFuture<DeleteResponse> delete(List<String> ids,
                                                   boolean deleteAll,
                                                   String namespace,
                                                   Struct filter) {
        DeleteRequest deleteRequest = validateDeleteRequest(ids, deleteAll, namespace, filter);

        return asyncStub.delete(deleteRequest);
    }

    /**
     * Returns the number of vectors in the index and in each of its namespaces, plus the index's dimension.
     *
     * <p>Use it to check how many vectors a load wrote and which namespaces hold them. Counts can lag behind recent
     * writes. On serverless indexes the default namespace appears under the key {@code "__default__"}. The server
     * rejects the call for an index with too many namespaces; use {@link #listNamespaces()} and
     * {@link #describeNamespace(String)} instead.
     *
     * <pre>{@code
     * DescribeIndexStatsResponse stats = asyncIndex.describeIndexStats().get();
     * System.out.println("Total vectors: " + stats.getTotalVectorCount());
     *
     * NamespaceSummary articles = stats.getNamespacesMap().get("articles-en");
     * if (articles != null) {
     *     System.out.println("articles-en: " + articles.getVectorCount());
     * }
     * }</pre>
     *
     * @return a future that completes with the statistics: {@code getTotalVectorCount()},
     *         {@code getNamespacesMap()} (namespace name to a summary whose {@code getVectorCount()} is that
     *         namespace's count) and {@code getDimension()} (0 for a sparse index)
     */
    @Override
    public ListenableFuture<DescribeIndexStatsResponse> describeIndexStats() {
        DescribeIndexStatsRequest describeIndexStatsRequest = validateDescribeIndexStatsRequest(null);

        return asyncStub.describeIndexStats(describeIndexStatsRequest);
    }

    /**
     * Returns index statistics whose per-namespace counts include only vectors whose metadata matches a filter.
     *
     * <p>Only pod-based indexes accept a filter: serverless indexes reject a non-empty filter, so use
     * {@link #describeIndexStats()} there. The filter narrows the per-namespace counts only;
     * {@code getTotalVectorCount()} still counts every vector in the index. Counts can lag behind recent writes.
     *
     * <pre>{@code
     * Struct filter = Struct.newBuilder()
     *         .putFields("category", Value.newBuilder()
     *                 .setStructValue(Struct.newBuilder()
     *                         .putFields("$eq", Value.newBuilder().setStringValue("news").build()))
     *                 .build())
     *         .build();
     *
     * DescribeIndexStatsResponse stats = asyncIndex.describeIndexStats(filter).get();
     * NamespaceSummary articles = stats.getNamespacesMap().get("articles-en");
     * if (articles != null) {
     *     System.out.println("News in articles-en: " + articles.getVectorCount());
     * }
     * }</pre>
     *
     * @param filter a metadata filter in Pinecone's filter syntax, such as {@code {"category": {"$eq": "news"}}}
     *               built as a {@code Struct}; null or an empty filter returns unfiltered statistics
     * @return a future that completes with the statistics: {@code getNamespacesMap()} (namespace name to a
     *         summary whose {@code getVectorCount()} counts the matching vectors), {@code getTotalVectorCount()}
     *         and {@code getDimension()}
     */
    @Override
    public ListenableFuture<DescribeIndexStatsResponse> describeIndexStats(Struct filter) {
        DescribeIndexStatsRequest describeIndexStatsRequest = validateDescribeIndexStatsRequest(filter);

        return asyncStub.describeIndexStats(describeIndexStatsRequest);
    }

    /**
     * Lists the first page of vector ids in the default namespace.
     *
     * <p>Each call returns one page of ids in sorted order. When {@code hasPagination()} is true on the response, pass
     * {@code getPagination().getNext()} to {@link #list(String, int, String)} to get the next page. To list a named
     * namespace, use {@link #list(String)}. A vector upserted just before the call may not be listed yet.
     *
     * <pre>{@code
     * ListResponse page = asyncIndex.list().get();
     * for (ListItem item : page.getVectorsList()) {
     *     System.out.println(item.getId());
     * }
     * }</pre>
     *
     * @return a future that completes with the page of ids: {@code getVectorsList()} holds an item per vector, whose
     *         {@code getId()} is the id, and {@code getPagination().getNext()} is the token for the next page when
     *         {@code hasPagination()} is true
     */
    @Override
    public ListenableFuture<ListResponse> list() {
        validateListEndpointParameters(null, null, null, null, false, false, false, false);
        ListRequest listRequest = ListRequest.newBuilder().build();
        return asyncStub.list(listRequest);
    }

    /**
     * Lists the first page of vector ids in a namespace.
     *
     * <p>Each call returns one page of ids in sorted order. When {@code hasPagination()} is true on the response, pass
     * {@code getPagination().getNext()} to {@link #list(String, int, String)} to get the next page. To list only ids
     * that start with a prefix, use {@link #list(String, String)}; to set the page size, use
     * {@link #list(String, int)}. A vector upserted just before the call may not be listed yet.
     *
     * <pre>{@code
     * ListResponse page = asyncIndex.list("articles-en").get();
     * for (ListItem item : page.getVectorsList()) {
     *     System.out.println(item.getId());
     * }
     * }</pre>
     *
     * @param namespace the namespace to list, such as {@code "articles-en"}; {@code ""} means the default namespace.
     *                  Must not be null. A namespace that does not exist returns an empty page.
     * @return a future that completes with the page of ids: {@code getVectorsList()} holds an item per vector, whose
     *         {@code getId()} is the id, and {@code getPagination().getNext()} is the token for the next page when
     *         {@code hasPagination()} is true
     * @throws PineconeValidationException if {@code namespace} is null; thrown by the call itself, not through the
     *                                     future
     */
    @Override
    public ListenableFuture<ListResponse> list(String namespace) {
        validateListEndpointParameters(namespace, null, null, null, true, false, false, false);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).build();
        return asyncStub.list(listRequest);
    }

    /**
     * Lists the next page of vector ids in a namespace, with a page size.
     *
     * <p>Get the first page with {@link #list(String, int)}, then pass each response's
     * {@code getPagination().getNext()} here until {@code hasPagination()} is false. Ids come back in sorted order. A
     * vector upserted just before the call may not be listed yet.
     *
     * <pre>{@code
     * ListResponse page = asyncIndex.list("articles-en", 50).get();
     * while (true) {
     *     for (ListItem item : page.getVectorsList()) {
     *         System.out.println(item.getId());
     *     }
     *     if (!page.hasPagination()) {
     *         break;
     *     }
     *     page = asyncIndex.list("articles-en", 50, page.getPagination().getNext()).get();
     * }
     * }</pre>
     *
     * @param namespace the namespace to list, such as {@code "articles-en"}; {@code ""} means the default namespace.
     *                  Must not be null. A namespace that does not exist returns an empty page.
     * @param limit the most ids to return in this page, such as {@code 50}; must be positive, and the server rejects a
     *              limit above its maximum
     * @param paginationToken the token for the next page, from {@code getPagination().getNext()} on the previous
     *                        response. Must not be null or empty.
     * @return a future that completes with the page of ids: {@code getVectorsList()} holds an item per vector, whose
     *         {@code getId()} is the id, and {@code getPagination().getNext()} is the token for the next page when
     *         {@code hasPagination()} is true
     * @throws PineconeValidationException if {@code namespace} is null, {@code paginationToken} is null or empty, or
     *                                     {@code limit} is not positive; thrown by the call itself, not through the
     *                                     future
     */
    @Override
    public ListenableFuture<ListResponse> list(String namespace, int limit, String paginationToken) {
        validateListEndpointParameters(namespace, null, paginationToken, limit, true, false, true, true);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setLimit(limit).setPaginationToken(paginationToken).build();
        return asyncStub.list(listRequest);
    }

    /**
     * Lists the first page of vector ids in a namespace, with a page size.
     *
     * <p>Each call returns one page of ids in sorted order. When {@code hasPagination()} is true on the response, pass
     * {@code getPagination().getNext()} to {@link #list(String, int, String)} to get the next page. A vector upserted
     * just before the call may not be listed yet.
     *
     * <pre>{@code
     * ListResponse page = asyncIndex.list("articles-en", 50).get();
     * for (ListItem item : page.getVectorsList()) {
     *     System.out.println(item.getId());
     * }
     * }</pre>
     *
     * @param namespace the namespace to list, such as {@code "articles-en"}; {@code ""} means the default namespace.
     *                  Must not be null. A namespace that does not exist returns an empty page.
     * @param limit the most ids to return in this page, such as {@code 50}; must be positive, and the server rejects a
     *              limit above its maximum
     * @return a future that completes with the page of ids: {@code getVectorsList()} holds an item per vector, whose
     *         {@code getId()} is the id, and {@code getPagination().getNext()} is the token for the next page when
     *         {@code hasPagination()} is true
     * @throws PineconeValidationException if {@code namespace} is null or {@code limit} is not positive; thrown by the
     *                                     call itself, not through the future
     */
    @Override
    public ListenableFuture<ListResponse> list(String namespace, int limit) {
        validateListEndpointParameters(namespace, null, null, limit, true, false, false, true);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setLimit(limit).build();
        return asyncStub.list(listRequest);
    }

    /**
     * Lists the first page of vector ids in a namespace that start with a prefix.
     *
     * <p>Give ids a shared prefix when you upsert them, such as {@code "doc-1#chunk-1"} for the chunks of one document,
     * and this lists them as a group. Each call returns one page of ids in sorted order. When {@code hasPagination()}
     * is true on the response, pass {@code getPagination().getNext()} to {@link #list(String, String, String)} to get
     * the next page. A vector upserted just before the call may not be listed yet.
     *
     * <pre>{@code
     * ListResponse page = asyncIndex.list("articles-en", "doc-1#").get();
     * for (ListItem item : page.getVectorsList()) {
     *     System.out.println(item.getId());
     * }
     * }</pre>
     *
     * @param namespace the namespace to list, such as {@code "articles-en"}; {@code ""} means the default namespace.
     *                  Must not be null. A namespace that does not exist returns an empty page.
     * @param prefix the start of the ids to list, such as {@code "doc-1#"}, so that only ids beginning with it are
     *               returned. Must not be null or empty.
     * @return a future that completes with the page of ids: {@code getVectorsList()} holds an item per vector, whose
     *         {@code getId()} is the id, and {@code getPagination().getNext()} is the token for the next page when
     *         {@code hasPagination()} is true
     * @throws PineconeValidationException if {@code namespace} is null or {@code prefix} is null or empty; thrown by
     *                                     the call itself, not through the future
     */
    @Override
    public ListenableFuture<ListResponse> list(String namespace, String prefix) {
        validateListEndpointParameters(namespace, prefix, null, null, true, true, false, false);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setPrefix(prefix).build();
        return asyncStub.list(listRequest);
    }

    /**
     * Lists the first page of vector ids in a namespace that start with a prefix, with a page size.
     *
     * <p>Each call returns one page of ids in sorted order. When {@code hasPagination()} is true on the response, pass
     * {@code getPagination().getNext()} to {@link #list(String, String, String)} to get the next page. A vector
     * upserted just before the call may not be listed yet.
     *
     * <pre>{@code
     * ListResponse page = asyncIndex.list("articles-en", "doc-1#", 50).get();
     * for (ListItem item : page.getVectorsList()) {
     *     System.out.println(item.getId());
     * }
     * }</pre>
     *
     * @param namespace the namespace to list, such as {@code "articles-en"}; {@code ""} means the default namespace.
     *                  Must not be null. A namespace that does not exist returns an empty page.
     * @param prefix the start of the ids to list, such as {@code "doc-1#"}, so that only ids beginning with it are
     *               returned. Must not be null or empty.
     * @param limit the most ids to return in this page, such as {@code 50}; must be positive, and the server rejects a
     *              limit above its maximum
     * @return a future that completes with the page of ids: {@code getVectorsList()} holds an item per vector, whose
     *         {@code getId()} is the id, and {@code getPagination().getNext()} is the token for the next page when
     *         {@code hasPagination()} is true
     * @throws PineconeValidationException if {@code namespace} is null, {@code prefix} is null or empty, or
     *                                     {@code limit} is not positive; thrown by the call itself, not through the
     *                                     future
     */
    @Override
    public ListenableFuture<ListResponse> list(String namespace, String prefix, int limit) {
        validateListEndpointParameters(namespace, prefix, null, limit, true, true, false, true);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setPrefix(prefix).
                setLimit(limit).build();
        return asyncStub.list(listRequest);
    }

    /**
     * Lists the next page of vector ids in a namespace that start with a prefix.
     *
     * <p>Get the first page with {@link #list(String, String)}, then pass each response's
     * {@code getPagination().getNext()} here until {@code hasPagination()} is false. Ids come back in sorted order. A
     * vector upserted just before the call may not be listed yet.
     *
     * <pre>{@code
     * ListResponse page = asyncIndex.list("articles-en", "doc-1#").get();
     * while (true) {
     *     for (ListItem item : page.getVectorsList()) {
     *         System.out.println(item.getId());
     *     }
     *     if (!page.hasPagination()) {
     *         break;
     *     }
     *     page = asyncIndex.list("articles-en", "doc-1#", page.getPagination().getNext()).get();
     * }
     * }</pre>
     *
     * @param namespace the namespace to list, such as {@code "articles-en"}; {@code ""} means the default namespace.
     *                  Must not be null. A namespace that does not exist returns an empty page.
     * @param prefix the start of the ids to list, such as {@code "doc-1#"}, so that only ids beginning with it are
     *               returned. Must not be null or empty.
     * @param paginationToken the token for the next page, from {@code getPagination().getNext()} on the previous
     *                        response. Must not be null or empty. It carries the prefix of the listing that returned
     *                        it, so pass the same prefix or the server rejects the request.
     * @return a future that completes with the page of ids: {@code getVectorsList()} holds an item per vector, whose
     *         {@code getId()} is the id, and {@code getPagination().getNext()} is the token for the next page when
     *         {@code hasPagination()} is true
     * @throws PineconeValidationException if {@code namespace} is null, {@code prefix} is null or empty, or
     *                                     {@code paginationToken} is null or empty; thrown by the call itself, not
     *                                     through the future
     */
    @Override
    public ListenableFuture<ListResponse> list(String namespace, String prefix, String paginationToken) {
        validateListEndpointParameters(namespace, prefix, paginationToken, null, true, true, true, false);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setPrefix(prefix).
                setPaginationToken(paginationToken).build();
        return asyncStub.list(listRequest);
    }

    /**
     * Lists the next page of vector ids in a namespace that start with a prefix, with a page size.
     *
     * <p>Get the first page with {@link #list(String, String, int)}, then pass each response's
     * {@code getPagination().getNext()} here until {@code hasPagination()} is false. Ids come back in sorted order. A
     * vector upserted just before the call may not be listed yet.
     *
     * <pre>{@code
     * ListResponse page = asyncIndex.list("articles-en", "doc-1#", 50).get();
     * while (true) {
     *     for (ListItem item : page.getVectorsList()) {
     *         System.out.println(item.getId());
     *     }
     *     if (!page.hasPagination()) {
     *         break;
     *     }
     *     page = asyncIndex.list("articles-en", "doc-1#", page.getPagination().getNext(), 50).get();
     * }
     * }</pre>
     *
     * @param namespace the namespace to list, such as {@code "articles-en"}; {@code ""} means the default namespace.
     *                  Must not be null. A namespace that does not exist returns an empty page.
     * @param prefix the start of the ids to list, such as {@code "doc-1#"}, so that only ids beginning with it are
     *               returned. Must not be null or empty.
     * @param paginationToken the token for the next page, from {@code getPagination().getNext()} on the previous
     *                        response. Must not be null or empty. It carries the prefix of the listing that returned
     *                        it, so pass the same prefix or the server rejects the request.
     * @param limit the most ids to return in this page, such as {@code 50}; must be positive, and the server rejects a
     *              limit above its maximum
     * @return a future that completes with the page of ids: {@code getVectorsList()} holds an item per vector, whose
     *         {@code getId()} is the id, and {@code getPagination().getNext()} is the token for the next page when
     *         {@code hasPagination()} is true
     * @throws PineconeValidationException if {@code namespace} is null, {@code prefix} is null or empty,
     *                                     {@code paginationToken} is null or empty, or {@code limit} is not positive;
     *                                     thrown by the call itself, not through the future
     */
    @Override
    public ListenableFuture<ListResponse> list(String namespace, String prefix, String paginationToken, int limit) {
        validateListEndpointParameters(namespace, prefix, paginationToken, limit, true, true, true, true);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setPrefix(prefix).
                setPaginationToken(paginationToken).setLimit(limit).build();
        return asyncStub.list(listRequest);
    }

    /**
     * Lists the first page of namespaces in this index.
     *
     * <p>Namespace operations work on serverless indexes only; the server rejects them on a pod-based index. Records
     * written without a namespace are listed under {@code "__default__"}. Each call returns one page: when
     * {@code hasPagination()} is true on the response, pass {@code getPagination().getNext()} to
     * {@link #listNamespaces(String)} to get the next page.
     *
     * <pre>{@code
     * ListNamespacesResponse page = asyncIndex.listNamespaces().get();
     * for (NamespaceDescription namespace : page.getNamespacesList()) {
     *     System.out.println(namespace.getName() + ": " + namespace.getRecordCount());
     * }
     * }</pre>
     *
     * @return a future that completes with the first page of namespaces; {@code getNamespacesList()} holds each
     *         namespace's name and record count, and {@code getPagination().getNext()} holds the token for the next
     *         page when there is one
     */
    @Override
    public ListenableFuture<ListNamespacesResponse> listNamespaces() {
        ListNamespacesRequest listNamespacesRequest = ListNamespacesRequest
                .newBuilder()
                .build();
        return asyncStub.listNamespaces(listNamespacesRequest);
    }

    /**
     * Lists the next page of namespaces in this index, continuing a previous listing.
     *
     * <p>Pass the token from {@code getPagination().getNext()} of the previous response. A response without pagination
     * is the last page. To get the first page, call {@link #listNamespaces()}.
     *
     * <pre>{@code
     * ListNamespacesResponse page = asyncIndex.listNamespaces().get();
     * List<NamespaceDescription> namespaces = new ArrayList<>(page.getNamespacesList());
     * while (page.hasPagination()) {
     *     page = asyncIndex.listNamespaces(page.getPagination().getNext()).get();
     *     namespaces.addAll(page.getNamespacesList());
     * }
     * }</pre>
     *
     * @param paginationToken the token from the previous page's {@code getPagination().getNext()}; must not be null
     * @return a future that completes with the next page of namespaces, in the same shape as {@link #listNamespaces()}
     */
    @Override
    public ListenableFuture<ListNamespacesResponse> listNamespaces(String paginationToken) {
        ListNamespacesRequest listNamespacesRequest = ListNamespacesRequest
                .newBuilder()
                .setPaginationToken(paginationToken)
                .build();
        return asyncStub.listNamespaces(listNamespacesRequest);
    }

    /**
     * Lists one page of namespaces in this index, with a page size you choose.
     *
     * <p>Use this overload to set the page size. Pagination works as in {@link #listNamespaces(String)}.
     *
     * <pre>{@code
     * ListNamespacesResponse page = asyncIndex.listNamespaces(null, 10).get();
     * }</pre>
     *
     * @param paginationToken the token from the previous page's {@code getPagination().getNext()}, or null for the
     *                        first page
     * @param limit           the maximum number of namespaces to return in this page, for example {@code 10}
     * @return a future that completes with one page of namespaces, in the same shape as {@link #listNamespaces()}
     */
    public ListenableFuture<ListNamespacesResponse> listNamespaces(String paginationToken, int limit) {
        ListNamespacesRequest.Builder listNamespacesRequest = ListNamespacesRequest
                .newBuilder()
                .setLimit(limit);
        if(paginationToken != null) {
            listNamespacesRequest.setPaginationToken(paginationToken);
        }
        return asyncStub.listNamespaces(listNamespacesRequest.build());
    }

    /**
     * Lists one page of the namespaces in this index whose names start with a prefix.
     *
     * <p>Use this overload when you name namespaces by a convention, such as one namespace per language or per tenant.
     * {@code getTotalCount()} on the response reports how many namespaces match across all pages. Pagination works as
     * in {@link #listNamespaces(String)}; pass the same prefix on every page.
     *
     * <pre>{@code
     * ListNamespacesResponse page = asyncIndex.listNamespaces("articles-", null, 50).get();
     * System.out.println(page.getTotalCount() + " namespaces start with articles-");
     * }</pre>
     *
     * @param prefix          the start of the namespace names to list, for example {@code "articles-"}; null or empty
     *                        lists every namespace
     * @param paginationToken the token from the previous page's {@code getPagination().getNext()}, or null for the
     *                        first page
     * @param limit           the maximum number of namespaces to return in this page, for example {@code 50}
     * @return a future that completes with one page of matching namespaces; {@code getTotalCount()} is the number of
     *         matches across all pages
     */
    @Override
    public ListenableFuture<ListNamespacesResponse> listNamespaces(String prefix, String paginationToken, int limit) {
        SurrogateCheck.requireWellFormed("list namespaces", "Prefix", prefix);
        ListNamespacesRequest.Builder listNamespacesRequest = ListNamespacesRequest
                .newBuilder()
                .setLimit(limit);
        if(prefix != null && !prefix.isEmpty()) {
            listNamespacesRequest.setPrefix(prefix);
        }
        if(paginationToken != null) {
            listNamespacesRequest.setPaginationToken(paginationToken);
        }
        return asyncStub.listNamespaces(listNamespacesRequest.build());
    }

    /**
     * Creates an empty namespace in this index.
     *
     * <p>Upserting records into a namespace that does not exist creates it, so you need this method only to create a
     * namespace before writing to it. To choose which metadata fields are indexed for filtering, use
     * {@link #createNamespace(String, MetadataSchema)} instead. Namespace operations work on serverless indexes only;
     * the server rejects them on a pod-based index.
     *
     * <pre>{@code
     * NamespaceDescription namespace = asyncIndex.createNamespace("articles-en").get();
     * }</pre>
     *
     * @param name the name of the namespace to create, for example {@code "articles-en"}; the server rejects an empty
     *             name and {@code "__default__"}
     * @return a future that completes with the new namespace's description, including {@code getName()}
     */
    @Override
    public ListenableFuture<NamespaceDescription> createNamespace(String name) {
        SurrogateCheck.requireWellFormed("create namespace", "Namespace", name);
        CreateNamespaceRequest createNamespaceRequest = CreateNamespaceRequest
                .newBuilder()
                .setName(name)
                .build();
        return asyncStub.createNamespace(createNamespaceRequest);
    }

    /**
     * Creates an empty namespace in this index that indexes only the metadata fields you list.
     *
     * <p>Only fields marked filterable in the schema can be used in metadata filters on records in this namespace.
     * Without a schema, every metadata field is indexed. Namespace operations work on serverless indexes only; the
     * server rejects them on a pod-based index.
     *
     * <pre>{@code
     * MetadataSchema schema = MetadataSchema.newBuilder()
     *         .putFields("genre", MetadataFieldProperties.newBuilder().setFilterable(true).build())
     *         .putFields("year", MetadataFieldProperties.newBuilder().setFilterable(true).build())
     *         .build();
     * NamespaceDescription namespace = asyncIndex.createNamespace("articles-en", schema).get();
     * }</pre>
     *
     * @param name   the name of the namespace to create, for example {@code "articles-en"}; the server rejects an empty
     *               name and {@code "__default__"}
     * @param schema the metadata fields to index for filtering; null creates the namespace without a schema, so every
     *               metadata field is indexed
     * @return a future that completes with the new namespace's description, including {@code getName()} and
     *         {@code getSchema()}
     */
    @Override
    public ListenableFuture<NamespaceDescription> createNamespace(String name, MetadataSchema schema) {
        SurrogateCheck.requireWellFormed("create namespace", "Namespace", name);
        CreateNamespaceRequest.Builder builder = CreateNamespaceRequest
                .newBuilder()
                .setName(name);
        if (schema != null) {
            builder.setSchema(schema);
        }
        return asyncStub.createNamespace(builder.build());
    }

    /**
     * Describes one namespace in this index, including how many records it holds.
     *
     * <p>To describe the default namespace, pass {@code "__default__"}; the server rejects an empty string. Namespace
     * operations work on serverless indexes only; the server rejects them on a pod-based index.
     *
     * <pre>{@code
     * NamespaceDescription namespace = asyncIndex.describeNamespace("articles-en").get();
     * System.out.println(namespace.getRecordCount());
     * }</pre>
     *
     * @param namespace the namespace to describe, for example {@code "articles-en"}; must not be null
     * @return a future that completes with the namespace's description: {@code getName()}, {@code getRecordCount()},
     *         and {@code getSchema()} if the namespace has one
     */
    public ListenableFuture<NamespaceDescription> describeNamespace(String namespace) {
        SurrogateCheck.requireWellFormed("describe namespace", "Namespace", namespace);
        DescribeNamespaceRequest describeNamespaceRequest = DescribeNamespaceRequest
                .newBuilder()
                .setNamespace(namespace)
                .build();
        return asyncStub.describeNamespace(describeNamespaceRequest);
    }

    /**
     * Deletes a namespace and every record in it.
     *
     * <p>This cannot be undone. To delete only some records, use {@code deleteByIds} or {@code deleteByFilter} instead.
     * To delete the default namespace, pass {@code "__default__"}; the server rejects an empty string. Namespace
     * operations work on serverless indexes only; the server rejects them on a pod-based index.
     *
     * <pre>{@code
     * asyncIndex.deleteNamespace("articles-en").get();
     * }</pre>
     *
     * @param namespace the namespace to delete, for example {@code "articles-en"}; must not be null
     * @return a future that completes with an empty {@link DeleteResponse}
     */
    public ListenableFuture<DeleteResponse> deleteNamespace(String namespace) {
        SurrogateCheck.requireWellFormed("delete namespace", "Namespace", namespace);
        DeleteNamespaceRequest deleteNamespaceRequest = DeleteNamespaceRequest
                .newBuilder()
                .setNamespace(namespace)
                .build();
        return asyncStub.deleteNamespace(deleteNamespaceRequest);
    }

    /**
     * Starts a bulk import of vectors from Parquet files in object storage into this index.
     *
     * <p>Use bulk import instead of upsert to load a large dataset. The server starts the import in the background
     * and returns its id right away; poll {@link #describeImport(String)} with that id until the status is
     * {@code "Completed"}, {@code "Failed"} or {@code "Cancelled"}. The server rejects imports into an index that has
     * an integrated embedding model. Unlike the vector operations, this call goes over HTTP, blocks, and returns the
     * result directly rather than a future.
     *
     * <pre>{@code
     * StartImportResponse response =
     *         asyncIndex.startImport("s3://acme-embeddings/articles/", "a12b3c4d-5e6f-7a8b-9c0d", "continue");
     * String importId = response.getId();
     * }</pre>
     *
     * @param uri the object storage prefix that holds the files to import, such as
     *            {@code "s3://acme-embeddings/articles/"}; required
     * @param integrationId the id of the storage integration that grants Pinecone access to a private bucket; null or
     *                      empty for a public location
     * @param errorMode {@code "abort"} to stop the import when it hits an error, or {@code "continue"} to skip what
     *                  fails and keep going; null means {@code "abort"}
     * @return the started import; read its id with {@code getId()}
     * @throws ApiException if the server rejects the request, for example because the URI scheme is not supported,
     *         the storage integration does not exist, or {@code errorMode} is not {@code "abort"} or
     *         {@code "continue"}, or if the request cannot be sent
     */
    public StartImportResponse startImport(String uri, String integrationId, String errorMode) throws ApiException {
        StartImportRequest importRequest = new StartImportRequest();
        importRequest.setUri(uri);
        if(integrationId != null && !integrationId.isEmpty()) {
            importRequest.setIntegrationId(integrationId);
        }
        if(errorMode != null) {
            ImportErrorMode importErrorMode = new ImportErrorMode().onError(errorMode);
            importRequest.setErrorMode(importErrorMode);
        }

        return bulkOperations.startBulkImport(Configuration.VERSION, importRequest);
    }

    /**
     * Lists the first page of up to 100 recent and ongoing imports for this index.
     *
     * <p>The result is one page. If {@code getPagination()} is not null, pass {@code getPagination().getNext()} to
     * {@link #listImports(Integer, String)} to get the next page. Like the other import operations, this call blocks
     * and returns the result directly rather than a future.
     *
     * <pre>{@code
     * ListImportsResponse response = asyncIndex.listImports();
     * for (ImportModel importModel : response.getData()) {
     *     System.out.println(importModel.getId() + " " + importModel.getStatus());
     * }
     * }</pre>
     *
     * @return one page of imports; read them with {@code getData()} and the next-page token with
     *         {@code getPagination()}, which is null on the last page
     * @throws ApiException if the request cannot be sent or the server returns an error
     */
    public ListImportsResponse listImports() throws ApiException {
        return listImports(100, null);
    }

    /**
     * Lists the first page of recent and ongoing imports for this index, with a page size you choose.
     *
     * <p>The result is one page. If {@code getPagination()} is not null, pass {@code getPagination().getNext()} to
     * {@link #listImports(Integer, String)} to get the next page. This call blocks and returns the result directly
     * rather than a future.
     *
     * <pre>{@code
     * ListImportsResponse response = asyncIndex.listImports(10);
     * for (ImportModel importModel : response.getData()) {
     *     System.out.println(importModel.getId() + " " + importModel.getStatus());
     * }
     * }</pre>
     *
     * @param limit the maximum number of imports to return, such as {@code 10}; null means the server's default, and
     *              the server rejects a limit of zero or less or above its maximum
     * @return one page of imports; read them with {@code getData()} and the next-page token with
     *         {@code getPagination()}, which is null on the last page
     * @throws ApiException if the server rejects the limit, or if the request cannot be sent
     */
    public ListImportsResponse listImports(Integer limit) throws ApiException {
        return listImports(limit, null);
    }

    /**
     * Lists one page of recent and ongoing imports for this index, starting from a pagination token.
     *
     * <p>Use this overload to walk every page: pass null as the token for the first page, then the
     * {@code getPagination().getNext()} value from each response until {@code getPagination()} is null. This call
     * blocks and returns the result directly rather than a future.
     *
     * <pre>{@code
     * String paginationToken = null;
     * do {
     *     ListImportsResponse response = asyncIndex.listImports(10, paginationToken);
     *     for (ImportModel importModel : response.getData()) {
     *         System.out.println(importModel.getId() + " " + importModel.getStatus());
     *     }
     *     paginationToken = response.getPagination() == null ? null : response.getPagination().getNext();
     * } while (paginationToken != null);
     * }</pre>
     *
     * @param limit the maximum number of imports to return, such as {@code 10}; null means the server's default, and
     *              the server rejects a limit of zero or less or above its maximum
     * @param paginationToken the {@code getPagination().getNext()} value from the previous page; null or empty for the
     *                        first page
     * @return one page of imports; read them with {@code getData()} and the next-page token with
     *         {@code getPagination()}, which is null on the last page
     * @throws ApiException if the server rejects the limit or the token, or if the request cannot be sent
     */
    public ListImportsResponse listImports(Integer limit, String paginationToken) throws ApiException {
        return bulkOperations.listBulkImports(Configuration.VERSION, limit, paginationToken);
    }

    /**
     * Gets the status and progress of one import.
     *
     * <p>Call it with the id that {@link #startImport(String, String, String)} returned to check whether an import
     * has finished. This call blocks and returns the result directly rather than a future.
     *
     * <pre>{@code
     * ImportModel importModel = asyncIndex.describeImport("101");
     * System.out.println(importModel.getStatus() + " " + importModel.getPercentComplete() + "%");
     * if ("Failed".equals(importModel.getStatus())) {
     *     System.out.println(importModel.getError());
     * }
     * }</pre>
     *
     * @param id the import id, as returned by {@code startImport}; must not be null
     * @return the import; read {@code getStatus()} ({@code "Pending"}, {@code "InProgress"}, {@code "Completed"},
     *         {@code "Failed"} or {@code "Cancelled"}), {@code getPercentComplete()}, {@code getRecordsImported()},
     *         and {@code getError()} when it failed
     * @throws ApiException if {@code id} is null, if no import with that id exists for this index, or if the request
     *         cannot be sent
     */
    public ImportModel describeImport(String id) throws ApiException {
        return bulkOperations.describeBulkImport(Configuration.VERSION, id);
    }

    /**
     * Cancels an import that has not finished yet.
     *
     * <p>The server returns an error if the import has already completed or failed. This call blocks rather than
     * returning a future.
     *
     * <pre>{@code
     * asyncIndex.cancelImport("101");
     * }</pre>
     *
     * @param id the import id, as returned by {@code startImport}; must not be null
     * @throws ApiException if {@code id} is null, if no import with that id exists for this index, if the import has
     *         already completed or failed, or if the request cannot be sent
     */
    public void cancelImport(String id) throws ApiException {
        bulkOperations.cancelBulkImport(Configuration.VERSION, id);
    }

    /**
     * Releases this client's use of the gRPC channel to the index, shutting the channel down and freeing its
     * threads and network connections once no other client uses it. Calls already issued are allowed to finish: when
     * the channel shuts down, this method waits up to 5 seconds for them and then cancels any still running.
     *
     * <p>Every {@link Index} and {@link AsyncIndex} that one {@link Pinecone} client (or clients with identical
     * settings) opens for the same index shares one channel, which shuts down only when the last of them is closed.
     * Calling this method more than once has no further effect. Getting a new connection from {@link Pinecone} after
     * the channel shuts down opens a fresh one.
     *
     * <pre>{@code
     * AsyncIndex asyncIndex = pinecone.getAsyncIndexConnection("product-catalog");
     * try {
     *     asyncIndex.upsert("doc-1", Arrays.asList(0.12f, 0.48f, 0.91f), "articles-en").get();
     * } finally {
     *     asyncIndex.close();
     * }
     * }</pre>
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            Pinecone.releaseConnection(connection);
        }
    }
}
