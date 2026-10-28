package io.pinecone.exceptions;

import org.openapitools.db_control.client.ApiException;

/**
 * Turns a failed index or collection management request into the {@link PineconeException} subclass for its HTTP
 * status.
 * <p>
 * {@link io.pinecone.clients.Pinecone} uses it internally; callers catch the resulting exceptions rather than
 * calling it.
 * <pre>{@code
 * try {
 *     HttpErrorMapper.mapHttpStatusError(new FailedRequestInfo(409, responseBody), apiException);
 * } catch (PineconeAlreadyExistsException e) {
 *     // 409 maps to PineconeAlreadyExistsException
 * }
 * }</pre>
 */
public class HttpErrorMapper {

    /**
     * Throws the exception for the request's status: 400 {@link PineconeBadRequestException}, 401
     * {@link PineconeAuthorizationException}, 403 {@link PineconeForbiddenException}, 404
     * {@link PineconeNotFoundException}, 409 {@link PineconeAlreadyExistsException}, 500
     * {@link PineconeInternalServerException}, and any other status {@link PineconeUnmappedHttpException}.
     * <p>
     * It always throws. The exception's message is the response body and its cause is {@code apiException}.
     *
     * @param failedRequestInfo the status code and response body of the failed request
     * @param apiException the error the generated HTTP client threw for the request
     * @throws PineconeException always, as the subclass for the status code
     */
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
