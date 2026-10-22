package io.pinecone.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Polls a probe until its result satisfies a condition or a deadline passes. Use this instead of a fixed
 * {@code Thread.sleep} when waiting for a backend resource to reach a state.
 */
public class Poll {
    private static final Logger logger = LoggerFactory.getLogger(Poll.class);

    /**
     * Calls {@code probe} every {@code intervalMs} until {@code done} accepts its result. Any exception from the
     * probe is rethrown at once.
     *
     * @throws AssertionError if the deadline passes first; the message names {@code condition} and the last result.
     */
    public static <T> T until(String condition, long timeoutMs, long intervalMs,
                              Supplier<T> probe, Predicate<? super T> done) throws InterruptedException {
        return until(condition, timeoutMs, intervalMs, probe, done, e -> false);
    }

    /**
     * Like {@link #until(String, long, long, Supplier, Predicate)}, but a probe exception accepted by
     * {@code retryable} counts as "not done yet" instead of failing. If the deadline passes, the last such exception
     * is the cause of the AssertionError.
     */
    public static <T> T until(String condition, long timeoutMs, long intervalMs, Supplier<T> probe,
                              Predicate<? super T> done, Predicate<? super RuntimeException> retryable)
            throws InterruptedException {
        long start = System.currentTimeMillis();
        long deadline = start + timeoutMs;
        T last = null;
        RuntimeException lastException = null;

        while (true) {
            try {
                last = probe.get();
                lastException = null;
                if (done.test(last)) {
                    logger.info("{} after {}ms", condition, System.currentTimeMillis() - start);
                    return last;
                }
            } catch (RuntimeException e) {
                if (!retryable.test(e)) {
                    throw e;
                }
                lastException = e;
            }

            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) {
                String lastSeen = lastException != null ? "last error: " + lastException : "last value: " + last;
                throw new AssertionError("Timed out after " + timeoutMs + "ms waiting for " + condition + "; "
                        + lastSeen, lastException);
            }
            Thread.sleep(Math.min(intervalMs, remaining));
        }
    }
}
