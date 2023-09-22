package io.pinecone.exceptions;

public class PineconeException extends RuntimeException {

    public PineconeException(String message) {
        super(message);
    }

    public PineconeException(String message, Throwable cause) {
        super(message, cause);
    }
}
