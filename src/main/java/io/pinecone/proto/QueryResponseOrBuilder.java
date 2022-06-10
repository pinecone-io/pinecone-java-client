// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: vector_service.proto

package io.pinecone.proto;

public interface QueryResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:QueryResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * DEPRECATED. The results of each query. The order is the same as `QueryRequest.queries`.
   * </pre>
   *
   * <code>repeated .SingleQueryResults results = 1 [deprecated = true];</code>
   */
  @java.lang.Deprecated java.util.List<io.pinecone.proto.SingleQueryResults> 
      getResultsList();
  /**
   * <pre>
   * DEPRECATED. The results of each query. The order is the same as `QueryRequest.queries`.
   * </pre>
   *
   * <code>repeated .SingleQueryResults results = 1 [deprecated = true];</code>
   */
  @java.lang.Deprecated io.pinecone.proto.SingleQueryResults getResults(int index);
  /**
   * <pre>
   * DEPRECATED. The results of each query. The order is the same as `QueryRequest.queries`.
   * </pre>
   *
   * <code>repeated .SingleQueryResults results = 1 [deprecated = true];</code>
   */
  @java.lang.Deprecated int getResultsCount();
  /**
   * <pre>
   * DEPRECATED. The results of each query. The order is the same as `QueryRequest.queries`.
   * </pre>
   *
   * <code>repeated .SingleQueryResults results = 1 [deprecated = true];</code>
   */
  @java.lang.Deprecated java.util.List<? extends io.pinecone.proto.SingleQueryResultsOrBuilder> 
      getResultsOrBuilderList();
  /**
   * <pre>
   * DEPRECATED. The results of each query. The order is the same as `QueryRequest.queries`.
   * </pre>
   *
   * <code>repeated .SingleQueryResults results = 1 [deprecated = true];</code>
   */
  @java.lang.Deprecated io.pinecone.proto.SingleQueryResultsOrBuilder getResultsOrBuilder(
      int index);

  /**
   * <pre>
   * The matches for the vectors.
   * </pre>
   *
   * <code>repeated .ScoredVector matches = 2;</code>
   */
  java.util.List<io.pinecone.proto.ScoredVector> 
      getMatchesList();
  /**
   * <pre>
   * The matches for the vectors.
   * </pre>
   *
   * <code>repeated .ScoredVector matches = 2;</code>
   */
  io.pinecone.proto.ScoredVector getMatches(int index);
  /**
   * <pre>
   * The matches for the vectors.
   * </pre>
   *
   * <code>repeated .ScoredVector matches = 2;</code>
   */
  int getMatchesCount();
  /**
   * <pre>
   * The matches for the vectors.
   * </pre>
   *
   * <code>repeated .ScoredVector matches = 2;</code>
   */
  java.util.List<? extends io.pinecone.proto.ScoredVectorOrBuilder> 
      getMatchesOrBuilderList();
  /**
   * <pre>
   * The matches for the vectors.
   * </pre>
   *
   * <code>repeated .ScoredVector matches = 2;</code>
   */
  io.pinecone.proto.ScoredVectorOrBuilder getMatchesOrBuilder(
      int index);

  /**
   * <pre>
   * The namespace for the vectors.
   * </pre>
   *
   * <code>string namespace = 3;</code>
   * @return The namespace.
   */
  java.lang.String getNamespace();
  /**
   * <pre>
   * The namespace for the vectors.
   * </pre>
   *
   * <code>string namespace = 3;</code>
   * @return The bytes for namespace.
   */
  com.google.protobuf.ByteString
      getNamespaceBytes();
}
