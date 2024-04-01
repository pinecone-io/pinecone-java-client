package io.pinecone;

import io.pinecone.helpers.IndexManagerSingleton;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AfterAllTestsListener implements TestExecutionListener {
    private static final Logger logger = LoggerFactory.getLogger(AfterAllTestsListener.class);

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        logger.info("Integration tests starting. Running setup...");

    }
    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        logger.info("Integration tests completed. Running cleanup...");
        IndexManagerSingleton.getInstance().cleanupResources();
    }
}