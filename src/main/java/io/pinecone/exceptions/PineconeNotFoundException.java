package io.pinecone.exceptions;

/**
 * Thrown when the named index or collection does not exist in the project (HTTP 404).
 * <p>
 * Check the name for typos and that the API key belongs to the project that holds the resource.
 * After a delete finishes, describing the resource throws this exception.
 * <pre>{@code
 * try {
 *     IndexModel index = pinecone.describeIndex("product-catalog");
 * } catch (PineconeNotFoundException e) {
 *     System.out.println("No index named product-catalog in this project");
 * }
 * }</pre>
 */
public class PineconeNotFoundException extends PineconeException {

    /**
     * Creates the exception with the server's error message.
     *
     * @param message the response body or other description of the failure
     */
    public PineconeNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates the exception with the server's error message and the underlying HTTP error.
     *
     * @param message the response body returned by the server
     * @param cause the {@link org.openapitools.db_control.client.ApiException} the request failed with, which
     *              holds the status code and response headers
     */
    public PineconeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
