package io.pinecone.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IndexStatus {
    private boolean ready;

    private String state;

    private String host;

    private String port;

    public IndexStatus() {
    }

    public boolean isReady() {
        return ready;
    }

    public IndexStatus withReady(boolean ready) {
        this.ready = ready;
        return this;
    }

    public String getState() {
        return state;
    }

    public IndexStatus withState(String state) {
        this.state = state;
        return this;
    }

    public String getHost() {
        return host;
    }

    public IndexStatus withHost(String host) {
        this.host = host;
        return this;
    }

    public String getPort() {
        return port;
    }

    public IndexStatus withPort(String port) {
        this.port = port;
        return this;
    }

    @Override
    public String toString() {
        return "IndexStatus{" +
                "ready=" + ready +
                ", state='" + state + '\'' +
                ", host='" + host + '\'' +
                ", port='" + port + '\'' +
                '}';
    }
}