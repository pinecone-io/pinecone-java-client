package io.pinecone.helpers;

import io.pinecone.exceptions.PineconeException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class AssertRetry {
    private static final int maxRetry = 4;
    private static final int delay = 1500;

    public static void assertWithRetry(AssertionRunnable assertionRunnable) throws InterruptedException, PineconeException {
        assertWithRetry(assertionRunnable, 2);
    }

    public static void assertWithRetry(AssertionRunnable assertionRunnable, int backOff) throws AssertionError, InterruptedException {
        int retryCount = 0;
        int delayCount = delay;
        boolean success = false;
        String errorMessage = null;

        while (retryCount < maxRetry && !success) {
            try {
                assertionRunnable.run();
                success = true;
            } catch (AssertionError | ExecutionException | IOException e) {
                errorMessage = e.getLocalizedMessage();
                retryCount++;
                delayCount*=backOff;
                Thread.sleep(delayCount);
            }
        }

        if (!success) {
            throw new AssertionError(errorMessage);
        }
    }

    @FunctionalInterface
    public interface AssertionRunnable {
        void run() throws AssertionError, ExecutionException, InterruptedException, IOException, PineconeException;
    }
}
