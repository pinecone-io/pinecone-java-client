package io.pinecone.exceptions;

/**
 * Thrown when the client is configured incorrectly: the API key is null or empty, the proxy host or port is
 * invalid, or both a proxy and a custom {@code OkHttpClient} are set.
 * <p>
 * {@link io.pinecone.clients.Pinecone.Builder#build()} throws it, so the problem shows up when the client is
 * created rather than on the first request.
 * <pre>{@code
 * String apiKey = System.getenv("PINECONE_API_KEY");
 * try {
 *     Pinecone pinecone = new Pinecone.Builder(apiKey).build();
 * } catch (PineconeConfigurationException e) {
 *     System.err.println("Set PINECONE_API_KEY before starting: " + e.getMessage());
 * }
 * }</pre>
 */
public class PineconeConfigurationException extends PineconeException {

    /**
     * Creates the exception with a message describing the problem.
     *
     * @param message what was wrong, for example {@code "Index name cannot be null or empty"}
     */
    public PineconeConfigurationException(String message) {
        super(message);
    }

    /**
     * Creates the exception with a message describing the problem and the error that caused it.
     *
     * @param message what was wrong
     * @param cause the underlying error
     */
    public PineconeConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
