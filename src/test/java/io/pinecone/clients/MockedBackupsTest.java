package io.pinecone.clients;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.pinecone.helpers.MockPinecone;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openapitools.db_control.client.ApiException;
import org.openapitools.db_control.client.model.BackupList;
import org.openapitools.db_control.client.model.BackupModel;
import org.openapitools.db_control.client.model.CreateIndexFromBackupResponse;
import org.openapitools.db_control.client.model.RestoreJobList;
import org.openapitools.db_control.client.model.RestoreJobModel;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Drives the backup and restore-job control-plane methods against {@link MockPinecone}: the method, path, query string,
 * API version header and request body each one sends, and how each response body is parsed.
 */
public class MockedBackupsTest {
    @RegisterExtension
    final MockPinecone mock = new MockPinecone();

    private static final String BACKUP = "{"
            + "\"backup_id\":\"bk-1\","
            + "\"source_index_name\":\"src-index\","
            + "\"source_index_id\":\"idx-1\","
            + "\"name\":\"nightly\","
            + "\"description\":\"before migration\","
            + "\"status\":\"Ready\","
            + "\"cloud\":\"aws\","
            + "\"region\":\"us-east-1\","
            + "\"dimension\":3,"
            + "\"metric\":\"cosine\","
            + "\"record_count\":120,"
            + "\"namespace_count\":2,"
            + "\"size_bytes\":4096,"
            + "\"tags\":{\"env\":\"test\"},"
            + "\"created_at\":\"2025-01-02T03:04:05Z\""
            + "}";

    private static final String RESTORE_JOB = "{"
            + "\"restore_job_id\":\"rj-1\","
            + "\"backup_id\":\"bk-1\","
            + "\"target_index_name\":\"restored\","
            + "\"target_index_id\":\"idx-2\","
            + "\"status\":\"Completed\","
            + "\"created_at\":\"2025-01-02T03:04:05Z\","
            + "\"completed_at\":\"2025-01-02T03:10:00Z\","
            + "\"percent_complete\":100.0"
            + "}";

    private RecordedRequest onlyRequest() {
        List<RecordedRequest> requests = mock.restRequests();
        assertEquals(1, requests.size());
        RecordedRequest request = requests.get(0);
        assertEquals("2025-10", request.getHeader("X-Pinecone-Api-Version"));
        assertEquals(MockPinecone.API_KEY, request.getHeader("Api-Key"));
        return request;
    }

    private static JsonObject body(RecordedRequest request) {
        return JsonParser.parseString(request.getBody().readUtf8()).getAsJsonObject();
    }

    private static void assertBackup(BackupModel backup) {
        assertEquals("bk-1", backup.getBackupId());
        assertEquals("src-index", backup.getSourceIndexName());
        assertEquals("idx-1", backup.getSourceIndexId());
        assertEquals("nightly", backup.getName());
        assertEquals("Ready", backup.getStatus());
        assertEquals("aws", backup.getCloud());
        assertEquals("us-east-1", backup.getRegion());
        assertEquals(3, backup.getDimension());
        assertEquals(120, backup.getRecordCount());
        assertEquals(2, backup.getNamespaceCount());
        assertEquals(4096, backup.getSizeBytes());
        assertEquals(Collections.singletonMap("env", "test"), backup.getTags());
    }

    @Test
    public void createBackupPostsNameAndDescription() throws ApiException {
        mock.stub("POST", "/indexes/src-index/backups", 201, BACKUP);

        BackupModel backup = mock.client().createBackup("src-index", "nightly", "before migration");

        assertBackup(backup);
        RecordedRequest request = onlyRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/indexes/src-index/backups", request.getPath());
        JsonObject sent = body(request);
        assertEquals("nightly", sent.get("name").getAsString());
        assertEquals("before migration", sent.get("description").getAsString());
    }

    @Test
    public void createBackupOmitsUnsetOptionalFields() throws ApiException {
        mock.stub("POST", "/indexes/src-index/backups", 201, BACKUP);

        mock.client().createBackup("src-index", null, null);

        JsonObject sent = body(onlyRequest());
        assertFalse(sent.has("name"), sent.toString());
        assertFalse(sent.has("description"), sent.toString());
    }

    @Test
    public void listIndexBackupsDefaultsToLimitTen() throws ApiException {
        mock.stub("GET", "/indexes/src-index/backups", 200,
                "{\"data\":[" + BACKUP + "],\"pagination\":{\"next\":\"tok-2\"}}");

        BackupList list = mock.client().listIndexBackups("src-index");

        assertEquals(1, list.getData().size());
        assertBackup(list.getData().get(0));
        assertEquals("tok-2", list.getPagination().getNext());
        RecordedRequest request = onlyRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/indexes/src-index/backups?limit=10", request.getPath());
    }

    @Test
    public void listIndexBackupsPassesLimitAndPaginationToken() throws ApiException {
        mock.stub("GET", "/indexes/src-index/backups", 200, "{\"data\":[]}");

        BackupList list = mock.client().listIndexBackups("src-index", 3, "tok-2");

        assertTrue(list.getData().isEmpty());
        assertNull(list.getPagination());
        assertEquals("/indexes/src-index/backups?limit=3&paginationToken=tok-2", onlyRequest().getPath());
    }

    @Test
    public void listProjectBackupsSendsNoQueryByDefault() throws ApiException {
        mock.stub("GET", "/backups", 200, "{\"data\":[" + BACKUP + "]}");

        BackupList list = mock.client().listProjectBackups();

        assertBackup(list.getData().get(0));
        assertEquals("/backups", onlyRequest().getPath());
    }

