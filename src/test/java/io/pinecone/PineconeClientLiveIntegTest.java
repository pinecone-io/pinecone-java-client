package io.pinecone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Floats;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.pinecone.proto.*;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class PineconeClientLiveIntegTest {

    public static class Args {
        public String indexName = "integ-test-sanity";
        public String apiKey = "mock-api-key";
        public String environment = "us-wst1-gcp";
        public String projectName = "mock-proj-name";
    }

    private static final Logger logger = LoggerFactory.getLogger(PineconeClientLiveIntegTest.class);

    private Args args;

    private PineconeClient pineconeClient;

    @Before
    public void setUp() throws Exception {
        args = new ObjectMapper()
                .readValue(System.getenv("PINECONE_TEST_ARGS"), Args.class);

        PineconeClientConfig configuration = new PineconeClientConfig()
                .withApiKey(args.apiKey)
                .withEnvironment(args.environment)
                .withProjectName(args.projectName)
                .withServerSideTimeoutSec(10);



        pineconeClient = new PineconeClient(configuration);
    }

//     @Test
    public void sanity() {
        String ns = "temp_namespace";
        PineconeConnection conn = pineconeClient.connect(
                new PineconeConnectionConfig()
                        .withIndexName("java-test"));

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

        // query
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

        QueryRequest queryRequest = QueryRequest.newBuilder()
                .addQueries(queryVector)
                .setNamespace(ns)
                .setTopK(2)
                .setIncludeMetadata(true)
                .build();
//        Query by id example
//        QueryRequest queryRequest = QueryRequest.newBuilder()
//                .setId("v2")
//                .setNamespace("temp_namespace")
//                .setTopK(2)
//                .setIncludeMetadata(true)
//                .build();

        QueryResponse queryResponse = conn.getBlockingStub().query(queryRequest);
        assertThat(queryResponse, notNullValue());
        assertThat(queryResponse.getResultsList(), notNullValue());
        assertThat(queryResponse.getResultsCount(), equalTo(1));
//        When querying by id and single vector, we get matches instead of results. hence use this assert
//        When using them.
//        assertThat(queryResponse.getMatchesCount(), equalTo(1));
        logger.info("got query result ids: "
                + queryResponse.getResultsList().get(0).getMatchesList());
        assertThat(queryResponse.getResultsList().get(0).getMatchesList().size(), equalTo(2));




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
