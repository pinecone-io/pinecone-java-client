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
import org.openapitools.db_control.client.model.ByocSpec;
import org.openapitools.db_control.client.model.PodSpec;
import org.openapitools.db_control.client.model.ServerlessSpec;

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
 * IndexModelSpec
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2025-04-09T20:04:12.828903Z[Etc/UTC]")
public class IndexModelSpec {
  public static final String SERIALIZED_NAME_BYOC = "byoc";
  @SerializedName(SERIALIZED_NAME_BYOC)
  private ByocSpec byoc;

  public static final String SERIALIZED_NAME_POD = "pod";
  @SerializedName(SERIALIZED_NAME_POD)
  private PodSpec pod;

  public static final String SERIALIZED_NAME_SERVERLESS = "serverless";
  @SerializedName(SERIALIZED_NAME_SERVERLESS)
  private ServerlessSpec serverless;

  public IndexModelSpec() {
  }

  public IndexModelSpec byoc(ByocSpec byoc) {
    
    this.byoc = byoc;
    return this;
  }

   /**
   * Get byoc
   * @return byoc
  **/
  @javax.annotation.Nullable
  public ByocSpec getByoc() {
    return byoc;
  }


  public void setByoc(ByocSpec byoc) {
    this.byoc = byoc;
  }


  public IndexModelSpec pod(PodSpec pod) {
    
    this.pod = pod;
    return this;
  }

   /**
   * Get pod
   * @return pod
  **/
  @javax.annotation.Nullable
  public PodSpec getPod() {
    return pod;
  }


  public void setPod(PodSpec pod) {
    this.pod = pod;
  }


  public IndexModelSpec serverless(ServerlessSpec serverless) {
    
    this.serverless = serverless;
    return this;
  }

   /**
   * Get serverless
   * @return serverless
  **/
  @javax.annotation.Nullable
  public ServerlessSpec getServerless() {
    return serverless;
  }


  public void setServerless(ServerlessSpec serverless) {
    this.serverless = serverless;
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
   * @return the IndexModelSpec instance itself
   */
  public IndexModelSpec putAdditionalProperty(String key, Object value) {
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
    IndexModelSpec indexModelSpec = (IndexModelSpec) o;
    return Objects.equals(this.byoc, indexModelSpec.byoc) &&
        Objects.equals(this.pod, indexModelSpec.pod) &&
        Objects.equals(this.serverless, indexModelSpec.serverless)&&
        Objects.equals(this.additionalProperties, indexModelSpec.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(byoc, pod, serverless, additionalProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IndexModelSpec {\n");
    sb.append("    byoc: ").append(toIndentedString(byoc)).append("\n");
    sb.append("    pod: ").append(toIndentedString(pod)).append("\n");
    sb.append("    serverless: ").append(toIndentedString(serverless)).append("\n");
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
    openapiFields.add("byoc");
    openapiFields.add("pod");
    openapiFields.add("serverless");

    // a set of required properties/fields (JSON key names)
    openapiRequiredFields = new HashSet<String>();
  }

 /**
  * Validates the JSON Element and throws an exception if issues found
  *
  * @param jsonElement JSON Element
  * @throws IOException if the JSON Element is invalid with respect to IndexModelSpec
  */
  public static void validateJsonElement(JsonElement jsonElement) throws IOException {
      if (jsonElement == null) {
        if (!IndexModelSpec.openapiRequiredFields.isEmpty()) { // has required fields but JSON element is null
          throw new IllegalArgumentException(String.format("The required field(s) %s in IndexModelSpec is not found in the empty JSON string", IndexModelSpec.openapiRequiredFields.toString()));
        }
      }
        JsonObject jsonObj = jsonElement.getAsJsonObject();
      // validate the optional field `byoc`
      if (jsonObj.get("byoc") != null && !jsonObj.get("byoc").isJsonNull()) {
        ByocSpec.validateJsonElement(jsonObj.get("byoc"));
      }
      // validate the optional field `pod`
      if (jsonObj.get("pod") != null && !jsonObj.get("pod").isJsonNull()) {
        PodSpec.validateJsonElement(jsonObj.get("pod"));
      }
      // validate the optional field `serverless`
      if (jsonObj.get("serverless") != null && !jsonObj.get("serverless").isJsonNull()) {
        ServerlessSpec.validateJsonElement(jsonObj.get("serverless"));
      }
  }

  public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
       if (!IndexModelSpec.class.isAssignableFrom(type.getRawType())) {
         return null; // this class only serializes 'IndexModelSpec' and its subtypes
       }
       final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
       final TypeAdapter<IndexModelSpec> thisAdapter
                        = gson.getDelegateAdapter(this, TypeToken.get(IndexModelSpec.class));

       return (TypeAdapter<T>) new TypeAdapter<IndexModelSpec>() {
           @Override
           public void write(JsonWriter out, IndexModelSpec value) throws IOException {
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
           public IndexModelSpec read(JsonReader in) throws IOException {
             JsonElement jsonElement = elementAdapter.read(in);
             validateJsonElement(jsonElement);
             JsonObject jsonObj = jsonElement.getAsJsonObject();
             // store additional fields in the deserialized instance
             IndexModelSpec instance = thisAdapter.fromJsonTree(jsonObj);
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
  * Create an instance of IndexModelSpec given an JSON string
  *
  * @param jsonString JSON string
  * @return An instance of IndexModelSpec
  * @throws IOException if the JSON string is invalid with respect to IndexModelSpec
  */
  public static IndexModelSpec fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, IndexModelSpec.class);
  }

 /**
  * Convert an instance of IndexModelSpec to an JSON string
  *
  * @return JSON string
  */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}

