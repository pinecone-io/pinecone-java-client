package io.pinecone.exceptions;

public class PineconeAuthorizationException extends PineconeException {

    public PineconeAuthorizationException(String message) {
        super(message);
    }

    public PineconeAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
