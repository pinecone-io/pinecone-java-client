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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openapitools.db_data.client.model.QueryVector;
import org.openapitools.db_data.client.model.SparseValues;

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
 * The request for the &#x60;query&#x60; operation.
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2025-03-04T16:21:06.205243Z[Etc/UTC]")
public class QueryRequest {
  public static final String SERIALIZED_NAME_NAMESPACE = "namespace";
  @SerializedName(SERIALIZED_NAME_NAMESPACE)
  private String namespace;

  public static final String SERIALIZED_NAME_TOP_K = "topK";
  @SerializedName(SERIALIZED_NAME_TOP_K)
  private Long topK;

  public static final String SERIALIZED_NAME_FILTER = "filter";
  @SerializedName(SERIALIZED_NAME_FILTER)
  private Object filter;

  public static final String SERIALIZED_NAME_INCLUDE_VALUES = "includeValues";
  @SerializedName(SERIALIZED_NAME_INCLUDE_VALUES)
  private Boolean includeValues = false;

  public static final String SERIALIZED_NAME_INCLUDE_METADATA = "includeMetadata";
  @SerializedName(SERIALIZED_NAME_INCLUDE_METADATA)
  private Boolean includeMetadata = false;

  public static final String SERIALIZED_NAME_QUERIES = "queries";
  @Deprecated
  @SerializedName(SERIALIZED_NAME_QUERIES)
  private List<QueryVector> queries;

  public static final String SERIALIZED_NAME_VECTOR = "vector";
  @SerializedName(SERIALIZED_NAME_VECTOR)
  private List<Float> vector;

  public static final String SERIALIZED_NAME_SPARSE_VECTOR = "sparseVector";
  @SerializedName(SERIALIZED_NAME_SPARSE_VECTOR)
  private SparseValues sparseVector;

  public static final String SERIALIZED_NAME_ID = "id";
  @SerializedName(SERIALIZED_NAME_ID)
  private String id;

  public QueryRequest() {
  }

  public QueryRequest namespace(String namespace) {
    
    this.namespace = namespace;
    return this;
  }

