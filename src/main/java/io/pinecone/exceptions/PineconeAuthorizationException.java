package io.pinecone.exceptions;

/**
 * Thrown when the server does not accept the API key (HTTP 401): it is missing, mistyped, or has been revoked.
 * <p>
 * Check the key passed to {@link io.pinecone.clients.Pinecone.Builder}. Retrying with the same key does not help.
 * <pre>{@code
 * try {
 *     pinecone.listIndexes();
 * } catch (PineconeAuthorizationException e) {
 *     System.err.println("Check PINECONE_API_KEY: " + e.getMessage());
 * }
 * }</pre>
 */
public class PineconeAuthorizationException extends PineconeException {

    /**
     * Creates the exception with the server's error message.
     *
     * @param message the response body or other description of the failure
     */
    public PineconeAuthorizationException(String message) {
        super(message);
    }

    /**
     * Creates the exception with the server's error message and the underlying HTTP error.
     *
     * @param message the response body returned by the server
     * @param cause the {@link org.openapitools.db_control.client.ApiException} the request failed with, which
     *              holds the status code and response headers
     */
    public PineconeAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
