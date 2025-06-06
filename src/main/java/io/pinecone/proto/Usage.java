// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: db_data_2025-04.proto
// Protobuf Java Version: 4.29.3

package io.pinecone.proto;

/**
 * Protobuf type {@code Usage}
 */
public final class Usage extends
    com.google.protobuf.GeneratedMessage implements
    // @@protoc_insertion_point(message_implements:Usage)
    UsageOrBuilder {
private static final long serialVersionUID = 0L;
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 29,
      /* patch= */ 3,
      /* suffix= */ "",
      Usage.class.getName());
  }
  // Use Usage.newBuilder() to construct.
  private Usage(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
    super(builder);
  }
  private Usage() {
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.pinecone.proto.DbData202504.internal_static_Usage_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.pinecone.proto.DbData202504.internal_static_Usage_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.pinecone.proto.Usage.class, io.pinecone.proto.Usage.Builder.class);
  }

  private int bitField0_;
  public static final int READ_UNITS_FIELD_NUMBER = 1;
  private int readUnits_ = 0;
  /**
   * <pre>
   * The number of read units consumed by this operation.
   * </pre>
   *
   * <code>optional uint32 read_units = 1 [json_name = "readUnits"];</code>
   * @return Whether the readUnits field is set.
   */
  @java.lang.Override
  public boolean hasReadUnits() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <pre>
   * The number of read units consumed by this operation.
   * </pre>
   *
   * <code>optional uint32 read_units = 1 [json_name = "readUnits"];</code>
   * @return The readUnits.
   */
  @java.lang.Override
  public int getReadUnits() {
    return readUnits_;
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
      output.writeUInt32(1, readUnits_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (((bitField0_ & 0x00000001) != 0)) {
      size += com.google.protobuf.CodedOutputStream
        .computeUInt32Size(1, readUnits_);
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
    if (!(obj instanceof io.pinecone.proto.Usage)) {
      return super.equals(obj);
    }
    io.pinecone.proto.Usage other = (io.pinecone.proto.Usage) obj;

    if (hasReadUnits() != other.hasReadUnits()) return false;
    if (hasReadUnits()) {
      if (getReadUnits()
          != other.getReadUnits()) return false;
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
    if (hasReadUnits()) {
      hash = (37 * hash) + READ_UNITS_FIELD_NUMBER;
      hash = (53 * hash) + getReadUnits();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.pinecone.proto.Usage parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.pinecone.proto.Usage parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.pinecone.proto.Usage parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.pinecone.proto.Usage parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.pinecone.proto.Usage parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.pinecone.proto.Usage parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.pinecone.proto.Usage parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.pinecone.proto.Usage parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.pinecone.proto.Usage parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.pinecone.proto.Usage parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.pinecone.proto.Usage parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.pinecone.proto.Usage parseFrom(
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
  public static Builder newBuilder(io.pinecone.proto.Usage prototype) {
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
   * Protobuf type {@code Usage}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessage.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:Usage)
      io.pinecone.proto.UsageOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.pinecone.proto.DbData202504.internal_static_Usage_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.pinecone.proto.DbData202504.internal_static_Usage_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.pinecone.proto.Usage.class, io.pinecone.proto.Usage.Builder.class);
    }

    // Construct using io.pinecone.proto.Usage.newBuilder()
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
      readUnits_ = 0;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.pinecone.proto.DbData202504.internal_static_Usage_descriptor;
    }

    @java.lang.Override
    public io.pinecone.proto.Usage getDefaultInstanceForType() {
      return io.pinecone.proto.Usage.getDefaultInstance();
    }

    @java.lang.Override
    public io.pinecone.proto.Usage build() {
      io.pinecone.proto.Usage result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.pinecone.proto.Usage buildPartial() {
      io.pinecone.proto.Usage result = new io.pinecone.proto.Usage(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.pinecone.proto.Usage result) {
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.readUnits_ = readUnits_;
        to_bitField0_ |= 0x00000001;
      }
      result.bitField0_ |= to_bitField0_;
    }

    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.pinecone.proto.Usage) {
        return mergeFrom((io.pinecone.proto.Usage)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.pinecone.proto.Usage other) {
      if (other == io.pinecone.proto.Usage.getDefaultInstance()) return this;
      if (other.hasReadUnits()) {
        setReadUnits(other.getReadUnits());
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
            case 8: {
              readUnits_ = input.readUInt32();
              bitField0_ |= 0x00000001;
              break;
            } // case 8
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

    private int readUnits_ ;
    /**
     * <pre>
     * The number of read units consumed by this operation.
     * </pre>
     *
     * <code>optional uint32 read_units = 1 [json_name = "readUnits"];</code>
     * @return Whether the readUnits field is set.
     */
    @java.lang.Override
    public boolean hasReadUnits() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <pre>
     * The number of read units consumed by this operation.
     * </pre>
     *
     * <code>optional uint32 read_units = 1 [json_name = "readUnits"];</code>
     * @return The readUnits.
     */
    @java.lang.Override
    public int getReadUnits() {
      return readUnits_;
    }
    /**
     * <pre>
     * The number of read units consumed by this operation.
     * </pre>
     *
     * <code>optional uint32 read_units = 1 [json_name = "readUnits"];</code>
     * @param value The readUnits to set.
     * @return This builder for chaining.
     */
    public Builder setReadUnits(int value) {

      readUnits_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The number of read units consumed by this operation.
     * </pre>
     *
     * <code>optional uint32 read_units = 1 [json_name = "readUnits"];</code>
     * @return This builder for chaining.
     */
    public Builder clearReadUnits() {
      bitField0_ = (bitField0_ & ~0x00000001);
      readUnits_ = 0;
      onChanged();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:Usage)
  }

  // @@protoc_insertion_point(class_scope:Usage)
  private static final io.pinecone.proto.Usage DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.pinecone.proto.Usage();
  }

  public static io.pinecone.proto.Usage getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Usage>
      PARSER = new com.google.protobuf.AbstractParser<Usage>() {
    @java.lang.Override
    public Usage parsePartialFrom(
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

  public static com.google.protobuf.Parser<Usage> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Usage> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.pinecone.proto.Usage getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

