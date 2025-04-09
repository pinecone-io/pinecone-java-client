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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openapitools.inference.client.model.ModelInfoSupportedParameterAllowedValuesInner;
import org.openapitools.inference.client.model.ModelInfoSupportedParameterDefault;

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
 * Describes a parameter supported by the model, including parameter value constraints.
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2025-04-09T20:04:16.895726Z[Etc/UTC]")
public class ModelInfoSupportedParameter {
  public static final String SERIALIZED_NAME_PARAMETER = "parameter";
  @SerializedName(SERIALIZED_NAME_PARAMETER)
  private String parameter;

  public static final String SERIALIZED_NAME_TYPE = "type";
  @SerializedName(SERIALIZED_NAME_TYPE)
  private String type;

  public static final String SERIALIZED_NAME_VALUE_TYPE = "value_type";
  @SerializedName(SERIALIZED_NAME_VALUE_TYPE)
  private String valueType;

  public static final String SERIALIZED_NAME_REQUIRED = "required";
  @SerializedName(SERIALIZED_NAME_REQUIRED)
  private Boolean required;

  public static final String SERIALIZED_NAME_ALLOWED_VALUES = "allowed_values";
  @SerializedName(SERIALIZED_NAME_ALLOWED_VALUES)
  private List<ModelInfoSupportedParameterAllowedValuesInner> allowedValues;

  public static final String SERIALIZED_NAME_MIN = "min";
  @SerializedName(SERIALIZED_NAME_MIN)
  private BigDecimal min;

  public static final String SERIALIZED_NAME_MAX = "max";
  @SerializedName(SERIALIZED_NAME_MAX)
  private BigDecimal max;

  public static final String SERIALIZED_NAME_DEFAULT = "default";
  @SerializedName(SERIALIZED_NAME_DEFAULT)
  private ModelInfoSupportedParameterDefault _default;

  public ModelInfoSupportedParameter() {
  }

  public ModelInfoSupportedParameter parameter(String parameter) {
    
    this.parameter = parameter;
    return this;
  }

   /**
   * The name of the parameter.
   * @return parameter
  **/
  @javax.annotation.Nonnull
  public String getParameter() {
    return parameter;
  }


  public void setParameter(String parameter) {
    this.parameter = parameter;
  }


  public ModelInfoSupportedParameter type(String type) {
    
    this.type = type;
    return this;
  }

   /**
   * The parameter type e.g. &#39;one_of&#39;, &#39;numeric_range&#39;, or &#39;any&#39;.  If the type is &#39;one_of&#39;, then &#39;allowed_values&#39; will be set, and the value specified must be one of the allowed values. &#39;one_of&#39; is only compatible with value_type &#39;string&#39; or &#39;integer&#39;.  If &#39;numeric_range&#39;, then &#39;min&#39; and &#39;max&#39; will be set, then the value specified must adhere to the value_type and must fall within the &#x60;[min, max]&#x60; range (inclusive).  If &#39;any&#39; then any value is allowed, as long as it adheres to the value_type.
   * @return type
  **/
  @javax.annotation.Nonnull
  public String getType() {
    return type;
  }


  public void setType(String type) {
    this.type = type;
  }


  public ModelInfoSupportedParameter valueType(String valueType) {
    
    this.valueType = valueType;
    return this;
  }

   /**
   * The type of value the parameter accepts, e.g. &#39;string&#39;, &#39;integer&#39;, &#39;float&#39;, or &#39;boolean&#39;.
   * @return valueType
  **/
  @javax.annotation.Nonnull
  public String getValueType() {
    return valueType;
  }


  public void setValueType(String valueType) {
    this.valueType = valueType;
  }


  public ModelInfoSupportedParameter required(Boolean required) {
    
    this.required = required;
    return this;
  }

   /**
   * Whether the parameter is required (true) or optional (false).
   * @return required
  **/
  @javax.annotation.Nonnull
  public Boolean getRequired() {
    return required;
  }


  public void setRequired(Boolean required) {
    this.required = required;
  }


  public ModelInfoSupportedParameter allowedValues(List<ModelInfoSupportedParameterAllowedValuesInner> allowedValues) {
    
    this.allowedValues = allowedValues;
    return this;
  }

