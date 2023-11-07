package io.pinecone.integration.DataPlane;

import com.google.common.primitives.Floats;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.pinecone.*;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.model.IndexMeta;
import io.pinecone.proto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AsyncStubTest {

    private static final Logger logger = LoggerFactory.getLogger(AsyncStubTest.class);
    public String indexName = "integ-test-sanity";

    private PineconeClient dataPlaneClient;
    private PineconeIndexOperationClient controlPlaneClient;

    @BeforeEach
    public void setUp() {
        PineconeClientConfig config = new PineconeClientConfig()
                .withApiKey(System.getenv("PINECONE_API_KEY"))
                .withEnvironment(System.getenv("PINECONE_ENVIRONMENT"))
                .withServerSideTimeoutSec(10);

        controlPlaneClient = new PineconeIndexOperationClient(config);
        dataPlaneClient = new PineconeClient(config);
    }

    @Test
    public void checkIndexSetup() throws Exception {
        IndexMeta indexMeta = controlPlaneClient.describeIndex(indexName);
        assertEquals(3, indexMeta.getDatabase().getDimension());
    }

    @Test
    public void sanity() throws Exception {
        IndexMeta indexMeta = controlPlaneClient.describeIndex(indexName);
        String host = indexMeta.getStatus().getHost();
        PineconeConnection connection = dataPlaneClient.connect(
                new PineconeConnectionConfig()
                        .withConnectionUrl("https://" + host));

        String namespace = RandomStringBuilder.build("ns", 8);

        VectorServiceGrpc.VectorServiceFutureStub futureStub = connection.getFutureStub();
        DescribeIndexStatsRequest describeIndexRequest = DescribeIndexStatsRequest.newBuilder().build();
        ListenableFuture<DescribeIndexStatsResponse> futureDescribeIndexResponse = futureStub.describeIndexStats(describeIndexRequest);

        assertEquals(futureDescribeIndexResponse.get().getDimension(), 3);

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

        ListenableFuture<UpsertResponse> upsertFuture = futureStub.upsert(request);

        UpsertResponse upsertResponse = upsertFuture.get();
        logger.info("Put " + upsertResponse.getUpsertedCount() + " vectors into the index");
        assertEquals(upsertResponse.getUpsertedCount(), 3);

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
        ListenableFuture<UpsertResponse> hybridUpsertFuture = connection.getFutureStub().upsert(hybridRequest);

        UpsertResponse hybridResponse = hybridUpsertFuture.get();
        logger.info("Put " + hybridResponse.getUpsertedCount() + " vectors into the index");
        assertEquals(hybridResponse.getUpsertedCount(), 3);

        // fetch
        List<String> ids = Arrays.asList("v1", "v2");
        FetchRequest fetchRequest = FetchRequest.newBuilder().addAllIds(ids).setNamespace(namespace).build();
        ListenableFuture<FetchResponse> fetchFuture = connection.getFutureStub().fetch(fetchRequest);
        assert (fetchFuture.get().containsVectors("v1"));
        assert (fetchFuture.get().containsVectors("v2"));


        // Updates vector v1's values to 10.0, 11.0, and 12.0 from 1.0, 2.0, and 3.0
        UpdateRequest updateRequest = UpdateRequest.newBuilder()
                .setId("v1")
                .setNamespace(namespace)
                .addAllValues(Floats.asList(10F, 11F, 12F))
                .build();
        connection.getFutureStub().update(updateRequest);
        fetchRequest = FetchRequest.newBuilder().addIds("v1").setNamespace(namespace).build();
        connection.getFutureStub().fetch(fetchRequest);

        // DEPRECATED: batch queries
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
                .addQueries(queryVector)
                .setNamespace(namespace)
                .setTopK(2)
                .setIncludeMetadata(true)
                .build();

        ListenableFuture<QueryResponse> queryFuture = connection.getFutureStub().query(batchQueryRequest);
        QueryResponse queryResponse = queryFuture.get();
        assertNotNull(queryResponse);
        assertNotNull(queryResponse.getResultsList());
        assertEquals(queryResponse.getResultsCount(), 1);

        Iterable<Float> iterable = Arrays.asList(1.0F, 2.0F, 3.0F);
        QueryRequest queryRequest = QueryRequest.newBuilder()
                .addAllVector(iterable)
                .setNamespace(namespace)
                .setTopK(2)
                .setIncludeMetadata(true)
                .build();

        // When querying using a single vector, we get matches instead of results
        queryResponse = connection.getFutureStub().query(queryRequest).get();
        assertNotNull(queryResponse);
        assertNotNull(queryResponse.getMatchesList());
        assertEquals(queryResponse.getMatchesCount(), 2);

        // Query by id example
        QueryRequest queryByIdRequest = QueryRequest.newBuilder()
                .setId("v2")
                .setNamespace(namespace)
                .setTopK(2)
                .setIncludeMetadata(true)
                .build();

        queryResponse = connection.getFutureStub().query(queryByIdRequest).get();
        assertNotNull(queryResponse);
        assertNotNull(queryResponse.getMatchesList());
        assertEquals(queryResponse.getMatchesCount(), 2);

        // Delete
        String[] idsToDelete = {"v2"};
        DeleteRequest deleteRequest = DeleteRequest.newBuilder()
                .setNamespace(namespace)
                .addAllIds(Arrays.asList(idsToDelete))
                .setDeleteAll(false)
                .build();

        DeleteResponse deleteResponse = connection.getFutureStub().delete(deleteRequest).get();
        fetchRequest = FetchRequest.newBuilder().addAllIds(ids).setNamespace(namespace).build();
        FetchResponse fetchResponse = connection.getFutureStub().fetch(fetchRequest).get();
        assertEquals(fetchResponse.getVectorsCount(), ids.size() - 1);

        // Clear out the test
        DeleteRequest deleteAllRequest = DeleteRequest.newBuilder()
                .setNamespace(namespace)
                .setDeleteAll(true)
                .build();


        connection.getFutureStub().delete(deleteAllRequest).get();

        fetchRequest = FetchRequest.newBuilder().addAllIds(ids).setNamespace(namespace).build();
        fetchResponse = connection.getFutureStub().fetch(fetchRequest).get();
        assertEquals(fetchResponse.getVectorsCount(), 0);
    }
}