package io.pinecone.clients;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.grpc.stub.StreamObserver;
import io.pinecone.commons.IndexInterface;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.helpers.MockPinecone;
import io.pinecone.proto.*;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks that every upsert, query, fetch and update overload of {@link Index} and {@link AsyncIndex} puts its
 * arguments into the gRPC request, including unsigned sparse indices, and that query and fetch responses are parsed,
 * against {@link MockPinecone}.
 */
public class MockedDataPlaneOverloadsTest {
    @RegisterExtension
    final MockPinecone mock = new MockPinecone();

    static final List<Float> VECTOR = Arrays.asList(0.1f, 0.2f, 0.3f);

    static final Struct FILTER = Struct.newBuilder()
            .putFields("genre", Value.newBuilder().setStringValue("drama").build())
            .build();

    static final Struct METADATA = Struct.newBuilder()
            .putFields("year", Value.newBuilder().setNumberValue(2020).build())
            .build();

    static class CannedVectorService extends VectorServiceGrpc.VectorServiceImplBase {
        @Override
        public void upsert(UpsertRequest request, StreamObserver<UpsertResponse> response) {
            response.onNext(UpsertResponse.newBuilder().setUpsertedCount(request.getVectorsCount()).build());
            response.onCompleted();
        }

        @Override
        public void query(QueryRequest request, StreamObserver<QueryResponse> response) {
            response.onNext(QueryResponse.newBuilder()
                    .setNamespace(request.getNamespace())
                    .addMatches(ScoredVector.newBuilder()
                            .setId("m1")
                            .setScore(0.5f)
                            .addAllValues(VECTOR)
                            .setSparseValues(SparseValues.newBuilder()
                                    .addAllIndices(Arrays.asList(7, -1))
                                    .addAllValues(Arrays.asList(0.25f, 0.75f)))
                            .setMetadata(METADATA))
                    .setUsage(Usage.newBuilder().setReadUnits(3))
                    .build());
            response.onCompleted();
        }

        @Override
        public void fetch(FetchRequest request, StreamObserver<FetchResponse> response) {
            FetchResponse.Builder fetched = FetchResponse.newBuilder().setNamespace(request.getNamespace());
            for (String id : request.getIdsList()) {
                fetched.putVectors(id, Vector.newBuilder().setId(id).addAllValues(VECTOR).build());
            }
            response.onNext(fetched.build());
            response.onCompleted();
        }

        @Override
        public void update(UpdateRequest request, StreamObserver<UpdateResponse> response) {
            response.onNext(UpdateResponse.getDefaultInstance());
            response.onCompleted();
        }
    }

    /** Turns a sync result into itself and an async result into its completed value, so calls arrive in order. */
    interface Await {
        Object on(Object result) throws Exception;
    }

    static final Await SYNC = result -> result;
    static final Await ASYNC = result -> ((Future<?>) result).get(10, TimeUnit.SECONDS);

    private void connect(String indexName) {
        mock.stubDescribeIndex(indexName, 3);
        mock.vectorService(new CannedVectorService());
    }

    private <T> T sent(int call, String method, Class<T> type) {
        MockPinecone.GrpcCall grpcCall = mock.grpcCalls().get(call);
        assertEquals("VectorService/" + method, grpcCall.method);
        assertEquals("2025-10", grpcCall.header("X-Pinecone-Api-Version"), grpcCall.method);
        assertEquals(MockPinecone.API_KEY, grpcCall.header("api-key"), grpcCall.method);
        return type.cast(grpcCall.request);
    }

    @Test
    public void queryOverloadsSendTheirArguments() throws Exception {
        connect("overloads-query");
        QueryResponseWithUnsignedIndices response;
        try (Index index = mock.client().getIndexConnection("overloads-query")) {
            response = (QueryResponseWithUnsignedIndices) callEveryQueryOverload(index, SYNC);
        }
        assertQueryRequests();
        assertQueryResponse(response);
    }

    @Test
    public void asyncQueryOverloadsSendTheirArguments() throws Exception {
        connect("overloads-query-async");
        QueryResponseWithUnsignedIndices response;
        try (AsyncIndex index = mock.client().getAsyncIndexConnection("overloads-query-async")) {
            response = (QueryResponseWithUnsignedIndices) callEveryQueryOverload(index, ASYNC);
        }
        assertQueryRequests();
        assertQueryResponse(response);
    }

