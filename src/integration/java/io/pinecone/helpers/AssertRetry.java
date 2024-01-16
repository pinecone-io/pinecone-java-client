package io.pinecone.helpers;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class AssertRetry {
    private static final int maxRetry = 4;
    private static int delay = 1500;

    public static void assertWithRetry(AssertionRunnable assertionRunnable) throws InterruptedException {
        int retryCount = 0;
        boolean success = false;

        while (retryCount < maxRetry && !success) {
            try {
                assertionRunnable.run();
                success = true;
            } catch (AssertionError | ExecutionException | IOException e) {
                retryCount++;
                delay*=2;
                Thread.sleep(delay);
            }
        }
    }

    @FunctionalInterface
    public interface AssertionRunnable {
        void run() throws AssertionError, ExecutionException, InterruptedException, IOException;
    }
}
