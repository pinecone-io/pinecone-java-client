package io.pinecone;

import io.pinecone.exceptions.PineconeValidationException;

import java.util.ArrayList;
import java.util.List;

public class Helpers {
    public static Iterable<? extends Integer> convertUnsigned32IntToSigned32Int(Iterable<? extends Long> unsigned32IntValues) {
        List<Integer> int32Values = new ArrayList<>();
        for (Long value : unsigned32IntValues) {
            if (value < 0 || value > 0xFFFFFFFFL) {
                throw new PineconeValidationException("Sparse indices are out of range for unsigned 32-bit integers.");
            }
            int32Values.add(value.intValue());
        }
        return int32Values;
    }
}
