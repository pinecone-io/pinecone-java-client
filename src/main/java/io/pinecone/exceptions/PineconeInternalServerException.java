package io.pinecone.exceptions;

/**
 * Thrown when the server fails while handling the request (HTTP 500).
 * <p>
 * The problem is on the server side, not in your arguments. Retry after a short wait; if it persists, include the
 * message when you contact Pinecone support.
 * <pre>{@code
 * try {
 *     IndexList indexes = pinecone.listIndexes();
 * } catch (PineconeInternalServerException e) {
 *     // Wait and retry.
 * }
 * }</pre>
 */
public class PineconeInternalServerException extends PineconeException {

    /**
     * Creates the exception with the server's error message.
     *
     * @param message the response body or other description of the failure
     */
    public PineconeInternalServerException(String message) {
        super(message);
    }

    /**
     * Creates the exception with the server's error message and the underlying HTTP error.
     *
     * @param message the response body returned by the server
     * @param cause the {@link org.openapitools.db_control.client.ApiException} the request failed with, which
     *              holds the status code and response headers
     */
    public PineconeInternalServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