    @Test
    public void writeAndFetchOverloadsSendTheirArguments() throws Exception {
        connect("overloads-write");
        Object[] responses;
        try (Index index = mock.client().getIndexConnection("overloads-write")) {
            responses = callEveryWriteAndFetchOverload(index, SYNC);
        }
        assertWriteAndFetchRequests();
        assertWriteAndFetchResponses(responses);
    }

    @Test
    public void asyncWriteAndFetchOverloadsSendTheirArguments() throws Exception {
        connect("overloads-write-async");
        Object[] responses;
        try (AsyncIndex index = mock.client().getAsyncIndexConnection("overloads-write-async")) {
            responses = callEveryWriteAndFetchOverload(index, ASYNC);
        }
        assertWriteAndFetchRequests();
        assertWriteAndFetchResponses(responses);
    }

    @Test
    public void sparseIndexOutOfUnsignedRangeIsRejectedBeforeSending() throws Exception {
        connect("overloads-range");
        List<Long> tooLarge = Collections.singletonList(4294967296L);
        List<Float> sparseValues = Collections.singletonList(0.5f);
        try (Index index = mock.client().getIndexConnection("overloads-range")) {
            PineconeValidationException upsert = assertThrows(PineconeValidationException.class,
                    () -> index.upsert("w1", VECTOR, tooLarge, sparseValues, null, "ns"));
            assertEquals("Sparse indices are out of range for unsigned 32-bit integers.", upsert.getMessage());
            PineconeValidationException query = assertThrows(PineconeValidationException.class,
                    () -> index.query(1, VECTOR, Collections.singletonList(-1L), sparseValues, null, "ns", null,
                            false, false));
            assertEquals("Sparse indices are out of range for unsigned 32-bit integers.", query.getMessage());
        }
        assertEquals(0, mock.grpcCalls().size());
    }

    @Test
    public void batchUpsertWithNullNamespaceUsesDefaultNamespace() throws Exception {
        connect("overloads-batch-null-ns");
        try (Index index = mock.client().getIndexConnection("overloads-batch-null-ns")) {
            SYNC.on(index.upsert(batchOfOne(), null));
        }
        assertSentToDefaultNamespace();
    }

    @Test
    public void asyncBatchUpsertWithNullNamespaceUsesDefaultNamespace() throws Exception {
        connect("overloads-batch-null-ns-async");
        try (AsyncIndex index = mock.client().getAsyncIndexConnection("overloads-batch-null-ns-async")) {
            ASYNC.on(index.upsert(batchOfOne(), null));
        }
        assertSentToDefaultNamespace();
    }

    private static List<VectorWithUnsignedIndices> batchOfOne() {
        return Collections.singletonList(IndexInterface.buildUpsertVectorWithUnsignedIndices("w1", VECTOR, null, null,
                null));
    }

    private void assertSentToDefaultNamespace() {
        assertEquals(1, mock.grpcCalls().size());
        UpsertRequest batch = sent(0, "Upsert", UpsertRequest.class);
        assertEquals("", batch.getNamespace());
        assertEquals("w1", batch.getVectors(0).getId());
    }

    /** Calls the six queryByVectorId and six queryByVector overloads in order, each with a distinct topK. */
    private static <U> Object callEveryQueryOverload(IndexInterface<?, U, ?, ?, ?, ?, ?, ?, ?> index, Await await)
            throws Exception {
        Object first = await.on(index.queryByVectorId(1, "q1"));
        await.on(index.queryByVectorId(2, "q1", true, true));
        await.on(index.queryByVectorId(3, "q1", "ns"));
        await.on(index.queryByVectorId(4, "q1", "ns", true, false));
        await.on(index.queryByVectorId(5, "q1", "ns", FILTER));
        await.on(index.queryByVectorId(6, "q1", "ns", FILTER, false, true));
        await.on(index.queryByVector(7, VECTOR));
        await.on(index.queryByVector(8, VECTOR, true, true));
        await.on(index.queryByVector(9, VECTOR, "ns"));
        await.on(index.queryByVector(10, VECTOR, "ns", true, false));
        await.on(index.queryByVector(11, VECTOR, "ns", FILTER));
        await.on(index.queryByVector(12, VECTOR, "ns", FILTER, false, true));
        await.on(index.query(13, VECTOR, Arrays.asList(1L, 4294967295L), Arrays.asList(0.5f, 0.25f), null, "ns",
                FILTER, true, true));
        return first;
    }

