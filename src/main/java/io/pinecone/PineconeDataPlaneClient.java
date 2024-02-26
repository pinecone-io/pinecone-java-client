
package io.pinecone;

import com.google.protobuf.Struct;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.proto.*;

import java.util.ArrayList;
import java.util.List;

public class PineconeDataPlaneClient {
    private VectorServiceGrpc.VectorServiceBlockingStub blockingStub;

    public PineconeDataPlaneClient(VectorServiceGrpc.VectorServiceBlockingStub blockingStub) {
        this.blockingStub = blockingStub;
    }

    public UpsertResponse upsert(
            String id,
            List<Float> values,
            Iterable<? extends Long> sparseIndices,
            Iterable<? extends Float> sparseValues,
            com.google.protobuf.Struct metadata,
            String namespace) {

        UpsertRequest.Builder upsertRequest = UpsertRequest.newBuilder();

        if (id.isEmpty() || values.isEmpty()) {
            throw new PineconeValidationException("Invalid Upsert Request. Please ensure that vector data is provided and not empty.");
        }

        Vector.Builder vectorBuilder = Vector.newBuilder()
                .setId(id)
                .addAllValues(values);

        // ToDo: add size validation
//        if(sparseIndices.size() != sparseValues.size()) {
//            throw new PineconeValidationException("Invalid Upsert Request. Please ensure that both sparse indices and values are of the same length.");
//        }

        if(sparseIndices != null && sparseValues != null) {
            vectorBuilder.setSparseValues(SparseValues.newBuilder()
                    .addAllIndices(convertUnsigned32IntToSigned32Int(sparseIndices))
                    .addAllValues(sparseValues)
                    .build());
        }

        if(metadata != null) {
            vectorBuilder.setMetadata(metadata);
        }

        upsertRequest.addVectors(vectorBuilder.build());

        if (namespace != null) {
            upsertRequest.setNamespace(namespace);
        }

        return blockingStub.upsert(upsertRequest.build());
    }

    public UpsertResponse batchUpsert(
            List<String> ids,
            List<Iterable<? extends Float>> valuesList,
            List<Iterable<? extends Long>> sparseIndicesList,
            List<Iterable<? extends Float>> sparseValuesList,
            List<com.google.protobuf.Struct> metadataList,
            String namespace) {

        UpsertRequest.Builder upsertRequest = UpsertRequest.newBuilder();

        if (ids.isEmpty() || valuesList.isEmpty()) {
            throw new PineconeValidationException("Invalid Upsert Request. Please ensure that vector IDs and values are provided and not empty.");
        }

        if(sparseIndicesList.size() != sparseValuesList.size()) {
            throw new PineconeValidationException("Invalid Upsert Request. Please ensure that both sparse indices and values are of the same length.");
        }

        // ToDo: add tests for null pointer exception
        for (int i = 0; i < ids.size(); i++) {
            Vector.Builder vectorBuilder = Vector.newBuilder()
                    .setId(ids.get(i))
                    .addAllValues(valuesList.get(i));

            if(sparseIndicesList.size() > i && sparseIndicesList.get(i) != null && sparseValuesList.size() > i && sparseValuesList.get(i) != null) {
                vectorBuilder.setSparseValues(SparseValues.newBuilder()
                        .addAllIndices(convertUnsigned32IntToSigned32Int(sparseIndicesList.get(i)))
                        .addAllValues(sparseValuesList.get(i))
                        .build());
            }

            if(metadataList.size() > i && metadataList.get(i) != null) {
                vectorBuilder.setMetadata(metadataList.get(i));
            }

            upsertRequest.addVectors(vectorBuilder.build());
        }

        if (namespace != null) {
            upsertRequest.setNamespace(namespace);
        }

        return blockingStub.upsert(upsertRequest.build());
    }

