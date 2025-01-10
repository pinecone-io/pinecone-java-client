/*
 * Pinecone Inference API
 * Pinecone is a vector database that makes it easy to search and retrieve billions of high-dimensional vectors.
 *
 * The version of the OpenAPI document: 2025-01
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * RerankRequest
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2025-01-10T18:59:22.280429Z[Etc/UTC]")
public class RerankRequest {
  public static final String SERIALIZED_NAME_MODEL = "model";
  @SerializedName(SERIALIZED_NAME_MODEL)
  private String model;

  public static final String SERIALIZED_NAME_QUERY = "query";
  @SerializedName(SERIALIZED_NAME_QUERY)
  private String query;

  public static final String SERIALIZED_NAME_TOP_N = "top_n";
  @SerializedName(SERIALIZED_NAME_TOP_N)
  private Integer topN;

  public static final String SERIALIZED_NAME_RETURN_DOCUMENTS = "return_documents";
  @SerializedName(SERIALIZED_NAME_RETURN_DOCUMENTS)
  private Boolean returnDocuments = true;

  public static final String SERIALIZED_NAME_RANK_FIELDS = "rank_fields";
  @SerializedName(SERIALIZED_NAME_RANK_FIELDS)
  private List<String> rankFields = new ArrayList<>(Arrays.asList("text"));

  public static final String SERIALIZED_NAME_DOCUMENTS = "documents";
  @SerializedName(SERIALIZED_NAME_DOCUMENTS)
  private List<Map<String, Object>> documents = new ArrayList<>();

  public static final String SERIALIZED_NAME_PARAMETERS = "parameters";
  @SerializedName(SERIALIZED_NAME_PARAMETERS)
  private Map<String, Object> parameters = new HashMap<>();

  public RerankRequest() {
  }

  public RerankRequest model(String model) {
    
    this.model = model;
    return this;
  }

   /**
   * The [model](https://docs.pinecone.io/guides/inference/understanding-inference#reranking-models) to use for reranking.
   * @return model
  **/
  @javax.annotation.Nonnull
  public String getModel() {
    return model;
  }


  public void setModel(String model) {
    this.model = model;
  }


  public RerankRequest query(String query) {
    
    this.query = query;
    return this;
  }

   /**
   * The query to rerank documents against.
   * @return query
  **/
  @javax.annotation.Nonnull
  public String getQuery() {
    return query;
  }


  public void setQuery(String query) {
    this.query = query;
  }


  public RerankRequest topN(Integer topN) {
    
    this.topN = topN;
    return this;
  }

   /**
   * The number of results to return sorted by relevance. Defaults to the number of inputs.
   * @return topN
  **/
  @javax.annotation.Nullable
  public Integer getTopN() {
    return topN;
  }


  public void setTopN(Integer topN) {
    this.topN = topN;
  }


  public RerankRequest returnDocuments(Boolean returnDocuments) {
    
    this.returnDocuments = returnDocuments;
    return this;
  }

   /**
   * Whether to return the documents in the response.
   * @return returnDocuments
  **/
  @javax.annotation.Nullable
  public Boolean getReturnDocuments() {
    return returnDocuments;
  }


  public void setReturnDocuments(Boolean returnDocuments) {
    this.returnDocuments = returnDocuments;
  }


  public RerankRequest rankFields(List<String> rankFields) {
    
    this.rankFields = rankFields;
    return this;
  }

  public RerankRequest addRankFieldsItem(String rankFieldsItem) {
    if (this.rankFields == null) {
      this.rankFields = new ArrayList<>(Arrays.asList("text"));
    }
    this.rankFields.add(rankFieldsItem);
    return this;
  }

   /**
   * The fields to rank the documents by. If not provided, the default is &#x60;\&quot;text\&quot;&#x60;.
   * @return rankFields
  **/
  @javax.annotation.Nullable
  public List<String> getRankFields() {
    return rankFields;
  }


  public void setRankFields(List<String> rankFields) {
    this.rankFields = rankFields;
  }


  public RerankRequest documents(List<Map<String, Object>> documents) {
    
    this.documents = documents;
    return this;
  }

  public RerankRequest addDocumentsItem(Map<String, Object> documentsItem) {
    if (this.documents == null) {
      this.documents = new ArrayList<>();
    }
    this.documents.add(documentsItem);
    return this;
  }

   /**
   * The documents to rerank.
   * @return documents
  **/
  @javax.annotation.Nonnull
  public List<Map<String, Object>> getDocuments() {
    return documents;
  }


  public void setDocuments(List<Map<String, Object>> documents) {
    this.documents = documents;
  }


  public RerankRequest parameters(Map<String, Object> parameters) {
    
    this.parameters = parameters;
    return this;
  }

  public RerankRequest putParametersItem(String key, Object parametersItem) {
    if (this.parameters == null) {
      this.parameters = new HashMap<>();
    }
    this.parameters.put(key, parametersItem);
    return this;
  }

   /**
   * Additional model-specific parameters. Refer to the [model guide](https://docs.pinecone.io/guides/inference/understanding-inference#reranking-models) for available model parameters.
   * @return parameters
  **/
  @javax.annotation.Nullable
  public Map<String, Object> getParameters() {
    return parameters;
  }


  public void setParameters(Map<String, Object> parameters) {
    this.parameters = parameters;
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
   * @return the RerankRequest instance itself
   */
  public RerankRequest putAdditionalProperty(String key, Object value) {
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
    RerankRequest rerankRequest = (RerankRequest) o;
    return Objects.equals(this.model, rerankRequest.model) &&
        Objects.equals(this.query, rerankRequest.query) &&
        Objects.equals(this.topN, rerankRequest.topN) &&
        Objects.equals(this.returnDocuments, rerankRequest.returnDocuments) &&
        Objects.equals(this.rankFields, rerankRequest.rankFields) &&
        Objects.equals(this.documents, rerankRequest.documents) &&
        Objects.equals(this.parameters, rerankRequest.parameters)&&
        Objects.equals(this.additionalProperties, rerankRequest.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(model, query, topN, returnDocuments, rankFields, documents, parameters, additionalProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RerankRequest {\n");
    sb.append("    model: ").append(toIndentedString(model)).append("\n");
    sb.append("    query: ").append(toIndentedString(query)).append("\n");
    sb.append("    topN: ").append(toIndentedString(topN)).append("\n");
    sb.append("    returnDocuments: ").append(toIndentedString(returnDocuments)).append("\n");
    sb.append("    rankFields: ").append(toIndentedString(rankFields)).append("\n");
    sb.append("    documents: ").append(toIndentedString(documents)).append("\n");
    sb.append("    parameters: ").append(toIndentedString(parameters)).append("\n");
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
    openapiFields.add("query");
    openapiFields.add("top_n");
    openapiFields.add("return_documents");
    openapiFields.add("rank_fields");
    openapiFields.add("documents");
    openapiFields.add("parameters");

    // a set of required properties/fields (JSON key names)
    openapiRequiredFields = new HashSet<String>();
    openapiRequiredFields.add("model");
    openapiRequiredFields.add("query");
    openapiRequiredFields.add("documents");
  }

 /**
  * Validates the JSON Element and throws an exception if issues found
  *
  * @param jsonElement JSON Element
  * @throws IOException if the JSON Element is invalid with respect to RerankRequest
  */
  public static void validateJsonElement(JsonElement jsonElement) throws IOException {
      if (jsonElement == null) {
        if (!RerankRequest.openapiRequiredFields.isEmpty()) { // has required fields but JSON element is null
          throw new IllegalArgumentException(String.format("The required field(s) %s in RerankRequest is not found in the empty JSON string", RerankRequest.openapiRequiredFields.toString()));
        }
      }

      // check to make sure all required properties/fields are present in the JSON string
      for (String requiredField : RerankRequest.openapiRequiredFields) {
        if (jsonElement.getAsJsonObject().get(requiredField) == null) {
          throw new IllegalArgumentException(String.format("The required field `%s` is not found in the JSON string: %s", requiredField, jsonElement.toString()));
        }
      }
        JsonObject jsonObj = jsonElement.getAsJsonObject();
      if (!jsonObj.get("model").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `model` to be a primitive type in the JSON string but got `%s`", jsonObj.get("model").toString()));
      }
      if (!jsonObj.get("query").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `query` to be a primitive type in the JSON string but got `%s`", jsonObj.get("query").toString()));
      }
      // ensure the optional json data is an array if present
      if (jsonObj.get("rank_fields") != null && !jsonObj.get("rank_fields").isJsonNull() && !jsonObj.get("rank_fields").isJsonArray()) {
        throw new IllegalArgumentException(String.format("Expected the field `rank_fields` to be an array in the JSON string but got `%s`", jsonObj.get("rank_fields").toString()));
      }
      // ensure the required json array is present
      if (jsonObj.get("documents") == null) {
        throw new IllegalArgumentException("Expected the field `linkedContent` to be an array in the JSON string but got `null`");
      } else if (!jsonObj.get("documents").isJsonArray()) {
        throw new IllegalArgumentException(String.format("Expected the field `documents` to be an array in the JSON string but got `%s`", jsonObj.get("documents").toString()));
      }
  }

  public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
       if (!RerankRequest.class.isAssignableFrom(type.getRawType())) {
         return null; // this class only serializes 'RerankRequest' and its subtypes
       }
       final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
       final TypeAdapter<RerankRequest> thisAdapter
                        = gson.getDelegateAdapter(this, TypeToken.get(RerankRequest.class));

       return (TypeAdapter<T>) new TypeAdapter<RerankRequest>() {
           @Override
           public void write(JsonWriter out, RerankRequest value) throws IOException {
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
           public RerankRequest read(JsonReader in) throws IOException {
             JsonElement jsonElement = elementAdapter.read(in);
             validateJsonElement(jsonElement);
             JsonObject jsonObj = jsonElement.getAsJsonObject();
             // store additional fields in the deserialized instance
             RerankRequest instance = thisAdapter.fromJsonTree(jsonObj);
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
  * Create an instance of RerankRequest given an JSON string
  *
  * @param jsonString JSON string
  * @return An instance of RerankRequest
  * @throws IOException if the JSON string is invalid with respect to RerankRequest
  */
  public static RerankRequest fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, RerankRequest.class);
  }

 /**
  * Convert an instance of RerankRequest to an JSON string
  *
  * @return JSON string
  */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}