    private void assertQueryRequests() {
        assertEquals(13, mock.grpcCalls().size());
        // {namespace, has filter, includeValues, includeMetadata} for each overload shape, repeated for id and vector.
        Object[][] shapes = {
                {"", false, false, false},
                {"", false, true, true},
                {"ns", false, false, false},
                {"ns", false, true, false},
                {"ns", true, false, false},
                {"ns", true, false, true},
        };
        for (int call = 0; call < 12; call++) {
            QueryRequest query = sent(call, "Query", QueryRequest.class);
            Object[] shape = shapes[call % 6];
            String context = "query call " + call;
            assertEquals(call + 1, query.getTopK(), context);
            if (call < 6) {
                assertEquals("q1", query.getId(), context);
                assertEquals(0, query.getVectorCount(), context);
            } else {
                assertEquals("", query.getId(), context);
                assertEquals(VECTOR, query.getVectorList(), context);
            }
            assertEquals(shape[0], query.getNamespace(), context);
            assertEquals(shape[1], query.hasFilter(), context);
            if (query.hasFilter()) {
                assertEquals(FILTER, query.getFilter(), context);
            }
            assertEquals(shape[2], query.getIncludeValues(), context);
            assertEquals(shape[3], query.getIncludeMetadata(), context);
            assertFalse(query.hasSparseVector(), context);
        }

        QueryRequest hybrid = sent(12, "Query", QueryRequest.class);
        assertEquals(13, hybrid.getTopK());
        assertEquals(VECTOR, hybrid.getVectorList());
        assertEquals(Arrays.asList(1, -1), hybrid.getSparseVector().getIndicesList());
        assertEquals(Arrays.asList(0.5f, 0.25f), hybrid.getSparseVector().getValuesList());
        assertEquals("ns", hybrid.getNamespace());
        assertEquals(FILTER, hybrid.getFilter());
        assertTrue(hybrid.getIncludeValues());
        assertTrue(hybrid.getIncludeMetadata());
    }

    private static void assertQueryResponse(QueryResponseWithUnsignedIndices response) {
        assertEquals("", response.getNamespace());
        assertEquals(3, response.getUsage().getReadUnits());
        assertEquals(1, response.getMatchesList().size());
        ScoredVectorWithUnsignedIndices match = response.getMatches(0);
        assertEquals("m1", match.getId());
        assertEquals(0.5f, match.getScore(), 1e-6f);
        assertEquals(VECTOR, match.getValuesList());
        assertEquals(Arrays.asList(7L, 4294967295L), match.getSparseValuesWithUnsignedIndices().getIndicesWithUnsigned32IntList());
        assertEquals(Arrays.asList(0.25f, 0.75f), match.getSparseValuesWithUnsignedIndices().getValuesList());
        assertEquals(METADATA, match.getMetadata());
    }

    /** Calls the four upsert, two fetch and three update overloads in order. */
    private static Object[] callEveryWriteAndFetchOverload(IndexInterface<?, ?, ?, ?, ?, ?, ?, ?, ?> index,
                                                           Await await) throws Exception {
        return new Object[]{
                await.on(index.upsert("w1", VECTOR)),
                await.on(index.upsert("w2", VECTOR, "ns")),
                await.on(index.upsert("w3", VECTOR, Arrays.asList(1L, 4294967295L), Arrays.asList(0.5f, 0.25f),
                        METADATA, "ns")),
                await.on(index.upsert(Arrays.asList(
                        IndexInterface.buildUpsertVectorWithUnsignedIndices("w4", VECTOR,
                                Collections.singletonList(2147483648L), Collections.singletonList(0.9f), null),
                        IndexInterface.buildUpsertVectorWithUnsignedIndices("w5", VECTOR, null, null, METADATA)),
                        "ns")),
                await.on(index.fetch(Arrays.asList("w1", "w2"))),
                await.on(index.fetch(Collections.singletonList("w3"), "ns")),
                await.on(index.update("w1", VECTOR)),
                await.on(index.update("w2", VECTOR, "ns")),
                await.on(index.update("w3", null, METADATA, "ns", Collections.singletonList(4294967295L),
                        Collections.singletonList(0.75f))),
        };
    }

