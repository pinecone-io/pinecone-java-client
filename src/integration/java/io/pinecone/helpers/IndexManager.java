package io.pinecone.helpers;

import io.pinecone.*;
import io.pinecone.model.CreateIndexRequest;
import io.pinecone.model.IndexMeta;

import java.io.IOException;
import java.util.List;

import static io.pinecone.helpers.AssertRetry.assertWithRetry;

public class IndexManager {
    private static PineconeClientConfig config;

    public static PineconeConnection createIndexIfNotExistsDataPlane(int dimension) throws IOException, InterruptedException {
        config = new PineconeClientConfig()
                .withApiKey(System.getenv("PINECONE_API_KEY"))
                .withEnvironment(System.getenv("PINECONE_ENVIRONMENT"));
        PineconeIndexOperationClient controlPlaneClient = new PineconeIndexOperationClient(config);
        List<String> indexList = controlPlaneClient.listIndexes();

        String indexName = findIndexWithDimensionAndPodType(indexList, dimension, controlPlaneClient);
        if(indexName.isEmpty()) indexName = createNewIndex(dimension, controlPlaneClient);

        // Do not proceed until the newly created index is ready
        isIndexReady(indexName, controlPlaneClient);
        PineconeClient dataPlaneClient = new PineconeClient(config);
        IndexMeta indexMeta = controlPlaneClient.describeIndex(indexName);
        String host = indexMeta.getStatus().getHost();

        return dataPlaneClient.connect(
                new PineconeConnectionConfig()
                        .withConnectionUrl("https://" + host));
    }

    public static String createIndexIfNotExistsControlPlane(PineconeClientConfig config, int dimension) throws IOException, InterruptedException {
        PineconeIndexOperationClient controlPlaneClient = new PineconeIndexOperationClient(config);
        List<String> indexList = controlPlaneClient.listIndexes();
        String indexName = findIndexWithDimensionAndPodType(indexList, dimension, controlPlaneClient);

        return (indexName.isEmpty()) ? createNewIndex(dimension, controlPlaneClient) : indexName;
    }

    private static String findIndexWithDimensionAndPodType(List<String> indexList, int dimension, PineconeIndexOperationClient controlPlaneClient)
            throws IOException, InterruptedException {
        int i = 0;
        while (i < indexList.size()) {
            IndexMeta indexMeta = isIndexReady(indexList.get(i), controlPlaneClient);
            if (indexMeta.getDatabase().getDimension() == dimension
                    && (indexMeta.getDatabase().getPodType().equals("p1.x1") || config.getEnvironment().equals("gcp-starter"))) {
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
        final IndexMeta[] indexMeta = new IndexMeta[1];
        assertWithRetry(() -> {
            indexMeta[0] = indexOperationClient.describeIndex(indexName);
            assert (indexMeta[0].getStatus().getState().equalsIgnoreCase("ready"));
        });

        return indexMeta[0];
    }
}
