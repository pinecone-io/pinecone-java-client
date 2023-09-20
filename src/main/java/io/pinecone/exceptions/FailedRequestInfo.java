package io.pinecone.exceptions;

public class FailedRequestInfo {
    private final int status;
    private final String message;

    public FailedRequestInfo(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}

