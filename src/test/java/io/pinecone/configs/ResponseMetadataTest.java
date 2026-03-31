package io.pinecone.configs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponseMetadataTest {

    @Test
    void testBuilderWithAllFields() {
        ResponseMetadata metadata = ResponseMetadata.builder()
                .operationName("query")
                .indexName("test-index")
                .namespace("test-namespace")
                .serverAddress("test-index-abc.svc.pinecone.io")
                .clientDurationMs(150)
                .serverDurationMs(100L)
                .status("success")
                .grpcStatusCode("OK")
                .readUnits(5)
                .upsertedCount(10)
                .matchedRecords(3)
                .build();

        assertEquals("query", metadata.getOperationName());
        assertEquals("test-index", metadata.getIndexName());
        assertEquals("test-namespace", metadata.getNamespace());
        assertEquals("test-index-abc.svc.pinecone.io", metadata.getServerAddress());
        assertEquals(150, metadata.getClientDurationMs());
        assertEquals(100L, metadata.getServerDurationMs());
        assertEquals("success", metadata.getStatus());
        assertEquals("OK", metadata.getGrpcStatusCode());
        assertTrue(metadata.isSuccess());
        assertNull(metadata.getErrorType());
        assertEquals(5, metadata.getReadUnits());
        assertEquals(10, metadata.getUpsertedCount());
        assertEquals(3, metadata.getMatchedRecords());
    }

    @Test
    void testNetworkOverheadCalculation() {
        ResponseMetadata metadata = ResponseMetadata.builder()
                .operationName("upsert")
                .clientDurationMs(200)
                .serverDurationMs(150L)
                .build();

        assertEquals(50L, metadata.getNetworkOverheadMs());
    }

    @Test
    void testNetworkOverheadNullWhenServerDurationMissing() {
        ResponseMetadata metadata = ResponseMetadata.builder()
                .operationName("upsert")
                .clientDurationMs(200)
                .serverDurationMs(null)
                .build();

        assertNull(metadata.getNetworkOverheadMs());
    }

    @Test
    void testErrorMetadata() {
        ResponseMetadata metadata = ResponseMetadata.builder()
                .operationName("query")
                .indexName("test-index")
                .clientDurationMs(50)
                .status("error")
                .grpcStatusCode("UNAVAILABLE")
                .errorType("connection")
                .build();

        assertFalse(metadata.isSuccess());
        assertEquals("error", metadata.getStatus());
        assertEquals("UNAVAILABLE", metadata.getGrpcStatusCode());
        assertEquals("connection", metadata.getErrorType());
    }

    @Test
    void testDefaultNamespaceIsEmptyString() {
        ResponseMetadata metadata = ResponseMetadata.builder()
                .operationName("fetch")
                .namespace(null)
                .build();

        assertEquals("", metadata.getNamespace());
    }

    @Test
    void testDefaultStatusIsSuccess() {
        ResponseMetadata metadata = ResponseMetadata.builder()
                .operationName("delete")
                .build();

        assertEquals("success", metadata.getStatus());
        assertEquals("OK", metadata.getGrpcStatusCode());
        assertTrue(metadata.isSuccess());
    }

    @Test
    void testToStringContainsKeyFields() {
        ResponseMetadata metadata = ResponseMetadata.builder()
                .operationName("delete")
                .indexName("my-index")
                .namespace("ns1")
                .clientDurationMs(100)
                .serverDurationMs(80L)
                .status("success")
                .build();

        String str = metadata.toString();
        assertTrue(str.contains("operation=delete"));
        assertTrue(str.contains("index=my-index"));
        assertTrue(str.contains("namespace=ns1"));
        assertTrue(str.contains("clientDurationMs=100"));
        assertTrue(str.contains("serverDurationMs=80"));
        assertTrue(str.contains("networkOverheadMs=20"));
    }

    @Test
    void testToStringWithoutServerDuration() {
        ResponseMetadata metadata = ResponseMetadata.builder()
                .operationName("query")
                .indexName("my-index")
                .clientDurationMs(100)
                .serverDurationMs(null)
                .status("success")
                .build();

        String str = metadata.toString();
        assertTrue(str.contains("operation=query"));
        assertTrue(str.contains("clientDurationMs=100"));
        assertFalse(str.contains("serverDurationMs"));
        assertFalse(str.contains("networkOverheadMs"));
    }

    @Test
    void testToStringWithError() {
        ResponseMetadata metadata = ResponseMetadata.builder()
                .operationName("upsert")
                .indexName("my-index")
                .clientDurationMs(50)
                .status("error")
                .errorType("rate_limit")
                .build();

        String str = metadata.toString();
        assertTrue(str.contains("status=error"));
        assertTrue(str.contains("errorType=rate_limit"));
    }

    @Test
    void testToStringWithoutNamespace() {
        ResponseMetadata metadata = ResponseMetadata.builder()
                .operationName("fetch")
                .indexName("my-index")
                .namespace("")
                .clientDurationMs(100)
                .build();

        String str = metadata.toString();
        assertFalse(str.contains("namespace="));
    }

    @Test
    void testAllOperationTypes() {
        String[] operations = {"upsert", "query", "fetch", "update", "delete"};
        for (String op : operations) {
            ResponseMetadata metadata = ResponseMetadata.builder()
                    .operationName(op)
                    .build();
            assertEquals(op, metadata.getOperationName());
        }
    }

    @Test
    void testAllErrorTypes() {
        String[] errorTypes = {"validation", "connection", "server", "rate_limit", "timeout", "auth", "not_found", "unknown"};
        for (String errorType : errorTypes) {
            ResponseMetadata metadata = ResponseMetadata.builder()
                    .operationName("query")
                    .status("error")
                    .errorType(errorType)
                    .build();
            assertEquals(errorType, metadata.getErrorType());
            assertFalse(metadata.isSuccess());
        }
    }

    @Test
    void testReadUnitsNullByDefault() {
        ResponseMetadata metadata = ResponseMetadata.builder()
                .operationName("query")
                .build();

        assertNull(metadata.getReadUnits());
    }

    @Test
    void testReadUnitsWithValue() {
        ResponseMetadata metadata = ResponseMetadata.builder()
                .operationName("query")
                .readUnits(10)
                .build();

        assertEquals(10, metadata.getReadUnits());
    }

    @Test
    void testUpsertedCountNullByDefault() {
        ResponseMetadata metadata = ResponseMetadata.builder()
                .operationName("upsert")
                .build();

        assertNull(metadata.getUpsertedCount());
    }

    @Test
    void testUpsertedCountWithValue() {
        ResponseMetadata metadata = ResponseMetadata.builder()
                .operationName("upsert")
                .upsertedCount(100)
                .build();

        assertEquals(100, metadata.getUpsertedCount());
    }

    @Test
    void testMatchedRecordsNullByDefault() {
        ResponseMetadata metadata = ResponseMetadata.builder()
                .operationName("update")
                .build();

        assertNull(metadata.getMatchedRecords());
    }

    @Test
    void testMatchedRecordsWithValue() {
        ResponseMetadata metadata = ResponseMetadata.builder()
                .operationName("update")
                .matchedRecords(42)
                .build();

        assertEquals(42, metadata.getMatchedRecords());
    }

    @Test
    void testToStringWithReadUnits() {
        ResponseMetadata metadata = ResponseMetadata.builder()
                .operationName("query")
                .indexName("my-index")
                .clientDurationMs(100)
                .readUnits(5)
                .build();

        String str = metadata.toString();
        assertTrue(str.contains("readUnits=5"));
    }

    @Test
    void testToStringWithUpsertedCount() {
        ResponseMetadata metadata = ResponseMetadata.builder()
                .operationName("upsert")
                .indexName("my-index")
                .clientDurationMs(100)
                .upsertedCount(50)
                .build();

        String str = metadata.toString();
        assertTrue(str.contains("upsertedCount=50"));
    }

    @Test
    void testToStringWithMatchedRecords() {
        ResponseMetadata metadata = ResponseMetadata.builder()
                .operationName("update")
                .indexName("my-index")
                .clientDurationMs(100)
                .matchedRecords(7)
                .build();

        String str = metadata.toString();
        assertTrue(str.contains("matchedRecords=7"));
    }

    @Test
    void testToStringOmitsNullResponseBodyFields() {
        ResponseMetadata metadata = ResponseMetadata.builder()
                .operationName("delete")
                .indexName("my-index")
                .clientDurationMs(100)
                .build();

        String str = metadata.toString();
        assertFalse(str.contains("readUnits"));
        assertFalse(str.contains("upsertedCount"));
        assertFalse(str.contains("matchedRecords"));
    }
}

