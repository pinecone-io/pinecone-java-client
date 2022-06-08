
Copyright (c) 2020-2021 Pinecone Systems Inc. All right reserved.


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

    /* These tests assume a service has been set up as follows with the python client:
     *     pinecone.service.deploy(service_name='integ-test-sanity', graph=pinecone.graph.IndexGraph())
     *
     * Then target comes from the domain/ip and port returned by:
     *     pinecone.service.describe(service_name='integ-test-sanity')
     */

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

    // @Test
    public void sanity() {
        String ns = "temp_namespace";
        PineconeConnection conn = pineconeClient.connect(
                new PineconeConnectionConfig()
                        .withIndexName(args.indexName));

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

        // fetch
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

        QueryResponse queryResponse = conn.getBlockingStub().query(queryRequest);
        assertThat(queryResponse, notNullValue());
        assertThat(queryResponse.getResultsList(), notNullValue());
        assertThat(queryResponse.getResultsCount(), equalTo(1));
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
