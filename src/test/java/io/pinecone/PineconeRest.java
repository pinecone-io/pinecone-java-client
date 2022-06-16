package io.pinecone;
import io.pinecone.rest.ApiClient;
import io.pinecone.rest.ApiException;
import io.pinecone.rest.Configuration;
import io.pinecone.rest.auth.*;
import io.pinecone.rest.model.*;
import io.pinecone.rest.api.IndexOperationsApi;

public class PineconeRest {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://controller.us-west1-gcp.pinecone.io");

        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("a02452e1-75e1-4237-92ff-c595cd76c825");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        IndexOperationsApi apiInstance = new IndexOperationsApi(defaultClient);
        CreateRequest createRequest = new CreateRequest(); // CreateRequest |
        createRequest.setDimension(128);
        createRequest.setName("test4");
        try {
            String result = apiInstance.createIndex(createRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling IndexOperationsApi#createIndex");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
