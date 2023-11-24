package io.pinecone.helpers;

import io.pinecone.*;
import io.pinecone.model.CreateIndexRequest;
import io.pinecone.model.IndexMeta;

import java.io.IOException;
import java.util.List;

public class IndexManager {
    public PineconeConnection createIndexIfNotExists(int dimension) throws IOException, InterruptedException {
        boolean createNewIndex = false;
        String indexName = "";
        PineconeClientConfig config = new PineconeClientConfig()
                .withApiKey(System.getenv("PINECONE_API_KEY"))
                .withEnvironment(System.getenv("PINECONE_ENVIRONMENT"));
        PineconeIndexOperationClient controlPlaneClient = new PineconeIndexOperationClient(config);
        List<String> indexList = controlPlaneClient.listIndexes();

        if (!indexList.isEmpty()) {
            indexName = indexList.get(0);
            IndexMeta indexMeta = isIndexReady(indexName, controlPlaneClient);
            if (indexMeta.getDatabase().getDimension() != dimension) {
                createNewIndex = true;
            }
        }

        if (createNewIndex) {
            indexName = RandomStringBuilder.build("index-name", 8);
            CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                    .withIndexName(indexName)
                    .withDimension(dimension)
                    .withMetric("euclidean");
            controlPlaneClient.createIndex(createIndexRequest);
        }

        PineconeClient dataPlaneClient = new PineconeClient(config);
        IndexMeta indexMeta = controlPlaneClient.describeIndex(indexName);
        String host = indexMeta.getStatus().getHost();

        return dataPlaneClient.connect(
                new PineconeConnectionConfig()
                        .withConnectionUrl("https://" + host));
    }

    public static IndexMeta isIndexReady(String indexName, PineconeIndexOperationClient indexOperationClient)
            throws IOException, InterruptedException {
        IndexMeta indexMeta;
        while (true) {
            indexMeta = indexOperationClient.describeIndex(indexName);
            if (indexMeta.getStatus().getState().equalsIgnoreCase("ready")) {
                break;
            }

            Thread.sleep(500);
        }
        return indexMeta;
    }
}
