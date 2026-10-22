package io.pinecone.helpers;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AssertRetryTest {

    @Test
    public void returnsAsSoonAsTheAssertionPasses() throws Exception {
        AtomicInteger attempts = new AtomicInteger();

        AssertRetry.assertWithRetry(() -> {
            if (attempts.incrementAndGet() < 3) {
                throw new AssertionError("not yet");
            }
        }, 1, 1);

        assertEquals(3, attempts.get());
    }

    @Test
    public void finalFailureKeepsTheLastFailureAsCause() {
        AtomicInteger attempts = new AtomicInteger();
        AssertionError last = new AssertionError("attempt 4");

        AssertionError thrown = assertThrows(AssertionError.class, () ->
                AssertRetry.assertWithRetry(() -> {
                    if (attempts.incrementAndGet() == 4) {
                        throw last;
                    }
                    throw new AssertionError("attempt " + attempts.get());
                }, 1, 1));

        assertEquals(4, attempts.get());
        assertSame(last, thrown.getCause());
        assertTrue(thrown.getMessage().contains("attempt 4"), thrown.getMessage());
    }

    @Test
    public void exceptionFromTheAssertionIsKeptAsCause() {
        IllegalStateException failure = new IllegalStateException("boom");

        AssertionError thrown = assertThrows(AssertionError.class, () ->
                AssertRetry.assertWithRetry(() -> {
                    throw failure;
                }, 1, 1));

        assertSame(failure, thrown.getCause());
    }

    @Test
    public void doesNotSleepAfterTheFinalAttempt() {
        long delayMs = 200;
        AtomicLong lastAttemptNanos = new AtomicLong();

        assertThrows(AssertionError.class, () ->
                AssertRetry.assertWithRetry(() -> {
                    lastAttemptNanos.set(System.nanoTime());
                    throw new AssertionError("always fails");
                }, 1, delayMs));

        long msAfterLastAttempt = (System.nanoTime() - lastAttemptNanos.get()) / 1_000_000;
        assertTrue(msAfterLastAttempt < delayMs / 2,
                "slept " + msAfterLastAttempt + " ms after the final attempt");
    }
}
