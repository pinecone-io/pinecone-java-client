package io.pinecone.configs;

import io.pinecone.exceptions.PineconeConfigurationException;
import io.pinecone.exceptions.PineconeValidationException;

/**
 * Represents the configuration for a proxy server.
 * This class encapsulates the host and port of the proxy server.
 */
public class ProxyConfig {

    private String host;
    private int port;

    /**
     * Constructs a ProxyConfig object with the specified host and port.
     *
     * @param host The hostname or IP address of the proxy server.
     * @param port The port number of the proxy server.
     */
    public ProxyConfig(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Gets the hostname or IP address of the proxy server.
     *
     * @return The hostname or IP address of the proxy server.
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the hostname or IP address of the proxy server.
     *
     * @param host The hostname or IP address of the proxy server.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Gets the port number of the proxy server.
     *
     * @return The port number of the proxy server.
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port number of the proxy server.
     *
     * @param port The port number of the proxy server.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Validates the proxy configuration.
     * Throws a PineconeValidationException if the host is null or empty, or if the port is less than or equal to 0.
     *
     * @throws PineconeConfigurationException If the proxy configuration is invalid.
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
