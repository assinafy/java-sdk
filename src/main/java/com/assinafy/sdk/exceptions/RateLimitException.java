package com.assinafy.sdk.exceptions;

import java.util.Map;

/**
 * Raised when the API returns HTTP {@code 429 Too Many Requests}. A subtype of
 * {@link ApiException}, so existing {@code catch (ApiException)} handlers keep working; catch
 * this type to implement backoff/retry. Inspect {@link #getResponseData()} for any
 * server-provided detail.
 */
public class RateLimitException extends ApiException {

    private static final long serialVersionUID = 1L;

    /**
     * Create a rate-limit exception without response headers or an underlying cause.
     *
     * @param message error message
     * @param statusCode rate-limit status code
     * @param responseData decoded response body or envelope
     */
    public RateLimitException(String message, int statusCode, Object responseData) {
        super(message, statusCode, responseData);
    }

    /**
     * Create a rate-limit exception with an underlying cause.
     *
     * @param message error message
     * @param statusCode rate-limit status code
     * @param responseData decoded response body or envelope
     * @param cause underlying failure
     */
    public RateLimitException(String message, int statusCode, Object responseData, Throwable cause) {
        super(message, statusCode, responseData, cause);
    }

    /**
     * Create a rate-limit exception with response headers.
     *
     * @param message error message
     * @param statusCode rate-limit status code
     * @param responseData decoded response body or envelope
     * @param headers response headers, including retry metadata when supplied by the API
     */
    public RateLimitException(String message, int statusCode, Object responseData,
                              Map<String, String> headers) {
        super(message, statusCode, responseData, headers);
    }
}
