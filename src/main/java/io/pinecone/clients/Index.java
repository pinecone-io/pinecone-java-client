package io.pinecone.clients;

import com.google.protobuf.Struct;
import io.pinecone.commons.IndexInterface;
import io.pinecone.configs.PineconeConfig;
import io.pinecone.configs.PineconeConnection;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.proto.*;
import io.pinecone.proto.DeleteRequest;
import io.pinecone.proto.DescribeIndexStatsRequest;
import io.pinecone.proto.FetchResponse;
import io.pinecone.proto.ListResponse;
import io.pinecone.proto.QueryRequest;
import io.pinecone.proto.UpdateRequest;
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
import java.util.List;
import java.util.Map;

import static io.pinecone.clients.Pinecone.buildOkHttpClient;

/**
 * A client for interacting with a Pinecone index synchronously. Allows for vector operations such as upserting,
 * querying, fetching, updating, and deleting vectors along with records operations such as upsert and search records.
 * This class provides a direct interface to interact with a specific index, encapsulating network communication and request validation.
 * <p>
 * Example:
 * <pre>{@code
 *     import io.pinecone.clients.Pinecone;
 *     import io.pinecone.clients.Index;
 *
 *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
 *     Index index = client.getIndexConnection("my-index");
 * }</pre>
 */
