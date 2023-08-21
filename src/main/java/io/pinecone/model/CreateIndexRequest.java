package io.pinecone.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pinecone.PineconeValidationException;

public class CreateIndexRequest {
    private String indexName;

    private Integer dimension;

    private String metric = "cosine";

    private Integer pods = 1;

    private Integer replicas = 1;

    private String podType = "p1.x1";

    private IndexMetadataConfig metadataConfig;

    private String sourceCollection;

    public CreateIndexRequest() {
    }

    public CreateIndexRequest indexName(String name) {
        this.indexName = name;
        return this;
    }

    /**
     * The name of the index to be created. The maximum length is 45 characters.
     * @return name
     **/
    @javax.annotation.Nullable
    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public CreateIndexRequest dimension(Integer dimension) {
        this.dimension = dimension;
        return this;
    }

    /**
     * The dimensions of the vectors to be inserted in the index
     * @return dimension
     **/
    @javax.annotation.Nonnull
    public Integer getDimension() {
        return dimension;
    }

    public void setDimension(Integer dimension) {
        this.dimension = dimension;
    }

    public CreateIndexRequest metric(String metric) {
        this.metric = metric;
        return this;
    }

    /**
     * The distance metric to be used for similarity search. You can use &#39;euclidean&#39;, &#39;cosine&#39;, or &#39;dotproduct&#39;.
     * @return metric
     **/
    @javax.annotation.Nullable
    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public CreateIndexRequest pods(Integer pods) {
        this.pods = pods;
        return this;
    }

    /**
     * The number of pods for the index to use,including replicas.
     * @return pods
     **/
    @javax.annotation.Nullable
    public Integer getPods() {
        return pods;
    }

    public void setPods(Integer pods) {
        this.pods = pods;
    }

    public CreateIndexRequest replicas(Integer replicas) {
        this.replicas = replicas;
        return this;
    }

    /**
     * The number of replicas. Replicas duplicate your index. They provide higher availability and throughput.
     * @return replicas
     **/
    @javax.annotation.Nullable
    public Integer getReplicas() {
        return replicas;
    }

    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }

    public CreateIndexRequest podType(String podType) {
        this.podType = podType;
        return this;
    }

    /**
     * The type of pod to use. One of s1, p1, or p2 appended with . and one of x1, x2, x4, or x8.
     * @return podType
     **/
    @javax.annotation.Nullable
    public String getPodType() {
        return podType;
    }

    public void setPodType(String podType) {
        this.podType = podType;
    }

    public CreateIndexRequest metadataConfig(IndexMetadataConfig metadataConfig) {
        this.metadataConfig = metadataConfig;
        return this;
    }

    /**
     * Get metadataConfig
     * @return metadataConfig
     **/
    @javax.annotation.Nullable
    public IndexMetadataConfig getMetadataConfig() {
        return metadataConfig;
    }

    public void setMetadataConfig(IndexMetadataConfig metadataConfig) {
        this.metadataConfig = metadataConfig;
    }

    public CreateIndexRequest sourceCollection(String sourceCollection) {
        this.sourceCollection = sourceCollection;
        return this;
    }

    /**
     * The name of the collection to create an index from
     * @return sourceCollection
     **/
    public String getSourceCollection() {
        return sourceCollection;
    }

    public void setSourceCollection(String sourceCollection) {
        this.sourceCollection = sourceCollection;
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode jsonNode = mapper.createObjectNode()
                .put("metric", metric)
                .put("pods", pods)
                .put("replicas", replicas)
                .put("pod_type", podType)
                .put("name", indexName)
                .put("dimension", dimension)
                .put("source_collection", sourceCollection)
                .set("metadata_config", mapper.valueToTree(metadataConfig));

        validateJsonObject(jsonNode);
        return jsonNode.toString();
    }

    public static void validateJsonObject(JsonNode jsonNode) {
        if(jsonNode.get("name").isNull()) {
            throw new PineconeValidationException("Index name cannot be empty");
        }
        System.out.println("jsonString: " + jsonNode.get("dimension"));
        if(jsonNode.get("dimension").isNull()) {
            throw new PineconeValidationException("Dimension cannot be empty");
        }
    }
}
