package io.pinecone.exceptions;

public class PineconeInternalServerException extends PineconeException {

    public PineconeInternalServerException(String message) {
        super(message);
    }

    public PineconeInternalServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
