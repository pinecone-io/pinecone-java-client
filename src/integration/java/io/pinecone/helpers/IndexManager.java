package io.pinecone.helpers;

import io.pinecone.*;
import org.openapitools.client.model.*;

import java.io.IOException;
import java.util.List;

import static io.pinecone.helpers.AssertRetry.assertWithRetry;

public class IndexManager {
    private static PineconeClientConfig config;

    public static PineconeConnection createIndexIfNotExistsDataPlane(int dimension, String indexType) throws IOException, InterruptedException {
        config = new PineconeClientConfig().withApiKey(System.getenv("PINECONE_API_KEY")).withEnvironment(System.getenv("PINECONE_ENVIRONMENT"));
        PineconeIndexOperationClient controlPlaneClient = new PineconeIndexOperationClient(config);
        IndexList indexList = controlPlaneClient.listIndexes();

        String indexName = findIndexWithDimensionAndType(indexList, dimension, controlPlaneClient, indexType);
        if (indexName.isEmpty()) indexName = createNewIndex(controlPlaneClient, indexType, dimension);

        // Do not proceed until the newly created index is ready
        isIndexReady(indexName, controlPlaneClient);
        PineconeClient dataPlaneClient = new PineconeClient(config);
        String host = controlPlaneClient.describeIndex(indexName).getHost();

        return dataPlaneClient.connect(
                new PineconeConnectionConfig()
                        .withConnectionUrl("https://" + host));
    }

    public static String createIndexIfNotExistsControlPlane(PineconeIndexOperationClient controlPlaneClient, int dimension, String indexType) throws IOException, InterruptedException {
        IndexList indexList = controlPlaneClient.listIndexes();
        String indexName = findIndexWithDimensionAndType(indexList, dimension, controlPlaneClient, indexType);

        return (indexName.isEmpty()) ? createNewIndex(controlPlaneClient, indexType, dimension) : indexName;
    }

    private static String findIndexWithDimensionAndType(IndexList indexList, int dimension, PineconeIndexOperationClient controlPlaneClient, String indexType)
            throws InterruptedException {
        int i = 0;
        List<IndexModel> indexModels = indexList.getIndexes();
        while (i < indexModels.size()) {
            IndexModel indexModel = isIndexReady(indexModels.get(i).getName(), controlPlaneClient);
            // ToDo: add pod type support
            if (indexModel.getDimension() == dimension
                    && ((indexType.equalsIgnoreCase(IndexModelSpec.SERIALIZED_NAME_POD) && indexModel.getSpec().getPod().getPodType().equalsIgnoreCase("p1.x1"))
                    || (indexType.equalsIgnoreCase(IndexModelSpec.SERIALIZED_NAME_SERVERLESS)))) {
                return indexModel.getName();
            }
            i++;
        }
        return "";
    }

    private static String createNewIndex(PineconeIndexOperationClient controlPlaneClient, String indexType, int dimension) throws IOException {
        String indexName = RandomStringBuilder.build("index-name", 8);
        String environment = System.getenv("PINECONE_ENVIRONMENT");
        CreateIndexRequestSpec createIndexRequestSpec;

        if (indexType.equalsIgnoreCase(IndexModelSpec.SERIALIZED_NAME_POD)) {
            CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().environment(environment).podType("p1.x1");
            createIndexRequestSpec = new CreateIndexRequestSpec().pod(podSpec);
        } else {
            ServerlessSpec serverlessSpec = new ServerlessSpec().cloud(ServerlessSpec.CloudEnum.AWS).region(environment);
            createIndexRequestSpec = new CreateIndexRequestSpec().serverless(serverlessSpec);
        }

        CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                .name(indexName)
                .dimension(dimension)
                .metric(IndexMetric.EUCLIDEAN)
                .spec(createIndexRequestSpec);
        controlPlaneClient.createIndex(createIndexRequest);

        return indexName;
    }

    public static IndexModel isIndexReady(String indexName, PineconeIndexOperationClient controlPlaneClient)
            throws InterruptedException {
        final IndexModel[] indexModels = new IndexModel[1];
        assertWithRetry(() -> {
            indexModels[0] = controlPlaneClient.describeIndex(indexName);
            assert (indexModels[0].getStatus().getReady());
        }, 1);

        return indexModels[0];
    }
}
