package io.pinecone;

import io.pinecone.proto.Core;

public class QueryRequest extends PineconeRequest {

    // intended for instantiation by PineconeClient
    QueryRequest(){}

    /**
     * The number of matches to return for each query vector.
     */
    private int topK;

    /**
     * Whether the vector values should be included in the response rather than just the vector
     * ids. Default: false.
     */
    private boolean includeData;

    /**
     * The query vectors. E.g. data[0] is the first query vector.
     */
    private float[][] data;

    /**
     * Optional namespace to apply to the request. Only vectors in this namespace will be returned.
     */
    private String namespace;

    /**
     * @param topK See {@link QueryRequest#topK}
     * @return This object, to allow chaining calls.
     */
    public QueryRequest topK(int topK) {
        this.topK = topK;
        return this;
    }

    /**
     * @return See {@link QueryRequest#topK}.
     */
    public int getTopK() {
        return topK;
    }

    /**
     * @param includeData See {@link QueryRequest#includeData}
     * @return This object, to allow chaining calls.
     */
    public QueryRequest includeData(boolean includeData) {
        this.includeData = includeData;
        return this;
    }

    /**
     * @return See {@link QueryRequest#includeData}.
     */
    public boolean isIncludeData() {
        return includeData;
    }

    /**
     * @param data See {@link QueryRequest#data}
     * @return This object, to allow chaining calls.
     */
    public QueryRequest data(float[][] data) {
        this.data = data;
        return this;
    }

    /**
     * @return See {@link QueryRequest#data}.
     */
    public float[][] getData() {
        return data;
    }

    /**
     * @param namespace See {@link QueryRequest#namespace}
     * @return This object, to allow chaining calls.
     */
    public QueryRequest namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    /**
     * @return See {@link QueryRequest#namespace}.
     */
    public String getNamespace() {
        return namespace;
    }

    @Override
    protected Core.Request.Builder customizeBaseRequest(Core.Request.Builder builder) {
        builder.setPath(READ_PATH);
        if(namespace != null) builder.setNamespace(namespace);
        return builder.setQuery(Core.QueryRequest.newBuilder()
                .setTopK(topK)
                .setIncludeData(includeData)
                .setData(translator.translate(data))
                .build());
    }
}
