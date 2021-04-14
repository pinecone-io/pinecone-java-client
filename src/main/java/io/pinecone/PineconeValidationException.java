package io.pinecone;

public class PineconeValidationException extends PineconeException {

    public PineconeValidationException(String message) {
        super(message);
    }

    public PineconeValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
