package io.pinecone.configs;

import java.util.Arrays;
import java.util.List;

public class RetryConfig {

    private int maxAttempts = 5;
    private double initialBackoff = 10;
    private double maxBackoff = 120;
    private double backoffMultiplier = 2;
    private List<String> retryableStatusCodes = Arrays.asList("UNAVAILABLE");

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public double getInitialBackoff() {
        return initialBackoff;
    }

    public void setInitialBackoff(double initialBackoff) {
        this.initialBackoff = initialBackoff;
    }

    public double getMaxBackoff() {
        return maxBackoff;
    }

    public void setMaxBackoff(double maxBackoff) {
        this.maxBackoff = maxBackoff;
    }

    public double getBackoffMultiplier() {
        return backoffMultiplier;
    }

    public void setBackoffMultiplier(double backoffMultiplier) {
        this.backoffMultiplier = backoffMultiplier;
    }

    public List<String> getRetryableStatusCodes() {
        return retryableStatusCodes;
    }

    public void setRetryableStatusCodes(List<String> retryableStatusCodes) {
        this.retryableStatusCodes = retryableStatusCodes;
    }

    public void addRetryableStatusCode(String statusCode) {
        retryableStatusCodes.add(statusCode);
    }

    public void removeRetryableStatusCode(String statusCode) {
        retryableStatusCodes.remove(statusCode);
    }
}
