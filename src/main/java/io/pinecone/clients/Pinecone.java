package io.pinecone.clients;

import io.pinecone.configs.PineconeConfig;
import io.pinecone.configs.PineconeConnection;
import io.pinecone.configs.ProxyConfig;
import io.pinecone.configs.ResponseMetadataListener;
import io.pinecone.exceptions.*;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.openapitools.db_control.client.ApiClient;
import org.openapitools.db_control.client.ApiException;
import org.openapitools.db_control.client.Configuration;
import org.openapitools.db_control.client.api.ManageIndexesApi;
import org.openapitools.db_control.client.model.*;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The control-plane client: creates, configures, describes and deletes indexes, collections and backups, and hands
 * out the data-plane {@link Index} and {@link AsyncIndex} connections and the {@link Inference} client.
 * <p>
 * Build one with {@link Pinecone.Builder} and reuse it for the life of your application. Each instance holds the
 * HTTP client for its control-plane requests. Repeated calls to {@link #getIndexConnection(String)} for the same
 * index reuse one gRPC channel, which is also shared with any other {@code Pinecone} instance in the JVM that has the
 * same API key, source tag, TLS setting and response metadata listener. The channel shuts down when the last
 * {@link Index} or {@link AsyncIndex} using it is closed. Each call to
 * {@link #getInferenceClient()} builds a new {@link Inference} client, so call it once and keep the result.
 *
 * <pre>{@code
 * import io.pinecone.clients.Index;
 * import io.pinecone.clients.Pinecone;
 * import java.util.Arrays;
 *
 * Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
 *
 * Index index = pinecone.getIndexConnection("product-catalog");
 * index.upsert("sku-1001", Arrays.asList(0.12f, 0.48f, 0.91f));
 * }</pre>
 */
public class Pinecone {

    // Keyed by every setting a PineconeConnection captures, so only clients with identical settings share a channel.
    private static final Map<List<Object>, SharedConnection> connectionsMap = new HashMap<>();
    private final ManageIndexesApi manageIndexesApi;
    private final PineconeConfig config;

    Pinecone(PineconeConfig config, ManageIndexesApi manageIndexesApi) {
        this.config = config;
        this.manageIndexesApi = manageIndexesApi;
    }

    PineconeConfig getConfig() {
        return config;
    }

    /**
     * Creates a serverless index for dense vectors and returns before the index is ready.
     * <p>
     * Use this overload for most serverless indexes. Use
     * {@link #createServerlessIndex(String, String, int, String, String, String, Map, ReadCapacity, BackupModelSchema)
     * the nine-argument overload} to set dedicated read capacity or limit which metadata fields are indexed,
     * {@link #createSparseServelessIndex(String, String, String, String, Map, String)} for sparse vectors, and
     * {@link #createIndexForModel(String, String, String, CreateIndexForModelRequestEmbed, String, Map)} to have
     * Pinecone embed your text. The index is not ready for reads and writes when this method returns: poll
     * {@link #describeIndex(String)} until its status reports ready.
     *
     * <pre>{@code
     * IndexModel index = pinecone.createServerlessIndex(
     *     "product-catalog", "cosine", 1536, "aws", "us-east-1", "disabled", null);
     * }</pre>
     *
     * @param indexName Name of the new index, for example {@code "product-catalog"}. Must be unique in the project.
     * @param metric Similarity metric: {@code "cosine"}, {@code "euclidean"} or {@code "dotproduct"}. Null or empty
     *               means {@code "cosine"}.
     * @param dimension Length of the vectors you will store, matching your embedding model's output, for example
     *                  {@code 1536}.
     * @param cloud Cloud provider that hosts the index, for example {@code "aws"}.
     * @param region Region in that cloud, for example {@code "us-east-1"}.
     * @param deletionProtection {@code "enabled"} to block {@link #deleteIndex(String)} until you disable it with
     *                           {@link #configureServerlessIndex(String, String, Map, ConfigureIndexRequestEmbed)};
     *                           {@code "disabled"} or null to allow deletion.
     * @param tags Key-value labels for the index, for example {@code "team" -> "search"}. Null or empty sends no
     *             tags.
     * @return the new index's {@link IndexModel}; its {@code getHost()} is the host data-plane connections use, and
     *         {@code getStatus()} reports whether the index is ready.
     * @throws PineconeValidationException if the name, cloud or region is null or empty, or the dimension is less
     *         than 1. The client checks these before contacting the server.
     * @throws PineconeBadRequestException if the server rejects the request, for example an index name with
     *         uppercase letters.
     * @throws PineconeNotFoundException if the cloud and region pair does not exist.
     */
    public IndexModel createServerlessIndex(String indexName,
                                            String metric,
                                            int dimension,
                                            String cloud,
                                            String region,
                                            String deletionProtection,
                                            Map<String, String> tags) throws PineconeException {
        return createServerlessIndex(indexName, metric, dimension, cloud, region, deletionProtection, tags, null, null);
    }

    /**
     * Creates a serverless index for dense vectors with optional read capacity and metadata schema settings, and
     * returns before the index is ready.
     * <p>
     * Use this overload when you need dedicated read nodes or want to index only the metadata fields you filter on;
     * otherwise use {@link #createServerlessIndex(String, String, int, String, String, String, Map) the seven-argument
     * overload}. The index is not ready for reads and writes when this method returns: poll
     * {@link #describeIndex(String)} until its status reports ready.
     *
     * <pre>{@code
     * import org.openapitools.db_control.client.model.BackupModelSchema;
     * import org.openapitools.db_control.client.model.BackupModelSchemaFieldsValue;
     * import java.util.HashMap;
     * import java.util.Map;
     *
     * Map<String, BackupModelSchemaFieldsValue> fields = new HashMap<>();
     * fields.put("category", new BackupModelSchemaFieldsValue().filterable(true));
     * fields.put("price", new BackupModelSchemaFieldsValue().filterable(true));
     * BackupModelSchema schema = new BackupModelSchema().fields(fields);
     *
     * IndexModel index = pinecone.createServerlessIndex(
     *     "product-catalog", "cosine", 1536, "aws", "us-east-1", "disabled", null, null, schema);
     * }</pre>
     *
     * @param indexName Name of the new index, for example {@code "product-catalog"}. Must be unique in the project.
     * @param metric Similarity metric: {@code "cosine"}, {@code "euclidean"} or {@code "dotproduct"}. Null or empty
     *               means {@code "cosine"}.
     * @param dimension Length of the vectors you will store, matching your embedding model's output, for example
     *                  {@code 1536}.
     * @param cloud Cloud provider that hosts the index, for example {@code "aws"}.
     * @param region Region in that cloud, for example {@code "us-east-1"}.
     * @param deletionProtection {@code "enabled"} to block {@link #deleteIndex(String)} until you disable it;
     *                           {@code "disabled"} or null to allow deletion.
     * @param tags Key-value labels for the index. Null or empty sends no tags.
     * @param readCapacity How read capacity is provisioned: wrap a {@link ReadCapacityDedicatedSpec} to reserve
     *                     dedicated read nodes, or a {@link ReadCapacityOnDemandSpec} for on-demand capacity. Null
     *                     means on-demand.
     * @param schema Metadata fields to index for filtering, each marked filterable. Null means every metadata
     *               field is indexed.
     * @return the new index's {@link IndexModel}; its {@code getHost()} is the host data-plane connections use, and
     *         {@code getStatus()} reports whether the index is ready.
     * @throws PineconeValidationException if the name, cloud or region is null or empty, or the dimension is less
     *         than 1. The client checks these before contacting the server.
     * @throws PineconeBadRequestException if the server rejects the request, for example an index name with
     *         uppercase letters.
     * @throws PineconeNotFoundException if the cloud and region pair does not exist.
     */
    public IndexModel createServerlessIndex(String indexName,
                                            String metric,
                                            int dimension,
                                            String cloud,
                                            String region,
                                            String deletionProtection,
                                            Map<String, String> tags,
                                            ReadCapacity readCapacity,
                                            BackupModelSchema schema) throws PineconeException {
        if (indexName == null || indexName.isEmpty()) {
            throw new PineconeValidationException("Index name cannot be null or empty");
        }

        if (metric == null || metric.isEmpty()) {
            metric = "cosine";
        }

        if (dimension < 1) {
            throw new PineconeValidationException("Dimension must be greater than 0. See limits for more info: https://docs.pinecone.io/reference/limits");
        }

        if (cloud == null || cloud.isEmpty()) {
            throw new PineconeValidationException("Cloud cannot be null or empty");
        }

        if (region == null || region.isEmpty()) {
            throw new PineconeValidationException("Region cannot be null or empty");
        }

        ServerlessSpec serverlessSpec = new ServerlessSpec().cloud(cloud).region(region);
        
        if (readCapacity != null) {
            serverlessSpec.readCapacity(readCapacity);
        }
        
        if (schema != null) {
            serverlessSpec.schema(schema);
        }
        
        IndexSpec createServerlessIndexRequestSpec = new IndexSpec(new IndexSpecServerless().serverless(serverlessSpec));

        IndexModel indexModel = null;

        try {
            CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                    .name(indexName)
                    .metric(metric)
                    .dimension(dimension)
                    .spec(createServerlessIndexRequestSpec)
                    .deletionProtection(deletionProtection);

            if(tags != null && !tags.isEmpty()) {
                createIndexRequest.tags(tags);
            }

            indexModel = manageIndexesApi.createIndex(Configuration.VERSION, createIndexRequest);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return indexModel;
    }

    /**
     * Creates a serverless index for sparse vectors and returns before the index is ready.
     * <p>
     * Sparse indexes store vectors with mostly zero values, such as keyword weights, and always use the
     * {@code "dotproduct"} metric, which this method sets for you. They take no dimension. For dense vectors use
     * {@link #createServerlessIndex(String, String, int, String, String, String, Map)}. The index is not ready for
     * reads and writes when this method returns: poll {@link #describeIndex(String)} until its status reports ready.
     *
     * <pre>{@code
     * IndexModel index = pinecone.createSparseServelessIndex(
     *     "articles-keywords", "aws", "us-east-1", "disabled", null, "sparse");
     * }</pre>
     *
     * @param indexName Name of the new index, for example {@code "articles-keywords"}. Must be unique in the project.
     * @param cloud Cloud provider that hosts the index, for example {@code "aws"}.
     * @param region Region in that cloud, for example {@code "us-east-1"}.
     * @param deletionProtection {@code "enabled"} to block {@link #deleteIndex(String)} until you disable it;
     *                           {@code "disabled"} or null to allow deletion.
     * @param tags Key-value labels for the index. Null or empty sends no tags.
     * @param vectorType Pass {@code "sparse"}. Must not be null. The client also accepts {@code "dense"}, but the
     *                   server rejects it because this method sends no dimension.
     * @return the new index's {@link IndexModel}; its {@code getHost()} is the host data-plane connections use, and
     *         {@code getStatus()} reports whether the index is ready.
     * @throws PineconeValidationException if the name, cloud or region is null or empty, or {@code vectorType} is
     *         neither {@code "sparse"} nor {@code "dense"}. The client checks these before contacting the server.
     * @throws PineconeBadRequestException if the server rejects the request, for example an index name with
     *         uppercase letters or {@code vectorType} {@code "dense"}.
     * @throws PineconeNotFoundException if the cloud and region pair does not exist.
     */
    public IndexModel createSparseServelessIndex(String indexName,
                                                  String cloud,
                                                  String region,
                                                  String deletionProtection,
                                                  Map<String, String> tags,
                                                  String vectorType)  throws PineconeException {
        if (indexName == null || indexName.isEmpty()) {
            throw new PineconeValidationException("Index name cannot be null or empty");
        }

        if (cloud == null || cloud.isEmpty()) {
            throw new PineconeValidationException("Cloud cannot be null or empty");
        }

        if (region == null || region.isEmpty()) {
            throw new PineconeValidationException("Region cannot be null or empty");
        }

        if(!vectorType.equalsIgnoreCase("sparse") && !vectorType.equalsIgnoreCase("dense")) {
            throw new PineconeValidationException("vectorType must be sparse or dense");
        }

        ServerlessSpec serverlessSpec = new ServerlessSpec().cloud(cloud).region(region);
        IndexSpec createServerlessIndexRequestSpec = new IndexSpec(new IndexSpecServerless().serverless(serverlessSpec));

        IndexModel indexModel = null;

        try {
            CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                    .name(indexName)
                    .metric("dotproduct")
                    .spec(createServerlessIndexRequestSpec)
                    .deletionProtection(deletionProtection)
                    .vectorType(vectorType);

            if(tags != null && !tags.isEmpty()) {
                createIndexRequest.tags(tags);
            }

            indexModel = manageIndexesApi.createIndex(Configuration.VERSION, createIndexRequest);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return indexModel;
    }

    /**
     * Creates a serverless index that embeds your text with a Pinecone-hosted model, and returns before the index
     * is ready.
     * <p>
     * Use this when you want to upsert and search plain text with {@link Index#upsertRecords(String, java.util.List) Index.upsertRecords} and
     * {@link Index#searchRecords Index.searchRecords} instead of computing vectors yourself. The model decides whether the index is dense
     * or sparse, its dimension and its default metric. Use
     * {@link #createIndexForModel(String, String, String, CreateIndexForModelRequestEmbed, String, Map, ReadCapacity,
     * BackupModelSchema) the eight-argument overload} to set read capacity or a metadata schema. The client sends the
     * request without checking any argument, and the index is not ready for reads and writes when this method
     * returns: poll {@link #describeIndex(String)} until its status reports ready.
     *
     * <pre>{@code
     * import org.openapitools.db_control.client.model.CreateIndexForModelRequestEmbed;
     * import java.util.HashMap;
     * import java.util.Map;
     *
     * Map<String, String> fieldMap = new HashMap<>();
     * fieldMap.put("text", "chunk_text");
     * CreateIndexForModelRequestEmbed embed = new CreateIndexForModelRequestEmbed()
     *     .model("multilingual-e5-large")
     *     .fieldMap(fieldMap);
     *
     * IndexModel index = pinecone.createIndexForModel(
     *     "articles-en", "aws", "us-east-1", embed, "disabled", null);
     * }</pre>
     *
     * @param name Name of the new index, for example {@code "articles-en"}. Must be unique in the project.
     * @param cloud Cloud provider that hosts the index, for example {@code "aws"}.
     * @param region Region in that cloud, for example {@code "us-east-1"}.
     * @param embed The embedding model and field map. The field map names the record field to embed, for example
     *              {@code "text" -> "chunk_text"}. You can set the metric and dimension here when the model
     *              supports more than one; otherwise the model's defaults apply. The model cannot be changed later.
     * @param deletionProtection {@code "enabled"} to block {@link #deleteIndex(String)} until you disable it;
     *                           {@code "disabled"} or null to allow deletion.
     * @param tags Key-value labels for the index. Null or empty sends no tags.
     * @return the new index's {@link IndexModel}; its {@code getHost()} is the host data-plane connections use,
     *         {@code getStatus()} reports whether the index is ready, and {@code getEmbed()} describes the model.
     * @throws ApiException if the server rejects the request, for example an unknown model name, a field map
     *         without the key the model expects, or an index name with uppercase letters. {@code getCode()} returns
     *         the HTTP status. This method does not convert server errors to {@link PineconeException} subclasses.
     */
    public IndexModel createIndexForModel(String name,
                                                     String cloud,
                                                     String region,
                                                     CreateIndexForModelRequestEmbed embed,
                                                     String deletionProtection,
                                                     Map<String, String> tags) throws PineconeException, ApiException {
        return createIndexForModel(name, cloud, region, embed, deletionProtection, tags, null, null);
    }

    /**
     * Creates a serverless index that embeds your text with a Pinecone-hosted model, with optional read capacity and
     * metadata schema settings, and returns before the index is ready.
     * <p>
     * Use this overload when you need dedicated read nodes or want to index only the metadata fields you filter on;
     * otherwise use {@link #createIndexForModel(String, String, String, CreateIndexForModelRequestEmbed, String, Map)
     * the six-argument overload}. The client sends the request without checking any argument, and the index is not
     * ready for reads and writes when this method returns: poll {@link #describeIndex(String)} until its status
     * reports ready.
     *
     * <pre>{@code
     * import org.openapitools.db_control.client.model.BackupModelSchema;
     * import org.openapitools.db_control.client.model.BackupModelSchemaFieldsValue;
     * import org.openapitools.db_control.client.model.CreateIndexForModelRequestEmbed;
     * import java.util.HashMap;
     * import java.util.Map;
     *
     * Map<String, String> fieldMap = new HashMap<>();
     * fieldMap.put("text", "chunk_text");
     * CreateIndexForModelRequestEmbed embed = new CreateIndexForModelRequestEmbed()
     *     .model("multilingual-e5-large")
     *     .fieldMap(fieldMap);
     *
     * Map<String, BackupModelSchemaFieldsValue> fields = new HashMap<>();
     * fields.put("category", new BackupModelSchemaFieldsValue().filterable(true));
     * BackupModelSchema schema = new BackupModelSchema().fields(fields);
     *
     * IndexModel index = pinecone.createIndexForModel(
     *     "articles-en", "aws", "us-east-1", embed, "disabled", null, null, schema);
     * }</pre>
     *
     * @param name Name of the new index, for example {@code "articles-en"}. Must be unique in the project.
     * @param cloud Cloud provider that hosts the index, for example {@code "aws"}.
     * @param region Region in that cloud, for example {@code "us-east-1"}.
     * @param embed The embedding model and field map. The field map names the record field to embed, for example
     *              {@code "text" -> "chunk_text"}. You can set the metric and dimension here when the model
     *              supports more than one; otherwise the model's defaults apply. The model cannot be changed later.
     * @param deletionProtection {@code "enabled"} to block {@link #deleteIndex(String)} until you disable it;
     *                           {@code "disabled"} or null to allow deletion.
     * @param tags Key-value labels for the index. Null or empty sends no tags.
     * @param readCapacity How read capacity is provisioned: wrap a {@link ReadCapacityDedicatedSpec} to reserve
     *                     dedicated read nodes, or a {@link ReadCapacityOnDemandSpec} for on-demand capacity. Null
     *                     means on-demand.
     * @param schema Metadata fields to index for filtering, each marked filterable. Null means every metadata
     *               field is indexed.
     * @return the new index's {@link IndexModel}; its {@code getHost()} is the host data-plane connections use,
     *         {@code getStatus()} reports whether the index is ready, and {@code getEmbed()} describes the model.
     * @throws ApiException if the server rejects the request, for example an unknown model name, a field map
     *         without the key the model expects, or an index name with uppercase letters. {@code getCode()} returns
     *         the HTTP status. This method does not convert server errors to {@link PineconeException} subclasses.
     */
    public IndexModel createIndexForModel(String name,
                                          String cloud,
                                          String region,
                                          CreateIndexForModelRequestEmbed embed,
                                          String deletionProtection,
                                          Map<String, String> tags,
                                          ReadCapacity readCapacity,
                                          BackupModelSchema schema) throws PineconeException, ApiException {

        CreateIndexForModelRequest createIndexForModelRequest = new CreateIndexForModelRequest()
                .name(name)
                .cloud(cloud)
                .region(region)
                .embed(embed)
                .deletionProtection(deletionProtection)
                .tags(tags);

        if (readCapacity != null) {
            createIndexForModelRequest.readCapacity(readCapacity);
        }

        if (schema != null) {
            createIndexForModelRequest.schema(schema);
        }

        return manageIndexesApi.createIndexForModel(Configuration.VERSION, createIndexForModelRequest);
    }

    /**
     * Creates a BYOC index for dense vectors with deletion protection off, and returns before the index is ready.
     * <p>
     * A BYOC (bring your own cloud) index runs in a Pinecone environment deployed in your own cloud account, which
     * must already be set up. Use {@link #createByocIndex(String, String, int, String, String, Map,
     * BackupModelSchema) the seven-argument overload} to turn on deletion protection, add tags or set a metadata
     * schema. The index is not ready for reads and writes when this method returns: poll
     * {@link #describeIndex(String)} until its status reports ready.
     *
     * <pre>{@code
     * IndexModel index = pinecone.createByocIndex("product-catalog", "cosine", 1536, "aws-us-east-1-b921");
     * }</pre>
     *
     * @param indexName Name of the new index, for example {@code "product-catalog"}. Must be unique in the project.
     * @param metric Similarity metric: {@code "cosine"}, {@code "euclidean"} or {@code "dotproduct"}. Null or empty
     *               means {@code "cosine"}.
     * @param dimension Length of the vectors you will store, matching your embedding model's output, for example
     *                  {@code 1536}.
     * @param environment Name of your BYOC environment, for example {@code "aws-us-east-1-b921"}. Pinecone gives
     *                    you this name when the environment is set up.
     * @return the new index's {@link IndexModel}; its {@code getHost()} is the host data-plane connections use, and
     *         {@code getStatus()} reports whether the index is ready.
     * @throws PineconeValidationException if the name or environment is null or empty, or the dimension is less
     *         than 1. The client checks these before contacting the server.
     * @throws PineconeBadRequestException if the server rejects the request, for example an index name with
     *         uppercase letters.
     */
    public IndexModel createByocIndex(String indexName,
                                      String metric,
                                      int dimension,
                                      String environment) throws PineconeException {
        return createByocIndex(indexName, metric, dimension, environment, "disabled", null, null);
    }

    /**
     * Creates a BYOC index for dense vectors with optional deletion protection, tags and metadata schema, and
     * returns before the index is ready.
     * <p>
     * Use this overload to set any of those options; otherwise use
     * {@link #createByocIndex(String, String, int, String) the four-argument overload}. The BYOC environment must
     * already be set up in your cloud account. The index is not ready for reads and writes when this method returns:
     * poll {@link #describeIndex(String)} until its status reports ready.
     *
     * <pre>{@code
     * import org.openapitools.db_control.client.model.BackupModelSchema;
     * import org.openapitools.db_control.client.model.BackupModelSchemaFieldsValue;
     * import java.util.HashMap;
     * import java.util.Map;
     *
     * Map<String, BackupModelSchemaFieldsValue> fields = new HashMap<>();
     * fields.put("category", new BackupModelSchemaFieldsValue().filterable(true));
     * BackupModelSchema schema = new BackupModelSchema().fields(fields);
     *
     * IndexModel index = pinecone.createByocIndex(
     *     "product-catalog", "cosine", 1536, "aws-us-east-1-b921", "enabled", null, schema);
     * }</pre>
     *
     * @param indexName Name of the new index, for example {@code "product-catalog"}. Must be unique in the project.
     * @param metric Similarity metric: {@code "cosine"}, {@code "euclidean"} or {@code "dotproduct"}. Null or empty
     *               means {@code "cosine"}.
     * @param dimension Length of the vectors you will store, matching your embedding model's output, for example
     *                  {@code 1536}.
     * @param environment Name of your BYOC environment, for example {@code "aws-us-east-1-b921"}. Pinecone gives
     *                    you this name when the environment is set up.
     * @param deletionProtection {@code "enabled"} to block {@link #deleteIndex(String)} until you disable it;
     *                           {@code "disabled"} or null to allow deletion.
     * @param tags Key-value labels for the index. Null or empty sends no tags.
     * @param schema Metadata fields to index for filtering, each marked filterable. Null means every metadata
     *               field is indexed.
     * @return the new index's {@link IndexModel}; its {@code getHost()} is the host data-plane connections use, and
     *         {@code getStatus()} reports whether the index is ready.
     * @throws PineconeValidationException if the name or environment is null or empty, or the dimension is less
     *         than 1. The client checks these before contacting the server.
     * @throws PineconeBadRequestException if the server rejects the request, for example an index name with
     *         uppercase letters.
     */
    public IndexModel createByocIndex(String indexName,
                                      String metric,
                                      int dimension,
                                      String environment,
                                      String deletionProtection,
                                      Map<String, String> tags,
                                      BackupModelSchema schema) throws PineconeException {
        if (indexName == null || indexName.isEmpty()) {
            throw new PineconeValidationException("Index name cannot be null or empty");
        }

        if (metric == null || metric.isEmpty()) {
            metric = "cosine";
        }

        if (dimension < 1) {
            throw new PineconeValidationException("Dimension must be greater than 0. See limits for more info: https://docs.pinecone.io/reference/limits");
        }

        if (environment == null || environment.isEmpty()) {
            throw new PineconeValidationException("Environment cannot be null or empty");
        }

        ByocSpec byocSpec = new ByocSpec().environment(environment);

        if (schema != null) {
            byocSpec.schema(schema);
        }

        IndexSpec createByocIndexRequestSpec = new IndexSpec(new IndexSpecBYOC().byoc(byocSpec));

        IndexModel indexModel = null;

        try {
            CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                    .name(indexName)
                    .metric(metric)
                    .dimension(dimension)
                    .spec(createByocIndexRequestSpec)
                    .deletionProtection(deletionProtection);

            if(tags != null && !tags.isEmpty()) {
                createIndexRequest.tags(tags);
            }

            indexModel = manageIndexesApi.createIndex(Configuration.VERSION, createIndexRequest);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return indexModel;
    }

    /**
     * Creates a pod-based index with one pod and the cosine metric, and returns before the index is ready.
     * <p>
     * Poll {@link #describeIndex(String)} until the status is ready before connecting with
     * {@link #getIndexConnection(String)}. Each other overload adds one or two settings; to combine settings that no
     * single overload offers, use the
     * {@link #createPodsIndex(String, Integer, String, String, String, Integer, Integer, Integer, PodSpecMetadataConfig, String, String, Map) twelve-argument overload}
     * and pass null for what you leave out.
     *
     * <pre>{@code
     *     IndexModel index = pinecone.createPodsIndex("product-catalog", 1536, "us-east-1-aws", "p1.x1");
     * }</pre>
     *
     * @param indexName   the name of the new index, for example {@code "product-catalog"}.
     * @param dimension   the length of the vectors the index stores, for example {@code 1536}. It must match the
     *                    output of your embedding model.
     * @param environment the pod environment to host the index in, for example {@code "us-east-1-aws"}.
     * @param podType     the pod type and size, for example {@code "p1.x1"}.
     * @return the new index as the server reports it right after creation, usually not ready yet.
     *         {@link IndexModel#getStatus()} reports readiness.
     * @throws PineconeValidationException if a required argument is null or empty, or a count is less than 1. The
     *                                     client checks these before sending the request.
     * @throws PineconeBadRequestException if the server rejects a value, such as an invalid index name or pod
     *                                     type.
     */
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment, String podType) {
        return createPodsIndex(indexName, dimension, environment, podType, null, null, null,
                null, null, null, "disabled", null);
    }

    /**
     * Creates a pod-based index with deletion protection set, and returns before the index is ready.
     * <p>
     * With deletion protection enabled, {@link #deleteIndex(String)} fails until you disable it with
     * {@link #configurePodsIndex(String, String)}.
     *
     * <pre>{@code
     *     pinecone.createPodsIndex("product-catalog", 1536, "us-east-1-aws", "p1.x1", "enabled");
     * }</pre>
     *
     * @param indexName   the name of the new index, for example {@code "product-catalog"}.
     * @param dimension   the length of the vectors the index stores, for example {@code 1536}. It must match the
     *                    output of your embedding model.
     * @param environment the pod environment to host the index in, for example {@code "us-east-1-aws"}.
     * @param podType     the pod type and size, for example {@code "p1.x1"}.
     * @param deletionProtection {@code "enabled"} to block deletion, or {@code "disabled"}. Pass null to leave it
     *                    disabled.
     * @return the new index as the server reports it right after creation, usually not ready yet.
     *         {@link IndexModel#getStatus()} reports readiness.
     * @throws PineconeValidationException if a required argument is null or empty, or a count is less than 1. The
     *                                     client checks these before sending the request.
     * @throws PineconeBadRequestException if the server rejects a value, such as an invalid index name, pod type or
     *                                     deletion protection value.
     */
    public IndexModel createPodsIndex(String indexName,
                                      Integer dimension,
                                      String environment,
                                      String podType,
                                      String deletionProtection) {
        return createPodsIndex(indexName, dimension, environment, podType, null, null, null,
                null, null, null, deletionProtection, null);
    }

    /**
     * Creates a pod-based index with a chosen metric that indexes only some metadata fields, and returns before the
     * index is ready.
     *
     * <pre>{@code
     *     PodSpecMetadataConfig metadataConfig = new PodSpecMetadataConfig()
     *             .indexed(Arrays.asList("category", "brand"));
     *     pinecone.createPodsIndex("product-catalog", 1536, "us-east-1-aws", "p1.x1", "cosine", metadataConfig);
     * }</pre>
     *
     * @param indexName   the name of the new index, for example {@code "product-catalog"}.
     * @param dimension   the length of the vectors the index stores, for example {@code 1536}. It must match the
     *                    output of your embedding model.
     * @param environment the pod environment to host the index in, for example {@code "us-east-1-aws"}.
     * @param podType     the pod type and size, for example {@code "p1.x1"}.
     * @param metric      the similarity metric: {@code "cosine"}, {@code "euclidean"} or {@code "dotproduct"}. Pass
     *                    null for cosine.
     * @param metadataConfig the metadata fields to index, for example
     *                    {@code new PodSpecMetadataConfig().indexed(Arrays.asList("category", "brand"))}.
     *                    Pass null to index every metadata field.
     * @return the new index as the server reports it right after creation, usually not ready yet.
     *         {@link IndexModel#getStatus()} reports readiness.
     * @throws PineconeValidationException if a required argument is null or empty, or a count is less than 1. The
     *                                     client checks these before sending the request.
     * @throws PineconeBadRequestException if the server rejects a value, such as an invalid index name or pod
     *                                     type.
     */
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment,
                                      String podType, String metric, PodSpecMetadataConfig metadataConfig) {
        return createPodsIndex(indexName, dimension, environment, podType, metric, null, null, null,
                metadataConfig,null, "disabled", null);
    }

    /**
     * Creates a pod-based index loaded with the vectors of a collection, and returns before the index is ready.
     * <p>
     * The collection must be in the same environment as the new index.
     *
     * <pre>{@code
     *     pinecone.createPodsIndex("product-catalog-restored", 1536, "us-east-1-aws", "p1.x1", "cosine",
     *             "product-catalog-snapshot");
     * }</pre>
     *
     * @param indexName   the name of the new index, for example {@code "product-catalog"}.
     * @param dimension   the length of the vectors the index stores, for example {@code 1536}. It must match the
     *                    output of your embedding model.
     * @param environment the pod environment to host the index in, for example {@code "us-east-1-aws"}.
     * @param podType     the pod type and size, for example {@code "p1.x1"}.
     * @param metric      the similarity metric: {@code "cosine"}, {@code "euclidean"} or {@code "dotproduct"}. Pass
     *                    null for cosine.
     * @param sourceCollection the name of the collection to load, for example {@code "product-catalog-snapshot"}.
     * @return the new index as the server reports it right after creation, usually not ready yet.
     *         {@link IndexModel#getStatus()} reports readiness.
     * @throws PineconeValidationException if a required argument is null or empty, or a count is less than 1. The
     *                                     client checks these before sending the request.
     * @throws PineconeException if the server rejects the request, for example because the collection does not
     *                           exist or is in a different environment.
     */
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment,
                                      String podType, String metric, String sourceCollection) {
        return createPodsIndex(indexName, dimension, environment, podType, metric, null, null, null, null,
                sourceCollection, "disabled", null);
    }

    /**
     * Creates a pod-based index with a given number of pods, and returns before the index is ready.
     * <p>
     * The pods are used as shards with one replica. To set replicas, use
     * {@link #createPodsIndex(String, Integer, String, String, Integer, Integer)}.
     *
     * <pre>{@code
     *     pinecone.createPodsIndex("product-catalog", 1536, "us-east-1-aws", "p1.x1", 2);
     * }</pre>
     *
     * @param indexName   the name of the new index, for example {@code "product-catalog"}.
     * @param dimension   the length of the vectors the index stores, for example {@code 1536}. It must match the
     *                    output of your embedding model.
     * @param environment the pod environment to host the index in, for example {@code "us-east-1-aws"}.
     * @param podType     the pod type and size, for example {@code "p1.x1"}.
     * @param pods        the number of pods, for example {@code 2}.
     * @return the new index as the server reports it right after creation, usually not ready yet.
     *         {@link IndexModel#getStatus()} reports readiness.
     * @throws PineconeValidationException if a required argument is null or empty, or a count is less than 1. The
     *                                     client checks these before sending the request.
     * @throws PineconeBadRequestException if the server rejects a value, such as an invalid index name or pod
     *                                     type.
     */
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment,
                                      String podType, Integer pods) {
        return createPodsIndex(indexName, dimension, environment, podType, null, null, null, pods,
                null, null, "disabled", null);
    }

    /**
     * Creates a pod-based index with a given number of pods that indexes only some metadata fields, and returns
     * before the index is ready.
     * <p>
     * The pods are used as shards with one replica.
     *
     * <pre>{@code
     *     PodSpecMetadataConfig metadataConfig = new PodSpecMetadataConfig()
     *             .indexed(Arrays.asList("category", "brand"));
     *     pinecone.createPodsIndex("product-catalog", 1536, "us-east-1-aws", "p1.x1", 2, metadataConfig);
     * }</pre>
     *
     * @param indexName   the name of the new index, for example {@code "product-catalog"}.
     * @param dimension   the length of the vectors the index stores, for example {@code 1536}. It must match the
     *                    output of your embedding model.
     * @param environment the pod environment to host the index in, for example {@code "us-east-1-aws"}.
     * @param podType     the pod type and size, for example {@code "p1.x1"}.
     * @param pods        the number of pods, for example {@code 2}.
     * @param metadataConfig the metadata fields to index, for example
     *                    {@code new PodSpecMetadataConfig().indexed(Arrays.asList("category", "brand"))}.
     *                    Pass null to index every metadata field.
     * @return the new index as the server reports it right after creation, usually not ready yet.
     *         {@link IndexModel#getStatus()} reports readiness.
     * @throws PineconeValidationException if a required argument is null or empty, or a count is less than 1. The
     *                                     client checks these before sending the request.
     * @throws PineconeBadRequestException if the server rejects a value, such as an invalid index name or pod
     *                                     type.
     */
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment,
                                      String podType, Integer pods,
                                      PodSpecMetadataConfig metadataConfig) {
        return createPodsIndex(indexName, dimension, environment, podType, null, null, null, pods, metadataConfig,
                null, "disabled", null);
    }

    /**
     * Creates a pod-based index with a given number of replicas and shards, and returns before the index is ready.
     * <p>
     * The index uses replicas times shards pods.
     *
     * <pre>{@code
     *     pinecone.createPodsIndex("product-catalog", 1536, "us-east-1-aws", "p1.x1", 2, 2);
     * }</pre>
     *
     * @param indexName   the name of the new index, for example {@code "product-catalog"}.
     * @param dimension   the length of the vectors the index stores, for example {@code 1536}. It must match the
     *                    output of your embedding model.
     * @param environment the pod environment to host the index in, for example {@code "us-east-1-aws"}.
     * @param podType     the pod type and size, for example {@code "p1.x1"}.
     * @param replicas    the number of copies of the index, for more throughput and availability. Pass null for
     *                    one.
     * @param shards      the number of pods to split the data across, for more capacity. Pass null for one.
     * @return the new index as the server reports it right after creation, usually not ready yet.
     *         {@link IndexModel#getStatus()} reports readiness.
     * @throws PineconeValidationException if a required argument is null or empty, or a count is less than 1. The
     *                                     client checks these before sending the request.
     * @throws PineconeBadRequestException if the server rejects a value, such as an invalid index name or pod
     *                                     type.
     */
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment,
                                      String podType, Integer replicas,
                                      Integer shards) {
        return createPodsIndex(indexName, dimension, environment, podType, null, replicas, shards, null,
                null, null, "disabled", null);
    }

    /**
     * Creates a pod-based index with a given number of replicas and shards that indexes only some metadata fields,
     * and returns before the index is ready.
     * <p>
     * The index uses replicas times shards pods.
     *
     * <pre>{@code
     *     PodSpecMetadataConfig metadataConfig = new PodSpecMetadataConfig()
     *             .indexed(Arrays.asList("category", "brand"));
     *     pinecone.createPodsIndex("product-catalog", 1536, "us-east-1-aws", "p1.x1", 2, 2, metadataConfig);
     * }</pre>
     *
     * @param indexName   the name of the new index, for example {@code "product-catalog"}.
     * @param dimension   the length of the vectors the index stores, for example {@code 1536}. It must match the
     *                    output of your embedding model.
     * @param environment the pod environment to host the index in, for example {@code "us-east-1-aws"}.
     * @param podType     the pod type and size, for example {@code "p1.x1"}.
     * @param replicas    the number of copies of the index, for more throughput and availability. Pass null for
     *                    one.
     * @param shards      the number of pods to split the data across, for more capacity. Pass null for one.
     * @param metadataConfig the metadata fields to index, for example
     *                    {@code new PodSpecMetadataConfig().indexed(Arrays.asList("category", "brand"))}.
     *                    Pass null to index every metadata field.
     * @return the new index as the server reports it right after creation, usually not ready yet.
     *         {@link IndexModel#getStatus()} reports readiness.
     * @throws PineconeValidationException if a required argument is null or empty, or a count is less than 1. The
     *                                     client checks these before sending the request.
     * @throws PineconeBadRequestException if the server rejects a value, such as an invalid index name or pod
     *                                     type.
     */
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment,
                                      String podType, Integer replicas,
                                      Integer shards, PodSpecMetadataConfig metadataConfig) {
        return createPodsIndex(indexName, dimension, environment,
                podType, null, replicas,
                shards, null, metadataConfig,
                null, "disabled", null);
    }

    /**
     * Creates a pod-based index with every setting available, and returns before the index is ready.
     * <p>
     * Use this overload to combine settings that no shorter overload offers, and pass null for what you leave out.
     * Poll {@link #describeIndex(String)} until the status is ready before connecting.
     *
     * <pre>{@code
     *     Map<String, String> tags = new HashMap<>();
     *     tags.put("team", "search");
     *     pinecone.createPodsIndex("product-catalog", 1536, "us-east-1-aws", "p1.x1", "cosine",
     *             2, 1, null, null, null, "enabled", tags);
     * }</pre>
     *
     * @param indexName   the name of the new index, for example {@code "product-catalog"}.
     * @param dimension   the length of the vectors the index stores, for example {@code 1536}. It must match the
     *                    output of your embedding model.
     * @param environment the pod environment to host the index in, for example {@code "us-east-1-aws"}.
     * @param podType     the pod type and size, for example {@code "p1.x1"}.
     * @param metric      the similarity metric: {@code "cosine"}, {@code "euclidean"} or {@code "dotproduct"}. Pass
     *                    null for cosine.
     * @param replicas    the number of copies of the index, for more throughput and availability. Pass null for
     *                    one.
     * @param shards      the number of pods to split the data across, for more capacity. Pass null for one.
     * @param pods        the total number of pods. If replicas and shards are also set, it must equal replicas times
     *                    shards. Pass null to use replicas times shards.
     * @param metadataConfig the metadata fields to index, for example
     *                    {@code new PodSpecMetadataConfig().indexed(Arrays.asList("category", "brand"))}.
     *                    Pass null to index every metadata field.
     * @param sourceCollection the name of a collection to load vectors from, for example
     *                    {@code "product-catalog-snapshot"}. It must be in the same environment. Pass null to
     *                    start empty.
     * @param deletionProtection {@code "enabled"} to block {@link #deleteIndex(String)}, or {@code "disabled"}. Pass
     *                    null to leave it disabled.
     * @param tags        labels to attach to the index, for example {@code team=search}. Pass null or an empty map
     *                    for no tags.
     * @return the new index as the server reports it right after creation, usually not ready yet.
     *         {@link IndexModel#getStatus()} reports readiness.
     * @throws PineconeValidationException if a required argument is null or empty, a count is less than 1, or pods
     *                                     does not equal replicas times shards. The client checks these before
     *                                     sending the request.
     * @throws PineconeException if the server rejects the request, for example because of an invalid index name or
     *                           pod type, or a source collection that does not exist.
     */
    public IndexModel createPodsIndex(String indexName,
                                      Integer dimension,
                                      String environment,
                                      String podType,
                                      String metric,
                                      Integer replicas,
                                      Integer shards,
                                      Integer pods,
                                      PodSpecMetadataConfig metadataConfig,
                                      String sourceCollection,
                                      String deletionProtection,
                                      Map<String, String> tags) throws PineconeException {
        validatePodIndexParams(indexName, dimension, environment, podType, metric, replicas, shards, pods);

        PodSpec podSpec = new PodSpec().environment(environment)
                .podType(podType)
                .replicas(replicas)
                .shards(shards)
                .pods(pods)
                .metadataConfig(metadataConfig)
                .sourceCollection(sourceCollection);
        IndexSpec createIndexRequestSpec = new IndexSpec(new IndexSpecPodBased().pod(podSpec));
        CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                .name(indexName)
                .dimension(dimension)
                .metric(metric)
                .spec(createIndexRequestSpec)
                .deletionProtection(deletionProtection);

        if (tags != null && !tags.isEmpty()) {
            createIndexRequest.tags(tags);
        }

        IndexModel indexModel = null;
        try {
            indexModel = manageIndexesApi.createIndex(Configuration.VERSION, createIndexRequest);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return indexModel;
    }

    /**
     * Checks pod-based index arguments the same way {@link #createPodsIndex(String, Integer, String, String)} and its
     * overloads do, without contacting the server.
     * <p>
     * Only the client-side checks run, so passing does not guarantee that the server accepts the values.
     *
     * <pre>{@code
     *     Pinecone.validatePodIndexParams("product-catalog", 1536, "us-east-1-aws", "p1.x1", "cosine", 2, 1, 2);
     * }</pre>
     *
     * @param indexName   the index name. Must not be null or empty.
     * @param dimension   the vector dimension. Must not be null and must be at least 1.
     * @param environment the pod environment. Must not be null or empty.
     * @param podType     the pod type. Must not be null or empty.
     * @param metric      the similarity metric. Null is allowed; an empty string is not.
     * @param replicas    the number of replicas. Null is allowed; otherwise at least 1.
     * @param shards      the number of shards. Null is allowed; otherwise at least 1.
     * @param pods        the number of pods. Null is allowed; otherwise at least 1, and equal to replicas times
     *                    shards when all three are set.
     * @throws PineconeValidationException if any check fails. The message names the argument.
     */
    public static void validatePodIndexParams(String indexName, Integer dimension, String environment,
                                              String podType, String metric,
                                              Integer replicas, Integer shards, Integer pods) {

        if (indexName == null || indexName.isEmpty()) {
            throw new PineconeValidationException("indexName cannot be null or empty");
        }

        if (dimension == null) {
            throw new PineconeValidationException("Dimension cannot be null");
        }

        if (dimension < 1) {
            throw new PineconeValidationException("Dimension must be greater than 0. See limits for more info: https://docs.pinecone.io/reference/limits");
        }

        if (environment == null || environment.isEmpty()) {
            throw new PineconeValidationException("Environment cannot be null or empty");
        }

        if (podType == null || podType.isEmpty()) {
            throw new PineconeValidationException("podType cannot be null or empty");
        }

        if (metric != null && metric.isEmpty()) {
            throw new PineconeValidationException("Metric cannot be null or empty. Must be cosine, euclidean, or dotproduct.");
        }

        if (replicas != null && replicas < 1) {
            throw new PineconeValidationException("Number of replicas must be >= 1");
        }

        if (shards != null && shards < 1) {
            throw new PineconeValidationException("Number of shards must be >= 1");
        }

        if (pods != null && pods < 1) {
            throw new PineconeValidationException("Number of pods must be >= 1");
        }

        if (replicas != null && shards != null && pods != null && (replicas*shards != pods)) {
            throw new PineconeValidationException("Number of pods does not equal number of shards times number of " +
                    "replicas"); }
    }

    /**
     * Returns the current configuration and status of an index.
     * <p>
     * Call this to find out whether a newly created or reconfigured index is ready ({@code getStatus().getReady()})
     * and to read its host, dimension, metric, spec and deletion protection setting. It is the usual way to poll
     * after a create, configure or delete call, all of which return before the server finishes.
     *
     * <pre>{@code
     * IndexModel index = pinecone.describeIndex("product-catalog");
     * if (Boolean.TRUE.equals(index.getStatus().getReady())) {
     *     System.out.println("Ready at " + index.getHost());
     * }
     * }</pre>
     *
     * @param indexName the name of the index, for example {@code "product-catalog"}.
     * @return the {@link IndexModel}; {@code getStatus()} gives the readiness flag and state (for example
     *         {@code Initializing}, {@code Ready}, {@code Terminating}), and {@code getHost()} the host that
     *         {@link #getIndexConnection(String)} connects to.
     * @throws PineconeNotFoundException if no index with that name exists in the project.
     */
    public IndexModel describeIndex(String indexName) throws PineconeException {
        IndexModel indexModel = null;
        try {
            indexModel = manageIndexesApi.describeIndex(Configuration.VERSION, indexName);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return indexModel;
    }

    /**
     * Changes the pod type, replica count, deletion protection or tags of a pod-based index, and returns before the
     * change is applied.
     * <p>
     * Use this overload to change the pod type or tags; the
     * {@link #configurePodsIndex(String, Integer, String) three-argument overload} changes replicas and the
     * {@link #configurePodsIndex(String, String) two-argument overload} changes only deletion protection. Pass null
     * for anything you want to leave as it is. The server applies the change in the background: poll
     * {@link #describeIndex(String)} to see it take effect. The method fails for serverless and BYOC indexes; use
     * {@link #configureServerlessIndex(String, String, Map, ConfigureIndexRequestEmbed)} for those.
     *
     * <pre>{@code
     * IndexModel index = pinecone.configurePodsIndex("product-catalog", "p1.x2", null, null, null);
     * }</pre>
     *
     * @param indexName Name of the index to change, for example {@code "product-catalog"}.
     * @param podType Larger pod size in the same family, for example {@code "p1.x2"} for an index on
     *                {@code "p1.x1"}. The server rejects a different family or a smaller size. Null keeps the
     *                current pod type.
     * @param replicas New number of replicas, at least 1. Add replicas for more query throughput; remove them to
     *                 cut cost. Null keeps the current count.
     * @param deletionProtection {@code "enabled"} to block {@link #deleteIndex(String)}, {@code "disabled"} to
     *                           allow it. Null leaves the setting unchanged.
     * @param tags Tags to add or update, for example {@code "team" -> "search"}. Tags not in the map are kept, and a
     *             tag whose value is an empty string is removed. Null or empty leaves the tags unchanged.
     * @return the {@link IndexModel} of the index; poll {@link #describeIndex(String)} and check
     *         {@code getStatus()} and {@code getSpec()} to see when the change has been applied.
     * @throws PineconeValidationException if the index name is null or empty, or replicas is less than 1. The
     *         client checks these before contacting the server.
     * @throws PineconeNotFoundException if no index with that name exists.
     * @throws PineconeBadRequestException if the server rejects the change, for example a pod type in a different
     *         family or a smaller size, or an index that is not pod-based.
     * @throws PineconeForbiddenException if the change would exceed the project's pod quota.
     */
    public IndexModel configurePodsIndex(String indexName,
                                         String podType,
                                         Integer replicas,
                                         String deletionProtection,
                                         Map<String, String> tags) throws PineconeException {
        if (indexName == null || indexName.isEmpty()) {
            throw new PineconeValidationException("indexName cannot be null or empty");
        }

        // If you pass a # replicas, but they're < 1, throw an exception
        if (replicas != null) {
            if (replicas < 1) {
                throw new PineconeValidationException("Number of replicas must be >= 1");
            }
        }

        // Build ConfigureIndexRequest object
        ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest()
                .spec(new ConfigureIndexRequestSpec(
                        new ConfigureIndexRequestPodBased()
                                .pod(new ConfigureIndexRequestPodBasedConfig()
                                        .replicas(replicas)
                                        .podType(podType)
                                )
                )).deletionProtection(deletionProtection);

        if(tags != null && !tags.isEmpty()) {
            configureIndexRequest.tags(tags);
        }

        IndexModel indexModel = null;
        try {
            indexModel = manageIndexesApi.configureIndex(Configuration.VERSION, indexName, configureIndexRequest);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return indexModel;
    }

    /**
     * Changes the replica count and deletion protection of a pod-based index, and returns before the change is
     * applied.
     * <p>
     * This is the usual way to scale a pod-based index. The server applies the change in the background: poll
     * {@link #describeIndex(String)} to see it take effect. To change the pod type or tags, use
     * {@link #configurePodsIndex(String, String, Integer, String, Map) the five-argument overload}.
     *
     * <pre>{@code
     * IndexModel index = pinecone.configurePodsIndex("product-catalog", 3, null);
     * }</pre>
     *
     * @param indexName Name of the index to change, for example {@code "product-catalog"}.
     * @param replicas New number of replicas, at least 1. Null keeps the current count.
     * @param deletionProtection {@code "enabled"} to block {@link #deleteIndex(String)}, {@code "disabled"} to
     *                           allow it. Null leaves the setting unchanged.
     * @return the {@link IndexModel} of the index; poll {@link #describeIndex(String)} and check
     *         {@code getStatus()} and {@code getSpec()} to see when the change has been applied.
     * @throws PineconeValidationException if the index name is null or empty, or replicas is less than 1. The
     *         client checks these before contacting the server.
     * @throws PineconeNotFoundException if no index with that name exists.
     * @throws PineconeBadRequestException if the server rejects the change, for example because the index is not
     *         pod-based.
     * @throws PineconeForbiddenException if the change would exceed the project's pod quota.
     */
    public IndexModel configurePodsIndex(String indexName, Integer replicas, String deletionProtection) throws PineconeException {
        return configurePodsIndex(indexName, null, replicas, deletionProtection, null);
    }

    /**
     * Turns deletion protection on or off for a pod-based index.
     * <p>
     * Disable deletion protection before calling {@link #deleteIndex(String)} on a protected index. For serverless
     * and BYOC indexes, use {@link #configureServerlessIndex(String, String, Map, ConfigureIndexRequestEmbed)}
     * instead.
     *
     * <pre>{@code
     * IndexModel index = pinecone.configurePodsIndex("product-catalog", "enabled");
     * }</pre>
     *
     * @param indexName Name of the index to change, for example {@code "product-catalog"}.
     * @param deletionProtection {@code "enabled"} to block {@link #deleteIndex(String)}, {@code "disabled"} to
     *                           allow it.
     * @return the {@link IndexModel} of the index; {@code getDeletionProtection()} reports the setting.
     * @throws PineconeValidationException if the index name is null or empty. The client checks this before
     *         contacting the server.
     * @throws PineconeNotFoundException if no index with that name exists.
     * @throws PineconeBadRequestException if the server rejects the change, for example a value other than
     *         {@code "enabled"} or {@code "disabled"}, or an index that is not pod-based.
     */
    public IndexModel configurePodsIndex(String indexName, String deletionProtection) throws PineconeException {
        return configurePodsIndex(indexName, null, null, deletionProtection, null);
    }

    /**
     * Changes the deletion protection, tags or integrated embedding of a serverless or BYOC index.
     * <p>
     * Use this overload for most changes; use
     * {@link #configureServerlessIndex(String, String, Map, ConfigureIndexRequestEmbed, String, String, Integer,
     * Integer) the eight-argument overload} to change read capacity. Pass null for anything you want to leave as it
     * is. Use {@link #configurePodsIndex(String, String, Integer, String, Map)} for pod-based indexes.
     *
     * <pre>{@code
     * IndexModel index = pinecone.configureServerlessIndex("product-catalog", "enabled", null, null);
     * }</pre>
     *
     * @param indexName Name of the index to change, for example {@code "product-catalog"}.
     * @param deletionProtection {@code "enabled"} to block {@link #deleteIndex(String)}, {@code "disabled"} to
     *                           allow it. Null leaves the setting unchanged.
     * @param tags Tags to add or update, for example {@code "team" -> "search"}. Tags not in the map are kept, and a
     *             tag whose value is an empty string is removed. Null or empty leaves the tags unchanged.
     * @param embed Embedding settings for a serverless index. On an index without integrated embedding, set the
     *              model and field map to have Pinecone embed text on upsert and search; the model must produce
     *              vectors of the index's type and dimension and support its metric. On an index that already has
     *              one, the model cannot be changed, but the field map and model parameters can. Null leaves the
     *              embedding settings unchanged.
     * @return the {@link IndexModel} of the index with the updated settings.
     * @throws PineconeValidationException if the index name is null or empty. The client checks this before
     *         contacting the server.
     * @throws PineconeNotFoundException if no index with that name exists.
     * @throws PineconeBadRequestException if the server rejects the change, for example an invalid deletion
     *         protection value, an embedding model that does not match the index, or embedding settings on an
     *         index that is not serverless.
     */
    public IndexModel configureServerlessIndex(String indexName,
                                               String deletionProtection,
                                               Map<String, String> tags,
                                               ConfigureIndexRequestEmbed embed) throws PineconeException {
        return configureServerlessIndex(indexName, deletionProtection, tags, embed, null, null, null, null);
    }

    /**
     * Changes the read capacity of a serverless index, along with any of its deletion protection, tags or integrated
     * embedding, and returns before a capacity change is applied.
     * <p>
     * Use this overload to switch between on-demand and dedicated read capacity or to resize dedicated read nodes;
     * otherwise use {@link #configureServerlessIndex(String, String, Map, ConfigureIndexRequestEmbed) the
     * four-argument overload}. Dedicated read capacity uses manual scaling with the shard and replica counts you
     * give, and is available only on paid plans. The server resizes in the background: poll
     * {@link #describeIndex(String)} to see the change take effect. Pass null for anything you want to leave as it
     * is.
     *
     * <pre>{@code
     * IndexModel index = pinecone.configureServerlessIndex(
     *     "product-catalog", null, null, null, "Dedicated", "t1", 1, 2);
     * }</pre>
     *
     * @param indexName Name of the index to change, for example {@code "product-catalog"}.
     * @param deletionProtection {@code "enabled"} to block {@link #deleteIndex(String)}, {@code "disabled"} to
     *                           allow it. Null leaves the setting unchanged.
     * @param tags Tags to add or update. Tags not in the map are kept, and a tag whose value is an empty string is
     *             removed. Null or empty leaves the tags unchanged.
     * @param embed Embedding settings, as in the four-argument overload. Null leaves them unchanged.
     * @param readCapacityMode {@code "OnDemand"} or {@code "Dedicated"}, case-sensitive. Null leaves read capacity
     *                         unchanged and ignores the next three arguments.
     * @param nodeType Dedicated node type, for example {@code "t1"}. Required for {@code "Dedicated"}; ignored
     *                 otherwise.
     * @param shards Number of dedicated shards, at least 1. Required for {@code "Dedicated"}; ignored otherwise.
     * @param replicas Number of dedicated replicas, at least 1. Add replicas for more query throughput. Required for
     *                 {@code "Dedicated"}; ignored otherwise.
     * @return the {@link IndexModel} of the index. Its serverless spec's {@code getReadCapacity()} reports the
     *         requested capacity and, for dedicated capacity, a status with the current shards and replicas.
     * @throws PineconeValidationException if the index name is null or empty, the mode is not one of the two
     *         values, or a dedicated request is missing the node type or has fewer than 1 shard or replica. The
     *         client checks these before contacting the server.
     * @throws PineconeNotFoundException if no index with that name exists.
     * @throws PineconeBadRequestException if the server rejects the change, for example dedicated capacity on a
     *         free plan or read capacity on an index that is not serverless.
     */
    public IndexModel configureServerlessIndex(String indexName,
                                               String deletionProtection,
                                               Map<String, String> tags,
                                               ConfigureIndexRequestEmbed embed,
                                               String readCapacityMode,
                                               String nodeType,
                                               Integer shards,
                                               Integer replicas) throws PineconeException {
        if (indexName == null || indexName.isEmpty()) {
            throw new PineconeValidationException("indexName cannot be null or empty");
        }

        // Build ConfigureIndexRequest object
        ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest()
                .deletionProtection(deletionProtection);

        if(tags != null && !tags.isEmpty()) {
            configureIndexRequest.tags(tags);
        }

        if(embed != null) {
            configureIndexRequest.embed(embed);
        }

        // Build ReadCapacity from primitive parameters if readCapacityMode is provided
        ReadCapacity readCapacity = null;
        if (readCapacityMode != null) {
            if ("OnDemand".equals(readCapacityMode)) {
                readCapacity = new ReadCapacity(new ReadCapacityOnDemandSpec().mode("OnDemand"));
            } else if ("Dedicated".equals(readCapacityMode)) {
                if (nodeType == null || nodeType.isEmpty()) {
                    throw new PineconeValidationException("nodeType is required when readCapacityMode is 'Dedicated'");
                }
                if (shards == null || shards < 1) {
                    throw new PineconeValidationException("shards must be at least 1 when readCapacityMode is 'Dedicated'");
                }
                if (replicas == null || replicas < 1) {
                    throw new PineconeValidationException("replicas must be at least 1 when readCapacityMode is 'Dedicated'");
                }
                
                ScalingConfigManual manual = new ScalingConfigManual().shards(shards).replicas(replicas);
                ReadCapacityDedicatedConfig dedicated = new ReadCapacityDedicatedConfig()
                        .nodeType(nodeType)
                        .scaling("Manual")
                        .manual(manual);
                readCapacity = new ReadCapacity(
                        new ReadCapacityDedicatedSpec().mode("Dedicated").dedicated(dedicated));
            } else {
                throw new PineconeValidationException("readCapacityMode must be 'OnDemand' or 'Dedicated'");
            }
        }

        // If readCapacity is provided, configure it via spec
        if (readCapacity != null) {
            ConfigureIndexRequestServerlessConfig serverlessConfig = new ConfigureIndexRequestServerlessConfig()
                    .readCapacity(readCapacity);
            ConfigureIndexRequestServerless serverless = new ConfigureIndexRequestServerless()
                    .serverless(serverlessConfig);
            ConfigureIndexRequestSpec spec = new ConfigureIndexRequestSpec(serverless);
            configureIndexRequest.spec(spec);
        }

        IndexModel indexModel = null;
        try {
            indexModel = manageIndexesApi.configureIndex(Configuration.VERSION, indexName, configureIndexRequest);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return indexModel;
    }

    /**
     * Lists every index in the project the API key belongs to.
     * <p>
     * The result is not paginated: one call returns all indexes, each with the same fields as
     * {@link #describeIndex(String)}.
     *
     * <pre>{@code
     * for (IndexModel index : pinecone.listIndexes().getIndexes()) {
     *     System.out.println(index.getName() + ": " + index.getStatus().getState());
     * }
     * }</pre>
     *
     * @return the {@link IndexList}; {@code getIndexes()} gives one {@link IndexModel} per index, and is empty when
     *         the project has none.
     */
    public IndexList listIndexes() throws PineconeException {
        IndexList indexList = null;
        try {
            indexList = manageIndexesApi.listIndexes(Configuration.VERSION);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return indexList;
    }

    /**
     * Permanently deletes an index and all its data, and returns before the deletion finishes.
     * <p>
     * The deletion cannot be undone. The index shows the {@code Terminating} state for a short time and then
     * {@link #describeIndex(String)} throws {@link PineconeNotFoundException}. The server refuses to delete an index
     * whose deletion protection is enabled; turn it off first with
     * {@link #configureServerlessIndex(String, String, Map, ConfigureIndexRequestEmbed)} or
     * {@link #configurePodsIndex(String, String)}. Deleting the index does not close a connection already obtained
     * from {@link #getIndexConnection(String)}; call {@link Index#close()} on it.
     *
     * <pre>{@code
     * pinecone.deleteIndex("product-catalog");
     * }</pre>
     *
     * @param indexName the name of the index to delete, for example {@code "product-catalog"}.
     * @throws PineconeNotFoundException if no index with that name exists in the project.
     * @throws PineconeForbiddenException if deletion protection is enabled for the index.
     * @throws PineconeUnmappedHttpException if a collection is still being created from the index.
     */
    public void deleteIndex(String indexName) throws PineconeException {
        try {
            manageIndexesApi.deleteIndex(Configuration.VERSION, indexName);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
    }

    /**
     * Starts a backup of a serverless index and returns before the backup is ready.
     * <p>
     * The backup starts in the {@code Initializing} status. Poll {@link #describeBackup(String)} until the status
     * is {@code Ready} before restoring from it with {@link #createIndexFromBackup(String, String)}. Pod-based
     * indexes cannot be backed up.
     *
     * <pre>{@code
     *     BackupModel backup = pinecone.createBackup("product-catalog", "product-catalog-nightly", null);
     *     String backupId = backup.getBackupId();
     * }</pre>
     *
     * @param indexName   the name of the index to back up, for example {@code "product-catalog"}.
     * @param backupName  a name for the backup, for example {@code "product-catalog-nightly"}. Pass null to omit it.
     * @param description a free-text description of the backup. Pass null to omit it.
     * @return the new backup. {@link BackupModel#getBackupId()} is the ID the other backup methods take, and
     *         {@link BackupModel#getStatus()} reports its progress.
     * @throws ApiException if {@code indexName} is null, or the server rejects the request, for example because
     *                      the index does not exist or is pod-based.
     */
    public BackupModel createBackup(String indexName, String backupName, String description) throws ApiException {
        return manageIndexesApi.createBackup(Configuration.VERSION, indexName,
                new CreateBackupRequest().name(backupName).description(description));
    }

    /**
     * Lists the first page of backups of one index, up to 10 backups.
     * <p>
     * Use {@link #listIndexBackups(String, Integer, String)} to choose the page size or fetch later pages, and
     * {@link #listProjectBackups()} to list backups of every index in the project.
     *
     * <pre>{@code
     *     for (BackupModel backup : pinecone.listIndexBackups("product-catalog").getData()) {
     *         System.out.println(backup.getBackupId() + " " + backup.getStatus());
     *     }
     * }</pre>
     *
     * @param indexName the name of the index whose backups to list, for example {@code "product-catalog"}.
     * @return one page of backups. {@link BackupList#getData()} holds the backups, and
     *         {@link BackupList#getPagination()} holds the token for the next page.
     * @throws ApiException if {@code indexName} is null, or the server rejects the request, for example because
     *                      the index does not exist.
     */
    public BackupList listIndexBackups(String indexName) throws ApiException {
        return manageIndexesApi.listIndexBackups(Configuration.VERSION, indexName, 10, null);
    }

    /**
     * Lists one page of backups of one index.
     * <p>
     * Returns a single page. To read every backup, call again with the token from the previous page until no
     * token comes back.
     *
     * <pre>{@code
     *     BackupList page = pinecone.listIndexBackups("product-catalog", 25, null);
     * }</pre>
     *
     * @param indexName       the name of the index whose backups to list, for example {@code "product-catalog"}.
     * @param limit           the maximum number of backups to return. Pass null for the server default.
     * @param paginationToken the token from {@link BackupList#getPagination()} of the previous page. Pass null
     *                        for the first page.
     * @return one page of backups. {@link BackupList#getData()} holds the backups, and
     *         {@link BackupList#getPagination()} holds the token for the next page.
     * @throws ApiException if {@code indexName} is null, or the server rejects the request, for example because
     *                      the index does not exist or the token is invalid.
     */
    public BackupList listIndexBackups(String indexName, Integer limit, String paginationToken) throws ApiException {
        return manageIndexesApi.listIndexBackups(Configuration.VERSION, indexName, limit, paginationToken);
    }

    /**
     * Lists the first page of backups of every index in the project.
     * <p>
     * The page size is the server default. Use {@link #listProjectBackups(Integer, String)} to choose the page
     * size or fetch later pages.
     *
     * <pre>{@code
     *     for (BackupModel backup : pinecone.listProjectBackups().getData()) {
     *         System.out.println(backup.getSourceIndexName() + " " + backup.getBackupId());
     *     }
     * }</pre>
     *
     * @return one page of backups. {@link BackupList#getData()} holds the backups, and
     *         {@link BackupList#getPagination()} holds the token for the next page.
     * @throws ApiException if the server rejects the request.
     */
    public BackupList listProjectBackups() throws ApiException {
        return listProjectBackups(null, null);
    }

    /**
     * Lists one page of backups of every index in the project.
     * <p>
     * Returns a single page. To read every backup, call again with the token from the previous page until no
     * token comes back.
     *
     * <pre>{@code
     *     BackupList page = pinecone.listProjectBackups(25, null);
     * }</pre>
     *
     * @param limit           the maximum number of backups to return. Pass null for the server default.
     * @param paginationToken the token from {@link BackupList#getPagination()} of the previous page. Pass null
     *                        for the first page.
     * @return one page of backups. {@link BackupList#getData()} holds the backups, and
     *         {@link BackupList#getPagination()} holds the token for the next page.
     * @throws ApiException if the server rejects the request, for example because the token is invalid.
     */
    public BackupList listProjectBackups(Integer limit, String paginationToken) throws ApiException {
        return manageIndexesApi.listProjectBackups(Configuration.VERSION, limit, paginationToken);
    }

    /**
     * Describes a backup, including its status.
     * <p>
     * Call this to wait for a backup created with {@link #createBackup(String, String, String)} to reach the
     * {@code Ready} status before restoring from it.
     *
     * <pre>{@code
     *     BackupModel backup = pinecone.describeBackup(backupId);
     *     boolean ready = "Ready".equals(backup.getStatus());
     * }</pre>
     *
     * @param backupId the ID of the backup, from {@link BackupModel#getBackupId()}.
     * @return the backup. {@link BackupModel#getStatus()} is its status, and the model also carries the source
     *         index name, dimension, metric, and record count.
     * @throws ApiException if {@code backupId} is null, or the server rejects the request, for example because
     *                      the backup does not exist.
     */
    public BackupModel describeBackup(String backupId) throws ApiException {
        return manageIndexesApi.describeBackup(Configuration.VERSION, backupId);
    }

    /**
     * Deletes a backup permanently.
     * <p>
     * The index the backup was taken from is not affected.
     *
     * <pre>{@code
     *     pinecone.deleteBackup(backupId);
     * }</pre>
     *
     * @param backupId the ID of the backup to delete, from {@link BackupModel#getBackupId()}.
     * @throws ApiException if {@code backupId} is null, or the server rejects the request, for example because
     *                      the backup does not exist.
     */
    public void deleteBackup(String backupId) throws ApiException {
        manageIndexesApi.deleteBackup(Configuration.VERSION, backupId);
    }

    /**
     * Starts creating a new index from a backup, with tags and deletion protection, and returns before the index
     * is ready.
     * <p>
     * This overload does not return the restore job. Use {@link #createIndexFromBackup(String, String)} when you
     * need the restore job ID to track progress, or {@link #listRestoreJobs(Integer)} to find the job later. The
     * backup must have the {@code Ready} status.
     *
     * <pre>{@code
     *     Map<String, String> tags = new HashMap<>();
     *     tags.put("env", "staging");
     *     pinecone.createIndexFromBackup(backupId, "product-catalog-restored", tags, "enabled");
     * }</pre>
     *
     * @param backupId           the ID of the backup to restore, from {@link BackupModel#getBackupId()}.
     * @param indexName          the name of the new index, for example {@code "product-catalog-restored"}. It must
     *                           not match an existing index.
     * @param tags               tags for the new index, for example {@code {"env": "staging"}}. Pass null to copy
     *                           the tags of the backup.
     * @param deletionProtection {@code "enabled"} to prevent the new index from being deleted until the setting is
     *                           turned off, or {@code "disabled"}. Pass null for {@code "disabled"}.
     * @throws ApiException if {@code backupId} is null, or the server rejects the request, for example because
     *                      the backup does not exist or is not ready, or the index name is taken or invalid.
     */
    public void createIndexFromBackup(String backupId, String indexName, Map<String, String> tags, String deletionProtection) throws ApiException {
        CreateIndexFromBackupRequest createIndexFromBackupRequest = new CreateIndexFromBackupRequest()
                .name(indexName)
                .tags(tags);

        if (deletionProtection != null) {
            createIndexFromBackupRequest.deletionProtection(deletionProtection);
        }
        manageIndexesApi.createIndexFromBackupOperation(Configuration.VERSION, backupId, createIndexFromBackupRequest);
    }

    /**
     * Starts creating a new index from a backup and returns the restore job, before the index is ready.
     * <p>
     * The new index gets the tags of the backup and has deletion protection disabled. Track progress with
     * {@link #describeRestoreJob(String)} or {@link #describeIndex(String)}. The backup must have the
     * {@code Ready} status. Use {@link #createIndexFromBackup(String, String, Map, String)} to set tags or
     * deletion protection.
     *
     * <pre>{@code
     *     CreateIndexFromBackupResponse response =
     *             pinecone.createIndexFromBackup(backupId, "product-catalog-restored");
     *     String restoreJobId = response.getRestoreJobId();
     * }</pre>
     *
     * @param backupId  the ID of the backup to restore, from {@link BackupModel#getBackupId()}.
     * @param indexName the name of the new index, for example {@code "product-catalog-restored"}. It must not match
     *                  an existing index.
     * @return the restore job ID from {@link CreateIndexFromBackupResponse#getRestoreJobId()} and the new index ID
     *         from {@link CreateIndexFromBackupResponse#getIndexId()}.
     * @throws ApiException if {@code backupId} is null, or the server rejects the request, for example because
     *                      the backup does not exist or is not ready, or the index name is taken or invalid.
     */
    public CreateIndexFromBackupResponse createIndexFromBackup(String backupId, String indexName) throws ApiException {
        CreateIndexFromBackupRequest createIndexFromBackupRequest = new CreateIndexFromBackupRequest()
                .name(indexName);
        return manageIndexesApi.createIndexFromBackupOperation(Configuration.VERSION, backupId, createIndexFromBackupRequest);
    }

    /**
     * Describes a restore job, the operation that builds an index from a backup.
     * <p>
     * Poll this after {@link #createIndexFromBackup(String, String)} until the status is {@code Completed} or
     * {@code Failed}.
     *
     * <pre>{@code
     *     RestoreJobModel job = pinecone.describeRestoreJob(restoreJobId);
     *     System.out.println(job.getStatus() + " " + job.getPercentComplete());
     * }</pre>
     *
     * @param jobId the ID of the restore job, from {@link CreateIndexFromBackupResponse#getRestoreJobId()}.
     * @return the restore job. {@link RestoreJobModel#getStatus()} is its status,
     *         {@link RestoreJobModel#getPercentComplete()} its progress, and
     *         {@link RestoreJobModel#getTargetIndexName()} the index being created.
     * @throws ApiException if {@code jobId} is null, or the server rejects the request, for example because the
     *                      restore job does not exist.
     */
    public RestoreJobModel describeRestoreJob(String jobId) throws ApiException {
        return manageIndexesApi.describeRestoreJob(Configuration.VERSION, jobId);
    }

    /**
     * Lists the first page of restore jobs in the project, with a chosen page size.
     * <p>
     * Use {@link #listRestoreJobs(Integer, String)} to fetch later pages.
     *
     * <pre>{@code
     *     for (RestoreJobModel job : pinecone.listRestoreJobs(25).getData()) {
     *         System.out.println(job.getTargetIndexName() + " " + job.getStatus());
     *     }
     * }</pre>
     *
     * @param limit the maximum number of restore jobs to return, for example {@code 25}. Pass null for the server
     *              default.
     * @return one page of restore jobs. {@link RestoreJobList#getData()} holds the jobs, and
     *         {@link RestoreJobList#getPagination()} holds the token for the next page.
     * @throws ApiException if the server rejects the request.
     */
    public RestoreJobList listRestoreJobs(Integer limit) throws ApiException {
        return manageIndexesApi.listRestoreJobs(Configuration.VERSION, limit, null);
    }

    /**
     * Lists the next page of restore jobs in the project, up to 10 jobs.
     * <p>
     * Use {@link #listRestoreJobs(Integer, String)} to choose the page size as well.
     *
     * <pre>{@code
     *     RestoreJobList nextPage = pinecone.listRestoreJobs(firstPage.getPagination().getNext());
     * }</pre>
     *
     * @param paginationToken the token from {@link RestoreJobList#getPagination()} of the previous page. Pass null
     *                        for the first page.
     * @return one page of restore jobs. {@link RestoreJobList#getData()} holds the jobs, and
     *         {@link RestoreJobList#getPagination()} holds the token for the next page.
     * @throws ApiException if the server rejects the request, for example because the token is invalid.
     */
    public RestoreJobList listRestoreJobs(String paginationToken) throws ApiException {
        return manageIndexesApi.listRestoreJobs(Configuration.VERSION, 10, paginationToken);
    }

    /**
     * Lists one page of restore jobs in the project.
     * <p>
     * Returns a single page. To read every restore job, call again with the token from the previous page until no
     * token comes back.
     *
     * <pre>{@code
     *     RestoreJobList page = pinecone.listRestoreJobs(25, null);
     * }</pre>
     *
     * @param limit           the maximum number of restore jobs to return. Pass null for the server default.
     * @param paginationToken the token from {@link RestoreJobList#getPagination()} of the previous page. Pass null
     *                        for the first page.
     * @return one page of restore jobs. {@link RestoreJobList#getData()} holds the jobs, and
     *         {@link RestoreJobList#getPagination()} holds the token for the next page.
     * @throws ApiException if the server rejects the request, for example because the token is invalid.
     */
    public RestoreJobList listRestoreJobs(Integer limit, String paginationToken) throws ApiException {
        return manageIndexesApi.listRestoreJobs(Configuration.VERSION, limit, paginationToken);
    }

    /**
     * Starts creating a collection, a static copy of a pod-based index's vectors and metadata.
     * <p>
     * Use a collection to back up a pod-based index or to create new pod-based indexes from it with
     * {@link #createPodsIndex(String, Integer, String, String, String, String) createPodsIndex}. The source index must
     * be pod-based and ready; for serverless indexes, use {@link #createBackup(String, String, String) createBackup}
     * instead. The method returns before the collection is ready: the returned collection's status is
     * {@code Initializing}, and you poll {@link #describeCollection(String)} until it is {@code Ready}.
     *
     * <pre>{@code
     *     CollectionModel collection = pinecone.createCollection("product-catalog-snapshot", "product-catalog");
     * }</pre>
     *
     * @param collectionName the name of the new collection, for example {@code "product-catalog-snapshot"}. Must not
     *                       be null or empty.
     * @param sourceIndex    the name of the pod-based index to copy, for example {@code "product-catalog"}. Must not
     *                       be null or empty.
     * @return the new collection. {@link CollectionModel#getStatus()} is {@code Initializing}, and
     *         {@link CollectionModel#getEnvironment()} and {@link CollectionModel#getDimension()} come from the
     *         source index.
     * @throws PineconeValidationException if {@code collectionName} or {@code sourceIndex} is null or empty. The
     *                                     client checks this before sending the request.
     * @throws PineconeNotFoundException   if the source index does not exist.
     * @throws PineconeBadRequestException if the server rejects the request, for example because the source index
     *                                     is serverless or not ready, or the collection name is invalid.
     * @throws PineconeForbiddenException  if the project has reached its collection quota.
     */
    public CollectionModel createCollection(String collectionName, String sourceIndex) throws PineconeException {
        if (collectionName == null || collectionName.isEmpty()) {
            throw new PineconeValidationException("collectionName cannot be null or empty");
        }
        if (sourceIndex == null || sourceIndex.isEmpty()) {
            throw new PineconeValidationException("sourceIndex cannot be null or empty");
        }

        CreateCollectionRequest createCollectionRequest = new CreateCollectionRequest()
                .name(collectionName)
                .source(sourceIndex);

        CollectionModel collection = null;
        try {
            collection = manageIndexesApi.createCollection(Configuration.VERSION, createCollectionRequest);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return collection;
    }

    /**
     * Describes a collection, including its status, size and vector count.
     * <p>
     * Poll this after {@link #createCollection(String, String)} until the status is {@code Ready}, the point at which
     * the collection can be used to create an index.
     *
     * <pre>{@code
     *     CollectionModel collection = pinecone.describeCollection("product-catalog-snapshot");
     *     if ("Ready".equals(collection.getStatus())) {
     *         System.out.println(collection.getVectorCount() + " vectors in " + collection.getEnvironment());
     *     }
     * }</pre>
     *
     * @param collectionName the name of the collection, for example {@code "product-catalog-snapshot"}.
     * @return the collection. {@link CollectionModel#getStatus()} is {@code Initializing}, {@code Ready} or
     *         {@code Terminating}; {@link CollectionModel#getEnvironment()} is the environment an index created from
     *         it must use. {@link CollectionModel#getSize()} and {@link CollectionModel#getVectorCount()} can be
     *         null while the collection is initializing.
     * @throws PineconeNotFoundException if the collection does not exist.
     */
    public CollectionModel describeCollection(String collectionName) throws PineconeException {
        CollectionModel collection = null;
        try {
            collection = manageIndexesApi.describeCollection(Configuration.VERSION, collectionName);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return collection;
    }

    /**
     * Lists every collection in the project.
     * <p>
     * One call returns all collections; there is no pagination.
     *
     * <pre>{@code
     *     for (CollectionModel collection : pinecone.listCollections().getCollections()) {
     *         System.out.println(collection.getName() + " " + collection.getStatus());
     *     }
     * }</pre>
     *
     * @return the collections. {@link CollectionList#getCollections()} holds one {@link CollectionModel} per
     *         collection, with the same fields as {@link #describeCollection(String)}.
     */
    public CollectionList listCollections() throws PineconeException {
        CollectionList collections = null;
        try {
            collections = manageIndexesApi.listCollections(Configuration.VERSION);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return collections;
    }

    /**
     * Starts deleting a collection. The deletion is permanent.
     * <p>
     * The method returns before the deletion finishes: the collection's status becomes {@code Terminating}, and once
     * it is gone {@link #describeCollection(String)} throws {@link PineconeNotFoundException}.
     *
     * <pre>{@code
     *     pinecone.deleteCollection("product-catalog-snapshot");
     * }</pre>
     *
     * @param collectionName the name of the collection to delete, for example {@code "product-catalog-snapshot"}.
     * @throws PineconeNotFoundException if the collection does not exist.
     */
    public void deleteCollection(String collectionName) throws PineconeException {
        try {
            manageIndexesApi.deleteCollection(Configuration.VERSION, collectionName);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
    }

    /**
     * Returns an {@link Index} for upserting, querying and otherwise working with the data in one index.
     * <p>
     * Each call describes the index to look up its host, so it makes one control-plane request and fails if the
     * index does not exist. Call it once per index and reuse the result. Every {@code Index} and {@link AsyncIndex}
     * returned for the same index name, API key, index host, TLS setting, source tag and response metadata listener
     * shares one gRPC channel, across all {@code Pinecone} instances in the JVM; a client with a different API key
     * or other settings gets its own channel. Use {@link #getAsyncIndexConnection(String)} for non-blocking calls.
     *
     * <pre>{@code
     *     Index index = pinecone.getIndexConnection("product-catalog");
     *     index.upsert("sku-1001", Arrays.asList(0.12f, 0.48f, 0.91f));
     * }</pre>
     *
     * @param indexName the name of the index to connect to, for example {@code "product-catalog"}.
     * @return the index connection. Call {@link Index#close()} when you are done with it; the shared channel shuts
     *         down once every {@code Index} and {@code AsyncIndex} using it is closed.
     * @throws PineconeValidationException if {@code indexName} is null or empty.
     * @throws PineconeNotFoundException if the index does not exist.
     */
    public Index getIndexConnection(String indexName) throws PineconeValidationException {
        if(indexName == null || indexName.isEmpty()) {
            throw new PineconeValidationException("Index name cannot be null or empty");
        }

        PineconeConfig perConnectionConfig = new PineconeConfig(config.getApiKey(), config.getSourceTag());
        perConnectionConfig.setHost(getIndexHost(indexName));
        perConnectionConfig.setTLSEnabled(config.isTLSEnabled());
        if (config.getResponseMetadataListener() != null) {
            perConnectionConfig.setResponseMetadataListener(config.getResponseMetadataListener());
        }

        PineconeConnection connection = getConnection(indexName, perConnectionConfig);
        try {
            return new Index(perConnectionConfig, connection, indexName);
        } catch (RuntimeException e) {
            releaseConnection(connection);
            throw e;
        }
    }

    /**
     * Returns an {@link AsyncIndex} for working with the data in one index through non-blocking calls that return
     * {@code ListenableFuture}.
     * <p>
     * It behaves like {@link #getIndexConnection(String)}: each call describes the index to look up its host, and
     * it shares one gRPC channel with every {@link Index} and {@code AsyncIndex} returned for the same index and the
     * same client settings. Use {@link #getIndexConnection(String)} for blocking calls.
     *
     * <pre>{@code
     *     AsyncIndex asyncIndex = pinecone.getAsyncIndexConnection("product-catalog");
     *     ListenableFuture<UpsertResponse> future =
     *             asyncIndex.upsert("sku-1001", Arrays.asList(0.12f, 0.48f, 0.91f));
     *     System.out.println(future.get().getUpsertedCount());
     * }</pre>
     *
     * @param indexName the name of the index to connect to, for example {@code "product-catalog"}.
     * @return the asynchronous index connection. Call {@link AsyncIndex#close()} when you are done with it; the
     *         shared channel shuts down once every {@code Index} and {@code AsyncIndex} using it is closed.
     * @throws PineconeValidationException if {@code indexName} is null or empty.
     * @throws PineconeNotFoundException if the index does not exist.
     */
    public AsyncIndex getAsyncIndexConnection(String indexName) throws PineconeValidationException {
        if(indexName == null || indexName.isEmpty()) {
            throw new PineconeValidationException("Index name cannot be null or empty");
        }

        PineconeConfig perConnectionConfig = new PineconeConfig(config.getApiKey(), config.getSourceTag());
        perConnectionConfig.setHost(getIndexHost(indexName));
        perConnectionConfig.setTLSEnabled(config.isTLSEnabled());
        if (config.getResponseMetadataListener() != null) {
            perConnectionConfig.setResponseMetadataListener(config.getResponseMetadataListener());
        }

        PineconeConnection connection = getConnection(indexName, perConnectionConfig);
        try {
            return new AsyncIndex(perConnectionConfig, connection, indexName);
        } catch (RuntimeException e) {
            releaseConnection(connection);
            throw e;
        }
    }

    /**
     * Returns a new {@link Inference} client for generating embeddings, reranking documents and listing the
     * hosted models.
     * <p>
     * The client uses this instance's API key, source tag, proxy and custom {@code OkHttpClient}, and always sends
     * requests to Pinecone's public API. Each call builds a new client, with its own HTTP client unless a custom
     * {@code OkHttpClient} is set, so call it once and reuse the result.
     *
     * <pre>{@code
     *     Inference inference = pinecone.getInferenceClient();
     *     Map<String, Object> parameters = new HashMap<>();
     *     parameters.put("input_type", "passage");
     *     EmbeddingsList embeddings = inference.embed("multilingual-e5-large", parameters,
     *             Arrays.asList("Wireless noise-cancelling headphones"));
     * }</pre>
     *
     * @return a new inference client.
     */
    public Inference getInferenceClient() {
        return new Inference(config);
    }

    PineconeConnection getConnection(String indexName, PineconeConfig perConnectionConfig) {
        List<Object> key = Arrays.asList(indexName, perConnectionConfig.getApiKey(), perConnectionConfig.getHost(),
                perConnectionConfig.isTLSEnabled(), perConnectionConfig.getUserAgent(),
                perConnectionConfig.getResponseMetadataListener());
        synchronized (connectionsMap) {
            SharedConnection shared = connectionsMap.computeIfAbsent(key,
                    k -> new SharedConnection(new PineconeConnection(perConnectionConfig, indexName)));
            shared.users++;
            return shared.connection;
        }
    }

    static Collection<PineconeConnection> openConnections() {
        synchronized (connectionsMap) {
            List<PineconeConnection> connections = new ArrayList<>();
            for (SharedConnection shared : connectionsMap.values()) {
                connections.add(shared.connection);
            }
            return connections;
        }
    }

    String getIndexHost(String indexName) {
        return this.describeIndex(indexName).getHost();
    }

    /**
     * Closes {@code connection} once no other {@link Index} or {@link AsyncIndex} obtained from
     * {@link #getIndexConnection} or {@link #getAsyncIndexConnection} still uses it.
     */
    static void releaseConnection(PineconeConnection connection) {
        synchronized (connectionsMap) {
            for (Iterator<SharedConnection> it = connectionsMap.values().iterator(); it.hasNext(); ) {
                SharedConnection shared = it.next();
                if (shared.connection == connection) {
                    if (--shared.users > 0) {
                        return;
                    }
                    it.remove();
                    break;
                }
            }
        }
        connection.close();
    }

    private static final class SharedConnection {
        final PineconeConnection connection;
        int users;

        SharedConnection(PineconeConnection connection) {
            this.connection = connection;
        }
    }

    private void handleApiException(ApiException apiException) throws PineconeException {
        int statusCode = apiException.getCode();
        String responseBody = apiException.getResponseBody();
        FailedRequestInfo failedRequestInfo = new FailedRequestInfo(statusCode, responseBody);
        HttpErrorMapper.mapHttpStatusError(failedRequestInfo, apiException);
    }

    /**
     * Configures and builds a {@link Pinecone} client.
     * <p>
     * Only the API key is required. The defaults suit most applications: requests go to Pinecone's public API over
     * TLS through a client-managed HTTP client. Each {@code with*} method returns the builder for chaining;
     * {@link #build()} checks the settings and makes no network calls.
     *
     * <pre>{@code
     * Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY"))
     *     .withSourceTag("acme_search")
     *     .build();
     * }</pre>
     */
    public static class Builder {
        // Required fields
        private final String apiKey;

        // Optional fields
        private String host;
        private String sourceTag;
        private ProxyConfig proxyConfig;
        private OkHttpClient customOkHttpClient;
        private boolean enableTls = true;
        private ResponseMetadataListener responseMetadataListener;

        /**
         * Starts a builder for a client that authenticates with the given API key.
         * <p>
         * The key is checked when you call {@link #build()}, not here.
         *
         * <pre>{@code
         * Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
         * }</pre>
         *
         * @param apiKey Your Pinecone API key, sent with every control-plane, data-plane and inference request.
         *               Read it from the environment or a secret store rather than hard-coding it. A null or empty
         *               key makes {@link #build()} throw.
         */
        public Builder(String apiKey) {
            this.apiKey = apiKey;
        }

        /**
         * Sets a source tag that identifies your application or integration in the {@code User-Agent} of every
         * request.
         * <p>
         * Partners building on Pinecone use it so Pinecone can attribute API activity to them; see
         * <a href="https://docs.pinecone.io/integrations/build-integration/attribute-api-activity">Attribute API
         * activity</a>. The client sends the tag exactly as given, so pass it already in the expected form:
         * lowercase letters, digits, underscores and colons.
         *
         * <pre>{@code
         * Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY"))
         *     .withSourceTag("acme_search")
         *     .build();
         * }</pre>
         *
         * @param sourceTag The tag to send, for example {@code "acme_search"}. Null or empty (the default) sends no
         *                  tag.
         * @return This builder.
         */
        public Builder withSourceTag(String sourceTag) {
            this.sourceTag = sourceTag;
            return this;
        }

        /**
         * Supplies your own {@link OkHttpClient} for control-plane and inference requests, for example to set
         * timeouts or add interceptors.
         * <p>
         * Requests from an {@link Index} or {@link AsyncIndex} do not use it. When a custom client is set, the client
         * ignores {@link #withHost(String)} and sends control-plane requests to Pinecone's public API. It cannot be
         * combined with {@link #withProxy(String, int)}: configure the proxy on your {@code OkHttpClient} instead.
         *
         * <pre>{@code
         * import java.util.concurrent.TimeUnit;
         * import okhttp3.OkHttpClient;
         *
         * OkHttpClient httpClient = new OkHttpClient.Builder()
         *     .readTimeout(60, TimeUnit.SECONDS)
         *     .build();
         * Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY"))
         *     .withOkHttpClient(httpClient)
         *     .build();
         * }</pre>
         *
         * @param okHttpClient The HTTP client to use. Null (the default) lets the client build its own.
         * @return This builder.
         */
        public Builder withOkHttpClient(OkHttpClient okHttpClient) {
            this.customOkHttpClient = okHttpClient;
            return this;
        }

        /**
         * Routes control-plane and inference requests through an HTTP proxy.
         * <p>
         * Index connections do not go through the proxy: upserts, queries and other data-plane calls on an
         * {@link Index} or {@link AsyncIndex} connect to the index host directly. Cannot be combined with
         * {@link #withOkHttpClient(OkHttpClient)}.
         *
         * <pre>{@code
         * Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY"))
         *     .withProxy("proxy.example.com", 8080)
         *     .build();
         * }</pre>
         *
         * @param proxyHost The proxy's hostname or IP address, for example {@code "proxy.example.com"}. Must not be
         *                  null or empty.
         * @param proxyPort The proxy's port, for example {@code 8080}. Must be greater than 0.
         * @return This builder.
         */
        public Builder withProxy(String proxyHost, int proxyPort) {
            this.proxyConfig = new ProxyConfig(proxyHost, proxyPort);
            return this;
        }

        /**
         * Sends control-plane requests to a different base URL, such as a local emulator.
         * <p>
         * Only control-plane requests use this URL. Index connections go to the host the control plane reports for
         * each index, and inference requests go to Pinecone's public API. The client ignores this setting when
         * {@link #withOkHttpClient(OkHttpClient)} is also set.
         *
         * <pre>{@code
         * Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY"))
         *     .withHost("http://localhost:5080")
         *     .build();
         * }</pre>
         *
         * @param host The base URL including the scheme, for example {@code "http://localhost:5080"}. Null or empty
         *             (the default) uses Pinecone's public API.
         * @return This builder.
         */
        public Builder withHost(String host) {
            this.host = host;
            return this;
        }

        /**
         * Turns TLS on or off for requests an {@link Index} or {@link AsyncIndex} sends to the index host.
         * <p>
         * Turn it off only to reach an index that does not serve TLS, such as one in a local emulator. Control-plane
         * and inference requests are unaffected; their scheme comes from the base URL.
         *
         * <pre>{@code
         * Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY"))
         *     .withHost("http://localhost:5080")
         *     .withTlsEnabled(false)
         *     .build();
         * }</pre>
         *
         * @param enableTls {@code true} (the default) to use TLS, {@code false} to connect in plain text.
         * @return This builder.
         */
        public Builder withTlsEnabled(boolean enableTls) {
            this.enableTls = enableTls;
            return this;
        }

        /**
         * Registers a listener that receives timing and status for each data-plane call, for logging, custom
         * metrics or OpenTelemetry.
         * <p>
         * The listener runs after each upsert, query, fetch, update, delete and list call on an {@link Index} or
         * {@link AsyncIndex}; control-plane and inference calls do not report to it. Index connections from clients
         * with different listeners do not share a gRPC channel, so each client's calls report to its own listener.
         *
         * <pre>{@code
         * Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY"))
         *     .withResponseMetadataListener(metadata ->
         *         System.out.println(metadata.getOperationName() + " took "
         *             + metadata.getClientDurationMs() + " ms"))
         *     .build();
         * }</pre>
         *
         * @param listener Receives a {@link io.pinecone.configs.ResponseMetadata} per call. Null (the default)
         *                 disables reporting.
         * @return This builder.
         */
        public Builder withResponseMetadataListener(ResponseMetadataListener listener) {
            this.responseMetadataListener = listener;
            return this;
        }

        /**
         * Checks the settings and builds the client.
         * <p>
         * Makes no network calls. If the {@code PINECONE_DEBUG} environment variable is {@code true}, the client
         * logs every control-plane request and response in full, with the {@code Api-Key} header value replaced by
         * {@code ██}. Logging interceptors you add to your own {@code OkHttpClient} are not changed and can still
         * log the key.
         *
         * <pre>{@code
         * Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
         * }</pre>
         *
         * @return A new {@link Pinecone} client.
         * @throws PineconeConfigurationException If the API key is null or empty, the proxy host or port is invalid,
         *                                        or both a proxy and a custom {@code OkHttpClient} are set.
         */
        public Pinecone build() {
            PineconeConfig config = new PineconeConfig(apiKey, sourceTag, proxyConfig, customOkHttpClient);
            config.setTLSEnabled(enableTls);
            if (responseMetadataListener != null) {
                config.setResponseMetadataListener(responseMetadataListener);
            }
            config.validate();

            if (proxyConfig != null && customOkHttpClient != null) {
                throw new PineconeConfigurationException("Invalid configuration: Both Custom OkHttpClient and Proxy are set. Please configure only one of these options.");
            }

            ApiClient apiClient;
            if (customOkHttpClient != null) {
                apiClient = new ApiClient(customOkHttpClient);
            } else {
                apiClient = new ApiClient(buildOkHttpClient(proxyConfig));
                if(host!=null && !host.isEmpty()) {
                    config.setHost(host);
                    apiClient.setBasePath(host);
                }
            }
            apiClient.setApiKey(config.getApiKey());
            apiClient.setUserAgent(config.getUserAgent());
            apiClient.addDefaultHeader("X-Pinecone-Api-Version", Configuration.VERSION);

            if (Boolean.parseBoolean(System.getenv("PINECONE_DEBUG"))) {
                enableDebugLogging(apiClient);
            }

            ManageIndexesApi manageIndexesApi = new ManageIndexesApi();
            manageIndexesApi.setApiClient(apiClient);

            return new Pinecone(config, manageIndexesApi);
        }
    }

    static void enableDebugLogging(ApiClient apiClient) {
        apiClient.setDebugging(true);
        List<Interceptor> interceptors = apiClient.getHttpClient().interceptors();
        Interceptor added = interceptors.get(interceptors.size() - 1);
        if (added instanceof HttpLoggingInterceptor) {
            ((HttpLoggingInterceptor) added).redactHeader("Api-Key");
        }
    }

    static OkHttpClient buildOkHttpClient(ProxyConfig proxyConfig) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if(proxyConfig != null) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyConfig.getHost(), proxyConfig.getPort()));
            builder.proxy(proxy);
        }
        return builder.build();
    }
}
