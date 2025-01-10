/*
 * Pinecone Control Plane API
 * Pinecone is a vector database that makes it easy to search and retrieve billions of high-dimensional vectors.
 *
 * The version of the OpenAPI document: 2025-01
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
import org.openapitools.db_control.client.model.CreateIndexForModelRequestEmbed;
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
 * The desired configuration for the index and associated embedding model.
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2025-01-10T18:59:18.406889Z[Etc/UTC]")
public class CreateIndexForModelRequest {
  public static final String SERIALIZED_NAME_NAME = "name";
  @SerializedName(SERIALIZED_NAME_NAME)
  private String name;

  /**
   * The public cloud where you would like your index hosted.
   */
  @JsonAdapter(CloudEnum.Adapter.class)
  public enum CloudEnum {
    GCP("gcp"),
    
    AWS("aws"),
    
    AZURE("azure");

    private String value;

    CloudEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static CloudEnum fromValue(String value) {
      for (CloudEnum b : CloudEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

    public static class Adapter extends TypeAdapter<CloudEnum> {
      @Override
      public void write(final JsonWriter jsonWriter, final CloudEnum enumeration) throws IOException {
        jsonWriter.value(enumeration.getValue());
      }

      @Override
      public CloudEnum read(final JsonReader jsonReader) throws IOException {
        String value =  jsonReader.nextString();
        return CloudEnum.fromValue(value);
      }
    }
  }

  public static final String SERIALIZED_NAME_CLOUD = "cloud";
  @SerializedName(SERIALIZED_NAME_CLOUD)
  private CloudEnum cloud;

  public static final String SERIALIZED_NAME_REGION = "region";
  @SerializedName(SERIALIZED_NAME_REGION)
  private String region;

  public static final String SERIALIZED_NAME_DELETION_PROTECTION = "deletion_protection";
  @SerializedName(SERIALIZED_NAME_DELETION_PROTECTION)
  private DeletionProtection deletionProtection = DeletionProtection.DISABLED;

  public static final String SERIALIZED_NAME_TAGS = "tags";
  @SerializedName(SERIALIZED_NAME_TAGS)
  private Map<String, String> tags = new HashMap<>();

  public static final String SERIALIZED_NAME_EMBED = "embed";
  @SerializedName(SERIALIZED_NAME_EMBED)
  private CreateIndexForModelRequestEmbed embed;

  public CreateIndexForModelRequest() {
  }

  public CreateIndexForModelRequest name(String name) {
    
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


  public CreateIndexForModelRequest cloud(CloudEnum cloud) {
    
    this.cloud = cloud;
    return this;
  }

   /**
   * The public cloud where you would like your index hosted.
   * @return cloud
  **/
  @javax.annotation.Nonnull
  public CloudEnum getCloud() {
    return cloud;
  }


  public void setCloud(CloudEnum cloud) {
    this.cloud = cloud;
  }


  public CreateIndexForModelRequest region(String region) {
    
    this.region = region;
    return this;
  }

   /**
   * The region where you would like your index to be created.
   * @return region
  **/
  @javax.annotation.Nonnull
  public String getRegion() {
    return region;
  }


  public void setRegion(String region) {
    this.region = region;
  }


  public CreateIndexForModelRequest deletionProtection(DeletionProtection deletionProtection) {
    
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


  public CreateIndexForModelRequest tags(Map<String, String> tags) {
    
    this.tags = tags;
    return this;
  }

  public CreateIndexForModelRequest putTagsItem(String key, String tagsItem) {
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


  public CreateIndexForModelRequest embed(CreateIndexForModelRequestEmbed embed) {
    
    this.embed = embed;
    return this;
  }

   /**
   * Get embed
   * @return embed
  **/
  @javax.annotation.Nonnull
  public CreateIndexForModelRequestEmbed getEmbed() {
    return embed;
  }


  public void setEmbed(CreateIndexForModelRequestEmbed embed) {
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
   * @return the CreateIndexForModelRequest instance itself
   */
  public CreateIndexForModelRequest putAdditionalProperty(String key, Object value) {
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
    CreateIndexForModelRequest createIndexForModelRequest = (CreateIndexForModelRequest) o;
    return Objects.equals(this.name, createIndexForModelRequest.name) &&
        Objects.equals(this.cloud, createIndexForModelRequest.cloud) &&
        Objects.equals(this.region, createIndexForModelRequest.region) &&
        Objects.equals(this.deletionProtection, createIndexForModelRequest.deletionProtection) &&
        Objects.equals(this.tags, createIndexForModelRequest.tags) &&
        Objects.equals(this.embed, createIndexForModelRequest.embed)&&
        Objects.equals(this.additionalProperties, createIndexForModelRequest.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, cloud, region, deletionProtection, tags, embed, additionalProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateIndexForModelRequest {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    cloud: ").append(toIndentedString(cloud)).append("\n");
    sb.append("    region: ").append(toIndentedString(region)).append("\n");
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
    openapiFields.add("name");
    openapiFields.add("cloud");
    openapiFields.add("region");
    openapiFields.add("deletion_protection");
    openapiFields.add("tags");
    openapiFields.add("embed");

    // a set of required properties/fields (JSON key names)
    openapiRequiredFields = new HashSet<String>();
    openapiRequiredFields.add("name");
    openapiRequiredFields.add("cloud");
    openapiRequiredFields.add("region");
    openapiRequiredFields.add("embed");
  }

 /**
  * Validates the JSON Element and throws an exception if issues found
  *
  * @param jsonElement JSON Element
  * @throws IOException if the JSON Element is invalid with respect to CreateIndexForModelRequest
  */
  public static void validateJsonElement(JsonElement jsonElement) throws IOException {
      if (jsonElement == null) {
        if (!CreateIndexForModelRequest.openapiRequiredFields.isEmpty()) { // has required fields but JSON element is null
          throw new IllegalArgumentException(String.format("The required field(s) %s in CreateIndexForModelRequest is not found in the empty JSON string", CreateIndexForModelRequest.openapiRequiredFields.toString()));
        }
      }

      // check to make sure all required properties/fields are present in the JSON string
      for (String requiredField : CreateIndexForModelRequest.openapiRequiredFields) {
        if (jsonElement.getAsJsonObject().get(requiredField) == null) {
          throw new IllegalArgumentException(String.format("The required field `%s` is not found in the JSON string: %s", requiredField, jsonElement.toString()));
        }
      }
        JsonObject jsonObj = jsonElement.getAsJsonObject();
      if (!jsonObj.get("name").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `name` to be a primitive type in the JSON string but got `%s`", jsonObj.get("name").toString()));
      }
      if (!jsonObj.get("cloud").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `cloud` to be a primitive type in the JSON string but got `%s`", jsonObj.get("cloud").toString()));
      }
      if (!jsonObj.get("region").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `region` to be a primitive type in the JSON string but got `%s`", jsonObj.get("region").toString()));
      }
      // validate the required field `embed`
      CreateIndexForModelRequestEmbed.validateJsonElement(jsonObj.get("embed"));
  }

  public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
       if (!CreateIndexForModelRequest.class.isAssignableFrom(type.getRawType())) {
         return null; // this class only serializes 'CreateIndexForModelRequest' and its subtypes
       }
       final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
       final TypeAdapter<CreateIndexForModelRequest> thisAdapter
                        = gson.getDelegateAdapter(this, TypeToken.get(CreateIndexForModelRequest.class));

       return (TypeAdapter<T>) new TypeAdapter<CreateIndexForModelRequest>() {
           @Override
           public void write(JsonWriter out, CreateIndexForModelRequest value) throws IOException {
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
           public CreateIndexForModelRequest read(JsonReader in) throws IOException {
             JsonElement jsonElement = elementAdapter.read(in);
             validateJsonElement(jsonElement);
             JsonObject jsonObj = jsonElement.getAsJsonObject();
             // store additional fields in the deserialized instance
             CreateIndexForModelRequest instance = thisAdapter.fromJsonTree(jsonObj);
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
  * Create an instance of CreateIndexForModelRequest given an JSON string
  *
  * @param jsonString JSON string
  * @return An instance of CreateIndexForModelRequest
  * @throws IOException if the JSON string is invalid with respect to CreateIndexForModelRequest
  */
  public static CreateIndexForModelRequest fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, CreateIndexForModelRequest.class);
  }

 /**
  * Convert an instance of CreateIndexForModelRequest to an JSON string
  *
  * @return JSON string
  */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}

