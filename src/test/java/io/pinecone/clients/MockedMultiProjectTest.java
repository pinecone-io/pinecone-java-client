package io.pinecone.clients;

import io.grpc.stub.StreamObserver;
import io.pinecone.configs.ResponseMetadata;
import io.pinecone.helpers.MockPinecone;
import io.pinecone.proto.UpsertRequest;
import io.pinecone.proto.UpsertResponse;
import io.pinecone.proto.VectorServiceGrpc;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Two clients with different API keys, each in a project holding an index with the same name, must each reach their
 * own project's index host with their own key and settings.
 */
public class MockedMultiProjectTest {
    @RegisterExtension
    final MockPinecone projectA = new MockPinecone();

    @RegisterExtension
    final MockPinecone projectB = new MockPinecone();

    private static final String INDEX_NAME = "product-catalog";

    private static final VectorServiceGrpc.VectorServiceImplBase UPSERT_SERVICE = new VectorServiceGrpc.VectorServiceImplBase() {
        @Override
        public void upsert(UpsertRequest request, StreamObserver<UpsertResponse> response) {
            response.onNext(UpsertResponse.newBuilder().setUpsertedCount(request.getVectorsCount()).build());
            response.onCompleted();
        }
    };

    private static Pinecone.Builder clientFor(MockPinecone project, String apiKey) {
        project.vectorService(UPSERT_SERVICE);
        project.stubDescribeIndex(INDEX_NAME, 3);
        return new Pinecone.Builder(apiKey).withHost(project.restUrl()).withTlsEnabled(false);
    }

    private static void assertOneUpsertWithKey(MockPinecone project, String apiKey) {
        List<MockPinecone.GrpcCall> calls = project.grpcCalls();
        assertEquals(1, calls.size());
        assertEquals("VectorService/Upsert", calls.get(0).method);
        assertEquals(apiKey, calls.get(0).header("api-key"));
    }

    @Test
    public void sameIndexNameInTwoProjectsUsesEachClientsHostKeyAndListener() {
        List<ResponseMetadata> reportedToB = Collections.synchronizedList(new ArrayList<>());
        Pinecone clientA = clientFor(projectA, "project-a-key").build();
        Pinecone clientB = clientFor(projectB, "project-b-key").withResponseMetadataListener(reportedToB::add).build();

        Index indexA = clientA.getIndexConnection(INDEX_NAME);
        Index indexB = clientB.getIndexConnection(INDEX_NAME);
        try {
            indexB.upsert("v1", Arrays.asList(1f, 2f, 3f), "ns");
            assertTrue(projectA.grpcCalls().isEmpty());
            assertOneUpsertWithKey(projectB, "project-b-key");
            assertEquals(1, reportedToB.size());
            assertEquals(projectB.grpcHost(), reportedToB.get(0).getServerAddress());

            indexA.upsert("v1", Arrays.asList(1f, 2f, 3f), "ns");
            assertOneUpsertWithKey(projectA, "project-a-key");
            assertEquals(1, reportedToB.size());

            indexA.close();
            indexB.upsert("v2", Arrays.asList(1f, 2f, 3f), "ns");
            assertEquals(2, projectB.grpcCalls().size());
        } finally {
            indexA.close();
            indexB.close();
        }
    }
}
