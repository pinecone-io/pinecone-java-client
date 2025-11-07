package io.pinecone.clients;

import io.pinecone.configs.PineconeConfig;
import io.pinecone.configs.PineconeConnection;
import io.pinecone.configs.ProxyConfig;
import io.pinecone.exceptions.*;
import okhttp3.OkHttpClient;
import org.openapitools.db_control.client.ApiClient;
import org.openapitools.db_control.client.ApiException;
import org.openapitools.db_control.client.Configuration;
import org.openapitools.db_control.client.api.ManageIndexesApi;
import org.openapitools.db_control.client.model.*;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Pinecone class is the main entry point for interacting with Pinecone via the Java SDK.
 * It is used to create, delete, and manage your indexes and collections, along with the inference api.
 * Note that the Pinecone class instantiates a single shared {@link OkHttpClient} instance,
 * which is used for both control plane and inference operations.The OkHttpClient performs best when you create a single
 * `OkHttpClient` instance and reuse it for all of your HTTP calls. This is because each client holds its own connection
 * pool and thread pools. Reusing connections and threads reduces latency and saves memory. Conversely, creating a
 * client for each request wastes resources on idle pools.
 * <p>
 * To instantiate the Pinecone class, use the {@link Pinecone.Builder} class to pass
 * an API key and any other optional configuration.
 *
 * <pre>{@code
 *     import io.pinecone.clients.Pinecone;
 *
 *     Pinecone client = new Pinecone.Builder("PINECONE_API_KEY").build();
 *
 *     // Use the client to interact with Pinecone
 *     client.describeIndex("YOUR-INDEX");
 *
 *     // Use the client to obtain an Index or AsyncIndex instance to upsert or query
 *     Index index = client.getIndexConnection("YOUR-INDEX");
 *     index.upsert(...);
 * }</pre>
 */
public class Pinecone {

