// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: db_data_2025-04.proto
// Protobuf Java Version: 4.29.3

package io.pinecone.proto;

public interface NamespaceSummaryOrBuilder extends
    // @@protoc_insertion_point(interface_extends:NamespaceSummary)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The number of vectors stored in this namespace. Note that updates to this field may lag behind updates to the
   * underlying index and corresponding query results, etc.
   * </pre>
   *
   * <code>uint32 vector_count = 1 [json_name = "vectorCount"];</code>
   * @return The vectorCount.
   */
  int getVectorCount();
}
