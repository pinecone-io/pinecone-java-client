package io.pinecone.helpers;

import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeInternalServerException;
import io.pinecone.exceptions.PineconeNotFoundException;
import org.junit.jupiter.api.Test;
import org.openapitools.db_control.client.model.CollectionModel;
import org.mockito.InOrder;
import org.openapitools.db_control.client.model.IndexModel;
import org.openapitools.db_control.client.model.IndexModelPodBased;
import org.openapitools.db_control.client.model.IndexModelServerless;
import org.openapitools.db_control.client.model.IndexModelSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ResourceCleanupTest {

    @Test
    public void deleteIndexWaitsWhileTheIndexIsStillTerminating() throws Exception {
        Pinecone pinecone = mock(Pinecone.class);
        when(pinecone.describeIndex("idx"))
                .thenReturn(new IndexModel())
                .thenReturn(new IndexModel())
                .thenReturn(new IndexModel())
                .thenThrow(new PineconeNotFoundException("gone"));

        ResourceCleanup.deleteIndex(pinecone, "idx", 5000, 1);

        verify(pinecone).deleteIndex("idx");
        verify(pinecone, times(4)).describeIndex("idx");
    }

    @Test
    public void deleteIndexTreatsAnAlreadyDeletedIndexAsDeleted() throws Exception {
        Pinecone pinecone = mock(Pinecone.class);
        when(pinecone.describeIndex("idx")).thenThrow(new PineconeNotFoundException("gone"));

        ResourceCleanup.deleteIndex(pinecone, "idx", 5000, 1);

        verify(pinecone, never()).deleteIndex("idx");
    }

    @Test
    public void deleteIndexTreatsAnIndexDeletedBeforeTheDeleteCallAsDeleted() throws Exception {
        Pinecone pinecone = mock(Pinecone.class);
        when(pinecone.describeIndex("idx")).thenReturn(new IndexModel());
        doThrow(new PineconeNotFoundException("gone")).when(pinecone).deleteIndex("idx");

        ResourceCleanup.deleteIndex(pinecone, "idx", 5000, 1);

        verify(pinecone, times(1)).describeIndex("idx");
    }

    @Test
    public void deleteIndexDisablesDeletionProtectionOnAServerlessIndexFirst() throws Exception {
        Pinecone pinecone = mock(Pinecone.class);
        IndexModel serverless = new IndexModel().name("idx").deletionProtection("enabled")
                .spec(new IndexModelSpec(new IndexModelServerless()));
        when(pinecone.describeIndex("idx"))
                .thenReturn(serverless)
                .thenThrow(new PineconeNotFoundException("gone"));

        ResourceCleanup.deleteIndex(pinecone, "idx", 5000, 1);

        InOrder inOrder = inOrder(pinecone);
        inOrder.verify(pinecone).configureServerlessIndex("idx", "disabled", null, null);
        inOrder.verify(pinecone).deleteIndex("idx");
        verify(pinecone, never()).configurePodsIndex(anyString(), anyString());
    }

    @Test
    public void deleteIndexDisablesDeletionProtectionOnAPodIndexFirst() throws Exception {
        Pinecone pinecone = mock(Pinecone.class);
        IndexModel pod = new IndexModel().name("idx").deletionProtection("enabled")
                .spec(new IndexModelSpec(new IndexModelPodBased()));
        when(pinecone.describeIndex("idx"))
                .thenReturn(pod)
                .thenThrow(new PineconeNotFoundException("gone"));

        ResourceCleanup.deleteIndex(pinecone, "idx", 5000, 1);

        InOrder inOrder = inOrder(pinecone);
        inOrder.verify(pinecone).configurePodsIndex("idx", "disabled");
        inOrder.verify(pinecone).deleteIndex("idx");
    }

    @Test
    public void deleteIndexLeavesAnUnprotectedIndexUnconfigured() throws Exception {
        Pinecone pinecone = mock(Pinecone.class);
        when(pinecone.describeIndex("idx"))
                .thenReturn(new IndexModel().name("idx").deletionProtection("disabled"))
                .thenThrow(new PineconeNotFoundException("gone"));

        ResourceCleanup.deleteIndex(pinecone, "idx", 5000, 1);

        verify(pinecone).deleteIndex("idx");
        verify(pinecone, never()).configurePodsIndex(anyString(), anyString());
        verify(pinecone, never()).configureServerlessIndex(anyString(), anyString(), any(), any());
    }

    @Test
    public void deleteIndexRethrowsADeleteError() {
        Pinecone pinecone = mock(Pinecone.class);
        when(pinecone.describeIndex("idx")).thenReturn(new IndexModel());
        PineconeInternalServerException error = new PineconeInternalServerException("boom");
        doThrow(error).when(pinecone).deleteIndex("idx");

        assertSame(error, assertThrows(PineconeInternalServerException.class,
                () -> ResourceCleanup.deleteIndex(pinecone, "idx", 5000, 1)));
    }

    @Test
    public void deleteIndexFailsWhenTheIndexOutlivesTheDeadline() {
        Pinecone pinecone = mock(Pinecone.class);
        when(pinecone.describeIndex("idx")).thenReturn(new IndexModel());

        AssertionError thrown = assertThrows(AssertionError.class,
                () -> ResourceCleanup.deleteIndex(pinecone, "idx", 20, 1));

        assertTrue(thrown.getMessage().contains("index idx is deleted"), thrown.getMessage());
    }

    @Test
    public void deleteCollectionWaitsUntilTheCollectionIsGone() throws Exception {
        Pinecone pinecone = mock(Pinecone.class);
        when(pinecone.describeCollection("col"))
                .thenReturn(new CollectionModel())
                .thenThrow(new PineconeNotFoundException("gone"));

        ResourceCleanup.deleteCollection(pinecone, "col", 5000, 1);

        verify(pinecone).deleteCollection("col");
        verify(pinecone, times(2)).describeCollection("col");
    }

    @Test
    public void deleteCollectionTreatsAnAlreadyDeletedCollectionAsDeleted() throws Exception {
        Pinecone pinecone = mock(Pinecone.class);
        doThrow(new PineconeNotFoundException("gone")).when(pinecone).deleteCollection("col");

        ResourceCleanup.deleteCollection(pinecone, "col", 5000, 1);

        verify(pinecone, never()).describeCollection("col");
    }

    @Test
    public void runAllRunsEveryStepAndReturnsTheFailedOnesByName() {
        List<String> ran = Collections.synchronizedList(new ArrayList<>());
        LinkedHashMap<String, ResourceCleanup.Step> steps = new LinkedHashMap<>();
        steps.put("a", () -> ran.add("a"));
        steps.put("b", () -> {
            ran.add("b");
            throw new PineconeInternalServerException("boom");
        });
        steps.put("c", () -> {
            ran.add("c");
            throw new AssertionError("timed out");
        });
        steps.put("d", () -> ran.add("d"));

        List<String> failed = ResourceCleanup.runAll(steps);

        assertEquals(new HashSet<>(Arrays.asList("a", "b", "c", "d")), new HashSet<>(ran));
        assertEquals(Arrays.asList("b", "c"), failed);
    }

    @Test
    public void runAllRunsStepsConcurrently() {
        CountDownLatch bothStarted = new CountDownLatch(2);
        LinkedHashMap<String, ResourceCleanup.Step> steps = new LinkedHashMap<>();
        ResourceCleanup.Step waitForTheOther = () -> {
            bothStarted.countDown();
            if (!bothStarted.await(5, TimeUnit.SECONDS)) {
                throw new AssertionError("the other step never started");
            }
        };
        steps.put("a", waitForTheOther);
        steps.put("b", waitForTheOther);

        assertEquals(Collections.emptyList(), ResourceCleanup.runAll(steps));
    }

    @Test
    public void runAllReturnsNoFailuresWhenEveryStepSucceeds() {
        LinkedHashMap<String, ResourceCleanup.Step> steps = new LinkedHashMap<>();
        steps.put("a", () -> { });

        assertEquals(Collections.emptyList(), ResourceCleanup.runAll(steps));
    }
}
