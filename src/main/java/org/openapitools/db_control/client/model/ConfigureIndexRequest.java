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
import org.openapitools.db_control.client.model.ConfigureIndexRequestEmbed;
import org.openapitools.db_control.client.model.ConfigureIndexRequestSpec;
import org.openapitools.db_control.client.model.DeletionProtection;

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
 * Configuration used to scale an index.
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2025-05-22T16:07:13.211110Z[Etc/UTC]")
public class ConfigureIndexRequest {
  public static final String SERIALIZED_NAME_SPEC = "spec";
  @SerializedName(SERIALIZED_NAME_SPEC)
  private ConfigureIndexRequestSpec spec;

  public static final String SERIALIZED_NAME_DELETION_PROTECTION = "deletion_protection";
  @SerializedName(SERIALIZED_NAME_DELETION_PROTECTION)
  private DeletionProtection deletionProtection = DeletionProtection.DISABLED;

  public static final String SERIALIZED_NAME_TAGS = "tags";
  @SerializedName(SERIALIZED_NAME_TAGS)
  private Map<String, String> tags = new HashMap<>();

  public static final String SERIALIZED_NAME_EMBED = "embed";
  @SerializedName(SERIALIZED_NAME_EMBED)
  private ConfigureIndexRequestEmbed embed;

  public ConfigureIndexRequest() {
  }

  public ConfigureIndexRequest spec(ConfigureIndexRequestSpec spec) {
    
    this.spec = spec;
    return this;
  }

   /**
   * Get spec
   * @return spec
  **/
  @javax.annotation.Nullable
  public ConfigureIndexRequestSpec getSpec() {
    return spec;
  }


  public void setSpec(ConfigureIndexRequestSpec spec) {
    this.spec = spec;
  }


  public ConfigureIndexRequest deletionProtection(DeletionProtection deletionProtection) {
    
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


  public ConfigureIndexRequest tags(Map<String, String> tags) {
    
    this.tags = tags;
    return this;
  }

  public ConfigureIndexRequest putTagsItem(String key, String tagsItem) {
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


  public ConfigureIndexRequest embed(ConfigureIndexRequestEmbed embed) {
    
    this.embed = embed;
    return this;
  }

   /**
   * Get embed
   * @return embed
  **/
  @javax.annotation.Nullable
  public ConfigureIndexRequestEmbed getEmbed() {
    return embed;
  }


  public void setEmbed(ConfigureIndexRequestEmbed embed) {
    this.embed = embed;
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
   * @return the ConfigureIndexRequest instance itself
   */
  public ConfigureIndexRequest putAdditionalProperty(String key, Object value) {
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
    ConfigureIndexRequest configureIndexRequest = (ConfigureIndexRequest) o;
    return Objects.equals(this.spec, configureIndexRequest.spec) &&
        Objects.equals(this.deletionProtection, configureIndexRequest.deletionProtection) &&
        Objects.equals(this.tags, configureIndexRequest.tags) &&
        Objects.equals(this.embed, configureIndexRequest.embed)&&
        Objects.equals(this.additionalProperties, configureIndexRequest.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(spec, deletionProtection, tags, embed, additionalProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConfigureIndexRequest {\n");
    sb.append("    spec: ").append(toIndentedString(spec)).append("\n");
    sb.append("    deletionProtection: ").append(toIndentedString(deletionProtection)).append("\n");
    sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
    sb.append("    embed: ").append(toIndentedString(embed)).append("\n");
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
    openapiFields.add("spec");
    openapiFields.add("deletion_protection");
    openapiFields.add("tags");
    openapiFields.add("embed");

    // a set of required properties/fields (JSON key names)
    openapiRequiredFields = new HashSet<String>();
  }

 /**
  * Validates the JSON Element and throws an exception if issues found
  *
  * @param jsonElement JSON Element
  * @throws IOException if the JSON Element is invalid with respect to ConfigureIndexRequest
  */
  public static void validateJsonElement(JsonElement jsonElement) throws IOException {
      if (jsonElement == null) {
        if (!ConfigureIndexRequest.openapiRequiredFields.isEmpty()) { // has required fields but JSON element is null
          throw new IllegalArgumentException(String.format("The required field(s) %s in ConfigureIndexRequest is not found in the empty JSON string", ConfigureIndexRequest.openapiRequiredFields.toString()));
        }
      }
        JsonObject jsonObj = jsonElement.getAsJsonObject();
      // validate the optional field `spec`
      if (jsonObj.get("spec") != null && !jsonObj.get("spec").isJsonNull()) {
        ConfigureIndexRequestSpec.validateJsonElement(jsonObj.get("spec"));
      }
      // validate the optional field `embed`
      if (jsonObj.get("embed") != null && !jsonObj.get("embed").isJsonNull()) {
        ConfigureIndexRequestEmbed.validateJsonElement(jsonObj.get("embed"));
      }
  }

  public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
       if (!ConfigureIndexRequest.class.isAssignableFrom(type.getRawType())) {
         return null; // this class only serializes 'ConfigureIndexRequest' and its subtypes
       }
       final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
       final TypeAdapter<ConfigureIndexRequest> thisAdapter
                        = gson.getDelegateAdapter(this, TypeToken.get(ConfigureIndexRequest.class));

       return (TypeAdapter<T>) new TypeAdapter<ConfigureIndexRequest>() {
           @Override
           public void write(JsonWriter out, ConfigureIndexRequest value) throws IOException {
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
           public ConfigureIndexRequest read(JsonReader in) throws IOException {
             JsonElement jsonElement = elementAdapter.read(in);
             validateJsonElement(jsonElement);
             JsonObject jsonObj = jsonElement.getAsJsonObject();
             // store additional fields in the deserialized instance
             ConfigureIndexRequest instance = thisAdapter.fromJsonTree(jsonObj);
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
  * Create an instance of ConfigureIndexRequest given an JSON string
  *
  * @param jsonString JSON string
  * @return An instance of ConfigureIndexRequest
  * @throws IOException if the JSON string is invalid with respect to ConfigureIndexRequest
  */
  public static ConfigureIndexRequest fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, ConfigureIndexRequest.class);
  }

 /**
  * Convert an instance of ConfigureIndexRequest to an JSON string
  *
  * @return JSON string
  */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}

