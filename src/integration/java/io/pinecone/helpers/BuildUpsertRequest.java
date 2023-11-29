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
        return buildRequiredUpsertRequest(new ArrayList<>(), "");
    }

    public static UpsertRequest buildRequiredUpsertRequest(String namespace) {
        return buildRequiredUpsertRequest(new ArrayList<>(), namespace);
    }

    public static UpsertRequest buildRequiredUpsertRequest(List<String> upsertIds, String namespace) {
        if (upsertIds.isEmpty()) upsertIds = Arrays.asList("v1", "v2", "v3");
        if (namespace.isEmpty()) namespace = RandomStringBuilder.build("ns", 8);

        List<Vector> upsertVectors = new ArrayList<>();
        for (int i = 0; i < upsertData.length; i++) {
            upsertVectors.add(Vector.newBuilder()
                    .addAllValues(Floats.asList(upsertData[i]))
                    .setId(upsertIds.get(i % upsertData.length))
                    .build());
        }

        return UpsertRequest.newBuilder()
                .addAllVectors(upsertVectors)
                .setNamespace(namespace)
                .build();
    }

    public static UpsertRequest buildOptionalUpsertRequest() {
        return buildOptionalUpsertRequest(new ArrayList<>(), "");
    }

    public static UpsertRequest buildOptionalUpsertRequest(String namespace) {
        return buildOptionalUpsertRequest(new ArrayList<>(), namespace);
    }

    public static UpsertRequest buildOptionalUpsertRequest(List<String> upsertIds, String namespace) {
        if (upsertIds.isEmpty()) upsertIds = Arrays.asList("v4", "v5", "v6");
        if (namespace.isEmpty()) namespace = RandomStringBuilder.build("ns", 8);

        List<Vector> hybridVectors = new ArrayList<>();
        List<Integer> sparseIndices = Arrays.asList(0, 1, 2);
        List<Float> sparseValues = Arrays.asList(0.11f, 0.22f, 0.33f);
        String[] genreArrays = {"thriller", "drama", "fiction"};
        String[] yearArrays = {"2018", "2019", "2020"};
        for (int i = 0; i < upsertIds.size(); i++) {
            hybridVectors.add(
                    Vector.newBuilder()
                            .addAllValues(Floats.asList(upsertData[i]))
                            .setMetadata(Struct.newBuilder()
                                    .putFields("genre", Value.newBuilder().setStringValue(genreArrays[i % genreArrays.length]).build())
                                    .putFields("year", Value.newBuilder().setStringValue(yearArrays[i % yearArrays.length]).build())
                                    .build())
                            .setSparseValues(
                                    SparseValues
                                            .newBuilder()
                                            .addAllIndices(sparseIndices)
                                            .addAllValues(sparseValues)
                                            .build()
                            )
                            .setId(upsertIds.get(i))
                            .build());
        }

        return UpsertRequest.newBuilder()
                .addAllVectors(hybridVectors)
                .setNamespace(namespace)
                .build();
    }
}
