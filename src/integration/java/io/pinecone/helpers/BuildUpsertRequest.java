package io.pinecone.helpers;

import com.google.common.primitives.Floats;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.pinecone.proto.SparseValues;
import io.pinecone.proto.UpsertRequest;
import io.pinecone.proto.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BuildUpsertRequest {
    private static final float[][] upsertData = {{1.0F, 2.0F, 3.0F}, {4.0F, 5.0F, 6.0F}, {7.0F, 8.0F, 9.0F}};

    public static UpsertRequest buildRequiredUpsertRequest() {
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
        List<Vector> upsertVectors = new ArrayList<>();

        for (int i = 0; i < upsertData.length; i++) {
            upsertVectors.add(Vector.newBuilder()
                    .addAllValues(Floats.asList(upsertData[i]))
                    .setMetadata(Struct.newBuilder()
                            .putFields("some_field", Value.newBuilder().setNumberValue(i).build())
                            .build())
                    .setId(upsertIds.get(i))
                    .build());
        }

        return UpsertRequest.newBuilder()
                .addAllVectors(upsertVectors)
                .setNamespace(namespace)
                .build();
    }

    public static UpsertRequest buildOptionalUpsertRequest() {
        String namespace = RandomStringBuilder.build("ns", 8);
        List<String> hybridsIds = Arrays.asList("v4", "v5", "v6");
        List<Vector> hybridVectors = new ArrayList<>();
        List<Integer> sparseIndices = Arrays.asList(0, 1, 2);
        List<Float> sparseValues = Arrays.asList(0.11f, 0.22f, 0.33f);
        for (int i = 0; i < hybridsIds.size(); i++) {
            hybridVectors.add(
                    Vector.newBuilder()
                            .addAllValues(Floats.asList(upsertData[i]))
                            .setSparseValues(
                                    SparseValues.newBuilder().addAllIndices(sparseIndices).addAllValues(sparseValues).build()
                            )
                            .setId(hybridsIds.get(i))
                            .build());
        }

        return UpsertRequest.newBuilder()
                .addAllVectors(hybridVectors)
                .setNamespace(namespace)
                .build();
    }
}
