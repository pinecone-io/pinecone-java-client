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
 * The request for the &#x60;Upsert&#x60; operation.
 */
@ApiModel(description = "The request for the `Upsert` operation.")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2022-06-15T23:32:38.960526Z[Etc/UTC]")
public class UpdateRequest {
  public static final String SERIALIZED_NAME_ID = "id";
  @SerializedName(SERIALIZED_NAME_ID)
  private String id;

  public static final String SERIALIZED_NAME_VALUES = "values";
  @SerializedName(SERIALIZED_NAME_VALUES)
  private List<Float> values = null;

  public static final String SERIALIZED_NAME_SET_METADATA = "setMetadata";
  @SerializedName(SERIALIZED_NAME_SET_METADATA)
  private Object setMetadata;

  public static final String SERIALIZED_NAME_NAMESPACE = "namespace";
  @SerializedName(SERIALIZED_NAME_NAMESPACE)
  private String namespace;


  public UpdateRequest id(String id) {
    
    this.id = id;
    return this;
  }

   /**
   * Vector&#39;s unique id.
   * @return id
  **/
  @ApiModelProperty(example = "example-vector-1", required = true, value = "Vector's unique id.")

  public String getId() {
    return id;
  }


  public void setId(String id) {
    this.id = id;
  }


  public UpdateRequest values(List<Float> values) {
    
    this.values = values;
    return this;
  }

  public UpdateRequest addValuesItem(Float valuesItem) {
    if (this.values == null) {
      this.values = new ArrayList<Float>();
    }
    this.values.add(valuesItem);
    return this;
  }

   /**
   * Vector data.
   * @return values
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(example = "[0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8]", value = "Vector data.")

  public List<Float> getValues() {
    return values;
  }


  public void setValues(List<Float> values) {
    this.values = values;
  }


  public UpdateRequest setMetadata(Object setMetadata) {
    
    this.setMetadata = setMetadata;
    return this;
  }

   /**
   * Metadata to *set* for the vector.
   * @return setMetadata
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(example = "{\"genre\":\"documentary\",\"year\":2019}", value = "Metadata to *set* for the vector.")

  public Object getSetMetadata() {
    return setMetadata;
  }


  public void setSetMetadata(Object setMetadata) {
    this.setMetadata = setMetadata;
  }


  public UpdateRequest namespace(String namespace) {
    
    this.namespace = namespace;
    return this;
  }

   /**
   * Namespace name where to update the vector.
   * @return namespace
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(example = "example-namespace", value = "Namespace name where to update the vector.")

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
    UpdateRequest updateRequest = (UpdateRequest) o;
    return Objects.equals(this.id, updateRequest.id) &&
        Objects.equals(this.values, updateRequest.values) &&
        Objects.equals(this.setMetadata, updateRequest.setMetadata) &&
        Objects.equals(this.namespace, updateRequest.namespace);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, values, setMetadata, namespace);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UpdateRequest {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    values: ").append(toIndentedString(values)).append("\n");
    sb.append("    setMetadata: ").append(toIndentedString(setMetadata)).append("\n");
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
