package io.pinecone;

import io.pinecone.proto.Core;

public class InfoRequest extends PineconeRequest {

    // intended for instantiation by PineconeClient
    InfoRequest() {}

    /**
     * Optional namespace to apply to the request
     */
    private String namespace;

    public InfoRequest namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    protected Core.Request.Builder customizeBaseRequest(Core.Request.Builder base) {
        return base.setPath(READ_PATH)
                .setNamespace(namespace)
                .setInfo(Core.InfoRequest.newBuilder().build());
    }
}
