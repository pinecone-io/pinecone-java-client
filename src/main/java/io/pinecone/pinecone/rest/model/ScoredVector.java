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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ScoredVector
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2022-06-15T23:32:38.960526Z[Etc/UTC]")
public class ScoredVector {
  public static final String SERIALIZED_NAME_ID = "id";
  @SerializedName(SERIALIZED_NAME_ID)
  private String id;

  public static final String SERIALIZED_NAME_SCORE = "score";
  @SerializedName(SERIALIZED_NAME_SCORE)
  private Float score;

  public static final String SERIALIZED_NAME_VALUES = "values";
  @SerializedName(SERIALIZED_NAME_VALUES)
  private List<Float> values = null;

  public static final String SERIALIZED_NAME_METADATA = "metadata";
  @SerializedName(SERIALIZED_NAME_METADATA)
  private Object metadata;


  public ScoredVector id(String id) {
    
    this.id = id;
    return this;
  }

   /**
   * This is the vector&#39;s unique id.
   * @return id
  **/
  @ApiModelProperty(example = "example-vector-1", required = true, value = "This is the vector's unique id.")

  public String getId() {
    return id;
  }


  public void setId(String id) {
    this.id = id;
  }


  public ScoredVector score(Float score) {
    
    this.score = score;
    return this;
  }

   /**
   * This is a measure of similarity between this vector and the query vector.  The higher the score, the more they are similar.
   * @return score
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(example = "0.08", value = "This is a measure of similarity between this vector and the query vector.  The higher the score, the more they are similar.")

  public Float getScore() {
    return score;
  }


  public void setScore(Float score) {
    this.score = score;
  }


  public ScoredVector values(List<Float> values) {
    
    this.values = values;
    return this;
  }

  public ScoredVector addValuesItem(Float valuesItem) {
    if (this.values == null) {
      this.values = new ArrayList<Float>();
    }
    this.values.add(valuesItem);
    return this;
  }

   /**
   * This is the vector data, if it is requested.
   * @return values
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(example = "[0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8]", value = "This is the vector data, if it is requested.")

  public List<Float> getValues() {
    return values;
  }


  public void setValues(List<Float> values) {
    this.values = values;
  }


  public ScoredVector metadata(Object metadata) {
    
    this.metadata = metadata;
    return this;
  }

   /**
   * This is the metadata, if it is requested.
   * @return metadata
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(example = "{\"genre\":\"documentary\",\"year\":2019}", value = "This is the metadata, if it is requested.")

  public Object getMetadata() {
    return metadata;
  }


  public void setMetadata(Object metadata) {
    this.metadata = metadata;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ScoredVector scoredVector = (ScoredVector) o;
    return Objects.equals(this.id, scoredVector.id) &&
        Objects.equals(this.score, scoredVector.score) &&
        Objects.equals(this.values, scoredVector.values) &&
        Objects.equals(this.metadata, scoredVector.metadata);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, score, values, metadata);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScoredVector {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    score: ").append(toIndentedString(score)).append("\n");
    sb.append("    values: ").append(toIndentedString(values)).append("\n");
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
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

