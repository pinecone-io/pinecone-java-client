package io.pinecone.clients;

import com.google.protobuf.Struct;
import io.pinecone.commons.IndexInterface;
import io.pinecone.configs.PineconeConnection;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.proto.*;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;

import java.util.List;

/**
 * A client for interacting with a Pinecone index via GRPC synchronously. Allows for upserting, querying, fetching, updating, and deleting vectors.
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
     * @param connection The {@link PineconeConnection} configuration to be used for this index.
     * @param indexName The name of the index to interact with. The index host will be automatically resolved.
     * @throws PineconeValidationException if the connection object is null.
     */
    public Index(PineconeConnection connection, String indexName) {
        if (connection == null) {
            throw new PineconeValidationException("Pinecone connection object cannot be null.");
        }

        this.connection = connection;
        this.indexName = indexName;
        this.blockingStub = connection.getBlockingStub();
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
     * <p>Example</p>
     *  <pre>{@code
     *     import io.pinecone.proto.ListResponse;
     *
     *     ...
     *
     *     ListResponse listResponse = index.list("example-namespace");
     *  }</pre>
     */
    @Override
    public ListResponse list(String namespace) {
        validateListEndpointParameters(namespace, null, null, null, false, false, false);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setLimit(100).build();
        return blockingStub.list(listRequest);
    }

    /**
     * {@inheritDoc}
     * <p>Example</p>
     *  <pre>{@code
     *     import io.pinecone.proto.ListResponse;
     *
     *     ...
     *
     *     ListResponse listResponse = index.list("example-namespace", 10);
     *  }</pre>
     */
    @Override
    public ListResponse list(String namespace, Integer limit) {
        validateListEndpointParameters(namespace, null, null, limit, false, false, true);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setLimit(limit).build();
        return blockingStub.list(listRequest);
    }

    /**
     * {@inheritDoc}
     * <p>Example</p>
     *  <pre>{@code
     *     import io.pinecone.proto.ListResponse;
     *
     *     ...
     *
     *     ListResponse listResponse = index.list("example-namespace", "st-");
     *  }</pre>
     */
    @Override
    public ListResponse list(String namespace, String prefix) {
        validateListEndpointParameters(namespace, prefix, null, null, true, false, false);
        ListRequest listRequest =
                ListRequest.newBuilder().setNamespace(namespace).setPrefix(prefix).setLimit(100).build();
        return blockingStub.list(listRequest);
    }

    /**
     * {@inheritDoc}
     * <p>Example</p>
     *  <pre>{@code
     *     import io.pinecone.proto.ListResponse;
     *
     *     ...
     *
     *     ListResponse listResponse = index.list("example-namespace", "st-", 10);
     *  }</pre>
     */
    @Override
    public ListResponse list(String namespace, String prefix, Integer limit) {
        validateListEndpointParameters(namespace, prefix, null, limit, true, false, true);

        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setPrefix(prefix).
                setLimit(limit).build();
        return blockingStub.list(listRequest);
    }

    /**
     * {@inheritDoc}
     * <p>Example</p>
     *  <pre>{@code
     *     import io.pinecone.proto.ListResponse;
     *
     *     ...
     *
     *     ListResponse listResponse = index.list("example-namespace", "st-", "some-pagToken");
     *  }</pre>
     */
    @Override
    public ListResponse list(String namespace, String prefix, String paginationToken) {
        validateListEndpointParameters(namespace, prefix, paginationToken, null, true, true, false);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setPrefix(prefix).
                setPaginationToken(paginationToken).setLimit(100).build();
        return blockingStub.list(listRequest);
    }

    /**
     * Base method that retrieves a list of vector IDs from a specific namespace within an index.
     *
     * <p>The method internally constructs a {@link ListRequest} using the provided namespace
     * and the provided limit, which cuts off the response after the specified number of IDs. It filters the
     * retrieve IDs to match a provided prefix. It also can accept a pagination token to deterministically paginate
     * through a list of vector IDs. It then makes a synchronous RPC call to fetch the list of vector IDs.</p>
     *
     * <p>Example</p>
     *  <pre>{@code
     *     import io.pinecone.proto.ListResponse;
     *
     *     ...
     *
     *     ListResponse listResponse = index.list("example-namespace", "st-", "some-pagToken", 10);
     *  * }</pre>
     *
     * @param namespace The namespace that holds the vector IDs you want to retrieve. Cannot be {@code null} or empty.
     * @param prefix The prefix with which vector IDs must start to be included in the response.
     * @param paginationToken The token to paginate through the list of vector IDs.
     * @param limit The maximum number of vector IDs you want to retrieve.
     * @return {@link ListResponse} containing the list of vector IDs fetched from the specified namespace.
     *         The response includes vector IDs up to 100 items.
     * @throws IllegalArgumentException if the namespace parameter is {@code null} or empty, as validated
     *         by {@link #validateListEndpointParameters}.
     * @throws RuntimeException if there are issues processing the request or communicating with the server.
     *         This includes network issues, server errors, or serialization issues with the request or response.
     */
    public ListResponse list(String namespace, String prefix, String paginationToken, Integer limit) {
        validateListEndpointParameters(namespace, prefix, paginationToken, limit, true, true, true);
        ListRequest listRequest = ListRequest.newBuilder().setNamespace(namespace).setPrefix(prefix).
                setPaginationToken(paginationToken).setLimit(limit).build();
        return blockingStub.list(listRequest);
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