    @Test
    public void listProjectBackupsPassesLimitAndPaginationToken() throws ApiException {
        mock.stub("GET", "/backups", 200, "{\"data\":[]}");

        mock.client().listProjectBackups(5, "tok-3");

        assertEquals("/backups?limit=5&paginationToken=tok-3", onlyRequest().getPath());
    }

    @Test
    public void describeBackupParsesEveryField() throws ApiException {
        mock.stub("GET", "/backups/bk-1", 200, BACKUP);

        BackupModel backup = mock.client().describeBackup("bk-1");

        assertBackup(backup);
        assertEquals("before migration", backup.getDescription());
        assertEquals("cosine", backup.getMetric());
        assertEquals("2025-01-02T03:04:05Z", backup.getCreatedAt());
        RecordedRequest request = onlyRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/backups/bk-1", request.getPath());
    }

    @Test
    public void deleteBackupSendsDelete() throws ApiException {
        mock.stub("DELETE", "/backups/bk-1", 202, "");

        mock.client().deleteBackup("bk-1");

        RecordedRequest request = onlyRequest();
        assertEquals("DELETE", request.getMethod());
        assertEquals("/backups/bk-1", request.getPath());
    }

    @Test
    public void backupErrorsSurfaceAsApiExceptionWithStatusAndBody() {
        String error = "{\"error\":{\"code\":\"NOT_FOUND\",\"message\":\"backup bk-missing not found\"},\"status\":404}";
        mock.stub("GET", "/backups/bk-missing", 404, error);

        ApiException thrown = assertThrows(ApiException.class, () -> mock.client().describeBackup("bk-missing"));

        assertEquals(404, thrown.getCode());
        assertEquals(error, thrown.getResponseBody());
    }

    @Test
    public void createIndexFromBackupSendsNameAndParsesResponse() throws ApiException {
        mock.stub("POST", "/backups/bk-1/create-index", 202, "{\"restore_job_id\":\"rj-1\",\"index_id\":\"idx-2\"}");

        CreateIndexFromBackupResponse response = mock.client().createIndexFromBackup("bk-1", "restored");

        assertEquals("rj-1", response.getRestoreJobId());
        assertEquals("idx-2", response.getIndexId());
        RecordedRequest request = onlyRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/backups/bk-1/create-index", request.getPath());
        JsonObject sent = body(request);
        assertEquals("restored", sent.get("name").getAsString());
        assertEquals("disabled", sent.get("deletion_protection").getAsString());
        assertTrue(!sent.has("tags") || sent.getAsJsonObject("tags").size() == 0, sent.toString());
    }

    @Test
    public void createIndexFromBackupWithTagsAndDeletionProtection() throws ApiException {
        mock.stub("POST", "/backups/bk-1/create-index", 202, "{\"restore_job_id\":\"rj-1\",\"index_id\":\"idx-2\"}");

        mock.client().createIndexFromBackup("bk-1", "restored", Collections.singletonMap("team", "search"), "enabled");

        JsonObject sent = body(onlyRequest());
        assertEquals("restored", sent.get("name").getAsString());
        assertEquals("search", sent.getAsJsonObject("tags").get("team").getAsString());
        assertEquals("enabled", sent.get("deletion_protection").getAsString());
    }

    @Test
    public void createIndexFromBackupDefaultsNullDeletionProtectionToDisabled() throws ApiException {
        mock.stub("POST", "/backups/bk-1/create-index", 202, "{\"restore_job_id\":\"rj-1\",\"index_id\":\"idx-2\"}");

        mock.client().createIndexFromBackup("bk-1", "restored", null, null);

        JsonObject sent = body(onlyRequest());
        assertEquals("disabled", sent.get("deletion_protection").getAsString());
        assertFalse(sent.has("tags"), sent.toString());
    }

    @Test
    public void describeRestoreJobParsesEveryField() throws ApiException {
        mock.stub("GET", "/restore-jobs/rj-1", 200, RESTORE_JOB);

        RestoreJobModel job = mock.client().describeRestoreJob("rj-1");

        assertEquals("rj-1", job.getRestoreJobId());
        assertEquals("bk-1", job.getBackupId());
        assertEquals("restored", job.getTargetIndexName());
        assertEquals("idx-2", job.getTargetIndexId());
        assertEquals("Completed", job.getStatus());
        assertEquals(OffsetDateTime.parse("2025-01-02T03:04:05Z"), job.getCreatedAt());
        assertEquals(OffsetDateTime.parse("2025-01-02T03:10:00Z"), job.getCompletedAt());
        assertEquals(100.0f, job.getPercentComplete(), 1e-6f);
        assertEquals("/restore-jobs/rj-1", onlyRequest().getPath());
    }

    @Test
    public void listRestoreJobsOverloadsSendTheirQuery() throws ApiException {
        mock.stub("GET", "/restore-jobs", 200, "{\"data\":[" + RESTORE_JOB + "],\"pagination\":{\"next\":\"tok-4\"}}");
        Pinecone client = mock.client();

        RestoreJobList byLimit = client.listRestoreJobs(2);
        client.listRestoreJobs("tok-4");
        client.listRestoreJobs(7, "tok-5");

        assertEquals("rj-1", byLimit.getData().get(0).getRestoreJobId());
        assertEquals("tok-4", byLimit.getPagination().getNext());
        List<RecordedRequest> requests = mock.restRequests();
        assertEquals(3, requests.size());
        assertEquals("/restore-jobs?limit=2", requests.get(0).getPath());
        assertEquals("/restore-jobs?limit=10&paginationToken=tok-4", requests.get(1).getPath());
        assertEquals("/restore-jobs?limit=7&paginationToken=tok-5", requests.get(2).getPath());
    }
}
