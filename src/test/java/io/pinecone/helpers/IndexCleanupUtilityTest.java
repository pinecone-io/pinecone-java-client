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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IndexCleanupUtilityTest {

    @Test
    void cleanup_dryRun_doesNotIncrementDeletedCounts() throws Exception {
        Pinecone pinecone = mock(Pinecone.class);

        IndexList indexes = new IndexList().indexes(Arrays.asList(
            new IndexModel().name("ready-index").status(new IndexModelStatus().state("Ready").ready(true)),
            new IndexModel().name("terminating-index").status(new IndexModelStatus().state("Terminating").ready(false))
        ));
        when(pinecone.listIndexes()).thenReturn(indexes);

        CollectionList collections = new CollectionList().collections(Arrays.asList(
            new CollectionModel().name("ready-collection").status("Ready"),
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
                .name("ready-index")
                .deletionProtection("disabled")
                .status(new IndexModelStatus().state("Ready").ready(true))
        ));
        when(pinecone.listIndexes()).thenReturn(indexes);

        CollectionList collections = new CollectionList().collections(Collections.singletonList(
            new CollectionModel().name("ready-collection").status("Ready")
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

        verify(pinecone).deleteIndex("ready-index");
        verify(pinecone).deleteCollection("ready-collection");
    }
}

