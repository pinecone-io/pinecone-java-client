// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto/vector_service.proto

package io.pinecone.proto;

public interface DeleteRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:DeleteRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * Vectors to delete.
   * </pre>
   *
   * <code>repeated string ids = 1 [(.grpc.gateway.protoc_gen_openapiv2.options.openapiv2_field) = { ... }</code>
   * @return A list containing the ids.
   */
  java.util.List<java.lang.String>
      getIdsList();
  /**
   * <pre>
   * Vectors to delete.
   * </pre>
   *
   * <code>repeated string ids = 1 [(.grpc.gateway.protoc_gen_openapiv2.options.openapiv2_field) = { ... }</code>
   * @return The count of ids.
   */
  int getIdsCount();
  /**
   * <pre>
   * Vectors to delete.
   * </pre>
   *
   * <code>repeated string ids = 1 [(.grpc.gateway.protoc_gen_openapiv2.options.openapiv2_field) = { ... }</code>
   * @param index The index of the element to return.
   * @return The ids at the given index.
   */
  java.lang.String getIds(int index);
  /**
   * <pre>
   * Vectors to delete.
   * </pre>
   *
   * <code>repeated string ids = 1 [(.grpc.gateway.protoc_gen_openapiv2.options.openapiv2_field) = { ... }</code>
   * @param index The index of the value to return.
   * @return The bytes of the ids at the given index.
   */
  com.google.protobuf.ByteString
      getIdsBytes(int index);

  /**
   * <pre>
   * This indicates that all vectors in the index namespace should be deleted.
   * </pre>
   *
   * <code>bool delete_all = 2 [(.grpc.gateway.protoc_gen_openapiv2.options.openapiv2_field) = { ... }</code>
   * @return The deleteAll.
   */
  boolean getDeleteAll();

  /**
   * <pre>
   * The namespace to delete vectors from, if applicable.
   * </pre>
   *
   * <code>string namespace = 3 [(.grpc.gateway.protoc_gen_openapiv2.options.openapiv2_field) = { ... }</code>
   * @return The namespace.
   */
  java.lang.String getNamespace();
  /**
   * <pre>
   * The namespace to delete vectors from, if applicable.
   * </pre>
   *
   * <code>string namespace = 3 [(.grpc.gateway.protoc_gen_openapiv2.options.openapiv2_field) = { ... }</code>
   * @return The bytes for namespace.
   */
  com.google.protobuf.ByteString
      getNamespaceBytes();

  /**
   * <pre>
   * If specified, the metadata filter here will be used to select the vectors to delete. This is mutually exclusive
   * with specifying ids to delete in the ids param or using delete_all=True.
   * See https://www.pinecone.io/docs/metadata-filtering/.
   * </pre>
   *
   * <code>.google.protobuf.Struct filter = 4;</code>
   * @return Whether the filter field is set.
   */
  boolean hasFilter();
  /**
   * <pre>
   * If specified, the metadata filter here will be used to select the vectors to delete. This is mutually exclusive
   * with specifying ids to delete in the ids param or using delete_all=True.
   * See https://www.pinecone.io/docs/metadata-filtering/.
   * </pre>
   *
   * <code>.google.protobuf.Struct filter = 4;</code>
   * @return The filter.
   */
  com.google.protobuf.Struct getFilter();
  /**
   * <pre>
   * If specified, the metadata filter here will be used to select the vectors to delete. This is mutually exclusive
   * with specifying ids to delete in the ids param or using delete_all=True.
   * See https://www.pinecone.io/docs/metadata-filtering/.
   * </pre>
   *
   * <code>.google.protobuf.Struct filter = 4;</code>
   */
  com.google.protobuf.StructOrBuilder getFilterOrBuilder();
}
