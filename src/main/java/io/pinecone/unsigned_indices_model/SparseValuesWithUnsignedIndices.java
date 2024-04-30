package io.pinecone.unsigned_indices_model;

import io.pinecone.proto.SparseValues;

import java.util.Collections;
import java.util.List;

import static io.pinecone.utils.SparseIndicesConverter.convertSigned32IntToUnsigned32Int;
/**
 * This class represents a set of sparse values, where the indices are represented as unsigned 32-bit integers.
 * Unlike the {@link SparseValues} class, which uses signed 32-bit integers for the indices, this class uses {@link Long}
 * to represent the indices, allowing for the full range of unsigned 32-bit integers (0 to 4,294,967,295).
 * <p>
 * The indicesWithUnsigned32Int list contains the indices, while the values list contains the corresponding
 * values. The two lists are parallel, meaning that the value at index i in the values list corresponds to
 * the index at index i in the indicesWithUnsigned32Int list.
 * <p>
 * This class provides a constructor that takes a {@link SparseValues} object and converts the signed 32-bit integer
 * indices to unsigned 32-bit integers, as well as methods to get and set the indices and values.
 */
public class SparseValuesWithUnsignedIndices {

    /**
     * The list of indices, represented as unsigned 32-bit integers.
     */
    private List<Long> indicesWithUnsigned32Int;

    /**
     * The list of values corresponding to the indices in indicesWithUnsigned32Int.
     */
    private List<Float> values;

    /**
     * Constructs an empty {@link SparseValuesWithUnsignedIndices} object with empty lists for indices and values.
     */
    public SparseValuesWithUnsignedIndices() {
        this.indicesWithUnsigned32Int = Collections.emptyList();
        this.values = Collections.emptyList();
    }

    /**
     * Constructs a {@link SparseValuesWithUnsignedIndices} object with the given lists of indices and values.
     *
     * @param indicesWithUnsigned32Int The list of indices, represented as unsigned 32-bit integers
     * @param values The list of values corresponding to the indices
     */
    public SparseValuesWithUnsignedIndices(List<Long> indicesWithUnsigned32Int, List<Float> values) {
        this.indicesWithUnsigned32Int = indicesWithUnsigned32Int;
        this.values = values;
    }

    /**
     * Constructs a {@link SparseValuesWithUnsignedIndices} object from a {@link SparseValues} object, converting the
     * signed 32-bit integer indices to unsigned 32-bit integers.
     *
     * @param sparseValues The {@link SparseValues} object to convert
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
     * Returns the list of indices, represented as unsigned 32-bit integers.
     *
     * @return The list of indices
     */
    public List<Long> getIndicesWithUnsigned32IntList() {
        return indicesWithUnsigned32Int;
    }

    /**
     * Sets the list of indices, represented as unsigned 32-bit integers.
     *
     * @param indicesWithUnsigned32Int The new list of indices
     */
    public void setIndicesWithUnsigned32Int(List<Long> indicesWithUnsigned32Int) {
        this.indicesWithUnsigned32Int = indicesWithUnsigned32Int;
    }

    /**
     * Returns the list of values corresponding to the indices.
     *
     * @return The list of values
     */
    public List<Float> getValuesList() {
        return values;
    }

    /**
     * Sets the list of values corresponding to the indices.
     *
     * @param values The new list of values
     */
    public void setValues(List<Float> values) {
        this.values = values;
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
        sb.append("SparseValuesWithUnsignedIndices {").append("\n");
        sb.append("    indicesWithUnsigned32Int: ").append(toIndentedString(indicesWithUnsigned32Int)).append("\n");
        sb.append("    values: ").append(toIndentedString(values)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    public int getSerializedSize() {
        int size = 0;
        {
            int dataSize = 0;
            for (int i = 0; i < indicesWithUnsigned32Int.size(); i++) {
                dataSize += com.google.protobuf.CodedOutputStream
                        .computeInt64SizeNoTag(indicesWithUnsigned32Int.get(i));
            }
            size += dataSize;
            if (!getIndicesWithUnsigned32IntList().isEmpty()) {
                size += 1;
                size += com.google.protobuf.CodedOutputStream
                        .computeInt32SizeNoTag(dataSize);
            }
        }
        {
            int dataSize = 4 * getValuesList().size();
            size += dataSize;
            if (!getValuesList().isEmpty()) {
                size += 1;
                size += com.google.protobuf.CodedOutputStream
                        .computeInt32SizeNoTag(dataSize);
            }
        }
        return size;
    }
}
