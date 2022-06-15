/*
 * Pinecone API
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: version not set
 * Contact: support@pinecone.io
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package io.pinecone.rest.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.pinecone.rest.model.OneOfApproximatedConfig;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;

/**
 * CreateRequest
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2022-06-15T23:32:38.960526Z[Etc/UTC]")
public class CreateRequest {
  public static final String SERIALIZED_NAME_NAME = "name";
  @SerializedName(SERIALIZED_NAME_NAME)
  private String name;

  public static final String SERIALIZED_NAME_DIMENSION = "dimension";
  @SerializedName(SERIALIZED_NAME_DIMENSION)
  private Integer dimension;

  public static final String SERIALIZED_NAME_INDEX_TYPE = "index_type";
  @SerializedName(SERIALIZED_NAME_INDEX_TYPE)
  private String indexType = "approximated";

  public static final String SERIALIZED_NAME_METRIC = "metric";
  @SerializedName(SERIALIZED_NAME_METRIC)
  private String metric = "cosine";

  public static final String SERIALIZED_NAME_PODS = "pods";
  @SerializedName(SERIALIZED_NAME_PODS)
  private Integer pods = 1;

  public static final String SERIALIZED_NAME_REPLICAS = "replicas";
  @SerializedName(SERIALIZED_NAME_REPLICAS)
  private Integer replicas = 1;

  public static final String SERIALIZED_NAME_SHARDS = "shards";
  @SerializedName(SERIALIZED_NAME_SHARDS)
  private Integer shards = 1;

  public static final String SERIALIZED_NAME_POD_TYPE = "pod_type";
  @SerializedName(SERIALIZED_NAME_POD_TYPE)
  private String podType = "p1";

  public static final String SERIALIZED_NAME_INDEX_CONFIG = "index_config";
  @SerializedName(SERIALIZED_NAME_INDEX_CONFIG)
  private OneOfApproximatedConfig indexConfig;

  public static final String SERIALIZED_NAME_METADATA_CONFIG = "metadata_config";
  @SerializedName(SERIALIZED_NAME_METADATA_CONFIG)
  private Object metadataConfig;


  public CreateRequest name(String name) {
    
    this.name = name;
    return this;
  }

   /**
   * The name of the index to be updated
   * @return name
  **/
  @ApiModelProperty(example = "example-index", required = true, value = "The name of the index to be updated")

  public String getName() {
    return name;
  }


  public void setName(String name) {
    this.name = name;
  }


  public CreateRequest dimension(Integer dimension) {
    
    this.dimension = dimension;
    return this;
  }

   /**
   * The dimensions of the vectors to be inserted in the index
   * @return dimension
  **/
  @ApiModelProperty(example = "1024", required = true, value = "The dimensions of the vectors to be inserted in the index")

  public Integer getDimension() {
    return dimension;
  }


  public void setDimension(Integer dimension) {
    this.dimension = dimension;
  }


  public CreateRequest indexType(String indexType) {
    
    this.indexType = indexType;
    return this;
  }

   /**
   * The type of vector index. Pinecone supports &#39;approximated&#39;.
   * @return indexType
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "The type of vector index. Pinecone supports 'approximated'.")

  public String getIndexType() {
    return indexType;
  }


  public void setIndexType(String indexType) {
    this.indexType = indexType;
  }


  public CreateRequest metric(String metric) {
    
    this.metric = metric;
    return this;
  }

   /**
   * The distance metric to be used for similarity search. You can use &#39;euclidean&#39;, &#39;cosine&#39;, or &#39;dotproduct&#39;.
   * @return metric
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "The distance metric to be used for similarity search. You can use 'euclidean', 'cosine', or 'dotproduct'.")

  public String getMetric() {
    return metric;
  }


  public void setMetric(String metric) {
    this.metric = metric;
  }


  public CreateRequest pods(Integer pods) {
    
    this.pods = pods;
    return this;
  }

   /**
   * The number of pods for the index to use,including replicas.
   * @return pods
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "The number of pods for the index to use,including replicas.")

  public Integer getPods() {
    return pods;
  }


  public void setPods(Integer pods) {
    this.pods = pods;
  }


  public CreateRequest replicas(Integer replicas) {
    
    this.replicas = replicas;
    return this;
  }

   /**
   * The number of replicas. Replicas duplicate your index. They provide higher availability and throughput.
   * @return replicas
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "The number of replicas. Replicas duplicate your index. They provide higher availability and throughput.")

  public Integer getReplicas() {
    return replicas;
  }


  public void setReplicas(Integer replicas) {
    this.replicas = replicas;
  }


  public CreateRequest shards(Integer shards) {
    
    this.shards = shards;
    return this;
  }

   /**
   * The number of shards to be used in the index.
   * @return shards
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "The number of shards to be used in the index.")

  public Integer getShards() {
    return shards;
  }


  public void setShards(Integer shards) {
    this.shards = shards;
  }


  public CreateRequest podType(String podType) {
    
    this.podType = podType;
    return this;
  }

   /**
   * The type of pod to use. One of &#39;s1&#39; or &#39;p1&#39;.
   * @return podType
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "The type of pod to use. One of 's1' or 'p1'.")

  public String getPodType() {
    return podType;
  }


  public void setPodType(String podType) {
    this.podType = podType;
  }


  public CreateRequest indexConfig(OneOfApproximatedConfig indexConfig) {
    
    this.indexConfig = indexConfig;
    return this;
  }

   /**
   * Get indexConfig
   * @return indexConfig
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public OneOfApproximatedConfig getIndexConfig() {
    return indexConfig;
  }


  public void setIndexConfig(OneOfApproximatedConfig indexConfig) {
    this.indexConfig = indexConfig;
  }


  public CreateRequest metadataConfig(Object metadataConfig) {
    
    this.metadataConfig = metadataConfig;
    return this;
  }

   /**
   * Configuration for the behavior of Pinecone&#39;s internal metadata index
   * @return metadataConfig
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "Configuration for the behavior of Pinecone's internal metadata index")

  public Object getMetadataConfig() {
    return metadataConfig;
  }


  public void setMetadataConfig(Object metadataConfig) {
    this.metadataConfig = metadataConfig;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateRequest createRequest = (CreateRequest) o;
    return Objects.equals(this.name, createRequest.name) &&
        Objects.equals(this.dimension, createRequest.dimension) &&
        Objects.equals(this.indexType, createRequest.indexType) &&
        Objects.equals(this.metric, createRequest.metric) &&
        Objects.equals(this.pods, createRequest.pods) &&
        Objects.equals(this.replicas, createRequest.replicas) &&
        Objects.equals(this.shards, createRequest.shards) &&
        Objects.equals(this.podType, createRequest.podType) &&
        Objects.equals(this.indexConfig, createRequest.indexConfig) &&
        Objects.equals(this.metadataConfig, createRequest.metadataConfig);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, dimension, indexType, metric, pods, replicas, shards, podType, indexConfig, metadataConfig);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateRequest {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    dimension: ").append(toIndentedString(dimension)).append("\n");
    sb.append("    indexType: ").append(toIndentedString(indexType)).append("\n");
    sb.append("    metric: ").append(toIndentedString(metric)).append("\n");
    sb.append("    pods: ").append(toIndentedString(pods)).append("\n");
    sb.append("    replicas: ").append(toIndentedString(replicas)).append("\n");
    sb.append("    shards: ").append(toIndentedString(shards)).append("\n");
    sb.append("    podType: ").append(toIndentedString(podType)).append("\n");
    sb.append("    indexConfig: ").append(toIndentedString(indexConfig)).append("\n");
    sb.append("    metadataConfig: ").append(toIndentedString(metadataConfig)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}
