package io.pinecone.commons;

/**
 * Values that describe this client library itself.
 * <p>
 * Read {@link #pineconeClientVersion} when you want to log or report which client version your application runs,
 * for example in a support request.
 * <pre>{@code
 * System.out.println("Pinecone Java client " + Constants.pineconeClientVersion);
 * }</pre>
 */
public class Constants {
    /**
     * The version of this client with a leading {@code v}. The client sends it to Pinecone in the User-Agent header
     * of every request.
     */
    public static final String pineconeClientVersion = "v6.2.0";
}
