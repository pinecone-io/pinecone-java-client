package io.pinecone;

import io.pinecone.exceptions.PineconeValidationException;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static io.pinecone.utils.SparseIndicesConverter.*;
import static org.junit.jupiter.api.Assertions.*;

public class SparseIndicesConverterTest {

    @Test
    public void testUnsigned32IntToSigned32IntConversionWithinRange() {
        List<Long> unsignedValues = Arrays.asList(0L, 100L, 200L, 300L, 2147483647L); // int32 => [0, 2147483647]
        Iterable<? extends Integer> signedValues = convertUnsigned32IntToSigned32Int(unsignedValues);
        Integer[] expected = {0, 100, 200, 300, 2147483647};
        int index = 0;
        for (Integer value : signedValues) {
            assertEquals(expected[index++], value);
        }
    }

    @Test
    public void testUnsigned32IntToSigned32IntConversionOutOfUnsigned32IntRange() {
        List<Long> unsignedValues = Arrays.asList(-1L, 4294967296L); // -1 and 4294967296 are out of unsigned 32-bit range
        assertThrows(PineconeValidationException.class, () -> convertUnsigned32IntToSigned32Int(unsignedValues));
    }

    @Test
    public void testUnsigned32IntToSigned32IntConversionOutOfSigned32IntRange() {
        List<Long> unsignedValues = Arrays.asList(2147483649L, 4294967295L); // Unsigned 32 bit integer range: [0, 4294967295]
        Iterable<? extends Integer> signedValues = convertUnsigned32IntToSigned32Int(unsignedValues);
        Integer[] expected = {-2147483647, -1};
        int index = 0;
        for(int value : signedValues) {
            assertEquals(expected[index++], value);
        }
    }

    @Test
    public void testUnsigned32IntToSigned32IntEmptyList() {
        List<Long> unsignedValues = Arrays.asList();
        Iterable<? extends Integer> signedValues = convertUnsigned32IntToSigned32Int(unsignedValues);
        assertFalse(signedValues.iterator().hasNext(), "Empty list should result in empty result");
    }

    @Test
    public void testSigned32IntToUnsigned32IntConversionWithinRange() {
        List<Integer> signedValues = Arrays.asList(0, 100, 200, 300, 2147483647); // int32 => [0, 2147483647]
        Iterable<? extends Long> unsignedValues = convertSigned32IntToUnsigned32Int(signedValues);
        Long[] expected = {0L, 100L, 200L, 300L, 2147483647L};
        int index = 0;
        for (Long value : unsignedValues) {
            assertEquals(expected[index++], value);
        }
    }

    @Test
    public void testSigned32IntToUnsigned32IntConversionOutOfSigned32IntRange() {
        List<Integer> signedValues = Arrays.asList(-2147483647, -1); // Signed 32 bit integer range: [-2147483648, 2147483647]
        Iterable<? extends Long> unsignedValues = convertSigned32IntToUnsigned32Int(signedValues);
        Long[] expected = {2147483649L, 4294967295L};
        int index = 0;
        for (Long value : unsignedValues) {
            assertEquals(expected[index++], value);
        }
    }

    @Test
    public void testSigned32IntToUnsigned32IntEmptyLis1() {
        List<Integer> signedValues = Arrays.asList();
        Iterable<? extends Long> unsignedValues = convertSigned32IntToUnsigned32Int(signedValues);
        assertFalse(unsignedValues.iterator().hasNext(), "Empty list should result in empty result");
    }
}
