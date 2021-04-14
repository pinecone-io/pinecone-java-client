package io.pinecone;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import io.pinecone.proto.Core;
import io.pinecone.proto.RPCClientGrpc;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * directExecutor() makes it easier to have deterministic tests.
 * However, if your implementation uses another thread and uses streaming it is better to use
 * the default executor, to avoid hitting bug #3084.
 */
@RunWith(JUnit4.class)
public class PineconeClientMockedIntegTest {
    /**
     * This rule manages automatic graceful shutdown for the registered servers and channels at the
     * end of test.
     */
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private final RPCClientGrpc.RPCClientImplBase serviceImpl =
            mock(RPCClientGrpc.RPCClientImplBase.class, delegatesTo(
                    new RPCClientGrpc.RPCClientImplBase() {
                         @Override
                         public void callUnary(Core.Request request, StreamObserver<Core.Request> responseObserver) {
                             // do something
                             if (request.hasQuery()) { // handle query request
                                 // return id:score "v1":0.5 and "v2":0.3
                                 Core.Request response = Core.Request.newBuilder(request)
                                         .setQuery(Core.QueryRequest.newBuilder(request.getQuery())
                                                 .addMatches(Core.ScoredResults.newBuilder()
                                                         .addAllIds(Arrays.asList("v1","v2"))
                                                         .addAllScores(Arrays.asList(0.5F,0.3F))
                                                         .build()))
                                         .build();
                                 responseObserver.onNext(response);
                             }
                             else if(request.hasIndex()) {
                                 if(request.getIndex().getIdsList().contains("sleep3sec")) {
                                     // delete me with checkIfBlockingStubSupportsConcurrentCalls
                                     try { TimeUnit.SECONDS.sleep(3); }
                                     catch (InterruptedException e) { e.printStackTrace(); }
                                 }
                                 // echo back copy of index request
                                 responseObserver.onNext(Core.Request.newBuilder(request).build());
                             }
                             responseObserver.onCompleted();
                         }
                    }));

    private ManagedChannel channel;

    private PineconeClient pineconeClient;

    @Before
    public void setUp() throws Exception {
        // Generate a unique in-process server name.
        String serverName = InProcessServerBuilder.generateName();

        // Create and start a server/service and channel, and register for automatic graceful shutdown
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName).directExecutor().addService(serviceImpl).build().start());
        channel = grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName).directExecutor().build());
        ManagedChannel managedChannel = channel;

        // Create a PineconeClient using the in-process channel;
        PineconeClientConfig configuration = new PineconeClientConfig()
                .withApiKey("abcdef")
                .withServerSideTimeoutSec(1);

        pineconeClient = new PineconeClient(configuration);
    }

    @Test
    public void checkIfBlockingStubSupportsConcurrentCalls() {
        ExecutorService executor = null;
        try {
            PineconeConnection connection =
                    pineconeClient.connect(
                            new PineconeConnectionConfig()
                                    .withServiceAuthority("testhost")
                                    .withSecure(false)
                                    .withServiceName("test-service")
                                    .withCustomChannelBuilder((c, d) -> channel));

            executor = Executors.newFixedThreadPool(5);
            ExecutorService finalExecutor = executor;
            List<Future<?>> futures = new ArrayList<>();
            for(int i=0; i<5; i++) {
                futures.add(
                        finalExecutor.submit(() -> {
                            connection.send(pineconeClient.upsertRequest()
                                    .data(new float[][]{{1F, 2F}})
                                    .ids(Collections.singletonList("sleep3sec")));
                        })
                );
            }
            Logger logger = LoggerFactory.getLogger(PineconeClientMockedIntegTest.class);
            while(futures.size() > 0) {
                try {
                    logger.info("got {}", futures.get(0).get());
                } catch (InterruptedException e) {
                    logger.error("interrupted", e);
                } catch (ExecutionException e) {
                    logger.error("execution error", e);
                }
                futures.remove(0);
            }
        }
        finally {
            if(executor != null) executor.shutdown();
        }
    }

    @Test
    public void connectionHandling_channelOpenedClosed() {
        PineconeConnection connection =
                pineconeClient.connect(
                        new PineconeConnectionConfig()
                                .withServiceAuthority("testhost")
                                .withSecure(false)
                                .withServiceName("test-service")
                                .withCustomChannelBuilder((c, d) -> channel));
        assertThat(channel.getState(false), equalTo(ConnectivityState.IDLE));
        connection.close();
        assertTrue(channel.isShutdown());
    }

    @Test
    public void upsert() {
        upsertTestHelper(null);
    }

    @Test
    public void upsertWithNamespace() {
        String ns = "test name";
        upsertTestHelper(ns);
    }

    private void upsertTestHelper(String ns) {
        ArgumentCaptor<Core.Request> requestCaptor = ArgumentCaptor.forClass(Core.Request.class);

        PineconeConnection connection = simpleConnection();
        List<String> ids = Arrays.asList("v1","v2");
        UpsertResponse upsertResponse = connection.send(pineconeClient.upsertRequest()
                .namespace(ns)
                .ids(ids)
                .data(new float[][]{{1F,2F},{3F,4F}}));
        connection.close();

        verify(serviceImpl).callUnary(requestCaptor.capture(),
                ArgumentMatchers.<StreamObserver<Core.Request>>any());
        if(ns != null) assertThat(requestCaptor.getValue().getNamespace(), equalTo(ns));
        assertTrue(requestCaptor.getValue().hasIndex());
        assertThat(requestCaptor.getValue().getPath(), equalTo("write"));
        assertThat(requestCaptor.getValue().getIndex().getIdsList(), equalTo(ids));
        assertThat(upsertResponse.getIds(), equalTo(ids));
    }

    @Test
    public void query() {
        queryTestHelper(null);
    }

    @Test
    public void queryWithNamespace() {
        String ns = "test name";
        queryTestHelper(ns);
    }

    private void queryTestHelper(String ns) {
        ArgumentCaptor<Core.Request> requestCaptor = ArgumentCaptor.forClass(Core.Request.class);
        int topK = 15;

        PineconeConnection connection = simpleConnection();
        QueryResponse queryResponse = connection.send(pineconeClient.queryRequest()
                .topK(topK)
                .namespace(ns)
                .includeData(false)
                .data(new float[][]{{1F,2F}}));
        connection.close();

        verify(serviceImpl).callUnary(requestCaptor.capture(),
                ArgumentMatchers.<StreamObserver<Core.Request>>any());
        if(ns != null) assertThat(requestCaptor.getValue().getNamespace(), equalTo(ns));
        assertTrue(requestCaptor.getValue().hasQuery());
        assertThat(requestCaptor.getValue().getPath(), equalTo("read"));
        assertThat(requestCaptor.getValue().getQuery().getTopK(), equalTo(topK));
        Core.NdArray queries = requestCaptor.getValue().getQuery().getData();
        assertThat(queries.getDtype(), equalTo("float32"));
        assertThat(queries.getShapeList(), equalTo(Arrays.asList(1, 2)));
        assertThat(queryResponse.getQueryResults(), notNullValue());
        assertThat(queryResponse.getQueryResults().size(), equalTo(1));
    }

    private PineconeConnection simpleConnection() {
        return pineconeClient.connect(
                new PineconeConnectionConfig()
                        .withServiceAuthority("testhost")
                        .withSecure(false)
                        .withServiceName("test-service")
                        .withCustomChannelBuilder((c, d) -> channel));
    }
}