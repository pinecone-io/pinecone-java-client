package io.pinecone.clients;

import io.pinecone.configs.PineconeConfig;
import io.pinecone.configs.PineconeConnection;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openapitools.db_data.client.ApiException;
import org.openapitools.db_data.client.api.BulkOperationsApi;
import org.openapitools.db_data.client.model.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ImportsTest {

    private BulkOperationsApi bulkOperationsApiMock;
    private AsyncIndex asyncIndex;

    @BeforeEach
    public void setUp() {
        PineconeConnection connectionMock = Mockito.mock(PineconeConnection.class);
        PineconeConfig configMock = Mockito.mock(PineconeConfig.class);

        bulkOperationsApiMock = mock(BulkOperationsApi.class);
        OkHttpClient httpClientMock = mock(OkHttpClient.class);

        when(configMock.getCustomOkHttpClient()).thenReturn(httpClientMock);
        when(configMock.getApiKey()).thenReturn("fake-api-key");
        when(configMock.getUserAgent()).thenReturn("fake-user-agent");
        when(configMock.isTLSEnabled()).thenReturn(true);
        when(configMock.getHost()).thenReturn("localhost");

        asyncIndex = new AsyncIndex(configMock, connectionMock, "test-index");
        asyncIndex.bulkOperations = bulkOperationsApiMock;  // Replace with mock
    }

    @Test
    public void testStartImportMinimal() throws ApiException {
        StartImportResponse mockResponse = new StartImportResponse();
        mockResponse.setId("1");

        when(bulkOperationsApiMock.startBulkImport(any(StartImportRequest.class)))
                .thenReturn(mockResponse);

        StartImportResponse response = asyncIndex.startImport("s3://path/to/file.parquet", null, null);

        assertEquals("1", response.getId());
    }

    @Test
    public void testStartImportWithIntegrationId() throws ApiException {
        StartImportResponse mockResponse = new StartImportResponse();
        mockResponse.setId("1");

        when(bulkOperationsApiMock.startBulkImport(any(StartImportRequest.class)))
                .thenReturn(mockResponse);

        StartImportResponse response = asyncIndex.startImport("s3://path/to/file.parquet", "integration-123", null);

        assertEquals("1", response.getId());

        ArgumentCaptor<StartImportRequest> requestCaptor = ArgumentCaptor.forClass(StartImportRequest.class);
        verify(bulkOperationsApiMock).startBulkImport(requestCaptor.capture());
        StartImportRequest capturedRequest = requestCaptor.getValue();

        assertEquals("s3://path/to/file.parquet", capturedRequest.getUri());
        assertEquals("integration-123", capturedRequest.getIntegrationId());
    }

    @Test
    public void testStartImportWithErrorMode() throws ApiException {
        StartImportResponse mockResponse = new StartImportResponse();
        mockResponse.setId("1");

        when(bulkOperationsApiMock.startBulkImport(any(StartImportRequest.class)))
                .thenReturn(mockResponse);

        StartImportResponse response = asyncIndex.startImport("s3://path/to/file.parquet", null, ImportErrorMode.OnErrorEnum.CONTINUE);

        assertEquals("1", response.getId());

        ArgumentCaptor<StartImportRequest> requestCaptor = ArgumentCaptor.forClass(StartImportRequest.class);
        verify(bulkOperationsApiMock).startBulkImport(requestCaptor.capture());
        StartImportRequest capturedRequest = requestCaptor.getValue();

        assertEquals(ImportErrorMode.OnErrorEnum.CONTINUE, capturedRequest.getErrorMode().getOnError());
    }

    @Test
    public void testStartImportWithInvalidUri() throws ApiException {
        ApiException exception = new ApiException(400, "Invalid URI");
        when(bulkOperationsApiMock.startBulkImport(any(StartImportRequest.class)))
                .thenThrow(exception);

        ApiException thrownException = assertThrows(ApiException.class, () -> {
            asyncIndex.startImport("invalid-uri", null, null);
        });

        assertEquals(400, thrownException.getCode());
        assert(thrownException.getLocalizedMessage().contains("Invalid URI"));
    }

    @Test
    public void testDescribeImport() throws ApiException {
        String uri = "s3://path/to/file.parquet";
        String errorMode = "CONTINUE";
        OffsetDateTime createdAt = OffsetDateTime.parse("2024-10-24T00:00:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        OffsetDateTime finishedAt = OffsetDateTime.parse("2024-10-24T05:02:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        float percentComplete = 43.2f;

        ImportModel mockResponse = new ImportModel();
        mockResponse.setId("1");
        mockResponse.setRecordsImported(1000L);
        mockResponse.setUri(uri);
        mockResponse.setStatus(ImportModel.StatusEnum.INPROGRESS);
        mockResponse.setError(errorMode);
        mockResponse.setCreatedAt(createdAt);
        mockResponse.setFinishedAt(finishedAt);
        mockResponse.setPercentComplete(43.2f);

        when(bulkOperationsApiMock.describeBulkImport("1")).thenReturn(mockResponse);

        ImportModel response = asyncIndex.describeImport("1");

        assertEquals("1", response.getId());
        assertEquals(1000, response.getRecordsImported());
        assertEquals(uri, response.getUri());
        assertEquals("InProgress", response.getStatus().getValue());
        assertEquals(errorMode, response.getError());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(finishedAt, response.getFinishedAt());
        assertEquals(percentComplete, response.getPercentComplete());

        // Verify that the describeBulkImport method was called once
        verify(bulkOperationsApiMock, times(1)).describeBulkImport("1");
    }

    @Test
    void testListImports() throws ApiException {
        ListImportsResponse mockResponse = new ListImportsResponse();
        mockResponse.setData(Collections.singletonList(new ImportModel()));
        mockResponse.setPagination(new Pagination());

        when(bulkOperationsApiMock.listBulkImports(anyInt(), anyString())).thenReturn(mockResponse);

        ListImportsResponse response = asyncIndex.listImport(10, "next-token");

        assertNotNull(response);
        assertEquals(1, response.getData().size());
        assertNotNull(response.getPagination());
        verify(bulkOperationsApiMock, times(1))
                .listBulkImports(10, "next-token");
    }
}
