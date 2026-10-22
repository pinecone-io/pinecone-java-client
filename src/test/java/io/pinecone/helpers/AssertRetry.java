package io.pinecone.helpers;

public class AssertRetry {
    private static final int maxRetry = 4;
    private static final int delay = 1500;

    public static void assertWithRetry(AssertionRunnable assertionRunnable) throws Exception {
        assertWithRetry(assertionRunnable, 2);
    }

    public static void assertWithRetry(AssertionRunnable assertionRunnable, int backOff) throws Exception {
        assertWithRetry(assertionRunnable, backOff, delay);
    }

    static void assertWithRetry(AssertionRunnable assertionRunnable, int backOff, long initialDelayMs)
            throws Exception {
        long delayMs = initialDelayMs;
        Throwable lastFailure = null;

        for (int attempt = 1; attempt <= maxRetry; attempt++) {
            try {
                assertionRunnable.run();
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            } catch (AssertionError | Exception e) {
                lastFailure = e;
                if (attempt < maxRetry) {
                    delayMs *= backOff;
                    Thread.sleep(delayMs);
                }
            }
        }

        throw new AssertionError(
                "Assertion still failing after " + maxRetry + " attempts: " + lastFailure.getLocalizedMessage(),
                lastFailure);
    }

    @FunctionalInterface
    public interface AssertionRunnable {
        void run() throws Exception;
    }
}
