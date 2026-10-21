package io.pinecone.helpers;

import io.pinecone.clients.Pinecone;
import org.junit.jupiter.api.Test;
import org.openapitools.db_control.client.model.CollectionList;
import org.openapitools.db_control.client.model.CollectionModel;
import org.openapitools.db_control.client.model.IndexList;
import org.openapitools.db_control.client.model.IndexModel;
import org.openapitools.db_control.client.model.IndexModelStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IndexCleanupUtilityTest {
    private static final String RUN_ID = "39659134836";
    private static final long NOW = System.currentTimeMillis();
    private static final long TWO_DAYS_AGO = NOW - TimeUnit.DAYS.toMillis(2);

    private static String testName(String runId, long createdAtMillis, String rest) {
        return IndexCleanupUtility.testResourcePrefix(runId, createdAtMillis) + rest;
    }

    @Test
    void parseArgs_defaults() {
        IndexCleanupUtility.ParsedArgs parsed = IndexCleanupUtility.parseArgs(new String[]{});
        assertEquals(1, parsed.ageThresholdDays);
        assertNull(parsed.runId);
        assertEquals(false, parsed.dryRun);
    }

    @Test
    void parseArgs_runId() {
        IndexCleanupUtility.ParsedArgs parsed = IndexCleanupUtility.parseArgs(new String[]{"--run-id", RUN_ID, "--dry-run"});
        assertEquals(RUN_ID, parsed.runId);
        assertEquals(true, parsed.dryRun);
    }

    @Test
    void parseArgs_runId_missingValue_throws() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> IndexCleanupUtility.parseArgs(new String[]{"--run-id"})
        );
        assertEquals("--run-id requires a value", ex.getMessage());
    }

    @Test
    void parseArgs_runId_blankOrNonNumeric_throws() {
        assertThrows(IllegalArgumentException.class, () -> IndexCleanupUtility.parseArgs(new String[]{"--run-id", ""}));
        assertThrows(IllegalArgumentException.class, () -> IndexCleanupUtility.parseArgs(new String[]{"--run-id", "abc"}));
    }

    @Test
    void testResourcePrefix_unsetRunIdIsTaggedLocal() {
        assertEquals(IndexCleanupUtility.testResourcePrefix(null, NOW), IndexCleanupUtility.testResourcePrefix("  ", NOW));
        assertEquals(true, IndexCleanupUtility.testResourcePrefix(null, NOW).startsWith("cilocal-"));
    }

    @Test
    void parseArgs_dryRun() {
        IndexCleanupUtility.ParsedArgs parsed = IndexCleanupUtility.parseArgs(new String[]{"--dry-run"});
        assertEquals(1, parsed.ageThresholdDays);
        assertEquals(true, parsed.dryRun);
    }

    @Test
    void parseArgs_ageThresholdDays() {
        IndexCleanupUtility.ParsedArgs parsed = IndexCleanupUtility.parseArgs(new String[]{"--age-threshold-days", "0"});
        assertEquals(0, parsed.ageThresholdDays);
        assertEquals(false, parsed.dryRun);
    }

    @Test
    void parseArgs_ageThresholdDays_missingValue_throws() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> IndexCleanupUtility.parseArgs(new String[]{"--age-threshold-days"})
        );
        assertEquals("--age-threshold-days requires a value", ex.getMessage());
    }

    @Test
    void parseArgs_ageThresholdDays_invalidValue_throws() {
        assertThrows(
            IllegalArgumentException.class,
            () -> IndexCleanupUtility.parseArgs(new String[]{"--age-threshold-days", "abc"})
        );
    }

    @Test
    void parseArgs_ageThresholdDays_negative_throws() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> IndexCleanupUtility.parseArgs(new String[]{"--age-threshold-days", "-1"})
        );
        assertEquals("--age-threshold-days must be >= 0", ex.getMessage());
    }

    @Test
    void cleanup_nullIndexesList_doesNotThrowAndProcessesZero() throws Exception {
        Pinecone pinecone = mock(Pinecone.class);

        when(pinecone.listIndexes()).thenReturn(new IndexList());
        when(pinecone.listCollections()).thenReturn(new CollectionList().collections(Collections.emptyList()));

        IndexCleanupUtility utility = new IndexCleanupUtility(pinecone, 1, false);
        IndexCleanupUtility.CleanupResult result = utility.cleanup();

        assertEquals(0, result.getIndexesProcessed());
        assertEquals(0, result.getIndexesDeleted());
        assertEquals(0, result.getIndexesFailed());

        assertEquals(0, result.getCollectionsProcessed());
        assertEquals(0, result.getCollectionsDeleted());
        assertEquals(0, result.getCollectionsFailed());

        verify(pinecone, never()).deleteIndex(anyString());
        verify(pinecone, never()).deleteCollection(anyString());
    }

    @Test
    void cleanup_nullCollectionsListObject_doesNotThrowAndProcessesZero() throws Exception {
        Pinecone pinecone = mock(Pinecone.class);

        when(pinecone.listIndexes()).thenReturn(new IndexList().indexes(Collections.emptyList()));
        when(pinecone.listCollections()).thenReturn(null);

        IndexCleanupUtility utility = new IndexCleanupUtility(pinecone, 1, false);
        IndexCleanupUtility.CleanupResult result = utility.cleanup();

        assertEquals(0, result.getIndexesProcessed());
        assertEquals(0, result.getIndexesDeleted());
        assertEquals(0, result.getIndexesFailed());

        assertEquals(0, result.getCollectionsProcessed());
        assertEquals(0, result.getCollectionsDeleted());
        assertEquals(0, result.getCollectionsFailed());

        verify(pinecone, never()).deleteIndex(anyString());
        verify(pinecone, never()).deleteCollection(anyString());
    }

    @Test
    void cleanup_dryRun_doesNotIncrementDeletedCounts() throws Exception {
        Pinecone pinecone = mock(Pinecone.class);

        IndexList indexes = new IndexList().indexes(Arrays.asList(
            new IndexModel().name(testName(RUN_ID, TWO_DAYS_AGO, "ready-index")).status(new IndexModelStatus().state("Ready").ready(true)),
            new IndexModel().name("terminating-index").status(new IndexModelStatus().state("Terminating").ready(false))
        ));
        when(pinecone.listIndexes()).thenReturn(indexes);

        CollectionList collections = new CollectionList().collections(Arrays.asList(
            new CollectionModel().name(testName(RUN_ID, TWO_DAYS_AGO, "ready-collection")).status("Ready"),
            new CollectionModel().name("terminating-collection").status("Terminating")
        ));
        when(pinecone.listCollections()).thenReturn(collections);

        IndexCleanupUtility utility = new IndexCleanupUtility(pinecone, 1, true);
        IndexCleanupUtility.CleanupResult result = utility.cleanup();

        assertEquals(2, result.getIndexesProcessed());
        assertEquals(0, result.getIndexesDeleted());
        assertEquals(0, result.getIndexesFailed());

        assertEquals(2, result.getCollectionsProcessed());
        assertEquals(0, result.getCollectionsDeleted());
        assertEquals(0, result.getCollectionsFailed());

        verify(pinecone, never()).deleteIndex(anyString());
        verify(pinecone, never()).deleteCollection(anyString());
    }

    @Test
    void cleanup_terminatingResources_doesNotIncrementDeletedCounts() throws Exception {
        Pinecone pinecone = mock(Pinecone.class);

        IndexList indexes = new IndexList().indexes(Collections.singletonList(
            new IndexModel().name("terminating-index").status(new IndexModelStatus().state("Terminating").ready(false))
        ));
        when(pinecone.listIndexes()).thenReturn(indexes);

        CollectionList collections = new CollectionList().collections(Collections.singletonList(
            new CollectionModel().name("terminating-collection").status("Terminating")
        ));
        when(pinecone.listCollections()).thenReturn(collections);

        IndexCleanupUtility utility = new IndexCleanupUtility(pinecone, 1, false);
        IndexCleanupUtility.CleanupResult result = utility.cleanup();

        assertEquals(1, result.getIndexesProcessed());
        assertEquals(0, result.getIndexesDeleted());
        assertEquals(0, result.getIndexesFailed());

        assertEquals(1, result.getCollectionsProcessed());
        assertEquals(0, result.getCollectionsDeleted());
        assertEquals(0, result.getCollectionsFailed());

        verify(pinecone, never()).deleteIndex(anyString());
        verify(pinecone, never()).deleteCollection(anyString());
    }

    @Test
    void cleanup_nonDryRun_incrementsDeletedCountsWhenDeletionIsInitiated() throws Exception {
        Pinecone pinecone = mock(Pinecone.class);

        IndexList indexes = new IndexList().indexes(Collections.singletonList(
            new IndexModel()
                .name(testName(RUN_ID, TWO_DAYS_AGO, "ready-index"))
                .deletionProtection("disabled")
                .status(new IndexModelStatus().state("Ready").ready(true))
        ));
        when(pinecone.listIndexes()).thenReturn(indexes);

        CollectionList collections = new CollectionList().collections(Collections.singletonList(
            new CollectionModel().name(testName(RUN_ID, TWO_DAYS_AGO, "ready-collection")).status("Ready")
        ));
        when(pinecone.listCollections()).thenReturn(collections);

        IndexCleanupUtility utility = new IndexCleanupUtility(pinecone, 1, false);
        IndexCleanupUtility.CleanupResult result = utility.cleanup();

        assertEquals(1, result.getIndexesProcessed());
        assertEquals(1, result.getIndexesDeleted());
        assertEquals(0, result.getIndexesFailed());

        assertEquals(1, result.getCollectionsProcessed());
        assertEquals(1, result.getCollectionsDeleted());
        assertEquals(0, result.getCollectionsFailed());

        verify(pinecone).deleteIndex(testName(RUN_ID, TWO_DAYS_AGO, "ready-index"));
        verify(pinecone).deleteCollection(testName(RUN_ID, TWO_DAYS_AGO, "ready-collection"));
    }

    @Test
    void cleanup_withRunId_deletesOnlyThatRunsResources() throws Exception {
        Pinecone pinecone = mock(Pinecone.class);
        String ownIndex = testName(RUN_ID, NOW, "serverless-index-abcdefgh");
        String otherRunIndex = testName("39658274834", NOW, "serverless-index-abcdefgh");
        String localIndex = testName(null, TWO_DAYS_AGO, "pod-index-abcdefgh");
        String ownCollection = testName(RUN_ID, NOW, "collection-abcdefgh");

        when(pinecone.listIndexes()).thenReturn(new IndexList().indexes(Arrays.asList(
            readyIndex(ownIndex), readyIndex(otherRunIndex), readyIndex(localIndex), readyIndex("customer-index")
        )));
        when(pinecone.listCollections()).thenReturn(new CollectionList().collections(Arrays.asList(
            new CollectionModel().name(ownCollection).status("Ready"),
            new CollectionModel().name("customer-collection").status("Ready")
        )));

        IndexCleanupUtility utility = new IndexCleanupUtility(pinecone, 1, RUN_ID, false);
        IndexCleanupUtility.CleanupResult result = utility.cleanup();

        assertEquals(4, result.getIndexesProcessed());
        assertEquals(1, result.getIndexesDeleted());
        assertEquals(1, result.getCollectionsDeleted());
        verify(pinecone).deleteIndex(ownIndex);
        verify(pinecone, never()).deleteIndex(otherRunIndex);
        verify(pinecone, never()).deleteIndex(localIndex);
        verify(pinecone, never()).deleteIndex("customer-index");
        verify(pinecone).deleteCollection(ownCollection);
        verify(pinecone, never()).deleteCollection("customer-collection");
    }

    @Test
    void cleanup_withoutRunId_deletesOnlyTestResourcesOlderThanThreshold() throws Exception {
        Pinecone pinecone = mock(Pinecone.class);
        String oldIndex = testName("39658274834", TWO_DAYS_AGO, "pod-index-abcdefgh");
        String oldLocalIndex = testName(null, TWO_DAYS_AGO, "pod-index-abcdefgh");
        String inProgressIndex = testName(RUN_ID, NOW - TimeUnit.HOURS.toMillis(1), "serverless-index-abcdefgh");
        String inProgressCollection = testName(RUN_ID, NOW, "collection-abcdefgh");

        when(pinecone.listIndexes()).thenReturn(new IndexList().indexes(Arrays.asList(
            readyIndex(oldIndex), readyIndex(oldLocalIndex), readyIndex(inProgressIndex), readyIndex("customer-index")
        )));
        when(pinecone.listCollections()).thenReturn(new CollectionList().collections(Arrays.asList(
            new CollectionModel().name(inProgressCollection).status("Ready"),
            new CollectionModel().name("customer-collection").status("Ready")
        )));

        IndexCleanupUtility utility = new IndexCleanupUtility(pinecone, 1, false);
        IndexCleanupUtility.CleanupResult result = utility.cleanup();

        assertEquals(2, result.getIndexesDeleted());
        assertEquals(0, result.getCollectionsDeleted());
        verify(pinecone).deleteIndex(oldIndex);
        verify(pinecone).deleteIndex(oldLocalIndex);
        verify(pinecone, never()).deleteIndex(inProgressIndex);
        verify(pinecone, never()).deleteIndex("customer-index");
        verify(pinecone, never()).deleteCollection(anyString());
    }

    @Test
    void cleanup_zeroAgeThreshold_deletesAllTestResourcesButNothingElse() throws Exception {
        Pinecone pinecone = mock(Pinecone.class);
        String inProgressIndex = testName(RUN_ID, NOW, "serverless-index-abcdefgh");

        when(pinecone.listIndexes()).thenReturn(new IndexList().indexes(Arrays.asList(
            readyIndex(inProgressIndex), readyIndex("customer-index")
        )));
        when(pinecone.listCollections()).thenReturn(new CollectionList().collections(Collections.emptyList()));

        IndexCleanupUtility utility = new IndexCleanupUtility(pinecone, 0, false);
        IndexCleanupUtility.CleanupResult result = utility.cleanup();

        assertEquals(1, result.getIndexesDeleted());
        verify(pinecone).deleteIndex(inProgressIndex);
        verify(pinecone, never()).deleteIndex("customer-index");
    }

    private static IndexModel readyIndex(String name) {
        return new IndexModel()
            .name(name)
            .deletionProtection("disabled")
            .status(new IndexModelStatus().state("Ready").ready(true));
    }
}

