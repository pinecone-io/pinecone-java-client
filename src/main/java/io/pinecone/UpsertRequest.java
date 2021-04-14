package io.pinecone;

import io.pinecone.proto.Core;

import java.util.List;

public class UpsertRequest extends PineconeRequest {

    // intended for instantiation by PineconeClient
    UpsertRequest() {}

    /**
     * Vector ids to upsert, in the same order as the vectors in {@link UpsertRequest#data}. That
     * is, ids.get(0) is for data[0], ids.get(1) is for data[1], and so on.
     */
    private List<String> ids;

    /**
     * The vectors to upsert. E.g. data[0] is the first vector to upsert.
     */
    private float[][] data;

    /**
     * Optional namespace to apply to the request. Vectors upserted will be tagged with this namespace.
     */
    private String namespace;

    /**
     * @param ids See {@link UpsertRequest#ids}.
     * @return This object, to allow chaining calls.
     */
    public UpsertRequest ids(List<String> ids) {
        this.ids = ids;
        return this;
    }

    /**
     * @return See {@link UpsertRequest#ids}.
     */
    public List<String> getIds() {
        return ids;
    }

    /**
     * @param data See {@link UpsertRequest#data}.
     * @return This object, to allow chaining calls.
     */
    public UpsertRequest data(float[][] data) {
        if(data == null || data.length == 0 || data[0].length == 0)
            throw new PineconeValidationException("data cannot be empty");
        this.data = data;
        return this;
    }

    /**
     * @return See {@link UpsertRequest#data}.
     */
    public float[][] getData() {
        return data;
    }

    /**
     * @param namespace See {@link UpsertRequest#namespace}.
     * @return This object, to allow chaining calls.
     */
    public UpsertRequest namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    /**
     * @return See {@link UpsertRequest#namespace}.
     */
    public String getNamespace() {
        return namespace;
    }

    @Override
    protected Core.Request.Builder customizeBaseRequest(Core.Request.Builder builder) {
        builder.setPath(WRITE_PATH);
        if(namespace != null) builder.setNamespace(namespace);
        return builder.setIndex(Core.IndexRequest.newBuilder()
                        .addAllIds(ids)
                        .setData(translator.translate(data))
                        .build());
    }
}
