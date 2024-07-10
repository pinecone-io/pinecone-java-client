package io.pinecone.exceptions;

import org.openapitools.control.client.ApiException;

public class HttpErrorMapper {

    public static void mapHttpStatusError(FailedRequestInfo failedRequestInfo,
                                          ApiException apiException) throws PineconeException {
        int statusCode = failedRequestInfo.getStatus();
        switch (statusCode) {
            case 400:
                throw new PineconeBadRequestException(failedRequestInfo.getMessage(), apiException);
            case 401:
                throw new PineconeAuthorizationException(failedRequestInfo.getMessage(), apiException);
            case 403:
                throw new PineconeForbiddenException(failedRequestInfo.getMessage(), apiException);
            case 404:
                throw new PineconeNotFoundException(failedRequestInfo.getMessage(), apiException);
            case 409:
                throw new PineconeAlreadyExistsException(failedRequestInfo.getMessage(), apiException);
            case 500:
                throw new PineconeInternalServerException(failedRequestInfo.getMessage(), apiException);
            default:
                throw new PineconeUnmappedHttpException(failedRequestInfo.getMessage(), apiException);
        }
    }
}
