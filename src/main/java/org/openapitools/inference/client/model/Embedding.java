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
import org.openapitools.inference.client.model.DenseEmbedding;
import org.openapitools.inference.client.model.SparseEmbedding;



import java.io.IOException;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;

import org.openapitools.inference.client.JSON;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2025-05-22T16:07:32.070880Z[Etc/UTC]")
public class Embedding extends AbstractOpenApiSchema {
    private static final Logger log = Logger.getLogger(Embedding.class.getName());

    public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (!Embedding.class.isAssignableFrom(type.getRawType())) {
                return null; // this class only serializes 'Embedding' and its subtypes
            }
            final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
            final TypeAdapter<DenseEmbedding> adapterDenseEmbedding = gson.getDelegateAdapter(this, TypeToken.get(DenseEmbedding.class));
            final TypeAdapter<SparseEmbedding> adapterSparseEmbedding = gson.getDelegateAdapter(this, TypeToken.get(SparseEmbedding.class));

            return (TypeAdapter<T>) new TypeAdapter<Embedding>() {
                @Override
                public void write(JsonWriter out, Embedding value) throws IOException {
                    if (value == null || value.getActualInstance() == null) {
                        elementAdapter.write(out, null);
                        return;
                    }

                    // check if the actual instance is of the type `DenseEmbedding`
                    if (value.getActualInstance() instanceof DenseEmbedding) {
                      JsonElement element = adapterDenseEmbedding.toJsonTree((DenseEmbedding)value.getActualInstance());
                      elementAdapter.write(out, element);
                      return;
                    }
                    // check if the actual instance is of the type `SparseEmbedding`
                    if (value.getActualInstance() instanceof SparseEmbedding) {
                      JsonElement element = adapterSparseEmbedding.toJsonTree((SparseEmbedding)value.getActualInstance());
                      elementAdapter.write(out, element);
                      return;
                    }
                    throw new IOException("Failed to serialize as the type doesn't match oneOf schemas: DenseEmbedding, SparseEmbedding");
                }

                @Override
                public Embedding read(JsonReader in) throws IOException {
                    Object deserialized = null;
                    JsonElement jsonElement = elementAdapter.read(in);

                    int match = 0;
                    ArrayList<String> errorMessages = new ArrayList<>();
                    TypeAdapter actualAdapter = elementAdapter;

                    // deserialize DenseEmbedding
                    try {
                      // validate the JSON object to see if any exception is thrown
                      DenseEmbedding.validateJsonElement(jsonElement);
                      actualAdapter = adapterDenseEmbedding;
                      match++;
                      log.log(Level.FINER, "Input data matches schema 'DenseEmbedding'");
                    } catch (Exception e) {
                      // deserialization failed, continue
                      errorMessages.add(String.format("Deserialization for DenseEmbedding failed with `%s`.", e.getMessage()));
                      log.log(Level.FINER, "Input data does not match schema 'DenseEmbedding'", e);
                    }
                    // deserialize SparseEmbedding
                    try {
                      // validate the JSON object to see if any exception is thrown
                      SparseEmbedding.validateJsonElement(jsonElement);
                      actualAdapter = adapterSparseEmbedding;
                      match++;
                      log.log(Level.FINER, "Input data matches schema 'SparseEmbedding'");
                    } catch (Exception e) {
                      // deserialization failed, continue
                      errorMessages.add(String.format("Deserialization for SparseEmbedding failed with `%s`.", e.getMessage()));
                      log.log(Level.FINER, "Input data does not match schema 'SparseEmbedding'", e);
                    }

                    if (match == 1) {
                        Embedding ret = new Embedding();
                        ret.setActualInstance(actualAdapter.fromJsonTree(jsonElement));
                        return ret;
                    }

                    throw new IOException(String.format("Failed deserialization for Embedding: %d classes match result, expected 1. Detailed failure message for oneOf schemas: %s. JSON: %s", match, errorMessages, jsonElement.toString()));
                }
            }.nullSafe();
        }
    }

    // store a list of schema names defined in oneOf
    public static final Map<String, Class<?>> schemas = new HashMap<String, Class<?>>();

    public Embedding() {
        super("oneOf", Boolean.FALSE);
    }

    public Embedding(DenseEmbedding o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public Embedding(SparseEmbedding o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    static {
        schemas.put("DenseEmbedding", DenseEmbedding.class);
        schemas.put("SparseEmbedding", SparseEmbedding.class);
    }

    @Override
    public Map<String, Class<?>> getSchemas() {
        return Embedding.schemas;
    }

    /**
     * Set the instance that matches the oneOf child schema, check
     * the instance parameter is valid against the oneOf child schemas:
     * DenseEmbedding, SparseEmbedding
     *
     * It could be an instance of the 'oneOf' schemas.
     */
    @Override
    public void setActualInstance(Object instance) {
        if (instance instanceof DenseEmbedding) {
            super.setActualInstance(instance);
            return;
        }

        if (instance instanceof SparseEmbedding) {
            super.setActualInstance(instance);
            return;
        }

        throw new RuntimeException("Invalid instance type. Must be DenseEmbedding, SparseEmbedding");
    }

    /**
     * Get the actual instance, which can be the following:
     * DenseEmbedding, SparseEmbedding
     *
     * @return The actual instance (DenseEmbedding, SparseEmbedding)
     */
    @Override
    public Object getActualInstance() {
        return super.getActualInstance();
    }

    /**
     * Get the actual instance of `DenseEmbedding`. If the actual instance is not `DenseEmbedding`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `DenseEmbedding`
     * @throws ClassCastException if the instance is not `DenseEmbedding`
     */
    public DenseEmbedding getDenseEmbedding() throws ClassCastException {
        return (DenseEmbedding)super.getActualInstance();
    }
    /**
     * Get the actual instance of `SparseEmbedding`. If the actual instance is not `SparseEmbedding`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `SparseEmbedding`
     * @throws ClassCastException if the instance is not `SparseEmbedding`
     */
    public SparseEmbedding getSparseEmbedding() throws ClassCastException {
        return (SparseEmbedding)super.getActualInstance();
    }

 /**
  * Validates the JSON Element and throws an exception if issues found
  *
  * @param jsonElement JSON Element
  * @throws IOException if the JSON Element is invalid with respect to Embedding
  */
  public static void validateJsonElement(JsonElement jsonElement) throws IOException {
    // validate oneOf schemas one by one
    int validCount = 0;
    ArrayList<String> errorMessages = new ArrayList<>();
    // validate the json string with DenseEmbedding
    try {
      DenseEmbedding.validateJsonElement(jsonElement);
      validCount++;
    } catch (Exception e) {
      errorMessages.add(String.format("Deserialization for DenseEmbedding failed with `%s`.", e.getMessage()));
      // continue to the next one
    }
    // validate the json string with SparseEmbedding
    try {
      SparseEmbedding.validateJsonElement(jsonElement);
      validCount++;
    } catch (Exception e) {
      errorMessages.add(String.format("Deserialization for SparseEmbedding failed with `%s`.", e.getMessage()));
      // continue to the next one
    }
    if (validCount != 1) {
      throw new IOException(String.format("The JSON string is invalid for Embedding with oneOf schemas: DenseEmbedding, SparseEmbedding. %d class(es) match the result, expected 1. Detailed failure message for oneOf schemas: %s. JSON: %s", validCount, errorMessages, jsonElement.toString()));
    }
  }

 /**
  * Create an instance of Embedding given an JSON string
  *
  * @param jsonString JSON string
  * @return An instance of Embedding
  * @throws IOException if the JSON string is invalid with respect to Embedding
  */
  public static Embedding fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, Embedding.class);
  }

 /**
  * Convert an instance of Embedding to an JSON string
  *
  * @return JSON string
  */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}

