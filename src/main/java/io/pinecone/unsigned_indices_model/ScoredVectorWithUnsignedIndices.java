package io.pinecone.unsigned_indices_model;

import com.google.protobuf.Struct;
import io.pinecone.proto.ScoredVector;

import java.util.Collections;
import java.util.List;

/**
 * Represents a scored vector with unsigned indices, typically used within query responses.
 * This class allows manipulation and conversion of scored vectors with indices represented as unsigned 32-bit integers.
 */
public class ScoredVectorWithUnsignedIndices {

    // Fields for score, ID, values, metadata, and sparse values with unsigned indices
    private float score;
    private String id;
    private List<Float> values;
    private Struct metadata;
    private SparseValuesWithUnsignedIndices sparseValuesWithUnsignedIndices;

    /**
     * Constructs a ScoredVectorWithUnsignedIndices object with default values for score, ID, values, metadata,
     * and an empty SparseValuesWithUnsignedIndices object.
     */
    public ScoredVectorWithUnsignedIndices() {
        this.score = 0F;
        this.id = "";
        this.values = Collections.emptyList();
        this.metadata = Struct.newBuilder().build();
        this.sparseValuesWithUnsignedIndices = new SparseValuesWithUnsignedIndices();
    }

    /**
     * Constructs a ScoredVectorWithUnsignedIndices object from a ProtoBuf ScoredVector object.
     * Converts signed 32-bit integer indices to unsigned 32-bit integers for compatibility.
     *
     * @param scoredVector A ProtoBuf ScoredVector object representing a scored vector with signed 32-bit integer indices.
     */
    public ScoredVectorWithUnsignedIndices(ScoredVector scoredVector) {
        if (scoredVector == null) {
            // Set default values if scoredVector is null
            this.score = 0F;
            this.id = "";
            this.values = Collections.emptyList();
            this.metadata = Struct.newBuilder().build();
            this.sparseValuesWithUnsignedIndices = new SparseValuesWithUnsignedIndices();
        } else {
            // Set values from the provided ScoredVector object
            this.score = scoredVector.getScore();
            this.id = scoredVector.getId();
            this.values = scoredVector.getValuesList();
            this.metadata = scoredVector.getMetadata();
            this.sparseValuesWithUnsignedIndices = new SparseValuesWithUnsignedIndices(scoredVector.getSparseValues());
        }
    }

    /**
     * Returns the score of the scored vector.
     *
     * @return The score of the scored vector.
     */
    public float getScore() {
        return score;
    }

    /**
     * Sets the score of the scored vector.
     *
     * @param score The score to set.
     */
    public void setScore(float score) {
        this.score = score;
    }

    /**
     * Returns the ID of the scored vector.
     *
     * @return The ID of the scored vector.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of the scored vector.
     *
     * @param id The ID to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the list of values of the scored vector.
     *
     * @return The list of values of the scored vector.
     */
    public List<Float> getValuesList() {
        return values;
    }

    /**
     * Sets the list of values of the scored vector.
     *
     * @param values The list of values to set.
     */
    public void setValues(List<Float> values) {
        this.values = values;
    }

    /**
     * Returns the metadata of the scored vector.
     *
     * @return The metadata of the scored vector.
     */
    public Struct getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata of the scored vector.
     *
     * @param metadata The metadata to set.
     */
    public void setMetadata(Struct metadata) {
        this.metadata = metadata;
    }

    /**
     * Returns the sparse values with unsigned indices of the scored vector.
     *
     * @return The sparse values with unsigned indices of the scored vector.
     */
    public SparseValuesWithUnsignedIndices getSparseValuesWithUnsignedIndices() {
        return sparseValuesWithUnsignedIndices;
    }

    /**
     * Sets the sparse values with unsigned indices of the scored vector.
     *
     * @param sparseValuesWithUnsignedIndices The sparse values with unsigned indices to set.
     */
    public void setSparseValuesWithUnsignedIndices(SparseValuesWithUnsignedIndices sparseValuesWithUnsignedIndices) {
        this.sparseValuesWithUnsignedIndices = sparseValuesWithUnsignedIndices;
    }

    /**
     * Returns a string representation of the ScoredVectorWithUnsignedIndices object.
     *
     * @return A string representation of the object.
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ScoredVectorWithUnsignedIndices {").append("\n");
        sb.append("    score: ").append(score).append("\n");
        sb.append("    id: ").append(id).append("\n");
        sb.append("    values: ").append(values).append("\n");
        sb.append("    metadata: ").append(metadata).append("\n");
        sb.append("    sparseValuesWithUnsignedIndices: ").append(sparseValuesWithUnsignedIndices).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
