package io.pinecone.exceptions;

/**
 * Thrown when the server reports a conflict (HTTP 409), most often because an index or collection with the
 * requested name already exists in the project.
 * <p>
 * Choose another name, or use the existing resource.
 * <pre>{@code
 * try {
 *     pinecone.createServerlessIndex("product-catalog", "cosine", 1536, "aws", "us-east-1", "disabled", null);
 * } catch (PineconeAlreadyExistsException e) {
 *     IndexModel existing = pinecone.describeIndex("product-catalog");
 * }
 * }</pre>
 */
public class PineconeAlreadyExistsException extends PineconeException {

    /**
     * Creates the exception with the server's error message.
     *
     * @param message the response body or other description of the failure
     */
    public PineconeAlreadyExistsException(String message) {
        super(message);
    }

    /**
     * Creates the exception with the server's error message and the underlying HTTP error.
     *
     * @param message the response body returned by the server
     * @param cause the {@link org.openapitools.db_control.client.ApiException} the request failed with, which
     *              holds the status code and response headers
     */
    public PineconeAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
