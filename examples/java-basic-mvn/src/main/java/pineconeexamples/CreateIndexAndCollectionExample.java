package pineconeexamples;

import java.util.List;

import io.pinecone.PineconeIndexOperationClient;
import io.pinecone.PineconeClientConfig;
import io.pinecone.exceptions.PineconeException;
import org.openapitools.client.ApiClient;
import org.openapitools.client.model.IndexList;
import org.openapitools.client.model.CollectionList;
import org.openapitools.client.ApiException;

public class CreateIndexAndCollectionExample {

    private static PineconeIndexOperationClient pc;
    public static class Args {
        String apiKey = System.getProperty("pinecone.apiKey", "example-api-key");
        String namespace = "";
    }

    public static void main(String[] cliArgs) throws ApiException {
        System.out.println("Starting CreateIndexAndCollectionExample application...");
        Args args = new Args();

        pc = new PineconeIndexOperationClient(args.apiKey);

        try {
            IndexList indexList = pc.listIndexes();
            System.out.println("Index list: " + indexList);

            CollectionList collections = pc.listCollections();
            System.out.println("Collection list: " + collections);
        }
        catch (PineconeException e) {
            e.printStackTrace();
        }
    }
}