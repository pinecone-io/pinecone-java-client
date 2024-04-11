package io.pinecone.unsigned_indices_model;

import com.google.protobuf.Struct;
import io.pinecone.proto.ScoredVector;

import java.util.Collections;
import java.util.List;

/**
 * This class represents a scored vector with unsigned 32-bit integer indices for the sparse values.
 * Unlike the {@link ScoredVector} class, which uses the SparseValues class to represent the sparse values,
 * this class uses the {@link SparseValuesWithUnsignedIndices} class, which allows for the use of unsigned 32-bit
 * integers as indices.
 * <p>
 * The {@link ScoredVectorWithUnsignedIndices} class contains the following fields:
 * - score: the score associated with the vector
 * - id: the identifier of the vector
 * - values: the dense vector values
 * - metadata: the metadata associated with the vector
 * - sparseValuesWithUnsignedIndices: the sparse values associated with the vector, using unsigned 32-bit integer indices
 * <p>
 * The class provides a constructor that takes a {@link ScoredVector} object and converts it to a
 * {@link ScoredVectorWithUnsignedIndices} object, as well as getter and setter methods for each of the fields.
 */
public class ScoredVectorWithUnsignedIndices {

    /**
     * The score associated with the vector.
     */
    private float score;

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
     * Constructs a {@link ScoredVectorWithUnsignedIndices} object from a {@link ScoredVector} object, converting the
     * SparseValues to {@link SparseValuesWithUnsignedIndices}.
     *
     * @param scoredVector the {@link ScoredVector} object to convert
     */
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

    /**
     * Returns the score associated with the vector.
     *
     * @return The score
     */
    public float getScore() {
        return score;
    }

    /**
     * Sets the score associated with the vector.
     *
     * @param score The new score
     */
    public void setScore(float score) {
        this.score = score;
    }

    /**
     * Returns the identifier of the vector.
     *
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the identifier of the vector.
     *
     * @param id The new id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the dense vector values.
     *
     * @return The list of values
     */
    public List<Float> getValuesList() {
        return values;
    }

    /**
     * Sets the dense vector values.
     *
     * @param values The new list of values
     */
    public void setValues(List<Float> values) {
        this.values = values;
    }

    /**
     * Returns the metadata associated with the vector.
     *
     * @return The metadata
     */
    public Struct getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata associated with the vector.
     *
     * @param metadata The new metadata
     */
    public void setMetadata(Struct metadata) {
        this.metadata = metadata;
    }

    /**
     * Returns the sparse values associated with the vector, using unsigned 32-bit integer indices.
     *
     * @return The {@link SparseValuesWithUnsignedIndices} object
     */
    public SparseValuesWithUnsignedIndices getSparseValuesWithUnsignedIndices() {
        return sparseValuesWithUnsignedIndices;
    }

    /**
     * Sets the sparse values associated with the vector, using unsigned 32-bit integer indices.
     *
     * @param sparseValuesWithUnsignedIndices The new {@link SparseValuesWithUnsignedIndices} object
     */
    public void setSparseValuesWithUnsignedIndices(SparseValuesWithUnsignedIndices sparseValuesWithUnsignedIndices) {
        this.sparseValuesWithUnsignedIndices = sparseValuesWithUnsignedIndices;
    }

    /**
     * Converts the given object to a string with each line indented by 4 spaces (except the first line).
     *
     * @param o The object to convert to a string
     * @return The indented string representation of the object
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ScoredVectorWithUnsignedIndices {").append("\n");
        sb.append("    score: ").append(toIndentedString(score)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    values: ").append(toIndentedString(values)).append("\n");
        sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
        sb.append("    sparseValuesWithUnsignedIndices: ").append(toIndentedString(sparseValuesWithUnsignedIndices)).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