    public QueryResponse query(String id, String namespace, int topK, Struct filter, boolean includeValues, boolean includeMetadata,
                               List<Float> vectors, Iterable<? extends Long> sparseIndices, Iterable<? extends Float> sparseValues) {
        QueryRequest.Builder queryRequest = QueryRequest.newBuilder();

        if (id == null && vectors.isEmpty()) {
            throw new PineconeValidationException("Invalid Query Request. Please include only one of the following parameters: id or vector");
        }
        queryRequest.setId(id);

        if (namespace != null) {
            queryRequest.setNamespace(namespace);
        }

        queryRequest.setTopK(topK)
                .setIncludeValues(includeValues)
                .setIncludeMetadata(includeMetadata);

        if (filter != null) {
            queryRequest.setFilter(filter);
        }

        if (!vectors.isEmpty()) {
            queryRequest.addAllVector(vectors);
        }

        // ToDo: add size validation
//        if(sparseIndices.size() != sparseValues.size()) {
//            throw new PineconeValidationException("Invalid Upsert Request. Please ensure that both sparse indices and values are of the same length.");
//        }
        if(sparseIndices != null && sparseValues != null) {
            queryRequest.setSparseVector(SparseValues.newBuilder()
                    .addAllIndices(convertUnsigned32IntToSigned32Int(sparseIndices))
                    .addAllValues(sparseValues)
                    .build());
        }

        return blockingStub.query(queryRequest.build());
    }

    public FetchResponse fetch(Iterable<String> ids, String namespace) {
        FetchRequest.Builder fetchRequest = FetchRequest.newBuilder();

        if (ids == null) {
            throw new PineconeValidationException("Invalid Fetch Request. Vector IDs must be present");
        }

        fetchRequest.addAllIds(ids);

        if (namespace != null) {
            fetchRequest.setNamespace(namespace);
        }

        return blockingStub.fetch(fetchRequest.build());
    }

    public UpdateResponse update(String id,
                                 Iterable<? extends Float> values,
                                 Struct metadata,
                                 String namespace,
                                 Iterable<? extends Long> sparseIndices,
                                 Iterable<? extends Float> sparseValues) {
        UpdateRequest.Builder updateRequest = UpdateRequest.newBuilder();

        if (id == null) {
            throw new PineconeValidationException("Invalid Update Request. Vector ID must be present");
        }
        updateRequest.setId(id);

        if (values != null) {
            updateRequest.addAllValues(values);
        }

        if (metadata != null) {
            updateRequest.setSetMetadata(metadata);
        }

        if (namespace != null) {
            updateRequest.setNamespace(namespace);
        }

        // ToDo: add size validation
//        if(sparseIndices.size() != sparseValues.size()) {
//            throw new PineconeValidationException("Invalid Upsert Request. Please ensure that both sparse indices and values are of the same length.");
//        }

        if(sparseIndices != null && sparseValues != null) {
            updateRequest.setSparseValues(SparseValues.newBuilder()
                    .addAllIndices(convertUnsigned32IntToSigned32Int(sparseIndices))
                    .addAllValues(sparseValues)
                    .build());
        }

        return blockingStub.update(updateRequest.build());
    }

    public DeleteResponse delete(Iterable<String> ids, boolean deleteAll, String namespace, Struct filter) {
        DeleteRequest.Builder deleteRequestBuilder = DeleteRequest.newBuilder().setDeleteAll(deleteAll);

        if (ids != null) {
            deleteRequestBuilder.addAllIds(ids);
        }

        if (namespace != null) {
            deleteRequestBuilder.setNamespace(namespace);
        }

        if (filter != null) {
            deleteRequestBuilder.setFilter(filter);
        }
        return blockingStub.delete(deleteRequestBuilder.build());
    }

    public DescribeIndexStatsResponse describeIndexStats(Struct filter) {
        DescribeIndexStatsRequest.Builder describeIndexStatsRequest = DescribeIndexStatsRequest.newBuilder();

        if (filter != null) {
            describeIndexStatsRequest.setFilter(filter);
        }

        return blockingStub.describeIndexStats(describeIndexStatsRequest.build());
    }

    // ToDo: write tests
    private Iterable<? extends Integer> convertUnsigned32IntToSigned32Int(Iterable<? extends Long> unsigned32IntValues) {
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
