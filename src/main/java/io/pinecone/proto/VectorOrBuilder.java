// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: db_data_2025-01.proto
// Protobuf Java Version: 4.29.3

package io.pinecone.proto;

public interface VectorOrBuilder extends
    // @@protoc_insertion_point(interface_extends:Vector)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * This is the vector's unique id.
   * </pre>
   *
   * <code>string id = 1 [json_name = "id", (.google.api.field_behavior) = REQUIRED];</code>
   * @return The id.
   */
  java.lang.String getId();
  /**
   * <pre>
   * This is the vector's unique id.
   * </pre>
   *
   * <code>string id = 1 [json_name = "id", (.google.api.field_behavior) = REQUIRED];</code>
   * @return The bytes for id.
   */
  com.google.protobuf.ByteString
      getIdBytes();

  /**
   * <pre>
   * This is the vector data included in the request.
   * </pre>
   *
   * <code>repeated float values = 2 [json_name = "values"];</code>
   * @return A list containing the values.
   */
  java.util.List<java.lang.Float> getValuesList();
  /**
   * <pre>
   * This is the vector data included in the request.
   * </pre>
   *
   * <code>repeated float values = 2 [json_name = "values"];</code>
   * @return The count of values.
   */
  int getValuesCount();
  /**
   * <pre>
   * This is the vector data included in the request.
   * </pre>
   *
   * <code>repeated float values = 2 [json_name = "values"];</code>
   * @param index The index of the element to return.
   * @return The values at the given index.
   */
  float getValues(int index);

  /**
   * <pre>
   * This is the sparse data included in the request. Can only be specified if `sparse` index.
   * </pre>
   *
   * <code>.SparseValues sparse_values = 4 [json_name = "sparseValues"];</code>
   * @return Whether the sparseValues field is set.
   */
  boolean hasSparseValues();
  /**
   * <pre>
   * This is the sparse data included in the request. Can only be specified if `sparse` index.
   * </pre>
   *
   * <code>.SparseValues sparse_values = 4 [json_name = "sparseValues"];</code>
   * @return The sparseValues.
   */
  io.pinecone.proto.SparseValues getSparseValues();
  /**
   * <pre>
   * This is the sparse data included in the request. Can only be specified if `sparse` index.
   * </pre>
   *
   * <code>.SparseValues sparse_values = 4 [json_name = "sparseValues"];</code>
   */
  io.pinecone.proto.SparseValuesOrBuilder getSparseValuesOrBuilder();

  /**
   * <pre>
   * This is the metadata included in the request.
   * </pre>
   *
   * <code>.google.protobuf.Struct metadata = 3 [json_name = "metadata"];</code>
   * @return Whether the metadata field is set.
   */
  boolean hasMetadata();
  /**
   * <pre>
   * This is the metadata included in the request.
   * </pre>
   *
   * <code>.google.protobuf.Struct metadata = 3 [json_name = "metadata"];</code>
   * @return The metadata.
   */
  com.google.protobuf.Struct getMetadata();
  /**
   * <pre>
   * This is the metadata included in the request.
   * </pre>
   *
   * <code>.google.protobuf.Struct metadata = 3 [json_name = "metadata"];</code>
   */
  com.google.protobuf.StructOrBuilder getMetadataOrBuilder();
}
