package io.pinecone.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IndexMeta {
    @JsonProperty("database")
    private IndexMetaDatabase metaDatabase;

    private IndexStatus status;

    public IndexMeta () {
    }

    public IndexMetaDatabase getMetaDatabase() {
        return metaDatabase;
    }

    public IndexMeta withMetaDatabase(IndexMetaDatabase metaDatabase) {
        this.metaDatabase = metaDatabase;
        return this;
    }

    public IndexStatus getStatus() {
        return status;
    }

    public IndexMeta withStatus(IndexStatus status) {
        this.status = status;
        return this;
    }

    public IndexMeta fromJsonString(String indexMetaString) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(indexMetaString, IndexMeta.class);
    }

    @Override
    public String toString() {
        return "IndexMeta{" +
                "metaDatabase=" + metaDatabase +
                ", status=" + status +
                '}';
    }
}
