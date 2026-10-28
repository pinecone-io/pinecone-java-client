package io.pinecone.clients;

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
import org.openapitools.db_data.client.api.VectorOperationsApi;
import org.openapitools.db_data.client.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.pinecone.clients.Pinecone.buildOkHttpClient;

/**
 * A blocking client for reading and writing the records in one Pinecone index.
 *
 * <p>Get one from {@link Pinecone#getIndexConnection(String)}, which looks up the index host for you. Vector
 * operations (upsert, query, fetch, update, delete, list, stats and namespaces) go over gRPC; the integrated
 * inference operations ({@code upsertRecords} and the {@code searchRecords} methods) go over HTTP. Each call blocks
 * until the server responds; {@link AsyncIndex} has the same vector operations but returns a
 * {@code ListenableFuture}, and is where bulk import lives. An {@code Index} is safe to share across threads. Every
 * {@code Index} and {@code AsyncIndex} that one {@link Pinecone} client opens for the same index shares one gRPC
 * channel, so get one, reuse it, and call {@link #close()} when you are done with it.
 *
 * <pre>{@code
 * Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
 * Index index = pinecone.getIndexConnection("product-catalog");
 *
 * QueryResponseWithUnsignedIndices response =
 *         index.queryByVector(3, Arrays.asList(0.12f, 0.48f, 0.91f), "articles-en");
 * for (ScoredVectorWithUnsignedIndices match : response.getMatchesList()) {
 *     System.out.println(match.getId() + " " + match.getScore());
 * }
 * }</pre>
 */
