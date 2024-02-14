// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: vector_service.proto

// Protobuf Java Version: 3.25.2
package io.pinecone.proto;

/**
 * <pre>
 * This is a container to hold mutating vector requests. This is not actually used
 * in any public APIs.
 * </pre>
 *
 * Protobuf type {@code RequestUnion}
 */
public final class RequestUnion extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:RequestUnion)
    RequestUnionOrBuilder {
private static final long serialVersionUID = 0L;
  // Use RequestUnion.newBuilder() to construct.
  private RequestUnion(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private RequestUnion() {
  }

  @Override
  @SuppressWarnings({"unused"})
  protected Object newInstance(
      UnusedPrivateParameter unused) {
    return new RequestUnion();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return VectorServiceOuterClass.internal_static_RequestUnion_descriptor;
  }

  @Override
  protected FieldAccessorTable
      internalGetFieldAccessorTable() {
    return VectorServiceOuterClass.internal_static_RequestUnion_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            RequestUnion.class, Builder.class);
  }

  private int requestUnionInnerCase_ = 0;
  @SuppressWarnings("serial")
  private Object requestUnionInner_;
  public enum RequestUnionInnerCase
      implements com.google.protobuf.Internal.EnumLite,
          InternalOneOfEnum {
    UPSERT(1),
    DELETE(2),
    UPDATE(3),
    REQUESTUNIONINNER_NOT_SET(0);
    private final int value;
    private RequestUnionInnerCase(int value) {
      this.value = value;
    }
    /**
     * @param value The number of the enum to look for.
     * @return The enum associated with the given number.
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @Deprecated
    public static RequestUnionInnerCase valueOf(int value) {
      return forNumber(value);
    }

    public static RequestUnionInnerCase forNumber(int value) {
      switch (value) {
        case 1: return UPSERT;
        case 2: return DELETE;
        case 3: return UPDATE;
        case 0: return REQUESTUNIONINNER_NOT_SET;
        default: return null;
      }
    }
    public int getNumber() {
      return this.value;
    }
  };

  public RequestUnionInnerCase
  getRequestUnionInnerCase() {
    return RequestUnionInnerCase.forNumber(
        requestUnionInnerCase_);
  }

  public static final int UPSERT_FIELD_NUMBER = 1;
  /**
   * <code>.UpsertRequest upsert = 1;</code>
   * @return Whether the upsert field is set.
   */
  @Override
  public boolean hasUpsert() {
    return requestUnionInnerCase_ == 1;
  }
  /**
   * <code>.UpsertRequest upsert = 1;</code>
   * @return The upsert.
   */
  @Override
  public UpsertRequest getUpsert() {
    if (requestUnionInnerCase_ == 1) {
       return (UpsertRequest) requestUnionInner_;
    }
    return UpsertRequest.getDefaultInstance();
  }
  /**
   * <code>.UpsertRequest upsert = 1;</code>
   */
  @Override
  public UpsertRequestOrBuilder getUpsertOrBuilder() {
    if (requestUnionInnerCase_ == 1) {
       return (UpsertRequest) requestUnionInner_;
    }
    return UpsertRequest.getDefaultInstance();
  }

  public static final int DELETE_FIELD_NUMBER = 2;
  /**
   * <code>.DeleteRequest delete = 2;</code>
   * @return Whether the delete field is set.
   */
  @Override
  public boolean hasDelete() {
    return requestUnionInnerCase_ == 2;
  }
  /**
   * <code>.DeleteRequest delete = 2;</code>
   * @return The delete.
   */
  @Override
  public DeleteRequest getDelete() {
    if (requestUnionInnerCase_ == 2) {
       return (DeleteRequest) requestUnionInner_;
    }
    return DeleteRequest.getDefaultInstance();
  }
  /**
   * <code>.DeleteRequest delete = 2;</code>
   */
  @Override
  public DeleteRequestOrBuilder getDeleteOrBuilder() {
    if (requestUnionInnerCase_ == 2) {
       return (DeleteRequest) requestUnionInner_;
    }
    return DeleteRequest.getDefaultInstance();
  }

  public static final int UPDATE_FIELD_NUMBER = 3;
  /**
   * <code>.UpdateRequest update = 3;</code>
   * @return Whether the update field is set.
   */
  @Override
  public boolean hasUpdate() {
    return requestUnionInnerCase_ == 3;
  }
  /**
   * <code>.UpdateRequest update = 3;</code>
   * @return The update.
   */
  @Override
  public UpdateRequest getUpdate() {
    if (requestUnionInnerCase_ == 3) {
       return (UpdateRequest) requestUnionInner_;
    }
    return UpdateRequest.getDefaultInstance();
  }
  /**
   * <code>.UpdateRequest update = 3;</code>
   */
  @Override
  public UpdateRequestOrBuilder getUpdateOrBuilder() {
    if (requestUnionInnerCase_ == 3) {
       return (UpdateRequest) requestUnionInner_;
    }
    return UpdateRequest.getDefaultInstance();
  }

  private byte memoizedIsInitialized = -1;
  @Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (requestUnionInnerCase_ == 1) {
      output.writeMessage(1, (UpsertRequest) requestUnionInner_);
    }
    if (requestUnionInnerCase_ == 2) {
      output.writeMessage(2, (DeleteRequest) requestUnionInner_);
    }
    if (requestUnionInnerCase_ == 3) {
      output.writeMessage(3, (UpdateRequest) requestUnionInner_);
    }
    getUnknownFields().writeTo(output);
  }

  @Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (requestUnionInnerCase_ == 1) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, (UpsertRequest) requestUnionInner_);
    }
    if (requestUnionInnerCase_ == 2) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, (DeleteRequest) requestUnionInner_);
    }
    if (requestUnionInnerCase_ == 3) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(3, (UpdateRequest) requestUnionInner_);
    }
    size += getUnknownFields().getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof RequestUnion)) {
      return super.equals(obj);
    }
    RequestUnion other = (RequestUnion) obj;

    if (!getRequestUnionInnerCase().equals(other.getRequestUnionInnerCase())) return false;
    switch (requestUnionInnerCase_) {
      case 1:
        if (!getUpsert()
            .equals(other.getUpsert())) return false;
        break;
      case 2:
        if (!getDelete()
            .equals(other.getDelete())) return false;
        break;
      case 3:
        if (!getUpdate()
            .equals(other.getUpdate())) return false;
        break;
      case 0:
      default:
    }
    if (!getUnknownFields().equals(other.getUnknownFields())) return false;
    return true;
  }

  @Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    switch (requestUnionInnerCase_) {
      case 1:
        hash = (37 * hash) + UPSERT_FIELD_NUMBER;
        hash = (53 * hash) + getUpsert().hashCode();
        break;
      case 2:
        hash = (37 * hash) + DELETE_FIELD_NUMBER;
        hash = (53 * hash) + getDelete().hashCode();
        break;
      case 3:
        hash = (37 * hash) + UPDATE_FIELD_NUMBER;
        hash = (53 * hash) + getUpdate().hashCode();
        break;
      case 0:
      default:
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static RequestUnion parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static RequestUnion parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static RequestUnion parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static RequestUnion parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static RequestUnion parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static RequestUnion parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static RequestUnion parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static RequestUnion parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static RequestUnion parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static RequestUnion parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static RequestUnion parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static RequestUnion parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(RequestUnion prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @Override
  protected Builder newBuilderForType(
      BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * <pre>
   * This is a container to hold mutating vector requests. This is not actually used
   * in any public APIs.
   * </pre>
   *
   * Protobuf type {@code RequestUnion}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:RequestUnion)
      RequestUnionOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return VectorServiceOuterClass.internal_static_RequestUnion_descriptor;
    }

    @Override
    protected FieldAccessorTable
        internalGetFieldAccessorTable() {
      return VectorServiceOuterClass.internal_static_RequestUnion_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              RequestUnion.class, Builder.class);
    }

    // Construct using io.pinecone.proto.RequestUnion.newBuilder()
    private Builder() {

    }

    private Builder(
        BuilderParent parent) {
      super(parent);

    }
    @Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      if (upsertBuilder_ != null) {
        upsertBuilder_.clear();
      }
      if (deleteBuilder_ != null) {
        deleteBuilder_.clear();
      }
      if (updateBuilder_ != null) {
        updateBuilder_.clear();
      }
      requestUnionInnerCase_ = 0;
      requestUnionInner_ = null;
      return this;
    }

    @Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return VectorServiceOuterClass.internal_static_RequestUnion_descriptor;
    }

    @Override
    public RequestUnion getDefaultInstanceForType() {
      return RequestUnion.getDefaultInstance();
    }

    @Override
    public RequestUnion build() {
      RequestUnion result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @Override
    public RequestUnion buildPartial() {
      RequestUnion result = new RequestUnion(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      buildPartialOneofs(result);
      onBuilt();
      return result;
    }

    private void buildPartial0(RequestUnion result) {
      int from_bitField0_ = bitField0_;
    }

    private void buildPartialOneofs(RequestUnion result) {
      result.requestUnionInnerCase_ = requestUnionInnerCase_;
      result.requestUnionInner_ = this.requestUnionInner_;
      if (requestUnionInnerCase_ == 1 &&
          upsertBuilder_ != null) {
        result.requestUnionInner_ = upsertBuilder_.build();
      }
      if (requestUnionInnerCase_ == 2 &&
          deleteBuilder_ != null) {
        result.requestUnionInner_ = deleteBuilder_.build();
      }
      if (requestUnionInnerCase_ == 3 &&
          updateBuilder_ != null) {
        result.requestUnionInner_ = updateBuilder_.build();
      }
    }

    @Override
    public Builder clone() {
      return super.clone();
    }
    @Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return super.setField(field, value);
    }
    @Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return super.addRepeatedField(field, value);
    }
    @Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof RequestUnion) {
        return mergeFrom((RequestUnion)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(RequestUnion other) {
      if (other == RequestUnion.getDefaultInstance()) return this;
      switch (other.getRequestUnionInnerCase()) {
        case UPSERT: {
          mergeUpsert(other.getUpsert());
          break;
        }
        case DELETE: {
          mergeDelete(other.getDelete());
          break;
        }
        case UPDATE: {
          mergeUpdate(other.getUpdate());
          break;
        }
        case REQUESTUNIONINNER_NOT_SET: {
          break;
        }
      }
      this.mergeUnknownFields(other.getUnknownFields());
      onChanged();
      return this;
    }

    @Override
    public final boolean isInitialized() {
      return true;
    }

    @Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      if (extensionRegistry == null) {
        throw new NullPointerException();
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
              input.readMessage(
                  getUpsertFieldBuilder().getBuilder(),
                  extensionRegistry);
              requestUnionInnerCase_ = 1;
              break;
            } // case 10
            case 18: {
              input.readMessage(
                  getDeleteFieldBuilder().getBuilder(),
                  extensionRegistry);
              requestUnionInnerCase_ = 2;
              break;
            } // case 18
            case 26: {
              input.readMessage(
                  getUpdateFieldBuilder().getBuilder(),
                  extensionRegistry);
              requestUnionInnerCase_ = 3;
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
    private int requestUnionInnerCase_ = 0;
    private Object requestUnionInner_;
    public RequestUnionInnerCase
        getRequestUnionInnerCase() {
      return RequestUnionInnerCase.forNumber(
          requestUnionInnerCase_);
    }

    public Builder clearRequestUnionInner() {
      requestUnionInnerCase_ = 0;
      requestUnionInner_ = null;
      onChanged();
      return this;
    }

    private int bitField0_;

    private com.google.protobuf.SingleFieldBuilderV3<
        UpsertRequest, UpsertRequest.Builder, UpsertRequestOrBuilder> upsertBuilder_;
    /**
     * <code>.UpsertRequest upsert = 1;</code>
     * @return Whether the upsert field is set.
     */
    @Override
    public boolean hasUpsert() {
      return requestUnionInnerCase_ == 1;
    }
    /**
     * <code>.UpsertRequest upsert = 1;</code>
     * @return The upsert.
     */
    @Override
    public UpsertRequest getUpsert() {
      if (upsertBuilder_ == null) {
        if (requestUnionInnerCase_ == 1) {
          return (UpsertRequest) requestUnionInner_;
        }
        return UpsertRequest.getDefaultInstance();
      } else {
        if (requestUnionInnerCase_ == 1) {
          return upsertBuilder_.getMessage();
        }
        return UpsertRequest.getDefaultInstance();
      }
    }
    /**
     * <code>.UpsertRequest upsert = 1;</code>
     */
    public Builder setUpsert(UpsertRequest value) {
      if (upsertBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        requestUnionInner_ = value;
        onChanged();
      } else {
        upsertBuilder_.setMessage(value);
      }
      requestUnionInnerCase_ = 1;
      return this;
    }
    /**
     * <code>.UpsertRequest upsert = 1;</code>
     */
    public Builder setUpsert(
        UpsertRequest.Builder builderForValue) {
      if (upsertBuilder_ == null) {
        requestUnionInner_ = builderForValue.build();
        onChanged();
      } else {
        upsertBuilder_.setMessage(builderForValue.build());
      }
      requestUnionInnerCase_ = 1;
      return this;
    }
    /**
     * <code>.UpsertRequest upsert = 1;</code>
     */
    public Builder mergeUpsert(UpsertRequest value) {
      if (upsertBuilder_ == null) {
        if (requestUnionInnerCase_ == 1 &&
            requestUnionInner_ != UpsertRequest.getDefaultInstance()) {
          requestUnionInner_ = UpsertRequest.newBuilder((UpsertRequest) requestUnionInner_)
              .mergeFrom(value).buildPartial();
        } else {
          requestUnionInner_ = value;
        }
        onChanged();
      } else {
        if (requestUnionInnerCase_ == 1) {
          upsertBuilder_.mergeFrom(value);
        } else {
          upsertBuilder_.setMessage(value);
        }
      }
      requestUnionInnerCase_ = 1;
      return this;
    }
    /**
     * <code>.UpsertRequest upsert = 1;</code>
     */
    public Builder clearUpsert() {
      if (upsertBuilder_ == null) {
        if (requestUnionInnerCase_ == 1) {
          requestUnionInnerCase_ = 0;
          requestUnionInner_ = null;
          onChanged();
        }
      } else {
        if (requestUnionInnerCase_ == 1) {
          requestUnionInnerCase_ = 0;
          requestUnionInner_ = null;
        }
        upsertBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>.UpsertRequest upsert = 1;</code>
     */
    public UpsertRequest.Builder getUpsertBuilder() {
      return getUpsertFieldBuilder().getBuilder();
    }
    /**
     * <code>.UpsertRequest upsert = 1;</code>
     */
    @Override
    public UpsertRequestOrBuilder getUpsertOrBuilder() {
      if ((requestUnionInnerCase_ == 1) && (upsertBuilder_ != null)) {
        return upsertBuilder_.getMessageOrBuilder();
      } else {
        if (requestUnionInnerCase_ == 1) {
          return (UpsertRequest) requestUnionInner_;
        }
        return UpsertRequest.getDefaultInstance();
      }
    }
    /**
     * <code>.UpsertRequest upsert = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        UpsertRequest, UpsertRequest.Builder, UpsertRequestOrBuilder>
        getUpsertFieldBuilder() {
      if (upsertBuilder_ == null) {
        if (!(requestUnionInnerCase_ == 1)) {
          requestUnionInner_ = UpsertRequest.getDefaultInstance();
        }
        upsertBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            UpsertRequest, UpsertRequest.Builder, UpsertRequestOrBuilder>(
                (UpsertRequest) requestUnionInner_,
                getParentForChildren(),
                isClean());
        requestUnionInner_ = null;
      }
      requestUnionInnerCase_ = 1;
      onChanged();
      return upsertBuilder_;
    }

    private com.google.protobuf.SingleFieldBuilderV3<
        DeleteRequest, DeleteRequest.Builder, DeleteRequestOrBuilder> deleteBuilder_;
    /**
     * <code>.DeleteRequest delete = 2;</code>
     * @return Whether the delete field is set.
     */
    @Override
    public boolean hasDelete() {
      return requestUnionInnerCase_ == 2;
    }
    /**
     * <code>.DeleteRequest delete = 2;</code>
     * @return The delete.
     */
    @Override
    public DeleteRequest getDelete() {
      if (deleteBuilder_ == null) {
        if (requestUnionInnerCase_ == 2) {
          return (DeleteRequest) requestUnionInner_;
        }
        return DeleteRequest.getDefaultInstance();
      } else {
        if (requestUnionInnerCase_ == 2) {
          return deleteBuilder_.getMessage();
        }
        return DeleteRequest.getDefaultInstance();
      }
    }
    /**
     * <code>.DeleteRequest delete = 2;</code>
     */
    public Builder setDelete(DeleteRequest value) {
      if (deleteBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        requestUnionInner_ = value;
        onChanged();
      } else {
        deleteBuilder_.setMessage(value);
      }
      requestUnionInnerCase_ = 2;
      return this;
    }
    /**
     * <code>.DeleteRequest delete = 2;</code>
     */
    public Builder setDelete(
        DeleteRequest.Builder builderForValue) {
      if (deleteBuilder_ == null) {
        requestUnionInner_ = builderForValue.build();
        onChanged();
      } else {
        deleteBuilder_.setMessage(builderForValue.build());
      }
      requestUnionInnerCase_ = 2;
      return this;
    }
    /**
     * <code>.DeleteRequest delete = 2;</code>
     */
    public Builder mergeDelete(DeleteRequest value) {
      if (deleteBuilder_ == null) {
        if (requestUnionInnerCase_ == 2 &&
            requestUnionInner_ != DeleteRequest.getDefaultInstance()) {
          requestUnionInner_ = DeleteRequest.newBuilder((DeleteRequest) requestUnionInner_)
              .mergeFrom(value).buildPartial();
        } else {
          requestUnionInner_ = value;
        }
        onChanged();
      } else {
        if (requestUnionInnerCase_ == 2) {
          deleteBuilder_.mergeFrom(value);
        } else {
          deleteBuilder_.setMessage(value);
        }
      }
      requestUnionInnerCase_ = 2;
      return this;
    }
    /**
     * <code>.DeleteRequest delete = 2;</code>
     */
    public Builder clearDelete() {
      if (deleteBuilder_ == null) {
        if (requestUnionInnerCase_ == 2) {
          requestUnionInnerCase_ = 0;
          requestUnionInner_ = null;
          onChanged();
        }
      } else {
        if (requestUnionInnerCase_ == 2) {
          requestUnionInnerCase_ = 0;
          requestUnionInner_ = null;
        }
        deleteBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>.DeleteRequest delete = 2;</code>
     */
    public DeleteRequest.Builder getDeleteBuilder() {
      return getDeleteFieldBuilder().getBuilder();
    }
    /**
     * <code>.DeleteRequest delete = 2;</code>
     */
    @Override
    public DeleteRequestOrBuilder getDeleteOrBuilder() {
      if ((requestUnionInnerCase_ == 2) && (deleteBuilder_ != null)) {
        return deleteBuilder_.getMessageOrBuilder();
      } else {
        if (requestUnionInnerCase_ == 2) {
          return (DeleteRequest) requestUnionInner_;
        }
        return DeleteRequest.getDefaultInstance();
      }
    }
    /**
     * <code>.DeleteRequest delete = 2;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        DeleteRequest, DeleteRequest.Builder, DeleteRequestOrBuilder>
        getDeleteFieldBuilder() {
      if (deleteBuilder_ == null) {
        if (!(requestUnionInnerCase_ == 2)) {
          requestUnionInner_ = DeleteRequest.getDefaultInstance();
        }
        deleteBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            DeleteRequest, DeleteRequest.Builder, DeleteRequestOrBuilder>(
                (DeleteRequest) requestUnionInner_,
                getParentForChildren(),
                isClean());
        requestUnionInner_ = null;
      }
      requestUnionInnerCase_ = 2;
      onChanged();
      return deleteBuilder_;
    }

    private com.google.protobuf.SingleFieldBuilderV3<
        UpdateRequest, UpdateRequest.Builder, UpdateRequestOrBuilder> updateBuilder_;
    /**
     * <code>.UpdateRequest update = 3;</code>
     * @return Whether the update field is set.
     */
    @Override
    public boolean hasUpdate() {
      return requestUnionInnerCase_ == 3;
    }
    /**
     * <code>.UpdateRequest update = 3;</code>
     * @return The update.
     */
    @Override
    public UpdateRequest getUpdate() {
      if (updateBuilder_ == null) {
        if (requestUnionInnerCase_ == 3) {
          return (UpdateRequest) requestUnionInner_;
        }
        return UpdateRequest.getDefaultInstance();
      } else {
        if (requestUnionInnerCase_ == 3) {
          return updateBuilder_.getMessage();
        }
        return UpdateRequest.getDefaultInstance();
      }
    }
    /**
     * <code>.UpdateRequest update = 3;</code>
     */
    public Builder setUpdate(UpdateRequest value) {
      if (updateBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        requestUnionInner_ = value;
        onChanged();
      } else {
        updateBuilder_.setMessage(value);
      }
      requestUnionInnerCase_ = 3;
      return this;
    }
    /**
     * <code>.UpdateRequest update = 3;</code>
     */
    public Builder setUpdate(
        UpdateRequest.Builder builderForValue) {
      if (updateBuilder_ == null) {
        requestUnionInner_ = builderForValue.build();
        onChanged();
      } else {
        updateBuilder_.setMessage(builderForValue.build());
      }
      requestUnionInnerCase_ = 3;
      return this;
    }
    /**
     * <code>.UpdateRequest update = 3;</code>
     */
    public Builder mergeUpdate(UpdateRequest value) {
      if (updateBuilder_ == null) {
        if (requestUnionInnerCase_ == 3 &&
            requestUnionInner_ != UpdateRequest.getDefaultInstance()) {
          requestUnionInner_ = UpdateRequest.newBuilder((UpdateRequest) requestUnionInner_)
              .mergeFrom(value).buildPartial();
        } else {
          requestUnionInner_ = value;
        }
        onChanged();
      } else {
        if (requestUnionInnerCase_ == 3) {
          updateBuilder_.mergeFrom(value);
        } else {
          updateBuilder_.setMessage(value);
        }
      }
      requestUnionInnerCase_ = 3;
      return this;
    }
    /**
     * <code>.UpdateRequest update = 3;</code>
     */
    public Builder clearUpdate() {
      if (updateBuilder_ == null) {
        if (requestUnionInnerCase_ == 3) {
          requestUnionInnerCase_ = 0;
          requestUnionInner_ = null;
          onChanged();
        }
      } else {
        if (requestUnionInnerCase_ == 3) {
          requestUnionInnerCase_ = 0;
          requestUnionInner_ = null;
        }
        updateBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>.UpdateRequest update = 3;</code>
     */
    public UpdateRequest.Builder getUpdateBuilder() {
      return getUpdateFieldBuilder().getBuilder();
    }
    /**
     * <code>.UpdateRequest update = 3;</code>
     */
    @Override
    public UpdateRequestOrBuilder getUpdateOrBuilder() {
      if ((requestUnionInnerCase_ == 3) && (updateBuilder_ != null)) {
        return updateBuilder_.getMessageOrBuilder();
      } else {
        if (requestUnionInnerCase_ == 3) {
          return (UpdateRequest) requestUnionInner_;
        }
        return UpdateRequest.getDefaultInstance();
      }
    }
    /**
     * <code>.UpdateRequest update = 3;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        UpdateRequest, UpdateRequest.Builder, UpdateRequestOrBuilder>
        getUpdateFieldBuilder() {
      if (updateBuilder_ == null) {
        if (!(requestUnionInnerCase_ == 3)) {
          requestUnionInner_ = UpdateRequest.getDefaultInstance();
        }
        updateBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            UpdateRequest, UpdateRequest.Builder, UpdateRequestOrBuilder>(
                (UpdateRequest) requestUnionInner_,
                getParentForChildren(),
                isClean());
        requestUnionInner_ = null;
      }
      requestUnionInnerCase_ = 3;
      onChanged();
      return updateBuilder_;
    }
    @Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:RequestUnion)
  }

  // @@protoc_insertion_point(class_scope:RequestUnion)
  private static final RequestUnion DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new RequestUnion();
  }

  public static RequestUnion getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<RequestUnion>
      PARSER = new com.google.protobuf.AbstractParser<RequestUnion>() {
    @Override
    public RequestUnion parsePartialFrom(
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

  public static com.google.protobuf.Parser<RequestUnion> parser() {
    return PARSER;
  }

  @Override
  public com.google.protobuf.Parser<RequestUnion> getParserForType() {
    return PARSER;
  }

  @Override
  public RequestUnion getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

