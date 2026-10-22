package io.pinecone.clients;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.grpc.stub.StreamObserver;
import io.pinecone.helpers.MockPinecone;
import io.pinecone.proto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks the gRPC requests that delete, list, namespace, fetch/update-by-metadata and filtered describe-stats calls
 * send, and that their responses are parsed, against {@link MockPinecone}.
 */
public class MockedVectorOperationsTest {
    @RegisterExtension
    final MockPinecone mock = new MockPinecone();

    static final Struct FILTER = Struct.newBuilder()
            .putFields("genre", Value.newBuilder().setStringValue("drama").build())
            .build();

    static class CannedVectorService extends VectorServiceGrpc.VectorServiceImplBase {
        @Override
        public void delete(DeleteRequest request, StreamObserver<DeleteResponse> response) {
            response.onNext(DeleteResponse.getDefaultInstance());
            response.onCompleted();
        }

        @Override
        public void list(ListRequest request, StreamObserver<ListResponse> response) {
            response.onNext(ListResponse.newBuilder()
                    .addVectors(ListItem.newBuilder().setId("doc#1"))
                    .addVectors(ListItem.newBuilder().setId("doc#2"))
                    .setPagination(Pagination.newBuilder().setNext("list-next"))
                    .setNamespace(request.getNamespace())
                    .setUsage(Usage.newBuilder().setReadUnits(1))
                    .build());
            response.onCompleted();
        }

        @Override
        public void listNamespaces(ListNamespacesRequest request, StreamObserver<ListNamespacesResponse> response) {
            response.onNext(ListNamespacesResponse.newBuilder()
                    .addNamespaces(NamespaceDescription.newBuilder().setName("ns-a").setRecordCount(7))
                    .setPagination(Pagination.newBuilder().setNext("ns-next"))
                    .setTotalCount(3)
                    .build());
            response.onCompleted();
        }

        @Override
        public void createNamespace(CreateNamespaceRequest request, StreamObserver<NamespaceDescription> response) {
            response.onNext(NamespaceDescription.newBuilder().setName(request.getName()).setSchema(request.getSchema()).build());
            response.onCompleted();
        }

        @Override
        public void describeNamespace(DescribeNamespaceRequest request, StreamObserver<NamespaceDescription> response) {
            response.onNext(NamespaceDescription.newBuilder().setName(request.getNamespace()).setRecordCount(42).build());
            response.onCompleted();
        }

        @Override
        public void deleteNamespace(DeleteNamespaceRequest request, StreamObserver<DeleteResponse> response) {
            response.onNext(DeleteResponse.getDefaultInstance());
            response.onCompleted();
        }

        @Override
        public void fetchByMetadata(FetchByMetadataRequest request, StreamObserver<FetchByMetadataResponse> response) {
            response.onNext(FetchByMetadataResponse.newBuilder()
                    .putVectors("v1", Vector.newBuilder().setId("v1").addAllValues(Arrays.asList(0.1f, 0.2f)).build())
                    .setNamespace(request.getNamespace())
                    .setUsage(Usage.newBuilder().setReadUnits(2))
                    .setPagination(Pagination.newBuilder().setNext("fetch-next"))
                    .build());
            response.onCompleted();
        }

        @Override
        public void update(UpdateRequest request, StreamObserver<UpdateResponse> response) {
            response.onNext(UpdateResponse.newBuilder().setMatchedRecords(5).build());
            response.onCompleted();
        }

        @Override
        public void describeIndexStats(DescribeIndexStatsRequest request, StreamObserver<DescribeIndexStatsResponse> response) {
            response.onNext(DescribeIndexStatsResponse.newBuilder()
                    .putNamespaces("ns", NamespaceSummary.newBuilder().setVectorCount(9).build())
                    .setDimension(3)
                    .setTotalVectorCount(9)
                    .setMetric("cosine")
                    .build());
            response.onCompleted();
        }
    }

    private Index connect(String indexName) {
        mock.stubDescribeIndex(indexName, 3);
        mock.vectorService(new CannedVectorService());
        return mock.client().getIndexConnection(indexName);
    }

    private <T> T sent(int call, String method, Class<T> type) {
        MockPinecone.GrpcCall grpcCall = mock.grpcCalls().get(call);
        assertEquals("VectorService/" + method, grpcCall.method);
        assertEquals("2025-10", grpcCall.header("X-Pinecone-Api-Version"), grpcCall.method);
        assertEquals(MockPinecone.API_KEY, grpcCall.header("api-key"), grpcCall.method);
        return type.cast(grpcCall.request);
    }

