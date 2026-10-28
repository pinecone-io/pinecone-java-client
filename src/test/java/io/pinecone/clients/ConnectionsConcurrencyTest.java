package io.pinecone.clients;

import io.pinecone.configs.PineconeConfig;
import io.pinecone.configs.PineconeConnection;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionsConcurrencyTest {

    private static PineconeConfig config(String host) {
        PineconeConfig config = new PineconeConfig("testApiKey");
        config.setHost(host);
        config.setTLSEnabled(false);
        return config;
    }

    private static long connectionsTo(String host) {
        return Pinecone.openConnections().stream().filter(connection -> host.equals(connection.toString())).count();
    }

    // The user count crosses zero constantly, so the last holder's close races the next acquire.
    @Test
    public void testConcurrentAcquireAndReleaseNeverHandsOutShutDownConnection() throws Exception {
        String host = "stress-host-" + System.nanoTime();
        Pinecone pinecone = new Pinecone.Builder("testApiKey").build();
        int threads = 16;
        int iterations = 20_000;
        AtomicInteger sawShutDown = new AtomicInteger();
        Set<PineconeConnection> created = ConcurrentHashMap.newKeySet();
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        try {
            for (int t = 0; t < threads; t++) {
                futures.add(pool.submit(() -> {
                    start.await();
                    for (int i = 0; i < iterations; i++) {
                        PineconeConnection connection = pinecone.getConnection("index1", config(host));
                        created.add(connection);
                        if (connection.getChannel().isShutdown()) sawShutDown.incrementAndGet();
                        if ((i & 7) == 0) Thread.yield();
                        if (connection.getChannel().isShutdown()) sawShutDown.incrementAndGet();
                        Pinecone.releaseConnection(connection);
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> future : futures) future.get(5, TimeUnit.MINUTES);
        } finally {
            pool.shutdownNow();
        }

        assertEquals(0, sawShutDown.get());
        assertEquals(0, connectionsTo(host));
        assertEquals(0, created.stream().filter(connection -> !connection.getChannel().isShutdown()).count());
    }

    @Test
    public void testConcurrentCloseOfOneIndexReleasesOnlyOnce() throws Exception {
        String host = "concurrent-close-host-" + System.nanoTime();
        Pinecone pinecone = new Pinecone.Builder("testApiKey").build();
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            for (int round = 0; round < 300; round++) {
                PineconeConfig config = config(host);
                Index victim = new Index(config, pinecone.getConnection("index1", config), "index1");
                PineconeConnection survivorConnection = pinecone.getConnection("index1", config);
                Index survivor = new Index(config, survivorConnection, "index1");
                CountDownLatch go = new CountDownLatch(1);
                List<Future<?>> futures = new ArrayList<>();
                for (int t = 0; t < 8; t++) {
                    futures.add(pool.submit(() -> {
                        go.await();
                        victim.close();
                        return null;
                    }));
                }
                go.countDown();
                for (Future<?> future : futures) future.get();

                assertFalse(survivorConnection.getChannel().isShutdown(), "round " + round);
                assertEquals(1, connectionsTo(host));
                survivor.close();
                assertEquals(0, connectionsTo(host));
                assertTrue(survivorConnection.getChannel().isShutdown());
            }
        } finally {
            pool.shutdownNow();
        }
    }
}
