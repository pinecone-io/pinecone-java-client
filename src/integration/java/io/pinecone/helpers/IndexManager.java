package io.pinecone.helpers;

import io.pinecone.*;
import io.pinecone.model.CreateIndexRequest;
import io.pinecone.model.IndexMeta;

import java.io.IOException;
import java.util.List;

public class IndexManager {
    public PineconeConnection createIndexIfNotExistsDataPlane(int dimension) throws IOException, InterruptedException {
        PineconeClientConfig config = new PineconeClientConfig()
                .withApiKey(System.getenv("PINECONE_API_KEY"))
                .withEnvironment(System.getenv("PINECONE_ENVIRONMENT"));
        PineconeIndexOperationClient controlPlaneClient = new PineconeIndexOperationClient(config);
        List<String> indexList = controlPlaneClient.listIndexes();

        String indexName = checkIfIndexExists(indexList, dimension, controlPlaneClient);
        if(indexName.isEmpty()) indexName = createNewIndex(dimension, controlPlaneClient);

        PineconeClient dataPlaneClient = new PineconeClient(config);
        IndexMeta indexMeta = controlPlaneClient.describeIndex(indexName);
        String host = indexMeta.getStatus().getHost();

        return dataPlaneClient.connect(
                new PineconeConnectionConfig()
                        .withConnectionUrl("https://" + host));
    }

    public String createIndexIfNotExistsControlPlane(PineconeClientConfig config, int dimension) throws IOException, InterruptedException {
        PineconeIndexOperationClient controlPlaneClient = new PineconeIndexOperationClient(config);
        List<String> indexList = controlPlaneClient.listIndexes();
        String indexName = checkIfIndexExists(indexList, dimension, controlPlaneClient);

        return (indexName.isEmpty()) ? createNewIndex(dimension, controlPlaneClient) : indexName;
    }

    private static String checkIfIndexExists(List<String> indexList, int dimension, PineconeIndexOperationClient controlPlaneClient)
            throws IOException, InterruptedException {
        int i = 0;
        while (i < indexList.size()) {
            IndexMeta indexMeta = isIndexReady(indexList.get(i), controlPlaneClient);
            if (indexMeta.getDatabase().getDimension() == dimension
                    && indexMeta.getDatabase().getPodType().equals("p1.x1")) {
                return indexList.get(i);
            }
            i++;
        }
        return "";
    }

    private static String createNewIndex(int dimension, PineconeIndexOperationClient controlPlaneClient) throws IOException {
        String indexName = RandomStringBuilder.build("index-name", 8);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                .withIndexName(indexName)
                .withDimension(dimension)
                .withMetric("euclidean")
                .withPodType("p1.x1");
        controlPlaneClient.createIndex(createIndexRequest);

        return indexName;
    }

    public static IndexMeta isIndexReady(String indexName, PineconeIndexOperationClient indexOperationClient)
            throws IOException, InterruptedException {
        IndexMeta indexMeta;
        while (true) {
            indexMeta = indexOperationClient.describeIndex(indexName);
            if (indexMeta.getStatus().getState().equalsIgnoreCase("ready")) {
                break;
            }
            Thread.sleep(1000);
        }

        return indexMeta;
    }
}
