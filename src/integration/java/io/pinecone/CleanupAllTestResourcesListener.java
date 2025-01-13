package io.pinecone;

import io.pinecone.helpers.TestResourcesManager;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

public class CleanupAllTestResourcesListener implements TestExecutionListener {
    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        try {
            TestResourcesManager.getInstance().cleanupResources();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}