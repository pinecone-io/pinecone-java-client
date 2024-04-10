package io.pinecone;

import com.google.protobuf.Struct;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.unsigned_indices_model.SparseValuesWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
import org.junit.jupiter.api.Test;

import static io.pinecone.GenerateMetadata.generateMetadataStruct;
import static io.pinecone.commons.IndexInterface.buildUpsertVectorWithUnsignedIndices;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

public class BuildUpsertVectorWithUnsignedIndicesTest {

    @Test
    void testRequiredAndOptionalParameters() {
        String id = "some_id";
        List<Float> values = Arrays.asList(1.0f, 2.0f, 3.0f);
        List<Long> sparseIndices = Arrays.asList(0L, 2L);
        List<Float> sparseValues = Arrays.asList(1.5f, 3.5f);
        SparseValuesWithUnsignedIndices sparseValuesWithUnsignedIndices = new SparseValuesWithUnsignedIndices(sparseIndices, sparseValues);
        Struct metadata = generateMetadataStruct(0, 0);

        VectorWithUnsignedIndices result = new VectorWithUnsignedIndices(id, values, metadata, sparseValuesWithUnsignedIndices);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(values, result.getValuesList());
        assertNotNull(result.getSparseValuesWithUnsignedIndices());
        assertEquals(sparseIndices, result.getSparseValuesWithUnsignedIndices().getIndicesWithUnsigned32IntList());
        assertEquals(sparseValues, result.getSparseValuesWithUnsignedIndices().getValuesList());
        assertEquals(metadata, result.getMetadata());
    }

    @Test
    void testRequiredParametersOnly() {
        String id = "some_id";
        List<Float> values = Arrays.asList(1.0f, 2.0f, 3.0f);
        SparseValuesWithUnsignedIndices sparseValuesWithUnsignedIndices = null;
        Struct metadata = null;

        VectorWithUnsignedIndices result = new VectorWithUnsignedIndices(id, values, metadata, sparseValuesWithUnsignedIndices);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(values, result.getValuesList());
        assertNull(result.getSparseValuesWithUnsignedIndices());
        assertNull(result.getMetadata());
    }

    @Test
    void testNullId() {
        String id = null;
        List<Float> values = Arrays.asList(1.0f, 2.0f, 3.0f);
        List<Long> sparseIndices = Arrays.asList(0L, 2L);
        List<Float> sparseValues = Arrays.asList(1.5f, 3.5f);
        Struct metadata = Struct.newBuilder().build();

        try {
            buildUpsertVectorWithUnsignedIndices(id, values, sparseIndices, sparseValues, metadata);

            fail("Expecting PineconeValidationException");
        } catch (PineconeValidationException exception) {

            assertEquals(exception.getLocalizedMessage(), "Invalid upsert request. Please ensure that both id " +
                    "and values are provided.");
        }
    }

    @Test
    void testNullValues() {
        String id = "some_id";
        List<Float> values = null;
        List<Long> sparseIndices = Arrays.asList(0L, 2L);
        List<Float> sparseValues = Arrays.asList(1.5f, 3.5f);
        Struct metadata = Struct.newBuilder().build();

        try {
            buildUpsertVectorWithUnsignedIndices(id, values, sparseIndices, sparseValues, metadata);

            fail("Expecting PineconeValidation exception");
        } catch (PineconeValidationException exception) {
            assertEquals(exception.getLocalizedMessage(), "Invalid upsert request. Please ensure that both id " +
                    "and values are provided.");
        }
    }

    @Test
    void testSparseValuseIndicesSizeMismatch() {
        String id = "some_id";
        List<Float> values = Arrays.asList(1.0f, 2.0f, 3.0f);
        List<Long> sparseIndices = Arrays.asList(0L, 1L, 2L);
        List<Float> sparseValues = Arrays.asList(1.5f, 3.5f);
        Struct metadata = null;

        try {
            buildUpsertVectorWithUnsignedIndices(id, values, sparseIndices, sparseValues, metadata);

            fail("Expecting PineconeValidation exception");
        } catch (PineconeValidationException exception) {
            assertEquals(exception.getLocalizedMessage(), "Invalid upsert request. Please ensure that both " +
                    "sparse indices and values are of the same length.");
        }
    }
}
