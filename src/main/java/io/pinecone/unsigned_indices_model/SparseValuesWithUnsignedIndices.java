package io.pinecone.unsigned_indices_model;

import io.pinecone.proto.SparseValues;

import java.util.Collections;
import java.util.List;

import static io.pinecone.utils.SparseIndicesConverter.convertSigned32IntToUnsigned32Int;

public class SparseValuesWithUnsignedIndices {
    private List<Long> indicesWithUnsigned32Int;
    private List<Float> values;

    public SparseValuesWithUnsignedIndices(SparseValues sparseValues) {
        if(sparseValues == null){
            this.indicesWithUnsigned32Int = Collections.emptyList();
            this.values = Collections.emptyList();
        }
        this.indicesWithUnsigned32Int = convertSigned32IntToUnsigned32Int(sparseValues.getIndicesList());
        this.values = sparseValues.getValuesList();
    }

    public List<Long> getIndicesWithUnsigned32IntList() {
        return indicesWithUnsigned32Int;
    }

    public void setIndicesWithUnsigned32Int(List<Long> indicesWithUnsigned32Int) {
        this.indicesWithUnsigned32Int = indicesWithUnsigned32Int;
    }

    public List<Float> getValuesList() {
        return values;
    }

    public void setValues(List<Float> values) {
        this.values = values;
    }
}