  public ModelInfoSupportedParameter addAllowedValuesItem(ModelInfoSupportedParameterAllowedValuesInner allowedValuesItem) {
    if (this.allowedValues == null) {
      this.allowedValues = new ArrayList<>();
    }
    this.allowedValues.add(allowedValuesItem);
    return this;
  }

   /**
   * The allowed parameter values when the type is &#39;one_of&#39;.
   * @return allowedValues
  **/
  @javax.annotation.Nullable
  public List<ModelInfoSupportedParameterAllowedValuesInner> getAllowedValues() {
    return allowedValues;
  }


  public void setAllowedValues(List<ModelInfoSupportedParameterAllowedValuesInner> allowedValues) {
    this.allowedValues = allowedValues;
  }


  public ModelInfoSupportedParameter min(BigDecimal min) {
    
    this.min = min;
    return this;
  }

   /**
   * The minimum allowed value (inclusive) when the type is &#39;numeric_range&#39;.
   * @return min
  **/
  @javax.annotation.Nullable
  public BigDecimal getMin() {
    return min;
  }


  public void setMin(BigDecimal min) {
    this.min = min;
  }


  public ModelInfoSupportedParameter max(BigDecimal max) {
    
    this.max = max;
    return this;
  }

   /**
   * The maximum allowed value (inclusive) when the type is &#39;numeric_range&#39;.
   * @return max
  **/
  @javax.annotation.Nullable
  public BigDecimal getMax() {
    return max;
  }


  public void setMax(BigDecimal max) {
    this.max = max;
  }


  public ModelInfoSupportedParameter _default(ModelInfoSupportedParameterDefault _default) {
    
    this._default = _default;
    return this;
  }

   /**
   * Get _default
   * @return _default
  **/
  @javax.annotation.Nullable
  public ModelInfoSupportedParameterDefault getDefault() {
    return _default;
  }


