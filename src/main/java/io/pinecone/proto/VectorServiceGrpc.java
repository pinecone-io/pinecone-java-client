package io.pinecone.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * The `VectorService` interface is exposed by Pinecone's vector index services.
 * This service could also be called a `gRPC` service or a `REST`-like api.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.47.0)",
    comments = "Source: vector_service.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class VectorServiceGrpc {

  private VectorServiceGrpc() {}

  public static final String SERVICE_NAME = "VectorService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<io.pinecone.proto.UpsertRequest,
      io.pinecone.proto.UpsertResponse> getUpsertMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Upsert",
      requestType = io.pinecone.proto.UpsertRequest.class,
      responseType = io.pinecone.proto.UpsertResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.pinecone.proto.UpsertRequest,
      io.pinecone.proto.UpsertResponse> getUpsertMethod() {
    io.grpc.MethodDescriptor<io.pinecone.proto.UpsertRequest, io.pinecone.proto.UpsertResponse> getUpsertMethod;
    if ((getUpsertMethod = VectorServiceGrpc.getUpsertMethod) == null) {
      synchronized (VectorServiceGrpc.class) {
        if ((getUpsertMethod = VectorServiceGrpc.getUpsertMethod) == null) {
          VectorServiceGrpc.getUpsertMethod = getUpsertMethod =
              io.grpc.MethodDescriptor.<io.pinecone.proto.UpsertRequest, io.pinecone.proto.UpsertResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Upsert"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.pinecone.proto.UpsertRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.pinecone.proto.UpsertResponse.getDefaultInstance()))
              .setSchemaDescriptor(new VectorServiceMethodDescriptorSupplier("Upsert"))
              .build();
        }
      }
    }
    return getUpsertMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.pinecone.proto.DeleteRequest,
      io.pinecone.proto.DeleteResponse> getDeleteMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Delete",
      requestType = io.pinecone.proto.DeleteRequest.class,
      responseType = io.pinecone.proto.DeleteResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.pinecone.proto.DeleteRequest,
      io.pinecone.proto.DeleteResponse> getDeleteMethod() {
    io.grpc.MethodDescriptor<io.pinecone.proto.DeleteRequest, io.pinecone.proto.DeleteResponse> getDeleteMethod;
    if ((getDeleteMethod = VectorServiceGrpc.getDeleteMethod) == null) {
      synchronized (VectorServiceGrpc.class) {
        if ((getDeleteMethod = VectorServiceGrpc.getDeleteMethod) == null) {
          VectorServiceGrpc.getDeleteMethod = getDeleteMethod =
              io.grpc.MethodDescriptor.<io.pinecone.proto.DeleteRequest, io.pinecone.proto.DeleteResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Delete"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.pinecone.proto.DeleteRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.pinecone.proto.DeleteResponse.getDefaultInstance()))
              .setSchemaDescriptor(new VectorServiceMethodDescriptorSupplier("Delete"))
              .build();
        }
      }
    }
    return getDeleteMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.pinecone.proto.FetchRequest,
      io.pinecone.proto.FetchResponse> getFetchMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Fetch",
      requestType = io.pinecone.proto.FetchRequest.class,
      responseType = io.pinecone.proto.FetchResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.pinecone.proto.FetchRequest,
      io.pinecone.proto.FetchResponse> getFetchMethod() {
    io.grpc.MethodDescriptor<io.pinecone.proto.FetchRequest, io.pinecone.proto.FetchResponse> getFetchMethod;
    if ((getFetchMethod = VectorServiceGrpc.getFetchMethod) == null) {
      synchronized (VectorServiceGrpc.class) {
        if ((getFetchMethod = VectorServiceGrpc.getFetchMethod) == null) {
          VectorServiceGrpc.getFetchMethod = getFetchMethod =
              io.grpc.MethodDescriptor.<io.pinecone.proto.FetchRequest, io.pinecone.proto.FetchResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Fetch"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.pinecone.proto.FetchRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.pinecone.proto.FetchResponse.getDefaultInstance()))
              .setSchemaDescriptor(new VectorServiceMethodDescriptorSupplier("Fetch"))
              .build();
        }
      }
    }
    return getFetchMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.pinecone.proto.QueryRequest,
      io.pinecone.proto.QueryResponse> getQueryMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Query",
      requestType = io.pinecone.proto.QueryRequest.class,
      responseType = io.pinecone.proto.QueryResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.pinecone.proto.QueryRequest,
      io.pinecone.proto.QueryResponse> getQueryMethod() {
    io.grpc.MethodDescriptor<io.pinecone.proto.QueryRequest, io.pinecone.proto.QueryResponse> getQueryMethod;
    if ((getQueryMethod = VectorServiceGrpc.getQueryMethod) == null) {
      synchronized (VectorServiceGrpc.class) {
        if ((getQueryMethod = VectorServiceGrpc.getQueryMethod) == null) {
          VectorServiceGrpc.getQueryMethod = getQueryMethod =
              io.grpc.MethodDescriptor.<io.pinecone.proto.QueryRequest, io.pinecone.proto.QueryResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Query"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.pinecone.proto.QueryRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.pinecone.proto.QueryResponse.getDefaultInstance()))
              .setSchemaDescriptor(new VectorServiceMethodDescriptorSupplier("Query"))
              .build();
        }
      }
    }
    return getQueryMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.pinecone.proto.UpdateRequest,
      io.pinecone.proto.UpdateResponse> getUpdateMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Update",
      requestType = io.pinecone.proto.UpdateRequest.class,
      responseType = io.pinecone.proto.UpdateResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.pinecone.proto.UpdateRequest,
      io.pinecone.proto.UpdateResponse> getUpdateMethod() {
    io.grpc.MethodDescriptor<io.pinecone.proto.UpdateRequest, io.pinecone.proto.UpdateResponse> getUpdateMethod;
    if ((getUpdateMethod = VectorServiceGrpc.getUpdateMethod) == null) {
      synchronized (VectorServiceGrpc.class) {
        if ((getUpdateMethod = VectorServiceGrpc.getUpdateMethod) == null) {
          VectorServiceGrpc.getUpdateMethod = getUpdateMethod =
              io.grpc.MethodDescriptor.<io.pinecone.proto.UpdateRequest, io.pinecone.proto.UpdateResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Update"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.pinecone.proto.UpdateRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.pinecone.proto.UpdateResponse.getDefaultInstance()))
              .setSchemaDescriptor(new VectorServiceMethodDescriptorSupplier("Update"))
              .build();
        }
      }
    }
    return getUpdateMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.pinecone.proto.DescribeIndexStatsRequest,
      io.pinecone.proto.DescribeIndexStatsResponse> getDescribeIndexStatsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DescribeIndexStats",
      requestType = io.pinecone.proto.DescribeIndexStatsRequest.class,
      responseType = io.pinecone.proto.DescribeIndexStatsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.pinecone.proto.DescribeIndexStatsRequest,
      io.pinecone.proto.DescribeIndexStatsResponse> getDescribeIndexStatsMethod() {
    io.grpc.MethodDescriptor<io.pinecone.proto.DescribeIndexStatsRequest, io.pinecone.proto.DescribeIndexStatsResponse> getDescribeIndexStatsMethod;
    if ((getDescribeIndexStatsMethod = VectorServiceGrpc.getDescribeIndexStatsMethod) == null) {
      synchronized (VectorServiceGrpc.class) {
        if ((getDescribeIndexStatsMethod = VectorServiceGrpc.getDescribeIndexStatsMethod) == null) {
          VectorServiceGrpc.getDescribeIndexStatsMethod = getDescribeIndexStatsMethod =
              io.grpc.MethodDescriptor.<io.pinecone.proto.DescribeIndexStatsRequest, io.pinecone.proto.DescribeIndexStatsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DescribeIndexStats"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.pinecone.proto.DescribeIndexStatsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.pinecone.proto.DescribeIndexStatsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new VectorServiceMethodDescriptorSupplier("DescribeIndexStats"))
              .build();
        }
      }
    }
    return getDescribeIndexStatsMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static VectorServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<VectorServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<VectorServiceStub>() {
        @java.lang.Override
        public VectorServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new VectorServiceStub(channel, callOptions);
        }
      };
    return VectorServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static VectorServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<VectorServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<VectorServiceBlockingStub>() {
        @java.lang.Override
        public VectorServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new VectorServiceBlockingStub(channel, callOptions);
        }
      };
    return VectorServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static VectorServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<VectorServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<VectorServiceFutureStub>() {
        @java.lang.Override
        public VectorServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new VectorServiceFutureStub(channel, callOptions);
        }
      };
    return VectorServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * The `VectorService` interface is exposed by Pinecone's vector index services.
   * This service could also be called a `gRPC` service or a `REST`-like api.
   * </pre>
   */
  public static abstract class VectorServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Upsert
     * The `Upsert` operation writes vectors into a namespace.
     * If a new value is upserted for an existing vector id, it will overwrite the previous value.
     * </pre>
     */
    public void upsert(io.pinecone.proto.UpsertRequest request,
        io.grpc.stub.StreamObserver<io.pinecone.proto.UpsertResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpsertMethod(), responseObserver);
    }

    /**
     * <pre>
     * Delete
     * The `Delete` operation deletes vectors, by id, from a single namespace.
     * You can delete items by their id, from a single namespace.
     * </pre>
     */
    public void delete(io.pinecone.proto.DeleteRequest request,
        io.grpc.stub.StreamObserver<io.pinecone.proto.DeleteResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteMethod(), responseObserver);
    }

    /**
     * <pre>
     * Fetch
     * The `Fetch` operation looks up and returns vectors, by id, from a single namespace.
     * The returned vectors include the vector data and/or metadata.
     * </pre>
     */
    public void fetch(io.pinecone.proto.FetchRequest request,
        io.grpc.stub.StreamObserver<io.pinecone.proto.FetchResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getFetchMethod(), responseObserver);
    }

    /**
     * <pre>
     * Query
     * The `Query` operation searches a namespace, using a query vector.
     * It retrieves the ids of the most similar items in a namespace, along with their similarity scores.
     * </pre>
     */
    public void query(io.pinecone.proto.QueryRequest request,
        io.grpc.stub.StreamObserver<io.pinecone.proto.QueryResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getQueryMethod(), responseObserver);
    }

    /**
     * <pre>
     * Update
     * The `Update` operation updates vector in a namespace.
     * If a value is included, it will overwrite the previous value.
     * If a set_metadata is included, the values of the fields specified in it will be added or overwrite the previous value.
     * </pre>
     */
    public void update(io.pinecone.proto.UpdateRequest request,
        io.grpc.stub.StreamObserver<io.pinecone.proto.UpdateResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpdateMethod(), responseObserver);
    }

    /**
     * <pre>
     * DescribeIndexStats
     * The `DescribeIndexStats` operation returns statistics about the index's contents.
     * For example: The vector count per namespace and the number of dimensions.
     * </pre>
     */
    public void describeIndexStats(io.pinecone.proto.DescribeIndexStatsRequest request,
        io.grpc.stub.StreamObserver<io.pinecone.proto.DescribeIndexStatsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDescribeIndexStatsMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getUpsertMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                io.pinecone.proto.UpsertRequest,
                io.pinecone.proto.UpsertResponse>(
                  this, METHODID_UPSERT)))
          .addMethod(
            getDeleteMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                io.pinecone.proto.DeleteRequest,
                io.pinecone.proto.DeleteResponse>(
                  this, METHODID_DELETE)))
          .addMethod(
            getFetchMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                io.pinecone.proto.FetchRequest,
                io.pinecone.proto.FetchResponse>(
                  this, METHODID_FETCH)))
          .addMethod(
            getQueryMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                io.pinecone.proto.QueryRequest,
                io.pinecone.proto.QueryResponse>(
                  this, METHODID_QUERY)))
          .addMethod(
            getUpdateMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                io.pinecone.proto.UpdateRequest,
                io.pinecone.proto.UpdateResponse>(
                  this, METHODID_UPDATE)))
          .addMethod(
            getDescribeIndexStatsMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                io.pinecone.proto.DescribeIndexStatsRequest,
                io.pinecone.proto.DescribeIndexStatsResponse>(
                  this, METHODID_DESCRIBE_INDEX_STATS)))
          .build();
    }
  }

  /**
   * <pre>
   * The `VectorService` interface is exposed by Pinecone's vector index services.
   * This service could also be called a `gRPC` service or a `REST`-like api.
   * </pre>
   */
  public static final class VectorServiceStub extends io.grpc.stub.AbstractAsyncStub<VectorServiceStub> {
    private VectorServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected VectorServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new VectorServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Upsert
     * The `Upsert` operation writes vectors into a namespace.
     * If a new value is upserted for an existing vector id, it will overwrite the previous value.
     * </pre>
     */
    public void upsert(io.pinecone.proto.UpsertRequest request,
        io.grpc.stub.StreamObserver<io.pinecone.proto.UpsertResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUpsertMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Delete
     * The `Delete` operation deletes vectors, by id, from a single namespace.
     * You can delete items by their id, from a single namespace.
     * </pre>
     */
    public void delete(io.pinecone.proto.DeleteRequest request,
        io.grpc.stub.StreamObserver<io.pinecone.proto.DeleteResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Fetch
     * The `Fetch` operation looks up and returns vectors, by id, from a single namespace.
     * The returned vectors include the vector data and/or metadata.
     * </pre>
     */
    public void fetch(io.pinecone.proto.FetchRequest request,
        io.grpc.stub.StreamObserver<io.pinecone.proto.FetchResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getFetchMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Query
     * The `Query` operation searches a namespace, using a query vector.
     * It retrieves the ids of the most similar items in a namespace, along with their similarity scores.
     * </pre>
     */
    public void query(io.pinecone.proto.QueryRequest request,
        io.grpc.stub.StreamObserver<io.pinecone.proto.QueryResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getQueryMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Update
     * The `Update` operation updates vector in a namespace.
     * If a value is included, it will overwrite the previous value.
     * If a set_metadata is included, the values of the fields specified in it will be added or overwrite the previous value.
     * </pre>
     */
    public void update(io.pinecone.proto.UpdateRequest request,
        io.grpc.stub.StreamObserver<io.pinecone.proto.UpdateResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUpdateMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * DescribeIndexStats
     * The `DescribeIndexStats` operation returns statistics about the index's contents.
     * For example: The vector count per namespace and the number of dimensions.
     * </pre>
     */
    public void describeIndexStats(io.pinecone.proto.DescribeIndexStatsRequest request,
        io.grpc.stub.StreamObserver<io.pinecone.proto.DescribeIndexStatsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDescribeIndexStatsMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * The `VectorService` interface is exposed by Pinecone's vector index services.
   * This service could also be called a `gRPC` service or a `REST`-like api.
   * </pre>
   */
  public static final class VectorServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<VectorServiceBlockingStub> {
    private VectorServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected VectorServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new VectorServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Upsert
     * The `Upsert` operation writes vectors into a namespace.
     * If a new value is upserted for an existing vector id, it will overwrite the previous value.
     * </pre>
     */
    public io.pinecone.proto.UpsertResponse upsert(io.pinecone.proto.UpsertRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUpsertMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Delete
     * The `Delete` operation deletes vectors, by id, from a single namespace.
     * You can delete items by their id, from a single namespace.
     * </pre>
     */
    public io.pinecone.proto.DeleteResponse delete(io.pinecone.proto.DeleteRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Fetch
     * The `Fetch` operation looks up and returns vectors, by id, from a single namespace.
     * The returned vectors include the vector data and/or metadata.
     * </pre>
     */
    public io.pinecone.proto.FetchResponse fetch(io.pinecone.proto.FetchRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getFetchMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Query
     * The `Query` operation searches a namespace, using a query vector.
     * It retrieves the ids of the most similar items in a namespace, along with their similarity scores.
     * </pre>
     */
    public io.pinecone.proto.QueryResponse query(io.pinecone.proto.QueryRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getQueryMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Update
     * The `Update` operation updates vector in a namespace.
     * If a value is included, it will overwrite the previous value.
     * If a set_metadata is included, the values of the fields specified in it will be added or overwrite the previous value.
     * </pre>
     */
    public io.pinecone.proto.UpdateResponse update(io.pinecone.proto.UpdateRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUpdateMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * DescribeIndexStats
     * The `DescribeIndexStats` operation returns statistics about the index's contents.
     * For example: The vector count per namespace and the number of dimensions.
     * </pre>
     */
    public io.pinecone.proto.DescribeIndexStatsResponse describeIndexStats(io.pinecone.proto.DescribeIndexStatsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDescribeIndexStatsMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * The `VectorService` interface is exposed by Pinecone's vector index services.
   * This service could also be called a `gRPC` service or a `REST`-like api.
   * </pre>
   */
  public static final class VectorServiceFutureStub extends io.grpc.stub.AbstractFutureStub<VectorServiceFutureStub> {
    private VectorServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected VectorServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new VectorServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Upsert
     * The `Upsert` operation writes vectors into a namespace.
     * If a new value is upserted for an existing vector id, it will overwrite the previous value.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.pinecone.proto.UpsertResponse> upsert(
        io.pinecone.proto.UpsertRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUpsertMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Delete
     * The `Delete` operation deletes vectors, by id, from a single namespace.
     * You can delete items by their id, from a single namespace.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.pinecone.proto.DeleteResponse> delete(
        io.pinecone.proto.DeleteRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Fetch
     * The `Fetch` operation looks up and returns vectors, by id, from a single namespace.
     * The returned vectors include the vector data and/or metadata.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.pinecone.proto.FetchResponse> fetch(
        io.pinecone.proto.FetchRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getFetchMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Query
     * The `Query` operation searches a namespace, using a query vector.
     * It retrieves the ids of the most similar items in a namespace, along with their similarity scores.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.pinecone.proto.QueryResponse> query(
        io.pinecone.proto.QueryRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getQueryMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Update
     * The `Update` operation updates vector in a namespace.
     * If a value is included, it will overwrite the previous value.
     * If a set_metadata is included, the values of the fields specified in it will be added or overwrite the previous value.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.pinecone.proto.UpdateResponse> update(
        io.pinecone.proto.UpdateRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUpdateMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * DescribeIndexStats
     * The `DescribeIndexStats` operation returns statistics about the index's contents.
     * For example: The vector count per namespace and the number of dimensions.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.pinecone.proto.DescribeIndexStatsResponse> describeIndexStats(
        io.pinecone.proto.DescribeIndexStatsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDescribeIndexStatsMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_UPSERT = 0;
  private static final int METHODID_DELETE = 1;
  private static final int METHODID_FETCH = 2;
  private static final int METHODID_QUERY = 3;
  private static final int METHODID_UPDATE = 4;
  private static final int METHODID_DESCRIBE_INDEX_STATS = 5;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final VectorServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(VectorServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_UPSERT:
          serviceImpl.upsert((io.pinecone.proto.UpsertRequest) request,
              (io.grpc.stub.StreamObserver<io.pinecone.proto.UpsertResponse>) responseObserver);
          break;
        case METHODID_DELETE:
          serviceImpl.delete((io.pinecone.proto.DeleteRequest) request,
              (io.grpc.stub.StreamObserver<io.pinecone.proto.DeleteResponse>) responseObserver);
          break;
        case METHODID_FETCH:
          serviceImpl.fetch((io.pinecone.proto.FetchRequest) request,
              (io.grpc.stub.StreamObserver<io.pinecone.proto.FetchResponse>) responseObserver);
          break;
        case METHODID_QUERY:
          serviceImpl.query((io.pinecone.proto.QueryRequest) request,
              (io.grpc.stub.StreamObserver<io.pinecone.proto.QueryResponse>) responseObserver);
          break;
        case METHODID_UPDATE:
          serviceImpl.update((io.pinecone.proto.UpdateRequest) request,
              (io.grpc.stub.StreamObserver<io.pinecone.proto.UpdateResponse>) responseObserver);
          break;
        case METHODID_DESCRIBE_INDEX_STATS:
          serviceImpl.describeIndexStats((io.pinecone.proto.DescribeIndexStatsRequest) request,
              (io.grpc.stub.StreamObserver<io.pinecone.proto.DescribeIndexStatsResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class VectorServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    VectorServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return io.pinecone.proto.VectorServiceOuterClass.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("VectorService");
    }
  }

  private static final class VectorServiceFileDescriptorSupplier
      extends VectorServiceBaseDescriptorSupplier {
    VectorServiceFileDescriptorSupplier() {}
  }

  private static final class VectorServiceMethodDescriptorSupplier
      extends VectorServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    VectorServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (VectorServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new VectorServiceFileDescriptorSupplier())
              .addMethod(getUpsertMethod())
              .addMethod(getDeleteMethod())
              .addMethod(getFetchMethod())
              .addMethod(getQueryMethod())
              .addMethod(getUpdateMethod())
              .addMethod(getDescribeIndexStatsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
