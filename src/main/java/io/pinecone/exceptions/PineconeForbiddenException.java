package io.pinecone.exceptions;

/**
 * Thrown when the API key is valid but the server does not allow the request (HTTP 403).
 * <p>
 * A common cause is deleting an index that has deletion protection enabled: turn deletion protection off by
 * configuring the index, then delete it. The server also returns 403 when the key lacks permission for the
 * operation or the project cannot use the requested feature or environment. The message says which.
 * <pre>{@code
 * try {
 *     pinecone.deleteIndex("product-catalog");
 * } catch (PineconeForbiddenException e) {
 *     System.err.println("Delete refused: " + e.getMessage());
 * }
 * }</pre>
 */
public class PineconeForbiddenException extends PineconeException {

    /**
     * Creates the exception with the server's error message.
     *
     * @param message the response body or other description of the failure
     */
    public PineconeForbiddenException(String message) {
        super(message);
    }

    /**
     * Creates the exception with the server's error message and the underlying HTTP error.
     *
     * @param message the response body returned by the server
     * @param cause the {@link org.openapitools.db_control.client.ApiException} the request failed with, which
     *              holds the status code and response headers
     */
    public PineconeForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
