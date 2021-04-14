package io.pinecone;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        public String target;
        public boolean secure = true;
        public String serviceName = "integ-test-sanity";
        public String apiKey = "mock-api-key";
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
                .withServerSideTimeoutSec(10);

        pineconeClient = new PineconeClient(configuration);
    }

//    @Test
    public void sanity() {
        String ns = "namespace";
        PineconeConnection conn = pineconeClient.connect(
                new PineconeConnectionConfig()
                        .withServiceAuthority(args.target)
                        .withSecure(args.secure)
                        .withServiceName(args.serviceName));

        // upsert
        float[][] upsertData = {{1.0F, 2.0F, 3.0F}, {4.0F, 5.0F, 6.0F}, {7.0F, 8.0F, 9.0F}};
        List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
        UpsertResponse upsertResponse = conn.send(
                pineconeClient.upsertRequest()
                        .data(upsertData)
                        .ids(upsertIds)
                        .namespace(ns));
        assertThat(upsertResponse.getIds(), equalTo(upsertIds));

        // fetch
        // query
        float[][] queries = {{1.0F, 2.0F, 3.0F}};
        QueryResponse queryResponse = conn.send(
                pineconeClient.queryRequest()
                        .topK(2)
                        .data(queries)
                        .namespace(ns));
        assertThat(queryResponse, notNullValue());
        assertThat(queryResponse.getQueryResults(), notNullValue());
        assertThat(queryResponse.getQueryResults().size(), equalTo(1));
        logger.info("got query result ids: "
                + queryResponse.getQueryResults().get(0).getIds());
        assertThat(queryResponse.getQueryResults().get(0).getIds().size(), equalTo(2));
        assertThat(queryResponse.getQueryResults().get(0).getScores().size(), equalTo(2));

        // delete
        // info
    }
}
