
package io.pinecone;

import com.google.protobuf.Struct;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.proto.*;

import java.util.List;

public class PineconeDataPlaneClient {
    private VectorServiceGrpc.VectorServiceBlockingStub blockingStub;

    public PineconeDataPlaneClient(VectorServiceGrpc.VectorServiceBlockingStub blockingStub) {
        this.blockingStub = blockingStub;
    }

    public UpsertResponse upsert(List<Vector> vectors, String namespace) {
        UpsertRequest.Builder upsertRequest = UpsertRequest.newBuilder();

        if (vectors.isEmpty()) {
            throw new PineconeValidationException("Invalid Update Request. Please ensure that vectors are provided and not empty.");
        }

        upsertRequest.addAllVectors(vectors);

        if (namespace != null) {
            upsertRequest.setNamespace(namespace);
        }

        return blockingStub.upsert(upsertRequest.build());
    }

    public QueryResponse query(String namespace, int topK, Struct filter, boolean includeValues, boolean includeMetadata,
                               List<Float> vectors, SparseValues sparseVector, String id) {
        QueryRequest.Builder queryRequest = QueryRequest.newBuilder()
                .setTopK(topK)
                .setIncludeValues(includeValues)
                .setIncludeMetadata(includeMetadata);

        if (id != null && !vectors.isEmpty()) {
            throw new PineconeValidationException("Invalid Query Request. Please include only one of the following parameters: id or vector");
        }

        if (namespace != null) {
            queryRequest.setNamespace(namespace);
        }

        if (filter != null) {
            queryRequest.setFilter(filter);
        }

        if (!vectors.isEmpty()) {
            queryRequest.addAllVector(vectors);
        }

        if (sparseVector != null) {
            queryRequest.setSparseVector(sparseVector);
        }

        if (id != null) {
            queryRequest.setId(id);
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

    public UpdateResponse update(String id, Iterable<? extends Float> values, Struct setMetadata, String namespace, SparseValues sparseValues) {
        UpdateRequest.Builder updateRequest = UpdateRequest.newBuilder();

        if (id == null) {
            throw new PineconeValidationException("Invalid Update Request. Vector ID must be present");
        }
        updateRequest.setId(id);

        if (values != null) {
            updateRequest.addAllValues(values);
        }

        if (setMetadata != null) {
            updateRequest.setSetMetadata(setMetadata);
        }

        if (namespace != null) {
            updateRequest.setNamespace(namespace);
        }

        // ToDo: Update this to longToInt()
        if (sparseValues != null) {
            if (sparseValues.getValuesCount() != sparseValues.getIndicesCount()) {
                throw new PineconeValidationException("Invalid Update Request. Sparse indices and values must be of the same length.");
            }
            updateRequest.setSparseValues(sparseValues);
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
}
