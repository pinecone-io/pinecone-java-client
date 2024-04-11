package io.pinecone.unsigned_indices_model;

import com.google.protobuf.Struct;

import java.util.List;

/**
 * This class represents a vector with sparse values, where the indices of the sparse values are represented
 * as unsigned 32-bit integers.
 * <p>
 * The `VectorWithUnsignedIndices` class contains the following fields:
 * - `id`: the identifier of the vector
 * - `values`: the dense vector values
 * - `metadata`: the metadata associated with the vector
 * - `sparseValuesWithUnsignedIndices`: the sparse values associated with the vector, using unsigned 32-bit integer indices
 * <p>
 * The class provides constructors to create `VectorWithUnsignedIndices` objects, as well as getter and setter
 * methods for each of the fields.
 */
public class VectorWithUnsignedIndices {

    /**
     * The identifier of the vector.
     */
    private String id;

    /**
     * The dense vector values.
     */
    private List<Float> values;

    /**
     * The metadata associated with the vector.
     */
    private Struct metadata;

    /**
     * The sparse values associated with the vector, using unsigned 32-bit integer indices.
     */
    private SparseValuesWithUnsignedIndices sparseValuesWithUnsignedIndices;

    /**
     * Constructs an empty `VectorWithUnsignedIndices` object.
     */
    public VectorWithUnsignedIndices() {}

    /**
     * Constructs a `VectorWithUnsignedIndices` object with the given identifier and dense vector values.
     *
     * @param id     the identifier of the vector
     * @param values the dense vector values
     */
    public VectorWithUnsignedIndices(String id, List<Float> values) {
        this.id = id;
        this.values = values;
    }

    /**
     * Constructs a `VectorWithUnsignedIndices` object with the given identifier, dense vector values, metadata,
     * and sparse values with unsigned 32-bit integer indices.
     *
     * @param id                              the identifier of the vector
     * @param values                         the dense vector values
     * @param metadata                       the metadata associated with the vector
     * @param sparseValuesWithUnsignedIndices the sparse values associated with the vector, using unsigned 32-bit integer indices
     */
    public VectorWithUnsignedIndices(String id, List<Float> values, Struct metadata, SparseValuesWithUnsignedIndices sparseValuesWithUnsignedIndices) {
        this.id = id;
        this.values = values;
        this.metadata = metadata;
        this.sparseValuesWithUnsignedIndices = sparseValuesWithUnsignedIndices;
    }

    /**
     * Returns the identifier of the vector.
     *
     * @return the identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the identifier of the vector.
     *
     * @param id the new identifier
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the dense vector values.
     *
     * @return the list of values
     */
    public List<Float> getValuesList() {
        return values;
    }

    /**
     * Sets the dense vector values.
     *
     * @param values the new list of values
     */
    public void setValues(List<Float> values) {
        this.values = values;
    }

    /**
     * Returns the metadata associated with the vector.
     *
     * @return the metadata
     */
    public Struct getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata associated with the vector.
     *
     * @param metadata the new metadata
     */
    public void setMetadata(Struct metadata) {
        this.metadata = metadata;
    }

    /**
     * Returns the sparse values associated with the vector, using unsigned 32-bit integer indices.
     *
     * @return the `SparseValuesWithUnsignedIndices` object
     */
    public SparseValuesWithUnsignedIndices getSparseValuesWithUnsignedIndices() {
        return sparseValuesWithUnsignedIndices;
    }

    /**
     * Sets the sparse values associated with the vector, using unsigned 32-bit integer indices.
     *
     * @param sparseValuesWithUnsignedIndices the new `SparseValuesWithUnsignedIndices` object
     */
    public void setSparseValuesWithUnsignedIndices(SparseValuesWithUnsignedIndices sparseValuesWithUnsignedIndices) {
        this.sparseValuesWithUnsignedIndices = sparseValuesWithUnsignedIndices;
    }
}
