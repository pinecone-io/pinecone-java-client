// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: vector_service.proto

// Protobuf Java Version: 3.25.3
package io.pinecone.proto;

/**
 * <pre>
 * The response for the `fetch` operation.
 * </pre>
 *
 * Protobuf type {@code FetchResponse}
 */
public final class FetchResponse extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:FetchResponse)
    FetchResponseOrBuilder {
private static final long serialVersionUID = 0L;
  // Use FetchResponse.newBuilder() to construct.
  private FetchResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private FetchResponse() {
    namespace_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new FetchResponse();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.pinecone.proto.VectorServiceOuterClass.internal_static_FetchResponse_descriptor;
  }

  @SuppressWarnings({"rawtypes"})
  @java.lang.Override
  protected com.google.protobuf.MapFieldReflectionAccessor internalGetMapFieldReflection(
      int number) {
    switch (number) {
      case 1:
        return internalGetVectors();
      default:
        throw new RuntimeException(
            "Invalid map field number: " + number);
    }
  }
  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.pinecone.proto.VectorServiceOuterClass.internal_static_FetchResponse_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.pinecone.proto.FetchResponse.class, io.pinecone.proto.FetchResponse.Builder.class);
  }

  private int bitField0_;
  public static final int VECTORS_FIELD_NUMBER = 1;
  private static final class VectorsDefaultEntryHolder {
    static final com.google.protobuf.MapEntry<
        java.lang.String, io.pinecone.proto.Vector> defaultEntry =
            com.google.protobuf.MapEntry
            .<java.lang.String, io.pinecone.proto.Vector>newDefaultInstance(
                io.pinecone.proto.VectorServiceOuterClass.internal_static_FetchResponse_VectorsEntry_descriptor, 
                com.google.protobuf.WireFormat.FieldType.STRING,
                "",
                com.google.protobuf.WireFormat.FieldType.MESSAGE,
                io.pinecone.proto.Vector.getDefaultInstance());
  }
  @SuppressWarnings("serial")
  private com.google.protobuf.MapField<
      java.lang.String, io.pinecone.proto.Vector> vectors_;
  private com.google.protobuf.MapField<java.lang.String, io.pinecone.proto.Vector>
  internalGetVectors() {
    if (vectors_ == null) {
      return com.google.protobuf.MapField.emptyMapField(
          VectorsDefaultEntryHolder.defaultEntry);
    }
    return vectors_;
  }
  public int getVectorsCount() {
    return internalGetVectors().getMap().size();
  }
  /**
   * <pre>
   * The fetched vectors, in the form of a map between the fetched ids and the fetched vectors
   * </pre>
   *
   * <code>map&lt;string, .Vector&gt; vectors = 1;</code>
   */
  @java.lang.Override
  public boolean containsVectors(
      java.lang.String key) {
    if (key == null) { throw new NullPointerException("map key"); }
    return internalGetVectors().getMap().containsKey(key);
  }
  /**
   * Use {@link #getVectorsMap()} instead.
   */
  @java.lang.Override
  @java.lang.Deprecated
  public java.util.Map<java.lang.String, io.pinecone.proto.Vector> getVectors() {
    return getVectorsMap();
  }
  /**
   * <pre>
   * The fetched vectors, in the form of a map between the fetched ids and the fetched vectors
   * </pre>
   *
   * <code>map&lt;string, .Vector&gt; vectors = 1;</code>
   */
  @java.lang.Override
  public java.util.Map<java.lang.String, io.pinecone.proto.Vector> getVectorsMap() {
    return internalGetVectors().getMap();
  }
  /**
   * <pre>
   * The fetched vectors, in the form of a map between the fetched ids and the fetched vectors
   * </pre>
   *
   * <code>map&lt;string, .Vector&gt; vectors = 1;</code>
   */
  @java.lang.Override
  public /* nullable */
