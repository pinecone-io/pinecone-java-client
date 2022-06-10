// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: vector_service.proto

package io.pinecone.proto;

public interface DescribeIndexStatsResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:DescribeIndexStatsResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * A mapping for each namespace in the index from namespace name to a summary of its contents.
   * </pre>
   *
   * <code>map&lt;string, .NamespaceSummary&gt; namespaces = 1;</code>
   */
  int getNamespacesCount();
  /**
   * <pre>
   * A mapping for each namespace in the index from namespace name to a summary of its contents.
   * </pre>
   *
   * <code>map&lt;string, .NamespaceSummary&gt; namespaces = 1;</code>
   */
  boolean containsNamespaces(
      java.lang.String key);
  /**
   * Use {@link #getNamespacesMap()} instead.
   */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, io.pinecone.proto.NamespaceSummary>
  getNamespaces();
  /**
   * <pre>
   * A mapping for each namespace in the index from namespace name to a summary of its contents.
   * </pre>
   *
   * <code>map&lt;string, .NamespaceSummary&gt; namespaces = 1;</code>
   */
  java.util.Map<java.lang.String, io.pinecone.proto.NamespaceSummary>
  getNamespacesMap();
  /**
   * <pre>
   * A mapping for each namespace in the index from namespace name to a summary of its contents.
   * </pre>
   *
   * <code>map&lt;string, .NamespaceSummary&gt; namespaces = 1;</code>
   */

  io.pinecone.proto.NamespaceSummary getNamespacesOrDefault(
      java.lang.String key,
      io.pinecone.proto.NamespaceSummary defaultValue);
  /**
   * <pre>
   * A mapping for each namespace in the index from namespace name to a summary of its contents.
   * </pre>
   *
   * <code>map&lt;string, .NamespaceSummary&gt; namespaces = 1;</code>
   */

  io.pinecone.proto.NamespaceSummary getNamespacesOrThrow(
      java.lang.String key);

  /**
   * <pre>
   * The dimension of the indexed vectors.
   * </pre>
   *
   * <code>uint32 dimension = 2 [(.grpc.gateway.protoc_gen_openapiv2.options.openapiv2_field) = { ... }</code>
   * @return The dimension.
   */
  int getDimension();

  /**
   * <pre>
   * The fullness of the index.
   * </pre>
   *
   * <code>float index_fullness = 3 [(.grpc.gateway.protoc_gen_openapiv2.options.openapiv2_field) = { ... }</code>
   * @return The indexFullness.
   */
  float getIndexFullness();
}
