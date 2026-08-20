package com.assinafy.sdk.exceptions;

import java.util.Map;

/**
 * Raised for authentication/authorization failures — HTTP {@code 401 Unauthorized} and
 * {@code 403 Forbidden}. A subtype of {@link ApiException}, so existing {@code catch
 * (ApiException)} handlers keep working; catch this type to react specifically to a missing,
 * invalid, or insufficiently-privileged credential.
 */
public class AuthenticationException extends ApiException {

    /**
     * Create an authentication exception without response headers or an underlying cause.
     *
     * @param message error message
     * @param statusCode authentication or authorization status code
     * @param responseData decoded response body or envelope
     */
    public AuthenticationException(String message, int statusCode, Object responseData) {
        super(message, statusCode, responseData);
    }

    /**
     * Create an authentication exception with an underlying cause.
     *
     * @param message error message
     * @param statusCode authentication or authorization status code
     * @param responseData decoded response body or envelope
     * @param cause underlying failure
     */
    public AuthenticationException(String message, int statusCode, Object responseData, Throwable cause) {
        super(message, statusCode, responseData, cause);
    }

    /**
     * Create an authentication exception with response headers.
     *
     * @param message error message
     * @param statusCode authentication or authorization status code
     * @param responseData decoded response body or envelope
     * @param headers response headers
     */
    public AuthenticationException(String message, int statusCode, Object responseData,
                                   Map<String, String> headers) {
        super(message, statusCode, responseData, headers);
    }
}
