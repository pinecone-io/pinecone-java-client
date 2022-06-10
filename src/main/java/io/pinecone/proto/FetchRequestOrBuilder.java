// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: vector_service.proto

package io.pinecone.proto;

public interface FetchRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:FetchRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The vector ids to fetch. Does not accept values containing spaces.
   * </pre>
   *
   * <code>repeated string ids = 1 [(.grpc.gateway.protoc_gen_openapiv2.options.openapiv2_field) = { ... }</code>
   * @return A list containing the ids.
   */
  java.util.List<java.lang.String>
      getIdsList();
  /**
   * <pre>
   * The vector ids to fetch. Does not accept values containing spaces.
   * </pre>
   *
   * <code>repeated string ids = 1 [(.grpc.gateway.protoc_gen_openapiv2.options.openapiv2_field) = { ... }</code>
   * @return The count of ids.
   */
  int getIdsCount();
  /**
   * <pre>
   * The vector ids to fetch. Does not accept values containing spaces.
   * </pre>
   *
   * <code>repeated string ids = 1 [(.grpc.gateway.protoc_gen_openapiv2.options.openapiv2_field) = { ... }</code>
   * @param index The index of the element to return.
   * @return The ids at the given index.
   */
  java.lang.String getIds(int index);
  /**
   * <pre>
   * The vector ids to fetch. Does not accept values containing spaces.
   * </pre>
   *
   * <code>repeated string ids = 1 [(.grpc.gateway.protoc_gen_openapiv2.options.openapiv2_field) = { ... }</code>
   * @param index The index of the value to return.
   * @return The bytes of the ids at the given index.
   */
  com.google.protobuf.ByteString
      getIdsBytes(int index);

  /**
   * <code>string namespace = 2 [(.grpc.gateway.protoc_gen_openapiv2.options.openapiv2_field) = { ... }</code>
   * @return The namespace.
   */
  java.lang.String getNamespace();
  /**
   * <code>string namespace = 2 [(.grpc.gateway.protoc_gen_openapiv2.options.openapiv2_field) = { ... }</code>
   * @return The bytes for namespace.
   */
  com.google.protobuf.ByteString
      getNamespaceBytes();
}
