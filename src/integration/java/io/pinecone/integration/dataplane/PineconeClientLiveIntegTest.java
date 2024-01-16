package io.pinecone.integration.dataplane;

import com.google.common.primitives.Floats;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.pinecone.PineconeConnection;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.pinecone.helpers.IndexManager.createIndexIfNotExistsDataPlane;
import static io.pinecone.helpers.AssertRetry.assertWithRetry;

public class PineconeClientLiveIntegTest {

    private static final Logger logger = LoggerFactory.getLogger(PineconeClientLiveIntegTest.class);
    private static VectorServiceGrpc.VectorServiceBlockingStub blockingStub;

    @BeforeAll
    public static void defineConfig() throws IOException, InterruptedException {
        PineconeConnection connection = createIndexIfNotExistsDataPlane(3);
        blockingStub = connection.getBlockingStub();
    }

    @Test
    public void sanity() throws InterruptedException {
        String namespace = RandomStringBuilder.build("ns", 8);

        // upsert
        float[][] upsertData = {{1.0F, 2.0F, 3.0F}, {4.0F, 5.0F, 6.0F}, {7.0F, 8.0F, 9.0F}};
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

        UpsertRequest request = UpsertRequest.newBuilder()
                .addAllVectors(upsertVectors)
                .setNamespace(namespace)
                .build();

        UpsertResponse upsertResponse = blockingStub.upsert(request);
        logger.info("Put " + upsertResponse.getUpsertedCount() + " vectors into the index");
        assert (upsertResponse.getUpsertedCount() == 3);

        // hybrid upsert
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

        UpsertRequest hybridRequest = UpsertRequest.newBuilder()
                .addAllVectors(hybridVectors)
                .setNamespace(namespace)
                .build();
        UpsertResponse hybridResponse = blockingStub.upsert(hybridRequest);
        logger.info("Put " + hybridResponse.getUpsertedCount() + " vectors into the index");
        assert (hybridResponse.getUpsertedCount() == 3);

        // fetch
        List<String> ids = Arrays.asList("v1", "v2");
        FetchRequest fetchRequest1 = FetchRequest.newBuilder().addAllIds(ids).setNamespace(namespace).build();
        assertWithRetry(() -> {
            FetchResponse fetchResponse = blockingStub.fetch(fetchRequest1);
            assert (fetchResponse.containsVectors("v1"));
        });

        // Updates vector v1's values to 10.0, 11.0, and 12.0 from 1.0, 2.0, and 3.0
        List<Float> updateValueList = Floats.asList(10F, 11F, 12F);
        UpdateRequest updateRequest = UpdateRequest.newBuilder()
                .setId("v1")
                .setNamespace(namespace)
                .addAllValues(updateValueList)
                .build();
        blockingStub.update(updateRequest);

        // DEPRECATED: queries parameter of QueryRequest has been deprecated
        // Use vector parameter and the associated methods.
        // Below commented example shows addQueries() which is deprecated
/*
        float[] rawVector = {1.0F, 2.0F, 3.0F};
        QueryVector queryVector = QueryVector.newBuilder()
                .addAllValues(Floats.asList(rawVector))
                .setFilter(Struct.newBuilder()
                        .putFields("some_field", Value.newBuilder()
                                .setStructValue(Struct.newBuilder()
                                        .putFields("$lt", Value.newBuilder()
                                                .setNumberValue(3)
                                                .build()))
                                .build())
                        .build())
                .setNamespace(namespace)
                .build();

        QueryRequest batchQueryRequest = QueryRequest.newBuilder()
                .addQueries(queryVector)            // Deprecated
                .setNamespace(namespace)
                .setTopK(2)
                .setIncludeMetadata(true)
                .build();
*/

        // Below example shows usage of addAllVector() which is associated with vector parameter of QueryRequest.
        Iterable<Float> iterableVector = Arrays.asList(1.0F, 2.0F, 3.0F);
        QueryRequest queryRequest = QueryRequest.newBuilder()
                .addAllVector(iterableVector)
                .setNamespace(namespace)
                .setTopK(2)
                .setIncludeMetadata(true)
                .build();

        // When querying using a single vector, we get matches instead of results
        assertWithRetry(() -> {
            QueryResponse queryResponse = blockingStub.query(queryRequest);
            Assertions.assertEquals(queryResponse.getMatchesCount(), 2);
        });

        // Query by id example
        QueryRequest queryByIdRequest = QueryRequest.newBuilder()
                .setId("v1")
                .setNamespace(namespace)
                .setTopK(1)
                .setIncludeMetadata(true)
                .setIncludeValues(true)
                .build();

        assertWithRetry(() -> {
            QueryResponse queryResponse = blockingStub.query(queryByIdRequest);
            Assertions.assertEquals(queryResponse.getMatchesCount(),1);
            assert (queryResponse.getMatches(0).getValuesList().containsAll(updateValueList));
        });

        // Delete
        String[] idsToDelete = {"v2"};
        DeleteRequest deleteRequest = DeleteRequest.newBuilder()
                .setNamespace(namespace)
                .addAllIds(Arrays.asList(idsToDelete))
                .setDeleteAll(false)
                .build();

        blockingStub.delete(deleteRequest);
        FetchRequest fetchRequest2 = FetchRequest.newBuilder().addAllIds(ids).setNamespace(namespace).build();
        assertWithRetry(() -> {
            FetchResponse fetchResponse = blockingStub.fetch(fetchRequest2);
            Assertions.assertEquals(fetchResponse.getVectorsCount(), ids.size() - 1);
        });

        // Clear out the test
        DeleteRequest deleteAllRequest = DeleteRequest.newBuilder()
                .setNamespace(namespace)
                .setDeleteAll(true)
                .build();

        blockingStub.delete(deleteAllRequest);
        FetchRequest fetchRequest3 = FetchRequest.newBuilder().addAllIds(ids).setNamespace(namespace).build();
        assertWithRetry(() -> {
            FetchResponse fetchResponse = blockingStub.fetch(fetchRequest3);
            Assertions.assertEquals(fetchResponse.getVectorsCount(), 0);
        });
    }
}
