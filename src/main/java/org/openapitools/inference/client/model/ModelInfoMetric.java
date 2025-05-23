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
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * A distance metric that the embedding model supports for similarity searches.
 */
@JsonAdapter(ModelInfoMetric.Adapter.class)
public enum ModelInfoMetric {
  
  COSINE("cosine"),
  
  EUCLIDEAN("euclidean"),
  
  DOTPRODUCT("dotproduct");

  private String value;

  ModelInfoMetric(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  public static ModelInfoMetric fromValue(String value) {
    for (ModelInfoMetric b : ModelInfoMetric.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }

  public static class Adapter extends TypeAdapter<ModelInfoMetric> {
    @Override
    public void write(final JsonWriter jsonWriter, final ModelInfoMetric enumeration) throws IOException {
      jsonWriter.value(enumeration.getValue());
    }

    @Override
    public ModelInfoMetric read(final JsonReader jsonReader) throws IOException {
      String value = jsonReader.nextString();
      return ModelInfoMetric.fromValue(value);
    }
  }
}

