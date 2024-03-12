// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: vector_service.proto

// Protobuf Java Version: 3.25.3
package io.pinecone.proto;

public interface SingleQueryResultsOrBuilder extends
    // @@protoc_insertion_point(interface_extends:SingleQueryResults)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The matches for the vectors.
   * </pre>
   *
   * <code>repeated .ScoredVector matches = 1;</code>
   */
  java.util.List<io.pinecone.proto.ScoredVector> 
      getMatchesList();
  /**
   * <pre>
   * The matches for the vectors.
   * </pre>
   *
   * <code>repeated .ScoredVector matches = 1;</code>
   */
  io.pinecone.proto.ScoredVector getMatches(int index);
  /**
   * <pre>
   * The matches for the vectors.
   * </pre>
   *
   * <code>repeated .ScoredVector matches = 1;</code>
   */
  int getMatchesCount();
  /**
   * <pre>
   * The matches for the vectors.
   * </pre>
   *
   * <code>repeated .ScoredVector matches = 1;</code>
   */
  java.util.List<? extends io.pinecone.proto.ScoredVectorOrBuilder> 
      getMatchesOrBuilderList();
  /**
   * <pre>
   * The matches for the vectors.
   * </pre>
   *
   * <code>repeated .ScoredVector matches = 1;</code>
   */
  io.pinecone.proto.ScoredVectorOrBuilder getMatchesOrBuilder(
      int index);

  /**
   * <pre>
   * The namespace for the vectors.
   * </pre>
   *
   * <code>string namespace = 2;</code>
   * @return The namespace.
   */
  java.lang.String getNamespace();
  /**
   * <pre>
   * The namespace for the vectors.
   * </pre>
   *
   * <code>string namespace = 2;</code>
   * @return The bytes for namespace.
   */
  com.google.protobuf.ByteString
      getNamespaceBytes();
}
