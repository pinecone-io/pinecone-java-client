package io.pinecone.exceptions;

/**
 * Thrown for an HTTP error status that has no more specific exception class, such as 429 (too many requests),
 * 412 (precondition failed) or 503 (service unavailable).
 * <p>
 * The message holds the server's response body. To get the status code, cast {@link #getCause()} to
 * {@link org.openapitools.db_control.client.ApiException} and call {@code getCode()}.
 * <pre>{@code
 * try {
 *     pinecone.createCollection("product-catalog-backup", "product-catalog");
 * } catch (PineconeUnmappedHttpException e) {
 *     int status = ((ApiException) e.getCause()).getCode();
 *     System.err.println("Request failed with HTTP " + status + ": " + e.getMessage());
 * }
 * }</pre>
 */
public class PineconeUnmappedHttpException extends PineconeException {

    /**
     * Creates the exception with the server's error message.
     *
     * @param message the response body or other description of the failure
     */
    public PineconeUnmappedHttpException(String message) {
        super(message);
    }

    /**
     * Creates the exception with the server's error message and the underlying HTTP error.
     *
     * @param message the response body returned by the server
     * @param cause the {@link org.openapitools.db_control.client.ApiException} the request failed with, which
     *              holds the status code and response headers
     */
    public PineconeUnmappedHttpException(String message, Throwable cause) {
        super(message, cause);
    }
}
