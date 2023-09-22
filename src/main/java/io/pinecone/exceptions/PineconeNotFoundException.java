package io.pinecone.exceptions;

public class PineconeNotFoundException extends PineconeException {

    public PineconeNotFoundException(String message) {
        super(message);
    }

    public PineconeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
