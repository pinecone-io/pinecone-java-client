package io.pinecone.unsigned_indices_model;

import com.google.protobuf.Struct;

import java.util.List;

public class VectorWithUnsignedIndices {

    private String id;
    private List<Float> values;
    private Struct metadata;
    private SparseValuesWithUnsignedIndices sparseValuesWithUnsignedIndices;

    public VectorWithUnsignedIndices() {}

    public VectorWithUnsignedIndices(String id, List<Float> values) {
        this.id = id;
        this.values = values;
    }

    public VectorWithUnsignedIndices(String id, List<Float> values, Struct metadata, SparseValuesWithUnsignedIndices sparseValuesWithUnsignedIndices) {
        this.id = id;
        this.values = values;
        this.metadata = metadata;
        this.sparseValuesWithUnsignedIndices = sparseValuesWithUnsignedIndices;
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
