package pineconeexamples;

import com.google.common.primitives.Floats;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.UpsertResponse;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinimalUpsertAndQueryExample {
    private static final Logger logger = LoggerFactory.getLogger(MinimalUpsertAndQueryExample.class);

    public static class Args {
        public String apiKey = System.getProperty("pinecone.apikey", "example-api-key");
        String indexName = System.getProperty("pinecone.indexName", "example-index-name");
        String namespace = "test-ns";
        int topK = 1;
    }

    public static void main(String[] cliArgs) {
        System.out.println("Starting application...");

        Args args = new Args();

        Pinecone pinecone = new Pinecone.Builder(args.apiKey).build();

        try {
            Index index = pinecone.getIndexConnection(args.indexName);

            logger.info("Sending upsert request.");

            UpsertResponse upsertResponse = index.upsert("v1", Floats.asList(1F, 3F, 5F), args.namespace);

            logger.info("Got upsert response:" + upsertResponse);

            logger.info("Sending query request");
            QueryResponseWithUnsignedIndices queryResponse = index.queryByVectorId(args.topK, "v1", args.namespace, true, false);
            logger.info("Got query response:" + queryResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
