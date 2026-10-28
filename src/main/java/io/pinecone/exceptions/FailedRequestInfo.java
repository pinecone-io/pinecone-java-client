package io.pinecone.exceptions;

/**
 * The HTTP status code and response body of a failed request, which {@link HttpErrorMapper} turns into the matching
 * {@link PineconeException} subclass.
 * <p>
 * The client creates these internally; callers don't normally need one. The same body is available from
 * {@link PineconeException#getMessage()} on the thrown exception.
 * <pre>{@code
 * FailedRequestInfo info = new FailedRequestInfo(404, "{\"error\":{\"code\":\"NOT_FOUND\"}}");
 * int status = info.getStatus(); // 404
 * }</pre>
 */
public class FailedRequestInfo {
    private final int status;
    private final String message;

    /**
     * Records a failed request's status code and response body.
     *
     * @param status the HTTP status code the server returned, such as {@code 404}
     * @param message the response body, usually a JSON error object; may be null when the response had no body
     */
    public FailedRequestInfo(int status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * HTTP status code the server returned, such as {@code 404}.
     *
     * @return the status code
     */
    public int getStatus() {
        return status;
    }

    /**
     * Response body the server returned, usually a JSON error object.
     *
     * @return the body, or null if the response had none
     */
    public String getMessage() {
        return message;
    }
}

