package io.pinecone.configs;

import io.pinecone.exceptions.PineconeConfigurationException;
import io.pinecone.exceptions.PineconeValidationException;

/**
 * The host and port of an HTTP proxy for the client to send requests through.
 * <p>
 * Most callers set a proxy with {@link io.pinecone.clients.Pinecone.Builder#withProxy(String, int) withProxy}, which
 * creates this object; the {@link io.pinecone.clients.Pinecone} client then uses it for index management and
 * {@link io.pinecone.clients.Inference} calls, but not for index connections opened with
 * {@link io.pinecone.clients.Pinecone#getIndexConnection(String)}. Construct one yourself to pass to
 * {@link PineconeConfig#setProxyConfig(ProxyConfig)} when you build a {@link PineconeConnection} directly.
 * <pre>{@code
 * PineconeConfig config = new PineconeConfig(System.getenv("PINECONE_API_KEY"));
 * config.setHost("product-catalog-abc1234.svc.aped-4627-b74a.pinecone.io");
 * config.setProxyConfig(new ProxyConfig("proxy.example.com", 8080));
 * config.validate();
 * }</pre>
 */
public class ProxyConfig {

    private String host;
    private int port;

    /**
     * Creates a proxy setting. Nothing is checked until {@link #validate()} runs.
     *
     * @param host the proxy's hostname or IP address, such as {@code "proxy.example.com"}
     * @param port the proxy's port, such as {@code 8080}
     */
    public ProxyConfig(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * The proxy's hostname or IP address.
     *
     * @return the proxy host
     */
    public String getHost() {
        return host;
    }

    /**
     * Changes the proxy host.
     *
     * @param host the proxy's hostname or IP address
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * The proxy's port.
     *
     * @return the proxy port
     */
    public int getPort() {
        return port;
    }

    /**
     * Changes the proxy port.
     *
     * @param port the proxy's port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Checks that the host is set and the port is positive. {@link PineconeConfig#validate()} calls this.
     *
     * @throws PineconeConfigurationException if the host is null or empty, or the port is 0 or negative
     */
    public void validate() {
        if (host == null || host.isEmpty()) {
            throw new PineconeConfigurationException("Proxy host cannot be null or empty.");
        }
        if (port <= 0) {
            throw new PineconeConfigurationException("Proxy port must be greater than 0.");
        }
    }
}
