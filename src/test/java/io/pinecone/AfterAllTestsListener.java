package io.pinecone;

import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

public class AfterAllTestsListener implements TestExecutionListener {

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        System.out.println("Unit tests starting. Running setup code...");

    }
    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        System.out.println("Unit tests ending. Running cleanup code...");
    }
}