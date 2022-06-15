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
import io.pinecone.rest.model.Vector;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The response for the &#x60;Fetch&#x60; operation.
 */
@ApiModel(description = "The response for the `Fetch` operation.")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2022-06-15T23:32:38.960526Z[Etc/UTC]")
public class FetchResponse {
  public static final String SERIALIZED_NAME_VECTORS = "vectors";
  @SerializedName(SERIALIZED_NAME_VECTORS)
  private Map<String, Vector> vectors = null;

  public static final String SERIALIZED_NAME_NAMESPACE = "namespace";
  @SerializedName(SERIALIZED_NAME_NAMESPACE)
  private String namespace;


  public FetchResponse vectors(Map<String, Vector> vectors) {
    
    this.vectors = vectors;
    return this;
  }

  public FetchResponse putVectorsItem(String key, Vector vectorsItem) {
    if (this.vectors == null) {
      this.vectors = new HashMap<String, Vector>();
    }
    this.vectors.put(key, vectorsItem);
    return this;
  }

   /**
   * Get vectors
   * @return vectors
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public Map<String, Vector> getVectors() {
    return vectors;
  }


  public void setVectors(Map<String, Vector> vectors) {
    this.vectors = vectors;
  }


  public FetchResponse namespace(String namespace) {
    
    this.namespace = namespace;
    return this;
  }

   /**
   * The namespace of the vectors.
   * @return namespace
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(example = "example-namespace", value = "The namespace of the vectors.")

  public String getNamespace() {
    return namespace;
  }


  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FetchResponse fetchResponse = (FetchResponse) o;
    return Objects.equals(this.vectors, fetchResponse.vectors) &&
        Objects.equals(this.namespace, fetchResponse.namespace);
  }

  @Override
  public int hashCode() {
    return Objects.hash(vectors, namespace);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FetchResponse {\n");
    sb.append("    vectors: ").append(toIndentedString(vectors)).append("\n");
    sb.append("    namespace: ").append(toIndentedString(namespace)).append("\n");
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
