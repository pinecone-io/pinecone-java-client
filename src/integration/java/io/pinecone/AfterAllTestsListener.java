package io.pinecone;

import io.pinecone.helpers.IndexManagerSingleton;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

public class AfterAllTestsListener implements TestExecutionListener {

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        System.out.println("Integration tests starting. Running setup code...");

    }
    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        System.out.println("Integration tests completed. Running cleanup code...");
        IndexManagerSingleton.getInstance().cleanupResources();
    }
}