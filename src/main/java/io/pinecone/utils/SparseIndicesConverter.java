package io.pinecone.utils;

import io.pinecone.exceptions.PineconeValidationException;

import java.util.ArrayList;
import java.util.List;

public class SparseIndicesConverter {
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
