package io.pinecone.helpers;

import io.pinecone.*;
import io.pinecone.model.CreateIndexRequest;
import io.pinecone.model.IndexMeta;

import java.io.IOException;
import java.util.List;

public class GetOrCreateIndex {
    public static PineconeConnection getOrCreateIndex(int dimension) throws IOException {
        PineconeClientConfig config = new PineconeClientConfig()
                .withApiKey(System.getenv("PINECONE_API_KEY"))
                .withEnvironment(System.getenv("PINECONE_ENVIRONMENT"));

        PineconeIndexOperationClient controlPlaneClient = new PineconeIndexOperationClient(config);

        List<String> indexList = controlPlaneClient.listIndexes();
        String requiredParamsIndexName;
        if(!indexList.isEmpty()) {
            requiredParamsIndexName = indexList.get(0);
        }
        else {
            requiredParamsIndexName = RandomStringBuilder.build("index-name", 8);

            CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                    .withIndexName(requiredParamsIndexName)
                    .withDimension(dimension)
                    .withMetric("euclidean");
            controlPlaneClient.createIndex(createIndexRequest);
        }
        PineconeClient dataPlaneClient = new PineconeClient(config);
        IndexMeta indexMeta = controlPlaneClient.describeIndex(requiredParamsIndexName);
        String host = indexMeta.getStatus().getHost();
        return dataPlaneClient.connect(
                new PineconeConnectionConfig()
                        .withConnectionUrl("https://" + host));
    }
}
