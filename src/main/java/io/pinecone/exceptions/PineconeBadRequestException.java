package io.pinecone.exceptions;

public class PineconeBadRequestException extends PineconeException {

    public PineconeBadRequestException(String message) {
        super(message);
    }

    public PineconeBadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}