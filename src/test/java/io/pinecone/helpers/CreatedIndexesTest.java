package io.pinecone.helpers;

import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeInternalServerException;
import io.pinecone.exceptions.PineconeNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class CreatedIndexesTest {

    @Test
    public void registerReturnsTheName() {
        CreatedIndexes created = new CreatedIndexes(mock(Pinecone.class));

        assertEquals("idx", created.register("idx"));
    }

    @Test
    public void deleteAllDeletesEveryRegisteredIndexOnce() {
        Pinecone pinecone = mock(Pinecone.class);
        when(pinecone.describeIndex("a")).thenThrow(new PineconeNotFoundException("gone"));
        when(pinecone.describeIndex("b")).thenThrow(new PineconeNotFoundException("gone"));
        CreatedIndexes created = new CreatedIndexes(pinecone);
        created.register("a");
        created.register("b");
        created.register("a");

        created.deleteAll();
        created.deleteAll();

        verify(pinecone, times(1)).describeIndex("a");
        verify(pinecone, times(1)).describeIndex("b");
    }

    @Test
    public void deleteAllTriesEveryIndexAndNamesTheOnesThatFailed() {
        Pinecone pinecone = mock(Pinecone.class);
        when(pinecone.describeIndex("ok")).thenThrow(new PineconeNotFoundException("gone"));
        when(pinecone.describeIndex("bad")).thenThrow(new PineconeInternalServerException("boom"));
        CreatedIndexes created = new CreatedIndexes(pinecone);
        created.register("bad");
        created.register("ok");

        AssertionError thrown = assertThrows(AssertionError.class, created::deleteAll);

        assertEquals("1 of 2 cleanup steps failed: [delete index bad]", thrown.getMessage());
        verify(pinecone).describeIndex("ok");
    }

    @Test
    public void deleteAllWithNothingRegisteredMakesNoCalls() {
        Pinecone pinecone = mock(Pinecone.class);

        new CreatedIndexes(pinecone).deleteAll();

        verifyNoInteractions(pinecone);
    }
}