   /**
   * The namespace to query.
   * @return namespace
  **/
  @javax.annotation.Nullable
  public String getNamespace() {
    return namespace;
  }


  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }


  public QueryRequest topK(Long topK) {
    
    this.topK = topK;
    return this;
  }

   /**
   * The number of results to return for each query.
   * minimum: 1
   * maximum: 10000
   * @return topK
  **/
  @javax.annotation.Nonnull
  public Long getTopK() {
    return topK;
  }


  public void setTopK(Long topK) {
    this.topK = topK;
  }


  public QueryRequest filter(Object filter) {
    
    this.filter = filter;
    return this;
  }

   /**
   * The filter to apply. You can use vector metadata to limit your search. See [Understanding metadata](https://docs.pinecone.io/guides/data/understanding-metadata). You can use vector metadata to limit your search. See [Understanding metadata](https://docs.pinecone.io/guides/data/understanding-metadata).
   * @return filter
  **/
  @javax.annotation.Nullable
  public Object getFilter() {
    return filter;
  }


  public void setFilter(Object filter) {
    this.filter = filter;
  }


  public QueryRequest includeValues(Boolean includeValues) {
    
    this.includeValues = includeValues;
    return this;
  }

   /**
   * Indicates whether vector values are included in the response.
   * @return includeValues
  **/
  @javax.annotation.Nullable
  public Boolean getIncludeValues() {
    return includeValues;
  }


  public void setIncludeValues(Boolean includeValues) {
    this.includeValues = includeValues;
  }


  public QueryRequest includeMetadata(Boolean includeMetadata) {
    
    this.includeMetadata = includeMetadata;
    return this;
  }

   /**
   * Indicates whether metadata is included in the response as well as the ids.
   * @return includeMetadata
  **/
  @javax.annotation.Nullable
  public Boolean getIncludeMetadata() {
    return includeMetadata;
  }


  public void setIncludeMetadata(Boolean includeMetadata) {
    this.includeMetadata = includeMetadata;
  }


  @Deprecated
  public QueryRequest queries(List<QueryVector> queries) {
    
    this.queries = queries;
    return this;
  }

  public QueryRequest addQueriesItem(QueryVector queriesItem) {
    if (this.queries == null) {
      this.queries = new ArrayList<>();
    }
    this.queries.add(queriesItem);
    return this;
  }

   /**
   * DEPRECATED. Use &#x60;vector&#x60; or &#x60;id&#x60; instead.
   * @return queries
   * @deprecated
  **/
  @Deprecated
  @javax.annotation.Nullable
  public List<QueryVector> getQueries() {
    return queries;
  }


  @Deprecated
  public void setQueries(List<QueryVector> queries) {
    this.queries = queries;
  }


  public QueryRequest vector(List<Float> vector) {
    
    this.vector = vector;
    return this;
  }

  public QueryRequest addVectorItem(Float vectorItem) {
    if (this.vector == null) {
      this.vector = new ArrayList<>();
    }
    this.vector.add(vectorItem);
    return this;
  }

   /**
   * The query vector. This should be the same length as the dimension of the index being queried. Each &#x60;query&#x60; request can contain only one of the parameters &#x60;id&#x60; or &#x60;vector&#x60;.
   * @return vector
  **/
  @javax.annotation.Nullable
  public List<Float> getVector() {
    return vector;
  }


  public void setVector(List<Float> vector) {
    this.vector = vector;
  }


  public QueryRequest sparseVector(SparseValues sparseVector) {
    
    this.sparseVector = sparseVector;
    return this;
  }

   /**
   * Get sparseVector
   * @return sparseVector
  **/
  @javax.annotation.Nullable
  public SparseValues getSparseVector() {
    return sparseVector;
  }


  public void setSparseVector(SparseValues sparseVector) {
    this.sparseVector = sparseVector;
  }


  public QueryRequest id(String id) {
    
    this.id = id;
    return this;
  }

   /**
   * The unique ID of the vector to be used as a query vector. Each request  can contain either the &#x60;vector&#x60; or &#x60;id&#x60; parameter.
   * @return id
  **/
  @javax.annotation.Nullable
  public String getId() {
    return id;
  }


  public void setId(String id) {
    this.id = id;
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
   * @return the QueryRequest instance itself
   */
  public QueryRequest putAdditionalProperty(String key, Object value) {
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
    QueryRequest queryRequest = (QueryRequest) o;
    return Objects.equals(this.namespace, queryRequest.namespace) &&
        Objects.equals(this.topK, queryRequest.topK) &&
        Objects.equals(this.filter, queryRequest.filter) &&
        Objects.equals(this.includeValues, queryRequest.includeValues) &&
        Objects.equals(this.includeMetadata, queryRequest.includeMetadata) &&
        Objects.equals(this.queries, queryRequest.queries) &&
        Objects.equals(this.vector, queryRequest.vector) &&
        Objects.equals(this.sparseVector, queryRequest.sparseVector) &&
        Objects.equals(this.id, queryRequest.id)&&
        Objects.equals(this.additionalProperties, queryRequest.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(namespace, topK, filter, includeValues, includeMetadata, queries, vector, sparseVector, id, additionalProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class QueryRequest {\n");
    sb.append("    namespace: ").append(toIndentedString(namespace)).append("\n");
    sb.append("    topK: ").append(toIndentedString(topK)).append("\n");
    sb.append("    filter: ").append(toIndentedString(filter)).append("\n");
    sb.append("    includeValues: ").append(toIndentedString(includeValues)).append("\n");
    sb.append("    includeMetadata: ").append(toIndentedString(includeMetadata)).append("\n");
    sb.append("    queries: ").append(toIndentedString(queries)).append("\n");
    sb.append("    vector: ").append(toIndentedString(vector)).append("\n");
    sb.append("    sparseVector: ").append(toIndentedString(sparseVector)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
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
    openapiFields.add("namespace");
    openapiFields.add("topK");
    openapiFields.add("filter");
    openapiFields.add("includeValues");
    openapiFields.add("includeMetadata");
    openapiFields.add("queries");
    openapiFields.add("vector");
    openapiFields.add("sparseVector");
    openapiFields.add("id");

    // a set of required properties/fields (JSON key names)
    openapiRequiredFields = new HashSet<String>();
    openapiRequiredFields.add("topK");
  }

 /**
  * Validates the JSON Element and throws an exception if issues found
  *
  * @param jsonElement JSON Element
  * @throws IOException if the JSON Element is invalid with respect to QueryRequest
  */
  public static void validateJsonElement(JsonElement jsonElement) throws IOException {
      if (jsonElement == null) {
        if (!QueryRequest.openapiRequiredFields.isEmpty()) { // has required fields but JSON element is null
          throw new IllegalArgumentException(String.format("The required field(s) %s in QueryRequest is not found in the empty JSON string", QueryRequest.openapiRequiredFields.toString()));
        }
      }

      // check to make sure all required properties/fields are present in the JSON string
      for (String requiredField : QueryRequest.openapiRequiredFields) {
        if (jsonElement.getAsJsonObject().get(requiredField) == null) {
          throw new IllegalArgumentException(String.format("The required field `%s` is not found in the JSON string: %s", requiredField, jsonElement.toString()));
        }
      }
        JsonObject jsonObj = jsonElement.getAsJsonObject();
      if ((jsonObj.get("namespace") != null && !jsonObj.get("namespace").isJsonNull()) && !jsonObj.get("namespace").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `namespace` to be a primitive type in the JSON string but got `%s`", jsonObj.get("namespace").toString()));
      }
      if (jsonObj.get("queries") != null && !jsonObj.get("queries").isJsonNull()) {
        JsonArray jsonArrayqueries = jsonObj.getAsJsonArray("queries");
        if (jsonArrayqueries != null) {
          // ensure the json data is an array
          if (!jsonObj.get("queries").isJsonArray()) {
            throw new IllegalArgumentException(String.format("Expected the field `queries` to be an array in the JSON string but got `%s`", jsonObj.get("queries").toString()));
          }

          // validate the optional field `queries` (array)
          for (int i = 0; i < jsonArrayqueries.size(); i++) {
            QueryVector.validateJsonElement(jsonArrayqueries.get(i));
          };
        }
      }
      // ensure the optional json data is an array if present
      if (jsonObj.get("vector") != null && !jsonObj.get("vector").isJsonNull() && !jsonObj.get("vector").isJsonArray()) {
        throw new IllegalArgumentException(String.format("Expected the field `vector` to be an array in the JSON string but got `%s`", jsonObj.get("vector").toString()));
      }
      // validate the optional field `sparseVector`
      if (jsonObj.get("sparseVector") != null && !jsonObj.get("sparseVector").isJsonNull()) {
        SparseValues.validateJsonElement(jsonObj.get("sparseVector"));
      }
      if ((jsonObj.get("id") != null && !jsonObj.get("id").isJsonNull()) && !jsonObj.get("id").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `id` to be a primitive type in the JSON string but got `%s`", jsonObj.get("id").toString()));
      }
  }

  public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
       if (!QueryRequest.class.isAssignableFrom(type.getRawType())) {
         return null; // this class only serializes 'QueryRequest' and its subtypes
       }
       final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
       final TypeAdapter<QueryRequest> thisAdapter
                        = gson.getDelegateAdapter(this, TypeToken.get(QueryRequest.class));

       return (TypeAdapter<T>) new TypeAdapter<QueryRequest>() {
           @Override
           public void write(JsonWriter out, QueryRequest value) throws IOException {
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
           public QueryRequest read(JsonReader in) throws IOException {
             JsonElement jsonElement = elementAdapter.read(in);
             validateJsonElement(jsonElement);
             JsonObject jsonObj = jsonElement.getAsJsonObject();
             // store additional fields in the deserialized instance
             QueryRequest instance = thisAdapter.fromJsonTree(jsonObj);
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
  * Create an instance of QueryRequest given an JSON string
  *
  * @param jsonString JSON string
  * @return An instance of QueryRequest
  * @throws IOException if the JSON string is invalid with respect to QueryRequest
  */
  public static QueryRequest fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, QueryRequest.class);
  }

 /**
  * Convert an instance of QueryRequest to an JSON string
  *
  * @return JSON string
  */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}

