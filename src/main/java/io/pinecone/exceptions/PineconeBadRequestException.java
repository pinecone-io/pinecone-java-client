package io.pinecone.exceptions;

/**
 * Thrown when the server rejects a request as invalid (HTTP 400), for example an index spec the server does not
 * accept.
 * <p>
 * The message holds the server's explanation. Fix the arguments before retrying; sending the same request again
 * fails the same way.
 * <pre>{@code
 * try {
 *     pinecone.createServerlessIndex("product-catalog", "cosine", 1536, "aws", "us-east-1", "disabled", null);
 * } catch (PineconeBadRequestException e) {
 *     System.err.println("Invalid index settings: " + e.getMessage());
 * }
 * }</pre>
 */
public class PineconeBadRequestException extends PineconeException {

    /**
     * Creates the exception with the server's error message.
     *
     * @param message the response body or other description of the failure
     */
    public PineconeBadRequestException(String message) {
        super(message);
    }

    /**
     * Creates the exception with the server's error message and the underlying HTTP error.
     *
     * @param message the response body returned by the server
     * @param cause the {@link org.openapitools.db_control.client.ApiException} the request failed with, which
     *              holds the status code and response headers
     */
    public PineconeBadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}