package io.pinecone.unsigned_indices_model;

import com.google.protobuf.Struct;
import java.util.List;

/**
 * Represents a vector with unsigned indices.
 */
public class VectorWithUnsignedIndices {

    private String id;
    private List<Float> values;
    private Struct metadata;
    private SparseValuesWithUnsignedIndices sparseValuesWithUnsignedIndices;

    /**
     * Constructs an empty VectorWithUnsignedIndices object.
     */
    public VectorWithUnsignedIndices() {}

    /**
     * Constructs a VectorWithUnsignedIndices object with the given ID and values.
     * @param id The ID of the vector.
     * @param values The list of floating-point values representing the vector.
     */
    public VectorWithUnsignedIndices(String id, List<Float> values) {
        this.id = id;
        this.values = values;
    }

    /**
     * Constructs a VectorWithUnsignedIndices object with the given ID, values, metadata, and sparse values with unsigned indices.
     * @param id The ID of the vector.
     * @param values The list of floating-point values representing the vector.
     * @param metadata Additional metadata for the vector.
     * @param sparseValuesWithUnsignedIndices Sparse values with unsigned indices associated with the vector.
     */
    public VectorWithUnsignedIndices(String id, List<Float> values, Struct metadata, SparseValuesWithUnsignedIndices sparseValuesWithUnsignedIndices) {
        this.id = id;
        this.values = values;
        this.metadata = metadata;
        this.sparseValuesWithUnsignedIndices = sparseValuesWithUnsignedIndices;
    }

    /**
     * Gets the ID of the vector.
     * @return The ID of the vector.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of the vector.
     * @param id The ID of the vector.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the list of floating-point values representing the vector.
     * @return The list of floating-point values representing the vector.
     */
    public List<Float> getValuesList() {
        return values;
    }

    /**
     * Sets the list of floating-point values representing the vector.
     * @param values The list of floating-point values representing the vector.
     */
    public void setValues(List<Float> values) {
        this.values = values;
    }

    /**
     * Gets the metadata associated with the vector.
     * @return The metadata associated with the vector.
     */
    public Struct getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata associated with the vector.
     * @param metadata The metadata associated with the vector.
     */
    public void setMetadata(Struct metadata) {
        this.metadata = metadata;
    }

    /**
     * Gets the sparse values with unsigned indices associated with the vector.
     * @return The sparse values with unsigned indices associated with the vector.
     */
    public SparseValuesWithUnsignedIndices getSparseValuesWithUnsignedIndices() {
        return sparseValuesWithUnsignedIndices;
    }

    /**
     * Sets the sparse values with unsigned indices associated with the vector.
     * @param sparseValuesWithUnsignedIndices The sparse values with unsigned indices associated with the vector.
     */
    public void setSparseValuesWithUnsignedIndices(SparseValuesWithUnsignedIndices sparseValuesWithUnsignedIndices) {
        this.sparseValuesWithUnsignedIndices = sparseValuesWithUnsignedIndices;
    }
}
