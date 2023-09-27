package io.pinecone.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IndexMeta {
    private IndexMetaDatabase database;

    private IndexStatus status;

    public IndexMeta () {
    }

    public IndexMetaDatabase getDatabase() {
        return database;
    }

    public IndexMeta withMetaDatabase(IndexMetaDatabase database) {
        this.database = database;
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
                "database=" + database +
                ", status=" + status +
                '}';
    }
}
