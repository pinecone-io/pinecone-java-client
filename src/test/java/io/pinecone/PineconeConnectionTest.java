package io.pinecone;

import io.pinecone.configs.PineconeConfig;
import io.pinecone.configs.PineconeConnection;
import io.pinecone.proto.UpsertRequest;
import io.pinecone.proto.UpsertResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import io.grpc.Status;
import io.pinecone.proto.VectorServiceGrpc;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class PineconeConnectionTest {

    private PineconeConnection pineconeConnection;
    private VectorServiceGrpc.VectorServiceBlockingStub blockingStub;

    @BeforeEach
    public void setUp() {
        // Create a mock PineconeConfig
        PineconeConfig config = new PineconeConfig("dummy-api-key");
        config.setHost("http://localhost:8080");

        // Initialize PineconeConnection with the mock config
        pineconeConnection = new PineconeConnection(config);

        // Mock the blocking stub
        blockingStub = mock(VectorServiceGrpc.VectorServiceBlockingStub.class);
        pineconeConnection = spy(pineconeConnection);
        doReturn(blockingStub).when(pineconeConnection).getBlockingStub();
    }

    @AfterEach
    public void tearDown() {
        pineconeConnection.close();
    }

    @Test
    public void testRetry_SuccessAfterOneFailure() {
        // Create a dummy request and response
        UpsertRequest request = UpsertRequest.newBuilder().build();
        UpsertResponse response = UpsertResponse.newBuilder().setUpsertedCount(3).build();

        // Simulate failure followed by success for upsert method
        when(blockingStub.upsert(any(UpsertRequest.class)))
                .thenThrow(Status.UNAVAILABLE.asRuntimeException()) // First call throws UNAVAILABLE
                .thenReturn(response);  // Second call returns response

        // Call the upsert method
        UpsertResponse actualResponse = blockingStub.upsert(request);

        // Verify the actualResponse equals expected response
        assertEquals(actualResponse, response);

        // Verify the method was retried
        verify(blockingStub, times(2)).upsert(any(UpsertRequest.class));
    }

    @Test
    void testGetEndpointWithConnectionUrlWithHttps() {
        String hostWithHttps = "https://steps-784-123-eqasas0aaaa1213aasasc-1223-f1eea9.svc.production.pinecone.io";
        String endpoint = PineconeConnection.formatEndpoint(hostWithHttps);

        assertEquals("steps-784-123-eqasas0aaaa1213aasasc-1223-f1eea9.svc.production.pinecone.io", endpoint);
    }

    @Test
    void testGetEndpointWithConnectionUrlWithHttp() {
        String hostWithHttp = "http://steps-784-123-eqasas0aaaa1213aasasc-1223-f1eea9.svc.production.pinecone.io";
        String endpoint = PineconeConnection.formatEndpoint(hostWithHttp);

        assertEquals("steps-784-123-eqasas0aaaa1213aasasc-1223-f1eea9.svc.production.pinecone.io", endpoint);
    }
}