/*
 * Pinecone Inference API
 * Pinecone is a vector database that makes it easy to search and retrieve billions of high-dimensional vectors.
 *
 * The version of the OpenAPI document: 2025-01
 * Contact: support@pinecone.io
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package org.openapitools.inference.client.api;

import org.openapitools.inference.client.ApiCallback;
import org.openapitools.inference.client.ApiClient;
import org.openapitools.inference.client.ApiException;
import org.openapitools.inference.client.ApiResponse;
import org.openapitools.inference.client.Configuration;
import org.openapitools.inference.client.Pair;
import org.openapitools.inference.client.ProgressRequestBody;
import org.openapitools.inference.client.ProgressResponseBody;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;


import org.openapitools.inference.client.model.EmbedRequest;
import org.openapitools.inference.client.model.EmbeddingsList;
import org.openapitools.inference.client.model.ErrorResponse;
import org.openapitools.inference.client.model.RerankRequest;
import org.openapitools.inference.client.model.RerankResult;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InferenceApi {
    private ApiClient localVarApiClient;
    private int localHostIndex;
    private String localCustomBaseUrl;

    public InferenceApi() {
        this(Configuration.getDefaultApiClient());
    }

    public InferenceApi(ApiClient apiClient) {
        this.localVarApiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return localVarApiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.localVarApiClient = apiClient;
    }

    public int getHostIndex() {
        return localHostIndex;
    }

    public void setHostIndex(int hostIndex) {
        this.localHostIndex = hostIndex;
    }

    public String getCustomBaseUrl() {
        return localCustomBaseUrl;
    }

    public void setCustomBaseUrl(String customBaseUrl) {
        this.localCustomBaseUrl = customBaseUrl;
    }

    /**
     * Build call for embed
     * @param embedRequest Generate embeddings for inputs. (optional)
     * @param _callback Callback for upload/download progress
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request. The request body included invalid request parameters. </td><td>  -  </td></tr>
        <tr><td> 401 </td><td> Unauthorized. Possible causes: Invalid API key. </td><td>  -  </td></tr>
        <tr><td> 500 </td><td> Internal server error. </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call embedCall(EmbedRequest embedRequest, final ApiCallback _callback) throws ApiException {
        String basePath = null;
        // Operation Servers
        String[] localBasePaths = new String[] {  };

        // Determine Base Path to Use
        if (localCustomBaseUrl != null){
            basePath = localCustomBaseUrl;
        } else if ( localBasePaths.length > 0 ) {
            basePath = localBasePaths[localHostIndex];
        } else {
            basePath = null;
        }

        Object localVarPostBody = embedRequest;

        // create path and map variables
        String localVarPath = "/embed";

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, String> localVarCookieParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
            "application/json"
        };
        final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) {
            localVarHeaderParams.put("Accept", localVarAccept);
        }

        final String[] localVarContentTypes = {
            "application/json"
        };
        final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
        if (localVarContentType != null) {
            localVarHeaderParams.put("Content-Type", localVarContentType);
        }

        String[] localVarAuthNames = new String[] { "ApiKeyAuth" };
        return localVarApiClient.buildCall(basePath, localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
    }

    @SuppressWarnings("rawtypes")
    private okhttp3.Call embedValidateBeforeCall(EmbedRequest embedRequest, final ApiCallback _callback) throws ApiException {
        return embedCall(embedRequest, _callback);

    }

    /**
     * Embed data
     * Generate embeddings for input data.  For guidance and examples, see [Generate embeddings](https://docs.pinecone.io/guides/inference/generate-embeddings).
     * @param embedRequest Generate embeddings for inputs. (optional)
     * @return EmbeddingsList
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request. The request body included invalid request parameters. </td><td>  -  </td></tr>
        <tr><td> 401 </td><td> Unauthorized. Possible causes: Invalid API key. </td><td>  -  </td></tr>
        <tr><td> 500 </td><td> Internal server error. </td><td>  -  </td></tr>
     </table>
     */
    public EmbeddingsList embed(EmbedRequest embedRequest) throws ApiException {
        ApiResponse<EmbeddingsList> localVarResp = embedWithHttpInfo(embedRequest);
        return localVarResp.getData();
    }

    /**
     * Embed data
     * Generate embeddings for input data.  For guidance and examples, see [Generate embeddings](https://docs.pinecone.io/guides/inference/generate-embeddings).
     * @param embedRequest Generate embeddings for inputs. (optional)
     * @return ApiResponse&lt;EmbeddingsList&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request. The request body included invalid request parameters. </td><td>  -  </td></tr>
        <tr><td> 401 </td><td> Unauthorized. Possible causes: Invalid API key. </td><td>  -  </td></tr>
        <tr><td> 500 </td><td> Internal server error. </td><td>  -  </td></tr>
     </table>
     */
    public ApiResponse<EmbeddingsList> embedWithHttpInfo(EmbedRequest embedRequest) throws ApiException {
        okhttp3.Call localVarCall = embedValidateBeforeCall(embedRequest, null);
        Type localVarReturnType = new TypeToken<EmbeddingsList>(){}.getType();
        return localVarApiClient.execute(localVarCall, localVarReturnType);
    }

    /**
     * Embed data (asynchronously)
     * Generate embeddings for input data.  For guidance and examples, see [Generate embeddings](https://docs.pinecone.io/guides/inference/generate-embeddings).
     * @param embedRequest Generate embeddings for inputs. (optional)
     * @param _callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request. The request body included invalid request parameters. </td><td>  -  </td></tr>
        <tr><td> 401 </td><td> Unauthorized. Possible causes: Invalid API key. </td><td>  -  </td></tr>
        <tr><td> 500 </td><td> Internal server error. </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call embedAsync(EmbedRequest embedRequest, final ApiCallback<EmbeddingsList> _callback) throws ApiException {

        okhttp3.Call localVarCall = embedValidateBeforeCall(embedRequest, _callback);
        Type localVarReturnType = new TypeToken<EmbeddingsList>(){}.getType();
        localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
        return localVarCall;
    }
    /**
     * Build call for rerank
     * @param rerankRequest Rerank documents for the given query (optional)
     * @param _callback Callback for upload/download progress
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request. The request body included invalid request parameters. </td><td>  -  </td></tr>
        <tr><td> 401 </td><td> Unauthorized. Possible causes: Invalid API key. </td><td>  -  </td></tr>
        <tr><td> 500 </td><td> Internal server error. </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call rerankCall(RerankRequest rerankRequest, final ApiCallback _callback) throws ApiException {
        String basePath = null;
        // Operation Servers
        String[] localBasePaths = new String[] {  };

        // Determine Base Path to Use
        if (localCustomBaseUrl != null){
            basePath = localCustomBaseUrl;
        } else if ( localBasePaths.length > 0 ) {
            basePath = localBasePaths[localHostIndex];
        } else {
            basePath = null;
        }

        Object localVarPostBody = rerankRequest;

        // create path and map variables
        String localVarPath = "/rerank";

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, String> localVarCookieParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
            "application/json"
        };
        final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) {
            localVarHeaderParams.put("Accept", localVarAccept);
        }

        final String[] localVarContentTypes = {
            "application/json"
        };
        final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
        if (localVarContentType != null) {
            localVarHeaderParams.put("Content-Type", localVarContentType);
        }

        String[] localVarAuthNames = new String[] { "ApiKeyAuth" };
        return localVarApiClient.buildCall(basePath, localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
    }

    @SuppressWarnings("rawtypes")
    private okhttp3.Call rerankValidateBeforeCall(RerankRequest rerankRequest, final ApiCallback _callback) throws ApiException {
        return rerankCall(rerankRequest, _callback);

    }

    /**
     * Rerank documents
     * Rerank documents according to their relevance to a query.  For guidance and examples, see [Rerank documents](https://docs.pinecone.io/guides/inference/rerank).
     * @param rerankRequest Rerank documents for the given query (optional)
     * @return RerankResult
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request. The request body included invalid request parameters. </td><td>  -  </td></tr>
        <tr><td> 401 </td><td> Unauthorized. Possible causes: Invalid API key. </td><td>  -  </td></tr>
        <tr><td> 500 </td><td> Internal server error. </td><td>  -  </td></tr>
     </table>
     */
    public RerankResult rerank(RerankRequest rerankRequest) throws ApiException {
        ApiResponse<RerankResult> localVarResp = rerankWithHttpInfo(rerankRequest);
        return localVarResp.getData();
    }

    /**
     * Rerank documents
     * Rerank documents according to their relevance to a query.  For guidance and examples, see [Rerank documents](https://docs.pinecone.io/guides/inference/rerank).
     * @param rerankRequest Rerank documents for the given query (optional)
     * @return ApiResponse&lt;RerankResult&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request. The request body included invalid request parameters. </td><td>  -  </td></tr>
        <tr><td> 401 </td><td> Unauthorized. Possible causes: Invalid API key. </td><td>  -  </td></tr>
        <tr><td> 500 </td><td> Internal server error. </td><td>  -  </td></tr>
     </table>
     */
    public ApiResponse<RerankResult> rerankWithHttpInfo(RerankRequest rerankRequest) throws ApiException {
        okhttp3.Call localVarCall = rerankValidateBeforeCall(rerankRequest, null);
        Type localVarReturnType = new TypeToken<RerankResult>(){}.getType();
        return localVarApiClient.execute(localVarCall, localVarReturnType);
    }

    /**
     * Rerank documents (asynchronously)
     * Rerank documents according to their relevance to a query.  For guidance and examples, see [Rerank documents](https://docs.pinecone.io/guides/inference/rerank).
     * @param rerankRequest Rerank documents for the given query (optional)
     * @param _callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request. The request body included invalid request parameters. </td><td>  -  </td></tr>
        <tr><td> 401 </td><td> Unauthorized. Possible causes: Invalid API key. </td><td>  -  </td></tr>
        <tr><td> 500 </td><td> Internal server error. </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call rerankAsync(RerankRequest rerankRequest, final ApiCallback<RerankResult> _callback) throws ApiException {

        okhttp3.Call localVarCall = rerankValidateBeforeCall(rerankRequest, _callback);
        Type localVarReturnType = new TypeToken<RerankResult>(){}.getType();
        localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
        return localVarCall;
    }
}