public class Index implements IndexInterface<UpsertResponse,
        QueryResponseWithUnsignedIndices,
        FetchResponse,
        UpdateResponse,
        DeleteResponse,
        DescribeIndexStatsResponse,
        ListResponse> {

    private final PineconeConnection connection;
    private final String indexName;
    private final VectorServiceGrpc.VectorServiceBlockingStub blockingStub;
    private final VectorOperationsApi vectorOperations;

    /**
     * Constructs an {@link Index} instance for interacting with a Pinecone index.
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.clients.Pinecone;
     *     import io.pinecone.clients.Index;
     *
     *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
     *     Index index = client.getIndexConnection("my-index");
     * }</pre>
     *
     * @param config     The {@link PineconeConfig} configuration of the index.
     * @param connection The {@link PineconeConnection} configuration to be used for this index.
     * @param indexName  The name of the index to interact with. The index host will be automatically resolved.
     * @throws PineconeValidationException if the connection object is null.
     */
    public Index(PineconeConfig config, PineconeConnection connection, String indexName) {
        if (connection == null) {
            throw new PineconeValidationException("Pinecone connection object cannot be null.");
        }

        this.connection = connection;
        this.indexName = indexName;
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
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.UpsertResponse;
     *     import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
     *     import static io.pinecone.commons.IndexInterface.buildUpsertVectorWithUnsignedIndices;
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
     *     List<VectorWithUnsignedIndices> vectors = new ArrayList<>(3);
     *
     *     for (int i=0; i<upsertIds.size(); i++) {
     *         vectors.add(
     *             buildUpsertVectorWithUnsignedIndices(upsertIds.get(i),
     *                 values.get(i), null, null, null));
     *     }
     *
     *     UpsertResponse upsertResponse = index.upsert(vectors, "example-namespace");
     * }</pre>
     */
    @Override
    public UpsertResponse upsert(List<VectorWithUnsignedIndices> vectorList,
                                 String namespace) {
        UpsertRequest upsertRequest = validateUpsertRequest(vectorList, namespace);
        return blockingStub.upsert(upsertRequest);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     index.upsert("my-vector-id", Arrays.asList(1.0f, 2.0f, 3.0f));
     * }</pre>
     */
    @Override
    public UpsertResponse upsert(String id,
                                 List<Float> values) {
        return upsert(id, values, null, null, null, null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.UpsertResponse;
     *
     *     ...
     *
     *     UpsertResponse upsertResponse =
     *     index.upsert("my-vector-id",
     *         Arrays.asList(1.0f, 2.0f, 3.0f),
     *         "example-namespace");
     * }</pre>
     */
    @Override
    public UpsertResponse upsert(String id,
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
     *
     *     ...
     *
     *     // metadata
     *     Struct metadataStruct = Struct.newBuilder()
     *         .putFields("genre", Value.newBuilder().setStringValue("action").build())
     *         .putFields("year", Value.newBuilder().setNumberValue(2019).build())
     *         .build();
     *
     *     UpsertResponse upsertResponse =
     *     index.upsert("my-vector-id",
     *         Arrays.asList(1.0f, 2.0f, 3.0f),
     *         Arrays.asList(1L, 2L, 3L),
     *         Arrays.asList(1000f, 2000f, 3000f),
     *         metadataStruct,
     *         "example-namespace");
     * }</pre>
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
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
     *
     *     ...
     *
     *     QueryResponseWithUnsignedIndices queryResponse =
     *         index.query(10,
     *             Arrays.asList(1.0f, 2.0f, 3.0f),
     *             Arrays.asList(1L, 2L, 3L),
     *             Arrays.asList(1000f, 2000f, 3000f),
     *             null,
     *             "example-namespace",
     *             null,
     *             true,
     *             true);
     * }</pre>
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
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
     *
     *     ...
     *
     *     QueryResponseWithUnsignedIndices queryResponse =
     *         index.queryByVectorId(10,
     *             "my-vector-id",
     *             "example-namespace",
     *             null,
     *             true,
     *             true);
     * }</pre>
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
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
     *     import com.google.protobuf.Struct;
     *
     *     ...
     *
     *     Struct filter = Struct.newBuilder()
     *         .putFields("genre", Value.newBuilder().setStringValue("action").build()).build();
     *     QueryResponseWithUnsignedIndices queryResponse =
     *         index.queryByVectorId(10,
     *             "my-vector-id",
     *             "example-namespace",
     *             filter);
     * }</pre>
     */
    @Override
    public QueryResponseWithUnsignedIndices queryByVectorId(int topK,
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
     *
     *     ...
     *
     *     QueryResponseWithUnsignedIndices queryResponse =
     *     index.queryByVectorId(10,
     *         "my-vector-id",
     *         "example-namespace",
     *         true,
     *         true);
     * }</pre>
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
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
     *
     *     ...
     *
     *     QueryResponseWithUnsignedIndices queryResponse =
     *     index.queryByVectorId(10,
     *         "my-vector-id",
     *         "example-namespace");
     * }</pre>
     */
    @Override
    public QueryResponseWithUnsignedIndices queryByVectorId(int topK,
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
     *
     *     ...
     *
     *     QueryResponseWithUnsignedIndices queryResponse =
     *     index.queryByVectorId(10,
     *         "my-vector-id",
     *         true,
     *         true);
     * }</pre>
     */
    @Override
    public QueryResponseWithUnsignedIndices queryByVectorId(int topK,
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
     *
     *     ...
     *
     *     QueryResponseWithUnsignedIndices queryResponse =
     *     index.queryByVectorId(10,
     *         "my-vector-id");
     * }</pre>
     */
    @Override
    public QueryResponseWithUnsignedIndices queryByVectorId(int topK,
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
     *
     *     ...
     *
     *     Struct filter = Struct.newBuilder()
     *         .putFields("genre", Value.newBuilder().setStringValue("action").build()).build();
     *     QueryResponseWithUnsignedIndices queryResponse =
     *     index.queryByVector(10,
     *         Arrays.asList(1.0f, 2.0f, 3.0f),
     *         "example-namespace",
     *         filter,
     *         true,
     *         true);
     * }</pre>
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
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
     *     import com.google.protobuf.Struct;
     *
     *     ...
     *
     *     Struct filter = Struct.newBuilder()
     *         .putFields("genre", Value.newBuilder().setStringValue("action").build()).build();
     *     QueryResponseWithUnsignedIndices queryResponse =
     *     index.queryByVector(10,
     *         Arrays.asList(1.0f, 2.0f, 3.0f),
     *         "example-namespace",
     *         filter);
     * }</pre>
     */
    @Override
    public QueryResponseWithUnsignedIndices queryByVector(int topK,
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
     *
     *     ...
     *
     *     QueryResponseWithUnsignedIndices queryResponse =
     *     index.queryByVector(10,
     *         Arrays.asList(1.0f, 2.0f, 3.0f),
     *         "example-namespace",
     *         true,
     *         true);
     * }</pre>
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
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
     *
     *     ...
     *
     *     QueryResponseWithUnsignedIndices queryResponse =
     *     index.queryByVector(10,
     *         Arrays.asList(1.0f, 2.0f, 3.0f),
     *         "example-namespace");
     * }</pre>
     */
    @Override
    public QueryResponseWithUnsignedIndices queryByVector(int topK,
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
     *
     *     ...
     *
     *     QueryResponseWithUnsignedIndices queryResponse =
     *     index.queryByVector(10,
     *         Arrays.asList(1.0f, 2.0f, 3.0f),
     *         true,
     *         true);
     * }</pre>
     */
    @Override
    public QueryResponseWithUnsignedIndices queryByVector(int topK,
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
     *
     *     ...
     *
     *     QueryResponseWithUnsignedIndices queryResponse =
     *     index.queryByVector(10, Arrays.asList(1.0f, 2.0f, 3.0f));
     * }</pre>
     */
    @Override
    public QueryResponseWithUnsignedIndices queryByVector(int topK,
                                                          List<Float> vector) {
        return query(topK, vector, null, null, null, null, null, false, false);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.FetchResponse;
     *
     *     ...
     *
     *     FetchResponse fetchResponse = index.fetch(Arrays.asList("v1", "v2", "v3"));
     * }</pre>
     */
    @Override
    public FetchResponse fetch(List<String> ids) {
        return fetch(ids, null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.FetchResponse;
     *
     *     ...
     *
     *     FetchResponse fetchResponse = index.fetch(Arrays.asList("v1", "v2", "v3"), "example-namespace");
     * }</pre>
     */
    @Override
    public FetchResponse fetch(List<String> ids,
                               String namespace) {
        FetchRequest fetchRequest = validateFetchRequest(ids, namespace);

        return blockingStub.fetch(fetchRequest);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.UpdateResponse;
     *
     *     ...
     *
     *     UpdateResponse updateResponse = index.update("my-vector-id", Arrays.asList(1.0f, 2.0f, 3.0f));
     * }</pre>
     */
    @Override
    public UpdateResponse update(String id,
                                 List<Float> values) {
        return update(id, values, null, null, null, null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.UpdateResponse;
     *
     *     ...
     *
     *     UpdateResponse updateResponse =
     *     index.update("my-vector-id",
     *         Arrays.asList(1.0f, 2.0f, 3.0f),
     *         "example-namespace");
     * }</pre>
     */
    @Override
    public UpdateResponse update(String id,
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
     *
     *     ...
     *
     *     Struct metadata = Struct.newBuilder()
     *         .putFields("genre", Value.newBuilder().setStringValue("action").build())
     *         .putFields("year", Value.newBuilder().setNumberValue(2019).build())
     *         .build();
     *
     *     UpdateResponse updateResponse =
     *     index.update("my-vector-id",
     *         Arrays.asList(1.0f, 2.0f, 3.0f),
     *         metadata,
     *         "example-namespace",
     *         Arrays.asList(1L, 2L, 3L),
     *         Arrays.asList(1000f, 2000f, 3000f));
     * }</pre>
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
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.DeleteResponse;
     *     import com.google.protobuf.Struct;
     *
     *     ...
     *
     *     DeleteResponse deleteResponse =
     *     index.deleteByIds(Arrays.asList("v1", "v2", "v3"), "example-namespace");
     * }</pre>
     */
    @Override
    public DeleteResponse deleteByIds(List<String> ids, String namespace) {
        return delete(ids, false, namespace, null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.DeleteResponse;
     *     import com.google.protobuf.Struct;
     *
     *     ...
     *
     *     DeleteResponse deleteResponse = index.deleteByIds(Arrays.asList("v1", "v2", "v3"));
     * }</pre>
     */
    @Override
    public DeleteResponse deleteByIds(List<String> ids) {
        return delete(ids, false, null, null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.DeleteResponse;
     *     import com.google.protobuf.Struct;
     *
     *     ...
     *
     *     Struct filter = Struct.newBuilder()
     *         .putFields("genre", Value.newBuilder().setStringValue("action").build()).build();
     *     DeleteResponse deleteResponse = index.deleteByFilter(filter, "example-namespace");
     * }</pre>
     */
    @Override
    public DeleteResponse deleteByFilter(Struct filter, String namespace) {
        return delete(null, false, namespace, filter);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.DeleteResponse;
     *     import com.google.protobuf.Struct;
     *
     *     ...
     *
     *     Struct filter = Struct.newBuilder()
     *         .putFields("genre", Value.newBuilder().setStringValue("action").build()).build();
     *     DeleteResponse deleteResponse = index.deleteByFilter(filter);
     * }</pre>
     */
    @Override
    public DeleteResponse deleteByFilter(Struct filter) {
        return delete(null, false, null, filter);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.DeleteResponse;
     *
     *     ...
     *
     *     DeleteResponse deleteResponse = index.deleteAll("example-namespace");
     * }</pre>
     */
    @Override
    public DeleteResponse deleteAll(String namespace) {
        return delete(null, true, namespace, null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.DeleteResponse;
     *
     *     ...
     *
     *     DeleteResponse deleteResponse =
     *     index.delete(Arrays.asList("v1", "v2", "v3"),false,"example-namespace",null);
     * }</pre>
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
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.DescribeIndexStatsResponse;
     *
     *     ...
     *
     *     DescribeIndexStatsResponse describeIndexStatsResponse = index.describeIndexStats();
     * }</pre>
     */
    @Override
    public DescribeIndexStatsResponse describeIndexStats() {
        DescribeIndexStatsRequest describeIndexStatsRequest = validateDescribeIndexStatsRequest(null);

        return blockingStub.describeIndexStats(describeIndexStatsRequest);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Example:
     * <pre>{@code
     *     import io.pinecone.proto.DescribeIndexStatsResponse;
     *     import com.google.protobuf.Struct;
     *
     *     ...
     *
     *     Struct filter = Struct.newBuilder()
     *         .putFields("genre", Value.newBuilder().setStringValue("action").build()).build();
     *     DescribeIndexStatsResponse describeIndexStatsResponse = index.describeIndexStats();
     * }</pre>
     */
    @Override
    public DescribeIndexStatsResponse describeIndexStats(Struct filter) {
        DescribeIndexStatsRequest describeIndexStatsRequest = validateDescribeIndexStatsRequest(filter);

        return blockingStub.describeIndexStats(describeIndexStatsRequest);
    }

    /**
     * {@inheritDoc}
     * <p> Example:
     * <pre>{@code
     *     import io.pinecone.proto.ListResponse;
     *
     *     ...
     *
     *     ListResponse listResponse = index.list();
     *  }</pre>
     */
    @Override
    public ListResponse list() {
        validateListEndpointParameters(null, null, null, null, false, false, false, false);
        ListRequest listRequest = ListRequest.newBuilder().build();
        return blockingStub.list(listRequest);
    }


    /**
     * {@inheritDoc}
     * <p>Example:
     * <pre>{@code
     *     import io.pinecone.proto.ListResponse;
     *
     *     ...
     *
     *     ListResponse listResponse = index.list("example-namespace");
     *  }</pre>
     */
    @Override
    public ListResponse list(String namespace) {
        validateListEndpointParameters(namespace, null, null, null, true, false, false, false);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).build();
        return blockingStub.list(listRequest);
    }

    /**
     * {@inheritDoc}
     * <p>Example:
     * <pre>{@code
     *     import io.pinecone.proto.ListResponse;
     *
     *     ...
     *
     *     ListResponse listResponse = index.list("example-namespace", 10, "some-pagToken");
     *  }</pre>
     */
    @Override
    public ListResponse list(String namespace, int limit, String paginationToken) {
        validateListEndpointParameters(namespace, null, paginationToken, limit, true, false, true, true);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setLimit(limit).setPaginationToken(paginationToken).build();
        return blockingStub.list(listRequest);
    }

    /**
     * {@inheritDoc}
     * <p>Example:
     * <pre>{@code
     *     import io.pinecone.proto.ListResponse;
     *
     *     ...
     *
     *     ListResponse listResponse = index.list("example-namespace", 10);
     *  }</pre>
     */
    @Override
    public ListResponse list(String namespace, int limit) {
        validateListEndpointParameters(namespace, null, null, limit, true, false, false, true);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setLimit(limit).build();
        return blockingStub.list(listRequest);
    }

    /**
     * {@inheritDoc}
     * <p>Example:
     * <pre>{@code
     *     import io.pinecone.proto.ListResponse;
     *
     *     ...
     *
     *     ListResponse listResponse = index.list("example-namespace", "st-");
     *  }</pre>
     */
    @Override
    public ListResponse list(String namespace, String prefix) {
        validateListEndpointParameters(namespace, prefix, null, null, true, true, false, false);
        ListRequest listRequest =
                ListRequest.newBuilder().setNamespace(namespace).setPrefix(prefix).build();
        return blockingStub.list(listRequest);
    }

    /**
     * {@inheritDoc}
     * <p> Example:
     * <pre>{@code
     *     import io.pinecone.proto.ListResponse;
     *
     *     ...
     *
     *     ListResponse listResponse = index.list("example-namespace", "st-", 10);
     *  }</pre>
     */
    @Override
    public ListResponse list(String namespace, String prefix, int limit) {
        validateListEndpointParameters(namespace, prefix, null, limit, true, true, false, true);

        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setPrefix(prefix).
                setLimit(limit).build();
        return blockingStub.list(listRequest);
    }

    /**
     * {@inheritDoc}
     * <p>Example:
     * <pre>{@code
     *     import io.pinecone.proto.ListResponse;
     *
     *     ...
     *
     *     ListResponse listResponse = index.list("example-namespace", "st-", "some-pagToken");
     *  }</pre>
     */
    @Override
    public ListResponse list(String namespace, String prefix, String paginationToken) {
        validateListEndpointParameters(namespace, prefix, paginationToken, null, true, true, true, false);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setPrefix(prefix).
                setPaginationToken(paginationToken).build();
        return blockingStub.list(listRequest);
    }

    /**
     * <p>Base method that retrieves a list of vector IDs from a specific namespace within an index.
     *
     * <p>The method internally constructs a {@link ListRequest} using the provided namespace
     * and the provided limit, which cuts off the response after the specified number of IDs. It filters the
     * retrieve IDs to match a provided prefix. It also can accept a pagination token to deterministically paginate
     * through a list of vector IDs. It then makes a synchronous RPC call to fetch the list of vector IDs.
     *
     * <p>Example:
     * <pre>{@code
     *     import io.pinecone.proto.ListResponse;
     *
     *     ...
     *
     *     ListResponse listResponse = index.list("example-namespace", "st-", "some-pagToken", 10);
     *   }</pre>
     *
     * @param namespace       The namespace that holds the vector IDs you want to retrieve. If namespace is not specified,
     *                        the default namespace is used.
     * @param prefix          The prefix with which vector IDs must start to be included in the response.
     * @param paginationToken The token to paginate through the list of vector IDs.
     * @param limit           The maximum number of vector IDs you want to retrieve.
     * @return {@link ListResponse} containing the list of vector IDs fetched from the specified namespace.
     * The response includes vector IDs up to {@code 100} items.
     * @throws RuntimeException if there are issues processing the request or communicating with the server.
     *                          This includes network issues, server errors, or serialization issues with the request or response.
     */
    public ListResponse list(String namespace, String prefix, String paginationToken, int limit) {
        validateListEndpointParameters(namespace, prefix, paginationToken, limit, true, true, true, true);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setPrefix(prefix).
                setPaginationToken(paginationToken).setLimit(limit).build();
        return blockingStub.list(listRequest);
    }

    /**
     * <p>Upserts records into a specified namespace within a Pinecone index. This operation
     * will insert new records or update existing ones based on the provided data.</p>
     *
     * <p>The method sends a list of {@link UpsertRecord} objects to the specified namespace
     * in the Pinecone index, either inserting new records or updating existing records
     * depending on whether the record IDs already exist.</p>
     *
     * <p>Example:
     * <pre>{@code
     *     List<UpsertRecord> records = new ArrayList<>();
     *     records.add(new UpsertRecord("rec1", "Apple's first product, the Apple I, was released in 1976.", "product"));
     *     records.add(new UpsertRecord("rec2", "Apples are a great source of dietary fiber.", "nutrition"));
     *
     *     try {
     *         index.upsertRecords("example-namespace", records);
     *     } catch (ApiException e) {
     *
     *     }
     * }</pre></p>
     *
     * @param namespace    The namespace within the Pinecone index where the records will be upserted.
     *                     The namespace must be an existing namespace or a valid one to create new records.
     * @param upsertRecords A list of Map<String, String> containing the records to be upserted.
     *                     Each record must include a unique ID represented by the key "_id" along with the data to be
     *                     stored.
     * @throws ApiException If there is an issue with the upsert operation. This could include network errors,
     *                      invalid input data, or issues communicating with the Pinecone service.
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
        vectorOperations.upsertRecordsNamespace(namespace, records);
    }

    /**
     * <p>Searches for records in a specified namespace within a Pinecone index by converting a query into a vector embedding.
     * Optionally, a reranking operation can be applied to refine the results.</p>
     *
     * <p>This method sends a search query along with specified fields to the Pinecone index, retrieves the relevant records,
     * and applies an optional reranking operation if provided.</p>
     *
     * <p>Example:
     * <pre>{@code
     *     String namespace = "example-namespace";
     *     HashMap<String, String> inputsMap = new HashMap<>();
     *     inputsMap.put("text", "Disease prevention");
     *     SearchRecordsRequestQuery query = new SearchRecordsRequestQuery()
     *             .topK(3)
     *             .inputs(inputsMap);
     *
     *     List<String> fields = new ArrayList<>();
     *     fields.add("category");
     *     fields.add("chunk_text");
     *
     *     SearchRecordsResponse recordsResponse = index.searchRecords(namespace, query, fields, null);
     * }</pre></p>
     *
     * @param namespace The namespace within the Pinecone index where the search will be performed.
     *                  The namespace must exist and contain records to search through.
     * @param query The query to be converted into a vector embedding for the search operation.
     *              This query contains the input data for the search and parameters like topK for result limits.
     * @param fields A list of fields to be searched within the records. These fields define which parts of the records
     *               are considered during the search.
     * @param rerank (Optional) A reranking operation that can be applied to refine or reorder the search results.
     *               Pass null if no reranking is required.
     * @return A {@link SearchRecordsResponse} object containing the search results, including the top matching records.
     * @throws ApiException If there is an issue with the search operation. This could include network errors,
     *                      invalid input data, or issues communicating with the Pinecone service.
     */
    public SearchRecordsResponse searchRecords(String namespace,
                                               SearchRecordsRequestQuery query,
                                               List<String> fields,
                                               SearchRecordsRequestRerank rerank) throws ApiException {
        SearchRecordsRequest request = new SearchRecordsRequest()
                .query(query)
                .fields(fields)
                .rerank(rerank);

        return vectorOperations.searchRecordsNamespace(namespace, request);
    }

    /**
     * <p>Searches for records by a specific record ID within a Pinecone index. The search query will use the ID as a
     * filter.</p>
     *
     * <p>This method retrieves records from the Pinecone index by searching for a specific record ID.
     * You can optionally apply a filter, limit the number of results with the topK parameter,
     * and refine the results using an optional reranking operation.</p>
     *
     * <p>Example:
     * <pre>{@code
     *     String id = "12345";
     *     String namespace = "example-namespace";
     *     List<String> fields = new ArrayList<>();
     *     fields.add("category");
     *     fields.add("chunk_text");
     *     int topK = 3;
     *
     *     SearchRecordsResponse recordsResponse = index.searchRecordsById(id, namespace, fields, topK, null, null);
     * }</pre></p>
     *
     * @param id The ID of the record to be searched within the Pinecone index.
     *           The ID must exist within the specified namespace for a valid search.
     * @param namespace The namespace within the Pinecone index where the search will be performed.
     *                  The namespace must exist and contain records to search through.
     * @param fields A list of fields to be searched within the records. These fields define which parts of the records
     *               are considered during the search.
     * @param topK The maximum number of results to be returned by the search.
     * @param filter (Optional) A filter to apply to the search query. It can be used to narrow down the search
     *               based on specific criteria.
     * @param rerank (Optional) A reranking operation that can be applied to refine or reorder the search results.
     *               Pass null if no reranking is required.
     * @return A {@link SearchRecordsResponse} object containing the search results, including the top matching records.
     * @throws ApiException If there is an issue with the search operation. This could include network errors,
     *                      invalid input data, or issues communicating with the Pinecone service.
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

        return vectorOperations.searchRecordsNamespace(namespace, request);
    }

    /**
     * <p>Searches for records in a Pinecone index by a vector. The vector represents the search query in vector space.</p>
     *
     * <p>This method converts the given vector into a query and searches for the most relevant records within the Pinecone index.
     * You can limit the results with the topK parameter, apply a filter, and optionally apply a reranking operation.</p>
     *
     * <p>The {@link SearchRecordsVector} class represents a vector in the query. It contains the following optional fields:
     * - `values`: A list of floats representing the dense vector values. If this field is provided, it is used for the search.
     * - `sparseValues`: An optional list of non-zero values for sparse vectors. If provided, the search will use sparse vector representation.
     * - `sparseIndices`: An optional list of indices corresponding to the non-zero values in the sparse vector. If provided along with `sparseValues`, it will enable sparse vector search.</p>
     *
     * <p>Example:
     * <pre>{@code
     *     SearchRecordsVector vector = new SearchRecordsVector();
     *     vector.setValues(Arrays.asList(1.0f, 2.0f, 3.0f)); // Dense vector values
     *     // Or, for sparse vectors:
     *     // vector.setSparseValues(Arrays.asList(1.0f, 2.0f));
     *     // vector.setSparseIndices(Arrays.asList(0, 2));
     *     String namespace = "example-namespace";
     *     List<String> fields = new ArrayList<>();
     *     fields.add("category");
     *     fields.add("chunk_text");
     *     int topK = 3;
     *
     *     SearchRecordsResponse recordsResponse = index.searchRecordsByVector(vector, namespace, fields, topK, null, null);
     * }</pre></p>
     *
     * @param vector The vector representing the search query, which is used to find the closest matching records.
     *              The vector can be a dense vector (via `values`) or a sparse vector (via `sparseValues` and `sparseIndices`).
     *              All fields in the vector are optional. If none are provided, the search will fail due to missing query data.
     * @param namespace The namespace within the Pinecone index where the search will be performed.
     *                  The namespace must exist and contain records to search through.
     * @param fields A list of fields to be searched within the records. These fields define which parts of the records
     *               are considered during the search.
     * @param topK The maximum number of results to be returned by the search.
     * @param filter (Optional) A filter to apply to the search query. It can be used to narrow down the search
     *               based on specific criteria.
     * @param rerank (Optional) A reranking operation that can be applied to refine or reorder the search results.
     *               Pass null if no reranking is required.
     * @return A {@link SearchRecordsResponse} object containing the search results, including the top matching records.
     * @throws ApiException If there is an issue with the search operation. This could include network errors,
     *                      invalid input data, or issues communicating with the Pinecone service.
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

        return vectorOperations.searchRecordsNamespace(namespace, request);
    }

    /**
     * <p>Searches for records in a Pinecone index using a text query. The text is converted into a vector for the search operation.</p>
     *
     * <p>This method converts the given text into a vector representation and performs a search within the Pinecone index.
     * You can limit the results with the topK parameter, apply a filter, and optionally apply a reranking operation.</p>
     *
     * <p>Example:
     * <pre>{@code
     *     String text = "Disease prevention";
     *     String namespace = "example-namespace";
     *     List<String> fields = new ArrayList<>();
     *     fields.add("category");
     *     fields.add("chunk_text");
     *     int topK = 3;
     *
     *     SearchRecordsResponse recordsResponse = index.searchRecordsByText(text, namespace, fields, topK, null, null);
     * }</pre></p>
     *
     * @param text The text used in the query for searching.
     * @param namespace The namespace within the Pinecone index where the search will be performed.
     *                  The namespace must exist and contain records to search through.
     * @param fields A list of fields to be searched within the records. These fields define which parts of the records
     *               are considered during the search.
     * @param topK The maximum number of results to be returned by the search.
     * @param filter (Optional) A filter to apply to the search query. It can be used to narrow down the search
     *               based on specific criteria.
     * @param rerank (Optional) A reranking operation that can be applied to refine or reorder the search results.
     *               Pass null if no reranking is required.
     * @return A {@link SearchRecordsResponse} object containing the search results, including the top matching records.
     * @throws ApiException If there is an issue with the search operation. This could include network errors,
     *                      invalid input data, or issues communicating with the Pinecone service.
     */
    public SearchRecordsResponse searchRecordsByText(String text,
                                    String namespace,
                                    List<String> fields,
                                    int topK,
                                    Map<String, Object>  filter,
                                    SearchRecordsRequestRerank rerank) throws ApiException {
        SearchRecordsRequestQuery query = new SearchRecordsRequestQuery()
                .inputs(text)
                .topK(topK)
                .filter(filter);

        SearchRecordsRequest request = new SearchRecordsRequest()
                .query(query)
                .fields(fields)
                .rerank(rerank);

        return vectorOperations.searchRecordsNamespace(namespace, request);
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
