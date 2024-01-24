/*
 * Pineonce.io Public API
 * Pinecone is a vector database that makes it easy to search and retrieve billions of high-dimensional vectors.
 *
 * The version of the OpenAPI document: 1.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package org.openapitools.client.model;

import java.io.IOException;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Regions available with the GCP cloud provider
 */
@JsonAdapter(GcpRegions.Adapter.class)
public enum GcpRegions {
  
  US_WEST1("us-west1"),
  
  US_WEST2("us-west2"),
  
  EU_WEST4("eu-west4"),
  
  NORTHAMERICA_NORTHEAST1("northamerica-northeast1"),
  
  ASIA_NORTHEAST1("asia-northeast1"),
  
  ASIA_SOUTHEAST1C("asia-southeast1C");

  private String value;

  GcpRegions(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  public static GcpRegions fromValue(String value) {
    for (GcpRegions b : GcpRegions.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }

  public static class Adapter extends TypeAdapter<GcpRegions> {
    @Override
    public void write(final JsonWriter jsonWriter, final GcpRegions enumeration) throws IOException {
      jsonWriter.value(enumeration.getValue());
    }

    @Override
    public GcpRegions read(final JsonReader jsonReader) throws IOException {
      String value = jsonReader.nextString();
      return GcpRegions.fromValue(value);
    }
  }
}

