package io.pinecone;

import io.pinecone.proto.Core;

import java.util.List;

public class UpsertResponse extends PineconeResponse {

    /**
     * The list of vector ids upserted.
     */
    private List<String> ids;

    private UpsertResponse(){}

    static UpsertResponse from(Core.Request response) {
        UpsertResponse ret = new UpsertResponse();
        ret.ids = response.getIndex().getIdsList();
        return ret;
    }

    public List<String> getIds() {
        return ids;
    }

    @Override
    public String toString() {
        return "UpsertResponse("
                + "ids=" + getIds()
                + ")";
    }
}
