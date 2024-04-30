package io.pinecone;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.pinecone.proto.SparseValues;
import io.pinecone.proto.Vector;
import io.pinecone.unsigned_indices_model.SparseValuesWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GetSerializedSizeTest {

    @Test
    public void testGetSerializedSize_WithIdAndValues() {
        String id = "v1";
        List<Float> values = Arrays.asList(1f, 2f, 3f);

        Vector vector = Vector.newBuilder()
                .setId(id)
                .addAllValues(values)
                .build();
        VectorWithUnsignedIndices vectorWithUnsignedIndices = new VectorWithUnsignedIndices(id,
                Arrays.asList(1f, 2f, 3f));

        int expectedSize = vector.getSerializedSize();
        int actualSize = vectorWithUnsignedIndices.getSerializedSize();

        assertEquals(expectedSize, actualSize);
    }

    @Test
    public void testGetSerializedSize_EmptyVector() {
        Vector vector = Vector.newBuilder().build();
        VectorWithUnsignedIndices vectorWithUnsignedIndices = new VectorWithUnsignedIndices();

        int expectedSize = vector.getSerializedSize();
        int actualSize = vectorWithUnsignedIndices.getSerializedSize();

        assertEquals(expectedSize, actualSize);
    }

    @Test
    public void testGetSerializedSize_WithMetadata() {
        String id = "v1";
        List<Float> values = Arrays.asList(1f, 2f, 3f);

        Struct.Builder metadataBuilder = Struct.newBuilder();

        for(int i=0; i<1500; i++) {
            metadataBuilder.putFields("somerandomfield" + i, Value.newBuilder().setStringValue("thriller").build());
        }

        Vector vector = Vector.newBuilder()
                .setId(id).
                addAllValues(values).
                setMetadata(metadataBuilder.build())
                .build();

        VectorWithUnsignedIndices vectorWithUnsignedIndices = new VectorWithUnsignedIndices(id,
                values,
                metadataBuilder.build(),
                null);

        int expectedSize = vector.getSerializedSize();
        int actualSize = vectorWithUnsignedIndices.getSerializedSize();

        assertEquals(expectedSize, actualSize);
    }

    @Test
    public void testGetSerializedSize_WithSparseValues() {
        String id = "v1";
        List<Float> values = Arrays.asList(1f, 2f, 3f);
        Struct metadata = Struct.newBuilder()
                .putFields("genre", Value.newBuilder().setStringValue("thriller").build())
                .putFields("year", Value.newBuilder().setNumberValue(2020).build())
                .build();
        List<Long> unsignedSparseIndices = Arrays.asList(1L, 2L, 3L);
        SparseValues sparseValues = SparseValues.newBuilder()
                .addAllIndices(Arrays.asList(1, 2, 3))
                .addAllValues(values)
                .build();

        SparseValuesWithUnsignedIndices sparseValuesWithUnsignedIndices = new SparseValuesWithUnsignedIndices(unsignedSparseIndices, values);

        Vector vector = Vector.newBuilder()
                .setId(id)
                .addAllValues(values)
                .setMetadata(metadata)
                .setSparseValues(sparseValues)
                .build();

        VectorWithUnsignedIndices vectorWithUnsignedIndices = new VectorWithUnsignedIndices(id,
                values,
                metadata,
                sparseValuesWithUnsignedIndices);

        int expectedSize = vector.getSerializedSize();
        int actualSize = vectorWithUnsignedIndices.getSerializedSize();

        assertEquals(expectedSize, actualSize);
    }
}
