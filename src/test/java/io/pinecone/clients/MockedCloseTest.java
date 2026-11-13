package io.pinecone.clients;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.stub.StreamObserver;
import io.pinecone.helpers.MockPinecone;
import io.pinecone.proto.UpsertRequest;
import io.pinecone.proto.UpsertResponse;
import io.pinecone.proto.VectorServiceGrpc;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks what {@link AsyncIndex#close()} does to calls already in flight when it shuts the channel down.
 */
public class MockedCloseTest {
    @RegisterExtension
    final MockPinecone mock = new MockPinecone();

    /** Holds every upsert until {@link #release} counts down, then answers it. */
    static class HeldUpsertService extends VectorServiceGrpc.VectorServiceImplBase {
        final CountDownLatch received = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);

        @Override
        public void upsert(UpsertRequest request, StreamObserver<UpsertResponse> response) {
            received.countDown();
            try {
                if (!release.await(30, TimeUnit.SECONDS)) {
                    return;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            response.onNext(UpsertResponse.newBuilder().setUpsertedCount(request.getVectorsCount()).build());
            response.onCompleted();
        }
    }

    @Test
    public void closeLetsInFlightCallsFinish() throws Exception {
        mock.stubDescribeIndex("close-drain", 3);
        HeldUpsertService service = new HeldUpsertService();
        mock.vectorService(service);
        AsyncIndex asyncIndex = mock.client().getAsyncIndexConnection("close-drain");

        ListenableFuture<UpsertResponse> upsert = asyncIndex.upsert("v1", Arrays.asList(0.1f, 0.2f, 0.3f), "ns");
        assertTrue(service.received.await(5, TimeUnit.SECONDS), "server never received the upsert");

        CompletableFuture<Void> closing = CompletableFuture.runAsync(asyncIndex::close);
        Thread.sleep(200);
        service.release.countDown();

        assertEquals(1, upsert.get(5, TimeUnit.SECONDS).getUpsertedCount());
        closing.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void closeCancelsCallsStillRunningAfterFiveSeconds() throws Exception {
        mock.stubDescribeIndex("close-timeout", 3);
        HeldUpsertService service = new HeldUpsertService();
        mock.vectorService(service);
        AsyncIndex asyncIndex = mock.client().getAsyncIndexConnection("close-timeout");

        ListenableFuture<UpsertResponse> upsert = asyncIndex.upsert("v1", Arrays.asList(0.1f, 0.2f, 0.3f), "ns");
        assertTrue(service.received.await(5, TimeUnit.SECONDS), "server never received the upsert");

        long start = System.nanoTime();
        asyncIndex.close();
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        service.release.countDown();

        assertTrue(elapsedMillis >= 4_000 && elapsedMillis < 10_000,
                "close() took " + elapsedMillis + " ms, expected about 5 s");
        assertThrows(ExecutionException.class, () -> upsert.get(5, TimeUnit.SECONDS));
    }
}