  public void setDefault(ModelInfoSupportedParameterDefault _default) {
    this._default = _default;
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
   * @return the ModelInfoSupportedParameter instance itself
   */
  public ModelInfoSupportedParameter putAdditionalProperty(String key, Object value) {
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
    ModelInfoSupportedParameter modelInfoSupportedParameter = (ModelInfoSupportedParameter) o;
    return Objects.equals(this.parameter, modelInfoSupportedParameter.parameter) &&
        Objects.equals(this.type, modelInfoSupportedParameter.type) &&
        Objects.equals(this.valueType, modelInfoSupportedParameter.valueType) &&
        Objects.equals(this.required, modelInfoSupportedParameter.required) &&
        Objects.equals(this.allowedValues, modelInfoSupportedParameter.allowedValues) &&
        Objects.equals(this.min, modelInfoSupportedParameter.min) &&
        Objects.equals(this.max, modelInfoSupportedParameter.max) &&
        Objects.equals(this._default, modelInfoSupportedParameter._default)&&
        Objects.equals(this.additionalProperties, modelInfoSupportedParameter.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(parameter, type, valueType, required, allowedValues, min, max, _default, additionalProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ModelInfoSupportedParameter {\n");
    sb.append("    parameter: ").append(toIndentedString(parameter)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    valueType: ").append(toIndentedString(valueType)).append("\n");
    sb.append("    required: ").append(toIndentedString(required)).append("\n");
    sb.append("    allowedValues: ").append(toIndentedString(allowedValues)).append("\n");
    sb.append("    min: ").append(toIndentedString(min)).append("\n");
    sb.append("    max: ").append(toIndentedString(max)).append("\n");
    sb.append("    _default: ").append(toIndentedString(_default)).append("\n");
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
    openapiFields.add("parameter");
    openapiFields.add("type");
    openapiFields.add("value_type");
    openapiFields.add("required");
    openapiFields.add("allowed_values");
    openapiFields.add("min");
    openapiFields.add("max");
    openapiFields.add("default");

    // a set of required properties/fields (JSON key names)
    openapiRequiredFields = new HashSet<String>();
    openapiRequiredFields.add("parameter");
    openapiRequiredFields.add("type");
    openapiRequiredFields.add("value_type");
    openapiRequiredFields.add("required");
  }

 /**
  * Validates the JSON Element and throws an exception if issues found
  *
  * @param jsonElement JSON Element
  * @throws IOException if the JSON Element is invalid with respect to ModelInfoSupportedParameter
  */
  public static void validateJsonElement(JsonElement jsonElement) throws IOException {
      if (jsonElement == null) {
        if (!ModelInfoSupportedParameter.openapiRequiredFields.isEmpty()) { // has required fields but JSON element is null
          throw new IllegalArgumentException(String.format("The required field(s) %s in ModelInfoSupportedParameter is not found in the empty JSON string", ModelInfoSupportedParameter.openapiRequiredFields.toString()));
        }
      }

      // check to make sure all required properties/fields are present in the JSON string
      for (String requiredField : ModelInfoSupportedParameter.openapiRequiredFields) {
        if (jsonElement.getAsJsonObject().get(requiredField) == null) {
          throw new IllegalArgumentException(String.format("The required field `%s` is not found in the JSON string: %s", requiredField, jsonElement.toString()));
        }
      }
        JsonObject jsonObj = jsonElement.getAsJsonObject();
      if (!jsonObj.get("parameter").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `parameter` to be a primitive type in the JSON string but got `%s`", jsonObj.get("parameter").toString()));
      }
      if (!jsonObj.get("type").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `type` to be a primitive type in the JSON string but got `%s`", jsonObj.get("type").toString()));
      }
      if (!jsonObj.get("value_type").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `value_type` to be a primitive type in the JSON string but got `%s`", jsonObj.get("value_type").toString()));
      }
      if (jsonObj.get("allowed_values") != null && !jsonObj.get("allowed_values").isJsonNull()) {
        JsonArray jsonArrayallowedValues = jsonObj.getAsJsonArray("allowed_values");
        if (jsonArrayallowedValues != null) {
          // ensure the json data is an array
          if (!jsonObj.get("allowed_values").isJsonArray()) {
            throw new IllegalArgumentException(String.format("Expected the field `allowed_values` to be an array in the JSON string but got `%s`", jsonObj.get("allowed_values").toString()));
          }

          // validate the optional field `allowed_values` (array)
          for (int i = 0; i < jsonArrayallowedValues.size(); i++) {
            ModelInfoSupportedParameterAllowedValuesInner.validateJsonElement(jsonArrayallowedValues.get(i));
          };
        }
      }
      // validate the optional field `default`
      if (jsonObj.get("default") != null && !jsonObj.get("default").isJsonNull()) {
        ModelInfoSupportedParameterDefault.validateJsonElement(jsonObj.get("default"));
      }
  }

  public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
       if (!ModelInfoSupportedParameter.class.isAssignableFrom(type.getRawType())) {
         return null; // this class only serializes 'ModelInfoSupportedParameter' and its subtypes
       }
       final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
       final TypeAdapter<ModelInfoSupportedParameter> thisAdapter
                        = gson.getDelegateAdapter(this, TypeToken.get(ModelInfoSupportedParameter.class));

       return (TypeAdapter<T>) new TypeAdapter<ModelInfoSupportedParameter>() {
           @Override
           public void write(JsonWriter out, ModelInfoSupportedParameter value) throws IOException {
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
           public ModelInfoSupportedParameter read(JsonReader in) throws IOException {
             JsonElement jsonElement = elementAdapter.read(in);
             validateJsonElement(jsonElement);
             JsonObject jsonObj = jsonElement.getAsJsonObject();
             // store additional fields in the deserialized instance
             ModelInfoSupportedParameter instance = thisAdapter.fromJsonTree(jsonObj);
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
  * Create an instance of ModelInfoSupportedParameter given an JSON string
  *
  * @param jsonString JSON string
  * @return An instance of ModelInfoSupportedParameter
  * @throws IOException if the JSON string is invalid with respect to ModelInfoSupportedParameter
  */
  public static ModelInfoSupportedParameter fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, ModelInfoSupportedParameter.class);
  }

 /**
  * Convert an instance of ModelInfoSupportedParameter to an JSON string
  *
  * @return JSON string
  */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}

