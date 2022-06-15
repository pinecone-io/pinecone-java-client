
//Copyright (c) 2020-2021 Pinecone Systems Inc. All right reserved.


package io.pinecone;

public class PineconeValidationException extends PineconeException {

    public PineconeValidationException(String message) {
        super(message);
    }

    public PineconeValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
