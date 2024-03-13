package io.pinecone.integration.dataPlane.serverless;

import io.grpc.StatusRuntimeException;
import io.pinecone.clients.AsyncIndex;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.DescribeIndexStatsResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.IndexModelSpec;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.pinecone.helpers.BuildUpsertRequest.generateVectorValuesByDimension;
import static io.pinecone.helpers.IndexManager.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class QueryErrorTest {

    private static Index index;
    private static AsyncIndex asyncIndex;
    private static final int dimension = 3;

    @BeforeAll
    public static void setUp() throws InterruptedException {
        String apiKey = System.getenv("PINECONE_API_KEY");
        String indexType = IndexModelSpec.SERIALIZED_NAME_SERVERLESS;
        Pinecone pinecone = new Pinecone(apiKey);

        String indexName = findIndexWithDimensionAndType(pinecone, dimension, indexType);
        if (indexName.isEmpty()) indexName = createNewIndex(pinecone, dimension, indexType);
        index = pinecone.createIndexConnection(indexName);
        asyncIndex = pinecone.createAsyncIndexConnection(indexName);
    }

    @Test
    public void queryWithIncorrectVectorDimensionSync() {
        String namespace = RandomStringBuilder.build("ns", 8);

        DescribeIndexStatsResponse describeIndexStatsResponse1 = index.describeIndexStats(null);
        assertEquals(describeIndexStatsResponse1.getDimension(), dimension);

        StringBuilder exceptionMessage = new StringBuilder();
        // Query with incorrect dimensions
        try {
            List<Float> vector = Arrays.asList(100F);
            index.query(5, vector, null, null, null, namespace, null, true, true);
        } catch (StatusRuntimeException statusRuntimeException) {
            exceptionMessage.append(statusRuntimeException.getTrailers().toString());
        } finally {
            assert (exceptionMessage.toString().contains("grpc-status=3"));
            assert (exceptionMessage.toString().contains("grpc-message=Query vector dimension 1 does not match the dimension of the index 3"));
        }
    }

    @Test
    public void QueryWithNullSparseIndicesNotNullSparseValuesSyncTest() {
        String id = RandomStringBuilder.build(3);
        StringBuilder exceptionMessage = new StringBuilder();

        try {
            index.update(id,
                    generateVectorValuesByDimension(dimension),
                    null,
                    null,
                    null,
                    generateVectorValuesByDimension(dimension));
        } catch (PineconeValidationException validationException) {
            exceptionMessage.append(validationException.getLocalizedMessage());
        } finally {
            assertEquals(exceptionMessage.toString(), "Invalid upsert request. Please ensure that both sparse indices and values are present.");
        }
    }

    @Test
    public void queryWithIncorrectVectorDimensionFuture() throws ExecutionException, InterruptedException {
        String namespace = RandomStringBuilder.build("ns", 8);
        DescribeIndexStatsResponse describeIndexStatsResponse = asyncIndex.describeIndexStats(null).get();
        assertEquals(describeIndexStatsResponse.getDimension(), dimension);

        StringBuilder exceptionMessage = new StringBuilder();
        // Query with incorrect dimensions
        try {
            List<Float> vector = Arrays.asList(100F);
            asyncIndex.query(5, vector, null, null, null, namespace, null, true, true).get();
        } catch (ExecutionException executionException) {
            exceptionMessage.append(executionException.getLocalizedMessage());
        } finally {
            assert (exceptionMessage.toString().contains("grpc-status=3"));
            assert (exceptionMessage.toString().contains("grpc-message=Query vector dimension 1 does not match the dimension of the index 3"));
        }
    }

    @Test
    public void QueryWithNullSparseIndicesNotNullSparseValuesFutureTest() throws ExecutionException, InterruptedException {
        String id = RandomStringBuilder.build(3);
        StringBuilder exceptionMessage = new StringBuilder();
        try {
            asyncIndex.update(id,
                    generateVectorValuesByDimension(dimension),
                    null,
                    null,
                    null,
                    generateVectorValuesByDimension(dimension)).get();
        } catch (PineconeValidationException validationException) {
            exceptionMessage.append(validationException.getLocalizedMessage());
        } finally {
            assertEquals(exceptionMessage.toString(), "Invalid upsert request. Please ensure that both sparse indices and values are present.");
        }
    }
}
