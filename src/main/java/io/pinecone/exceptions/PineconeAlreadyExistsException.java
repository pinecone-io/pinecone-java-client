package io.pinecone.exceptions;

public class PineconeAlreadyExistsException extends PineconeException {

    public PineconeAlreadyExistsException(String message) {
        super(message);
    }

    public PineconeAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
