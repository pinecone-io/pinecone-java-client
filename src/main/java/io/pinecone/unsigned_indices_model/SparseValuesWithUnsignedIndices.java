package io.pinecone.unsigned_indices_model;

import io.pinecone.proto.SparseValues;

import java.util.Collections;
import java.util.List;

import static io.pinecone.utils.SparseIndicesConverter.convertSigned32IntToUnsigned32Int;

/**
 * Represents sparse values with unsigned indices, typically used within scored vectors.
 * This class allows manipulation and conversion of sparse values with indices represented as unsigned 32-bit integers.
 */
public class SparseValuesWithUnsignedIndices {

    private List<Long> indicesWithUnsigned32Int;
    private List<Float> values;

    /**
     * Constructs a SparseValuesWithUnsignedIndices object with empty lists for indices and values.
     */
    public SparseValuesWithUnsignedIndices() {
        this.indicesWithUnsigned32Int = Collections.emptyList();
        this.values = Collections.emptyList();
    }

    /**
     * Constructs a SparseValuesWithUnsignedIndices object with the provided lists of indices and values.
     *
     * @param indicesWithUnsigned32Int A list of unsigned 32-bit integers representing indices.
     * @param values                   A list of floating-point values.
     */
    public SparseValuesWithUnsignedIndices(List<Long> indicesWithUnsigned32Int, List<Float> values) {
        this.indicesWithUnsigned32Int = indicesWithUnsigned32Int;
        this.values = values;
    }

    /**
     * Constructs a SparseValuesWithUnsignedIndices object from a ProtoBuf SparseValues object.
     * Converts signed 32-bit integer indices to unsigned 32-bit integers.
     *
     * @param sparseValues A ProtoBuf SparseValues object.
     */
    public SparseValuesWithUnsignedIndices(SparseValues sparseValues) {
        if (sparseValues == null) {
            this.indicesWithUnsigned32Int = Collections.emptyList();
            this.values = Collections.emptyList();
        } else {
            this.indicesWithUnsigned32Int = convertSigned32IntToUnsigned32Int(sparseValues.getIndicesList());
            this.values = sparseValues.getValuesList();
        }
    }

    /**
     * Returns the list of unsigned 32-bit integer indices.
     *
     * @return The list of indices represented as unsigned 32-bit integers.
     */
    public List<Long> getIndicesWithUnsigned32IntList() {
        return indicesWithUnsigned32Int;
    }

    /**
     * Sets the list of unsigned 32-bit integer indices.
     *
     * @param indicesWithUnsigned32Int The list of indices to set, represented as unsigned 32-bit integers.
     */
    public void setIndicesWithUnsigned32Int(List<Long> indicesWithUnsigned32Int) {
        this.indicesWithUnsigned32Int = indicesWithUnsigned32Int;
    }

    /**
     * Returns the list of floating-point values.
     *
     * @return The list of floating-point values.
     */
    public List<Float> getValuesList() {
        return values;
    }

    /**
     * Sets the list of floating-point values.
     *
     * @param values The list of values to set.
     */
    public void setValues(List<Float> values) {
        this.values = values;
    }

    /**
     * Converts the object to a string with each line indented by 4 spaces (except the first line).
     *
     * @param o The object to convert.
     * @return The string representation of the object with indentation.
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    /**
     * Returns a string representation of the SparseValuesWithUnsignedIndices object.
     *
     * @return A string representation of the object.
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SparseValuesWithUnsignedIndices {").append("\n");
        sb.append("    indicesWithUnsigned32Int: ").append(toIndentedString(indicesWithUnsigned32Int)).append("\n");
        sb.append("    values: ").append(toIndentedString(values)).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
