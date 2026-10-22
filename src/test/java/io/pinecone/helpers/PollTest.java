package io.pinecone.helpers;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PollTest {

    @Test
    public void returnsTheFirstResultThatSatisfiesTheCondition() throws Exception {
        AtomicInteger probes = new AtomicInteger();

        int result = Poll.until("count reaches 3", 5000, 1, probes::incrementAndGet, n -> n >= 3);

        assertEquals(3, result);
        assertEquals(3, probes.get());
    }

    @Test
    public void deadlineFailsWithTheConditionAndLastValue() {
        AssertionError thrown = assertThrows(AssertionError.class, () ->
                Poll.until("index foo is Ready", 50, 5, () -> "Initializing", "Ready"::equals));

        assertTrue(thrown.getMessage().contains("Timed out after 50ms waiting for index foo is Ready"),
                thrown.getMessage());
        assertTrue(thrown.getMessage().contains("last value: Initializing"), thrown.getMessage());
    }

    @Test
    public void nonRetryableExceptionIsRethrownWithoutPolling() {
        AtomicInteger probes = new AtomicInteger();
        IllegalStateException boom = new IllegalStateException("boom");

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () ->
                Poll.until("never", 5000, 1, () -> {
                    probes.incrementAndGet();
                    throw boom;
                }, x -> true));

        assertSame(boom, thrown);
        assertEquals(1, probes.get());
    }

    @Test
    public void retryableExceptionIsPolledThrough() throws Exception {
        AtomicInteger probes = new AtomicInteger();

        String result = Poll.until("upsert succeeds", 5000, 1, () -> {
            if (probes.incrementAndGet() < 3) {
                throw new IllegalStateException("UNAVAILABLE");
            }
            return "ok";
        }, x -> true, e -> e instanceof IllegalStateException);

        assertEquals("ok", result);
        assertEquals(3, probes.get());
    }

    @Test
    public void deadlineAfterRetryableExceptionKeepsItAsCause() {
        IllegalStateException unavailable = new IllegalStateException("UNAVAILABLE");

        AssertionError thrown = assertThrows(AssertionError.class, () ->
                Poll.until("upsert succeeds", 50, 5, () -> {
                    throw unavailable;
                }, x -> true, e -> e instanceof IllegalStateException));

        assertSame(unavailable, thrown.getCause());
        assertTrue(thrown.getMessage().contains("last error: java.lang.IllegalStateException: UNAVAILABLE"),
                thrown.getMessage());
    }
}
