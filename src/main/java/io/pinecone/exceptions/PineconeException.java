package io.pinecone.exceptions;

/**
 * Base class for the errors this client throws. It is unchecked, so the compiler does not force you to catch it.
 * <p>
 * The client throws {@link PineconeValidationException} and {@link PineconeConfigurationException} before any
 * request is sent. Most index and collection management calls on {@link io.pinecone.clients.Pinecone} turn an
 * HTTP error from the server into a subclass named after the status, such as {@link PineconeNotFoundException}
 * for 404, and put the server's response body in {@link #getMessage()}. Other calls, including data-plane operations on
 * {@link io.pinecone.clients.Index} and {@link io.pinecone.clients.Inference} requests, report server errors as
 * {@code io.grpc.StatusRuntimeException} or a generated {@code ApiException}, which are not subclasses of this
 * class. Catch the specific subclass when you can act on it and this class as a fallback.
 * <pre>{@code
 * try {
 *     pinecone.deleteIndex("product-catalog");
 * } catch (PineconeNotFoundException e) {
 *     // The index was already deleted; nothing to do.
 * } catch (PineconeException e) {
 *     System.err.println("Could not delete index: " + e.getMessage());
 * }
 * }</pre>
 */
public class PineconeException extends RuntimeException {

    /**
     * Creates the exception with a message describing the problem.
     *
     * @param message what was wrong, for example {@code "Index name cannot be null or empty"}
     */
    public PineconeException(String message) {
        super(message);
    }

    /**
     * Creates the exception with a message describing the problem and the error that caused it.
     *
     * @param message what was wrong
     * @param cause the underlying error
     */
    public PineconeException(String message, Throwable cause) {
        super(message, cause);
    }
}
