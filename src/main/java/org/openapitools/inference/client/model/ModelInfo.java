/*
 * Pinecone Inference API
 * Pinecone is a vector database that makes it easy to search and retrieve billions of high-dimensional vectors.
 *
 * The version of the OpenAPI document: 2025-04
 * Contact: support@pinecone.io
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package org.openapitools.inference.client.model;

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
import org.openapitools.inference.client.model.ModelInfoMetric;
import org.openapitools.inference.client.model.ModelInfoSupportedParameter;

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

import org.openapitools.inference.client.JSON;

/**
 * Represents the model configuration including model type, supported parameters, and other model details.
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2025-04-09T20:09:49.238595Z[Etc/UTC]")
public class ModelInfo {
  public static final String SERIALIZED_NAME_NAME = "name";
  @SerializedName(SERIALIZED_NAME_NAME)
  private String name;

  public static final String SERIALIZED_NAME_SHORT_DESCRIPTION = "short_description";
  @SerializedName(SERIALIZED_NAME_SHORT_DESCRIPTION)
  private String shortDescription;

  /**
   * The type of model (e.g. &#39;embed&#39; or &#39;rerank&#39;).
   */
  @JsonAdapter(TypeEnum.Adapter.class)
  public enum TypeEnum {
    EMBED("embed"),
    
    RERANK("rerank");

    private String value;

    TypeEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static TypeEnum fromValue(String value) {
      for (TypeEnum b : TypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

    public static class Adapter extends TypeAdapter<TypeEnum> {
      @Override
      public void write(final JsonWriter jsonWriter, final TypeEnum enumeration) throws IOException {
        jsonWriter.value(enumeration.getValue());
      }

      @Override
      public TypeEnum read(final JsonReader jsonReader) throws IOException {
        String value =  jsonReader.nextString();
        return TypeEnum.fromValue(value);
      }
    }
  }

  public static final String SERIALIZED_NAME_TYPE = "type";
  @SerializedName(SERIALIZED_NAME_TYPE)
  private TypeEnum type;

  /**
   * Whether the embedding model produces &#39;dense&#39; or &#39;sparse&#39; embeddings.
   */
  @JsonAdapter(VectorTypeEnum.Adapter.class)
  public enum VectorTypeEnum {
    DENSE("dense"),
    
    SPARSE("sparse");

    private String value;

    VectorTypeEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static VectorTypeEnum fromValue(String value) {
      for (VectorTypeEnum b : VectorTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

    public static class Adapter extends TypeAdapter<VectorTypeEnum> {
      @Override
      public void write(final JsonWriter jsonWriter, final VectorTypeEnum enumeration) throws IOException {
        jsonWriter.value(enumeration.getValue());
      }

      @Override
      public VectorTypeEnum read(final JsonReader jsonReader) throws IOException {
        String value =  jsonReader.nextString();
        return VectorTypeEnum.fromValue(value);
      }
    }
  }

  public static final String SERIALIZED_NAME_VECTOR_TYPE = "vector_type";
  @SerializedName(SERIALIZED_NAME_VECTOR_TYPE)
  private VectorTypeEnum vectorType;

  public static final String SERIALIZED_NAME_DIMENSION = "dimension";
  @SerializedName(SERIALIZED_NAME_DIMENSION)
  private Integer dimension;

  public static final String SERIALIZED_NAME_MODALITY = "modality";
  @SerializedName(SERIALIZED_NAME_MODALITY)
  private String modality;

  public static final String SERIALIZED_NAME_SEQUENCE_LENGTH = "sequence_length";
  @SerializedName(SERIALIZED_NAME_SEQUENCE_LENGTH)
  private Integer sequenceLength;

  public static final String SERIALIZED_NAME_BATCH_SIZE = "batch_size";
  @SerializedName(SERIALIZED_NAME_BATCH_SIZE)
  private Integer batchSize;

  public static final String SERIALIZED_NAME_SUPPORTED_METRICS = "supported_metrics";
  @SerializedName(SERIALIZED_NAME_SUPPORTED_METRICS)
  private List<ModelInfoMetric> supportedMetrics;

  public static final String SERIALIZED_NAME_SUPPORTED_PARAMETERS = "supported_parameters";
  @SerializedName(SERIALIZED_NAME_SUPPORTED_PARAMETERS)
  private List<ModelInfoSupportedParameter> supportedParameters = new ArrayList<>();

  public ModelInfo() {
  }

  public ModelInfo name(String name) {
    
    this.name = name;
    return this;
  }

   /**
   * The name of the model.
   * @return name
  **/
  @javax.annotation.Nonnull
  public String getName() {
    return name;
  }


  public void setName(String name) {
    this.name = name;
  }


  public ModelInfo shortDescription(String shortDescription) {
    
    this.shortDescription = shortDescription;
    return this;
  }

   /**
   * A summary of the model.
   * @return shortDescription
  **/
  @javax.annotation.Nonnull
  public String getShortDescription() {
    return shortDescription;
  }


  public void setShortDescription(String shortDescription) {
    this.shortDescription = shortDescription;
  }


  public ModelInfo type(TypeEnum type) {
    
    this.type = type;
    return this;
  }

   /**
   * The type of model (e.g. &#39;embed&#39; or &#39;rerank&#39;).
   * @return type
  **/
  @javax.annotation.Nonnull
  public TypeEnum getType() {
    return type;
  }


  public void setType(TypeEnum type) {
    this.type = type;
  }


  public ModelInfo vectorType(VectorTypeEnum vectorType) {
    
    this.vectorType = vectorType;
    return this;
  }

   /**
   * Whether the embedding model produces &#39;dense&#39; or &#39;sparse&#39; embeddings.
   * @return vectorType
  **/
  @javax.annotation.Nullable
  public VectorTypeEnum getVectorType() {
    return vectorType;
  }


  public void setVectorType(VectorTypeEnum vectorType) {
    this.vectorType = vectorType;
  }


  public ModelInfo dimension(Integer dimension) {
    
    this.dimension = dimension;
    return this;
  }

   /**
   * The embedding model dimension (applies to dense embedding models only).
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


  public ModelInfo modality(String modality) {
    
    this.modality = modality;
    return this;
  }

   /**
   * The modality of the model (e.g. &#39;text&#39;).
   * @return modality
  **/
  @javax.annotation.Nullable
  public String getModality() {
    return modality;
  }


  public void setModality(String modality) {
    this.modality = modality;
  }


  public ModelInfo sequenceLength(Integer sequenceLength) {
    
    this.sequenceLength = sequenceLength;
    return this;
  }

   /**
   * The maximum tokens per sequence supported by the model.
   * minimum: 1
   * @return sequenceLength
  **/
  @javax.annotation.Nullable
  public Integer getSequenceLength() {
    return sequenceLength;
  }


  public void setSequenceLength(Integer sequenceLength) {
    this.sequenceLength = sequenceLength;
  }


  public ModelInfo batchSize(Integer batchSize) {
    
    this.batchSize = batchSize;
    return this;
  }

   /**
   * The maximum batch size (number of sequences) supported by the model.
   * minimum: 1
   * @return batchSize
  **/
  @javax.annotation.Nullable
  public Integer getBatchSize() {
    return batchSize;
  }


  public void setBatchSize(Integer batchSize) {
    this.batchSize = batchSize;
  }


  public ModelInfo supportedMetrics(List<ModelInfoMetric> supportedMetrics) {
    
    this.supportedMetrics = supportedMetrics;
    return this;
  }

  public ModelInfo addSupportedMetricsItem(ModelInfoMetric supportedMetricsItem) {
    if (this.supportedMetrics == null) {
      this.supportedMetrics = new ArrayList<>();
    }
    this.supportedMetrics.add(supportedMetricsItem);
    return this;
  }

   /**
   * The distance metrics supported by the model for similarity search.
   * @return supportedMetrics
  **/
  @javax.annotation.Nullable
  public List<ModelInfoMetric> getSupportedMetrics() {
    return supportedMetrics;
  }


  public void setSupportedMetrics(List<ModelInfoMetric> supportedMetrics) {
    this.supportedMetrics = supportedMetrics;
  }


  public ModelInfo supportedParameters(List<ModelInfoSupportedParameter> supportedParameters) {
    
    this.supportedParameters = supportedParameters;
    return this;
  }

  public ModelInfo addSupportedParametersItem(ModelInfoSupportedParameter supportedParametersItem) {
    if (this.supportedParameters == null) {
      this.supportedParameters = new ArrayList<>();
    }
    this.supportedParameters.add(supportedParametersItem);
    return this;
  }

   /**
   * Get supportedParameters
   * @return supportedParameters
  **/
  @javax.annotation.Nonnull
  public List<ModelInfoSupportedParameter> getSupportedParameters() {
    return supportedParameters;
  }


  public void setSupportedParameters(List<ModelInfoSupportedParameter> supportedParameters) {
    this.supportedParameters = supportedParameters;
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
   * @return the ModelInfo instance itself
   */
  public ModelInfo putAdditionalProperty(String key, Object value) {
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
    ModelInfo modelInfo = (ModelInfo) o;
    return Objects.equals(this.name, modelInfo.name) &&
        Objects.equals(this.shortDescription, modelInfo.shortDescription) &&
        Objects.equals(this.type, modelInfo.type) &&
        Objects.equals(this.vectorType, modelInfo.vectorType) &&
        Objects.equals(this.dimension, modelInfo.dimension) &&
        Objects.equals(this.modality, modelInfo.modality) &&
        Objects.equals(this.sequenceLength, modelInfo.sequenceLength) &&
        Objects.equals(this.batchSize, modelInfo.batchSize) &&
        Objects.equals(this.supportedMetrics, modelInfo.supportedMetrics) &&
        Objects.equals(this.supportedParameters, modelInfo.supportedParameters)&&
        Objects.equals(this.additionalProperties, modelInfo.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, shortDescription, type, vectorType, dimension, modality, sequenceLength, batchSize, supportedMetrics, supportedParameters, additionalProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ModelInfo {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    shortDescription: ").append(toIndentedString(shortDescription)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    vectorType: ").append(toIndentedString(vectorType)).append("\n");
    sb.append("    dimension: ").append(toIndentedString(dimension)).append("\n");
    sb.append("    modality: ").append(toIndentedString(modality)).append("\n");
    sb.append("    sequenceLength: ").append(toIndentedString(sequenceLength)).append("\n");
    sb.append("    batchSize: ").append(toIndentedString(batchSize)).append("\n");
    sb.append("    supportedMetrics: ").append(toIndentedString(supportedMetrics)).append("\n");
    sb.append("    supportedParameters: ").append(toIndentedString(supportedParameters)).append("\n");
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
    openapiFields.add("short_description");
    openapiFields.add("type");
    openapiFields.add("vector_type");
    openapiFields.add("dimension");
    openapiFields.add("modality");
    openapiFields.add("sequence_length");
    openapiFields.add("batch_size");
    openapiFields.add("supported_metrics");
    openapiFields.add("supported_parameters");

    // a set of required properties/fields (JSON key names)
    openapiRequiredFields = new HashSet<String>();
    openapiRequiredFields.add("name");
    openapiRequiredFields.add("short_description");
    openapiRequiredFields.add("type");
    openapiRequiredFields.add("supported_parameters");
  }

 /**
  * Validates the JSON Element and throws an exception if issues found
  *
  * @param jsonElement JSON Element
  * @throws IOException if the JSON Element is invalid with respect to ModelInfo
  */
  public static void validateJsonElement(JsonElement jsonElement) throws IOException {
      if (jsonElement == null) {
        if (!ModelInfo.openapiRequiredFields.isEmpty()) { // has required fields but JSON element is null
          throw new IllegalArgumentException(String.format("The required field(s) %s in ModelInfo is not found in the empty JSON string", ModelInfo.openapiRequiredFields.toString()));
        }
      }

      // check to make sure all required properties/fields are present in the JSON string
      for (String requiredField : ModelInfo.openapiRequiredFields) {
        if (jsonElement.getAsJsonObject().get(requiredField) == null) {
          throw new IllegalArgumentException(String.format("The required field `%s` is not found in the JSON string: %s", requiredField, jsonElement.toString()));
        }
      }
        JsonObject jsonObj = jsonElement.getAsJsonObject();
      if (!jsonObj.get("name").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `name` to be a primitive type in the JSON string but got `%s`", jsonObj.get("name").toString()));
      }
      if (!jsonObj.get("short_description").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `short_description` to be a primitive type in the JSON string but got `%s`", jsonObj.get("short_description").toString()));
      }
      if (!jsonObj.get("type").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `type` to be a primitive type in the JSON string but got `%s`", jsonObj.get("type").toString()));
      }
      if ((jsonObj.get("vector_type") != null && !jsonObj.get("vector_type").isJsonNull()) && !jsonObj.get("vector_type").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `vector_type` to be a primitive type in the JSON string but got `%s`", jsonObj.get("vector_type").toString()));
      }
      if ((jsonObj.get("modality") != null && !jsonObj.get("modality").isJsonNull()) && !jsonObj.get("modality").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `modality` to be a primitive type in the JSON string but got `%s`", jsonObj.get("modality").toString()));
      }
      // ensure the optional json data is an array if present
      if (jsonObj.get("supported_metrics") != null && !jsonObj.get("supported_metrics").isJsonNull() && !jsonObj.get("supported_metrics").isJsonArray()) {
        throw new IllegalArgumentException(String.format("Expected the field `supported_metrics` to be an array in the JSON string but got `%s`", jsonObj.get("supported_metrics").toString()));
      }
      // ensure the json data is an array
      if (!jsonObj.get("supported_parameters").isJsonArray()) {
        throw new IllegalArgumentException(String.format("Expected the field `supported_parameters` to be an array in the JSON string but got `%s`", jsonObj.get("supported_parameters").toString()));
      }

      JsonArray jsonArraysupportedParameters = jsonObj.getAsJsonArray("supported_parameters");
      // validate the required field `supported_parameters` (array)
      for (int i = 0; i < jsonArraysupportedParameters.size(); i++) {
        ModelInfoSupportedParameter.validateJsonElement(jsonArraysupportedParameters.get(i));
      };
  }

  public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
       if (!ModelInfo.class.isAssignableFrom(type.getRawType())) {
         return null; // this class only serializes 'ModelInfo' and its subtypes
       }
       final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
       final TypeAdapter<ModelInfo> thisAdapter
                        = gson.getDelegateAdapter(this, TypeToken.get(ModelInfo.class));

       return (TypeAdapter<T>) new TypeAdapter<ModelInfo>() {
           @Override
           public void write(JsonWriter out, ModelInfo value) throws IOException {
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
           public ModelInfo read(JsonReader in) throws IOException {
             JsonElement jsonElement = elementAdapter.read(in);
             validateJsonElement(jsonElement);
             JsonObject jsonObj = jsonElement.getAsJsonObject();
             // store additional fields in the deserialized instance
             ModelInfo instance = thisAdapter.fromJsonTree(jsonObj);
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
  * Create an instance of ModelInfo given an JSON string
  *
  * @param jsonString JSON string
  * @return An instance of ModelInfo
  * @throws IOException if the JSON string is invalid with respect to ModelInfo
  */
  public static ModelInfo fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, ModelInfo.class);
  }

 /**
  * Convert an instance of ModelInfo to an JSON string
  *
  * @return JSON string
  */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}

