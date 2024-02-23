package io.pinecone.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * The `VectorService` interface is exposed by Pinecone's vector index services.
 * This service could also be called a `gRPC` service or a `REST`-like api.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.58.0)",
    comments = "Source: vector_service.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class VectorServiceGrpc {

  private VectorServiceGrpc() {}

  public static final String SERVICE_NAME = "VectorService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<UpsertRequest,
      UpsertResponse> getUpsertMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Upsert",
      requestType = UpsertRequest.class,
      responseType = UpsertResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<UpsertRequest,
      UpsertResponse> getUpsertMethod() {
    io.grpc.MethodDescriptor<UpsertRequest, UpsertResponse> getUpsertMethod;
    if ((getUpsertMethod = VectorServiceGrpc.getUpsertMethod) == null) {
      synchronized (VectorServiceGrpc.class) {
        if ((getUpsertMethod = VectorServiceGrpc.getUpsertMethod) == null) {
          VectorServiceGrpc.getUpsertMethod = getUpsertMethod =
              io.grpc.MethodDescriptor.<UpsertRequest, UpsertResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Upsert"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  UpsertRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  UpsertResponse.getDefaultInstance()))
              .setSchemaDescriptor(new VectorServiceMethodDescriptorSupplier("Upsert"))
              .build();
        }
      }
    }
    return getUpsertMethod;
  }

  private static volatile io.grpc.MethodDescriptor<DeleteRequest,
      DeleteResponse> getDeleteMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Delete",
      requestType = DeleteRequest.class,
      responseType = DeleteResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<DeleteRequest,
      DeleteResponse> getDeleteMethod() {
    io.grpc.MethodDescriptor<DeleteRequest, DeleteResponse> getDeleteMethod;
    if ((getDeleteMethod = VectorServiceGrpc.getDeleteMethod) == null) {
      synchronized (VectorServiceGrpc.class) {
        if ((getDeleteMethod = VectorServiceGrpc.getDeleteMethod) == null) {
          VectorServiceGrpc.getDeleteMethod = getDeleteMethod =
              io.grpc.MethodDescriptor.<DeleteRequest, DeleteResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Delete"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  DeleteRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  DeleteResponse.getDefaultInstance()))
              .setSchemaDescriptor(new VectorServiceMethodDescriptorSupplier("Delete"))
              .build();
        }
      }
    }
    return getDeleteMethod;
  }

  private static volatile io.grpc.MethodDescriptor<FetchRequest,
      FetchResponse> getFetchMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Fetch",
      requestType = FetchRequest.class,
      responseType = FetchResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<FetchRequest,
      FetchResponse> getFetchMethod() {
    io.grpc.MethodDescriptor<FetchRequest, FetchResponse> getFetchMethod;
    if ((getFetchMethod = VectorServiceGrpc.getFetchMethod) == null) {
      synchronized (VectorServiceGrpc.class) {
        if ((getFetchMethod = VectorServiceGrpc.getFetchMethod) == null) {
          VectorServiceGrpc.getFetchMethod = getFetchMethod =
              io.grpc.MethodDescriptor.<FetchRequest, FetchResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Fetch"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  FetchRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  FetchResponse.getDefaultInstance()))
              .setSchemaDescriptor(new VectorServiceMethodDescriptorSupplier("Fetch"))
              .build();
        }
      }
    }
    return getFetchMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ListRequest,
      ListResponse> getListMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "List",
      requestType = ListRequest.class,
      responseType = ListResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ListRequest,
      ListResponse> getListMethod() {
    io.grpc.MethodDescriptor<ListRequest, ListResponse> getListMethod;
    if ((getListMethod = VectorServiceGrpc.getListMethod) == null) {
      synchronized (VectorServiceGrpc.class) {
        if ((getListMethod = VectorServiceGrpc.getListMethod) == null) {
          VectorServiceGrpc.getListMethod = getListMethod =
              io.grpc.MethodDescriptor.<ListRequest, ListResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "List"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ListRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ListResponse.getDefaultInstance()))
              .setSchemaDescriptor(new VectorServiceMethodDescriptorSupplier("List"))
              .build();
        }
      }
    }
    return getListMethod;
  }

  private static volatile io.grpc.MethodDescriptor<QueryRequest,
      QueryResponse> getQueryMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Query",
      requestType = QueryRequest.class,
      responseType = QueryResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<QueryRequest,
      QueryResponse> getQueryMethod() {
    io.grpc.MethodDescriptor<QueryRequest, QueryResponse> getQueryMethod;
    if ((getQueryMethod = VectorServiceGrpc.getQueryMethod) == null) {
      synchronized (VectorServiceGrpc.class) {
        if ((getQueryMethod = VectorServiceGrpc.getQueryMethod) == null) {
          VectorServiceGrpc.getQueryMethod = getQueryMethod =
              io.grpc.MethodDescriptor.<QueryRequest, QueryResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Query"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  QueryRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  QueryResponse.getDefaultInstance()))
              .setSchemaDescriptor(new VectorServiceMethodDescriptorSupplier("Query"))
              .build();
        }
      }
    }
    return getQueryMethod;
  }

  private static volatile io.grpc.MethodDescriptor<UpdateRequest,
      UpdateResponse> getUpdateMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Update",
      requestType = UpdateRequest.class,
      responseType = UpdateResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<UpdateRequest,
      UpdateResponse> getUpdateMethod() {
    io.grpc.MethodDescriptor<UpdateRequest, UpdateResponse> getUpdateMethod;
    if ((getUpdateMethod = VectorServiceGrpc.getUpdateMethod) == null) {
      synchronized (VectorServiceGrpc.class) {
        if ((getUpdateMethod = VectorServiceGrpc.getUpdateMethod) == null) {
          VectorServiceGrpc.getUpdateMethod = getUpdateMethod =
              io.grpc.MethodDescriptor.<UpdateRequest, UpdateResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Update"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  UpdateRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  UpdateResponse.getDefaultInstance()))
              .setSchemaDescriptor(new VectorServiceMethodDescriptorSupplier("Update"))
              .build();
        }
      }
    }
    return getUpdateMethod;
  }

  private static volatile io.grpc.MethodDescriptor<DescribeIndexStatsRequest,
      DescribeIndexStatsResponse> getDescribeIndexStatsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DescribeIndexStats",
      requestType = DescribeIndexStatsRequest.class,
      responseType = DescribeIndexStatsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<DescribeIndexStatsRequest,
      DescribeIndexStatsResponse> getDescribeIndexStatsMethod() {
    io.grpc.MethodDescriptor<DescribeIndexStatsRequest, DescribeIndexStatsResponse> getDescribeIndexStatsMethod;
    if ((getDescribeIndexStatsMethod = VectorServiceGrpc.getDescribeIndexStatsMethod) == null) {
      synchronized (VectorServiceGrpc.class) {
        if ((getDescribeIndexStatsMethod = VectorServiceGrpc.getDescribeIndexStatsMethod) == null) {
          VectorServiceGrpc.getDescribeIndexStatsMethod = getDescribeIndexStatsMethod =
              io.grpc.MethodDescriptor.<DescribeIndexStatsRequest, DescribeIndexStatsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DescribeIndexStats"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  DescribeIndexStatsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  DescribeIndexStatsResponse.getDefaultInstance()))
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
        @Override
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
        @Override
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
        @Override
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
  public interface AsyncService {

    /**
     * <pre>
     * Upsert
     * The `Upsert` operation writes vectors into a namespace.
     * If a new value is upserted for an existing vector id, it will overwrite the previous value.
     * </pre>
     */
    default void upsert(UpsertRequest request,
                        io.grpc.stub.StreamObserver<UpsertResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpsertMethod(), responseObserver);
    }

    /**
     * <pre>
     * Delete
     * The `Delete` operation deletes vectors, by id, from a single namespace.
     * You can delete items by their id, from a single namespace.
     * </pre>
     */
    default void delete(DeleteRequest request,
                        io.grpc.stub.StreamObserver<DeleteResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteMethod(), responseObserver);
    }

    /**
     * <pre>
     * Fetch
     * The `Fetch` operation looks up and returns vectors, by ID, from a single namespace.
     * The returned vectors include the vector data and/or metadata.
     * </pre>
     */
    default void fetch(FetchRequest request,
                       io.grpc.stub.StreamObserver<FetchResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getFetchMethod(), responseObserver);
    }

    /**
     * <pre>
     * List
     * The `List` operations lists the IDs of vectors in a single namespace.
     * An optional prefix can be passed to limit the listing to those ids that start
     * with the given prefix.
     * Ids are returned in sorted order (bitwise/"C" collation).
     * A maximum of 100 ids are returned at a time.
     * A pagination token is also returned to allow the client to list the next 100.
     * The absence of the pagination token indicates that there are no more ids that
     * match the given parameters.
     * </pre>
     */
    default void list(ListRequest request,
                      io.grpc.stub.StreamObserver<ListResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListMethod(), responseObserver);
    }

    /**
     * <pre>
     * Query
     * The `Query` operation searches a namespace, using a query vector.
     * It retrieves the ids of the most similar items in a namespace, along with their similarity scores.
     * </pre>
     */
    default void query(QueryRequest request,
                       io.grpc.stub.StreamObserver<QueryResponse> responseObserver) {
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
    default void update(UpdateRequest request,
                        io.grpc.stub.StreamObserver<UpdateResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpdateMethod(), responseObserver);
    }

    /**
     * <pre>
     * DescribeIndexStats
     * The `DescribeIndexStats` operation returns statistics about the index's
     * contents, including the vector count per namespace, the number of
     * dimensions, and the index fullness. The index fullness result  may be
     * inaccurate during pod resizing; to get the status of a pod resizing
     * process, use
     * [`describe_index`](https://www.pinecone.io/docs/api/operation/describe_index/).
     * </pre>
     */
    default void describeIndexStats(DescribeIndexStatsRequest request,
                                    io.grpc.stub.StreamObserver<DescribeIndexStatsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDescribeIndexStatsMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service VectorService.
   * <pre>
   * The `VectorService` interface is exposed by Pinecone's vector index services.
   * This service could also be called a `gRPC` service or a `REST`-like api.
   * </pre>
   */
  public static abstract class VectorServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @Override public final io.grpc.ServerServiceDefinition bindService() {
      return VectorServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service VectorService.
   * <pre>
   * The `VectorService` interface is exposed by Pinecone's vector index services.
   * This service could also be called a `gRPC` service or a `REST`-like api.
   * </pre>
   */
  public static final class VectorServiceStub
      extends io.grpc.stub.AbstractAsyncStub<VectorServiceStub> {
    private VectorServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
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
    public void upsert(UpsertRequest request,
                       io.grpc.stub.StreamObserver<UpsertResponse> responseObserver) {
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
    public void delete(DeleteRequest request,
                       io.grpc.stub.StreamObserver<DeleteResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Fetch
     * The `Fetch` operation looks up and returns vectors, by ID, from a single namespace.
     * The returned vectors include the vector data and/or metadata.
     * </pre>
     */
    public void fetch(FetchRequest request,
                      io.grpc.stub.StreamObserver<FetchResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getFetchMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * List
     * The `List` operations lists the IDs of vectors in a single namespace.
     * An optional prefix can be passed to limit the listing to those ids that start
     * with the given prefix.
     * Ids are returned in sorted order (bitwise/"C" collation).
     * A maximum of 100 ids are returned at a time.
     * A pagination token is also returned to allow the client to list the next 100.
     * The absence of the pagination token indicates that there are no more ids that
     * match the given parameters.
     * </pre>
     */
    public void list(ListRequest request,
                     io.grpc.stub.StreamObserver<ListResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Query
     * The `Query` operation searches a namespace, using a query vector.
     * It retrieves the ids of the most similar items in a namespace, along with their similarity scores.
     * </pre>
     */
    public void query(QueryRequest request,
                      io.grpc.stub.StreamObserver<QueryResponse> responseObserver) {
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
    public void update(UpdateRequest request,
                       io.grpc.stub.StreamObserver<UpdateResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUpdateMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * DescribeIndexStats
     * The `DescribeIndexStats` operation returns statistics about the index's
     * contents, including the vector count per namespace, the number of
     * dimensions, and the index fullness. The index fullness result  may be
     * inaccurate during pod resizing; to get the status of a pod resizing
     * process, use
     * [`describe_index`](https://www.pinecone.io/docs/api/operation/describe_index/).
     * </pre>
     */
    public void describeIndexStats(DescribeIndexStatsRequest request,
                                   io.grpc.stub.StreamObserver<DescribeIndexStatsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDescribeIndexStatsMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service VectorService.
   * <pre>
   * The `VectorService` interface is exposed by Pinecone's vector index services.
   * This service could also be called a `gRPC` service or a `REST`-like api.
   * </pre>
   */
  public static final class VectorServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<VectorServiceBlockingStub> {
    private VectorServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
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
    public UpsertResponse upsert(UpsertRequest request) {
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
    public DeleteResponse delete(DeleteRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Fetch
     * The `Fetch` operation looks up and returns vectors, by ID, from a single namespace.
     * The returned vectors include the vector data and/or metadata.
     * </pre>
     */
    public FetchResponse fetch(FetchRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getFetchMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * List
     * The `List` operations lists the IDs of vectors in a single namespace.
     * An optional prefix can be passed to limit the listing to those ids that start
     * with the given prefix.
     * Ids are returned in sorted order (bitwise/"C" collation).
     * A maximum of 100 ids are returned at a time.
     * A pagination token is also returned to allow the client to list the next 100.
     * The absence of the pagination token indicates that there are no more ids that
     * match the given parameters.
     * </pre>
     */
    public ListResponse list(ListRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Query
     * The `Query` operation searches a namespace, using a query vector.
     * It retrieves the ids of the most similar items in a namespace, along with their similarity scores.
     * </pre>
     */
    public QueryResponse query(QueryRequest request) {
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
    public UpdateResponse update(UpdateRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUpdateMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * DescribeIndexStats
     * The `DescribeIndexStats` operation returns statistics about the index's
     * contents, including the vector count per namespace, the number of
     * dimensions, and the index fullness. The index fullness result  may be
     * inaccurate during pod resizing; to get the status of a pod resizing
     * process, use
     * [`describe_index`](https://www.pinecone.io/docs/api/operation/describe_index/).
     * </pre>
     */
    public DescribeIndexStatsResponse describeIndexStats(DescribeIndexStatsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDescribeIndexStatsMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service VectorService.
   * <pre>
   * The `VectorService` interface is exposed by Pinecone's vector index services.
   * This service could also be called a `gRPC` service or a `REST`-like api.
   * </pre>
   */
  public static final class VectorServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<VectorServiceFutureStub> {
    private VectorServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
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
    public com.google.common.util.concurrent.ListenableFuture<UpsertResponse> upsert(
        UpsertRequest request) {
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
    public com.google.common.util.concurrent.ListenableFuture<DeleteResponse> delete(
        DeleteRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Fetch
     * The `Fetch` operation looks up and returns vectors, by ID, from a single namespace.
     * The returned vectors include the vector data and/or metadata.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<FetchResponse> fetch(
        FetchRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getFetchMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * List
     * The `List` operations lists the IDs of vectors in a single namespace.
     * An optional prefix can be passed to limit the listing to those ids that start
     * with the given prefix.
     * Ids are returned in sorted order (bitwise/"C" collation).
     * A maximum of 100 ids are returned at a time.
     * A pagination token is also returned to allow the client to list the next 100.
     * The absence of the pagination token indicates that there are no more ids that
     * match the given parameters.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ListResponse> list(
        ListRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Query
     * The `Query` operation searches a namespace, using a query vector.
     * It retrieves the ids of the most similar items in a namespace, along with their similarity scores.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<QueryResponse> query(
        QueryRequest request) {
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
    public com.google.common.util.concurrent.ListenableFuture<UpdateResponse> update(
        UpdateRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUpdateMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * DescribeIndexStats
     * The `DescribeIndexStats` operation returns statistics about the index's
     * contents, including the vector count per namespace, the number of
     * dimensions, and the index fullness. The index fullness result  may be
     * inaccurate during pod resizing; to get the status of a pod resizing
     * process, use
     * [`describe_index`](https://www.pinecone.io/docs/api/operation/describe_index/).
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<DescribeIndexStatsResponse> describeIndexStats(
        DescribeIndexStatsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDescribeIndexStatsMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_UPSERT = 0;
  private static final int METHODID_DELETE = 1;
  private static final int METHODID_FETCH = 2;
  private static final int METHODID_LIST = 3;
  private static final int METHODID_QUERY = 4;
  private static final int METHODID_UPDATE = 5;
  private static final int METHODID_DESCRIBE_INDEX_STATS = 6;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_UPSERT:
          serviceImpl.upsert((UpsertRequest) request,
              (io.grpc.stub.StreamObserver<UpsertResponse>) responseObserver);
          break;
        case METHODID_DELETE:
          serviceImpl.delete((DeleteRequest) request,
              (io.grpc.stub.StreamObserver<DeleteResponse>) responseObserver);
          break;
        case METHODID_FETCH:
          serviceImpl.fetch((FetchRequest) request,
              (io.grpc.stub.StreamObserver<FetchResponse>) responseObserver);
          break;
        case METHODID_LIST:
          serviceImpl.list((ListRequest) request,
              (io.grpc.stub.StreamObserver<ListResponse>) responseObserver);
          break;
        case METHODID_QUERY:
          serviceImpl.query((QueryRequest) request,
              (io.grpc.stub.StreamObserver<QueryResponse>) responseObserver);
          break;
        case METHODID_UPDATE:
          serviceImpl.update((UpdateRequest) request,
              (io.grpc.stub.StreamObserver<UpdateResponse>) responseObserver);
          break;
        case METHODID_DESCRIBE_INDEX_STATS:
          serviceImpl.describeIndexStats((DescribeIndexStatsRequest) request,
              (io.grpc.stub.StreamObserver<DescribeIndexStatsResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getUpsertMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              UpsertRequest,
              UpsertResponse>(
                service, METHODID_UPSERT)))
        .addMethod(
          getDeleteMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              DeleteRequest,
              DeleteResponse>(
                service, METHODID_DELETE)))
        .addMethod(
          getFetchMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              FetchRequest,
              FetchResponse>(
                service, METHODID_FETCH)))
        .addMethod(
          getListMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ListRequest,
              ListResponse>(
                service, METHODID_LIST)))
        .addMethod(
          getQueryMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              QueryRequest,
              QueryResponse>(
                service, METHODID_QUERY)))
        .addMethod(
          getUpdateMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              UpdateRequest,
              UpdateResponse>(
                service, METHODID_UPDATE)))
        .addMethod(
          getDescribeIndexStatsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              DescribeIndexStatsRequest,
              DescribeIndexStatsResponse>(
                service, METHODID_DESCRIBE_INDEX_STATS)))
        .build();
  }

  private static abstract class VectorServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    VectorServiceBaseDescriptorSupplier() {}

    @Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return VectorServiceOuterClass.getDescriptor();
    }

    @Override
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

    @Override
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
              .addMethod(getListMethod())
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
