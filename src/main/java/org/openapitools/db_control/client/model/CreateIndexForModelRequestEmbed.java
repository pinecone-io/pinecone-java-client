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
 * Specify the integrated inference embedding configuration for the index.  Once set the model cannot be changed, but you can later update the embedding configuration for an integrated inference index including field map, read parameters, or write parameters.  Refer to the [model guide](https://docs.pinecone.io/guides/inference/understanding-inference#embedding-models) for available models and model details.
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2025-03-04T16:21:04.592916Z[Etc/UTC]")
public class CreateIndexForModelRequestEmbed {
  public static final String SERIALIZED_NAME_MODEL = "model";
  @SerializedName(SERIALIZED_NAME_MODEL)
  private String model;

  /**
   * The distance metric to be used for similarity search. You can use &#39;euclidean&#39;, &#39;cosine&#39;, or &#39;dotproduct&#39;. If not specified, the metric will be defaulted according to the model. Cannot be updated once set.
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

  public static final String SERIALIZED_NAME_FIELD_MAP = "field_map";
  @SerializedName(SERIALIZED_NAME_FIELD_MAP)
  private Object fieldMap;

  public static final String SERIALIZED_NAME_DIMENSION = "dimension";
  @SerializedName(SERIALIZED_NAME_DIMENSION)
  private Integer dimension;

  public static final String SERIALIZED_NAME_READ_PARAMETERS = "read_parameters";
  @SerializedName(SERIALIZED_NAME_READ_PARAMETERS)
  private Object readParameters;

  public static final String SERIALIZED_NAME_WRITE_PARAMETERS = "write_parameters";
  @SerializedName(SERIALIZED_NAME_WRITE_PARAMETERS)
  private Object writeParameters;

  public CreateIndexForModelRequestEmbed() {
  }

  public CreateIndexForModelRequestEmbed model(String model) {
    
    this.model = model;
    return this;
  }

   /**
   * The name of the embedding model to use for the index.
   * @return model
  **/
  @javax.annotation.Nonnull
  public String getModel() {
    return model;
  }


  public void setModel(String model) {
    this.model = model;
  }


  public CreateIndexForModelRequestEmbed metric(MetricEnum metric) {
    
    this.metric = metric;
    return this;
  }

   /**
   * The distance metric to be used for similarity search. You can use &#39;euclidean&#39;, &#39;cosine&#39;, or &#39;dotproduct&#39;. If not specified, the metric will be defaulted according to the model. Cannot be updated once set.
   * @return metric
  **/
  @javax.annotation.Nullable
  public MetricEnum getMetric() {
    return metric;
  }


  public void setMetric(MetricEnum metric) {
    this.metric = metric;
  }


  public CreateIndexForModelRequestEmbed fieldMap(Object fieldMap) {
    
    this.fieldMap = fieldMap;
    return this;
  }

   /**
   * Identifies the name of the text field from your document model that will be embedded.
   * @return fieldMap
  **/
  @javax.annotation.Nonnull
  public Object getFieldMap() {
    return fieldMap;
  }


  public void setFieldMap(Object fieldMap) {
    this.fieldMap = fieldMap;
  }


  public CreateIndexForModelRequestEmbed dimension(Integer dimension) {
    
    this.dimension = dimension;
    return this;
  }

   /**
   * The dimension of embedding vectors produced for the index.
   * @return dimension
  **/
  @javax.annotation.Nullable
  public Integer getDimension() {
    return dimension;
  }


  public void setDimension(Integer dimension) {
    this.dimension = dimension;
  }


  public CreateIndexForModelRequestEmbed readParameters(Object readParameters) {
    
    this.readParameters = readParameters;
    return this;
  }

   /**
   * The read parameters for the embedding model.
   * @return readParameters
  **/
  @javax.annotation.Nullable
  public Object getReadParameters() {
    return readParameters;
  }


  public void setReadParameters(Object readParameters) {
    this.readParameters = readParameters;
  }


  public CreateIndexForModelRequestEmbed writeParameters(Object writeParameters) {
    
    this.writeParameters = writeParameters;
    return this;
  }

   /**
   * The write parameters for the embedding model.
   * @return writeParameters
  **/
  @javax.annotation.Nullable
  public Object getWriteParameters() {
    return writeParameters;
  }


  public void setWriteParameters(Object writeParameters) {
    this.writeParameters = writeParameters;
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
   * @return the CreateIndexForModelRequestEmbed instance itself
   */
  public CreateIndexForModelRequestEmbed putAdditionalProperty(String key, Object value) {
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
    CreateIndexForModelRequestEmbed createIndexForModelRequestEmbed = (CreateIndexForModelRequestEmbed) o;
    return Objects.equals(this.model, createIndexForModelRequestEmbed.model) &&
        Objects.equals(this.metric, createIndexForModelRequestEmbed.metric) &&
        Objects.equals(this.fieldMap, createIndexForModelRequestEmbed.fieldMap) &&
        Objects.equals(this.dimension, createIndexForModelRequestEmbed.dimension) &&
        Objects.equals(this.readParameters, createIndexForModelRequestEmbed.readParameters) &&
        Objects.equals(this.writeParameters, createIndexForModelRequestEmbed.writeParameters)&&
        Objects.equals(this.additionalProperties, createIndexForModelRequestEmbed.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(model, metric, fieldMap, dimension, readParameters, writeParameters, additionalProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateIndexForModelRequestEmbed {\n");
    sb.append("    model: ").append(toIndentedString(model)).append("\n");
    sb.append("    metric: ").append(toIndentedString(metric)).append("\n");
    sb.append("    fieldMap: ").append(toIndentedString(fieldMap)).append("\n");
    sb.append("    dimension: ").append(toIndentedString(dimension)).append("\n");
    sb.append("    readParameters: ").append(toIndentedString(readParameters)).append("\n");
    sb.append("    writeParameters: ").append(toIndentedString(writeParameters)).append("\n");
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
    openapiFields.add("model");
    openapiFields.add("metric");
    openapiFields.add("field_map");
    openapiFields.add("dimension");
    openapiFields.add("read_parameters");
    openapiFields.add("write_parameters");

    // a set of required properties/fields (JSON key names)
    openapiRequiredFields = new HashSet<String>();
    openapiRequiredFields.add("model");
    openapiRequiredFields.add("field_map");
  }

 /**
  * Validates the JSON Element and throws an exception if issues found
  *
  * @param jsonElement JSON Element
  * @throws IOException if the JSON Element is invalid with respect to CreateIndexForModelRequestEmbed
  */
  public static void validateJsonElement(JsonElement jsonElement) throws IOException {
      if (jsonElement == null) {
        if (!CreateIndexForModelRequestEmbed.openapiRequiredFields.isEmpty()) { // has required fields but JSON element is null
          throw new IllegalArgumentException(String.format("The required field(s) %s in CreateIndexForModelRequestEmbed is not found in the empty JSON string", CreateIndexForModelRequestEmbed.openapiRequiredFields.toString()));
        }
      }

      // check to make sure all required properties/fields are present in the JSON string
      for (String requiredField : CreateIndexForModelRequestEmbed.openapiRequiredFields) {
        if (jsonElement.getAsJsonObject().get(requiredField) == null) {
          throw new IllegalArgumentException(String.format("The required field `%s` is not found in the JSON string: %s", requiredField, jsonElement.toString()));
        }
      }
        JsonObject jsonObj = jsonElement.getAsJsonObject();
      if (!jsonObj.get("model").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `model` to be a primitive type in the JSON string but got `%s`", jsonObj.get("model").toString()));
      }
      if ((jsonObj.get("metric") != null && !jsonObj.get("metric").isJsonNull()) && !jsonObj.get("metric").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `metric` to be a primitive type in the JSON string but got `%s`", jsonObj.get("metric").toString()));
      }
  }

  public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
       if (!CreateIndexForModelRequestEmbed.class.isAssignableFrom(type.getRawType())) {
         return null; // this class only serializes 'CreateIndexForModelRequestEmbed' and its subtypes
       }
       final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
       final TypeAdapter<CreateIndexForModelRequestEmbed> thisAdapter
                        = gson.getDelegateAdapter(this, TypeToken.get(CreateIndexForModelRequestEmbed.class));

       return (TypeAdapter<T>) new TypeAdapter<CreateIndexForModelRequestEmbed>() {
           @Override
           public void write(JsonWriter out, CreateIndexForModelRequestEmbed value) throws IOException {
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
           public CreateIndexForModelRequestEmbed read(JsonReader in) throws IOException {
             JsonElement jsonElement = elementAdapter.read(in);
             validateJsonElement(jsonElement);
             JsonObject jsonObj = jsonElement.getAsJsonObject();
             // store additional fields in the deserialized instance
             CreateIndexForModelRequestEmbed instance = thisAdapter.fromJsonTree(jsonObj);
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
  * Create an instance of CreateIndexForModelRequestEmbed given an JSON string
  *
  * @param jsonString JSON string
  * @return An instance of CreateIndexForModelRequestEmbed
  * @throws IOException if the JSON string is invalid with respect to CreateIndexForModelRequestEmbed
  */
  public static CreateIndexForModelRequestEmbed fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, CreateIndexForModelRequestEmbed.class);
  }

 /**
  * Convert an instance of CreateIndexForModelRequestEmbed to an JSON string
  *
  * @return JSON string
  */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}

