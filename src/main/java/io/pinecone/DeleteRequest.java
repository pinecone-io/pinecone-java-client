package io.pinecone;

import io.pinecone.proto.Core;

import java.util.List;

public class DeleteRequest extends PineconeRequest {

    // intended for instantiation by PineconeClient
    DeleteRequest() {}

    /**
     * Vector ids to delete.
     */
    private List<String> ids;

    /**
     * Optional namespace to apply to the request
     */
    private String namespace;

    public DeleteRequest ids(List<String> ids) {
        this.ids = ids;
        return this;
    }

    public DeleteRequest namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    protected Core.Request.Builder customizeBaseRequest(Core.Request.Builder base) {
        return base.setPath(WRITE_PATH)
                .setNamespace(namespace)
                .setDelete(Core.DeleteRequest.newBuilder()
                        .addAllIds(ids)
                        .build());
    }
}