public class Index implements IndexInterface<UpsertResponse,
        QueryResponseWithUnsignedIndices,
        FetchResponse,
        UpdateResponse,
        DeleteResponse,
        DescribeIndexStatsResponse,
        ListResponse,
        ListNamespacesResponse,
        NamespaceDescription> {

    private final PineconeConnection connection;
    private final AtomicBoolean closed = new AtomicBoolean();
    private final VectorServiceGrpc.VectorServiceBlockingStub blockingStub;
    private final VectorOperationsApi vectorOperations;

    /**
     * Creates an {@code Index} from an existing connection.
     *
     * <p>Most code calls {@link Pinecone#getIndexConnection(String)} instead, which resolves the index host and
     * reuses the shared connection for you. The constructor does not look anything up: the HTTP operations use the
     * host and TLS setting in {@code config}, and the gRPC operations use {@code connection}.
     *
     * @param config the API key, index host and TLS setting for the HTTP operations; the host is the index host
     *               (for example, from {@link Pinecone#describeIndex(String)}), not the control-plane host
     * @param connection the gRPC connection to the index; must not be null
     * @param indexName the name of the index, such as {@code "product-catalog"}
     * @throws PineconeValidationException if {@code connection} is null
     */
    public Index(PineconeConfig config, PineconeConnection connection, String indexName) {
        if (connection == null) {
            throw new PineconeValidationException("Pinecone connection object cannot be null.");
        }

        this.connection = connection;
        this.blockingStub = connection.getBlockingStub();

        OkHttpClient customOkHttpClient = config.getCustomOkHttpClient();
        ApiClient apiClient = (customOkHttpClient != null) ? new ApiClient(customOkHttpClient) : new ApiClient(buildOkHttpClient(config.getProxyConfig()));
        apiClient.setApiKey(config.getApiKey());
        apiClient.setUserAgent(config.getUserAgent());
        apiClient.addDefaultHeader("X-Pinecone-Api-Version", Configuration.VERSION);

        this.vectorOperations = new VectorOperationsApi(apiClient);
        String protocol = config.isTLSEnabled() ? "https://" : "http://";
        vectorOperations.setCustomBaseUrl(protocol + config.getHost());
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
     * UpsertResponse response = index.upsert(vectors, "articles-en");
     * System.out.println(response.getUpsertedCount());
     * }</pre>
     *
     * @param vectorList the vectors to write, for example built with
     *                   {@link IndexInterface#buildUpsertVectorWithUnsignedIndices}; each needs an id and dense values
     *                   whose length matches the index dimension (an empty list, not null, for a sparse index)
     * @param namespace the namespace to write to, such as {@code "articles-en"}; {@code ""} writes to the default
     *                  namespace. Must not be null.
     * @return the server's response; {@code getUpsertedCount()} is the number of vectors written
     * @throws PineconeValidationException if a vector has no id, has sparse indices without sparse values or the
     *         reverse, has sparse lists of different lengths, or has a sparse index outside the unsigned 32-bit range
     */
    @Override
    public UpsertResponse upsert(List<VectorWithUnsignedIndices> vectorList,
                                 String namespace) {
        UpsertRequest upsertRequest = validateUpsertRequest(vectorList, namespace);
        return blockingStub.upsert(upsertRequest);
    }

    /**
     * Writes one dense vector to the default namespace, replacing any existing vector with the same id.
     *
     * <p>To write to a named namespace, use {@link #upsert(String, List, String)}; to write many vectors, use
     * {@link #upsert(List, String)}. A query made right after an upsert may not reflect it yet.
     *
     * <pre>{@code
     * UpsertResponse response = index.upsert("doc-1", Arrays.asList(0.12f, 0.48f, 0.91f));
     * }</pre>
     *
     * @param id the vector id, such as {@code "doc-1"}; must not be null or empty
     * @param values the dense values; the length must match the index dimension. Must not be null.
     * @return the server's response; {@code getUpsertedCount()} is 1 when the vector was written
     * @throws PineconeValidationException if {@code id} is null or empty
     */
    @Override
    public UpsertResponse upsert(String id,
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
     * UpsertResponse response = index.upsert("doc-1", Arrays.asList(0.12f, 0.48f, 0.91f), "articles-en");
     * }</pre>
     *
     * @param id the vector id, such as {@code "doc-1"}; must not be null or empty
     * @param values the dense values; the length must match the index dimension. Must not be null.
     * @param namespace the namespace to write to, such as {@code "articles-en"}; null or {@code ""} writes to the
     *                  default namespace
     * @return the server's response; {@code getUpsertedCount()} is 1 when the vector was written
     * @throws PineconeValidationException if {@code id} is null or empty
     */
    @Override
    public UpsertResponse upsert(String id,
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
     * UpsertResponse response = index.upsert("doc-1", Arrays.asList(0.12f, 0.48f, 0.91f),
     *         null, null, metadata, "articles-en");
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
     * @return the server's response; {@code getUpsertedCount()} is 1 when the vector was written
     * @throws PineconeValidationException if {@code id} is null or empty, only one of {@code sparseIndices} and
     *         {@code sparseValues} is given, they differ in length, or a sparse index is outside the unsigned 32-bit
     *         range
     */
    @Override
    public UpsertResponse upsert(String id,
                                 List<Float> values,
                                 List<Long> sparseIndices,
                                 List<Float> sparseValues,
                                 com.google.protobuf.Struct metadata,
                                 String namespace) {
        UpsertRequest upsertRequest = validateUpsertRequest(id, values, sparseIndices, sparseValues, metadata,
                namespace);

        return blockingStub.upsert(upsertRequest);
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
     * QueryResponseWithUnsignedIndices response = index.query(10, queryVector,
     *         Arrays.asList(10L, 45L), Arrays.asList(0.5f, 0.3f),
     *         null, "articles-en", filter, false, true);
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
     * @return the matches; {@code getMatchesList()} holds up to {@code topK} matches, each with {@code getId()},
     *         {@code getScore()} and, when requested, {@code getValuesList()} and {@code getMetadata()}
     * @throws PineconeValidationException if both {@code id} and {@code vector} are given, only one of
     *         {@code sparseIndices} and {@code sparseValues} is given, they differ in length, or a sparse index is
     *         outside the unsigned 32-bit range
     */
    @Override
    public QueryResponseWithUnsignedIndices query(int topK,
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

        return new QueryResponseWithUnsignedIndices(blockingStub.query(queryRequest));
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
     * QueryResponseWithUnsignedIndices response = index.queryByVectorId(10, "doc-1", "articles-en",
     *         filter, false, true);
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
     * @return the matches; {@code getMatchesList()} holds up to {@code topK} matches, each with {@code getId()},
     *         {@code getScore()} and, when requested, {@code getValuesList()} and {@code getMetadata()}
     */
    @Override
    public QueryResponseWithUnsignedIndices queryByVectorId(int topK,
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
     * QueryResponseWithUnsignedIndices response = index.queryByVectorId(10, "doc-1", "articles-en", filter);
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
     * @return the matches; {@code getMatchesList()} holds up to {@code topK} matches, each with {@code getId()}
     *         and {@code getScore()}
     */
    @Override
    public QueryResponseWithUnsignedIndices queryByVectorId(int topK,
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
     * QueryResponseWithUnsignedIndices response = index.queryByVectorId(10, "doc-1", "articles-en", false, true);
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
     * @return the matches; {@code getMatchesList()} holds up to {@code topK} matches, each with {@code getId()},
     *         {@code getScore()} and, when requested, {@code getValuesList()} and {@code getMetadata()}
     */
    @Override
    public QueryResponseWithUnsignedIndices queryByVectorId(int topK,
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
     * QueryResponseWithUnsignedIndices response = index.queryByVectorId(10, "doc-1", "articles-en");
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
     * @return the matches; {@code getMatchesList()} holds up to {@code topK} matches, each with {@code getId()}
     *         and {@code getScore()}
     */
    @Override
    public QueryResponseWithUnsignedIndices queryByVectorId(int topK,
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
     * QueryResponseWithUnsignedIndices response = index.queryByVectorId(10, "doc-1", false, true);
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
     * @return the matches; {@code getMatchesList()} holds up to {@code topK} matches, each with {@code getId()},
     *         {@code getScore()} and, when requested, {@code getValuesList()} and {@code getMetadata()}
     */
    @Override
    public QueryResponseWithUnsignedIndices queryByVectorId(int topK,
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
     * QueryResponseWithUnsignedIndices response = index.queryByVectorId(10, "doc-1");
     * for (ScoredVectorWithUnsignedIndices match : response.getMatchesList()) {
     *     System.out.println(match.getId() + " " + match.getScore());
     * }
     * }</pre>
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param id the id of a stored vector to search from, such as {@code "doc-1"}; if no vector has this id,
     *           the response has no matches
     * @return the matches; {@code getMatchesList()} holds up to {@code topK} matches, each with {@code getId()}
     *         and {@code getScore()}
     */
    @Override
    public QueryResponseWithUnsignedIndices queryByVectorId(int topK,
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
     * QueryResponseWithUnsignedIndices response = index.queryByVector(10, queryVector, "articles-en",
     *         filter, false, true);
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
     * @return the matches; {@code getMatchesList()} holds up to {@code topK} matches, each with {@code getId()},
     *         {@code getScore()} and, when requested, {@code getValuesList()} and {@code getMetadata()}
     */
    @Override
    public QueryResponseWithUnsignedIndices queryByVector(int topK,
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
     * QueryResponseWithUnsignedIndices response = index.queryByVector(10, queryVector, "articles-en", filter);
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
     * @return the matches; {@code getMatchesList()} holds up to {@code topK} matches, each with {@code getId()}
     *         and {@code getScore()}
     */
    @Override
    public QueryResponseWithUnsignedIndices queryByVector(int topK,
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
     * QueryResponseWithUnsignedIndices response = index.queryByVector(10, queryVector, "articles-en", false, true);
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
     * @return the matches; {@code getMatchesList()} holds up to {@code topK} matches, each with {@code getId()},
     *         {@code getScore()} and, when requested, {@code getValuesList()} and {@code getMetadata()}
     */
    @Override
    public QueryResponseWithUnsignedIndices queryByVector(int topK,
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
     * QueryResponseWithUnsignedIndices response = index.queryByVector(10, queryVector, "articles-en");
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
     * @return the matches; {@code getMatchesList()} holds up to {@code topK} matches, each with {@code getId()}
     *         and {@code getScore()}
     */
    @Override
    public QueryResponseWithUnsignedIndices queryByVector(int topK,
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
     * QueryResponseWithUnsignedIndices response = index.queryByVector(10, queryVector, false, true);
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
     * @return the matches; {@code getMatchesList()} holds up to {@code topK} matches, each with {@code getId()},
     *         {@code getScore()} and, when requested, {@code getValuesList()} and {@code getMetadata()}
     */
    @Override
    public QueryResponseWithUnsignedIndices queryByVector(int topK,
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
     * QueryResponseWithUnsignedIndices response = index.queryByVector(10, queryVector);
     * for (ScoredVectorWithUnsignedIndices match : response.getMatchesList()) {
     *     System.out.println(match.getId() + " " + match.getScore());
     * }
     * }</pre>
     *
     * @param topK the number of matches to return, such as {@code 10}; the server rejects values below 1 or
     *             above its limit
     * @param vector the dense query vector, such as {@code Arrays.asList(0.12f, 0.48f, 0.91f)}; the server rejects
     *               a vector whose length does not match the index dimension
     * @return the matches; {@code getMatchesList()} holds up to {@code topK} matches, each with {@code getId()}
     *         and {@code getScore()}
     */
    @Override
    public QueryResponseWithUnsignedIndices queryByVector(int topK,
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
     * FetchResponse response = index.fetch(Arrays.asList("doc-1", "doc-2"));
     * Vector vector = response.getVectorsMap().get("doc-1");
     * if (vector != null) {
     *     System.out.println(vector.getValuesList() + " " + vector.getMetadata());
     * }
     * }</pre>
     *
     * @param ids the ids of the vectors to fetch, such as {@code Arrays.asList("doc-1", "doc-2")}; must not be null
     *            or empty, and the server rejects a request with too many ids
     * @return the fetched vectors; {@code getVectorsMap()} maps each id that was found to its {@code Vector}, which
     *         holds {@code getValuesList()}, {@code getSparseValues()} and {@code getMetadata()}
     * @throws PineconeValidationException if {@code ids} is null or empty
     */
    @Override
    public FetchResponse fetch(List<String> ids) {
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
     * FetchResponse response = index.fetch(Arrays.asList("doc-1", "doc-2"), "articles-en");
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
     * @return the fetched vectors; {@code getVectorsMap()} maps each id that was found to its {@code Vector}, which
     *         holds {@code getValuesList()}, {@code getSparseValues()} and {@code getMetadata()}
     * @throws PineconeValidationException if {@code ids} is null or empty
     */
    @Override
    public FetchResponse fetch(List<String> ids,
                               String namespace) {
        FetchRequest fetchRequest = validateFetchRequest(ids, namespace);

        return blockingStub.fetch(fetchRequest);
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
     * FetchByMetadataResponse response = index.fetchByMetadata("articles-en", filter);
     * for (Map.Entry<String, Vector> entry : response.getVectorsMap().entrySet()) {
     *     System.out.println(entry.getKey() + " " + entry.getValue().getMetadata());
     * }
     * }</pre>
     *
     * @param namespace the namespace to fetch from, such as {@code "articles-en"}; must not be null or empty, so pass
     *                  {@code "__default__"} for the default namespace
     * @param filter the metadata filter the vectors must match, such as {@code {"category": {"$eq": "news"}}}
     *               built as a {@code Struct}; the server rejects a null or empty filter
     * @return one page of matching vectors: {@code getVectorsMap()} maps each id to its {@code Vector}, and
     *         {@code hasPagination()} is true when more matches follow
     * @throws PineconeValidationException if {@code namespace} is null or empty
     */
    public FetchByMetadataResponse fetchByMetadata(String namespace, Struct filter) {
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
     * FetchByMetadataResponse page = index.fetchByMetadata("articles-en", filter, 50, null);
     * while (true) {
     *     for (String id : page.getVectorsMap().keySet()) {
     *         System.out.println(id);
     *     }
     *     if (!page.hasPagination()) {
     *         break;
     *     }
     *     page = index.fetchByMetadata("articles-en", filter, 50, page.getPagination().getNext());
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
     * @return the page of matching vectors: {@code getVectorsMap()} maps each id to its {@code Vector}, and
     *         {@code getPagination().getNext()} is the token for the next page when {@code hasPagination()} is true
     * @throws PineconeValidationException if {@code namespace} is null or empty
     */
    public FetchByMetadataResponse fetchByMetadata(String namespace, Struct filter, Integer limit, String paginationToken) {
        FetchByMetadataRequest fetchByMetadataRequest = validateFetchByMetadataRequest(namespace, filter, limit, paginationToken);

        return blockingStub.fetchByMetadata(fetchByMetadataRequest);
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
     * index.update("doc-1", Arrays.asList(0.12f, 0.48f, 0.91f));
     * }</pre>
     *
     * @param id the id of the vector to update, such as {@code "doc-1"}; must not be null, and the server rejects
     *           an empty id
     * @param values the new dense values, such as {@code Arrays.asList(0.12f, 0.48f, 0.91f)}; the length must
     *               match the index dimension
     * @return the server's response; a normal return means the server accepted the update
     * @throws PineconeValidationException if {@code id} is null
     */
    @Override
    public UpdateResponse update(String id,
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
     * index.update("doc-1", Arrays.asList(0.12f, 0.48f, 0.91f), "articles-en");
     * }</pre>
     *
     * @param id the id of the vector to update, such as {@code "doc-1"}; must not be null, and the server rejects
     *           an empty id
     * @param values the new dense values, such as {@code Arrays.asList(0.12f, 0.48f, 0.91f)}; the length must
     *               match the index dimension
     * @param namespace the namespace that holds the vector, such as {@code "articles-en"}; null or
     *                  {@code ""} means the default namespace
     * @return the server's response; a normal return means the server accepted the update
     * @throws PineconeValidationException if {@code id} is null
     */
    @Override
    public UpdateResponse update(String id,
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
     * index.update("doc-1", null, metadata, "articles-en", null, null);
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
     * @return the server's response; a normal return means the server accepted the update
     * @throws PineconeValidationException if {@code id} is null, only one of {@code sparseIndices} and
     *         {@code sparseValues} is given, they differ in length, or a sparse index is outside the unsigned
     *         32-bit range
     */
    @Override
    public UpdateResponse update(String id,
                                 List<Float> values,
                                 Struct metadata,
                                 String namespace,
                                 List<Long> sparseIndices,
                                 List<Float> sparseValues) {
        UpdateRequest updateRequest = validateUpdateRequest(id, values, metadata, namespace, sparseIndices,
                sparseValues);

        return blockingStub.update(updateRequest);
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
     * UpdateResponse response = index.updateByMetadata(filter, metadata, "articles-en");
     * System.out.println("Updated " + response.getMatchedRecords() + " vectors");
     * }</pre>
     *
     * @param filter the metadata filter that selects the vectors to update, such as
     *               {@code {"category": {"$eq": "news"}}} built as a {@code Struct}; must not be null, and the server
     *               rejects an empty filter
     * @param metadata the metadata fields to set on each matching vector, such as {@code {"status": "archived"}}
     * @param namespace the namespace that holds the vectors, such as {@code "articles-en"}; null or {@code ""} means
     *                  the default namespace
     * @return the server's response; {@code getMatchedRecords()} is the number of vectors the filter matched, and
     *         {@code hasMatchedRecords()} is false if the server could not report it
     * @throws PineconeValidationException if {@code filter} is null
     */
    @Override
    public UpdateResponse updateByMetadata(Struct filter,
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
     * UpdateResponse preview = index.updateByMetadata(filter, metadata, "articles-en", true);
     * System.out.println("Would update " + preview.getMatchedRecords() + " vectors");
     *
     * index.updateByMetadata(filter, metadata, "articles-en", false);
     * }</pre>
     *
     * @param filter the metadata filter that selects the vectors to update, such as
     *               {@code {"category": {"$eq": "news"}}} built as a {@code Struct}; must not be null, and the server
     *               rejects an empty filter
     * @param metadata the metadata fields to set on each matching vector, such as {@code {"status": "archived"}}
     * @param namespace the namespace that holds the vectors, such as {@code "articles-en"}; null or {@code ""} means
     *                  the default namespace
     * @param dryRun true to only count the vectors the filter matches, without changing them; false to update them
     * @return the server's response; {@code getMatchedRecords()} is the number of vectors the filter matched, and
     *         {@code hasMatchedRecords()} is false if the server could not report it
     * @throws PineconeValidationException if {@code filter} is null
     */
    @Override
    public UpdateResponse updateByMetadata(Struct filter,
                                          Struct metadata,
                                          String namespace,
                                          boolean dryRun) {
        UpdateRequest updateRequest = validateUpdateByMetadataRequest(filter, metadata, namespace, dryRun);

        return blockingStub.update(updateRequest);
    }

    /**
     * Deletes vectors by id from a namespace.
     *
     * <p>Ids that are not in the namespace are ignored rather than reported as an error. To delete vectors by metadata
     * instead, use {@link #deleteByFilter(Struct, String)}. A query made right after a delete may not reflect it yet.
     *
     * <pre>{@code
     * index.deleteByIds(Arrays.asList("doc-1", "doc-2"), "articles-en");
     * }</pre>
     *
     * @param ids the ids of the vectors to delete, such as {@code Arrays.asList("doc-1", "doc-2")}; the server
     *            rejects a null or empty list and a request with too many ids
     * @param namespace the namespace to delete from, such as {@code "articles-en"}; null or {@code ""} means the
     *                  default namespace
     * @return the server's response, which has no fields; a normal return means the server accepted the delete
     */
    @Override
    public DeleteResponse deleteByIds(List<String> ids, String namespace) {
        return delete(ids, false, namespace, null);
    }

    /**
     * Deletes vectors by id from the default namespace.
     *
     * <p>To delete from a named namespace, use {@link #deleteByIds(List, String)}. Ids that are not in the namespace
     * are ignored rather than reported as an error. A query made right after a delete may not reflect it yet.
     *
     * <pre>{@code
     * index.deleteByIds(Arrays.asList("doc-1", "doc-2"));
     * }</pre>
     *
     * @param ids the ids of the vectors to delete, such as {@code Arrays.asList("doc-1", "doc-2")}; the server
     *            rejects a null or empty list and a request with too many ids
     * @return the server's response, which has no fields; a normal return means the server accepted the delete
     */
    @Override
    public DeleteResponse deleteByIds(List<String> ids) {
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
     * index.deleteByFilter(filter, "articles-en");
     * }</pre>
     *
     * @param filter a metadata filter in Pinecone's filter syntax, such as {@code {"category": {"$eq": "archived"}}}
     *               built as a {@code Struct}; every vector whose metadata matches is deleted. The server rejects
     *               a null or empty filter.
     * @param namespace the namespace to delete from, such as {@code "articles-en"}; null or {@code ""} means the
     *                  default namespace
     * @return the server's response, which has no fields; a normal return means the server accepted the delete
     */
    @Override
    public DeleteResponse deleteByFilter(Struct filter, String namespace) {
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
     * index.deleteByFilter(filter);
     * }</pre>
     *
     * @param filter a metadata filter in Pinecone's filter syntax, such as {@code {"category": {"$eq": "archived"}}}
     *               built as a {@code Struct}; every vector whose metadata matches is deleted. The server rejects
     *               a null or empty filter.
     * @return the server's response, which has no fields; a normal return means the server accepted the delete
     */
    @Override
    public DeleteResponse deleteByFilter(Struct filter) {
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
     * index.deleteAll("articles-en");
     * }</pre>
     *
     * @param namespace the namespace to empty, such as {@code "articles-en"}; null or {@code ""} means the
     *                  default namespace
     * @return the server's response, which has no fields; a normal return means the server accepted the delete
     */
    @Override
    public DeleteResponse deleteAll(String namespace) {
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
     * index.delete(Arrays.asList("doc-1", "doc-2"), false, "articles-en", null);
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
     * @return the server's response, which has no fields; a normal return means the server accepted the delete
     */
    @Override
    public DeleteResponse delete(List<String> ids,
                                 boolean deleteAll,
                                 String namespace,
                                 Struct filter) {
        DeleteRequest deleteRequest = validateDeleteRequest(ids, deleteAll, namespace, filter);

        return blockingStub.delete(deleteRequest);
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
     * DescribeIndexStatsResponse stats = index.describeIndexStats();
     * System.out.println("Total vectors: " + stats.getTotalVectorCount());
     *
     * NamespaceSummary articles = stats.getNamespacesMap().get("articles-en");
     * if (articles != null) {
     *     System.out.println("articles-en: " + articles.getVectorCount());
     * }
     * }</pre>
     *
     * @return the statistics: {@code getTotalVectorCount()}, {@code getNamespacesMap()} (namespace name to a
     *         summary whose {@code getVectorCount()} is that namespace's count) and {@code getDimension()} (0 for a
     *         sparse index)
     */
    @Override
    public DescribeIndexStatsResponse describeIndexStats() {
        DescribeIndexStatsRequest describeIndexStatsRequest = validateDescribeIndexStatsRequest(null);

        return blockingStub.describeIndexStats(describeIndexStatsRequest);
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
     * DescribeIndexStatsResponse stats = index.describeIndexStats(filter);
     * NamespaceSummary articles = stats.getNamespacesMap().get("articles-en");
     * if (articles != null) {
     *     System.out.println("News in articles-en: " + articles.getVectorCount());
     * }
     * }</pre>
     *
     * @param filter a metadata filter in Pinecone's filter syntax, such as {@code {"category": {"$eq": "news"}}}
     *               built as a {@code Struct}; null or an empty filter returns unfiltered statistics
     * @return the statistics: {@code getNamespacesMap()} (namespace name to a summary whose {@code getVectorCount()}
     *         counts the matching vectors), {@code getTotalVectorCount()} and {@code getDimension()}
     */
    @Override
    public DescribeIndexStatsResponse describeIndexStats(Struct filter) {
        DescribeIndexStatsRequest describeIndexStatsRequest = validateDescribeIndexStatsRequest(filter);

        return blockingStub.describeIndexStats(describeIndexStatsRequest);
    }

    /**
     * Lists the first page of vector ids in the default namespace.
     *
     * <p>Each call returns one page of ids in sorted order. When {@code hasPagination()} is true on the response, pass
     * {@code getPagination().getNext()} to {@link #list(String, int, String)} to get the next page. To list a named
     * namespace, use {@link #list(String)}. A vector upserted just before the call may not be listed yet.
     *
     * <pre>{@code
     * ListResponse page = index.list();
     * for (ListItem item : page.getVectorsList()) {
     *     System.out.println(item.getId());
     * }
     * }</pre>
     *
     * @return the page of ids: {@code getVectorsList()} holds an item per vector, whose {@code getId()} is the id, and
     *         {@code getPagination().getNext()} is the token for the next page when {@code hasPagination()} is true
     */
    @Override
    public ListResponse list() {
        validateListEndpointParameters(null, null, null, null, false, false, false, false);
        ListRequest listRequest = ListRequest.newBuilder().build();
        return blockingStub.list(listRequest);
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
     * ListResponse page = index.list("articles-en");
     * for (ListItem item : page.getVectorsList()) {
     *     System.out.println(item.getId());
     * }
     * }</pre>
     *
     * @param namespace the namespace to list, such as {@code "articles-en"}; {@code ""} means the default namespace.
     *                  Must not be null. A namespace that does not exist returns an empty page.
     * @return the page of ids: {@code getVectorsList()} holds an item per vector, whose {@code getId()} is the id, and
     *         {@code getPagination().getNext()} is the token for the next page when {@code hasPagination()} is true
     * @throws PineconeValidationException if {@code namespace} is null
     */
    @Override
    public ListResponse list(String namespace) {
        validateListEndpointParameters(namespace, null, null, null, true, false, false, false);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).build();
        return blockingStub.list(listRequest);
    }

    /**
     * Lists the next page of vector ids in a namespace, with a page size.
     *
     * <p>Get the first page with {@link #list(String, int)}, then pass each response's
     * {@code getPagination().getNext()} here until {@code hasPagination()} is false. Ids come back in sorted order. A
     * vector upserted just before the call may not be listed yet.
     *
     * <pre>{@code
     * ListResponse page = index.list("articles-en", 50);
     * while (true) {
     *     for (ListItem item : page.getVectorsList()) {
     *         System.out.println(item.getId());
     *     }
     *     if (!page.hasPagination()) {
     *         break;
     *     }
     *     page = index.list("articles-en", 50, page.getPagination().getNext());
     * }
     * }</pre>
     *
     * @param namespace the namespace to list, such as {@code "articles-en"}; {@code ""} means the default namespace.
     *                  Must not be null. A namespace that does not exist returns an empty page.
     * @param limit the most ids to return in this page, such as {@code 50}; must be positive, and the server rejects a
     *              limit above its maximum
     * @param paginationToken the token for the next page, from {@code getPagination().getNext()} on the previous
     *                        response. Must not be null or empty.
     * @return the page of ids: {@code getVectorsList()} holds an item per vector, whose {@code getId()} is the id, and
     *         {@code getPagination().getNext()} is the token for the next page when {@code hasPagination()} is true
     * @throws PineconeValidationException if {@code namespace} is null, {@code paginationToken} is null or empty, or
     *                                     {@code limit} is not positive
     */
    @Override
    public ListResponse list(String namespace, int limit, String paginationToken) {
        validateListEndpointParameters(namespace, null, paginationToken, limit, true, false, true, true);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setLimit(limit).setPaginationToken(paginationToken).build();
        return blockingStub.list(listRequest);
    }

    /**
     * Lists the first page of vector ids in a namespace, with a page size.
     *
     * <p>Each call returns one page of ids in sorted order. When {@code hasPagination()} is true on the response, pass
     * {@code getPagination().getNext()} to {@link #list(String, int, String)} to get the next page. A vector upserted
     * just before the call may not be listed yet.
     *
     * <pre>{@code
     * ListResponse page = index.list("articles-en", 50);
     * for (ListItem item : page.getVectorsList()) {
     *     System.out.println(item.getId());
     * }
     * }</pre>
     *
     * @param namespace the namespace to list, such as {@code "articles-en"}; {@code ""} means the default namespace.
     *                  Must not be null. A namespace that does not exist returns an empty page.
     * @param limit the most ids to return in this page, such as {@code 50}; must be positive, and the server rejects a
     *              limit above its maximum
     * @return the page of ids: {@code getVectorsList()} holds an item per vector, whose {@code getId()} is the id, and
     *         {@code getPagination().getNext()} is the token for the next page when {@code hasPagination()} is true
     * @throws PineconeValidationException if {@code namespace} is null or {@code limit} is not positive
     */
    @Override
    public ListResponse list(String namespace, int limit) {
        validateListEndpointParameters(namespace, null, null, limit, true, false, false, true);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setLimit(limit).build();
        return blockingStub.list(listRequest);
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
     * ListResponse page = index.list("articles-en", "doc-1#");
     * for (ListItem item : page.getVectorsList()) {
     *     System.out.println(item.getId());
     * }
     * }</pre>
     *
     * @param namespace the namespace to list, such as {@code "articles-en"}; {@code ""} means the default namespace.
     *                  Must not be null. A namespace that does not exist returns an empty page.
     * @param prefix the start of the ids to list, such as {@code "doc-1#"}, so that only ids beginning with it are
     *               returned. Must not be null or empty.
     * @return the page of ids: {@code getVectorsList()} holds an item per vector, whose {@code getId()} is the id, and
     *         {@code getPagination().getNext()} is the token for the next page when {@code hasPagination()} is true
     * @throws PineconeValidationException if {@code namespace} is null or {@code prefix} is null or empty
     */
    @Override
    public ListResponse list(String namespace, String prefix) {
        validateListEndpointParameters(namespace, prefix, null, null, true, true, false, false);
        ListRequest listRequest =
                ListRequest.newBuilder().setNamespace(namespace).setPrefix(prefix).build();
        return blockingStub.list(listRequest);
    }

    /**
     * Lists the first page of vector ids in a namespace that start with a prefix, with a page size.
     *
     * <p>Each call returns one page of ids in sorted order. When {@code hasPagination()} is true on the response, pass
     * {@code getPagination().getNext()} to {@link #list(String, String, String)} to get the next page. A vector
     * upserted just before the call may not be listed yet.
     *
     * <pre>{@code
     * ListResponse page = index.list("articles-en", "doc-1#", 50);
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
     * @return the page of ids: {@code getVectorsList()} holds an item per vector, whose {@code getId()} is the id, and
     *         {@code getPagination().getNext()} is the token for the next page when {@code hasPagination()} is true
     * @throws PineconeValidationException if {@code namespace} is null, {@code prefix} is null or empty, or
     *                                     {@code limit} is not positive
     */
    @Override
    public ListResponse list(String namespace, String prefix, int limit) {
        validateListEndpointParameters(namespace, prefix, null, limit, true, true, false, true);

        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setPrefix(prefix).
                setLimit(limit).build();
        return blockingStub.list(listRequest);
    }

    /**
     * Lists the next page of vector ids in a namespace that start with a prefix.
     *
     * <p>Get the first page with {@link #list(String, String)}, then pass each response's
     * {@code getPagination().getNext()} here until {@code hasPagination()} is false. Ids come back in sorted order. A
     * vector upserted just before the call may not be listed yet.
     *
     * <pre>{@code
     * ListResponse page = index.list("articles-en", "doc-1#");
     * while (true) {
     *     for (ListItem item : page.getVectorsList()) {
     *         System.out.println(item.getId());
     *     }
     *     if (!page.hasPagination()) {
     *         break;
     *     }
     *     page = index.list("articles-en", "doc-1#", page.getPagination().getNext());
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
     * @return the page of ids: {@code getVectorsList()} holds an item per vector, whose {@code getId()} is the id, and
     *         {@code getPagination().getNext()} is the token for the next page when {@code hasPagination()} is true
     * @throws PineconeValidationException if {@code namespace} is null, {@code prefix} is null or empty, or
     *                                     {@code paginationToken} is null or empty
     */
    @Override
    public ListResponse list(String namespace, String prefix, String paginationToken) {
        validateListEndpointParameters(namespace, prefix, paginationToken, null, true, true, true, false);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setPrefix(prefix).
                setPaginationToken(paginationToken).build();
        return blockingStub.list(listRequest);
    }

    /**
     * Lists the next page of vector ids in a namespace that start with a prefix, with a page size.
     *
     * <p>Get the first page with {@link #list(String, String, int)}, then pass each response's
     * {@code getPagination().getNext()} here until {@code hasPagination()} is false. Ids come back in sorted order. A
     * vector upserted just before the call may not be listed yet.
     *
     * <pre>{@code
     * ListResponse page = index.list("articles-en", "doc-1#", 50);
     * while (true) {
     *     for (ListItem item : page.getVectorsList()) {
     *         System.out.println(item.getId());
     *     }
     *     if (!page.hasPagination()) {
     *         break;
     *     }
     *     page = index.list("articles-en", "doc-1#", page.getPagination().getNext(), 50);
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
     * @return the page of ids: {@code getVectorsList()} holds an item per vector, whose {@code getId()} is the id, and
     *         {@code getPagination().getNext()} is the token for the next page when {@code hasPagination()} is true
     * @throws PineconeValidationException if {@code namespace} is null, {@code prefix} is null or empty,
     *                                     {@code paginationToken} is null or empty, or {@code limit} is not positive
     */
    public ListResponse list(String namespace, String prefix, String paginationToken, int limit) {
        validateListEndpointParameters(namespace, prefix, paginationToken, limit, true, true, true, true);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setPrefix(prefix).
                setPaginationToken(paginationToken).setLimit(limit).build();
        return blockingStub.list(listRequest);
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
     * ListNamespacesResponse page = index.listNamespaces();
     * for (NamespaceDescription namespace : page.getNamespacesList()) {
     *     System.out.println(namespace.getName() + ": " + namespace.getRecordCount());
     * }
     * }</pre>
     *
     * @return the first page of namespaces; {@code getNamespacesList()} holds each namespace's name and record count,
     *         and {@code getPagination().getNext()} holds the token for the next page when there is one
     */
    @Override
    public ListNamespacesResponse listNamespaces() {
        ListNamespacesRequest listNamespacesRequest = ListNamespacesRequest
                .newBuilder()
                .build();
        return blockingStub.listNamespaces(listNamespacesRequest);
    }

    /**
     * Lists the next page of namespaces in this index, continuing a previous listing.
     *
     * <p>Pass the token from {@code getPagination().getNext()} of the previous response. A response without pagination
     * is the last page. To get the first page, call {@link #listNamespaces()}.
     *
     * <pre>{@code
     * ListNamespacesResponse page = index.listNamespaces();
     * List<NamespaceDescription> namespaces = new ArrayList<>(page.getNamespacesList());
     * while (page.hasPagination()) {
     *     page = index.listNamespaces(page.getPagination().getNext());
     *     namespaces.addAll(page.getNamespacesList());
     * }
     * }</pre>
     *
     * @param paginationToken the token from the previous page's {@code getPagination().getNext()}; must not be null
     * @return the next page of namespaces, in the same shape as {@link #listNamespaces()}
     */
    @Override
    public ListNamespacesResponse listNamespaces(String paginationToken) {
        ListNamespacesRequest listNamespacesRequest = ListNamespacesRequest
                .newBuilder()
                .setPaginationToken(paginationToken)
                .build();
        return blockingStub.listNamespaces(listNamespacesRequest);
    }

    /**
     * Lists one page of namespaces in this index, with a page size you choose.
     *
     * <p>Use this overload to set the page size. Pagination works as in {@link #listNamespaces(String)}.
     *
     * <pre>{@code
     * ListNamespacesResponse page = index.listNamespaces(null, 10);
     * }</pre>
     *
     * @param paginationToken the token from the previous page's {@code getPagination().getNext()}, or null for the
     *                        first page
     * @param limit           the maximum number of namespaces to return in this page, for example {@code 10}
     * @return one page of namespaces, in the same shape as {@link #listNamespaces()}
     */
    @Override
    public ListNamespacesResponse listNamespaces(String paginationToken, int limit) {
        ListNamespacesRequest.Builder listNamespacesRequest = ListNamespacesRequest
                .newBuilder()
                .setLimit(limit);
        if(paginationToken != null) {
            listNamespacesRequest.setPaginationToken(paginationToken);
        }
        return blockingStub.listNamespaces(listNamespacesRequest.build());
    }

    /**
     * Lists one page of the namespaces in this index whose names start with a prefix.
     *
     * <p>Use this overload when you name namespaces by a convention, such as one namespace per language or per tenant.
     * {@code getTotalCount()} on the response reports how many namespaces match across all pages. Pagination works as
     * in {@link #listNamespaces(String)}; pass the same prefix on every page.
     *
     * <pre>{@code
     * ListNamespacesResponse page = index.listNamespaces("articles-", null, 50);
     * System.out.println(page.getTotalCount() + " namespaces start with articles-");
     * }</pre>
     *
     * @param prefix          the start of the namespace names to list, for example {@code "articles-"}; null or empty
     *                        lists every namespace
     * @param paginationToken the token from the previous page's {@code getPagination().getNext()}, or null for the
     *                        first page
     * @param limit           the maximum number of namespaces to return in this page, for example {@code 50}
     * @return one page of matching namespaces; {@code getTotalCount()} is the number of matches across all pages
     */
    @Override
    public ListNamespacesResponse listNamespaces(String prefix, String paginationToken, int limit) {
        ListNamespacesRequest.Builder listNamespacesRequest = ListNamespacesRequest
                .newBuilder()
                .setLimit(limit);
        if(prefix != null && !prefix.isEmpty()) {
            listNamespacesRequest.setPrefix(prefix);
        }
        if(paginationToken != null) {
            listNamespacesRequest.setPaginationToken(paginationToken);
        }
        return blockingStub.listNamespaces(listNamespacesRequest.build());
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
     * NamespaceDescription namespace = index.createNamespace("articles-en");
     * }</pre>
     *
     * @param name the name of the namespace to create, for example {@code "articles-en"}; the server rejects an empty
     *             name and {@code "__default__"}
     * @return the new namespace's description, including {@code getName()}
     */
    @Override
    public NamespaceDescription createNamespace(String name) {
        CreateNamespaceRequest createNamespaceRequest = CreateNamespaceRequest
                .newBuilder()
                .setName(name)
                .build();
        return blockingStub.createNamespace(createNamespaceRequest);
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
     * NamespaceDescription namespace = index.createNamespace("articles-en", schema);
     * }</pre>
     *
     * @param name   the name of the namespace to create, for example {@code "articles-en"}; the server rejects an empty
     *               name and {@code "__default__"}
     * @param schema the metadata fields to index for filtering; null creates the namespace without a schema, so every
     *               metadata field is indexed
     * @return the new namespace's description, including {@code getName()} and {@code getSchema()}
     */
    @Override
    public NamespaceDescription createNamespace(String name, MetadataSchema schema) {
        CreateNamespaceRequest.Builder builder = CreateNamespaceRequest
                .newBuilder()
                .setName(name);
        if (schema != null) {
            builder.setSchema(schema);
        }
        return blockingStub.createNamespace(builder.build());
    }

    /**
     * Describes one namespace in this index, including how many records it holds.
     *
     * <p>To describe the default namespace, pass {@code "__default__"}; the server rejects an empty string. Namespace
     * operations work on serverless indexes only; the server rejects them on a pod-based index.
     *
     * <pre>{@code
     * NamespaceDescription namespace = index.describeNamespace("articles-en");
     * System.out.println(namespace.getRecordCount());
     * }</pre>
     *
     * @param namespace the namespace to describe, for example {@code "articles-en"}; must not be null
     * @return the namespace's description: {@code getName()}, {@code getRecordCount()}, and {@code getSchema()} if
     *         the namespace has one
     */
    public NamespaceDescription describeNamespace(String namespace) {
        DescribeNamespaceRequest describeNamespaceRequest = DescribeNamespaceRequest
                .newBuilder()
                .setNamespace(namespace)
                .build();
        return blockingStub.describeNamespace(describeNamespaceRequest);
    }

    /**
     * Deletes a namespace and every record in it.
     *
     * <p>This cannot be undone. To delete only some records, use {@code deleteByIds} or {@code deleteByFilter} instead.
     * To delete the default namespace, pass {@code "__default__"}; the server rejects an empty string. Namespace
     * operations work on serverless indexes only; the server rejects them on a pod-based index.
     *
     * <pre>{@code
     * index.deleteNamespace("articles-en");
     * }</pre>
     *
     * @param namespace the namespace to delete, for example {@code "articles-en"}; must not be null
     * @return an empty {@link DeleteResponse}
     */
    public DeleteResponse deleteNamespace(String namespace) {
        DeleteNamespaceRequest deleteNamespaceRequest = DeleteNamespaceRequest
                .newBuilder()
                .setNamespace(namespace)
                .build();
        return blockingStub.deleteNamespace(deleteNamespaceRequest);
    }

    /**
     * Writes records to a namespace; the server turns each record's text into a vector with the index's integrated
     * embedding model.
     *
     * <p>Use this instead of {@code upsert} for an index created with {@code Pinecone.createIndexForModel}: you send
     * text, not vectors. Each record needs an {@code "_id"} and the text field named in the index's field map, such as
     * {@code "chunk_text"}; every other field is stored as metadata. This method sends every value as a string. A
     * record with an existing id is replaced, and a namespace that does not exist yet is created. A search made right
     * after an upsert may not reflect it yet.
     *
     * <pre>{@code
     * Map<String, String> record = new HashMap<>();
     * record.put("_id", "doc-1");
     * record.put("chunk_text", "Vaccines train the immune system to recognize a virus.");
     * record.put("category", "health");
     *
     * index.upsertRecords("articles-en", Collections.singletonList(record));
     * }</pre>
     *
     * @param namespace the namespace to write to, such as {@code "articles-en"}; pass {@code "__default__"} for the
     *                  default namespace. The server rejects an empty string, and the client throws
     *                  {@link ApiException} for null.
     * @param upsertRecords the records to write, each a map from field name to value with at least {@code "_id"} and
     *                      the index's text field; must not be null or empty
     * @throws ApiException if the server rejects the request, for example because the index has no integrated embedding
     *         model or a record lacks its text field, or if the request cannot be sent
     */
    public void upsertRecords(String namespace, List<Map<String, String>> upsertRecords) throws ApiException {
        List<UpsertRecord> records = new ArrayList<>();
        for(Map<String, String> record: upsertRecords) {
            UpsertRecord upsertRecord = new UpsertRecord();
            for (Map.Entry<String, String> entry : record.entrySet()) {
                if(entry.getKey().equals("_id")) {
                    upsertRecord.id(entry.getValue());
                }
                else {
                    upsertRecord.putAdditionalProperty(entry.getKey(), entry.getValue());
                }
            }

            records.add(upsertRecord);
        }
        vectorOperations.upsertRecordsNamespace(Configuration.VERSION, namespace, records);
    }

    /**
     * Searches a namespace with a query object you build, for options the other {@code searchRecords} methods do not
     * take.
     *
     * <p>For a plain search by text, record id or vector, {@link #searchRecordsByText searchRecordsByText},
     * {@link #searchRecordsById searchRecordsById} and {@link #searchRecordsByVector searchRecordsByVector} are
     * shorter. Use this method when you need a query option they do not expose, such as {@code matchTerms}. The query
     * must set {@code topK} and exactly one of {@code inputs} (text for the server to embed, under the key
     * {@code "text"}), {@code vector} or {@code id}; the server rejects any other combination. A search made right
     * after an upsert may not reflect it yet.
     *
     * <pre>{@code
     * Map<String, Object> inputs = new HashMap<>();
     * inputs.put("text", "How do vaccines prevent disease?");
     * SearchRecordsRequestQuery query = new SearchRecordsRequestQuery()
     *         .topK(10)
     *         .inputs(inputs);
     * SearchRecordsRequestRerank rerank = new SearchRecordsRequestRerank()
     *         .model("bge-reranker-v2-m3")
     *         .rankFields(Arrays.asList("chunk_text"))
     *         .topN(3);
     *
     * SearchRecordsResponse response = index.searchRecords(
     *         "articles-en", query, Arrays.asList("chunk_text", "category"), rerank);
     * for (Hit hit : response.getResult().getHits()) {
     *     System.out.println(hit.getId() + " " + hit.getScore() + " " + hit.getFields());
     * }
     * }</pre>
     *
     * @param namespace the namespace to search, such as {@code "articles-en"}; pass {@code "__default__"} for the
     *                  default namespace. The server rejects an empty string, and the client throws
     *                  {@link ApiException} for null.
     * @param query what to search for: {@code topK}, exactly one of {@code inputs}, {@code vector} or {@code id}, and
     *              optionally a {@code filter}; must not be null
     * @param fields the record fields to return with each hit, such as {@code Arrays.asList("chunk_text", "category")};
     *               null returns every field and an empty list returns none
     * @param rerank how to rerank the hits before they are returned: a {@link SearchRecordsRequestRerank} with the
     *               reranking model and the fields to rank by; null to skip reranking. Its {@code query} defaults to
     *               the search text, and the server requires it when the query uses {@code vector} or {@code id}.
     * @return the hits, most relevant first: {@code getResult().getHits()} holds each hit's {@code getId()},
     *         {@code getScore()} and {@code getFields()} (the requested fields as a map), and {@code getUsage()}
     *         reports the read units and any embedding or rerank usage
     * @throws ApiException if the server rejects the query, for example because it sets more than one of
     *         {@code inputs}, {@code vector} and {@code id}, or if the request cannot be sent
     */
    public SearchRecordsResponse searchRecords(String namespace,
                                               SearchRecordsRequestQuery query,
                                               List<String> fields,
                                               SearchRecordsRequestRerank rerank) throws ApiException {
        SearchRecordsRequest request = new SearchRecordsRequest()
                .query(query)
                .fields(fields)
                .rerank(rerank);

        return vectorOperations.searchRecordsNamespace(Configuration.VERSION, namespace, request);
    }

    /**
     * Searches a namespace for the records most similar to a record already stored in it.
     *
     * <p>The server uses the stored vector of the record with this id as the query, so nothing is embedded and the
     * index does not need an integrated embedding model. If no record has the id, the response has no hits. To search
     * by text, use {@link #searchRecordsByText searchRecordsByText}. A search made right after an upsert may not
     * reflect it yet.
     *
     * <pre>{@code
     * SearchRecordsResponse response = index.searchRecordsById("doc-1", "articles-en",
     *         Arrays.asList("chunk_text", "category"), 5, null, null);
     * for (Hit hit : response.getResult().getHits()) {
     *     System.out.println(hit.getId() + " " + hit.getScore() + " " + hit.getFields());
     * }
     * }</pre>
     *
     * @param id the id of the stored record to search from, such as {@code "doc-1"}
     * @param namespace the namespace to search, such as {@code "articles-en"}; pass {@code "__default__"} for the
     *                  default namespace. The server rejects an empty string, and the client throws
     *                  {@link ApiException} for null.
     * @param fields the record fields to return with each hit, such as {@code Arrays.asList("chunk_text", "category")};
     *               null returns every field and an empty list returns none
     * @param topK the number of hits to return, such as {@code 5}; the server rejects values below 1 or above its limit
     * @param filter a metadata filter in Pinecone's filter syntax, built as nested maps, such as
     *               {@code {"category": {"$eq": "health"}}}; only records whose fields match are returned. Null for no
     *               filter.
     * @param rerank how to rerank the hits before they are returned: a {@link SearchRecordsRequestRerank} with the
     *               reranking model, the fields to rank by and the {@code query} text to rank against, which the server
     *               requires here because there is no search text; null to skip reranking
     * @return the hits, most relevant first: {@code getResult().getHits()} holds each hit's {@code getId()},
     *         {@code getScore()} and {@code getFields()} (the requested fields as a map), and {@code getUsage()}
     *         reports the read units and any embedding or rerank usage
     * @throws ApiException if the server rejects the request, for example because {@code topK} is out of range, or if
     *         the request cannot be sent
     */
    public SearchRecordsResponse searchRecordsById(String id,
                                  String namespace,
                                  List<String> fields,
                                  int topK,
                                  Map<String, Object> filter,
                                  SearchRecordsRequestRerank rerank) throws ApiException {
        SearchRecordsRequestQuery query = new SearchRecordsRequestQuery()
                .id(id)
                .topK(topK)
                .filter(filter);

        SearchRecordsRequest request = new SearchRecordsRequest()
                .query(query)
                .fields(fields)
                .rerank(rerank);

        return vectorOperations.searchRecordsNamespace(Configuration.VERSION, namespace, request);
    }

    /**
     * Searches a namespace for the records most similar to a query vector you supply.
     *
     * <p>Use this when you computed the query embedding yourself; it must match the index's dimension and come from the
     * same model as the stored records. For a dense index set {@code values} on the {@link SearchRecordsVector}, and
     * optionally both {@code sparseIndices} and {@code sparseValues}; for a sparse index set both sparse lists. Nothing
     * is embedded, so the index does not need an integrated embedding model. To search by text, use
     * {@link #searchRecordsByText searchRecordsByText}. A search made right after an upsert may not reflect it yet.
     *
     * <pre>{@code
     * SearchRecordsVector vector = new SearchRecordsVector()
     *         .values(Arrays.asList(0.12f, 0.48f, 0.91f));
     * SearchRecordsResponse response = index.searchRecordsByVector(vector, "articles-en",
     *         Arrays.asList("chunk_text", "category"), 5, null, null);
     * for (Hit hit : response.getResult().getHits()) {
     *     System.out.println(hit.getId() + " " + hit.getScore() + " " + hit.getFields());
     * }
     * }</pre>
     *
     * @param vector the query vector, with dense {@code values}, sparse {@code sparseIndices} and {@code sparseValues},
     *               or both; must not be null
     * @param namespace the namespace to search, such as {@code "articles-en"}; pass {@code "__default__"} for the
     *                  default namespace. The server rejects an empty string, and the client throws
     *                  {@link ApiException} for null.
     * @param fields the record fields to return with each hit, such as {@code Arrays.asList("chunk_text", "category")};
     *               null returns every field and an empty list returns none
     * @param topK the number of hits to return, such as {@code 5}; the server rejects values below 1 or above its limit
     * @param filter a metadata filter in Pinecone's filter syntax, built as nested maps, such as
     *               {@code {"category": {"$eq": "health"}}}; only records whose fields match are returned. Null for no
     *               filter.
     * @param rerank how to rerank the hits before they are returned: a {@link SearchRecordsRequestRerank} with the
     *               reranking model, the fields to rank by and the {@code query} text to rank against, which the server
     *               requires here because there is no search text; null to skip reranking
     * @return the hits, most relevant first: {@code getResult().getHits()} holds each hit's {@code getId()},
     *         {@code getScore()} and {@code getFields()} (the requested fields as a map), and {@code getUsage()}
     *         reports the read units and any embedding or rerank usage
     * @throws ApiException if the server rejects the request, for example because a dense index got no dense values or
     *         only one of the sparse lists was set, or if the request cannot be sent
     */
    public SearchRecordsResponse searchRecordsByVector(SearchRecordsVector vector,
                                                       String namespace,
                                                       List<String> fields,
                                                       int topK,
                                                       Map<String, Object>  filter,
                                                       SearchRecordsRequestRerank rerank) throws ApiException {
        SearchRecordsRequestQuery query = new SearchRecordsRequestQuery()
                .vector(vector)
                .topK(topK)
                .filter(filter);

        SearchRecordsRequest request = new SearchRecordsRequest()
                .query(query)
                .fields(fields)
                .rerank(rerank);

        return vectorOperations.searchRecordsNamespace(Configuration.VERSION, namespace, request);
    }

    /**
     * Searches a namespace for the records most similar to a piece of text, which the server embeds with the index's
     * integrated embedding model.
     *
     * <p>This is the usual way to search an index created with {@code Pinecone.createIndexForModel} and loaded with
     * {@link #upsertRecords upsertRecords}. To search from a stored record or with a vector you computed, use
     * {@link #searchRecordsById searchRecordsById} or {@link #searchRecordsByVector searchRecordsByVector}; for query
     * options these methods do not take, use {@link #searchRecords searchRecords}. A search made right after an upsert
     * may not reflect it yet.
     *
     * <pre>{@code
     * Map<String, Object> filter = new HashMap<>();
     * filter.put("category", Collections.singletonMap("$eq", "health"));
     *
     * SearchRecordsResponse response = index.searchRecordsByText("How do vaccines prevent disease?",
     *         "articles-en", Arrays.asList("chunk_text", "category"), 5, filter, null);
     * for (Hit hit : response.getResult().getHits()) {
     *     System.out.println(hit.getId() + " " + hit.getScore() + " " + hit.getFields());
     * }
     * }</pre>
     *
     * @param text the text to search for, such as {@code "How do vaccines prevent disease?"}; the server rejects blank
     *             text
     * @param namespace the namespace to search, such as {@code "articles-en"}; pass {@code "__default__"} for the
     *                  default namespace. The server rejects an empty string, and the client throws
     *                  {@link ApiException} for null.
     * @param fields the record fields to return with each hit, such as {@code Arrays.asList("chunk_text", "category")};
     *               null returns every field and an empty list returns none
     * @param topK the number of hits to return, such as {@code 5}; the server rejects values below 1 or above its limit
     * @param filter a metadata filter in Pinecone's filter syntax, built as nested maps, such as
     *               {@code {"category": {"$eq": "health"}}}; only records whose fields match are returned. Null for no
     *               filter.
     * @param rerank how to rerank the hits before they are returned: a {@link SearchRecordsRequestRerank} with the
     *               reranking model and the fields to rank by; null to skip reranking. Its {@code query} defaults to
     *               the search text.
     * @return the hits, most relevant first: {@code getResult().getHits()} holds each hit's {@code getId()},
     *         {@code getScore()} and {@code getFields()} (the requested fields as a map), and {@code getUsage()}
     *         reports the read units and any embedding or rerank usage
     * @throws ApiException if the server rejects the request, for example because the index has no integrated embedding
     *         model, or if the request cannot be sent
     */
    public SearchRecordsResponse searchRecordsByText(String text,
                                    String namespace,
                                    List<String> fields,
                                    int topK,
                                    Map<String, Object>  filter,
                                    SearchRecordsRequestRerank rerank) throws ApiException {
        HashMap<String, String> inputs = new HashMap<>();
        inputs.put("text", text);

        SearchRecordsRequestQuery query = new SearchRecordsRequestQuery()
                .inputs(inputs)
                .topK(topK)
                .filter(filter);

        SearchRecordsRequest request = new SearchRecordsRequest()
                .query(query)
                .fields(fields)
                .rerank(rerank);

        return vectorOperations.searchRecordsNamespace(Configuration.VERSION, namespace, request);
    }

    /**
     * Releases this client's use of the gRPC channel to the index, shutting the channel down and freeing its
     * threads and network connections once no other client uses it.
     *
     * <p>Every {@link Index} and {@link AsyncIndex} that one {@link Pinecone} client (or clients with identical
     * settings) opens for the same index shares one channel, which shuts down only when the last of them is closed.
     * Calling this method more than once has no further effect. Getting a new connection from {@link Pinecone} after
     * the channel shuts down opens a fresh one.
     *
     * <pre>{@code
     * Index index = pinecone.getIndexConnection("product-catalog");
     * try {
     *     index.upsert("doc-1", Arrays.asList(0.12f, 0.48f, 0.91f), "articles-en");
     * } finally {
     *     index.close();
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
