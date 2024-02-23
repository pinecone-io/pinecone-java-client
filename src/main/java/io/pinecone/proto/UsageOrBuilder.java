// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: vector_service.proto

// Protobuf Java Version: 3.25.2
package io.pinecone.proto;

public interface UsageOrBuilder extends
    // @@protoc_insertion_point(interface_extends:Usage)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The number of read units consumed by this operation.
   * </pre>
   *
   * <code>optional uint32 read_units = 1 [(.grpc.gateway.protoc_gen_openapiv2.options.openapiv2_field) = { ... }</code>
   * @return Whether the readUnits field is set.
   */
  boolean hasReadUnits();
  /**
   * <pre>
   * The number of read units consumed by this operation.
   * </pre>
   *
   * <code>optional uint32 read_units = 1 [(.grpc.gateway.protoc_gen_openapiv2.options.openapiv2_field) = { ... }</code>
   * @return The readUnits.
   */
  int getReadUnits();
}
