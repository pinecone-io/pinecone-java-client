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
import io.pinecone.proto.DeleteRequest;
import io.pinecone.proto.DescribeIndexStatsRequest;
import io.pinecone.proto.FetchResponse;
import io.pinecone.proto.ListNamespacesResponse;
import io.pinecone.proto.ListResponse;
import io.pinecone.proto.NamespaceDescription;
import io.pinecone.proto.QueryRequest;
import io.pinecone.proto.QueryResponse;
import io.pinecone.proto.UpdateRequest;
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

import static io.pinecone.clients.Pinecone.buildOkHttpClient;

/**
 * A client for interacting with a Pinecone index asynchronously. Allows for vector operations such as upserting,
 * querying, fetching, updating, and deleting vectors along with records operations such as upsert and search records.
 * This class provides a direct interface to interact with a specific index, encapsulating network communication and request validation.
 * <p>
 * Example:
 * <pre>{@code
 *     import io.pinecone.clients.Pinecone;
 *     import io.pinecone.clients.AsyncIndex;
 *
 *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
 *     AsyncIndex asyncIndex = client.getAsyncIndexConnection("my-index");
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
    private final String indexName;
    BulkOperationsApi bulkOperations;

    /**
     * Constructs an {@link AsyncIndex} instance for interacting with a Pinecone index.
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.clients.Pinecone;
     *     import io.pinecone.clients.AsyncIndex;
     *
     *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
     *     AsyncIndex asyncIndex = client.getAsyncIndexConnection("my-index");
     * }</pre>
     *
     * @param config The {@link PineconeConfig} configuration of the index.
     * @param connection The {@link PineconeConnection} configuration to be used for this index.
     * @param indexName The name of the index to interact with. The index host will be automatically resolved.
     * @throws PineconeValidationException if the connection object is null.
     */
    public AsyncIndex(PineconeConfig config, PineconeConnection connection, String indexName) {
        if (connection == null) {
            throw new PineconeValidationException("Pinecone connection object cannot be null.");
        }

        this.indexName = indexName;
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
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.UpsertResponse;
     *     import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
     *     import static io.pinecone.commons.IndexInterface.buildUpsertVectorWithUnsignedIndices;
     *     import com.google.protobuf.Struct;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     // Vector ids to be upserted
     *     List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
     *
     *     // List of values to be upserted
     *     List<List<Float>> values = new ArrayList<>();
     *     values.add(Arrays.asList(1.0f, 2.0f, 3.0f));
     *     values.add(Arrays.asList(4.0f, 5.0f, 6.0f));
     *     values.add(Arrays.asList(7.0f, 8.0f, 9.0f));
     *
     *     // List of sparse indices to be upserted
     *     List<List<Long>> sparseIndices = new ArrayList<>();
     *     sparseIndices.add(Arrays.asList(1L, 2L, 3L));
     *     sparseIndices.add(Arrays.asList(4L, 5L, 6L));
     *     sparseIndices.add(Arrays.asList(7L, 8L, 9L));
     *
     *     // List of sparse values to be upserted
     *     List<List<Float>> sparseValues = new ArrayList<>();
     *     sparseValues.add(Arrays.asList(1000f, 2000f, 3000f));
     *     sparseValues.add(Arrays.asList(4000f, 5000f, 6000f));
     *     sparseValues.add(Arrays.asList(7000f, 8000f, 9000f));
     *
     *     List<VectorWithUnsignedIndices> vectors = new ArrayList<>(3);
     *
     *     // metadata
     *     Struct metadataStruct1 = Struct.newBuilder()
     *         .putFields("genre", Value.newBuilder().setStringValue("action").build())
     *         .putFields("year", Value.newBuilder().setNumberValue(2019).build())
     *         .build();
     *
     *     Struct metadataStruct2 = Struct.newBuilder()
     *         .putFields("genre", Value.newBuilder().setStringValue("thriller").build())
     *         .putFields("year", Value.newBuilder().setNumberValue(2020).build())
     *         .build();
     *
     *     Struct metadataStruct3 = Struct.newBuilder()
     *         .putFields("genre", Value.newBuilder().setStringValue("comedy").build())
     *         .putFields("year", Value.newBuilder().setNumberValue(2021).build())
     *         .build();
     *     List<Struct> metadataStructList =
     *     Arrays.asList(metadataStruct1, metadataStruct2, metadataStruct3);
     *
     *     List<VectorWithUnsignedIndices> vectors = new ArrayList<>(3);
     *
     *     for (int i=0; i<upsertIds.size(); i++) {
     *         vectors.add(
     *             buildUpsertVectorWithUnsignedIndices(upsertIds.get(i),
     *                 values.get(i),
     *                 sparseIndices.get(i),
     *                 sparseValues.get(i),
     *                 metadataStructList.get(i)));
     *     }
     *
     *     ListenableFuture<UpsertResponse> listenableFuture = asyncIndex.upsert(vectors, "example-namespace");
     * }</pre>
     */
    @Override
    public ListenableFuture<UpsertResponse> upsert(List<VectorWithUnsignedIndices> vectorList,
                                                   String namespace) {
        UpsertRequest upsertRequest = validateUpsertRequest(vectorList, namespace);
        return asyncStub.upsert(upsertRequest);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.UpsertResponse;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     ListenableFuture<UpsertResponse> listenableFuture =
     *     asyncIndex.upsert("my-vector-id", Arrays.asList(1.0f, 2.0f, 3.0f));
     * }</pre>
     */
    @Override
    public ListenableFuture<UpsertResponse> upsert(String id,
                                                   List<Float> values) {
        return upsert(id, values, null, null, null, null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.UpsertResponse;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     ListenableFuture<UpsertResponse> listenableFuture =
     *     asyncIndex.upsert("my-vector-id", Arrays.asList(1.0f, 2.0f, 3.0f), "example-namespace");
     * }</pre>
     */
    @Override
    public ListenableFuture<UpsertResponse> upsert(String id,
                                                   List<Float> values,
                                                   String namespace) {
        return upsert(id, values, null, null, null, namespace);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.UpsertResponse;
     *     import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
     *     import static io.pinecone.commons.IndexInterface.buildUpsertVectorWithUnsignedIndices;
     *     import com.google.protobuf.Struct;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     // metadata
     *     Struct metadataStruct = Struct.newBuilder()
     *         .putFields("genre", Value.newBuilder().setStringValue("action").build())
     *         .putFields("year", Value.newBuilder().setNumberValue(2019).build())
     *         .build();
     *
     *     ListenableFuture<UpsertResponse> listenableFuture =
     *     asyncIndex.upsert("my-vector-id",
     *         Arrays.asList(1.0f, 2.0f, 3.0f),
     *         Arrays.asList(1L, 2L, 3L),
     *         Arrays.asList(1000f, 2000f, 3000f),
     *         metadataStruct,
     *         "example-namespace");
     * }</pre>
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
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     ListenableFuture<QueryResponseWithUnsignedIndices> listenableFuture =
     *     asyncIndex.query(10,
     *         Arrays.asList(1.0f, 2.0f, 3.0f),
     *         Arrays.asList(1L, 2L, 3L),
     *         Arrays.asList(1000f, 2000f, 3000f),
     *         null,
     *         "example-namespace",
     *         null,
     *         true,
     *         true);
     * }</pre>
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
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     ListenableFuture<QueryResponseWithUnsignedIndices> listenableFuture =
     *     asyncIndex.queryByVectorId(10,
     *         "my-vector-id",
     *         "example-namespace",
     *         null,
     *         true,
     *         true);
     * }</pre>
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
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
     *     import com.google.protobuf.Struct;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     Struct filter = Struct.newBuilder()
     *         .putFields("genre", Value.newBuilder().setStringValue("action").build()).build();
     *     ListenableFuture<QueryResponseWithUnsignedIndices> listenableFuture =
     *     asyncIndex.queryByVectorId(10,
     *         "my-vector-id",
     *         "example-namespace",
     *         filter);
     * }</pre>
     */
    @Override
    public ListenableFuture<QueryResponseWithUnsignedIndices> queryByVectorId(int topK,
                                                                              String id,
                                                                              String namespace,
                                                                              Struct filter) {
        return query(topK, null, null, null, id, namespace, filter, false, false);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     ListenableFuture<QueryResponseWithUnsignedIndices> listenableFuture =
     *     asyncIndex.queryByVectorId(10,
     *         "my-vector-id",
     *         "example-namespace",
     *         true,
     *         true);
     * }</pre>
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
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     ListenableFuture<QueryResponseWithUnsignedIndices> listenableFuture =
     *     asyncIndex.queryByVectorId(10, "my-vector-id", "example-namespace");
     * }</pre>
     */
    @Override
    public ListenableFuture<QueryResponseWithUnsignedIndices> queryByVectorId(int topK,
                                                                              String id,
                                                                              String namespace) {
        return query(topK, null, null, null, id, namespace, null, false, false);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     ListenableFuture<QueryResponseWithUnsignedIndices> listenableFuture =
     *     asyncIndex.queryByVectorId(10, "my-vector-id", true, true);
     * }</pre>
     */
    @Override
    public ListenableFuture<QueryResponseWithUnsignedIndices> queryByVectorId(int topK,
                                                                              String id,
                                                                              boolean includeValues,
                                                                              boolean includeMetadata) {
        return query(topK, null, null, null, id, null, null, includeValues, includeMetadata);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     ListenableFuture<QueryResponseWithUnsignedIndices> listenableFuture =
     *     asyncIndex.queryByVectorId(10, "my-vector-id");
     * }</pre>
     */
    @Override
    public ListenableFuture<QueryResponseWithUnsignedIndices> queryByVectorId(int topK,
                                                                              String id) {
        return query(topK, null, null, null, id, null, null, false, false);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
     *     import com.google.protobuf.Struct;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     Struct filter = Struct.newBuilder()
     *         .putFields("genre", Value.newBuilder().setStringValue("action").build()).build();
     *     ListenableFuture<QueryResponseWithUnsignedIndices> listenableFuture =
     *     asyncIndex.queryByVector(10,
     *         Arrays.asList(1.0f, 2.0f, 3.0f),
     *         "example-namespace",
     *         filter,
     *         true,
     *         true);
     * }</pre>
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
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
     *     import com.google.protobuf.Struct;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     Struct filter = Struct.newBuilder()
     *         .putFields("genre", Value.newBuilder().setStringValue("action").build()).build();
     *     ListenableFuture<QueryResponseWithUnsignedIndices> listenableFuture =
     *     asyncIndex.queryByVector(10,
     *         Arrays.asList(1.0f, 2.0f, 3.0f),
     *         "example-namespace",
     *         filter);
     * }</pre>
     */
    @Override
    public ListenableFuture<QueryResponseWithUnsignedIndices> queryByVector(int topK,
                                                                            List<Float> vector,
                                                                            String namespace,
                                                                            Struct filter) {
        return query(topK, vector, null, null, null, namespace, filter, false, false);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     ListenableFuture<QueryResponseWithUnsignedIndices> listenableFuture =
     *     index.queryByVector(10,
     *         Arrays.asList(1.0f, 2.0f, 3.0f),
     *         "example-namespace",
     *         true,
     *         true);
     * }</pre>
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
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     ListenableFuture<QueryResponseWithUnsignedIndices> listenableFuture =
     *     asyncIndex.queryByVector(10, Arrays.asList(1.0f, 2.0f, 3.0f), "example-namespace");
     * }</pre>
     */
    @Override
    public ListenableFuture<QueryResponseWithUnsignedIndices> queryByVector(int topK,
                                                                            List<Float> vector,
                                                                            String namespace) {
        return query(topK, vector, null, null, null, namespace, null, false, false);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     ListenableFuture<QueryResponseWithUnsignedIndices> listenableFuture =
     *     asyncIndex.queryByVector(10, Arrays.asList(1.0f, 2.0f, 3.0f), true, true);
     * }</pre>
     */
    @Override
    public ListenableFuture<QueryResponseWithUnsignedIndices> queryByVector(int topK,
                                                                            List<Float> vector,
                                                                            boolean includeValues,
                                                                            boolean includeMetadata) {
        return query(topK, vector, null, null, null, null, null, includeValues, includeMetadata);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     ListenableFuture<QueryResponseWithUnsignedIndices> listenableFuture =
     *     asyncIndex.queryByVector(10, Arrays.asList(1.0f, 2.0f, 3.0f));
     * }</pre>
     */
    @Override
    public ListenableFuture<QueryResponseWithUnsignedIndices> queryByVector(int topK,
                                                                            List<Float> vector) {
        return query(topK, vector, null, null, null, null, null, false, false);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.FetchResponse;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     ListenableFuture<FetchResponse> listenableFuture =
     *     asyncIndex.fetch(Arrays.asList("v1", "v2", "v3"));
     * }</pre>
     */
    @Override
    public ListenableFuture<FetchResponse> fetch(List<String> ids) {
        return fetch(ids, null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.FetchResponse;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     ListenableFuture<FetchResponse> listenableFuture =
     *     asyncIndex.fetch(Arrays.asList("v1", "v2", "v3"), "example-namespace");
     * }</pre>
     */
    @Override
    public ListenableFuture<FetchResponse> fetch(List<String> ids,
                                                 String namespace) {
        FetchRequest fetchRequest = validateFetchRequest(ids, namespace);

        return asyncStub.fetch(fetchRequest);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.UpdateResponse;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     ListenableFuture<UpdateResponse> listenableFuture =
     *     asyncIndex.update("my-vector-id", Arrays.asList(1.0f, 2.0f, 3.0f));
     * }</pre>
     */
    @Override
    public ListenableFuture<UpdateResponse> update(String id,
                                                   List<Float> values) {
        return update(id, values, null, null, null, null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.UpdateResponse;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     ListenableFuture<UpdateResponse> listenableFuture =
     *     asyncIndex.update("my-vector-id",Arrays.asList(1.0f, 2.0f, 3.0f),"example-namespace");
     * }</pre>
     */
    @Override
    public ListenableFuture<UpdateResponse> update(String id,
                                                   List<Float> values,
                                                   String namespace) {
        return update(id, values, null, namespace, null, null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.UpdateResponse;
     *     import com.google.protobuf.Struct;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     Struct metadata = Struct.newBuilder()
     *         .putFields("genre", Value.newBuilder().setStringValue("action").build())
     *         .putFields("year", Value.newBuilder().setNumberValue(2019).build())
     *         .build();
     *
     *     ListenableFuture<UpdateResponse> listenableFuture =
     *     asyncIndex.update("my-vector-id",
     *         Arrays.asList(1.0f, 2.0f, 3.0f),
     *         metadata,
     *         "example-namespace",
     *         Arrays.asList(1L, 2L, 3L),
     *         Arrays.asList(1000f, 2000f, 3000f));
     * }</pre>
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
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.DeleteResponse;
     *     import com.google.protobuf.Struct;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     ListenableFuture<DeleteResponse> listenableFuture =
     *     asyncIndex.deleteByIds(Arrays.asList("v1", "v2", "v3"), "example-namespace");
     * }</pre>
     */
    @Override
    public ListenableFuture<DeleteResponse> deleteByIds(List<String> ids, String namespace) {
        return delete(ids, false, namespace, null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.DeleteResponse;
     *     import com.google.protobuf.Struct;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     ListenableFuture<DeleteResponse> listenableFuture =
     *     asyncIndex.deleteByIds(Arrays.asList("v1", "v2", "v3"));
     * }</pre>
     */
    @Override
    public ListenableFuture<DeleteResponse> deleteByIds(List<String> ids) {
        return delete(ids, false, null, null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.DeleteResponse;
     *     import com.google.protobuf.Struct;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     Struct filter = Struct.newBuilder()
     *         .putFields("genre", Value.newBuilder().setStringValue("action").build()).build();
     *     ListenableFuture<DeleteResponse> listenableFuture =
     *     asyncIndex.deleteByFilter(filter, "example-namespace");
     * }</pre>
     */
    @Override
    public ListenableFuture<DeleteResponse> deleteByFilter(Struct filter, String namespace) {
        return delete(null, false, namespace, filter);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.DeleteResponse;
     *     import com.google.protobuf.Struct;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     Struct filter = Struct.newBuilder()
     *         .putFields("genre", Value.newBuilder().setStringValue("action").build()).build();
     *     ListenableFuture<DeleteResponse> listenableFuture = asyncIndex.deleteByFilter(filter);
     * }</pre>
     */
    @Override
    public ListenableFuture<DeleteResponse> deleteByFilter(Struct filter) {
        return delete(null, false, null, filter);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.DeleteResponse;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     ListenableFuture<DeleteResponse> listenableFuture = asyncIndex.deleteAll("example-namespace");
     * }</pre>
     */
    @Override
    public ListenableFuture<DeleteResponse> deleteAll(String namespace) {
        return delete(null, true, namespace, null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.DeleteResponse;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     ListenableFuture<DeleteResponse> listenableFuture =
     *     asyncIndex.delete(Arrays.asList("v1", "v2", "v3"),false,"example-namespace",null);
     * }</pre>
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
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.DescribeIndexStatsResponse;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     ListenableFuture<DescribeIndexStatsResponse> listenableFuture = asyncIndex.describeIndexStats();
     * }</pre>
     */
    @Override
    public ListenableFuture<DescribeIndexStatsResponse> describeIndexStats() {
        DescribeIndexStatsRequest describeIndexStatsRequest = validateDescribeIndexStatsRequest(null);

        return asyncStub.describeIndexStats(describeIndexStatsRequest);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.DescribeIndexStatsResponse;
     *     import com.google.protobuf.Struct;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     Struct filter = Struct.newBuilder()
     *         .putFields("genre", Value.newBuilder().setStringValue("action").build()).build();
     *     ListenableFuture<DescribeIndexStatsResponse> listenableFuture = asyncIndex.describeIndexStats();
     * }</pre>
     */
    @Override
    public ListenableFuture<DescribeIndexStatsResponse> describeIndexStats(Struct filter) {
        DescribeIndexStatsRequest describeIndexStatsRequest = validateDescribeIndexStatsRequest(filter);

        return asyncStub.describeIndexStats(describeIndexStatsRequest);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Example:
     *  <pre>{@code
     *     import io.pinecone.proto.ListResponse;
     *     import com.google.common.util.concurrent.Futures;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *      ListenableFuture<ListResponse> futureResponse = asyncIndex.list("");
     *      ListResponse asyncListResponse = Futures.getUnchecked(futureResponse);
     *  }</pre>
     */
    @Override
    public ListenableFuture<ListResponse> list() {
        validateListEndpointParameters(null, null, null, null, false, false, false, false);
        ListRequest listRequest = ListRequest.newBuilder().build();
        return asyncStub.list(listRequest);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Example:
     *  <pre>{@code
     *     import io.pinecone.proto.ListResponse;
     *     import com.google.common.util.concurrent.Futures;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *      ListenableFuture<ListResponse> futureResponse = asyncIndex.list("example-namespace");
     *      ListResponse asyncListResponse = Futures.getUnchecked(futureResponse);
     *  }</pre>
     */
    @Override
    public ListenableFuture<ListResponse> list(String namespace) {
        validateListEndpointParameters(namespace, null, null, null, true, false, false, false);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).build();
        return asyncStub.list(listRequest);
    }

    /**
     * {@inheritDoc}
     * <p>Example:
     *  <pre>{@code
     *     import io.pinecone.proto.ListResponse;
     *
     *     ...
     *
     *     ListenableFuture<ListResponse> futureResponse = asyncIndex.list("example-namespace", 10, "some-pagToken");
     *     ListResponse asyncListResponse = Futures.getUnchecked(futureResponse);
     *  }</pre>
     */
    @Override
    public ListenableFuture<ListResponse> list(String namespace, int limit, String paginationToken) {
        validateListEndpointParameters(namespace, null, paginationToken, limit, true, false, true, true);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setLimit(limit).setPaginationToken(paginationToken).build();
        return asyncStub.list(listRequest);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Example:
     *  <pre>{@code
     *     import io.pinecone.proto.ListResponse;
     *     import com.google.common.util.concurrent.Futures;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *      ListenableFuture<ListResponse> futureResponse = asyncIndex.list("example-namespace", 10);
     *      ListResponse asyncListResponse = Futures.getUnchecked(futureResponse);
     *  }</pre>
     */
    @Override
    public ListenableFuture<ListResponse> list(String namespace, int limit) {
        validateListEndpointParameters(namespace, null, null, limit, true, false, false, true);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setLimit(limit).build();
        return asyncStub.list(listRequest);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Example:
     *  <pre>{@code
     *     import io.pinecone.proto.ListResponse;
     *     import com.google.common.util.concurrent.Futures;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     ListenableFuture<ListResponse> futureResponse = asyncIndex.list("example-namespace", "prefix-");
     *     ListResponse asyncListResponse = Futures.getUnchecked(futureResponse);
     *  }</pre>
     */
    @Override
    public ListenableFuture<ListResponse> list(String namespace, String prefix) {
        validateListEndpointParameters(namespace, prefix, null, null, true, true, false, false);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setPrefix(prefix).build();
        return asyncStub.list(listRequest);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Example:
     *  <pre>{@code
     *     import io.pinecone.proto.ListResponse;
     *     import com.google.common.util.concurrent.Futures;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     ListenableFuture<ListResponse> futureResponse = asyncIndex.list("example-namespace", "prefix-", 10);
     *     ListResponse asyncListResponse = Futures.getUnchecked(futureResponse);
     *  }</pre>
     */
    @Override
    public ListenableFuture<ListResponse> list(String namespace, String prefix, int limit) {
        validateListEndpointParameters(namespace, prefix, null, limit, true, true, false, true);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setPrefix(prefix).
                setLimit(limit).build();
        return asyncStub.list(listRequest);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Example:
     *  <pre>{@code
     *     import io.pinecone.proto.ListResponse;
     *     import com.google.common.util.concurrent.Futures;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     ListenableFuture<ListResponse> futureResponse = asyncIndex.list("example-namespace", "prefix-", "some-pagToken");
     *     ListResponse asyncListResponse = Futures.getUnchecked(futureResponse);
     *  }</pre>
     */
    @Override
    public ListenableFuture<ListResponse> list(String namespace, String prefix, String paginationToken) {
        validateListEndpointParameters(namespace, prefix, paginationToken, null, true, true, true, false);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setPrefix(prefix).
                setPaginationToken(paginationToken).build();
        return asyncStub.list(listRequest);
    }

    /**
     * <p>Base method that retrieves a list of vector IDs from a specific namespace within an index.
     *
     * <p>The method internally constructs a {@link ListRequest} using the provided namespace
     * and the provided limit, which cuts off the response after the specified number of IDs. It filters the
     * retrieve IDs to match a provided prefix. It also can accept a pagination token to deterministically paginate
     * through a list of vector IDs. It then makes a synchronous RPC call to fetch the list of vector IDs.</p>
     *
     * <p>Example:
     *  <pre>{@code
     *     import io.pinecone.proto.ListResponse;
     *     import com.google.common.util.concurrent.Futures;
     *     import com.google.common.util.concurrent.ListenableFuture;
     *
     *     ...
     *
     *     ListenableFuture<ListResponse> futureResponse = asyncIndex.list("example-namespace", "prefix-", "some-pagToken", 10);
     *     ListResponse asyncListResponse = Futures.getUnchecked(futureResponse);
     *  }</pre>
     *
     * @param namespace The namespace that holds the vector IDs you want to retrieve. If namespace is not specified,
     *      *                  the default namespace is used.
     * @param prefix The prefix with which vector IDs must start to be included in the response.
     * @param paginationToken The token to paginate through the list of vector IDs.
     * @param limit The maximum number of vector IDs you want to retrieve.
     * @return {@link ListResponse} containing the list of vector IDs fetched from the specified namespace.
     *         The response includes vector IDs up to {@code 100} items.
     * @throws RuntimeException if there are issues processing the request or communicating with the server.
     *         This includes network issues, server errors, or serialization issues with the request or response.
     */
    @Override
    public ListenableFuture<ListResponse> list(String namespace, String prefix, String paginationToken, int limit) {
        validateListEndpointParameters(namespace, prefix, paginationToken, limit, true, true, true, true);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setPrefix(prefix).
                setPaginationToken(paginationToken).setLimit(limit).build();
        return asyncStub.list(listRequest);
    }

    /**
     * <pre>
     * Overload to get a list of all namespaces without limit and pagination token. When limit is not set, it'll
     * default to 100.
     * @return {@link ListenableFuture<ListNamespacesResponse>} The response for the list namespace operation.
     * </pre>
     */
    @Override
    public ListenableFuture<ListNamespacesResponse> listNamespaces() {
        ListNamespacesRequest listNamespacesRequest = ListNamespacesRequest
                .newBuilder()
                .build();
        return asyncStub.listNamespaces(listNamespacesRequest);
    }

    /**
     * <pre>
     * Overload to get a list of all namespaces withing an index with default limit set to 100.
     * @param paginationToken The token to paginate through the list of vector IDs.
     * @return {@link ListenableFuture<ListNamespacesResponse>} The response for the list namespace operation.
     * </pre>
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
     * <pre>
     * Get list of all namespaces within an index.
     * @param paginationToken The token to paginate through the list of vector IDs. If pagination token is set to null,
     *                        it'll be ignored.
     * @param limit           The maximum number of vector IDs you want to retrieve.
     * @return {@link ListenableFuture<ListNamespacesResponse>} The response for the list namespace operation.
     * </pre>
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
     * <pre>
     * Describe a namespace within an index, showing the vector count within the namespace.
     * @param namespace The namespace to describe.
     * @return {@link ListenableFuture<NamespaceDescription>} The response for the describe namespace operation.
     * </pre>
     */
    public ListenableFuture<NamespaceDescription> describeNamespace(String namespace) {
        DescribeNamespaceRequest describeNamespaceRequest = DescribeNamespaceRequest
                .newBuilder()
                .setNamespace(namespace)
                .build();
        return asyncStub.describeNamespace(describeNamespaceRequest);
    }

    /**
     * <pre>
     * Delete a namespace from an index.
     * @param namespace The namespace to describe.
     * @return {@link ListenableFuture<DeleteResponse>} The response for the delete namespace operation.
     * </pre>
     */
    public ListenableFuture<DeleteResponse> deleteNamespace(String namespace) {
        DeleteNamespaceRequest deleteNamespaceRequest = DeleteNamespaceRequest
                .newBuilder()
                .setNamespace(namespace)
                .build();
        return asyncStub.deleteNamespace(deleteNamespaceRequest);
    }

    /**
     * <p>Initiates an asynchronous import of vectors from object storage into a specified index.</p>
     *
     * <p>The method constructs a {@link StartImportRequest} using the provided URI for the data and optional
     * storage integration ID. It also allows for specifying how to respond to errors during the import process
     * through the {@link ImportErrorMode}. The import operation is then initiated via a call to the
     * underlying {@link BulkOperationsApi}.</p>
     *
     * <p>Example:
     *  <pre>{@code
     *     import org.openapitools.db_data.client.ApiException;
     *     import org.openapitools.db_data.client.model.ImportErrorMode;
     *
     *     ...
     *
     *     String uri = "s3://path/to/file.parquet";
     *     String integrationId = "123-456-789";
     *     StartImportResponse response = asyncIndex.startImport(uri, integrationId, ImportErrorMode.OnErrorEnum.CONTINUE);
     *  }</pre>
     *
     * @param uri The URI prefix under which the data to import is available.
     * @param integrationId The ID of the storage integration to access the data. Can be null or empty.
     * @param errorMode Indicates how to respond to errors during the import process. Can be null.
     * @return {@link StartImportResponse} containing the details of the initiated import operation.
     * @throws ApiException if there are issues processing the request or communicating with the server.
     *         This includes network issues, server errors, or serialization issues with the request or response.
     */
    public StartImportResponse startImport(String uri, String integrationId, ImportErrorMode.OnErrorEnum errorMode) throws ApiException {
        StartImportRequest importRequest = new StartImportRequest();
        importRequest.setUri(uri);
        if(integrationId != null && !integrationId.isEmpty()) {
            importRequest.setIntegrationId(integrationId);
        }
        if(errorMode != null) {
            ImportErrorMode importErrorMode = new ImportErrorMode().onError(errorMode);
            importRequest.setErrorMode(importErrorMode);
        }

        return bulkOperations.startBulkImport(importRequest);
    }

    /**
     * <p>Lists all recent and ongoing import operations for the specified index with default limit and pagination.</p>
     *
     * <p>The method constructs a request to fetch a list of import operations, limited by the default value set to 100
     * number of operations to return per page. The pagination token is set to null as well by default.</p>
     *
     *
     * <p>Example:
     *  <pre>{@code
     *     import org.openapitools.db_data.client.ApiException;
     *     import org.openapitools.db_data.client.model.ListImportsResponse;
     *
     *     ...
     *
     *     ListImportsResponse response = asyncIndex.listImports();
     *  }</pre>
     *
     * @return {@link ListImportsResponse} containing the list of recent and ongoing import operations.
     * @throws ApiException if there are issues processing the request or communicating with the server.
     *         This includes network issues, server errors, or serialization issues with the request or response.
     */
    public ListImportsResponse listImports() throws ApiException {
        return listImports(100, null);
    }

    /**
     * <p>Lists all recent and ongoing import operations for the specified index based on limit.</p>
     *
     * <p>The method constructs a request to fetch a list of import operations, limited by the specified
     * maximum number of operations to return per page. The pagination token is set to null by default.</p>
     *
     *
     * <p>Example:
     *  <pre>{@code
     *     import org.openapitools.db_data.client.ApiException;
     *     import org.openapitools.db_data.client.model.ListImportsResponse;
     *
     *     ...
     *     int limit = 10;
     *     ListImportsResponse response = asyncIndex.listImports(limit);
     *  }</pre>
     *
     * @param limit The maximum number of operations to return per page. Default is 100.
     * @return {@link ListImportsResponse} containing the list of recent and ongoing import operations.
     * @throws ApiException if there are issues processing the request or communicating with the server.
     *         This includes network issues, server errors, or serialization issues with the request or response.
     */
    public ListImportsResponse listImports(Integer limit) throws ApiException {
        return listImports(limit, null);
    }

    /**
     * <p>Lists all recent and ongoing import operations for the specified index.</p>
     *
     * <p>The method constructs a request to fetch a list of import operations, limited by the specified
     * maximum number of operations to return per page. The pagination token allows for
     * deterministic pagination through the list of import operations.</p>
     *
     * <p>Example:
     *  <pre>{@code
     *     import org.openapitools.db_data.client.ApiException;
     *     import org.openapitools.db_data.client.model.ListImportsResponse;
     *
     *     ...
     *     int limit = 10;
     *     String paginationToken = "some-pagination-token";
     *     ListImportsResponse response = asyncIndex.listImports(limit, paginationToken);
     *  }</pre>
     *
     * @param limit The maximum number of operations to return per page. Default is 100.
     * @param paginationToken The token to continue a previous listing operation. Can be null or empty.
     * @return {@link ListImportsResponse} containing the list of recent and ongoing import operations.
     * @throws ApiException if there are issues processing the request or communicating with the server.
     *         This includes network issues, server errors, or serialization issues with the request or response.
     */
    public ListImportsResponse listImports(Integer limit, String paginationToken) throws ApiException {
        return bulkOperations.listBulkImports(limit, paginationToken);
    }

    /**
     * <p>Retrieves detailed information about a specific import operation using its unique identifier.</p>
     *
     * <p>The method constructs a request to fetch details of the specified import operation by its ID,
     * allowing users to monitor the status and results of the import process.</p>
     *
     * <p>Example:
     *  <pre>{@code
     *     import org.openapitools.db_data.client.ApiException;
     *     import org.openapitools.db_data.client.model.ImportModel;
     *
     *     ...
     *
     *     String importId = "1";
     *     ImportModel importDetails = asyncIndex.describeImport(importId);
     *  }</pre>
     *
     * @param id The unique identifier for the import operation.
     * @return {@link ImportModel} containing details of the specified import operation.
     * @throws ApiException if there are issues processing the request or communicating with the server.
     *         This includes network issues, server errors, or serialization issues with the request or response.
     */
    public ImportModel describeImport(String id) throws ApiException {
        return bulkOperations.describeBulkImport(id);
    }

    /**
     * <p>Attempts to cancel an ongoing import operation using its unique identifier.</p>
     *
     * <p>The method issues a request to cancel the specified import operation if it has not yet finished.
     * If the operation is already completed, the method has no effect.</p>
     *
     * <p>Example:
     *  <pre>{@code
     *     import org.openapitools.db_data.client.ApiException;
     *
     *     ...
     *     String importId = "2";
     *     asyncIndex.cancelImport(importId);
     *  }</pre>
     *
     * @param id The unique identifier for the import operation to cancel.
     * @throws ApiException if there are issues processing the request or communicating with the server.
     *         This includes network issues, server errors, or serialization issues with the request or response.
     */
    public void cancelImport(String id) throws ApiException {
        bulkOperations.cancelBulkImport(id);
    }

    /**
     * {@inheritDoc}
     * Closes the current index connection gracefully, releasing any resources associated with it. This method should
     * be called when the index instance is no longer needed, to ensure proper cleanup of network connections and other
     * resources. It closes both the connection to the Pinecone service identified by {@code indexName} and any internal
     * resources held by the {@link PineconeConnection} object.
     */
    @Override
    public void close() {
        Pinecone.closeConnection(indexName);
        connection.close();
    }
}
