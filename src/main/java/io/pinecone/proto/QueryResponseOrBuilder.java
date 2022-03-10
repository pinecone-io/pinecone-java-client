// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: protos/vector_service.proto

package io.pinecone.proto;

public interface QueryResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:QueryResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The results of each query. The order is the same as `QueryRequest.queries`.
   * </pre>
   *
   * <code>repeated .SingleQueryResults results = 1;</code>
   */
  java.util.List<io.pinecone.proto.SingleQueryResults> 
      getResultsList();
  /**
   * <pre>
   * The results of each query. The order is the same as `QueryRequest.queries`.
   * </pre>
   *
   * <code>repeated .SingleQueryResults results = 1;</code>
   */
  io.pinecone.proto.SingleQueryResults getResults(int index);
  /**
   * <pre>
   * The results of each query. The order is the same as `QueryRequest.queries`.
   * </pre>
   *
   * <code>repeated .SingleQueryResults results = 1;</code>
   */
  int getResultsCount();
  /**
   * <pre>
   * The results of each query. The order is the same as `QueryRequest.queries`.
   * </pre>
   *
   * <code>repeated .SingleQueryResults results = 1;</code>
   */
  java.util.List<? extends io.pinecone.proto.SingleQueryResultsOrBuilder> 
      getResultsOrBuilderList();
  /**
   * <pre>
   * The results of each query. The order is the same as `QueryRequest.queries`.
   * </pre>
   *
   * <code>repeated .SingleQueryResults results = 1;</code>
   */
  io.pinecone.proto.SingleQueryResultsOrBuilder getResultsOrBuilder(
      int index);
}
