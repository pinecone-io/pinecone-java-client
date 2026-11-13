package io.pinecone.clients;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.grpc.stub.StreamObserver;
import io.pinecone.commons.IndexInterface;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.helpers.MockPinecone;
import io.pinecone.proto.*;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks that {@link Index} and {@link AsyncIndex} reject an id or namespace holding an unpaired UTF-16 surrogate
 * before sending anything, since protobuf would otherwise send it as {@code ?} (issue #84), and that a well-formed
 * surrogate pair is sent unchanged.
 */
public class MockedSurrogateIdTest {
    @RegisterExtension
    final MockPinecone mock = new MockPinecone();

    static final String LONE_HIGH = "x\uD83D";
    static final String LONE_LOW = "x\uDE00";
    static final String EMOJI = "x😀";
    static final List<Float> VECTOR = Arrays.asList(0.1f, 0.2f, 0.3f);
    static final Struct FILTER = Struct.newBuilder()
            .putFields("genre", Value.newBuilder().setStringValue("drama").build())
            .build();

    static class CannedVectorService extends VectorServiceGrpc.VectorServiceImplBase {
        @Override
        public void upsert(UpsertRequest request, StreamObserver<UpsertResponse> response) {
            response.onNext(UpsertResponse.newBuilder().setUpsertedCount(request.getVectorsCount()).build());
            response.onCompleted();
        }

        @Override
        public void query(QueryRequest request, StreamObserver<QueryResponse> response) {
            response.onNext(QueryResponse.getDefaultInstance());
            response.onCompleted();
        }

        @Override
        public void fetch(FetchRequest request, StreamObserver<FetchResponse> response) {
            response.onNext(FetchResponse.getDefaultInstance());
            response.onCompleted();
        }

        @Override
        public void update(UpdateRequest request, StreamObserver<UpdateResponse> response) {
            response.onNext(UpdateResponse.getDefaultInstance());
            response.onCompleted();
        }

        @Override
        public void delete(DeleteRequest request, StreamObserver<DeleteResponse> response) {
            response.onNext(DeleteResponse.getDefaultInstance());
            response.onCompleted();
        }
    }

    interface Await {
        Object on(Object result) throws Exception;
    }

    static final Await SYNC = result -> result;
    static final Await ASYNC = result -> ((Future<?>) result).get(10, TimeUnit.SECONDS);

    private void connect(String indexName) {
        mock.stubDescribeIndex(indexName, 3);
        mock.vectorService(new CannedVectorService());
    }

    @Test
    public void loneSurrogateIdsAreRejectedBeforeSending() throws Exception {
        connect("surrogate-ids");
        try (Index index = mock.client().getIndexConnection("surrogate-ids")) {
            assertEveryCallRejects(index, () -> index.fetchByMetadata(LONE_HIGH, FILTER));
        }
        assertEquals(0, mock.grpcCalls().size());
    }

    @Test
    public void asyncLoneSurrogateIdsAreRejectedBeforeSending() throws Exception {
        connect("surrogate-ids-async");
        try (AsyncIndex index = mock.client().getAsyncIndexConnection("surrogate-ids-async")) {
            assertEveryCallRejects(index, () -> index.fetchByMetadata(LONE_HIGH, FILTER));
        }
        assertEquals(0, mock.grpcCalls().size());
    }

    @Test
    public void buildUpsertVectorWithUnsignedIndicesRejectsLoneSurrogate() {
        PineconeValidationException e = assertThrows(PineconeValidationException.class,
                () -> IndexInterface.buildUpsertVectorWithUnsignedIndices(LONE_HIGH, VECTOR, null, null, null));
        assertEquals("Invalid upsert request. Vector id contains an unpaired UTF-16 surrogate (\\uD83D) at index 1, "
                + "so it cannot be sent. This usually means a string was cut in the middle of a character such as "
                + "an emoji.", e.getMessage());
    }

    @Test
    public void surrogatePairsAreSentUnchanged() throws Exception {
        connect("surrogate-pairs");
        try (Index index = mock.client().getIndexConnection("surrogate-pairs")) {
            sendWellFormedPairs(index, SYNC);
        }
        assertWellFormedPairsSent();
    }

    @Test
    public void asyncSurrogatePairsAreSentUnchanged() throws Exception {
        connect("surrogate-pairs-async");
        try (AsyncIndex index = mock.client().getAsyncIndexConnection("surrogate-pairs-async")) {
            sendWellFormedPairs(index, ASYNC);
        }
        assertWellFormedPairsSent();
    }

    private static void assertEveryCallRejects(IndexInterface<?, ?, ?, ?, ?, ?, ?, ?, ?> index,
                                               Executable fetchByMetadata) {
        List<VectorWithUnsignedIndices> batch = Collections.singletonList(new VectorWithUnsignedIndices(LONE_LOW,
                VECTOR));
        List<VectorWithUnsignedIndices> goodBatch = Collections.singletonList(new VectorWithUnsignedIndices("ok",
                VECTOR));

        assertRejects("upsert", "Vector id", '\uD83D', () -> index.upsert(LONE_HIGH, VECTOR, "ns"));
        assertRejects("upsert", "Vector id", '\uDE00', () -> index.upsert(LONE_LOW, VECTOR, "ns"));
        assertRejects("upsert", "Vector id", '\uDE00', () -> index.upsert(batch, "ns"));
        assertRejects("upsert", "Namespace", '\uD83D', () -> index.upsert("ok", VECTOR, LONE_HIGH));
        assertRejects("upsert", "Namespace", '\uD83D', () -> index.upsert(goodBatch, LONE_HIGH));
        assertRejects("query", "Vector id", '\uD83D', () -> index.queryByVectorId(1, LONE_HIGH, "ns"));
        assertRejects("query", "Namespace", '\uD83D', () -> index.queryByVector(1, VECTOR, LONE_HIGH));
        assertRejects("fetch", "Vector id", '\uD83D',
                () -> index.fetch(Arrays.asList("ok", LONE_HIGH), "ns"));
        assertRejects("fetch", "Namespace", '\uD83D',
                () -> index.fetch(Collections.singletonList("ok"), LONE_HIGH));
        assertRejects("fetch by metadata", "Namespace", '\uD83D', fetchByMetadata);
        assertRejects("update", "Vector id", '\uD83D', () -> index.update(LONE_HIGH, VECTOR, "ns"));
        assertRejects("update", "Namespace", '\uD83D', () -> index.update("ok", VECTOR, LONE_HIGH));
        assertRejects("update by metadata", "Namespace", '\uD83D',
                () -> index.updateByMetadata(FILTER, FILTER, LONE_HIGH));
        assertRejects("delete", "Vector id", '\uD83D',
                () -> index.deleteByIds(Collections.singletonList(LONE_HIGH), "ns"));
        assertRejects("delete", "Namespace", '\uD83D', () -> index.deleteAll(LONE_HIGH));
        assertRejects("delete", "Namespace", '\uD83D', () -> index.deleteByFilter(FILTER, LONE_HIGH));
    }

    private static void assertRejects(String request, String field, char surrogate, Executable call) {
        PineconeValidationException e = assertThrows(PineconeValidationException.class, call);
        String expected = String.format("Invalid %s request. %s contains an unpaired UTF-16 surrogate (\\u%04X) at "
                + "index", request, field, (int) surrogate);
        assertTrue(e.getMessage().startsWith(expected), e.getMessage());
    }

    private static void sendWellFormedPairs(IndexInterface<?, ?, ?, ?, ?, ?, ?, ?, ?> index, Await await)
            throws Exception {
        await.on(index.upsert(EMOJI, VECTOR, EMOJI));
        await.on(index.upsert(Collections.singletonList(
                IndexInterface.buildUpsertVectorWithUnsignedIndices(EMOJI, VECTOR, null, null, null)), EMOJI));
        await.on(index.queryByVectorId(1, EMOJI, EMOJI));
        await.on(index.fetch(Collections.singletonList(EMOJI), EMOJI));
        await.on(index.update(EMOJI, VECTOR, EMOJI));
        await.on(index.deleteByIds(Collections.singletonList(EMOJI), EMOJI));
    }

    private void assertWellFormedPairsSent() {
        List<MockPinecone.GrpcCall> calls = mock.grpcCalls();
        assertEquals(6, calls.size());
        UpsertRequest single = (UpsertRequest) calls.get(0).request;
        assertEquals(EMOJI, single.getVectors(0).getId());
        assertEquals(EMOJI, single.getNamespace());
        UpsertRequest batch = (UpsertRequest) calls.get(1).request;
        assertEquals(EMOJI, batch.getVectors(0).getId());
        assertEquals(EMOJI, batch.getNamespace());
        QueryRequest query = (QueryRequest) calls.get(2).request;
        assertEquals(EMOJI, query.getId());
        assertEquals(EMOJI, query.getNamespace());
        FetchRequest fetch = (FetchRequest) calls.get(3).request;
        assertEquals(Collections.singletonList(EMOJI), fetch.getIdsList());
        assertEquals(EMOJI, fetch.getNamespace());
        UpdateRequest update = (UpdateRequest) calls.get(4).request;
        assertEquals(EMOJI, update.getId());
        assertEquals(EMOJI, update.getNamespace());
        DeleteRequest delete = (DeleteRequest) calls.get(5).request;
        assertEquals(Collections.singletonList(EMOJI), delete.getIdsList());
        assertEquals(EMOJI, delete.getNamespace());
    }
}
