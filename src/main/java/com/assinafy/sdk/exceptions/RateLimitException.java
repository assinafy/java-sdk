package com.assinafy.sdk.exceptions;

/**
 * Raised when the API returns HTTP {@code 429 Too Many Requests}. A subtype of
 * {@link ApiException}, so existing {@code catch (ApiException)} handlers keep working; catch
 * this type to implement backoff/retry. Inspect {@link #getResponseData()} for any
 * server-provided detail.
 */
public class RateLimitException extends ApiException {

    public RateLimitException(String message, int statusCode, Object responseData) {
        super(message, statusCode, responseData);
    }

    public RateLimitException(String message, int statusCode, Object responseData, Throwable cause) {
        super(message, statusCode, responseData, cause);
    }
}
