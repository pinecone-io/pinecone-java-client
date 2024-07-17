/*
 * Pinecone Control Plane API
 * Pinecone is a vector database that makes it easy to search and retrieve billions of high-dimensional vectors.
 *
 * The version of the OpenAPI document: 2024-07
 * Contact: support@pinecone.io
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package org.openapitools.control.client.model;

import java.util.Objects;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Whether delete protection is enabled/disabled for the resource. 
 */
@JsonAdapter(DeletionProtection.Adapter.class)
public enum DeletionProtection {
  
  DISABLED("disabled"),
  
  ENABLED("enabled");

  private String value;

  DeletionProtection(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  public static DeletionProtection fromValue(String value) {
    for (DeletionProtection b : DeletionProtection.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }

  public static class Adapter extends TypeAdapter<DeletionProtection> {
    @Override
    public void write(final JsonWriter jsonWriter, final DeletionProtection enumeration) throws IOException {
      jsonWriter.value(enumeration.getValue());
    }

    @Override
    public DeletionProtection read(final JsonReader jsonReader) throws IOException {
      String value = jsonReader.nextString();
      return DeletionProtection.fromValue(value);
    }
  }
}
