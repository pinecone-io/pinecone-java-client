package io.pinecone.helpers;

import io.grpc.ForwardingServerCall;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.Status;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.util.MutableHandlerRegistry;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.VectorServiceGrpc;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * A local mock of the Pinecone control plane (REST, on a MockWebServer) and data plane (gRPC VectorService, on a
 * loopback Netty server), so unit tests can drive the real client stack with no API key. Register it with
 * {@code @RegisterExtension}; both servers start before each test and stop after it.
 * <p>
 * REST routes are stubbed by method and path. gRPC methods are served by the {@link VectorServiceGrpc.VectorServiceImplBase}
 * passed to {@link #vectorService}. Every request is captured. A REST request with no stubbed route, or a gRPC call the
 * service does not implement, fails the test after it runs, naming the method and path.
 */
public class MockPinecone implements BeforeEachCallback, AfterEachCallback {
    public static final String API_KEY = "mock-api-key";

    private final Map<String, MockResponse> routes = new ConcurrentHashMap<>();
    private final List<RecordedRequest> restRequests = Collections.synchronizedList(new ArrayList<>());
    private final List<GrpcCall> grpcCalls = Collections.synchronizedList(new ArrayList<>());
    private final List<String> unmatched = Collections.synchronizedList(new ArrayList<>());
    private final MutableHandlerRegistry grpcRegistry = new MutableHandlerRegistry();
    private MockWebServer rest;
    private Server grpc;

    /** A captured gRPC call: the full method name (e.g. {@code VectorService/Upsert}), its metadata and request. */
    public static class GrpcCall {
        public final String method;
        public final Metadata metadata;
        public volatile Object request;

        GrpcCall(String method, Metadata metadata) {
            this.method = method;
            this.metadata = metadata;
        }

        public String header(String name) {
            return metadata.get(Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER));
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws IOException {
        rest = new MockWebServer();
        rest.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                restRequests.add(request);
                String route = request.getMethod() + " " + request.getRequestUrl().encodedPath();
                MockResponse response = routes.get(route);
                if (response == null) {
                    unmatched.add("REST " + route);
                    return new MockResponse().setResponseCode(404).setBody("{\"error\":\"no mock route for " + route + "\"}");
                }
                return response;
            }
        });
        rest.start(InetAddress.getLoopbackAddress(), 0);

        vectorService(new VectorServiceGrpc.VectorServiceImplBase() {});
        grpc = NettyServerBuilder.forAddress(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0))
                .fallbackHandlerRegistry(grpcRegistry)
                .build()
                .start();
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        try {
            grpc.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            rest.shutdown();
        } finally {
            verifyNoUnmatchedRequests();
        }
    }

    /** Serves {@code method path} with the given status and JSON body. The path excludes the query string. */
    public void stub(String method, String path, int status, String jsonBody) {
        routes.put(method + " " + path, new MockResponse()
                .setResponseCode(status)
                .addHeader("Content-Type", "application/json")
                .setBody(jsonBody));
    }

    /** Stubs {@code GET /indexes/{name}} with a ready serverless index whose host is this fixture's gRPC server. */
    public void stubDescribeIndex(String indexName, int dimension) {
        stub("GET", "/indexes/" + indexName, 200, "{"
                + "\"name\":\"" + indexName + "\","
                + "\"dimension\":" + dimension + ","
                + "\"metric\":\"cosine\","
                + "\"host\":\"" + grpcHost() + "\","
                + "\"deletion_protection\":\"disabled\","
                + "\"spec\":{\"serverless\":{\"cloud\":\"aws\",\"region\":\"us-east-1\",\"read_capacity\":{\"mode\":\"OnDemand\",\"status\":{\"state\":\"Ready\"}}}},"
                + "\"status\":{\"ready\":true,\"state\":\"Ready\"},"
                + "\"vector_type\":\"dense\""
                + "}");
    }

    /** Serves the data plane with {@code service}; methods it does not override fail the test. */
    public void vectorService(VectorServiceGrpc.VectorServiceImplBase service) {
        grpcRegistry.addService(ServerInterceptors.intercept(service, new Recorder()));
    }

    /** A client whose control plane is this fixture, with TLS off so the data plane dials the gRPC server in plaintext. */
    public Pinecone client() {
        return new Pinecone.Builder(API_KEY).withHost(restUrl()).withTlsEnabled(false).build();
    }

    public String restUrl() {
        String url = rest.url("/").toString();
        return url.substring(0, url.length() - 1);
    }

    public String grpcHost() {
        return InetAddress.getLoopbackAddress().getHostAddress() + ":" + grpc.getPort();
    }

    public List<RecordedRequest> restRequests() {
        synchronized (restRequests) {
            return new ArrayList<>(restRequests);
        }
    }

    public List<GrpcCall> grpcCalls() {
        synchronized (grpcCalls) {
            return new ArrayList<>(grpcCalls);
        }
    }

    /** Throws an AssertionError naming every unmatched request seen so far, then forgets them. */
    public void verifyNoUnmatchedRequests() {
        List<String> seen;
        synchronized (unmatched) {
            seen = new ArrayList<>(unmatched);
            unmatched.clear();
        }
        if (!seen.isEmpty()) {
            throw new AssertionError("Requests with no mock route: " + seen);
        }
    }

    private class Recorder implements ServerInterceptor {
        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
                                                                     ServerCallHandler<ReqT, RespT> next) {
            String method = call.getMethodDescriptor().getFullMethodName();
            GrpcCall captured = new GrpcCall(method, headers);
            grpcCalls.add(captured);
            ServerCall<ReqT, RespT> watched = new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
                @Override
                public void close(Status status, Metadata trailers) {
                    if (status.getCode() == Status.Code.UNIMPLEMENTED) {
                        unmatched.add("gRPC " + method);
                    }
                    super.close(status, trailers);
                }
            };
            return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(next.startCall(watched, headers)) {
                @Override
                public void onMessage(ReqT message) {
                    captured.request = message;
                    super.onMessage(message);
                }
            };
        }
    }
}
