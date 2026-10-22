package io.pinecone.clients;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.pinecone.exceptions.PineconeForbiddenException;
import io.pinecone.exceptions.PineconeInternalServerException;
import io.pinecone.exceptions.PineconeNotFoundException;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.helpers.MockPinecone;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openapitools.db_control.client.ApiException;
import org.openapitools.db_control.client.model.CollectionList;
import org.openapitools.db_control.client.model.CollectionModel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Offline coverage of the collection methods on {@link Pinecone}: the request each one sends, how the
 * {@link CollectionModel} responses are parsed, argument validation, and error mapping.
 */
public class MockedCollectionsTest {
    @RegisterExtension
    final MockPinecone mock = new MockPinecone();

    private static String collectionJson(String name, String status) {
        return "{"
                + "\"name\":\"" + name + "\","
                + "\"size\":10000000000,"
                + "\"status\":\"" + status + "\","
                + "\"dimension\":1536,"
                + "\"vector_count\":120000,"
                + "\"environment\":\"us-east1-gcp\""
                + "}";
    }

    private static void assertControlPlaneRequest(RecordedRequest request, String method, String path) {
        assertEquals(method, request.getMethod());
        assertEquals(path, request.getPath());
        assertEquals("2025-10", request.getHeader("X-Pinecone-Api-Version"));
        assertEquals(MockPinecone.API_KEY, request.getHeader("Api-Key"));
    }

    @Test
    public void createCollectionSendsNameAndSourceAndParsesResponse() {
        mock.stub("POST", "/collections", 201, collectionJson("my-coll", "Initializing"));

        CollectionModel collection = mock.client().createCollection("my-coll", "src-index");

        List<RecordedRequest> requests = mock.restRequests();
        assertEquals(1, requests.size());
        assertControlPlaneRequest(requests.get(0), "POST", "/collections");
        JsonObject body = JsonParser.parseString(requests.get(0).getBody().readUtf8()).getAsJsonObject();
        assertEquals(2, body.size());
        assertEquals("my-coll", body.get("name").getAsString());
        assertEquals("src-index", body.get("source").getAsString());

        assertEquals("my-coll", collection.getName());
        assertEquals("Initializing", collection.getStatus());
        assertEquals(Long.valueOf(10000000000L), collection.getSize());
        assertEquals(Integer.valueOf(1536), collection.getDimension());
        assertEquals(Integer.valueOf(120000), collection.getVectorCount());
        assertEquals("us-east1-gcp", collection.getEnvironment());
    }

    @Test
    public void createCollectionRejectsInvalidArgumentsBeforeAnyRequest() {
        Pinecone client = mock.client();

        assertEquals("collectionName cannot be null or empty", assertThrows(PineconeValidationException.class,
                () -> client.createCollection(null, "src-index")).getMessage());
        assertEquals("collectionName cannot be null or empty", assertThrows(PineconeValidationException.class,
                () -> client.createCollection("", "src-index")).getMessage());
        assertEquals("sourceIndex cannot be null or empty", assertThrows(PineconeValidationException.class,
                () -> client.createCollection("my-coll", null)).getMessage());
        assertEquals("sourceIndex cannot be null or empty", assertThrows(PineconeValidationException.class,
                () -> client.createCollection("my-coll", "")).getMessage());

        assertEquals(0, mock.restRequests().size());
    }

    @Test
    public void describeCollectionSendsGetAndParsesResponse() {
        mock.stub("GET", "/collections/my-coll", 200, collectionJson("my-coll", "Ready"));

        CollectionModel collection = mock.client().describeCollection("my-coll");

        List<RecordedRequest> requests = mock.restRequests();
        assertEquals(1, requests.size());
        assertControlPlaneRequest(requests.get(0), "GET", "/collections/my-coll");
        assertEquals("my-coll", collection.getName());
        assertEquals("Ready", collection.getStatus());
        assertEquals(Integer.valueOf(120000), collection.getVectorCount());
    }

    @Test
    public void listCollectionsParsesEveryCollection() {
        mock.stub("GET", "/collections", 200, "{\"collections\":["
                + collectionJson("coll-a", "Ready") + ","
                + "{\"name\":\"coll-b\",\"status\":\"Initializing\",\"environment\":\"us-east1-gcp\"}"
                + "]}");

        CollectionList list = mock.client().listCollections();

        List<RecordedRequest> requests = mock.restRequests();
        assertEquals(1, requests.size());
        assertControlPlaneRequest(requests.get(0), "GET", "/collections");
        assertEquals(2, list.getCollections().size());
        CollectionModel first = list.getCollections().get(0);
        assertEquals("coll-a", first.getName());
        assertEquals("Ready", first.getStatus());
        assertEquals(Integer.valueOf(1536), first.getDimension());
        CollectionModel second = list.getCollections().get(1);
        assertEquals("coll-b", second.getName());
        assertEquals("Initializing", second.getStatus());
        assertNull(second.getSize());
        assertNull(second.getDimension());
        assertNull(second.getVectorCount());
    }

    @Test
    public void deleteCollectionSendsDelete() {
        mock.stub("DELETE", "/collections/my-coll", 202, "");

        mock.client().deleteCollection("my-coll");

        List<RecordedRequest> requests = mock.restRequests();
        assertEquals(1, requests.size());
        assertControlPlaneRequest(requests.get(0), "DELETE", "/collections/my-coll");
    }

    @Test
    public void errorsAreMappedToPineconeExceptions() {
        String notFound = "{\"error\":{\"code\":\"NOT_FOUND\",\"message\":\"Collection missing not found\"},\"status\":404}";
        mock.stub("GET", "/collections/missing", 404, notFound);
        mock.stub("DELETE", "/collections/missing", 404, notFound);
        String forbidden = "{\"error\":{\"code\":\"FORBIDDEN\",\"message\":\"Collections are not supported in serverless projects\"},\"status\":403}";
        mock.stub("POST", "/collections", 403, forbidden);
        String internal = "{\"error\":{\"code\":\"UNKNOWN\",\"message\":\"Internal error\"},\"status\":500}";
        mock.stub("GET", "/collections", 500, internal);
        Pinecone client = mock.client();

        PineconeNotFoundException describe = assertThrows(PineconeNotFoundException.class,
                () -> client.describeCollection("missing"));
        assertEquals(notFound, describe.getMessage());
        assertEquals(404, assertInstanceOf(ApiException.class, describe.getCause()).getCode());

        PineconeNotFoundException delete = assertThrows(PineconeNotFoundException.class,
                () -> client.deleteCollection("missing"));
        assertEquals(404, assertInstanceOf(ApiException.class, delete.getCause()).getCode());

        PineconeForbiddenException create = assertThrows(PineconeForbiddenException.class,
                () -> client.createCollection("my-coll", "serverless-index"));
        assertEquals(forbidden, create.getMessage());
        assertEquals(403, assertInstanceOf(ApiException.class, create.getCause()).getCode());

        PineconeInternalServerException list = assertThrows(PineconeInternalServerException.class,
                client::listCollections);
        assertEquals(internal, list.getMessage());
        assertEquals(500, assertInstanceOf(ApiException.class, list.getCause()).getCode());
    }
}
