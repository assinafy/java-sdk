package com.assinafy.sdk.exceptions;

import java.util.Map;

/**
 * Raised when an OAuth endpoint answers with an RFC 6749 error object rather than this API's usual
 * {@code {status, message, data}} envelope.
 *
 * <p>The token and revocation endpoints report failures as a flat
 * {@code {"error": "...", "error_description": "..."}} body, and an authorization response reports
 * them as {@code ?error=...&error_description=...} query parameters on the redirect URI. Both are
 * surfaced as this type so callers can branch on the machine-readable {@link #getError() code}
 * instead of parsing a message.
 *
 * <p>A subtype of {@link ApiException}, so existing {@code catch (ApiException)} handlers keep
 * working. The most common codes are:
 *
 * <table class="striped">
 * <caption>OAuth error codes returned by Assinafy</caption>
 * <thead><tr><th scope="col">Code</th><th scope="col">Usual cause</th></tr></thead>
 * <tbody>
 * <tr><td>{@code access_denied}</td><td>The user declined the consent screen.</td></tr>
 * <tr><td>{@code invalid_grant}</td><td>Authorization code spent, expired or replayed; wrong
 *     {@code code_verifier} or {@code redirect_uri}; refresh token already used or the user
 *     reconnected with different permissions.</td></tr>
 * <tr><td>{@code invalid_client}</td><td>Unknown {@code client_id}, wrong {@code client_secret},
 *     or a disabled application.</td></tr>
 * <tr><td>{@code invalid_scope}</td><td>A scope the application is not registered for.</td></tr>
 * <tr><td>{@code invalid_target}</td><td>A {@code resource} the server does not issue tokens for,
 *     or one disagreeing with the authorized value.</td></tr>
 * <tr><td>{@code unsupported_grant_type}</td><td>Anything other than {@code authorization_code}
 *     or {@code refresh_token}.</td></tr>
 * </tbody>
 * </table>
 */
public class OAuthException extends ApiException {

    private static final long serialVersionUID = 1L;

    /** RFC 6749 machine-readable error code. */
    private final String error;
    /** Server-supplied human-readable explanation. */
    private final String errorDescription;

    /**
     * Create an OAuth protocol error.
     *
     * @param error RFC 6749 error code, such as {@code invalid_grant}
     * @param errorDescription server-supplied explanation, or {@code null} when none was sent
     * @param statusCode HTTP status that carried the error; an authorization response arrives as
     *                   redirect query parameters rather than an HTTP response and is reported
     *                   as {@code 400}
     * @param responseData the raw error object
     * @param headers response headers; names are normalized for case-insensitive lookup
     */
    public OAuthException(String error, String errorDescription, int statusCode,
                          Object responseData, Map<String, String> headers) {
        super(errorDescription != null && !errorDescription.isBlank()
                        ? error + ": " + errorDescription : error,
                statusCode, responseData, headers);
        this.error = error;
        this.errorDescription = errorDescription;
    }

    /** {@return the RFC 6749 error code, such as {@code invalid_grant}} */
    public String getError() {
        return error;
    }

    /** {@return the server's explanation, or {@code null} when none was sent} */
    public String getErrorDescription() {
        return errorDescription;
    }

    /**
     * Re-wrap an {@link ApiException} as an {@code OAuthException} when its body carries an
     * RFC 6749 {@code error} field, so callers see the protocol code rather than a generic
     * HTTP failure.
     *
     * @param failure any failure thrown by an OAuth request
     * @return an {@code OAuthException} when the body is an RFC 6749 error object, otherwise
     *         {@code failure} unchanged
     */
    public static RuntimeException upgrade(RuntimeException failure) {
        if (!(failure instanceof ApiException api) || failure instanceof OAuthException) return failure;
        if (!(api.getResponseData() instanceof Map<?, ?> body)) return failure;
        if (!(body.get("error") instanceof String code) || code.isBlank()) return failure;
        String description = body.get("error_description") instanceof String text && !text.isBlank()
                ? text : null;
        return new OAuthException(code, description, api.getStatusCode(), body, api.getResponseHeaders());
    }
}
