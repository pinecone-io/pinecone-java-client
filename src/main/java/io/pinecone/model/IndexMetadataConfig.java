package io.pinecone.model;

import java.util.ArrayList;
import java.util.List;

public class IndexMetadataConfig {
    private List<String> indexed;

    public IndexMetadataConfig() {
    }

    public IndexMetadataConfig indexed(List<String> indexed) {
        this.indexed = indexed;
        return this;
    }

    public IndexMetadataConfig addIndexedItem(String indexedItem) {
        if (this.indexed == null) {
            this.indexed = new ArrayList<>();
        }
        this.indexed.add(indexedItem);
        return this;
    }

    /**
     * A list of metadata fields to index.
     * @return indexed
     **/
    @javax.annotation.Nullable
    public List<String> getIndexed() {
        return indexed;
    }

    public void setIndexed(List<String> indexed) {
        this.indexed = indexed;
    }
}