    private void assertWriteAndFetchRequests() {
        assertEquals(9, mock.grpcCalls().size());

        UpsertRequest dense = sent(0, "Upsert", UpsertRequest.class);
        assertEquals("", dense.getNamespace());
        assertEquals(1, dense.getVectorsCount());
        assertEquals("w1", dense.getVectors(0).getId());
        assertEquals(VECTOR, dense.getVectors(0).getValuesList());
        assertFalse(dense.getVectors(0).hasSparseValues());
        assertFalse(dense.getVectors(0).hasMetadata());

        UpsertRequest denseInNamespace = sent(1, "Upsert", UpsertRequest.class);
        assertEquals("ns", denseInNamespace.getNamespace());
        assertEquals("w2", denseInNamespace.getVectors(0).getId());
        assertEquals(VECTOR, denseInNamespace.getVectors(0).getValuesList());

        UpsertRequest full = sent(2, "Upsert", UpsertRequest.class);
        assertEquals("ns", full.getNamespace());
        Vector fullVector = full.getVectors(0);
        assertEquals("w3", fullVector.getId());
        assertEquals(VECTOR, fullVector.getValuesList());
        assertEquals(Arrays.asList(1, -1), fullVector.getSparseValues().getIndicesList());
        assertEquals(Arrays.asList(0.5f, 0.25f), fullVector.getSparseValues().getValuesList());
        assertEquals(METADATA, fullVector.getMetadata());

        UpsertRequest batch = sent(3, "Upsert", UpsertRequest.class);
        assertEquals("ns", batch.getNamespace());
        assertEquals(2, batch.getVectorsCount());
        assertEquals("w4", batch.getVectors(0).getId());
        assertEquals(Collections.singletonList(Integer.MIN_VALUE), batch.getVectors(0).getSparseValues().getIndicesList());
        assertEquals(Collections.singletonList(0.9f), batch.getVectors(0).getSparseValues().getValuesList());
        assertFalse(batch.getVectors(0).hasMetadata());
        assertEquals("w5", batch.getVectors(1).getId());
        assertFalse(batch.getVectors(1).hasSparseValues());
        assertEquals(METADATA, batch.getVectors(1).getMetadata());

        FetchRequest fetchDefault = sent(4, "Fetch", FetchRequest.class);
        assertEquals(Arrays.asList("w1", "w2"), fetchDefault.getIdsList());
        assertEquals("", fetchDefault.getNamespace());
        FetchRequest fetchInNamespace = sent(5, "Fetch", FetchRequest.class);
        assertEquals(Collections.singletonList("w3"), fetchInNamespace.getIdsList());
        assertEquals("ns", fetchInNamespace.getNamespace());

        UpdateRequest values = sent(6, "Update", UpdateRequest.class);
        assertEquals("w1", values.getId());
        assertEquals(VECTOR, values.getValuesList());
        assertEquals("", values.getNamespace());
        assertFalse(values.hasSetMetadata());
        assertFalse(values.hasSparseValues());
        assertFalse(values.hasFilter());

        UpdateRequest valuesInNamespace = sent(7, "Update", UpdateRequest.class);
        assertEquals("w2", valuesInNamespace.getId());
        assertEquals(VECTOR, valuesInNamespace.getValuesList());
        assertEquals("ns", valuesInNamespace.getNamespace());

        UpdateRequest metadataAndSparse = sent(8, "Update", UpdateRequest.class);
        assertEquals("w3", metadataAndSparse.getId());
        assertEquals(0, metadataAndSparse.getValuesCount());
        assertEquals(METADATA, metadataAndSparse.getSetMetadata());
        assertEquals("ns", metadataAndSparse.getNamespace());
        assertEquals(Collections.singletonList(-1), metadataAndSparse.getSparseValues().getIndicesList());
        assertEquals(Collections.singletonList(0.75f), metadataAndSparse.getSparseValues().getValuesList());
    }

    private static void assertWriteAndFetchResponses(Object[] responses) {
        assertEquals(1, ((UpsertResponse) responses[0]).getUpsertedCount());
        assertEquals(2, ((UpsertResponse) responses[3]).getUpsertedCount());

        FetchResponse fetched = (FetchResponse) responses[4];
        assertEquals("", fetched.getNamespace());
        assertEquals(2, fetched.getVectorsCount());
        assertEquals(VECTOR, fetched.getVectorsOrThrow("w2").getValuesList());
        FetchResponse fetchedInNamespace = (FetchResponse) responses[5];
        assertEquals("ns", fetchedInNamespace.getNamespace());
        assertEquals("w3", fetchedInNamespace.getVectorsOrThrow("w3").getId());
    }
}
