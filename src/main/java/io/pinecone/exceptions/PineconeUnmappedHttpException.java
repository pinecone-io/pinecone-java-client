package io.pinecone.exceptions;

public class PineconeUnmappedHttpException extends PineconeException {

    public PineconeUnmappedHttpException(String message) {
        super(message);
    }

    public PineconeUnmappedHttpException(String message, Throwable cause) {
        super(message, cause);
    }
}
