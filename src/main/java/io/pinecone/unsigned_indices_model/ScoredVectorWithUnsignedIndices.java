package io.pinecone.unsigned_indices_model;

import com.google.protobuf.Struct;
import io.pinecone.proto.ScoredVector;

import java.util.Collections;
import java.util.List;

public class ScoredVectorWithUnsignedIndices {

    private float score;
    private String id;
    private List<Float> values;
    private Struct metadata;
    private SparseValuesWithUnsignedIndices sparseValuesWithUnsignedIndices;

    public ScoredVectorWithUnsignedIndices(ScoredVector scoredVector) {
        if (scoredVector == null) {
            this.score = 0F;
            this.id = "";
            this.values = Collections.emptyList();
            this.metadata = Struct.newBuilder().build();
            this.sparseValuesWithUnsignedIndices = new SparseValuesWithUnsignedIndices();
        } else {
            this.score = scoredVector.getScore();
            this.id = scoredVector.getId();
            this.values = scoredVector.getValuesList();
            this.metadata = scoredVector.getMetadata();
            this.sparseValuesWithUnsignedIndices = new SparseValuesWithUnsignedIndices(scoredVector.getSparseValues());
        }
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Float> getValuesList() {
        return values;
    }

    public void setValues(List<Float> values) {
        this.values = values;
    }

    public Struct getMetadata() {
        return metadata;
    }

    public void setMetadata(Struct metadata) {
        this.metadata = metadata;
    }

    public SparseValuesWithUnsignedIndices getSparseValuesWithUnsignedIndices() {
        return sparseValuesWithUnsignedIndices;
    }

    public void setSparseValuesWithUnsignedIndices(SparseValuesWithUnsignedIndices sparseValuesWithUnsignedIndices) {
        this.sparseValuesWithUnsignedIndices = sparseValuesWithUnsignedIndices;
    }
}
