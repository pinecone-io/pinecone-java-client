package io.pinecone.unsigned_indices_model;

import io.pinecone.proto.SparseValues;

import java.util.Collections;
import java.util.List;

import static io.pinecone.utils.SparseIndicesConverter.convertSigned32IntToUnsigned32Int;
/**
 * The sparse part of a vector: two parallel lists, where the value at position i belongs to the index at
 * position i.
 *
 * <p>Indices are {@code Long} so they can hold the full unsigned 32-bit range, 0 to 4294967295, which the
 * generated {@link SparseValues} class can only store as signed {@code int}. You get one from each match of a
 * query on {@link io.pinecone.clients.Index} or {@link io.pinecone.clients.AsyncIndex}, and you can build one to
 * attach to a {@link VectorWithUnsignedIndices} for upsert.
 *
 * <pre>{@code
 * SparseValuesWithUnsignedIndices sparse = new SparseValuesWithUnsignedIndices(
 *     Arrays.asList(102L, 4017L, 3000000000L),
 *     Arrays.asList(0.62f, 0.31f, 0.07f));
 * }</pre>
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
     * Creates sparse values with no indices and no values.
     */
    public SparseValuesWithUnsignedIndices() {
        this.indicesWithUnsigned32Int = Collections.emptyList();
        this.values = Collections.emptyList();
    }

    /**
     * Creates sparse values from parallel lists of indices and values.
     *
     * <p>The constructor does not check the lists. When you upsert, the client rejects lists of different lengths
     * and indices outside 0 to 4294967295 with a
     * {@link io.pinecone.exceptions.PineconeValidationException}.
     *
     * <pre>{@code
     * SparseValuesWithUnsignedIndices sparse = new SparseValuesWithUnsignedIndices(
     *     Arrays.asList(102L, 4017L),
     *     Arrays.asList(0.62f, 0.31f));
     * }</pre>
     *
     * @param indicesWithUnsigned32Int the dimensions that have a value, each from 0 to 4294967295, such as
     *                                 token ids from a sparse embedding
     * @param values the value for each index, in the same order and of the same length
     */
    public SparseValuesWithUnsignedIndices(List<Long> indicesWithUnsigned32Int, List<Float> values) {
        this.indicesWithUnsigned32Int = indicesWithUnsigned32Int;
        this.values = values;
    }

    /**
     * Creates sparse values from the generated {@link SparseValues} class, converting each index back to its
     * unsigned value.
     *
     * <pre>{@code
     * SparseValuesWithUnsignedIndices sparse =
     *     new SparseValuesWithUnsignedIndices(vector.getSparseValues());
     * }</pre>
     *
     * @param sparseValues sparse values read from a gRPC response, such as a fetched vector; null gives empty lists
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
     * The dimensions that have a value, each from 0 to 4294967295.
     *
     * @return the indices, parallel to {@link #getValuesList()}
     */
    public List<Long> getIndicesWithUnsigned32IntList() {
        return indicesWithUnsigned32Int;
    }

    /**
     * Replaces the indices; keep them the same length as the values.
     *
     * @param indicesWithUnsigned32Int the dimensions that have a value, each from 0 to 4294967295
     */
    public void setIndicesWithUnsigned32Int(List<Long> indicesWithUnsigned32Int) {
        this.indicesWithUnsigned32Int = indicesWithUnsigned32Int;
    }

    /**
     * The value at each index.
     *
     * @return the values, parallel to {@link #getIndicesWithUnsigned32IntList()}
     */
    public List<Float> getValuesList() {
        return values;
    }

    /**
     * Replaces the values; keep them the same length as the indices.
     *
     * @param values the value for each index, in the same order
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
}
