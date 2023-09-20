package io.pinecone.exceptions;

public class HttpErrorMapper {

    public static void mapHttpStatusError(FailedRequestInfo failedRequestInfo) {
        int statusCode = failedRequestInfo.getStatus();
        switch (statusCode) {
            case 400:
                throw new PineconeBadRequestException(failedRequestInfo.getMessage());
            case 401:
                throw new PineconeAuthorizationException(failedRequestInfo.getMessage());
            case 404:
                throw new PineconeNotFoundException(failedRequestInfo.getMessage());
            case 409:
                throw new PineconeAlreadyIndexExistsException(failedRequestInfo.getMessage());
            case 500:
                throw new PineconeInternalServerException(failedRequestInfo.getMessage());
            default:
                throw new PineconeUnmappedHttpException(failedRequestInfo.getMessage());
        }
    }
}
