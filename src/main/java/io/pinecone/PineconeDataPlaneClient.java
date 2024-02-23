
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
            throw new PineconeValidationException("");
        }

        upsertRequest.addAllVectors(vectors);

        if (namespace != null) {
            upsertRequest.setNamespace(namespace);
        }

        return blockingStub.upsert(upsertRequest.build());
    }

    public QueryResponse query(QueryRequest queryRequest) {
        return blockingStub.query(queryRequest);
    }

    public FetchResponse fetch(Iterable<String> ids, String namespace) {
        FetchRequest.Builder fetchRequest = FetchRequest.newBuilder();

        if (ids == null) {
            throw new PineconeValidationException("Vector IDs to fetch must be present");
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
            throw new PineconeValidationException("Vector ID must be present");
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
                throw new PineconeValidationException("Sparse indices and values must be of the same length.");
            }
            updateRequest.setSparseValues(sparseValues);
        }

        return blockingStub.update(updateRequest.build());
    }

    public DeleteResponse delete(Iterable<String> ids, boolean deleteAll, String namespace, Struct filter) {
        DeleteRequest.Builder deleteRequestBuilder = DeleteRequest.newBuilder().setDeleteAll(deleteAll);

        // Confirm: add validation for empty ids?
        if (ids == null) {
            throw new PineconeValidationException("Vector IDs to delete must be present");
        }
        deleteRequestBuilder.addAllIds(ids);

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
