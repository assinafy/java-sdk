package com.assinafy.sdk.exceptions;

/**
 * Raised for authentication/authorization failures — HTTP {@code 401 Unauthorized} and
 * {@code 403 Forbidden}. A subtype of {@link ApiException}, so existing {@code catch
 * (ApiException)} handlers keep working; catch this type to react specifically to a missing,
 * invalid, or insufficiently-privileged credential.
 */
public class AuthenticationException extends ApiException {

    public AuthenticationException(String message, int statusCode, Object responseData) {
        super(message, statusCode, responseData);
    }

    public AuthenticationException(String message, int statusCode, Object responseData, Throwable cause) {
        super(message, statusCode, responseData, cause);
    }
}
