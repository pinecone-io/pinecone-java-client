package io.pinecone;

import io.pinecone.proto.Core;

import java.util.List;

public class FetchRequest extends PineconeRequest {

    // intended for instantiation by PineconeClient
    FetchRequest() {}

    /**
     * Vector ids to fetch.
     */
    private List<String> ids;

    /**
     * Optional namespace to apply to the request
     */
    private String namespace;

    public FetchRequest ids(List<String> ids) {
        this.ids = ids;
        return this;
    }

    public FetchRequest namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    protected Core.Request.Builder customizeBaseRequest(Core.Request.Builder base) {
        return base.setPath(READ_PATH)
                .setNamespace(namespace)
                .setFetch(Core.FetchRequest.newBuilder()
                        .addAllIds(ids)
                        .build());
    }

}
