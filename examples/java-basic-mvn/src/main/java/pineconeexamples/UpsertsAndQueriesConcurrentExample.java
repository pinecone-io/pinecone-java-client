package pineconeexamples;

import io.pinecone.PineconeClient;
import io.pinecone.PineconeClientConfig;
import io.pinecone.PineconeConnection;
import io.pinecone.PineconeConnectionConfig;
import io.pinecone.PineconeException;
import io.pinecone.PineconeResponse;
import io.pinecone.QueryResponse;
import io.pinecone.UpsertResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This example upserts a specified number of vectors in batches (with exponential
 * backoff) then concurrently submits a specified number of queries.
 */
public class UpsertsAndQueriesConcurrentExample {

    public static class Args {
        public String apiKey = System.getProperty("pinecone.apikey", "example-api-key");
        public String serviceName = System.getProperty("pinecone.service.name", "example-service-name");
        public String target = System.getProperty("pinecone.service.url_authority",
                "example-service-url-authority");

        public int vectorDim = 128;
        public int vectorCount = 1024 * 8;

        public int queryCount = 1024 * 8;
        public int queryBatchSize = 1;
        public int maxConcurrentQueryRequests = 256;

        public int upsertBatchSize = 100;
        public int maxUpsertAttempts = 10;
        public int upsertBackoffIntervalStartMs = 100;

        public double progressUpdatePercentStep = 0.1;
    }

    private static final Logger logger = LoggerFactory.getLogger(UpsertsAndQueriesConcurrentExample.class);

    public static void main(String[] cliArgs) throws InterruptedException {
        logger.info("Starting application... (process {})", ManagementFactory.getRuntimeMXBean().getName());
        Args args = new Args();

        PineconeClientConfig configuration = new PineconeClientConfig()
                .withApiKey(args.apiKey);

        PineconeClient pineconeClient = new PineconeClient(configuration);

        PineconeConnectionConfig connectionConfig = new PineconeConnectionConfig()
                .withServiceAuthority(args.target)
                .withServiceName(args.serviceName);

        try (PineconeConnection conn = pineconeClient.connect(connectionConfig)) {
            //
            // upsert phase
            //
            int upsertCount = (int) Math.ceil(((double) args.vectorCount) / args.upsertBatchSize);
            logger.info("submitting {} upsert requests with {} vectors per request, {} floats" +
                            " per vector, and up to {} retries with exponential backoff...",
                    upsertCount,
                    args.upsertBatchSize,
                    args.vectorDim,
                    args.maxUpsertAttempts);
            long upsertStartTime = System.nanoTime();
            long upsertSuccessCount = 0;
            try {
                int updateStepSize = (int) (args.vectorCount / args.upsertBatchSize * args.progressUpdatePercentStep); // for progress reporting
                int nextProgressUpdate = updateStepSize;
                for (int requestNum = 0; requestNum < upsertCount; requestNum++) {
                    final String vectorIdPrefix = "v" + requestNum + ".";
                    int backoffIntervalMs = args.upsertBackoffIntervalStartMs;
                    for (int retriesLeft = args.maxUpsertAttempts; retriesLeft > 0; retriesLeft--) {
                        float[][] data = new float[args.upsertBatchSize][args.vectorDim];
                        List<String> ids = new ArrayList<>();
                        ThreadLocalRandom rand = ThreadLocalRandom.current();
                        for (int i = 0; i < args.upsertBatchSize; i++) {
                            for (int j = 0; j < args.vectorDim; j++) {
                                data[i][j] = rand.nextFloat();
                            }
                            ids.add(vectorIdPrefix + i);
                        }

                        try {
                            UpsertResponse response = conn.send(pineconeClient.upsertRequest()
                                    .data(data)
                                    .ids(ids));
                            upsertSuccessCount++;
                            break;
                        } catch (PineconeException e) {
                            if (retriesLeft == 1) {
                                logger.error("giving up on upsert after all {} retries failed",
                                        args.maxUpsertAttempts, e);
                            } else { // retry with exponential backoff
                                logger.debug("error occurred; retrying upsert after {} ms",
                                        backoffIntervalMs, e);
                                TimeUnit.MILLISECONDS.sleep(backoffIntervalMs);
                                backoffIntervalMs *= 2;
                            }
                        }
                    }
                    if (requestNum == nextProgressUpdate) { // give progress update
                        double completionPercent = ((double) requestNum) / upsertCount * 100;
                        logger.info("finished with upsert request {} of {} ({}%)",
                                requestNum, upsertCount, completionPercent);
                        nextProgressUpdate += updateStepSize;
                    }
                }
            } finally {
                long duration = (System.nanoTime() - upsertStartTime);
                logger.info("upsert success rate: {}%; {} succeeded of total {}",
                        ((double) upsertSuccessCount) / upsertCount * 100,
                        upsertSuccessCount,
                        upsertCount);
                logger.info("upsert phase duration: {} sec",
                        TimeUnit.NANOSECONDS.toSeconds(duration));
            }

            //
            // query phase
            //
            logger.info("submitting {} query requests (of {} floats each) with {} " +
                            "queries per request and up to {} concurrent requests...",
                    args.queryCount,
                    args.vectorDim,
                    args.queryBatchSize,
                    args.maxConcurrentQueryRequests);

            ExecutorService executor = Executors.newCachedThreadPool();
            CompletionService<PineconeResponse> completionService =
                    new ExecutorCompletionService<>(executor);

            AtomicInteger querySuccessCount = new AtomicInteger();
            long queryStartTime = System.nanoTime();
            try {
                int updateStepSize = (int) (args.queryCount * args.progressUpdatePercentStep); // for progress reporting
                int nextUpdate = updateStepSize;
                // send requests and blocking-retrieve responses w/ requests ahead of responses by <maxPoolSize-1>
                int requestNum = 0;
                int responseNum = 0;
                while (responseNum < args.queryCount) {
                    if (requestNum >= args.maxConcurrentQueryRequests) { // retrieve next upsert result, blocking if necessary
                        responseNum++;
                        try {
                            completionService.take().get();
                        } catch (ExecutionException e) {
                            logger.error("query error", e);
                        }
                        if (responseNum == nextUpdate) { // give progress update
                            double completionPercent = ((double) responseNum) / args.queryCount * 100;
                            logger.info("finished with query request {} of {} ({}%)",
                                    responseNum, args.queryCount, completionPercent);
                            nextUpdate += updateStepSize;
                        }
                    }
                    if (requestNum < args.queryCount) { // submit another request
                        requestNum++;

                        completionService.submit(() -> {
                            float[][] queries = new float[args.queryBatchSize][args.vectorDim];
                            ThreadLocalRandom rand = ThreadLocalRandom.current();
                            for (int i = 0; i < args.queryBatchSize; i++) {
                                for (int j = 0; j < args.vectorDim; j++) {
                                    queries[i][j] = rand.nextFloat();
                                }
                            }
                            QueryResponse response = conn.send(pineconeClient.queryRequest()
                                    .topK(1)
                                    .data(queries));
                            querySuccessCount.incrementAndGet();
                            return response;
                        });
                    }
                }
            } finally {
                long duration = (System.nanoTime() - queryStartTime);
                logger.info("query success rate: {}%; {} succeeded of total {}",
                        ((double) querySuccessCount.get()) / args.queryCount * 100,
                        querySuccessCount.get(),
                        args.queryCount);
                logger.info("query phase duration: {} sec",
                        TimeUnit.NANOSECONDS.toSeconds(duration));

                executor.shutdown();
            }
        }
    }
}