    private static final ConcurrentHashMap<String, PineconeConnection> connectionsMap = new ConcurrentHashMap<>();
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
     * Creates a new serverless index with the specified parameters.
     * <p>
     * Example:
     * <pre>{@code 
     *     client.createServerlessIndex("YOUR-INDEX", "cosine", 1536, "aws", "us-west-2", DeletionProtection.ENABLED);
     * }</pre>
     *
     * @param indexName The name of the index to be created.
     * @param metric The metric type for the index. Must be one of "cosine", "euclidean", or "dotproduct".
     * @param dimension The number of dimensions for the index.
     * @param cloud The cloud provider for the index.
     * @param region The cloud region for the index.
     * @param deletionProtection Enable or disable deletion protection for the index.
     * @param tags A map of tags to associate with the Index.
     * @return {@link IndexModel} representing the created serverless index.
     * @throws PineconeException if the API encounters an error during index creation or if any of the arguments are invalid.
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
     * Creates a new serverless index with the specified parameters, including optional read capacity and metadata schema configuration.
     * <p>
     * This method allows you to configure dedicated read capacity nodes for better performance and cost predictability,
     * and to limit metadata indexing to specific fields for improved performance.
     * <p>
     * Example - Create index with OnDemand read capacity (default):
     * <pre>{@code
     *     import org.openapitools.db_control.client.model.ReadCapacity;
     *     import org.openapitools.db_control.client.model.ReadCapacityOnDemandSpec;
     *     ...
     *     
     *     ReadCapacity readCapacity = new ReadCapacity(new ReadCapacityOnDemandSpec().mode("OnDemand"));
     *     client.createServerlessIndex("YOUR-INDEX", "cosine", 1536, "aws", "us-west-2", 
     *                                  DeletionProtection.ENABLED, null, readCapacity, null);
     * }</pre>
     * <p>
     * Example - Create index with Dedicated read capacity:
     * <pre>{@code
     *     import org.openapitools.db_control.client.model.ReadCapacity;
     *     import org.openapitools.db_control.client.model.ReadCapacityDedicatedSpec;
     *     import org.openapitools.db_control.client.model.ReadCapacityDedicatedConfig;
     *     import org.openapitools.db_control.client.model.ScalingConfigManual;
     *     ...
     *     
     *     ScalingConfigManual manual = new ScalingConfigManual().shards(2).replicas(2);
     *     ReadCapacityDedicatedConfig dedicated = new ReadCapacityDedicatedConfig()
     *         .nodeType("t1")
     *         .scaling("Manual")
     *         .manual(manual);
     *     ReadCapacity readCapacity = new ReadCapacity(
     *         new ReadCapacityDedicatedSpec().mode("Dedicated").dedicated(dedicated));
     *     client.createServerlessIndex("YOUR-INDEX", "cosine", 1536, "aws", "us-west-2", 
     *                                  DeletionProtection.ENABLED, null, readCapacity, null);
     * }</pre>
     * <p>
     * Example - Create index with metadata schema:
     * <pre>{@code
     *     import org.openapitools.db_control.client.model.BackupModelSchema;
     *     import org.openapitools.db_control.client.model.BackupModelSchemaFieldsValue;
     *     ...
     *     
     *     Map<String, BackupModelSchemaFieldsValue> fields = new HashMap<>();
     *     fields.put("genre", new BackupModelSchemaFieldsValue().filterable(true));
     *     fields.put("year", new BackupModelSchemaFieldsValue().filterable(true));
     *     BackupModelSchema schema = new BackupModelSchema().fields(fields);
     *     client.createServerlessIndex("YOUR-INDEX", "cosine", 1536, "aws", "us-west-2", 
     *                                  DeletionProtection.ENABLED, null, null, schema);
     * }</pre>
     *
     * @param indexName The name of the index to be created.
     * @param metric The metric type for the index. Must be one of "cosine", "euclidean", or "dotproduct".
     * @param dimension The number of dimensions for the index.
     * @param cloud The cloud provider for the index.
     * @param region The cloud region for the index.
     * @param deletionProtection Enable or disable deletion protection for the index.
     * @param tags A map of tags to associate with the Index.
     * @param readCapacity The read capacity configuration. If null, defaults to OnDemand mode.
     *                     Use {@link ReadCapacityOnDemandSpec} for OnDemand or {@link ReadCapacityDedicatedSpec} for Dedicated mode.
     * @param schema The metadata schema configuration. If null, all metadata fields are indexed.
     *               Use this to limit metadata indexing to specific fields for improved performance.
     * @return {@link IndexModel} representing the created serverless index.
     * @throws PineconeException if the API encounters an error during index creation or if any of the arguments are invalid.
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
     * Creates a new sparse serverless index.
     * <p>
     * Example:
     * <pre>{@code
     *     client.createServerlessIndex("YOUR-INDEX", "cosine", 1536, "aws", "us-west-2", DeletionProtection.ENABLED);
     * }</pre>
     *
     * @param indexName The name of the index to be created.
     * @param cloud The cloud provider for the index.
     * @param region The cloud region for the index.
     * @param deletionProtection Enable or disable deletion protection for the index.
     * @param tags A map of tags to associate with the Index.
     * @param vectorType The metric type for the index. Must be one of "cosine", "euclidean", or "dotproduct".
     * @return {@link IndexModel} representing the created serverless index.
     * @throws PineconeException if the API encounters an error during index creation or if any of the arguments are invalid.
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
     * Creates a new serverless index with an associated embedding model.
     * <p>
     * Example:
     * <pre>{@code
     *     client.createIndexForModel("my-index", CreateIndexForModelRequest.CloudEnum.AWS,
     *                                            "us-west-2", embedConfig, DeletionProtection.DISABLED, tags);
     * }</pre>
     *
     * @param name The name of the index to be created. The name must be between 1 and 45 characters,
     *             start and end with an alphanumeric character, and consist only of lowercase alphanumeric
     *             characters or hyphens ('-').
     * @param cloud The cloud provider where the index will be hosted. Must be one of the supported cloud providers.
     * @param region The cloud region where the index will be created.
     * @param embed The embedding model configuration. Once set, the model cannot be changed, but configurations
     *              such as field map and parameters can be updated.
     * @param deletionProtection Whether deletion protection is enabled for the index. If enabled, the index
     *                           cannot be deleted. Defaults to disabled if not provided.
     * @param tags A map of custom user tags to associate with the index. Keys must be alphanumeric or contain
     *             underscores ('_') or hyphens ('-'). Values must be alphanumeric, or contain characters such
     *             as ';', '@', '_', '-', '.', '+', or spaces.
     * @return {@link IndexModel} representing the created serverless index with the associated embedding model.
     * @throws PineconeException if the API encounters an error during index creation, or if any of the arguments
     *                           are invalid.
     * @throws ApiException if an error occurs while communicating with the API.
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
     * Creates a new serverless index with an associated embedding model, including optional read capacity and metadata schema configuration.
     * <p>
     * This method allows you to configure dedicated read capacity nodes for better performance and cost predictability,
     * and to limit metadata indexing to specific fields for improved performance.
     * <p>
     * Example - Create index for model with Dedicated read capacity:
     * <pre>{@code
     *     import org.openapitools.db_control.client.model.ReadCapacity;
     *     import org.openapitools.db_control.client.model.ReadCapacityDedicatedSpec;
     *     import org.openapitools.db_control.client.model.ReadCapacityDedicatedConfig;
     *     import org.openapitools.db_control.client.model.ScalingConfigManual;
     *     ...
     *     
     *     ScalingConfigManual manual = new ScalingConfigManual().shards(1).replicas(1);
     *     ReadCapacityDedicatedConfig dedicated = new ReadCapacityDedicatedConfig()
     *         .nodeType("t1")
     *         .scaling("Manual")
     *         .manual(manual);
     *     ReadCapacity readCapacity = new ReadCapacity(
     *         new ReadCapacityDedicatedSpec().mode("Dedicated").dedicated(dedicated));
     *     
     *     CreateIndexForModelRequestEmbed embed = new CreateIndexForModelRequestEmbed();
     *     embed.model("multilingual-e5-large");
     *     Map<String, String> fieldMap = new HashMap<>();
     *     fieldMap.put("text", "my-sample-text");
     *     embed.fieldMap(fieldMap);
     *     
     *     client.createIndexForModel("my-index", "aws", "us-east-1", embed, 
     *                                DeletionProtection.DISABLED, null, readCapacity, null);
     * }</pre>
     * <p>
     * Example - Create index for model with metadata schema:
     * <pre>{@code
     *     import org.openapitools.db_control.client.model.BackupModelSchema;
     *     import org.openapitools.db_control.client.model.BackupModelSchemaFieldsValue;
     *     ...
     *     
     *     Map<String, BackupModelSchemaFieldsValue> fields = new HashMap<>();
     *     fields.put("category", new BackupModelSchemaFieldsValue().filterable(true));
     *     fields.put("tags", new BackupModelSchemaFieldsValue().filterable(true));
     *     BackupModelSchema schema = new BackupModelSchema().fields(fields);
     *     
     *     CreateIndexForModelRequestEmbed embed = new CreateIndexForModelRequestEmbed();
     *     embed.model("multilingual-e5-large");
     *     Map<String, String> fieldMap = new HashMap<>();
     *     fieldMap.put("text", "my-sample-text");
     *     embed.fieldMap(fieldMap);
     *     
     *     client.createIndexForModel("my-index", "aws", "us-east-1", embed, 
     *                                DeletionProtection.DISABLED, null, null, schema);
     * }</pre>
     *
     * @param name The name of the index to be created. The name must be between 1 and 45 characters,
     *             start and end with an alphanumeric character, and consist only of lowercase alphanumeric
     *             characters or hyphens ('-').
     * @param cloud The cloud provider where the index will be hosted. Must be one of the supported cloud providers.
     * @param region The cloud region where the index will be created.
     * @param embed The embedding model configuration. Once set, the model cannot be changed, but configurations
     *              such as field map and parameters can be updated.
     * @param deletionProtection Whether deletion protection is enabled for the index. If enabled, the index
     *                           cannot be deleted. Defaults to disabled if not provided.
     * @param tags A map of custom user tags to associate with the index. Keys must be alphanumeric or contain
     *             underscores ('_') or hyphens ('-'). Values must be alphanumeric, or contain characters such
     *             as ';', '@', '_', '-', '.', '+', or spaces.
     * @param readCapacity The read capacity configuration. If null, defaults to OnDemand mode.
     *                     Use {@link ReadCapacityOnDemandSpec} for OnDemand or {@link ReadCapacityDedicatedSpec} for Dedicated mode.
     * @param schema The metadata schema configuration. If null, all metadata fields are indexed.
     *               Use this to limit metadata indexing to specific fields for improved performance.
     * @return {@link IndexModel} representing the created serverless index with the associated embedding model.
     * @throws PineconeException if the API encounters an error during index creation, or if any of the arguments
     *                           are invalid.
     * @throws ApiException if an error occurs while communicating with the API.
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
     * Overload for creating a new pods index with environment and podType, the minimum required parameters.
     * <p>
     * Example:
     * <pre>{@code 
     *     client.createPodsIndex("YOUR-INDEX", 1536, "us-east4-gcp", "p1.x2");
     * }</pre>
     *
     * @param indexName The name of the index to be created.
     * @param dimension The number of dimensions for the index.
     * @param environment The cloud environment where you want the index to be hosted.
     * @param podType The type of pod to use. A string with one of s1, p1, or p2 appended with a "." and one of x1, x2, x4, or x8.
     * @return {@link IndexModel} representing the created serverless index.
     * @throws PineconeException if the API encounters an error during index creation or if any of the arguments are invalid.
     */
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment, String podType) {
        return createPodsIndex(indexName, dimension, environment, podType, null, null, null,
                null, null, null, "disabled", null);
    }

    /**
     * Overload for creating a new pods index with environment, podType, and deletion protection.
     * <p>
     * Example:
     * <pre>{@code
     *     client.createPodsIndex("YOUR-INDEX", 1536, "us-east4-gcp", "p1.x2", DeletionProtection.ENABLED);
     * }</pre>
     *
     * @param indexName The name of the index to be created.
     * @param dimension The number of dimensions for the index.
     * @param environment The cloud environment where you want the index to be hosted.
     * @param podType The type of pod to use. A string with one of s1, p1, or p2 appended with a "." and one of x1, x2, x4, or x8.
     * @param deletionProtection Enable or disable deletion protection for the index.
     * @return {@link IndexModel} representing the created serverless index.
     * @throws PineconeException if the API encounters an error during index creation or if any of the arguments are invalid.
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
     * Overload for creating a new pods index with environment, podType, metric, and metadataConfig.
     * <p>
     * Example:
     * <pre>{@code 
     *     import org.openapitools.control.client.model.PodSpecMetadataConfig;
     *     ...
     *
     *     PodSpecMetadataConfig metadataConfig =
     *         new PodSpecMetadataConfig()
     *         .fields(Arrays.asList("genre", "year"));
     *
     *     client.createPodsIndex("YOUR-INDEX", 1536, "us-east4-gcp", "p1.x2", "cosine", metadataConfig);
     * }</pre>
     *
     * @param indexName The name of the index to be created.
     * @param dimension The number of dimensions for the index.
     * @param environment The cloud environment where you want the index to be hosted.
     * @param podType The type of pod to use. A string with one of s1, p1, or p2 appended with a "." and one of x1, x2, x4, or x8.
     * @param metric The metric type for the index. Must be one of "cosine", "euclidean", or "dotproduct".
     * @param metadataConfig The configuration for the behavior of Pinecone's internal metadata index. By default, all metadata is indexed;
     *                       when metadataConfig is present, only specified metadata fields are indexed.
     * @return {@link IndexModel} representing the created serverless index.
     * @throws PineconeException if the API encounters an error during index creation or if any of the arguments are invalid.
     */
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment,
                                      String podType, String metric, PodSpecMetadataConfig metadataConfig) {
        return createPodsIndex(indexName, dimension, environment, podType, metric, null, null, null,
                metadataConfig,null, "disabled", null);
    }

    /**
     * Overload for creating a new pods index with environment, podType, metric, and sourceCollection.
     * <p>
     * Example:
     * <pre>{@code 
     *     client.createPodsIndex("YOUR-INDEX", 1536, "us-east4-gcp", "p1.x2", "cosine", "my-collection");
     * }</pre>
     *
     * @param indexName The name of the index to be created.
     * @param dimension The number of dimensions for the index.
     * @param environment The cloud environment where you want the index to be hosted.
     * @param podType The type of pod to use. A string with one of s1, p1, or p2 appended with a "." and one of x1, x2, x4, or x8.
     * @param metric The metric type for the index. Must be one of "cosine", "euclidean", or "dotproduct".
     * @param sourceCollection The name of the collection to be used as the source for the index. Collections are snapshots of an index at a point in time.
     * @return {@link IndexModel} representing the created serverless index.
     * @throws PineconeException if the API encounters an error during index creation or if any of the arguments are invalid.
     */
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment,
                                      String podType, String metric, String sourceCollection) {
        return createPodsIndex(indexName, dimension, environment, podType, metric, null, null, null, null,
                sourceCollection, "disabled", null);
    }

    /**
     * Overload for creating a new pods index with environment, podType, and pods.
     * <p>
     * Example:
     * <pre>{@code 
     *     client.createPodsIndex("YOUR-INDEX", 1536, "us-east4-gcp", "p1.x2", "cosine", 6);
     * }</pre>
     *
     * @param indexName The name of the index to be created.
     * @param dimension The number of dimensions for the index.
     * @param environment The cloud environment where you want the index to be hosted.
     * @param podType The type of pod to use. A string with one of s1, p1, or p2 appended with a "." and one of x1, x2, x4, or x8.
     * @param pods The number of pods to be used in the index.
     * @return {@link IndexModel} representing the created serverless index.
     * @throws PineconeException if the API encounters an error during index creation or if any of the arguments are invalid.
     */
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment,
                                      String podType, Integer pods) {
        return createPodsIndex(indexName, dimension, environment, podType, null, null, null, pods,
                null, null, "disabled", null);
    }

    /**
     * Overload for creating a new pods index with environment, podType, pods, and metadataConfig.
     * <p>
     * Example:
     * <pre>{@code 
     *     import org.openapitools.control.client.model.PodSpecMetadataConfig;
     *     ...
     *
     *     PodSpecMetadataConfig metadataConfig =
     *         new PodSpecMetadataConfig()
     *         .fields(Arrays.asList("genre", "year"));
     *     client.createPodsIndex("YOUR-INDEX", 1536, "us-east4-gcp", "p1.x2", "cosine", 6);
     * }</pre>
     *
     * @param indexName The name of the index to be created.
     * @param dimension The number of dimensions for the index.
     * @param environment The cloud environment where you want the index to be hosted.
     * @param podType The type of pod to use. A string with one of s1, p1, or p2 appended with a "." and one of x1, x2, x4, or x8.
     * @param pods The number of pods to be used in the index.
     * @return {@link IndexModel} representing the created serverless index.
     * @throws PineconeException if the API encounters an error during index creation or if any of the arguments are invalid.
     */
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment,
                                      String podType, Integer pods,
                                      PodSpecMetadataConfig metadataConfig) {
        return createPodsIndex(indexName, dimension, environment, podType, null, null, null, pods, metadataConfig,
                null, "disabled", null);
    }

    /**
     * Overload for creating a new pods index with environment, podType, replicas, and shards.
     * <p>
     * Example:
     * <pre>{@code
     *     client.createPodsIndex("YOUR-INDEX", 1536, "us-east4-gcp", "p1.x2", "cosine", 2, 2);
     * }</pre>
     *
     * @param indexName The name of the index to be created.
     * @param dimension The number of dimensions for the index.
     * @param environment The cloud environment where you want the index to be hosted.
     * @param podType The type of pod to use. A string with one of s1, p1, or p2 appended with a "." and one of x1, x2, x4, or x8.
     * @param replicas The number of replicas. Replicas duplicate your index. They provide higher availability and throughput and can be scaled.
     * @param shards The number of shards. Shards split your data across multiple pods so you can fit more data into an index.
     * @return {@link IndexModel} representing the created serverless index.
     * @throws PineconeException if the API encounters an error during index creation or if any of the arguments are invalid.
     */
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment,
                                      String podType, Integer replicas,
                                      Integer shards) {
        return createPodsIndex(indexName, dimension, environment, podType, null, replicas, shards, null,
                null, null, "disabled", null);
    }

    /**
     * Overload for creating a new pods index with environment, podType, replicas, shards, and metadataConfig.
     * <p>
     * Example:
     * <pre>{@code 
     *     import org.openapitools.control.client.model.PodSpecMetadataConfig;
     *     ...
     *
     *     PodSpecMetadataConfig metadataConfig =
     *         new PodSpecMetadataConfig()
     *         .fields(Arrays.asList("genre", "year"));
     *     client.createPodsIndex("YOUR-INDEX", 1536, "us-east4-gcp", "p1.x2", "cosine", 2, 2, metadataConfig);
     * }</pre>
     *
     * @param indexName The name of the index to be created.
     * @param dimension The number of dimensions for the index.
     * @param environment The cloud environment where you want the index to be hosted.
     * @param podType The type of pod to use. A string with one of s1, p1, or p2 appended with a "." and one of x1, x2, x4, or x8.
     * @param replicas The number of replicas. Replicas duplicate your index. They provide higher availability and throughput and can be scaled.
     * @param shards The number of shards. Shards split your data across multiple pods so you can fit more data into an index.
     * @return {@link IndexModel} representing the created serverless index.
     * @throws PineconeException if the API encounters an error during index creation or if any of the arguments are invalid.
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
     * Creates a new pods index with the specified parameters.
     * <p>
     * Example:
     * <pre>{@code 
     *     import org.openapitools.control.client.model.PodSpecMetadataConfig;
     *     ...
     *
     *     PodSpecMetadataConfig metadataConfig =
     *         new PodSpecMetadataConfig()
     *         .fields(Arrays.asList("genre", "year"));
     *     client.createPodsIndex("YOUR-INDEX", 1536, "us-east4-gcp", "p1.x2", "cosine", 2, 2, 4, null, null, DeletionProtection.DISABLED);
     * }</pre>
     *
     * @param indexName The name of the index to be created.
     * @param dimension The number of dimensions for the index.
     * @param environment The cloud environment where you want the index to be hosted.
     * @param podType The type of pod to use. A string with one of s1, p1, or p2 appended with a "." and one of x1, x2, x4, or x8.
     * @param metric The metric type for the index. Must be one of "cosine", "euclidean", or "dotproduct".
     * @param replicas The number of replicas. Replicas duplicate your index. They provide higher availability and throughput and can be scaled.
     * @param shards The number of shards. Shards split your data across multiple pods so you can fit more data into an index.
     * @param pods The number of pods to be used in the index. This should be equal to shards x replicas.
     * @param metadataConfig The configuration for the behavior of Pinecone's internal metadata index. By default, all metadata is indexed;
     *                       when metadataConfig is present, only specified metadata fields are indexed.
     * @param sourceCollection The name of the collection to be used as the source for the index. Collections are snapshots of an index at a point in time.
     * @param deletionProtection Enable or disable deletion protection for the index.
     * @param tags A map of tags to associate with the Index.
     * @return {@link IndexModel} representing the created serverless index.
     * @throws PineconeException if the API encounters an error during index creation or if any of the arguments are invalid.
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
     * Retrieves information about an existing index.
     * <p>
     * Example:
     * <pre>{@code 
     *     import org.openapitools.control.client.model.IndexModel;
     *     ...
     *
     *     IndexModel indexModel = client.describeIndex("YOUR-INDEX");
     * }</pre>
     *
     * @param indexName The name of the index to describe.
     * @return {@link IndexModel} with the details of the index.
     * @throws PineconeException if an error occurs during the operation or the index does not exist.
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
     * Configures an existing pod-based index with new settings.
     * <p>
     * Example:
     * <pre>{@code 
     *     import org.openapitools.control.client.model.IndexModel;
     *     ...
     *
     *     // Make a configuration change
     *     IndexModel indexModel = client.configurePodsIndex("YOUR-INDEX", "p1.x2", 4, DeletionProtection.ENABLED);
     *
     *     // Call describeIndex to see the index status as the change is applied.
     *     indexModel = client.describeIndex("YOUR-INDEX");
     * }</pre>
     *
     * @param indexName The name of the index to configure.
     * @param podType The new podType for the index. Can be null if not changing the pod type.
     * @param replicas The desired number of replicas for the index, lowest value is 0. Can be null if not changing the number of replicas.
     * @param deletionProtection Enable or disable deletion protection for the index.
     * @param tags A map of tags to associate with the Index.
     * @return {@link IndexModel} representing the configured index.
     * @throws PineconeException if an error occurs during the operation, the index does not exist, or if any of the arguments are invalid.
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
     * Overload for configurePodsIndex to change the number of replicas and deletion protection for an index.
     * <p>
     * Example:
     * <pre>{@code 
     *     import org.openapitools.control.client.model.IndexModel;
     *     ...
     *
     *     IndexModel indexModel = client.configurePodsIndex("YOUR-INDEX", 4, DeletionProtection.ENABLED);
     * }</pre>
     *
     * @param indexName The name of the index.
     * @param replicas The desired number of replicas for the index, lowest value is 0.
     * @param deletionProtection Enable or disable deletion protection for the index.
     * @return {@link IndexModel} of the configured index.
     * @throws PineconeException if an error occurs during the operation, the index does not exist, or if the number of replicas is invalid.
     */
    public IndexModel configurePodsIndex(String indexName, Integer replicas, String deletionProtection) throws PineconeException {
        return configurePodsIndex(indexName, null, replicas, deletionProtection, null);
    }

    /**
     * Overload for configurePodsIndex to only change the deletion protection of an index.
     * <p>
     * Example:
     * <pre>{@code
     *     import org.openapitools.control.client.model.IndexModel;
     *     ...
     *
     *     IndexModel indexModel = client.configurePodsIndex("YOUR-INDEX", DeletionProtection.ENABLED);
     * }</pre>
     *
     * @param indexName The name of the index.
     * @param deletionProtection Enable or disable deletion protection for the index.
     * @return {@link IndexModel} of the configured index.
     * @throws PineconeException if an error occurs during the operation, the index does not exist, or if the podType is invalid.
     */
    public IndexModel configurePodsIndex(String indexName, String deletionProtection) throws PineconeException {
        return configurePodsIndex(indexName, null, null, deletionProtection, null);
    }

    /**
     * Configures an existing serverless index with deletion protection.
     * <p>
     * Example:
     * <pre>{@code
     *     import org.openapitools.control.client.model.IndexModel;
     *     ...
     *
     *     HashMap<String, String> tags = new HashMap<>();
     *     tags.put("env", "test);
     *
     *     ConfigureIndexRequestEmbed embed = new ConfigureIndexRequestEmbed();
     *     embed.model("multilingual-e5-large");
     *     HashMap<String, String> fieldMap = new HashMap<>();
     *     fieldMap.put("text", "your-text-field");
     *     embed.fieldMap(fieldMap);
     *
     *     // Make a configuration change
     *     IndexModel indexModel = client.configureServerlessIndex("YOUR-INDEX", DeletionProtection.ENABLED, tags, embed);
     *
     *     // Call describeIndex to see the index status as the change is applied.
     *     indexModel = client.describeIndex("YOUR-INDEX");
     * }</pre>
     *
     * @param indexName The name of the index to configure.
     * @param deletionProtection Enable or disable deletion protection for the index.
     * @param tags A map of tags to associate with the Index.
     * @param embed Convert an existing index to an integrated index by specifying the embedding model and field_map.
     *              The index vector type and dimension must match the model vector type and dimension, and the index
     *              similarity metric must be supported by the model
     * @return {@link IndexModel} representing the configured index.
     * @throws PineconeException if an error occurs during the operation, the index does not exist, or if any of the arguments are invalid.
     */
    public IndexModel configureServerlessIndex(String indexName,
                                               String deletionProtection,
                                               Map<String, String> tags,
                                               ConfigureIndexRequestEmbed embed) throws PineconeException {
        return configureServerlessIndex(indexName, deletionProtection, tags, embed, null, null, null, null);
    }

    /**
     * Configures an existing serverless index with deletion protection, tags, embed settings, and optional read capacity configuration.
     * <p>
     * This method allows you to configure or change the read capacity mode of an existing serverless index.
     * You can switch between OnDemand and Dedicated modes, or scale dedicated read nodes.
     * <p>
     * Example - Switch to OnDemand read capacity:
     * <pre>{@code
     *     client.configureServerlessIndex("my-index", "enabled", null, null, "OnDemand", null, null, null);
     * }</pre>
     * <p>
     * Example - Switch to Dedicated read capacity with manual scaling:
     * <pre>{@code
     *     client.configureServerlessIndex("my-index", "enabled", null, null, "Dedicated", "t1", 2, 2);
     * }</pre>
     * <p>
     * Example - Scale up dedicated read capacity:
     * <pre>{@code
     *     // Scale up by increasing shards and replicas
     *     client.configureServerlessIndex("my-index", "enabled", null, null, "Dedicated", "t1", 4, 3);
     *     
     *     // Verify the configuration was applied
     *     IndexModel desc = client.describeIndex("my-index");
     *     // Check desc.getSpec().getServerless().getReadCapacity()...
     * }</pre>
     *
     * @param indexName The name of the index to configure.
     * @param deletionProtection Enable or disable deletion protection for the index.
     * @param tags A map of tags to associate with the Index.
     * @param embed Convert an existing index to an integrated index by specifying the embedding model and field_map.
     *              The index vector type and dimension must match the model vector type and dimension, and the index
     *              similarity metric must be supported by the model
     * @param readCapacityMode The read capacity mode. Must be "OnDemand" or "Dedicated". If null, read capacity is not changed.
     * @param nodeType The node type for Dedicated mode (e.g., "t1"). Required if readCapacityMode is "Dedicated", ignored otherwise.
     * @param shards The number of shards for Dedicated mode. Required if readCapacityMode is "Dedicated", ignored otherwise.
     * @param replicas The number of replicas for Dedicated mode. Required if readCapacityMode is "Dedicated", ignored otherwise.
     * @return {@link IndexModel} representing the configured index.
     * @throws PineconeException if an error occurs during the operation, the index does not exist, or if any of the arguments are invalid.
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
     * Lists all indexes in your project, including the index name, dimension, metric, status, and spec.
     * <p>
     * Example:
     * <pre>{@code 
     *     import org.openapitools.control.client.model.IndexList;
     *     ...
     *
     *     IndexList indexes = client.listIndexes();
     * }</pre>
     *
     * @return {@link IndexList} containing all indexes.
     * @throws PineconeException if an error occurs during the operation.
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
     * Deletes an index.
     * <p>
     * Deleting an index is an irreversible operation. All data in the index will be lost.
     * When you use this command, a request is sent to the Pinecone control plane to delete
     * the index, but the termination is not synchronous because resources take a few moments to
     * be released.
     * <p>
     * You can check the status of the index by calling the describeIndex command.
     * With repeated polling of the describeIndex command, you will see the index transition to a
     * Terminating state before eventually resulting in a 404 after it has been removed.
     * <p>
     * Example:
     * <pre>{@code 
     *     import org.openapitools.control.client.model.IndexModel;
     *     ...
     *
     *     // Delete an index
     *     client.deleteIndex("YOUR-INDEX");
     *
     *     // Get index status with describeIndex
     *     IndexModel indexModel = client.describeIndex("YOUR-INDEX");
     *     indexModel.getStatus().getState();
     * }</pre>
     *
     * @param indexName The name of the index to delete.
     * @throws PineconeException if an error occurs during the deletion operation or the index does not exist.
     */
    public void deleteIndex(String indexName) throws PineconeException {
        try {
            manageIndexesApi.deleteIndex(Configuration.VERSION, indexName);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
    }

    /**
     * Create a backup of an index
     *
     * @param indexName   Name of the index to backup (required)
     * @param backupName  Name of the backup (optional)
     * @param description A description of the backup. (optional)
     * @return BackupModel
     */
    public BackupModel createBackup(String indexName, String backupName, String description) throws ApiException {
        return manageIndexesApi.createBackup(Configuration.VERSION, indexName,
                new CreateBackupRequest().name(backupName).description(description));
    }

    /**
     * Overload to list all backups for an index with default limit = 10 and pagination token = null.
     *
     * @param indexName Name of the backed up index (required)
     *                  limit which is the number of results to return per page is set to 10 by default.
     *                  paginationToken which is the token to use to retrieve the next page of results is set to null.
     * @return BackupList
     */
    public BackupList listIndexBackups(String indexName) throws ApiException {
        return manageIndexesApi.listIndexBackups(Configuration.VERSION, indexName, 10, null);
    }

    /**
     * List all backups for an index.
     *
     * @param indexName       Name of the backed up index (required)
     * @param limit           The number of results to return per page. (optional, default to 10)
     * @param paginationToken The token to use to retrieve the next page of results. (optional)
     * @return BackupList
     */
    public BackupList listIndexBackups(String indexName, Integer limit, String paginationToken) throws ApiException {
        return manageIndexesApi.listIndexBackups(Configuration.VERSION, indexName, limit, paginationToken);
    }

    /**
     * List backups for all indexes in a project
     *
     * @return BackupList
     */
    public BackupList listProjectBackups() throws ApiException {
        return listProjectBackups(null, null);
    }

    /**
     * List backups for all indexes in a project
     * @param limit The number of results to return per page. (optional)
     * @param paginationToken The token to use to retrieve the next page of results. (optional)
     *
     * @return BackupList
     */
    public BackupList listProjectBackups(Integer limit, String paginationToken) throws ApiException {
        return manageIndexesApi.listProjectBackups(Configuration.VERSION, limit, paginationToken);
    }

    /**
     * Describe a backup
     *
     * @param backupId The ID of the backup to describe. (required)
     * @return BackupModel
     */
    public BackupModel describeBackup(String backupId) throws ApiException {
        return manageIndexesApi.describeBackup(Configuration.VERSION, backupId);
    }

    /**
     * Delete a backup
     *
     * @param backupId The ID of the backup to delete. (required)
     */
    public void deleteBackup(String backupId) throws ApiException {
        manageIndexesApi.deleteBackup(Configuration.VERSION, backupId);
    }

    /**
     * Create an index from a backup
     *
     * @param backupId           The ID of the backup to create an index from. (required)
     * @param indexName          The name of the index. Resource name must be 1-45 characters long, start and end with an
     *                           alphanumeric character, and consist only of lower case alphanumeric characters. (required)
     * @param tags               Custom user tags added to an index. (optional)
     * @param deletionProtection Whether deletion protection is enabled for the index. If enabled, the index
     *                           cannot be deleted. Defaults to disabled if not provided.
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
     * Overload to create an index from a backup with name and backupId.
     *
     * @param backupId  The ID of the backup to create an index from. (required)
     * @param indexName The name of the index. Resource name must be 1-45 characters long, start and end with an
     *                  alphanumeric character, and consist only of lower case alphanumeric characters. (required)
     *                  cannot be deleted. Defaults to disabled if not provided.
     * @return CreateIndexFromBackupResponse
     */
    public CreateIndexFromBackupResponse createIndexFromBackup(String backupId, String indexName) throws ApiException {
        CreateIndexFromBackupRequest createIndexFromBackupRequest = new CreateIndexFromBackupRequest()
                .name(indexName);
        return manageIndexesApi.createIndexFromBackupOperation(Configuration.VERSION, backupId, createIndexFromBackupRequest);
    }

    /**
     * Describe a restore job
     * Get a description of a restore job.
     *
     * @param jobId The ID of the restore job to describe. (required)
     * @return RestoreJobModel
     */
    public RestoreJobModel describeRestoreJob(String jobId) throws ApiException {
        return manageIndexesApi.describeRestoreJob(Configuration.VERSION, jobId);
    }

    /**
     * Overload to list restore jobs
     * List all restore jobs for a project.
     *
     * @param limit The number of results to return per page.
     * @return RestoreJobList
     */
    public RestoreJobList listRestoreJobs(Integer limit) throws ApiException {
        return manageIndexesApi.listRestoreJobs(Configuration.VERSION, limit, null);
    }

    /**
     * Overload to list restore jobs
     * List all restore jobs for a project.
     *
     * @param paginationToken The token to use to retrieve the next page of results.
     * @return RestoreJobList
     */
    public RestoreJobList listRestoreJobs(String paginationToken) throws ApiException {
        return manageIndexesApi.listRestoreJobs(Configuration.VERSION, 10, paginationToken);
    }

    /**
     * List restore jobs
     * List all restore jobs for a project.
     *
     * @param limit           The number of results to return per page. (optional, default to 10)
     * @param paginationToken The token to use to retrieve the next page of results. (optional)
     * @return RestoreJobList
     */
    public RestoreJobList listRestoreJobs(Integer limit, String paginationToken) throws ApiException {
        return manageIndexesApi.listRestoreJobs(Configuration.VERSION, limit, paginationToken);
    }

    /**
     * Creates a new collection from a source index.
     * <p>
     * Example:
     * <pre>{@code 
     *     import org.openapitools.control.client.model.CollectionModel;
     *     ...
     *
     *     CollectionModel collection = client.createCollection("my-collection", "my-source-index");
     * }</pre>
     *
     * @param collectionName The name of the new collection.
     * @param sourceIndex The name of the source index.
     * @return {@link CollectionModel} representing the created collection.
     * @throws PineconeException if an error occurs during the operation, or the source collection is invalid.
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
     * Describes an existing collection.
     * <p>
     * Example:
     * <pre>{@code 
     *     import io.pinecone.clients.Pinecone;
     *     import org.openapitools.control.client.model.CollectionModel;
     *     ...
     *
     *     CollectionModel collection = client.describeCollection("my-collection");
     * }</pre>
     *
     * @param collectionName The name of the collection to describe.
     * @return {@link CollectionModel} with the description of the collection.
     * @throws PineconeException if an error occurs during the operation or the collection does not exist.
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
     * Lists all collections in the project.
     * <p>
     * Example:
     * <pre>{@code 
     *     import org.openapitools.control.client.model.CollectionList;
     *     ...
     *
     *     CollectionList collections = client.listCollections();
     * }</pre>
     *
     * @return {@link CollectionList} containing all collections.
     * @throws PineconeException if an error occurs during the listing operation.
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
     * Deletes a collection.
     * <p>
     * Deleting a collection is an irreversible operation. All data in the collection will be lost.
     * This method tells Pinecone you would like to delete a collection, but it takes a few moments to complete the operation.
     * Use the describeCollection() method to confirm that the collection has been deleted.
     * <p>
     * Example:
     * <pre>{@code 
     *     client.deleteCollection('my-collection');
     *
     *     // Verify collection status with describeCollection
     *     client.describeCollection('my-collection');
     * }</pre>
     *
     * @param collectionName The name of the collection to delete.
     * @throws PineconeException if an error occurs during the deletion operation or the collection does not exist.
     */
    public void deleteCollection(String collectionName) throws PineconeException {
        try {
            manageIndexesApi.deleteCollection(Configuration.VERSION, collectionName);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
    }

    /**
     * Retrieves a connection to a specific index for synchronous operations. This method initializes
     * and returns an {@link Index} object that represents a connection to an index and allowing for
     * synchronous operations against it.
     * <p>
     * Example:
     * <pre>{@code 
     *     import io.pinecone.clients.Index;
     *     ...
     *
     *     Index index = client.getIndexConnection("YOUR-INDEX");
     *
     *     // Use the index object to interact with the index
     *     index.describeIndexStats();
     * }</pre>
     *
     * @param indexName The name of the index to connect to. Must not be null or empty.
     * @return An {@link Index} object representing the connection to the specified index.
     * @throws PineconeValidationException If the indexName is null or empty.
     */
    public Index getIndexConnection(String indexName) throws PineconeValidationException {
        if(indexName == null || indexName.isEmpty()) {
            throw new PineconeValidationException("Index name cannot be null or empty");
        }

        PineconeConfig perConnectionConfig = new PineconeConfig(config.getApiKey(), config.getSourceTag());
        perConnectionConfig.setHost(getIndexHost(indexName));

        PineconeConnection connection = getConnection(indexName, perConnectionConfig);
        return new Index(perConnectionConfig, connection, indexName);
    }

    /**
     * Retrieves a connection to a specific index for asynchronous operations. This method initializes
     * and returns an {@link AsyncIndex} object that represents a connection to an index and allowing for
     * synchronous operations against it.
     * <p>
     * Example:
     * <pre>{@code 
     *     import io.pinecone.clients.AsyncIndex;
     *     ...
     *
     *     AsyncIndex asyncIndex = client.getAsyncIndexConnection("YOUR-INDEX");
     *
     *     // Use the index object to interact with the index
     *     asyncIndex.describeIndexStats();
     * }</pre>
     *
     * @param indexName The name of the index to connect to. Must not be null or empty.
     * @return An {@link AsyncIndex} object representing the connection to the specified index.
     * @throws PineconeValidationException If the indexName is null or empty.
     */
    public AsyncIndex getAsyncIndexConnection(String indexName) throws PineconeValidationException {
        if(indexName == null || indexName.isEmpty()) {
            throw new PineconeValidationException("Index name cannot be null or empty");
        }

        PineconeConfig perConnectionConfig = new PineconeConfig(config.getApiKey(), config.getSourceTag());
        perConnectionConfig.setHost(getIndexHost(indexName));

        PineconeConnection connection = getConnection(indexName, perConnectionConfig);
        return new AsyncIndex(config, connection, indexName);
    }

    /**
     * A method to create and return a new instance of the {@link Inference} client.
     * <p>
     * This method initializes the Inference client using the current {@link PineconeConfig} instance which is
     * initialized as a part of the Builder class. The {@link Inference} client can then be used to interact with
     * Pinecone's inference API.
     * @return A new {@link Inference} client instance.
     */
    public Inference getInferenceClient() {
        return new Inference(config);
    }

    PineconeConnection getConnection(String indexName, PineconeConfig perConnectionConfig) {
        return connectionsMap.computeIfAbsent(indexName, key -> new PineconeConnection(perConnectionConfig));
    }

    ConcurrentHashMap<String, PineconeConnection> getConnectionsMap() {
        return connectionsMap;
    }

    String getIndexHost(String indexName) {
        return this.describeIndex(indexName).getHost();
    }

    static void closeConnection(String indexName) {
        connectionsMap.remove(indexName);
    }

    private void handleApiException(ApiException apiException) throws PineconeException {
        int statusCode = apiException.getCode();
        String responseBody = apiException.getResponseBody();
        FailedRequestInfo failedRequestInfo = new FailedRequestInfo(statusCode, responseBody);
        HttpErrorMapper.mapHttpStatusError(failedRequestInfo, apiException);
    }

    /**
     * A builder class for creating a {@link Pinecone} instance. This builder allows for configuring a {@link Pinecone}
     * instance with custom parameters including an API key, a source tag, and a custom OkHttpClient.
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

        /**
         * Constructs a new {@link Builder} with the mandatory API key.
         *
         * <pre>{@code
         *     import io.pinecone.clients.Pinecone;
         *
         *     Pinecone client = new Pinecone.Builder("PINECONE_API_KEY").build();
         * }</pre>
         *
         * @param apiKey The API key required for authenticating requests to Pinecone services.
         */
        public Builder(String apiKey) {
            this.apiKey = apiKey;
        }

        /**
         * Sets the source tag to include with all requests made by the Pinecone client.
         * <p>
         * Source tag is an optional string identifier used to help Pinecone attribute API activity to our partners.
         * For more info, see
         * <a href="https://docs.pinecone.io/integrations/build-integration/attribute-api-activity">
         *    https://docs.pinecone.io/integrations/build-integration/attribute-api-activity
         * </a>
         *
         * <pre>{@code
         *     Pinecone client = new Pinecone.Builder("PINECONE_API_KEY")
         *         .withSourceTag("YOUR_SOURCE_TAG")
         *         .build();
         *
         *     // The tag will be included in all requests made by the client, e.g.
         *     client.listIndexes();
         * }</pre>
         *
         * @param sourceTag The source tag to identify the origin of the requests.
         * @return This {@link Builder} instance for chaining method calls.
         */
        public Builder withSourceTag(String sourceTag) {
            this.sourceTag = sourceTag;
            return this;
        }

        /**
         * Sets a custom OkHttpClient for the Pinecone client to use for making HTTP requests.
         *
         * <pre>{@code
         *     import okhttp3.OkHttpClient;
         *
         *     OkHttpClient myClient = new OkHttpClient();
         *     Pinecone client = new Pinecone.Builder("PINECONE_API_KEY")
         *         .withOkHttpClient(myClient)
         *         .build();
         *
         *     // Network requests will now be made using your custom OkHttpClient
         *     client.listIndexes();
         * }</pre>
         *
         * @param okHttpClient The custom OkHttpClient to be used. Must not be null.
         * @return This {@link Builder} instance for chaining method calls.
         */
        public Builder withOkHttpClient(OkHttpClient okHttpClient) {
            this.customOkHttpClient = okHttpClient;
            return this;
        }

        /**
         * Sets a proxy for the Pinecone client to use for control and data plane requests.
         * <p>
         * When a proxy is configured using this method, all control and data plane requests made by the Pinecone client
         * will be routed through the specified proxy server.
         * <p>
         * It's important to note that both proxyHost and proxyPort parameters should be provided to establish
         * the connection to the proxy server.
         * <p>
         * Example usage:
         * <pre>{@code
         *
         * String proxyHost = System.getenv("PROXY_HOST");
         * int proxyPort = Integer.parseInt(System.getenv("PROXY_PORT"));
         * Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY")
         *     .withProxy(proxyHost, proxyPort)
         *     .build();
         *
         * // Network requests for control plane operations will now be made using the specified proxy.
         * pinecone.listIndexes();
         *
         * // Network requests for data plane operations will now be made using the specified proxy.
         * Index index = pinecone.getIndexConnection("PINECONE_INDEX");
         * index.describeIndexStats();
         * }</pre>
         *
         * @param proxyHost The hostname or IP address of the proxy server. Must not be null.
         * @param proxyPort The port number of the proxy server. Must not be null.
         * @return This {@link Builder} instance for chaining method calls.
         */
        public Builder withProxy(String proxyHost, int proxyPort) {
            this.proxyConfig = new ProxyConfig(proxyHost, proxyPort);
            return this;
        }

        /**
         * Sets a custom host URL for the control and data plane operations.
         * <p>
         * This method allows you to specify a custom base URL for Pinecone control and data plane requests.
         * <p>
         * Example usage:
         * <pre>{@code
         * Pinecone client = new Pinecone.Builder("PINECONE_API_KEY")
         *     .withHost("http://localhost:5080")
         *     .build();
         *
         * // Requests will now be sent to the specified host.
         * client.listIndexes();
         * }</pre>
         *
         * @param host The custom host URL for the Pinecone service. Must be a valid URL.
         * @return This {@link Builder} instance for chaining method calls.
         */
        public Builder withHost(String host) {
            this.host = host;
            return this;
        }

        /**
         * Configures whether TLS (Transport Layer Security) should be enabled for data plane operations.
         * <p>
         * By default, TLS is enabled for data plane requests to ensure secure communication for data plane operations.
         * This method can be used to disable TLS if needed (e.g., for testing or when communicating with non-secure
         * endpoints). Disabling TLS in a production environment is not recommended due to potential security risks.
         * <p>
         * Example usage:
         * <pre>{@code
         * Pinecone client = new Pinecone.Builder("PINECONE_API_KEY")
         *     .withTlsEnabled(false)
         *     .build();
         *
         * // Get index for data plane operations
         * Index index = pinecone.getIndexConnection("PINECONE_INDEX_NAME");
         *
         * // Requests will now be made without TLS encryption (not recommended for production use).
         * index.upsert("v1", Arrays.asList(1f, 2f, 3f));
         * }</pre>
         *
         * @param enableTls {@code true} to enable TLS (default), {@code false} to disable it.
         * @return This {@link Builder} instance for chaining method calls.
         */
        public Builder withTlsEnabled(boolean enableTls) {
            this.enableTls = enableTls;
            return this;
        }

        /**
         * Builds and returns a {@link Pinecone} instance configured with the provided API key, optional source tag,
         * and OkHttpClient.
         * <p>
         * This method also performs configuration validation, sets up the internal API client, and manages indexes API
         * with debugging options based on the environment variables.
         *
         * @return A new {@link Pinecone} instance configured based on the builder parameters.
         */
        public Pinecone build() {
            PineconeConfig config = new PineconeConfig(apiKey, sourceTag, proxyConfig, customOkHttpClient);
            config.setTLSEnabled(enableTls);
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
                apiClient.setDebugging(true);
            }

            ManageIndexesApi manageIndexesApi = new ManageIndexesApi();
            manageIndexesApi.setApiClient(apiClient);

            return new Pinecone(config, manageIndexesApi);
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