package io.pinecone.helpers;

import com.google.common.primitives.Floats;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.pinecone.proto.SparseValues;
import io.pinecone.proto.UpsertRequest;
import io.pinecone.proto.Vector;

import java.util.*;

public class BuildUpsertRequest {
    // ToDo: Remove all buildUpsertRequest methods after future stub test changes are in and rename this class
    public static final float[][] upsertValues = {{1.0F, 2.0F, 3.0F}, {4.0F, 5.0F, 6.0F}, {7.0F, 8.0F, 9.0F}};
    public static final String[] metadataFields = new String[]{"genre", "year"};
    public static final List<Integer> sparseIndices = Arrays.asList(0, 1, 2);
    public static final List<Float> sparseValues = Arrays.asList(0.11f, 0.22f, 0.33f);

    public static List<String> getIdsList(int numOfVectors) {
        List<String> idList = new ArrayList<>(numOfVectors);

        for(int i=0; i<numOfVectors; i++) {
            idList.add(RandomStringBuilder.build("v", 2));
        }

        return idList;
    }

    public static List<List<Float>> getValuesList(int valuesListSize, int dimension) {
        List<List<Float>> valuesList = new ArrayList<>(dimension);
        Random random = new Random();

        for(int i = 0; i < valuesListSize; i++) {
            List<Float> row = new ArrayList<>(dimension);
            for (int j = 0; j < dimension; j++) {
                row.add(random.nextFloat());
            }
            valuesList.add(row);
        }

        return valuesList;
    }

    public static List<List<Long>> getSparseIndicesList(int indicesListSize, int dimension) {
        List<List<Long>> valuesList = new ArrayList<>(dimension);
        Random random = new Random();
        long maxUnsignedInt = (1L << 32) - 1;

        for(int i = 0; i < indicesListSize; i++) {
            List<Long> row = new ArrayList<>(dimension);
            for (int j = 0; j < dimension; j++) {
                row.add(random.nextLong() & maxUnsignedInt);
            }
            valuesList.add(row);
        }

        return valuesList;
    }

    public static List<Struct> getMetadataStruct(int metadataSize) {
        HashMap<String, List<String>> metadataMap = createAndGetMetadataMap();

        List<Struct> structList = new ArrayList<>(metadataSize);

        for(int i=0; i<metadataSize; i++) {
            structList.add(Struct.newBuilder()
                    .putFields(metadataFields[0],
                            Value.newBuilder().setStringValue(metadataMap.get(metadataFields[0]).get(i % metadataSize)).build())
                    .putFields(metadataFields[1],
                            Value.newBuilder().setStringValue(metadataMap.get(metadataFields[1]).get(i % metadataSize)).build())
                    .build());
        }

        return structList;
    }

    public static UpsertRequest buildRequiredUpsertRequest() {
        return buildRequiredUpsertRequest(new ArrayList<>(), "");
    }

    public static UpsertRequest buildRequiredUpsertRequest(String namespace) {
        return buildRequiredUpsertRequest(new ArrayList<>(), namespace);
    }

    public static UpsertRequest buildRequiredUpsertRequest(List<String> upsertIds, String namespace) {
        // Namespace is not mandatory but added for each test, so they are independent
        if (upsertIds.isEmpty()) upsertIds = Arrays.asList("v1", "v2", "v3");
        if (namespace.isEmpty()) namespace = RandomStringBuilder.build("ns", 8);

        List<Vector> upsertVectors = new ArrayList<>();
        for (int i = 0; i < upsertValues.length; i++) {
            upsertVectors.add(Vector.newBuilder()
                    .addAllValues(Floats.asList(upsertValues[i]))
                    .setId(upsertIds.get(i % upsertValues.length))
                    .build());
        }

        return UpsertRequest.newBuilder()
                .addAllVectors(upsertVectors)
                .setNamespace(namespace)
                .build();
    }

    public static UpsertRequest buildRequiredUpsertRequestByDimension(List<String> upsertIds, int dimension, String namespace) {
        if (upsertIds.isEmpty()) upsertIds = Arrays.asList("v1", "v2", "v3");

        List<Vector> upsertVectors = new ArrayList<>();
        for (String upsertId : upsertIds) {
            upsertVectors.add(Vector.newBuilder()
                    .addAllValues(generateVectorValuesByDimension(dimension))
                    .setId(upsertId)
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
        return buildOptionalUpsertRequest(upsertIds, namespace, new HashMap<>());
    }

    public static UpsertRequest buildOptionalUpsertRequest(List<String> upsertIds, String namespace, HashMap<String, List<String>> metadataMap) {
        if(upsertIds.isEmpty()) upsertIds = Arrays.asList("v4", "v5", "v6");
        if(namespace.isEmpty()) namespace = RandomStringBuilder.build("ns", 8);
        if(metadataMap.isEmpty()) metadataMap = createAndGetMetadataMap();

        List<Vector> hybridVectors = new ArrayList<>();

        for (int i = 0; i < upsertIds.size(); i++) {
            String field1 = metadataFields[i % metadataFields.length];
            String field2 = metadataFields[(i+1) % metadataFields.length];
            int metadataValuesLength = metadataMap.get(field1).size();

            hybridVectors.add(
                    Vector.newBuilder()
                            .addAllValues(Floats.asList(upsertValues[i]))
                            .setMetadata(Struct.newBuilder()
                                    .putFields(field1, Value.newBuilder().setStringValue(metadataMap.get(field1).get(i % metadataValuesLength)).build())
                                    .putFields(field2, Value.newBuilder().setStringValue(metadataMap.get(field2).get(i % metadataValuesLength)).build())
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

    public static HashMap<String, List<String>> createAndGetMetadataMap() {
        HashMap<String, List<String>> metadataMap;
        metadataMap = new HashMap<>();
        List<String> metadataValues1 = new ArrayList<>();
        metadataValues1.add("drama");
        metadataValues1.add("thriller");
        metadataValues1.add("fiction");
        metadataMap.put(metadataFields[0], metadataValues1);
        List<String> metadataValues2 = new ArrayList<>();
        metadataValues2.add("2019");
        metadataValues2.add("2020");
        metadataValues2.add("2021");
        metadataMap.put(metadataFields[1], metadataValues2);

        return metadataMap;
    }

    public static ArrayList<Float> generateVectorValuesByDimension(int dimension) {
        ArrayList<Float> values = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < dimension; i++) {
            values.add(random.nextFloat());
        }

        return values;
    }
}
