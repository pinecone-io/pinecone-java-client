
//Copyright (c) 2020-2021 Pinecone Systems Inc. All right reserved.


package io.pinecone;

public class PineconeException extends RuntimeException {

    public PineconeException(String message) {
        super(message);
    }

    public PineconeException(String message, Throwable cause) {
        super(message, cause);
    }
}
