package io.pinecone.exceptions;

/**
 * Thrown by the client, before any request is sent, when an argument is missing or invalid: for example an empty
 * index name, a non-positive dimension, or sparse indices and values of different lengths.
 * <p>
 * The message says which argument to fix.
 * <pre>{@code
 * try {
 *     Index index = pinecone.getIndexConnection("");
 * } catch (PineconeValidationException e) {
 *     System.err.println(e.getMessage()); // Index name cannot be null or empty
 * }
 * }</pre>
 */
public class PineconeValidationException extends PineconeException {

    /**
     * Creates the exception with a message describing the problem.
     *
     * @param message what was wrong, for example {@code "Index name cannot be null or empty"}
     */
    public PineconeValidationException(String message) {
        super(message);
    }

    /**
     * Creates the exception with a message describing the problem and the error that caused it.
     *
     * @param message what was wrong
     * @param cause the underlying error
     */
    public PineconeValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
