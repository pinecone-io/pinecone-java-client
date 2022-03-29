package pineconeexamples;

import com.google.common.primitives.Floats;
import io.pinecone.*;
import io.pinecone.proto.*;


public class MinimalUpsertAndQueryExample {
    public static class Args {
        public String apiKey = System.getProperty("pinecone.apikey", "example-api-key");
        String indexName = System.getProperty("pinecone.indexName", "example-index-name");
        String environment = System.getProperty("pinecone.environment",
                "example-environment");
        String projectName = System.getProperty("pinecone.projectName", "example-project-name");
        String namespace = "test-ns";
        int topK = 1;
    }


    public static void main(String[] cliArgs) {
        System.out.println("Starting application...");

        Args args = new Args();

        PineconeClientConfig configuration = new PineconeClientConfig()
                .withApiKey(args.apiKey)
                .withEnvironment(args.environment)
                .withProjectName(args.projectName);

        PineconeClient pineconeClient = new PineconeClient(configuration);


        PineconeConnectionConfig connectionConfig = new PineconeConnectionConfig()
                .withIndexName(args.indexName);

        try (PineconeConnection connection = pineconeClient.connect(connectionConfig)) {
            Vector v1 = Vector.newBuilder()
                    .setId("v1")
                    .addAllValues(Floats.asList(1F, 3F, 5F))
                    .build();

            Vector v2 = Vector.newBuilder()
                    .setId("v2")
                    .addAllValues(Floats.asList(5F, 3F, 1F))
                    .build();

            UpsertRequest upsertRequest = UpsertRequest.newBuilder()
                    .addVectors(v1)
                    .addVectors(v2)
                    .setNamespace(args.namespace)
                    .build();

            System.out.println("Sending upsert request:");
            System.out.println(upsertRequest);

            UpsertResponse upsertResponse = connection.getBlockingStub().upsert(upsertRequest);

            System.out.println("Got upsert response:");
            System.out.println(upsertResponse);


            QueryVector queryVector = QueryVector
                    .newBuilder()
                    .addAllValues(Floats.asList(1F, 2F, 2F))
                    .setTopK(args.topK)
                    .setNamespace(args.namespace)
                    .build();

            QueryRequest queryRequest = QueryRequest
                    .newBuilder()
                    .addQueries(queryVector)
                    .setTopK(args.topK)
                    .build();

            System.out.println("Sending query request:");
            System.out.println(queryRequest);

            QueryResponse queryResponse = connection.getBlockingStub().query(queryRequest);

            System.out.println("Got query response:");
            System.out.println(queryResponse);
        } catch (PineconeException e) {
            e.printStackTrace();
        }
    }
}
