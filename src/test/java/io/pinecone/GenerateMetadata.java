package io.pinecone;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GenerateMetadata {

    public static final String[] metadataFields = new String[]{"genre", "year"};

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

    public static Struct generateMetadataStruct(int metadataField0, int metadataField1) {
        HashMap<String, List<String>> metadataMap = createAndGetMetadataMap();
        int metadataSize = metadataMap.get(metadataFields[0]).size();
        return Struct.newBuilder()
                .putFields(metadataFields[0],
                        Value.newBuilder().setStringValue(metadataMap.get(metadataFields[0]).get(metadataField0 % metadataSize)).build())
                .putFields(metadataFields[1],
                        Value.newBuilder().setStringValue(metadataMap.get(metadataFields[1]).get(metadataField1 % metadataSize)).build())
                .build();
    }
}
