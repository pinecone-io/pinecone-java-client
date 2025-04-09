/*
 * Pinecone Data Plane API
 * Pinecone is a vector database that makes it easy to search and retrieve billions of high-dimensional vectors.
 *
 * The version of the OpenAPI document: 2025-04
 * Contact: support@pinecone.io
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package org.openapitools.db_data.client.model;

import java.util.Objects;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.openapitools.db_data.client.model.NamespaceSummary;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openapitools.db_data.client.JSON;

/**
 * The response for the &#x60;describe_index_stats&#x60; operation.
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2025-04-09T20:04:14.849797Z[Etc/UTC]")
public class IndexDescription {
  public static final String SERIALIZED_NAME_NAMESPACES = "namespaces";
  @SerializedName(SERIALIZED_NAME_NAMESPACES)
  private Map<String, NamespaceSummary> namespaces = new HashMap<>();

  public static final String SERIALIZED_NAME_DIMENSION = "dimension";
  @SerializedName(SERIALIZED_NAME_DIMENSION)
  private Long dimension;

  public static final String SERIALIZED_NAME_INDEX_FULLNESS = "indexFullness";
  @SerializedName(SERIALIZED_NAME_INDEX_FULLNESS)
  private Float indexFullness;

  public static final String SERIALIZED_NAME_TOTAL_VECTOR_COUNT = "totalVectorCount";
  @SerializedName(SERIALIZED_NAME_TOTAL_VECTOR_COUNT)
  private Long totalVectorCount;

  public static final String SERIALIZED_NAME_METRIC = "metric";
  @SerializedName(SERIALIZED_NAME_METRIC)
  private String metric;

  public static final String SERIALIZED_NAME_VECTOR_TYPE = "vectorType";
  @SerializedName(SERIALIZED_NAME_VECTOR_TYPE)
  private String vectorType;

  public IndexDescription() {
  }

  public IndexDescription namespaces(Map<String, NamespaceSummary> namespaces) {
    
    this.namespaces = namespaces;
    return this;
  }

  public IndexDescription putNamespacesItem(String key, NamespaceSummary namespacesItem) {
    if (this.namespaces == null) {
      this.namespaces = new HashMap<>();
    }
    this.namespaces.put(key, namespacesItem);
    return this;
  }

   /**
   * A mapping for each namespace in the index from the namespace name to a summary of its contents. If a metadata filter expression is present, the summary will reflect only vectors matching that expression.
   * @return namespaces
  **/
  @javax.annotation.Nullable
  public Map<String, NamespaceSummary> getNamespaces() {
    return namespaces;
  }


  public void setNamespaces(Map<String, NamespaceSummary> namespaces) {
    this.namespaces = namespaces;
  }


  public IndexDescription dimension(Long dimension) {
    
    this.dimension = dimension;
    return this;
  }

   /**
   * The dimension of the indexed vectors. Not specified if &#x60;sparse&#x60; index.
   * @return dimension
  **/
  @javax.annotation.Nullable
  public Long getDimension() {
    return dimension;
  }


  public void setDimension(Long dimension) {
    this.dimension = dimension;
  }


  public IndexDescription indexFullness(Float indexFullness) {
    
    this.indexFullness = indexFullness;
    return this;
  }

   /**
   * The fullness of the index, regardless of whether a metadata filter expression was passed. The granularity of this metric is 10%.  Serverless indexes scale automatically as needed, so index fullness  is relevant only for pod-based indexes.  The index fullness result may be inaccurate during pod resizing; to get the status of a pod resizing process, use [&#x60;describe_index&#x60;](https://docs.pinecone.io/reference/api/2024-10/control-plane/describe_index).
   * @return indexFullness
  **/
  @javax.annotation.Nullable
  public Float getIndexFullness() {
    return indexFullness;
  }


  public void setIndexFullness(Float indexFullness) {
    this.indexFullness = indexFullness;
  }


  public IndexDescription totalVectorCount(Long totalVectorCount) {
    
    this.totalVectorCount = totalVectorCount;
    return this;
  }

   /**
   * The total number of vectors in the index, regardless of whether a metadata filter expression was passed
   * @return totalVectorCount
  **/
  @javax.annotation.Nullable
  public Long getTotalVectorCount() {
    return totalVectorCount;
  }


  public void setTotalVectorCount(Long totalVectorCount) {
    this.totalVectorCount = totalVectorCount;
  }


  public IndexDescription metric(String metric) {
    
    this.metric = metric;
    return this;
  }

   /**
   * The metric used to measure similarity.
   * @return metric
  **/
  @javax.annotation.Nullable
  public String getMetric() {
    return metric;
  }


  public void setMetric(String metric) {
    this.metric = metric;
  }


  public IndexDescription vectorType(String vectorType) {
    
    this.vectorType = vectorType;
    return this;
  }

   /**
   * The type of vectors stored in the index.
   * @return vectorType
  **/
  @javax.annotation.Nullable
  public String getVectorType() {
    return vectorType;
  }


  public void setVectorType(String vectorType) {
    this.vectorType = vectorType;
  }

  /**
   * A container for additional, undeclared properties.
   * This is a holder for any undeclared properties as specified with
   * the 'additionalProperties' keyword in the OAS document.
   */
  private Map<String, Object> additionalProperties;

  /**
   * Set the additional (undeclared) property with the specified name and value.
   * If the property does not already exist, create it otherwise replace it.
   *
   * @param key name of the property
   * @param value value of the property
   * @return the IndexDescription instance itself
   */
  public IndexDescription putAdditionalProperty(String key, Object value) {
    if (this.additionalProperties == null) {
        this.additionalProperties = new HashMap<String, Object>();
    }
    this.additionalProperties.put(key, value);
    return this;
  }

  /**
   * Return the additional (undeclared) property.
   *
   * @return a map of objects
   */
  public Map<String, Object> getAdditionalProperties() {
    return additionalProperties;
  }

  /**
   * Return the additional (undeclared) property with the specified name.
   *
   * @param key name of the property
   * @return an object
   */
  public Object getAdditionalProperty(String key) {
    if (this.additionalProperties == null) {
        return null;
    }
    return this.additionalProperties.get(key);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IndexDescription indexDescription = (IndexDescription) o;
    return Objects.equals(this.namespaces, indexDescription.namespaces) &&
        Objects.equals(this.dimension, indexDescription.dimension) &&
        Objects.equals(this.indexFullness, indexDescription.indexFullness) &&
        Objects.equals(this.totalVectorCount, indexDescription.totalVectorCount) &&
        Objects.equals(this.metric, indexDescription.metric) &&
        Objects.equals(this.vectorType, indexDescription.vectorType)&&
        Objects.equals(this.additionalProperties, indexDescription.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(namespaces, dimension, indexFullness, totalVectorCount, metric, vectorType, additionalProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IndexDescription {\n");
    sb.append("    namespaces: ").append(toIndentedString(namespaces)).append("\n");
    sb.append("    dimension: ").append(toIndentedString(dimension)).append("\n");
    sb.append("    indexFullness: ").append(toIndentedString(indexFullness)).append("\n");
    sb.append("    totalVectorCount: ").append(toIndentedString(totalVectorCount)).append("\n");
    sb.append("    metric: ").append(toIndentedString(metric)).append("\n");
    sb.append("    vectorType: ").append(toIndentedString(vectorType)).append("\n");
    sb.append("    additionalProperties: ").append(toIndentedString(additionalProperties)).append("\n");
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


  public static HashSet<String> openapiFields;
  public static HashSet<String> openapiRequiredFields;

  static {
    // a set of all properties/fields (JSON key names)
    openapiFields = new HashSet<String>();
    openapiFields.add("namespaces");
    openapiFields.add("dimension");
    openapiFields.add("indexFullness");
    openapiFields.add("totalVectorCount");
    openapiFields.add("metric");
    openapiFields.add("vectorType");

    // a set of required properties/fields (JSON key names)
    openapiRequiredFields = new HashSet<String>();
  }

 /**
  * Validates the JSON Element and throws an exception if issues found
  *
  * @param jsonElement JSON Element
  * @throws IOException if the JSON Element is invalid with respect to IndexDescription
  */
  public static void validateJsonElement(JsonElement jsonElement) throws IOException {
      if (jsonElement == null) {
        if (!IndexDescription.openapiRequiredFields.isEmpty()) { // has required fields but JSON element is null
          throw new IllegalArgumentException(String.format("The required field(s) %s in IndexDescription is not found in the empty JSON string", IndexDescription.openapiRequiredFields.toString()));
        }
      }
        JsonObject jsonObj = jsonElement.getAsJsonObject();
      if ((jsonObj.get("metric") != null && !jsonObj.get("metric").isJsonNull()) && !jsonObj.get("metric").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `metric` to be a primitive type in the JSON string but got `%s`", jsonObj.get("metric").toString()));
      }
      if ((jsonObj.get("vectorType") != null && !jsonObj.get("vectorType").isJsonNull()) && !jsonObj.get("vectorType").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `vectorType` to be a primitive type in the JSON string but got `%s`", jsonObj.get("vectorType").toString()));
      }
  }

  public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
       if (!IndexDescription.class.isAssignableFrom(type.getRawType())) {
         return null; // this class only serializes 'IndexDescription' and its subtypes
       }
       final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
       final TypeAdapter<IndexDescription> thisAdapter
                        = gson.getDelegateAdapter(this, TypeToken.get(IndexDescription.class));

       return (TypeAdapter<T>) new TypeAdapter<IndexDescription>() {
           @Override
           public void write(JsonWriter out, IndexDescription value) throws IOException {
             JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
             obj.remove("additionalProperties");
             // serialize additional properties
             if (value.getAdditionalProperties() != null) {
               for (Map.Entry<String, Object> entry : value.getAdditionalProperties().entrySet()) {
                 if (entry.getValue() instanceof String)
                   obj.addProperty(entry.getKey(), (String) entry.getValue());
                 else if (entry.getValue() instanceof Number)
                   obj.addProperty(entry.getKey(), (Number) entry.getValue());
                 else if (entry.getValue() instanceof Boolean)
                   obj.addProperty(entry.getKey(), (Boolean) entry.getValue());
                 else if (entry.getValue() instanceof Character)
                   obj.addProperty(entry.getKey(), (Character) entry.getValue());
                 else {
                   obj.add(entry.getKey(), gson.toJsonTree(entry.getValue()).getAsJsonObject());
                 }
               }
             }
             elementAdapter.write(out, obj);
           }

           @Override
           public IndexDescription read(JsonReader in) throws IOException {
             JsonElement jsonElement = elementAdapter.read(in);
             validateJsonElement(jsonElement);
             JsonObject jsonObj = jsonElement.getAsJsonObject();
             // store additional fields in the deserialized instance
             IndexDescription instance = thisAdapter.fromJsonTree(jsonObj);
             for (Map.Entry<String, JsonElement> entry : jsonObj.entrySet()) {
               if (!openapiFields.contains(entry.getKey())) {
                 if (entry.getValue().isJsonPrimitive()) { // primitive type
                   if (entry.getValue().getAsJsonPrimitive().isString())
                     instance.putAdditionalProperty(entry.getKey(), entry.getValue().getAsString());
                   else if (entry.getValue().getAsJsonPrimitive().isNumber())
                     instance.putAdditionalProperty(entry.getKey(), entry.getValue().getAsNumber());
                   else if (entry.getValue().getAsJsonPrimitive().isBoolean())
                     instance.putAdditionalProperty(entry.getKey(), entry.getValue().getAsBoolean());
                   else
                     throw new IllegalArgumentException(String.format("The field `%s` has unknown primitive type. Value: %s", entry.getKey(), entry.getValue().toString()));
                 } else if (entry.getValue().isJsonArray()) {
                     instance.putAdditionalProperty(entry.getKey(), gson.fromJson(entry.getValue(), List.class));
                 } else { // JSON object
                     instance.putAdditionalProperty(entry.getKey(), gson.fromJson(entry.getValue(), HashMap.class));
                 }
               }
             }
             return instance;
           }

       }.nullSafe();
    }
  }

 /**
  * Create an instance of IndexDescription given an JSON string
  *
  * @param jsonString JSON string
  * @return An instance of IndexDescription
  * @throws IOException if the JSON string is invalid with respect to IndexDescription
  */
  public static IndexDescription fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, IndexDescription.class);
  }

 /**
  * Convert an instance of IndexDescription to an JSON string
  *
  * @return JSON string
  */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}

