package io.pinecone.exceptions;

public class PineconeConfigurationException extends PineconeException {

    public PineconeConfigurationException(String message) {
        super(message);
    }

    public PineconeConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
