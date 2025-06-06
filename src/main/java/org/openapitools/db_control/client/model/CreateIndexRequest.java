/*
 * Pinecone Control Plane API
 * Pinecone is a vector database that makes it easy to search and retrieve billions of high-dimensional vectors.
 *
 * The version of the OpenAPI document: 2025-04
 * Contact: support@pinecone.io
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package org.openapitools.db_control.client.model;

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
import org.openapitools.db_control.client.model.DeletionProtection;
import org.openapitools.db_control.client.model.IndexSpec;

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

import org.openapitools.db_control.client.JSON;

/**
 * The configuration needed to create a Pinecone index.
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2025-05-22T16:07:13.211110Z[Etc/UTC]")
public class CreateIndexRequest {
  public static final String SERIALIZED_NAME_NAME = "name";
  @SerializedName(SERIALIZED_NAME_NAME)
  private String name;

  public static final String SERIALIZED_NAME_DIMENSION = "dimension";
  @SerializedName(SERIALIZED_NAME_DIMENSION)
  private Integer dimension;

  /**
   * The distance metric to be used for similarity search. You can use &#39;euclidean&#39;, &#39;cosine&#39;, or &#39;dotproduct&#39;. If the &#39;vector_type&#39; is &#39;sparse&#39;, the metric must be &#39;dotproduct&#39;. If the &#x60;vector_type&#x60; is &#x60;dense&#x60;, the metric defaults to &#39;cosine&#39;.
   */
  @JsonAdapter(MetricEnum.Adapter.class)
  public enum MetricEnum {
    COSINE("cosine"),
    
    EUCLIDEAN("euclidean"),
    
    DOTPRODUCT("dotproduct");

    private String value;

    MetricEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static MetricEnum fromValue(String value) {
      for (MetricEnum b : MetricEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

    public static class Adapter extends TypeAdapter<MetricEnum> {
      @Override
      public void write(final JsonWriter jsonWriter, final MetricEnum enumeration) throws IOException {
        jsonWriter.value(enumeration.getValue());
      }

      @Override
      public MetricEnum read(final JsonReader jsonReader) throws IOException {
        String value =  jsonReader.nextString();
        return MetricEnum.fromValue(value);
      }
    }
  }

  public static final String SERIALIZED_NAME_METRIC = "metric";
  @SerializedName(SERIALIZED_NAME_METRIC)
  private MetricEnum metric;

  public static final String SERIALIZED_NAME_DELETION_PROTECTION = "deletion_protection";
  @SerializedName(SERIALIZED_NAME_DELETION_PROTECTION)
  private DeletionProtection deletionProtection = DeletionProtection.DISABLED;

  public static final String SERIALIZED_NAME_TAGS = "tags";
  @SerializedName(SERIALIZED_NAME_TAGS)
  private Map<String, String> tags = new HashMap<>();

  public static final String SERIALIZED_NAME_SPEC = "spec";
  @SerializedName(SERIALIZED_NAME_SPEC)
  private IndexSpec spec;

  public static final String SERIALIZED_NAME_VECTOR_TYPE = "vector_type";
  @SerializedName(SERIALIZED_NAME_VECTOR_TYPE)
  private String vectorType = "dense";

  public CreateIndexRequest() {
  }

  public CreateIndexRequest name(String name) {
    
    this.name = name;
    return this;
  }

   /**
   * The name of the index. Resource name must be 1-45 characters long, start and end with an alphanumeric character, and consist only of lower case alphanumeric characters or &#39;-&#39;. 
   * @return name
  **/
  @javax.annotation.Nonnull
  public String getName() {
    return name;
  }


  public void setName(String name) {
    this.name = name;
  }


  public CreateIndexRequest dimension(Integer dimension) {
    
    this.dimension = dimension;
    return this;
  }

   /**
   * The dimensions of the vectors to be inserted in the index.
   * minimum: 1
   * maximum: 20000
   * @return dimension
  **/
  @javax.annotation.Nullable
  public Integer getDimension() {
    return dimension;
  }


  public void setDimension(Integer dimension) {
    this.dimension = dimension;
  }


  public CreateIndexRequest metric(MetricEnum metric) {
    
    this.metric = metric;
    return this;
  }

   /**
   * The distance metric to be used for similarity search. You can use &#39;euclidean&#39;, &#39;cosine&#39;, or &#39;dotproduct&#39;. If the &#39;vector_type&#39; is &#39;sparse&#39;, the metric must be &#39;dotproduct&#39;. If the &#x60;vector_type&#x60; is &#x60;dense&#x60;, the metric defaults to &#39;cosine&#39;.
   * @return metric
  **/
  @javax.annotation.Nullable
  public MetricEnum getMetric() {
    return metric;
  }


  public void setMetric(MetricEnum metric) {
    this.metric = metric;
  }


  public CreateIndexRequest deletionProtection(DeletionProtection deletionProtection) {
    
    this.deletionProtection = deletionProtection;
    return this;
  }

   /**
   * Get deletionProtection
   * @return deletionProtection
  **/
  @javax.annotation.Nullable
  public DeletionProtection getDeletionProtection() {
    return deletionProtection;
  }


  public void setDeletionProtection(DeletionProtection deletionProtection) {
    this.deletionProtection = deletionProtection;
  }


  public CreateIndexRequest tags(Map<String, String> tags) {
    
    this.tags = tags;
    return this;
  }

  public CreateIndexRequest putTagsItem(String key, String tagsItem) {
    if (this.tags == null) {
      this.tags = new HashMap<>();
    }
    this.tags.put(key, tagsItem);
    return this;
  }

   /**
   * Custom user tags added to an index. Keys must be 80 characters or less. Values must be 120 characters or less. Keys must be alphanumeric, &#39;_&#39;, or &#39;-&#39;.  Values must be alphanumeric, &#39;;&#39;, &#39;@&#39;, &#39;_&#39;, &#39;-&#39;, &#39;.&#39;, &#39;+&#39;, or &#39; &#39;. To unset a key, set the value to be an empty string.
   * @return tags
  **/
  @javax.annotation.Nullable
  public Map<String, String> getTags() {
    return tags;
  }


  public void setTags(Map<String, String> tags) {
    this.tags = tags;
  }


  public CreateIndexRequest spec(IndexSpec spec) {
    
    this.spec = spec;
    return this;
  }

   /**
   * Get spec
   * @return spec
  **/
  @javax.annotation.Nullable
  public IndexSpec getSpec() {
    return spec;
  }


  public void setSpec(IndexSpec spec) {
    this.spec = spec;
  }


  public CreateIndexRequest vectorType(String vectorType) {
    
    this.vectorType = vectorType;
    return this;
  }

   /**
   * The index vector type. You can use &#39;dense&#39; or &#39;sparse&#39;. If &#39;dense&#39;, the vector dimension must be specified.  If &#39;sparse&#39;, the vector dimension should not be specified.
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
   * @return the CreateIndexRequest instance itself
   */
  public CreateIndexRequest putAdditionalProperty(String key, Object value) {
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
    CreateIndexRequest createIndexRequest = (CreateIndexRequest) o;
    return Objects.equals(this.name, createIndexRequest.name) &&
        Objects.equals(this.dimension, createIndexRequest.dimension) &&
        Objects.equals(this.metric, createIndexRequest.metric) &&
        Objects.equals(this.deletionProtection, createIndexRequest.deletionProtection) &&
        Objects.equals(this.tags, createIndexRequest.tags) &&
        Objects.equals(this.spec, createIndexRequest.spec) &&
        Objects.equals(this.vectorType, createIndexRequest.vectorType)&&
        Objects.equals(this.additionalProperties, createIndexRequest.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, dimension, metric, deletionProtection, tags, spec, vectorType, additionalProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateIndexRequest {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    dimension: ").append(toIndentedString(dimension)).append("\n");
    sb.append("    metric: ").append(toIndentedString(metric)).append("\n");
    sb.append("    deletionProtection: ").append(toIndentedString(deletionProtection)).append("\n");
    sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
    sb.append("    spec: ").append(toIndentedString(spec)).append("\n");
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
    openapiFields.add("name");
    openapiFields.add("dimension");
    openapiFields.add("metric");
    openapiFields.add("deletion_protection");
    openapiFields.add("tags");
    openapiFields.add("spec");
    openapiFields.add("vector_type");

    // a set of required properties/fields (JSON key names)
    openapiRequiredFields = new HashSet<String>();
    openapiRequiredFields.add("name");
    openapiRequiredFields.add("spec");
  }

 /**
  * Validates the JSON Element and throws an exception if issues found
  *
  * @param jsonElement JSON Element
  * @throws IOException if the JSON Element is invalid with respect to CreateIndexRequest
  */
  public static void validateJsonElement(JsonElement jsonElement) throws IOException {
      if (jsonElement == null) {
        if (!CreateIndexRequest.openapiRequiredFields.isEmpty()) { // has required fields but JSON element is null
          throw new IllegalArgumentException(String.format("The required field(s) %s in CreateIndexRequest is not found in the empty JSON string", CreateIndexRequest.openapiRequiredFields.toString()));
        }
      }

      // check to make sure all required properties/fields are present in the JSON string
      for (String requiredField : CreateIndexRequest.openapiRequiredFields) {
        if (jsonElement.getAsJsonObject().get(requiredField) == null) {
          throw new IllegalArgumentException(String.format("The required field `%s` is not found in the JSON string: %s", requiredField, jsonElement.toString()));
        }
      }
        JsonObject jsonObj = jsonElement.getAsJsonObject();
      if (!jsonObj.get("name").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `name` to be a primitive type in the JSON string but got `%s`", jsonObj.get("name").toString()));
      }
      if ((jsonObj.get("metric") != null && !jsonObj.get("metric").isJsonNull()) && !jsonObj.get("metric").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `metric` to be a primitive type in the JSON string but got `%s`", jsonObj.get("metric").toString()));
      }
      // validate the required field `spec`
      IndexSpec.validateJsonElement(jsonObj.get("spec"));
      if ((jsonObj.get("vector_type") != null && !jsonObj.get("vector_type").isJsonNull()) && !jsonObj.get("vector_type").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `vector_type` to be a primitive type in the JSON string but got `%s`", jsonObj.get("vector_type").toString()));
      }
  }

  public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
       if (!CreateIndexRequest.class.isAssignableFrom(type.getRawType())) {
         return null; // this class only serializes 'CreateIndexRequest' and its subtypes
       }
       final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
       final TypeAdapter<CreateIndexRequest> thisAdapter
                        = gson.getDelegateAdapter(this, TypeToken.get(CreateIndexRequest.class));

       return (TypeAdapter<T>) new TypeAdapter<CreateIndexRequest>() {
           @Override
           public void write(JsonWriter out, CreateIndexRequest value) throws IOException {
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
           public CreateIndexRequest read(JsonReader in) throws IOException {
             JsonElement jsonElement = elementAdapter.read(in);
             validateJsonElement(jsonElement);
             JsonObject jsonObj = jsonElement.getAsJsonObject();
             // store additional fields in the deserialized instance
             CreateIndexRequest instance = thisAdapter.fromJsonTree(jsonObj);
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
  * Create an instance of CreateIndexRequest given an JSON string
  *
  * @param jsonString JSON string
  * @return An instance of CreateIndexRequest
  * @throws IOException if the JSON string is invalid with respect to CreateIndexRequest
  */
  public static CreateIndexRequest fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, CreateIndexRequest.class);
  }

 /**
  * Convert an instance of CreateIndexRequest to an JSON string
  *
  * @return JSON string
  */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}

