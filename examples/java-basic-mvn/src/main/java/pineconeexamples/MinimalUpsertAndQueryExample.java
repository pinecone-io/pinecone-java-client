package pineconeexamples;

import io.pinecone.PineconeClient;
import io.pinecone.PineconeClientConfig;
import io.pinecone.PineconeConnection;
import io.pinecone.PineconeConnectionConfig;
import io.pinecone.PineconeException;
import io.pinecone.QueryRequest;
import io.pinecone.QueryResponse;
import io.pinecone.UpsertRequest;
import io.pinecone.UpsertResponse;

import java.util.Arrays;

public class MinimalUpsertAndQueryExample {

    public static void main(String[] args) {
        String apiKey = System.getProperty("pinecone.apikey", "example-api-key");
        String serviceName = System.getProperty("pinecone.service.name", "example-service-name");
        String target = System.getProperty("pinecone.service.url_authority",
                "example-service-url-authority"); // see PineconeConnectionConfig#serviceAuthority for how to obtain

        System.out.println("Starting application...");

        PineconeClientConfig configuration = new PineconeClientConfig()
                .withApiKey(apiKey);

        PineconeClient pineconeClient = new PineconeClient(configuration);

        PineconeConnectionConfig connectionConfig = new PineconeConnectionConfig()
                .withServiceAuthority(target)
                .withServiceName(serviceName);

        try (PineconeConnection connection = pineconeClient.connect(connectionConfig)) {

            UpsertRequest upsertRequest = pineconeClient.upsertRequest()
                    .ids(Arrays.asList("v1", "v2"))
                    .data(new float[][]{{1F, 2F}, {3F, 4F}});

            System.out.println("Sending upsert request:");
            System.out.println(upsertRequest);

            UpsertResponse upsertResponse = connection.send(upsertRequest);

            System.out.println("Got upsert response:");
            System.out.println(upsertResponse);
            QueryRequest queryRequest = pineconeClient.queryRequest()
                    .topK(1)
                    .data(new float[][]{{1F, 2F}});

            System.out.println("Sending query request:");
            System.out.println(queryRequest);

            QueryResponse queryResponse = connection.send(queryRequest);

            System.out.println("Got query response:");
            System.out.println(queryResponse);
        }
        catch(PineconeException e) {
            e.printStackTrace();
        }
    }
}
