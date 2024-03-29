// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: vector_service.proto

// Protobuf Java Version: 3.25.3
package io.pinecone.proto;

public interface QueryRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:QueryRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The namespace to query.
   * </pre>
   *
   * <code>string namespace = 1;</code>
   * @return The namespace.
   */
  java.lang.String getNamespace();
  /**
   * <pre>
   * The namespace to query.
   * </pre>
   *
   * <code>string namespace = 1;</code>
   * @return The bytes for namespace.
   */
  com.google.protobuf.ByteString
      getNamespaceBytes();

  /**
   * <pre>
   * The number of results to return for each query.
   * </pre>
   *
   * <code>uint32 top_k = 2 [(.google.api.field_behavior) = REQUIRED];</code>
   * @return The topK.
   */
  int getTopK();

  /**
   * <pre>
   * The filter to apply. You can use vector metadata to limit your search. See https://www.pinecone.io/docs/metadata-filtering/.
   * </pre>
   *
   * <code>.google.protobuf.Struct filter = 3;</code>
   * @return Whether the filter field is set.
   */
  boolean hasFilter();
  /**
   * <pre>
   * The filter to apply. You can use vector metadata to limit your search. See https://www.pinecone.io/docs/metadata-filtering/.
   * </pre>
   *
   * <code>.google.protobuf.Struct filter = 3;</code>
   * @return The filter.
   */
  com.google.protobuf.Struct getFilter();
  /**
   * <pre>
   * The filter to apply. You can use vector metadata to limit your search. See https://www.pinecone.io/docs/metadata-filtering/.
   * </pre>
   *
   * <code>.google.protobuf.Struct filter = 3;</code>
   */
  com.google.protobuf.StructOrBuilder getFilterOrBuilder();

  /**
   * <pre>
   * Indicates whether vector values are included in the response.
   * </pre>
   *
   * <code>bool include_values = 4;</code>
   * @return The includeValues.
   */
  boolean getIncludeValues();

  /**
   * <pre>
   * Indicates whether metadata is included in the response as well as the ids.
   * </pre>
   *
   * <code>bool include_metadata = 5;</code>
   * @return The includeMetadata.
   */
  boolean getIncludeMetadata();

  /**
   * <pre>
   * DEPRECATED. The query vectors. Each `query()` request can contain only one of the parameters `queries`, `vector`, or  `id`.
   * </pre>
   *
   * <code>repeated .QueryVector queries = 6 [deprecated = true];</code>
   */
  @java.lang.Deprecated java.util.List<io.pinecone.proto.QueryVector> 
      getQueriesList();
  /**
   * <pre>
   * DEPRECATED. The query vectors. Each `query()` request can contain only one of the parameters `queries`, `vector`, or  `id`.
   * </pre>
   *
   * <code>repeated .QueryVector queries = 6 [deprecated = true];</code>
   */
  @java.lang.Deprecated io.pinecone.proto.QueryVector getQueries(int index);
  /**
   * <pre>
   * DEPRECATED. The query vectors. Each `query()` request can contain only one of the parameters `queries`, `vector`, or  `id`.
   * </pre>
   *
   * <code>repeated .QueryVector queries = 6 [deprecated = true];</code>
   */
  @java.lang.Deprecated int getQueriesCount();
  /**
   * <pre>
   * DEPRECATED. The query vectors. Each `query()` request can contain only one of the parameters `queries`, `vector`, or  `id`.
   * </pre>
   *
   * <code>repeated .QueryVector queries = 6 [deprecated = true];</code>
   */
  @java.lang.Deprecated java.util.List<? extends io.pinecone.proto.QueryVectorOrBuilder> 
      getQueriesOrBuilderList();
  /**
   * <pre>
   * DEPRECATED. The query vectors. Each `query()` request can contain only one of the parameters `queries`, `vector`, or  `id`.
   * </pre>
   *
   * <code>repeated .QueryVector queries = 6 [deprecated = true];</code>
   */
  @java.lang.Deprecated io.pinecone.proto.QueryVectorOrBuilder getQueriesOrBuilder(
      int index);

  /**
   * <pre>
   * The query vector. This should be the same length as the dimension of the index being queried. Each `query()` request can contain only one of the parameters `id` or `vector`.
   * </pre>
   *
   * <code>repeated float vector = 7;</code>
   * @return A list containing the vector.
   */
  java.util.List<java.lang.Float> getVectorList();
  /**
   * <pre>
   * The query vector. This should be the same length as the dimension of the index being queried. Each `query()` request can contain only one of the parameters `id` or `vector`.
   * </pre>
   *
   * <code>repeated float vector = 7;</code>
   * @return The count of vector.
   */
  int getVectorCount();
  /**
   * <pre>
   * The query vector. This should be the same length as the dimension of the index being queried. Each `query()` request can contain only one of the parameters `id` or `vector`.
   * </pre>
   *
   * <code>repeated float vector = 7;</code>
   * @param index The index of the element to return.
   * @return The vector at the given index.
   */
  float getVector(int index);

  /**
   * <pre>
   * The query sparse values.
   * </pre>
   *
   * <code>.SparseValues sparse_vector = 9;</code>
   * @return Whether the sparseVector field is set.
   */
  boolean hasSparseVector();
  /**
   * <pre>
   * The query sparse values.
   * </pre>
   *
   * <code>.SparseValues sparse_vector = 9;</code>
   * @return The sparseVector.
   */
  io.pinecone.proto.SparseValues getSparseVector();
  /**
   * <pre>
   * The query sparse values.
   * </pre>
   *
   * <code>.SparseValues sparse_vector = 9;</code>
   */
  io.pinecone.proto.SparseValuesOrBuilder getSparseVectorOrBuilder();

  /**
   * <pre>
   * The unique ID of the vector to be used as a query vector. Each `query()` request can contain only one of the parameters `queries`, `vector`, or  `id`.
   * </pre>
   *
   * <code>string id = 8;</code>
   * @return The id.
   */
  java.lang.String getId();
  /**
   * <pre>
   * The unique ID of the vector to be used as a query vector. Each `query()` request can contain only one of the parameters `queries`, `vector`, or  `id`.
   * </pre>
   *
   * <code>string id = 8;</code>
   * @return The bytes for id.
   */
  com.google.protobuf.ByteString
      getIdBytes();
}