    @Test
    public void deleteVariantsSendOnlyTheirSelector() {
        try (Index index = connect("vector-ops-delete")) {
            index.deleteByIds(Arrays.asList("v1", "v2"), "ns");
            index.deleteByFilter(FILTER, "ns");
            index.deleteAll("ns");
            index.deleteByIds(Collections.singletonList("v3"));
        }
        assertEquals(4, mock.grpcCalls().size());

        DeleteRequest byIds = sent(0, "Delete", DeleteRequest.class);
        assertEquals(Arrays.asList("v1", "v2"), byIds.getIdsList());
        assertEquals("ns", byIds.getNamespace());
        assertFalse(byIds.getDeleteAll());
        assertFalse(byIds.hasFilter());

        DeleteRequest byFilter = sent(1, "Delete", DeleteRequest.class);
        assertEquals(FILTER, byFilter.getFilter());
        assertEquals("ns", byFilter.getNamespace());
        assertEquals(0, byFilter.getIdsCount());
        assertFalse(byFilter.getDeleteAll());

        DeleteRequest all = sent(2, "Delete", DeleteRequest.class);
        assertTrue(all.getDeleteAll());
        assertEquals("ns", all.getNamespace());
        assertEquals(0, all.getIdsCount());
        assertFalse(all.hasFilter());

        DeleteRequest defaultNamespace = sent(3, "Delete", DeleteRequest.class);
        assertEquals(Collections.singletonList("v3"), defaultNamespace.getIdsList());
        assertEquals("", defaultNamespace.getNamespace());
    }

    @Test
    public void listSendsOnlyTheGivenParametersAndParsesThePage() {
        ListResponse page;
        try (Index index = connect("vector-ops-list")) {
            page = index.list("ns", "doc#", "token-1", 25);
            index.list();
            index.list("ns", 10);
            index.list("ns", "doc#");
        }

        assertEquals(Arrays.asList("doc#1", "doc#2"),
                Arrays.asList(page.getVectors(0).getId(), page.getVectors(1).getId()));
        assertEquals("list-next", page.getPagination().getNext());
        assertEquals("ns", page.getNamespace());
        assertEquals(1, page.getUsage().getReadUnits());

        ListRequest full = sent(0, "List", ListRequest.class);
        assertEquals("ns", full.getNamespace());
        assertEquals("doc#", full.getPrefix());
        assertEquals("token-1", full.getPaginationToken());
        assertEquals(25, full.getLimit());

        ListRequest empty = sent(1, "List", ListRequest.class);
        assertEquals("", empty.getNamespace());
        assertFalse(empty.hasPrefix());
        assertFalse(empty.hasLimit());
        assertFalse(empty.hasPaginationToken());

        ListRequest limitOnly = sent(2, "List", ListRequest.class);
        assertEquals(10, limitOnly.getLimit());
        assertFalse(limitOnly.hasPrefix());
        assertFalse(limitOnly.hasPaginationToken());

        ListRequest prefixOnly = sent(3, "List", ListRequest.class);
        assertEquals("doc#", prefixOnly.getPrefix());
        assertFalse(prefixOnly.hasLimit());
        assertFalse(prefixOnly.hasPaginationToken());
    }

    @Test
    public void listNamespacesOmitsEmptyPrefixAndNullToken() {
        ListNamespacesResponse page;
        try (Index index = connect("vector-ops-list-ns")) {
            page = index.listNamespaces("tenant-", "token-1", 5);
            index.listNamespaces("", null, 20);
            index.listNamespaces();
        }

        assertEquals(1, page.getNamespacesCount());
        assertEquals("ns-a", page.getNamespaces(0).getName());
        assertEquals(7, page.getNamespaces(0).getRecordCount());
        assertEquals("ns-next", page.getPagination().getNext());
        assertEquals(3, page.getTotalCount());

        ListNamespacesRequest full = sent(0, "ListNamespaces", ListNamespacesRequest.class);
        assertEquals("tenant-", full.getPrefix());
        assertEquals("token-1", full.getPaginationToken());
        assertEquals(5, full.getLimit());

        ListNamespacesRequest blankPrefix = sent(1, "ListNamespaces", ListNamespacesRequest.class);
        assertFalse(blankPrefix.hasPrefix());
        assertFalse(blankPrefix.hasPaginationToken());
        assertEquals(20, blankPrefix.getLimit());

        ListNamespacesRequest none = sent(2, "ListNamespaces", ListNamespacesRequest.class);
        assertFalse(none.hasPrefix());
        assertFalse(none.hasPaginationToken());
        assertFalse(none.hasLimit());
    }

    @Test
    public void namespaceLifecycleSendsNamesAndSchema() {
        MetadataSchema schema = MetadataSchema.newBuilder()
                .putFields("genre", MetadataFieldProperties.newBuilder().setFilterable(true).build())
                .build();
        NamespaceDescription created;
        NamespaceDescription described;
        try (Index index = connect("vector-ops-ns")) {
            created = index.createNamespace("tenant-1", schema);
            index.createNamespace("tenant-2");
            described = index.describeNamespace("tenant-1");
            index.deleteNamespace("tenant-1");
        }

        assertEquals("tenant-1", created.getName());
        assertTrue(created.getSchema().getFieldsOrThrow("genre").getFilterable());
        assertEquals("tenant-1", described.getName());
        assertEquals(42, described.getRecordCount());

        CreateNamespaceRequest withSchema = sent(0, "CreateNamespace", CreateNamespaceRequest.class);
        assertEquals("tenant-1", withSchema.getName());
        assertEquals(schema, withSchema.getSchema());

        CreateNamespaceRequest withoutSchema = sent(1, "CreateNamespace", CreateNamespaceRequest.class);
        assertEquals("tenant-2", withoutSchema.getName());
        assertFalse(withoutSchema.hasSchema());

        assertEquals("tenant-1", sent(2, "DescribeNamespace", DescribeNamespaceRequest.class).getNamespace());
        assertEquals("tenant-1", sent(3, "DeleteNamespace", DeleteNamespaceRequest.class).getNamespace());
    }