io.pinecone.proto.Vector getVectorsOrDefault(
      java.lang.String key,
      /* nullable */
io.pinecone.proto.Vector defaultValue) {
    if (key == null) { throw new NullPointerException("map key"); }
    java.util.Map<java.lang.String, io.pinecone.proto.Vector> map =
        internalGetVectors().getMap();
    return map.containsKey(key) ? map.get(key) : defaultValue;
  }
  /**
   * <pre>
   * The fetched vectors, in the form of a map between the fetched ids and the fetched vectors
   * </pre>
   *
   * <code>map&lt;string, .Vector&gt; vectors = 1;</code>
   */
  @java.lang.Override
  public io.pinecone.proto.Vector getVectorsOrThrow(
      java.lang.String key) {
    if (key == null) { throw new NullPointerException("map key"); }
    java.util.Map<java.lang.String, io.pinecone.proto.Vector> map =
        internalGetVectors().getMap();
    if (!map.containsKey(key)) {
      throw new java.lang.IllegalArgumentException();
    }
    return map.get(key);
  }

  public static final int NAMESPACE_FIELD_NUMBER = 2;
  @SuppressWarnings("serial")
  private volatile java.lang.Object namespace_ = "";
  /**
   * <pre>
   * The namespace of the vectors.
   * </pre>
   *
   * <code>string namespace = 2;</code>
   * @return The namespace.
   */
  @java.lang.Override
  public java.lang.String getNamespace() {
    java.lang.Object ref = namespace_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      namespace_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * The namespace of the vectors.
   * </pre>
   *
   * <code>string namespace = 2;</code>
   * @return The bytes for namespace.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getNamespaceBytes() {
    java.lang.Object ref = namespace_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      namespace_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int USAGE_FIELD_NUMBER = 3;
  private io.pinecone.proto.Usage usage_;
  /**
   * <pre>
   *  The usage for this operation.
   * </pre>
   *
   * <code>optional .Usage usage = 3;</code>
   * @return Whether the usage field is set.
   */
  @java.lang.Override
  public boolean hasUsage() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <pre>
   *  The usage for this operation.
   * </pre>
   *
   * <code>optional .Usage usage = 3;</code>
   * @return The usage.
   */
  @java.lang.Override
  public io.pinecone.proto.Usage getUsage() {
    return usage_ == null ? io.pinecone.proto.Usage.getDefaultInstance() : usage_;
  }
  /**
   * <pre>
   *  The usage for this operation.
   * </pre>
   *
   * <code>optional .Usage usage = 3;</code>
   */
  @java.lang.Override
  public io.pinecone.proto.UsageOrBuilder getUsageOrBuilder() {
    return usage_ == null ? io.pinecone.proto.Usage.getDefaultInstance() : usage_;
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
    com.google.protobuf.GeneratedMessageV3
      .serializeStringMapTo(
        output,
        internalGetVectors(),
        VectorsDefaultEntryHolder.defaultEntry,
        1);
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(namespace_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, namespace_);
    }
    if (((bitField0_ & 0x00000001) != 0)) {
      output.writeMessage(3, getUsage());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (java.util.Map.Entry<java.lang.String, io.pinecone.proto.Vector> entry
         : internalGetVectors().getMap().entrySet()) {
      com.google.protobuf.MapEntry<java.lang.String, io.pinecone.proto.Vector>
      vectors__ = VectorsDefaultEntryHolder.defaultEntry.newBuilderForType()
          .setKey(entry.getKey())
          .setValue(entry.getValue())
          .build();
      size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(1, vectors__);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(namespace_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, namespace_);
    }
    if (((bitField0_ & 0x00000001) != 0)) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(3, getUsage());
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
    if (!(obj instanceof io.pinecone.proto.FetchResponse)) {
      return super.equals(obj);
    }
    io.pinecone.proto.FetchResponse other = (io.pinecone.proto.FetchResponse) obj;

    if (!internalGetVectors().equals(
        other.internalGetVectors())) return false;
    if (!getNamespace()
        .equals(other.getNamespace())) return false;
    if (hasUsage() != other.hasUsage()) return false;
    if (hasUsage()) {
      if (!getUsage()
          .equals(other.getUsage())) return false;
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
    if (!internalGetVectors().getMap().isEmpty()) {
      hash = (37 * hash) + VECTORS_FIELD_NUMBER;
      hash = (53 * hash) + internalGetVectors().hashCode();
    }
    hash = (37 * hash) + NAMESPACE_FIELD_NUMBER;
    hash = (53 * hash) + getNamespace().hashCode();
    if (hasUsage()) {
      hash = (37 * hash) + USAGE_FIELD_NUMBER;
      hash = (53 * hash) + getUsage().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.pinecone.proto.FetchResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.pinecone.proto.FetchResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.pinecone.proto.FetchResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.pinecone.proto.FetchResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.pinecone.proto.FetchResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.pinecone.proto.FetchResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.pinecone.proto.FetchResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.pinecone.proto.FetchResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.pinecone.proto.FetchResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.pinecone.proto.FetchResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.pinecone.proto.FetchResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.pinecone.proto.FetchResponse parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(io.pinecone.proto.FetchResponse prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * <pre>
   * The response for the `fetch` operation.
   * </pre>
   *
   * Protobuf type {@code FetchResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:FetchResponse)
      io.pinecone.proto.FetchResponseOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.pinecone.proto.VectorServiceOuterClass.internal_static_FetchResponse_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapFieldReflectionAccessor internalGetMapFieldReflection(
        int number) {
      switch (number) {
        case 1:
          return internalGetVectors();
        default:
          throw new RuntimeException(
              "Invalid map field number: " + number);
      }
    }
    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapFieldReflectionAccessor internalGetMutableMapFieldReflection(
        int number) {
      switch (number) {
        case 1:
          return internalGetMutableVectors();
        default:
          throw new RuntimeException(
              "Invalid map field number: " + number);
      }
    }
    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.pinecone.proto.VectorServiceOuterClass.internal_static_FetchResponse_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.pinecone.proto.FetchResponse.class, io.pinecone.proto.FetchResponse.Builder.class);
    }

    // Construct using io.pinecone.proto.FetchResponse.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
        getUsageFieldBuilder();
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      internalGetMutableVectors().clear();
      namespace_ = "";
      usage_ = null;
      if (usageBuilder_ != null) {
        usageBuilder_.dispose();
        usageBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.pinecone.proto.VectorServiceOuterClass.internal_static_FetchResponse_descriptor;
    }

    @java.lang.Override
    public io.pinecone.proto.FetchResponse getDefaultInstanceForType() {
      return io.pinecone.proto.FetchResponse.getDefaultInstance();
    }

    @java.lang.Override
    public io.pinecone.proto.FetchResponse build() {
      io.pinecone.proto.FetchResponse result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.pinecone.proto.FetchResponse buildPartial() {
      io.pinecone.proto.FetchResponse result = new io.pinecone.proto.FetchResponse(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.pinecone.proto.FetchResponse result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.vectors_ = internalGetVectors().build(VectorsDefaultEntryHolder.defaultEntry);
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.namespace_ = namespace_;
      }
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.usage_ = usageBuilder_ == null
            ? usage_
            : usageBuilder_.build();
        to_bitField0_ |= 0x00000001;
      }
      result.bitField0_ |= to_bitField0_;
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.pinecone.proto.FetchResponse) {
        return mergeFrom((io.pinecone.proto.FetchResponse)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.pinecone.proto.FetchResponse other) {
      if (other == io.pinecone.proto.FetchResponse.getDefaultInstance()) return this;
      internalGetMutableVectors().mergeFrom(
          other.internalGetVectors());
      bitField0_ |= 0x00000001;
      if (!other.getNamespace().isEmpty()) {
        namespace_ = other.namespace_;
        bitField0_ |= 0x00000002;
        onChanged();
      }
      if (other.hasUsage()) {
        mergeUsage(other.getUsage());
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
              com.google.protobuf.MapEntry<java.lang.String, io.pinecone.proto.Vector>
              vectors__ = input.readMessage(
                  VectorsDefaultEntryHolder.defaultEntry.getParserForType(), extensionRegistry);
              internalGetMutableVectors().ensureBuilderMap().put(
                  vectors__.getKey(), vectors__.getValue());
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              namespace_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000002;
              break;
            } // case 18
            case 26: {
              input.readMessage(
                  getUsageFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000004;
              break;
            } // case 26
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

    private static final class VectorsConverter implements com.google.protobuf.MapFieldBuilder.Converter<java.lang.String, io.pinecone.proto.VectorOrBuilder, io.pinecone.proto.Vector> {
      @java.lang.Override
      public io.pinecone.proto.Vector build(io.pinecone.proto.VectorOrBuilder val) {
        if (val instanceof io.pinecone.proto.Vector) { return (io.pinecone.proto.Vector) val; }
        return ((io.pinecone.proto.Vector.Builder) val).build();
      }

      @java.lang.Override
      public com.google.protobuf.MapEntry<java.lang.String, io.pinecone.proto.Vector> defaultEntry() {
        return VectorsDefaultEntryHolder.defaultEntry;
      }
    };
    private static final VectorsConverter vectorsConverter = new VectorsConverter();

    private com.google.protobuf.MapFieldBuilder<
        java.lang.String, io.pinecone.proto.VectorOrBuilder, io.pinecone.proto.Vector, io.pinecone.proto.Vector.Builder> vectors_;
    private com.google.protobuf.MapFieldBuilder<java.lang.String, io.pinecone.proto.VectorOrBuilder, io.pinecone.proto.Vector, io.pinecone.proto.Vector.Builder>
        internalGetVectors() {
      if (vectors_ == null) {
        return new com.google.protobuf.MapFieldBuilder<>(vectorsConverter);
      }
      return vectors_;
    }
    private com.google.protobuf.MapFieldBuilder<java.lang.String, io.pinecone.proto.VectorOrBuilder, io.pinecone.proto.Vector, io.pinecone.proto.Vector.Builder>
        internalGetMutableVectors() {
      if (vectors_ == null) {
        vectors_ = new com.google.protobuf.MapFieldBuilder<>(vectorsConverter);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return vectors_;
    }
    public int getVectorsCount() {
      return internalGetVectors().ensureBuilderMap().size();
    }
    /**
     * <pre>
     * The fetched vectors, in the form of a map between the fetched ids and the fetched vectors
     * </pre>
     *
     * <code>map&lt;string, .Vector&gt; vectors = 1;</code>
     */
    @java.lang.Override
    public boolean containsVectors(
        java.lang.String key) {
      if (key == null) { throw new NullPointerException("map key"); }
      return internalGetVectors().ensureBuilderMap().containsKey(key);
    }
    /**
     * Use {@link #getVectorsMap()} instead.
     */
    @java.lang.Override
    @java.lang.Deprecated
    public java.util.Map<java.lang.String, io.pinecone.proto.Vector> getVectors() {
      return getVectorsMap();
    }
    /**
     * <pre>
     * The fetched vectors, in the form of a map between the fetched ids and the fetched vectors
     * </pre>
     *
     * <code>map&lt;string, .Vector&gt; vectors = 1;</code>
     */
    @java.lang.Override
    public java.util.Map<java.lang.String, io.pinecone.proto.Vector> getVectorsMap() {
      return internalGetVectors().getImmutableMap();
    }
    /**
     * <pre>
     * The fetched vectors, in the form of a map between the fetched ids and the fetched vectors
     * </pre>
     *
     * <code>map&lt;string, .Vector&gt; vectors = 1;</code>
     */
    @java.lang.Override
    public /* nullable */
io.pinecone.proto.Vector getVectorsOrDefault(
        java.lang.String key,
        /* nullable */
io.pinecone.proto.Vector defaultValue) {
      if (key == null) { throw new NullPointerException("map key"); }
      java.util.Map<java.lang.String, io.pinecone.proto.VectorOrBuilder> map = internalGetMutableVectors().ensureBuilderMap();
      return map.containsKey(key) ? vectorsConverter.build(map.get(key)) : defaultValue;
    }
    /**
     * <pre>
     * The fetched vectors, in the form of a map between the fetched ids and the fetched vectors
     * </pre>
     *
     * <code>map&lt;string, .Vector&gt; vectors = 1;</code>
     */
    @java.lang.Override
    public io.pinecone.proto.Vector getVectorsOrThrow(
        java.lang.String key) {
      if (key == null) { throw new NullPointerException("map key"); }
      java.util.Map<java.lang.String, io.pinecone.proto.VectorOrBuilder> map = internalGetMutableVectors().ensureBuilderMap();
      if (!map.containsKey(key)) {
        throw new java.lang.IllegalArgumentException();
      }
      return vectorsConverter.build(map.get(key));
    }
    public Builder clearVectors() {
      bitField0_ = (bitField0_ & ~0x00000001);
      internalGetMutableVectors().clear();
      return this;
    }
    /**
     * <pre>
     * The fetched vectors, in the form of a map between the fetched ids and the fetched vectors
     * </pre>
     *
     * <code>map&lt;string, .Vector&gt; vectors = 1;</code>
     */
    public Builder removeVectors(
        java.lang.String key) {
      if (key == null) { throw new NullPointerException("map key"); }
      internalGetMutableVectors().ensureBuilderMap()
          .remove(key);
      return this;
    }
    /**
     * Use alternate mutation accessors instead.
     */
    @java.lang.Deprecated
    public java.util.Map<java.lang.String, io.pinecone.proto.Vector>
        getMutableVectors() {
      bitField0_ |= 0x00000001;
      return internalGetMutableVectors().ensureMessageMap();
    }
    /**
     * <pre>
     * The fetched vectors, in the form of a map between the fetched ids and the fetched vectors
     * </pre>
     *
     * <code>map&lt;string, .Vector&gt; vectors = 1;</code>
     */
    public Builder putVectors(
        java.lang.String key,
        io.pinecone.proto.Vector value) {
      if (key == null) { throw new NullPointerException("map key"); }
      if (value == null) { throw new NullPointerException("map value"); }
      internalGetMutableVectors().ensureBuilderMap()
          .put(key, value);
      bitField0_ |= 0x00000001;
      return this;
    }
    /**
     * <pre>
     * The fetched vectors, in the form of a map between the fetched ids and the fetched vectors
     * </pre>
     *
     * <code>map&lt;string, .Vector&gt; vectors = 1;</code>
     */
    public Builder putAllVectors(
        java.util.Map<java.lang.String, io.pinecone.proto.Vector> values) {
      for (java.util.Map.Entry<java.lang.String, io.pinecone.proto.Vector> e : values.entrySet()) {
        if (e.getKey() == null || e.getValue() == null) {
          throw new NullPointerException();
        }
      }
      internalGetMutableVectors().ensureBuilderMap()
          .putAll(values);
      bitField0_ |= 0x00000001;
      return this;
    }
    /**
     * <pre>
     * The fetched vectors, in the form of a map between the fetched ids and the fetched vectors
     * </pre>
     *
     * <code>map&lt;string, .Vector&gt; vectors = 1;</code>
     */
    public io.pinecone.proto.Vector.Builder putVectorsBuilderIfAbsent(
        java.lang.String key) {
      java.util.Map<java.lang.String, io.pinecone.proto.VectorOrBuilder> builderMap = internalGetMutableVectors().ensureBuilderMap();
      io.pinecone.proto.VectorOrBuilder entry = builderMap.get(key);
      if (entry == null) {
        entry = io.pinecone.proto.Vector.newBuilder();
        builderMap.put(key, entry);
      }
      if (entry instanceof io.pinecone.proto.Vector) {
        entry = ((io.pinecone.proto.Vector) entry).toBuilder();
        builderMap.put(key, entry);
      }
      return (io.pinecone.proto.Vector.Builder) entry;
    }

    private java.lang.Object namespace_ = "";
    /**
     * <pre>
     * The namespace of the vectors.
     * </pre>
     *
     * <code>string namespace = 2;</code>
     * @return The namespace.
     */
    public java.lang.String getNamespace() {
      java.lang.Object ref = namespace_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        namespace_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * The namespace of the vectors.
     * </pre>
     *
     * <code>string namespace = 2;</code>
     * @return The bytes for namespace.
     */
    public com.google.protobuf.ByteString
        getNamespaceBytes() {
      java.lang.Object ref = namespace_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        namespace_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * The namespace of the vectors.
     * </pre>
     *
     * <code>string namespace = 2;</code>
     * @param value The namespace to set.
     * @return This builder for chaining.
     */
    public Builder setNamespace(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      namespace_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The namespace of the vectors.
     * </pre>
     *
     * <code>string namespace = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearNamespace() {
      namespace_ = getDefaultInstance().getNamespace();
      bitField0_ = (bitField0_ & ~0x00000002);
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The namespace of the vectors.
     * </pre>
     *
     * <code>string namespace = 2;</code>
     * @param value The bytes for namespace to set.
     * @return This builder for chaining.
     */
    public Builder setNamespaceBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      namespace_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }

    private io.pinecone.proto.Usage usage_;
    private com.google.protobuf.SingleFieldBuilderV3<
        io.pinecone.proto.Usage, io.pinecone.proto.Usage.Builder, io.pinecone.proto.UsageOrBuilder> usageBuilder_;
    /**
     * <pre>
     *  The usage for this operation.
     * </pre>
     *
     * <code>optional .Usage usage = 3;</code>
     * @return Whether the usage field is set.
     */
    public boolean hasUsage() {
      return ((bitField0_ & 0x00000004) != 0);
    }
    /**
     * <pre>
     *  The usage for this operation.
     * </pre>
     *
     * <code>optional .Usage usage = 3;</code>
     * @return The usage.
     */
    public io.pinecone.proto.Usage getUsage() {
      if (usageBuilder_ == null) {
        return usage_ == null ? io.pinecone.proto.Usage.getDefaultInstance() : usage_;
      } else {
        return usageBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     *  The usage for this operation.
     * </pre>
     *
     * <code>optional .Usage usage = 3;</code>
     */
    public Builder setUsage(io.pinecone.proto.Usage value) {
      if (usageBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        usage_ = value;
      } else {
        usageBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <pre>
     *  The usage for this operation.
     * </pre>
     *
     * <code>optional .Usage usage = 3;</code>
     */
    public Builder setUsage(
        io.pinecone.proto.Usage.Builder builderForValue) {
      if (usageBuilder_ == null) {
        usage_ = builderForValue.build();
      } else {
        usageBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <pre>
     *  The usage for this operation.
     * </pre>
     *
     * <code>optional .Usage usage = 3;</code>
     */
    public Builder mergeUsage(io.pinecone.proto.Usage value) {
      if (usageBuilder_ == null) {
        if (((bitField0_ & 0x00000004) != 0) &&
          usage_ != null &&
          usage_ != io.pinecone.proto.Usage.getDefaultInstance()) {
          getUsageBuilder().mergeFrom(value);
        } else {
          usage_ = value;
        }
      } else {
        usageBuilder_.mergeFrom(value);
      }
      if (usage_ != null) {
        bitField0_ |= 0x00000004;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     *  The usage for this operation.
     * </pre>
     *
     * <code>optional .Usage usage = 3;</code>
     */
    public Builder clearUsage() {
      bitField0_ = (bitField0_ & ~0x00000004);
      usage_ = null;
      if (usageBuilder_ != null) {
        usageBuilder_.dispose();
        usageBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     *  The usage for this operation.
     * </pre>
     *
     * <code>optional .Usage usage = 3;</code>
     */
    public io.pinecone.proto.Usage.Builder getUsageBuilder() {
      bitField0_ |= 0x00000004;
      onChanged();
      return getUsageFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     *  The usage for this operation.
     * </pre>
     *
     * <code>optional .Usage usage = 3;</code>
     */
    public io.pinecone.proto.UsageOrBuilder getUsageOrBuilder() {
      if (usageBuilder_ != null) {
        return usageBuilder_.getMessageOrBuilder();
      } else {
        return usage_ == null ?
            io.pinecone.proto.Usage.getDefaultInstance() : usage_;
      }
    }
    /**
     * <pre>
     *  The usage for this operation.
     * </pre>
     *
     * <code>optional .Usage usage = 3;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        io.pinecone.proto.Usage, io.pinecone.proto.Usage.Builder, io.pinecone.proto.UsageOrBuilder> 
        getUsageFieldBuilder() {
      if (usageBuilder_ == null) {
        usageBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            io.pinecone.proto.Usage, io.pinecone.proto.Usage.Builder, io.pinecone.proto.UsageOrBuilder>(
                getUsage(),
                getParentForChildren(),
                isClean());
        usage_ = null;
      }
      return usageBuilder_;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:FetchResponse)
  }

  // @@protoc_insertion_point(class_scope:FetchResponse)
  private static final io.pinecone.proto.FetchResponse DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.pinecone.proto.FetchResponse();
  }

  public static io.pinecone.proto.FetchResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<FetchResponse>
      PARSER = new com.google.protobuf.AbstractParser<FetchResponse>() {
    @java.lang.Override
    public FetchResponse parsePartialFrom(
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

  public static com.google.protobuf.Parser<FetchResponse> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<FetchResponse> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.pinecone.proto.FetchResponse getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

