// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: db_data_2025-04.proto
// Protobuf Java Version: 4.29.3

package io.pinecone.proto;

public interface ListNamespacesRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:ListNamespacesRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * Pagination token to continue a previous listing operation
   * </pre>
   *
   * <code>optional string pagination_token = 1 [json_name = "paginationToken"];</code>
   * @return Whether the paginationToken field is set.
   */
  boolean hasPaginationToken();
  /**
   * <pre>
   * Pagination token to continue a previous listing operation
   * </pre>
   *
   * <code>optional string pagination_token = 1 [json_name = "paginationToken"];</code>
   * @return The paginationToken.
   */
  java.lang.String getPaginationToken();
  /**
   * <pre>
   * Pagination token to continue a previous listing operation
   * </pre>
   *
   * <code>optional string pagination_token = 1 [json_name = "paginationToken"];</code>
   * @return The bytes for paginationToken.
   */
  com.google.protobuf.ByteString
      getPaginationTokenBytes();

  /**
   * <pre>
   * Max number of namespaces to return
   * </pre>
   *
   * <code>optional uint32 limit = 2 [json_name = "limit"];</code>
   * @return Whether the limit field is set.
   */
  boolean hasLimit();
  /**
   * <pre>
   * Max number of namespaces to return
   * </pre>
   *
   * <code>optional uint32 limit = 2 [json_name = "limit"];</code>
   * @return The limit.
   */
  int getLimit();
}
