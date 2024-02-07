package io.pinecone.exceptions;

public class PineconeForbiddenException extends PineconeException {

    public PineconeForbiddenException(String message) {
        super(message);
    }

    public PineconeForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
