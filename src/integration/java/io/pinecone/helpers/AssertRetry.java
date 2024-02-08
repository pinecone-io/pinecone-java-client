package io.pinecone.helpers;

import io.pinecone.exceptions.PineconeException;
import org.openapitools.client.ApiException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class AssertRetry {
    private static final int maxRetry = 5;
    private static int delay = 3000;

    public static void assertWithRetry(AssertionRunnable assertionRunnable) throws InterruptedException, PineconeException {
        assertWithRetry(assertionRunnable, 2);
    }

    public static void assertWithRetry(AssertionRunnable assertionRunnable, int backOff) throws InterruptedException, PineconeException {
        int retryCount = 0;
        boolean success = false;

        while (retryCount < maxRetry && !success) {
            try {
                assertionRunnable.run();
                success = true;
            } catch (AssertionError | ExecutionException | IOException e) {
                retryCount++;
                delay*=backOff;
                Thread.sleep(delay);
            }
        }
    }

    @FunctionalInterface
    public interface AssertionRunnable {
        void run() throws AssertionError, ExecutionException, InterruptedException, IOException, PineconeException;
    }
}
