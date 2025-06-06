/*
 * Pinecone Data Plane API
 * Pinecone is a vector database that makes it easy to search and retrieve billions of high-dimensional vectors.
 *
 * The version of the OpenAPI document: 2025-04
 * Contact: support@pinecone.io
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package org.openapitools.db_data.client.api;

import org.openapitools.db_data.client.ApiCallback;
import org.openapitools.db_data.client.ApiClient;
import org.openapitools.db_data.client.ApiException;
import org.openapitools.db_data.client.ApiResponse;
import org.openapitools.db_data.client.Configuration;
import org.openapitools.db_data.client.Pair;
import org.openapitools.db_data.client.ProgressRequestBody;
import org.openapitools.db_data.client.ProgressResponseBody;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;


import org.openapitools.db_data.client.model.ImportModel;
import org.openapitools.db_data.client.model.ListImportsResponse;
import org.openapitools.db_data.client.model.RpcStatus;
import org.openapitools.db_data.client.model.StartImportRequest;
import org.openapitools.db_data.client.model.StartImportResponse;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BulkOperationsApi {
    private ApiClient localVarApiClient;
    private int localHostIndex;
    private String localCustomBaseUrl;

    public BulkOperationsApi() {
        this(Configuration.getDefaultApiClient());
    }

    public BulkOperationsApi(ApiClient apiClient) {
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
     * Build call for cancelBulkImport
     * @param id Unique identifier for the import operation. (required)
     * @param _callback Callback for upload/download progress
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Operation cancelled successfully </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request. The request body included invalid request parameters. </td><td>  -  </td></tr>
        <tr><td> 4XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
        <tr><td> 5XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call cancelBulkImportCall(String id, final ApiCallback _callback) throws ApiException {
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

        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/bulk/imports/{id}"
            .replace("{" + "id" + "}", localVarApiClient.escapeString(id.toString()));

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
        };
        final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
        if (localVarContentType != null) {
            localVarHeaderParams.put("Content-Type", localVarContentType);
        }

        String[] localVarAuthNames = new String[] { "ApiKeyAuth" };
        return localVarApiClient.buildCall(basePath, localVarPath, "DELETE", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
    }

    @SuppressWarnings("rawtypes")
    private okhttp3.Call cancelBulkImportValidateBeforeCall(String id, final ApiCallback _callback) throws ApiException {
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new ApiException("Missing the required parameter 'id' when calling cancelBulkImport(Async)");
        }

        return cancelBulkImportCall(id, _callback);

    }

    /**
     * Cancel an import
     * Cancel an import operation if it is not yet finished. It has no effect if the operation is already finished.  For guidance and examples, see [Import data](https://docs.pinecone.io/guides/index-data/import-data).
     * @param id Unique identifier for the import operation. (required)
     * @return Object
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Operation cancelled successfully </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request. The request body included invalid request parameters. </td><td>  -  </td></tr>
        <tr><td> 4XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
        <tr><td> 5XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
     </table>
     */
    public Object cancelBulkImport(String id) throws ApiException {
        ApiResponse<Object> localVarResp = cancelBulkImportWithHttpInfo(id);
        return localVarResp.getData();
    }

    /**
     * Cancel an import
     * Cancel an import operation if it is not yet finished. It has no effect if the operation is already finished.  For guidance and examples, see [Import data](https://docs.pinecone.io/guides/index-data/import-data).
     * @param id Unique identifier for the import operation. (required)
     * @return ApiResponse&lt;Object&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Operation cancelled successfully </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request. The request body included invalid request parameters. </td><td>  -  </td></tr>
        <tr><td> 4XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
        <tr><td> 5XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
     </table>
     */
    public ApiResponse<Object> cancelBulkImportWithHttpInfo(String id) throws ApiException {
        okhttp3.Call localVarCall = cancelBulkImportValidateBeforeCall(id, null);
        Type localVarReturnType = new TypeToken<Object>(){}.getType();
        return localVarApiClient.execute(localVarCall, localVarReturnType);
    }

    /**
     * Cancel an import (asynchronously)
     * Cancel an import operation if it is not yet finished. It has no effect if the operation is already finished.  For guidance and examples, see [Import data](https://docs.pinecone.io/guides/index-data/import-data).
     * @param id Unique identifier for the import operation. (required)
     * @param _callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Operation cancelled successfully </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request. The request body included invalid request parameters. </td><td>  -  </td></tr>
        <tr><td> 4XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
        <tr><td> 5XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call cancelBulkImportAsync(String id, final ApiCallback<Object> _callback) throws ApiException {

        okhttp3.Call localVarCall = cancelBulkImportValidateBeforeCall(id, _callback);
        Type localVarReturnType = new TypeToken<Object>(){}.getType();
        localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
        return localVarCall;
    }
    /**
     * Build call for describeBulkImport
     * @param id Unique identifier for the import operation. (required)
     * @param _callback Callback for upload/download progress
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Details of the import operation. </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request. The request body included invalid request parameters. </td><td>  -  </td></tr>
        <tr><td> 4XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
        <tr><td> 5XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call describeBulkImportCall(String id, final ApiCallback _callback) throws ApiException {
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

        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/bulk/imports/{id}"
            .replace("{" + "id" + "}", localVarApiClient.escapeString(id.toString()));

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
        };
        final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
        if (localVarContentType != null) {
            localVarHeaderParams.put("Content-Type", localVarContentType);
        }

        String[] localVarAuthNames = new String[] { "ApiKeyAuth" };
        return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
    }

    @SuppressWarnings("rawtypes")
    private okhttp3.Call describeBulkImportValidateBeforeCall(String id, final ApiCallback _callback) throws ApiException {
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new ApiException("Missing the required parameter 'id' when calling describeBulkImport(Async)");
        }

        return describeBulkImportCall(id, _callback);

    }

    /**
     * Describe an import
     * Return details of a specific import operation.  For guidance and examples, see [Import data](https://docs.pinecone.io/guides/index-data/import-data).
     * @param id Unique identifier for the import operation. (required)
     * @return ImportModel
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Details of the import operation. </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request. The request body included invalid request parameters. </td><td>  -  </td></tr>
        <tr><td> 4XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
        <tr><td> 5XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
     </table>
     */
    public ImportModel describeBulkImport(String id) throws ApiException {
        ApiResponse<ImportModel> localVarResp = describeBulkImportWithHttpInfo(id);
        return localVarResp.getData();
    }

    /**
     * Describe an import
     * Return details of a specific import operation.  For guidance and examples, see [Import data](https://docs.pinecone.io/guides/index-data/import-data).
     * @param id Unique identifier for the import operation. (required)
     * @return ApiResponse&lt;ImportModel&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Details of the import operation. </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request. The request body included invalid request parameters. </td><td>  -  </td></tr>
        <tr><td> 4XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
        <tr><td> 5XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
     </table>
     */
    public ApiResponse<ImportModel> describeBulkImportWithHttpInfo(String id) throws ApiException {
        okhttp3.Call localVarCall = describeBulkImportValidateBeforeCall(id, null);
        Type localVarReturnType = new TypeToken<ImportModel>(){}.getType();
        return localVarApiClient.execute(localVarCall, localVarReturnType);
    }

    /**
     * Describe an import (asynchronously)
     * Return details of a specific import operation.  For guidance and examples, see [Import data](https://docs.pinecone.io/guides/index-data/import-data).
     * @param id Unique identifier for the import operation. (required)
     * @param _callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Details of the import operation. </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request. The request body included invalid request parameters. </td><td>  -  </td></tr>
        <tr><td> 4XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
        <tr><td> 5XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call describeBulkImportAsync(String id, final ApiCallback<ImportModel> _callback) throws ApiException {

        okhttp3.Call localVarCall = describeBulkImportValidateBeforeCall(id, _callback);
        Type localVarReturnType = new TypeToken<ImportModel>(){}.getType();
        localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
        return localVarCall;
    }
    /**
     * Build call for listBulkImports
     * @param limit Max number of operations to return per page. (optional)
     * @param paginationToken Pagination token to continue a previous listing operation. (optional)
     * @param _callback Callback for upload/download progress
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> A list of import operations </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request. The request body included invalid request parameters. </td><td>  -  </td></tr>
        <tr><td> 4XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
        <tr><td> 5XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call listBulkImportsCall(Integer limit, String paginationToken, final ApiCallback _callback) throws ApiException {
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

        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/bulk/imports";

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, String> localVarCookieParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        if (limit != null) {
            localVarQueryParams.addAll(localVarApiClient.parameterToPair("limit", limit));
        }

        if (paginationToken != null) {
            localVarQueryParams.addAll(localVarApiClient.parameterToPair("paginationToken", paginationToken));
        }

        final String[] localVarAccepts = {
            "application/json"
        };
        final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) {
            localVarHeaderParams.put("Accept", localVarAccept);
        }

        final String[] localVarContentTypes = {
        };
        final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
        if (localVarContentType != null) {
            localVarHeaderParams.put("Content-Type", localVarContentType);
        }

        String[] localVarAuthNames = new String[] { "ApiKeyAuth" };
        return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
    }

    @SuppressWarnings("rawtypes")
    private okhttp3.Call listBulkImportsValidateBeforeCall(Integer limit, String paginationToken, final ApiCallback _callback) throws ApiException {
        return listBulkImportsCall(limit, paginationToken, _callback);

    }

    /**
     * List imports
     * List all recent and ongoing import operations.  By default, &#x60;list_imports&#x60; returns up to 100 imports per page. If the &#x60;limit&#x60; parameter is set, &#x60;list&#x60; returns up to that number of imports instead. Whenever there are additional IDs to return, the response also includes a &#x60;pagination_token&#x60; that you can use to get the next batch of imports. When the response does not include a &#x60;pagination_token&#x60;, there are no more imports to return.  For guidance and examples, see [Import data](https://docs.pinecone.io/guides/index-data/import-data).
     * @param limit Max number of operations to return per page. (optional)
     * @param paginationToken Pagination token to continue a previous listing operation. (optional)
     * @return ListImportsResponse
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> A list of import operations </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request. The request body included invalid request parameters. </td><td>  -  </td></tr>
        <tr><td> 4XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
        <tr><td> 5XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
     </table>
     */
    public ListImportsResponse listBulkImports(Integer limit, String paginationToken) throws ApiException {
        ApiResponse<ListImportsResponse> localVarResp = listBulkImportsWithHttpInfo(limit, paginationToken);
        return localVarResp.getData();
    }

    /**
     * List imports
     * List all recent and ongoing import operations.  By default, &#x60;list_imports&#x60; returns up to 100 imports per page. If the &#x60;limit&#x60; parameter is set, &#x60;list&#x60; returns up to that number of imports instead. Whenever there are additional IDs to return, the response also includes a &#x60;pagination_token&#x60; that you can use to get the next batch of imports. When the response does not include a &#x60;pagination_token&#x60;, there are no more imports to return.  For guidance and examples, see [Import data](https://docs.pinecone.io/guides/index-data/import-data).
     * @param limit Max number of operations to return per page. (optional)
     * @param paginationToken Pagination token to continue a previous listing operation. (optional)
     * @return ApiResponse&lt;ListImportsResponse&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> A list of import operations </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request. The request body included invalid request parameters. </td><td>  -  </td></tr>
        <tr><td> 4XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
        <tr><td> 5XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
     </table>
     */
    public ApiResponse<ListImportsResponse> listBulkImportsWithHttpInfo(Integer limit, String paginationToken) throws ApiException {
        okhttp3.Call localVarCall = listBulkImportsValidateBeforeCall(limit, paginationToken, null);
        Type localVarReturnType = new TypeToken<ListImportsResponse>(){}.getType();
        return localVarApiClient.execute(localVarCall, localVarReturnType);
    }

    /**
     * List imports (asynchronously)
     * List all recent and ongoing import operations.  By default, &#x60;list_imports&#x60; returns up to 100 imports per page. If the &#x60;limit&#x60; parameter is set, &#x60;list&#x60; returns up to that number of imports instead. Whenever there are additional IDs to return, the response also includes a &#x60;pagination_token&#x60; that you can use to get the next batch of imports. When the response does not include a &#x60;pagination_token&#x60;, there are no more imports to return.  For guidance and examples, see [Import data](https://docs.pinecone.io/guides/index-data/import-data).
     * @param limit Max number of operations to return per page. (optional)
     * @param paginationToken Pagination token to continue a previous listing operation. (optional)
     * @param _callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> A list of import operations </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request. The request body included invalid request parameters. </td><td>  -  </td></tr>
        <tr><td> 4XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
        <tr><td> 5XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call listBulkImportsAsync(Integer limit, String paginationToken, final ApiCallback<ListImportsResponse> _callback) throws ApiException {

        okhttp3.Call localVarCall = listBulkImportsValidateBeforeCall(limit, paginationToken, _callback);
        Type localVarReturnType = new TypeToken<ListImportsResponse>(){}.getType();
        localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
        return localVarCall;
    }
    /**
     * Build call for startBulkImport
     * @param startImportRequest  (required)
     * @param _callback Callback for upload/download progress
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Successful import operation </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request. The request body included invalid request parameters. </td><td>  -  </td></tr>
        <tr><td> 4XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
        <tr><td> 5XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call startBulkImportCall(StartImportRequest startImportRequest, final ApiCallback _callback) throws ApiException {
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

        Object localVarPostBody = startImportRequest;

        // create path and map variables
        String localVarPath = "/bulk/imports";

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
    private okhttp3.Call startBulkImportValidateBeforeCall(StartImportRequest startImportRequest, final ApiCallback _callback) throws ApiException {
        // verify the required parameter 'startImportRequest' is set
        if (startImportRequest == null) {
            throw new ApiException("Missing the required parameter 'startImportRequest' when calling startBulkImport(Async)");
        }

        return startBulkImportCall(startImportRequest, _callback);

    }

    /**
     * Start import
     * Start an asynchronous import of vectors from object storage into an index.  For guidance and examples, see [Import data](https://docs.pinecone.io/guides/index-data/import-data).
     * @param startImportRequest  (required)
     * @return StartImportResponse
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Successful import operation </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request. The request body included invalid request parameters. </td><td>  -  </td></tr>
        <tr><td> 4XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
        <tr><td> 5XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
     </table>
     */
    public StartImportResponse startBulkImport(StartImportRequest startImportRequest) throws ApiException {
        ApiResponse<StartImportResponse> localVarResp = startBulkImportWithHttpInfo(startImportRequest);
        return localVarResp.getData();
    }

    /**
     * Start import
     * Start an asynchronous import of vectors from object storage into an index.  For guidance and examples, see [Import data](https://docs.pinecone.io/guides/index-data/import-data).
     * @param startImportRequest  (required)
     * @return ApiResponse&lt;StartImportResponse&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Successful import operation </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request. The request body included invalid request parameters. </td><td>  -  </td></tr>
        <tr><td> 4XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
        <tr><td> 5XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
     </table>
     */
    public ApiResponse<StartImportResponse> startBulkImportWithHttpInfo(StartImportRequest startImportRequest) throws ApiException {
        okhttp3.Call localVarCall = startBulkImportValidateBeforeCall(startImportRequest, null);
        Type localVarReturnType = new TypeToken<StartImportResponse>(){}.getType();
        return localVarApiClient.execute(localVarCall, localVarReturnType);
    }

    /**
     * Start import (asynchronously)
     * Start an asynchronous import of vectors from object storage into an index.  For guidance and examples, see [Import data](https://docs.pinecone.io/guides/index-data/import-data).
     * @param startImportRequest  (required)
     * @param _callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Successful import operation </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Bad request. The request body included invalid request parameters. </td><td>  -  </td></tr>
        <tr><td> 4XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
        <tr><td> 5XX </td><td> An unexpected error response. </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call startBulkImportAsync(StartImportRequest startImportRequest, final ApiCallback<StartImportResponse> _callback) throws ApiException {

        okhttp3.Call localVarCall = startBulkImportValidateBeforeCall(startImportRequest, _callback);
        Type localVarReturnType = new TypeToken<StartImportResponse>(){}.getType();
        localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
        return localVarCall;
    }
}
