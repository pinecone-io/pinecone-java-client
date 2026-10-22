package io.pinecone.helpers;

import io.pinecone.clients.Pinecone;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Tracks the indexes a test class creates so an @AfterAll method can delete them whether the tests passed or not.
 * Register a name before creating the index, so an index whose create call fails part way is still deleted.
 */
public class CreatedIndexes {
    private final Pinecone pinecone;
    private final Set<String> names = new LinkedHashSet<>();

    public CreatedIndexes(Pinecone pinecone) {
        this.pinecone = pinecone;
    }

    public synchronized String register(String indexName) {
        names.add(indexName);
        return indexName;
    }

    /**
     * Deletes every registered index concurrently, turning deletion protection off first. Throws an AssertionError
     * naming every index that could not be deleted.
     */
    public void deleteAll() {
        List<String> toDelete;
        synchronized (this) {
            toDelete = new ArrayList<>(names);
            names.clear();
        }
        LinkedHashMap<String, ResourceCleanup.Step> steps = new LinkedHashMap<>();
        for (String name : toDelete) {
            steps.put("delete index " + name, () -> ResourceCleanup.deleteIndex(pinecone, name));
        }
        List<String> failed = ResourceCleanup.runAll(steps);
        if (!failed.isEmpty()) {
            throw new AssertionError(failed.size() + " of " + steps.size() + " cleanup steps failed: " + failed);
        }
    }
}
