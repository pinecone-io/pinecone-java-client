package io.pinecone.exceptions;

public class PineconeAlreadyIndexExistsException extends PineconeException {

    public PineconeAlreadyIndexExistsException(String message) {
        super(message);
    }

    public PineconeAlreadyIndexExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
