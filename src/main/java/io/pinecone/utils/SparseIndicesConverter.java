package io.pinecone.utils;

import io.pinecone.exceptions.PineconeValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts sparse vector indices between the {@code Long} values you work with and the {@code int} values the
 * generated gRPC classes store.
 *
 * <p>Sparse indices are unsigned 32-bit integers, from 0 to 4294967295. Java has no unsigned {@code int}, so the
 * generated {@link io.pinecone.proto.SparseValues} class stores each index in an {@code int}, and an index of
 * 2147483648 or more reads back as a negative number. The upsert and query methods on
 * {@link io.pinecone.clients.Index} and {@link io.pinecone.clients.AsyncIndex} take and return {@code Long} indices
 * and call this class for you. Call it yourself when you read sparse values from a response that holds the
 * generated classes, such as a fetch response.
 *
 * <pre>{@code
 * FetchResponse response = index.fetch(Arrays.asList("doc-42"), "articles-en");
 * Vector vector = response.getVectorsMap().get("doc-42");
 * List<Long> indices =
 *     SparseIndicesConverter.convertSigned32IntToUnsigned32Int(vector.getSparseValues().getIndicesList());
 * }</pre>
 */
public class SparseIndicesConverter {
    /**
     * Converts unsigned sparse indices held in {@code Long} values to the {@code int} form the generated gRPC
     * classes store.
     *
     * <p>Indices of 2147483648 and above become negative {@code int} values with the same 32 bits, which the
     * server reads back as the original unsigned index.
     *
     * <pre>{@code
     * SparseValues sparse = SparseValues.newBuilder()
     *     .addAllIndices(SparseIndicesConverter.convertUnsigned32IntToSigned32Int(Arrays.asList(17L, 3000000000L)))
     *     .addAllValues(Arrays.asList(0.42f, 0.18f))
     *     .build();
     * }</pre>
     *
     * @param unsigned32IntValues sparse indices, each from 0 to 4294967295
     * @return the indices as {@code int} values, in the same order
     * @throws PineconeValidationException if any index is negative or greater than 4294967295
     */
    public static List<Integer> convertUnsigned32IntToSigned32Int(List<Long> unsigned32IntValues) {
        List<Integer> int32Values = new ArrayList<>();
        for (Long value : unsigned32IntValues) {
            if (value < 0 || value > 0xFFFFFFFFL) {
                throw new PineconeValidationException("Sparse indices are out of range for unsigned 32-bit integers.");
            }
            int32Values.add(value.intValue());
        }
        return int32Values;
    }

    /**
     * Converts sparse indices read from a generated gRPC class back to their unsigned values.
     *
     * <p>Negative {@code int} values become the unsigned index they stand for: -1 becomes 4294967295.
     *
     * <pre>{@code
     * List<Long> indices =
     *     SparseIndicesConverter.convertSigned32IntToUnsigned32Int(vector.getSparseValues().getIndicesList());
     * }</pre>
     *
     * @param signed32IntValues sparse indices as stored in {@link io.pinecone.proto.SparseValues}
     * @return the unsigned indices, each from 0 to 4294967295, in the same order
     */
    public static List<Long> convertSigned32IntToUnsigned32Int(List<Integer> signed32IntValues) {
        List<Long> uint32Values = new ArrayList<>();
        for (Integer value : signed32IntValues) {
            if (value < 0) {
                uint32Values.add((long) value + 4294967296L);
            } else {
                uint32Values.add((long) value);
            }
        }
        return uint32Values;
    }
}
