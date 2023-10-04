package io.pinecone;

import com.google.common.primitives.Floats;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PineconeClientLiveIntegTest {

    public String indexName = "integ-test-sanity";

    private static final Logger logger = LoggerFactory.getLogger(PineconeClientLiveIntegTest.class);

    private PineconeClient dataPlaneClient;
    private PineconeIndexOperationClient controlPlaneClient;

    @BeforeEach
    public void setUp() throws Exception {
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
        PineconeConnection conn = dataPlaneClient.connect(
                new PineconeConnectionConfig()
                        .withConnectionUrl("https://" + host));

        String ns = RandomStringBuilder.build("ns", 8);

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
                .setNamespace(ns)
                .build();

        UpsertResponse upsertResponse = conn.getBlockingStub().upsert(request);
        logger.info("Put " + upsertResponse.getUpsertedCount() + " vectors into the index");
        assert (upsertResponse.getUpsertedCount() == 3);

        // hybrid upsert

        List<String> hybridsIds = Arrays.asList("v4","v5","v6");
        List<Vector> hybridVectors = new ArrayList<>();
        List<Integer> sparseIndices = Arrays.asList(0,1,2);
        List<Float> sparseValues = Arrays.asList(0.11f,0.22f,0.33f);
        for (int i=0; i< hybridsIds.size(); i++) {
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
                .setNamespace(ns)
                .build();
        UpsertResponse hybridResponse = conn.getBlockingStub().upsert(hybridRequest);
        logger.info("Put " + hybridResponse.getUpsertedCount() + " vectors into the index");
        assert (hybridResponse.getUpsertedCount() == 3);

        // fetch
        List<String> ids = Arrays.asList("v1","v2");
        FetchRequest fetchRequest = FetchRequest.newBuilder().addAllIds(ids).setNamespace(ns).build();
        FetchResponse fetchResponse = conn.getBlockingStub().fetch(fetchRequest);
        assert(fetchResponse.containsVectors("v1"));

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
                .setNamespace(ns)
                .build();

        QueryRequest batchQueryRequest = QueryRequest.newBuilder()
                .addQueries(queryVector)
                .setNamespace(ns)
                .setTopK(2)
                .setIncludeMetadata(true)
                .build();

        QueryResponse queryResponse = conn.getBlockingStub().query(batchQueryRequest);
        assertThat(queryResponse, notNullValue());
        assertThat(queryResponse.getResultsList(), notNullValue());
        assertThat(queryResponse.getResultsCount(), equalTo(1));

        Iterable<Float> iterable = Arrays.asList(1.0F, 2.0F, 3.0F);
        QueryRequest queryRequest = QueryRequest.newBuilder()
                .addAllVector(iterable)
                .setNamespace(ns)
                .setTopK(2)
                .setIncludeMetadata(true)
                .build();

        // When querying using a single vector, we get matches instead of results
        queryResponse = conn.getBlockingStub().query(queryRequest);
        assertThat(queryResponse, notNullValue());
        assertThat(queryResponse.getMatchesList(), notNullValue());
        assertThat(queryResponse.getMatchesCount(), equalTo(2));

        // Query by id example
        QueryRequest queryByIdRequest = QueryRequest.newBuilder()
                .setId("v2")
                .setNamespace(ns)
                .setTopK(2)
                .setIncludeMetadata(true)
                .build();

        queryResponse = conn.getBlockingStub().query(queryByIdRequest);
        assertThat(queryResponse, notNullValue());
        assertThat(queryResponse.getMatchesList(), notNullValue());
        assertThat(queryResponse.getMatchesCount(), equalTo(2));

        // Delete
        String[] idsToDelete = {"v2"};
        DeleteRequest deleteRequest = DeleteRequest.newBuilder()
                .setNamespace(ns)
                .addAllIds(Arrays.asList(idsToDelete))
                .setDeleteAll(false)
                .build();

        conn.getBlockingStub().delete(deleteRequest);

        // Clear out the test
        DeleteRequest deleteAllRequest = DeleteRequest.newBuilder()
                .setNamespace(ns)
                .setDeleteAll(true)
                .build();

        conn.getBlockingStub().delete(deleteAllRequest);
    }
}
