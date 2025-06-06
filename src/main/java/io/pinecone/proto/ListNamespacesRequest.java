// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: db_data_2025-04.proto
// Protobuf Java Version: 4.29.3

package io.pinecone.proto;

/**
 * <pre>
 * The request for the list namespaces operation.
 * </pre>
 *
 * Protobuf type {@code ListNamespacesRequest}
 */
public final class ListNamespacesRequest extends
    com.google.protobuf.GeneratedMessage implements
    // @@protoc_insertion_point(message_implements:ListNamespacesRequest)
    ListNamespacesRequestOrBuilder {
private static final long serialVersionUID = 0L;
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 29,
      /* patch= */ 3,
      /* suffix= */ "",
      ListNamespacesRequest.class.getName());
  }
  // Use ListNamespacesRequest.newBuilder() to construct.
  private ListNamespacesRequest(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
    super(builder);
  }
  private ListNamespacesRequest() {
    paginationToken_ = "";
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.pinecone.proto.DbData202504.internal_static_ListNamespacesRequest_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.pinecone.proto.DbData202504.internal_static_ListNamespacesRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.pinecone.proto.ListNamespacesRequest.class, io.pinecone.proto.ListNamespacesRequest.Builder.class);
  }

  private int bitField0_;
  public static final int PAGINATION_TOKEN_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private volatile java.lang.Object paginationToken_ = "";
  /**
   * <pre>
   * Pagination token to continue a previous listing operation
   * </pre>
   *
   * <code>optional string pagination_token = 1 [json_name = "paginationToken"];</code>
   * @return Whether the paginationToken field is set.
   */
  @java.lang.Override
  public boolean hasPaginationToken() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <pre>
   * Pagination token to continue a previous listing operation
   * </pre>
   *
   * <code>optional string pagination_token = 1 [json_name = "paginationToken"];</code>
   * @return The paginationToken.
   */
  @java.lang.Override
  public java.lang.String getPaginationToken() {
    java.lang.Object ref = paginationToken_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      paginationToken_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * Pagination token to continue a previous listing operation
   * </pre>
   *
   * <code>optional string pagination_token = 1 [json_name = "paginationToken"];</code>
   * @return The bytes for paginationToken.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getPaginationTokenBytes() {
    java.lang.Object ref = paginationToken_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      paginationToken_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int LIMIT_FIELD_NUMBER = 2;
  private int limit_ = 0;
  /**
   * <pre>
   * Max number of namespaces to return
   * </pre>
   *
   * <code>optional uint32 limit = 2 [json_name = "limit"];</code>
   * @return Whether the limit field is set.
   */
  @java.lang.Override
  public boolean hasLimit() {
    return ((bitField0_ & 0x00000002) != 0);
  }
  /**
   * <pre>
   * Max number of namespaces to return
   * </pre>
   *
   * <code>optional uint32 limit = 2 [json_name = "limit"];</code>
   * @return The limit.
   */
  @java.lang.Override
  public int getLimit() {
    return limit_;
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (((bitField0_ & 0x00000001) != 0)) {
      com.google.protobuf.GeneratedMessage.writeString(output, 1, paginationToken_);
    }
    if (((bitField0_ & 0x00000002) != 0)) {
      output.writeUInt32(2, limit_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (((bitField0_ & 0x00000001) != 0)) {
      size += com.google.protobuf.GeneratedMessage.computeStringSize(1, paginationToken_);
    }
    if (((bitField0_ & 0x00000002) != 0)) {
      size += com.google.protobuf.CodedOutputStream
        .computeUInt32Size(2, limit_);
    }
    size += getUnknownFields().getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof io.pinecone.proto.ListNamespacesRequest)) {
      return super.equals(obj);
    }
    io.pinecone.proto.ListNamespacesRequest other = (io.pinecone.proto.ListNamespacesRequest) obj;

    if (hasPaginationToken() != other.hasPaginationToken()) return false;
    if (hasPaginationToken()) {
      if (!getPaginationToken()
          .equals(other.getPaginationToken())) return false;
    }
    if (hasLimit() != other.hasLimit()) return false;
    if (hasLimit()) {
      if (getLimit()
          != other.getLimit()) return false;
    }
    if (!getUnknownFields().equals(other.getUnknownFields())) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    if (hasPaginationToken()) {
      hash = (37 * hash) + PAGINATION_TOKEN_FIELD_NUMBER;
      hash = (53 * hash) + getPaginationToken().hashCode();
    }
    if (hasLimit()) {
      hash = (37 * hash) + LIMIT_FIELD_NUMBER;
      hash = (53 * hash) + getLimit();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.pinecone.proto.ListNamespacesRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.pinecone.proto.ListNamespacesRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.pinecone.proto.ListNamespacesRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.pinecone.proto.ListNamespacesRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.pinecone.proto.ListNamespacesRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.pinecone.proto.ListNamespacesRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.pinecone.proto.ListNamespacesRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.pinecone.proto.ListNamespacesRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.pinecone.proto.ListNamespacesRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.pinecone.proto.ListNamespacesRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.pinecone.proto.ListNamespacesRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.pinecone.proto.ListNamespacesRequest parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(io.pinecone.proto.ListNamespacesRequest prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessage.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * <pre>
   * The request for the list namespaces operation.
   * </pre>
   *
   * Protobuf type {@code ListNamespacesRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessage.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:ListNamespacesRequest)
      io.pinecone.proto.ListNamespacesRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.pinecone.proto.DbData202504.internal_static_ListNamespacesRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.pinecone.proto.DbData202504.internal_static_ListNamespacesRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.pinecone.proto.ListNamespacesRequest.class, io.pinecone.proto.ListNamespacesRequest.Builder.class);
    }

    // Construct using io.pinecone.proto.ListNamespacesRequest.newBuilder()
    private Builder() {

    }

    private Builder(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      super(parent);

    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      paginationToken_ = "";
      limit_ = 0;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.pinecone.proto.DbData202504.internal_static_ListNamespacesRequest_descriptor;
    }

    @java.lang.Override
    public io.pinecone.proto.ListNamespacesRequest getDefaultInstanceForType() {
      return io.pinecone.proto.ListNamespacesRequest.getDefaultInstance();
    }

    @java.lang.Override
    public io.pinecone.proto.ListNamespacesRequest build() {
      io.pinecone.proto.ListNamespacesRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.pinecone.proto.ListNamespacesRequest buildPartial() {
      io.pinecone.proto.ListNamespacesRequest result = new io.pinecone.proto.ListNamespacesRequest(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.pinecone.proto.ListNamespacesRequest result) {
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.paginationToken_ = paginationToken_;
        to_bitField0_ |= 0x00000001;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.limit_ = limit_;
        to_bitField0_ |= 0x00000002;
      }
      result.bitField0_ |= to_bitField0_;
    }

    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.pinecone.proto.ListNamespacesRequest) {
        return mergeFrom((io.pinecone.proto.ListNamespacesRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.pinecone.proto.ListNamespacesRequest other) {
      if (other == io.pinecone.proto.ListNamespacesRequest.getDefaultInstance()) return this;
      if (other.hasPaginationToken()) {
        paginationToken_ = other.paginationToken_;
        bitField0_ |= 0x00000001;
        onChanged();
      }
      if (other.hasLimit()) {
        setLimit(other.getLimit());
      }
      this.mergeUnknownFields(other.getUnknownFields());
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            case 10: {
              paginationToken_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 16: {
              limit_ = input.readUInt32();
              bitField0_ |= 0x00000002;
              break;
            } // case 16
            default: {
              if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                done = true; // was an endgroup tag
              }
              break;
            } // default:
          } // switch (tag)
        } // while (!done)
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.unwrapIOException();
      } finally {
        onChanged();
      } // finally
      return this;
    }
    private int bitField0_;

    private java.lang.Object paginationToken_ = "";
    /**
     * <pre>
     * Pagination token to continue a previous listing operation
     * </pre>
     *
     * <code>optional string pagination_token = 1 [json_name = "paginationToken"];</code>
     * @return Whether the paginationToken field is set.
     */
    public boolean hasPaginationToken() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <pre>
     * Pagination token to continue a previous listing operation
     * </pre>
     *
     * <code>optional string pagination_token = 1 [json_name = "paginationToken"];</code>
     * @return The paginationToken.
     */
    public java.lang.String getPaginationToken() {
      java.lang.Object ref = paginationToken_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        paginationToken_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * Pagination token to continue a previous listing operation
     * </pre>
     *
     * <code>optional string pagination_token = 1 [json_name = "paginationToken"];</code>
     * @return The bytes for paginationToken.
     */
    public com.google.protobuf.ByteString
        getPaginationTokenBytes() {
      java.lang.Object ref = paginationToken_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        paginationToken_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * Pagination token to continue a previous listing operation
     * </pre>
     *
     * <code>optional string pagination_token = 1 [json_name = "paginationToken"];</code>
     * @param value The paginationToken to set.
     * @return This builder for chaining.
     */
    public Builder setPaginationToken(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      paginationToken_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Pagination token to continue a previous listing operation
     * </pre>
     *
     * <code>optional string pagination_token = 1 [json_name = "paginationToken"];</code>
     * @return This builder for chaining.
     */
    public Builder clearPaginationToken() {
      paginationToken_ = getDefaultInstance().getPaginationToken();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Pagination token to continue a previous listing operation
     * </pre>
     *
     * <code>optional string pagination_token = 1 [json_name = "paginationToken"];</code>
     * @param value The bytes for paginationToken to set.
     * @return This builder for chaining.
     */
    public Builder setPaginationTokenBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      paginationToken_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }

    private int limit_ ;
    /**
     * <pre>
     * Max number of namespaces to return
     * </pre>
     *
     * <code>optional uint32 limit = 2 [json_name = "limit"];</code>
     * @return Whether the limit field is set.
     */
    @java.lang.Override
    public boolean hasLimit() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <pre>
     * Max number of namespaces to return
     * </pre>
     *
     * <code>optional uint32 limit = 2 [json_name = "limit"];</code>
     * @return The limit.
     */
    @java.lang.Override
    public int getLimit() {
      return limit_;
    }
    /**
     * <pre>
     * Max number of namespaces to return
     * </pre>
     *
     * <code>optional uint32 limit = 2 [json_name = "limit"];</code>
     * @param value The limit to set.
     * @return This builder for chaining.
     */
    public Builder setLimit(int value) {

      limit_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Max number of namespaces to return
     * </pre>
     *
     * <code>optional uint32 limit = 2 [json_name = "limit"];</code>
     * @return This builder for chaining.
     */
    public Builder clearLimit() {
      bitField0_ = (bitField0_ & ~0x00000002);
      limit_ = 0;
      onChanged();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:ListNamespacesRequest)
  }

  // @@protoc_insertion_point(class_scope:ListNamespacesRequest)
  private static final io.pinecone.proto.ListNamespacesRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.pinecone.proto.ListNamespacesRequest();
  }

  public static io.pinecone.proto.ListNamespacesRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ListNamespacesRequest>
      PARSER = new com.google.protobuf.AbstractParser<ListNamespacesRequest>() {
    @java.lang.Override
    public ListNamespacesRequest parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      Builder builder = newBuilder();
      try {
        builder.mergeFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(builder.buildPartial());
      } catch (com.google.protobuf.UninitializedMessageException e) {
        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(e)
            .setUnfinishedMessage(builder.buildPartial());
      }
      return builder.buildPartial();
    }
  };

  public static com.google.protobuf.Parser<ListNamespacesRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ListNamespacesRequest> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.pinecone.proto.ListNamespacesRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