    @Test
    public void fetchByMetadataSendsFilterAndOnlyPositiveLimitAndNonEmptyToken() {
        FetchByMetadataResponse fetched;
        try (Index index = connect("vector-ops-fetch")) {
            fetched = index.fetchByMetadata("ns", FILTER, 10, "token-1");
            index.fetchByMetadata("ns", FILTER, 0, "");
            index.fetchByMetadata("ns", FILTER);
        }

        assertEquals(Arrays.asList(0.1f, 0.2f), fetched.getVectorsOrThrow("v1").getValuesList());
        assertEquals("ns", fetched.getNamespace());
        assertEquals(2, fetched.getUsage().getReadUnits());
        assertEquals("fetch-next", fetched.getPagination().getNext());

        FetchByMetadataRequest full = sent(0, "FetchByMetadata", FetchByMetadataRequest.class);
        assertEquals("ns", full.getNamespace());
        assertEquals(FILTER, full.getFilter());
        assertEquals(10, full.getLimit());
        assertEquals("token-1", full.getPaginationToken());

        for (int call = 1; call <= 2; call++) {
            FetchByMetadataRequest defaults = sent(call, "FetchByMetadata", FetchByMetadataRequest.class);
            assertEquals(FILTER, defaults.getFilter());
            assertFalse(defaults.hasLimit(), "call " + call);
            assertFalse(defaults.hasPaginationToken(), "call " + call);
        }
    }

    @Test
    public void updateByMetadataSendsFilterMetadataAndDryRun() {
        Struct newMetadata = Struct.newBuilder()
                .putFields("reviewed", Value.newBuilder().setBoolValue(true).build())
                .build();
        UpdateResponse updated;
        try (Index index = connect("vector-ops-update")) {
            updated = index.updateByMetadata(FILTER, newMetadata, "ns", true);
            index.updateByMetadata(FILTER, newMetadata, "ns");
        }

        assertEquals(5, updated.getMatchedRecords());

        UpdateRequest dryRun = sent(0, "Update", UpdateRequest.class);
        assertEquals(FILTER, dryRun.getFilter());
        assertEquals(newMetadata, dryRun.getSetMetadata());
        assertEquals("ns", dryRun.getNamespace());
        assertTrue(dryRun.getDryRun());
        assertEquals("", dryRun.getId());
        assertEquals(0, dryRun.getValuesCount());

        UpdateRequest applied = sent(1, "Update", UpdateRequest.class);
        assertTrue(applied.hasDryRun());
        assertFalse(applied.getDryRun());
    }

    @Test
    public void describeIndexStatsSendsFilterOnlyWhenGiven() {
        DescribeIndexStatsResponse stats;
        try (Index index = connect("vector-ops-stats")) {
            stats = index.describeIndexStats(FILTER);
            index.describeIndexStats();
        }

        assertEquals(9, stats.getNamespacesOrThrow("ns").getVectorCount());
        assertEquals(3, stats.getDimension());
        assertEquals(9, stats.getTotalVectorCount());
        assertEquals("cosine", stats.getMetric());

        assertEquals(FILTER, sent(0, "DescribeIndexStats", DescribeIndexStatsRequest.class).getFilter());
        assertFalse(sent(1, "DescribeIndexStats", DescribeIndexStatsRequest.class).hasFilter());
    }

    @Test
    public void asyncDeleteAllAndListUseTheSameRequests() throws Exception {
        mock.stubDescribeIndex("vector-ops-async", 3);
        mock.vectorService(new CannedVectorService());
        List<String> ids;
        try (AsyncIndex index = mock.client().getAsyncIndexConnection("vector-ops-async")) {
            index.deleteAll("ns").get();
            ListResponse page = index.list("ns", "doc#", "token-1", 25).get();
            ids = Arrays.asList(page.getVectors(0).getId(), page.getVectors(1).getId());
        }

        assertEquals(Arrays.asList("doc#1", "doc#2"), ids);
        DeleteRequest all = sent(0, "Delete", DeleteRequest.class);
        assertTrue(all.getDeleteAll());
        assertEquals("ns", all.getNamespace());
        ListRequest list = sent(1, "List", ListRequest.class);
        assertEquals("doc#", list.getPrefix());
        assertEquals("token-1", list.getPaginationToken());
        assertEquals(25, list.getLimit());
    }
}
