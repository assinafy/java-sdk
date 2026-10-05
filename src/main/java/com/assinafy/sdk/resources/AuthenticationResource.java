package com.assinafy.sdk.resources;

import com.assinafy.sdk.Logger;
import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.http.ApiHttpClient;
import com.assinafy.sdk.models.AuthSession;

import java.util.HashMap;
import java.util.Map;

/** Login, social-login, and password operations from the documented Authentication API. */
public class AuthenticationResource extends BaseResource {

    /**
     * Create authentication operations with a logger.
     *
     * @param http HTTP transport
     * @param logger diagnostic logger
     */
    public AuthenticationResource(ApiHttpClient http, Logger logger) { super(http, null, logger); }

    /**
     * Create authentication operations with no-op logging.
     *
     * @param http HTTP transport
     */
    public AuthenticationResource(ApiHttpClient http) { super(http); }

    /**
     * Authenticate with {@code {email, password}} via {@code POST /login}.
     *
     * <p><strong>HTTP:</strong> <code>POST /v1/login</code>.
     * <strong>Authentication:</strong> Public (no SDK credential).</p>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "email": "user@example.invalid",
     *   "password": "password"
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "access_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
     *     "user": {
     *       "id": "bgjazeo5r9v2lq7l36dx48np",
     *       "name": "John Smith",
     *       "email": "example@example.invalid",
     *       "telephone": null,
     *       "government_id": null,
     *       "is_email_verified": false,
     *       "has_accepted_terms": true,
     *       "created_at": "2023-03-03T11:51:34Z",
     *       "to_be_deleted_at": null
     *     },
     *     "accounts": [
     *       {
     *         "id": "6401df46d6a6b0c692d9ec49",
     *         "name": "JS",
     *         "roles": [
     *           "roles_example"
     *         ],
     *         "is_delete_allowed": true,
     *         "created_at": "2023-03-03T11:51:34Z"
     *       }
     *     ]
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Access token, user and accounts; 400 One or more fields failed
     * validation.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param email account email address
     * @param password account password
     * @return {@code {access_token, user: AuthUser, accounts: AuthAccount[]}}
     * @throws ValidationException if the email or password is invalid
     */
    public AuthSession login(String email, String password) {
        requireEmail(email);
        requireId(password, "Password");
        return call("Login failed",
                () -> http.post("/login", serialise(Map.of("email", email, "password", password))),
                AuthSession.class);
    }

    /**
     * Exchange a provider token via {@code POST /authentication/social-login} with
     * {@code {provider, token, has_accepted_terms}}. The published provider is {@code google}.
     *
     * <p><strong>HTTP:</strong> <code>POST /v1/authentication/social-login</code>.
     * <strong>Authentication:</strong> Public (no SDK credential).</p>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "provider": "google",
     *   "token": "yOTUvImV4cCI6MTY3OTY1ODY5NS...",
     *   "has_accepted_terms": true
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "access_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
     *     "user": {
     *       "id": "bgjazeo5r9v2lq7l36dx48np",
     *       "name": "John Smith",
     *       "email": "example@example.invalid",
     *       "telephone": null,
     *       "government_id": null,
     *       "is_email_verified": false,
     *       "has_accepted_terms": true,
     *       "created_at": "2023-03-03T11:51:34Z",
     *       "to_be_deleted_at": null
     *     },
     *     "accounts": [
     *       {
     *         "id": "6401df46d6a6b0c692d9ec49",
     *         "name": "JS",
     *         "roles": [
     *           "roles_example"
     *         ],
     *         "is_delete_allowed": true,
     *         "created_at": "2023-03-03T11:51:34Z"
     *       }
     *     ]
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Access token, user and accounts; 400 One or more fields failed
     * validation.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param provider social provider; currently {@code google}
     * @param token provider access token
     * @param hasAcceptedTerms whether the user accepted the platform terms
     * @return {@code {access_token, user: AuthUser, accounts: AuthAccount[]}}
     * @throws ValidationException if the provider or token is invalid
     */
    public AuthSession socialLogin(String provider, String token, boolean hasAcceptedTerms) {
        String name = requireGoogleProvider(provider);
        requireId(token, "Provider token");
        Map<String, Object> body = new HashMap<>();
        body.put("provider", name);
        body.put("token", token);
        body.put("has_accepted_terms", hasAcceptedTerms);
        return call("Social login failed",
                () -> http.post("/authentication/social-login", serialise(body)),
                AuthSession.class);
    }

    /**
     * Link a provider account to the authenticated user via {@code POST /auth/link-social-login}
     * with {@code {provider, token}}. The success envelope has no data payload.
     *
     * <p><strong>HTTP:</strong> <code>POST /v1/auth/link-social-login</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "provider": "google",
     *   "token": "token_example"
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": ""
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Provider linked; 400 One or more fields failed validation.; 401
     * Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param provider social provider; currently {@code google}
     * @param token provider access token
     * @throws ValidationException if the provider or token is invalid
     */
    public void linkSocialLogin(String provider, String token) {
        String name = requireGoogleProvider(provider);
        requireId(token, "Provider token");
        callVoid("Failed to link social login", () -> http.post("/auth/link-social-login",
                serialise(Map.of("provider", name, "token", token))));
    }

    /**
     * Change the authenticated user's password via
     * {@code PUT /authentication/change-password} with
     * {@code {email, password, new_password}}.
     *
     * The response {@code data.email} is decoded and discarded. Use
     * {@link #changePasswordResult(String, String, String)} when the response payload is needed.
     *
     * <p><strong>HTTP:</strong> <code>PUT /v1/authentication/change-password</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "email": "user@example.invalid",
     *   "password": "X3$_!456aTa",
     *   "new_password": "X3$_!456aT"
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "email": "user@example.invalid"
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Password changed; 400 One or more fields failed validation.; 401
     * Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param email account email address
     * @param password current password
     * @param newPassword replacement password
     * @throws ValidationException if any credential is invalid
     */
    public void changePassword(String email, String password, String newPassword) {
        changePasswordResult(email, password, newPassword);
    }

    /**
     * Change the authenticated user's password and return {@code {email}} from the response.
     *
     * <p><strong>HTTP:</strong> <code>PUT /v1/authentication/change-password</code>.
     * <strong>Authentication:</strong> Bearer JWT or <code>X-Api-Key</code>.</p>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "email": "user@example.invalid",
     *   "password": "X3$_!456aTa",
     *   "new_password": "X3$_!456aT"
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "email": "user@example.invalid"
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Password changed; 400 One or more fields failed validation.; 401
     * Missing or invalid credentials.; 500 Unexpected server error. Non-2xx HTTP or numeric envelope
     * statuses raise <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429
     * raises <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>.
     * Invalid local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param email account email address
     * @param password current password
     * @param newPassword replacement password
     * @return response data containing the affected {@code email}
     * @throws ValidationException if any credential is invalid
     */
    public Map<String, Object> changePasswordResult(String email, String password, String newPassword) {
        requireEmail(email);
        requireId(password, "Current password");
        requireId(newPassword, "New password");
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("new_password", newPassword);
        return callMap("Failed to change password",
                () -> http.put("/authentication/change-password", serialise(body)));
    }

    /**
     * Send password-reset instructions via {@code PUT /authentication/request-password-reset}
     * with {@code {email}}. This operation is unauthenticated.
     *
     * The response {@code data.email} is decoded and discarded. Use
     * {@link #requestPasswordResetResult(String)} when the response payload is needed.
     *
     * <p><strong>HTTP:</strong> <code>PUT /v1/authentication/request-password-reset</code>.
     * <strong>Authentication:</strong> Public (no SDK credential).</p>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "email": "user@example.invalid"
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "email": "user@example.invalid"
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Reset email sent; 500 Unexpected server error. Non-2xx HTTP or
     * numeric envelope statuses raise <code>ApiException</code>; 401/403 raise
     * <code>AuthenticationException</code>, 429 raises <code>RateLimitException</code>, and I/O failures
     * raise <code>NetworkException</code>. Invalid local arguments raise
     * <code>ValidationException</code> before a request.</p>
     *
     * @param email account email address
     * @throws ValidationException if the email is invalid
     */
    public void requestPasswordReset(String email) {
        requestPasswordResetResult(email);
    }

    /**
     * Send password-reset instructions and return {@code {email}} from the response.
     *
     * <p><strong>HTTP:</strong> <code>PUT /v1/authentication/request-password-reset</code>.
     * <strong>Authentication:</strong> Public (no SDK credential).</p>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "email": "user@example.invalid"
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "email": "user@example.invalid"
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Reset email sent; 500 Unexpected server error. Non-2xx HTTP or
     * numeric envelope statuses raise <code>ApiException</code>; 401/403 raise
     * <code>AuthenticationException</code>, 429 raises <code>RateLimitException</code>, and I/O failures
     * raise <code>NetworkException</code>. Invalid local arguments raise
     * <code>ValidationException</code> before a request.</p>
     *
     * <p>An email that does not identify a registered user returns HTTP 404. A signer contact
     * alone does not create a user identity.</p>
     *
     * @param email account email address
     * @return response data containing the affected {@code email}
     * @throws ValidationException if the email is invalid
     */
    public Map<String, Object> requestPasswordResetResult(String email) {
        requireEmail(email);
        return callMap("Failed to request password reset",
                () -> http.put("/authentication/request-password-reset", serialise(Map.of("email", email))));
    }

    /**
     * Set a new password via {@code PUT /authentication/reset-password} with
     * {@code {email, token, new_password}}. This operation is unauthenticated; {@code token} is the
     * value delivered by the reset email.
     *
     * The reset token is optional in the published schema. The response {@code data.email} is
     * decoded and discarded; use {@link #resetPasswordResult(String, String, String)} when the
     * response payload is needed.
     *
     * <p><strong>HTTP:</strong> <code>PUT /v1/authentication/reset-password</code>.
     * <strong>Authentication:</strong> Public (no SDK credential).</p>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "email": "user@example.invalid",
     *   "token": "b3ac64d6c55b3ac64d6c55",
     *   "new_password": "N3w_p4ssw0rd"
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "email": "user@example.invalid"
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Password reset; 400 One or more fields failed validation.; 500
     * Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param email account email address
     * @param token optional reset token delivered by email
     * @param newPassword replacement password
     * @throws ValidationException if the email or new password is invalid
     */
    public void resetPassword(String email, String token, String newPassword) {
        resetPasswordResult(email, token, newPassword);
    }

    /**
     * Set a new password and return {@code {email}} from the response.
     *
     * <p><strong>HTTP:</strong> <code>PUT /v1/authentication/reset-password</code>.
     * <strong>Authentication:</strong> Public (no SDK credential).</p>
     * <p>Request body: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "email": "user@example.invalid",
     *   "token": "b3ac64d6c55b3ac64d6c55",
     *   "new_password": "N3w_p4ssw0rd"
     * }</pre>
     * <p>Success 200: <code>application/json</code>. Illustrative payload with the documented fields;
     * optional fields may be absent or null.</p>
     * <pre>{
     *   "status": 200,
     *   "message": "",
     *   "data": {
     *     "email": "user@example.invalid"
     *   }
     * }</pre>
     * <p>The SDK unwraps JSON envelopes to their <code>data</code> value; OAuth responses remain flat.
     * Void methods discard a success payload. Binary methods return the bytes directly.</p>
     * <p>Documented HTTP statuses: 200 Password reset; 400 One or more fields failed validation.; 500
     * Unexpected server error. Non-2xx HTTP or numeric envelope statuses raise
     * <code>ApiException</code>; 401/403 raise <code>AuthenticationException</code>, 429 raises
     * <code>RateLimitException</code>, and I/O failures raise <code>NetworkException</code>. Invalid
     * local arguments raise <code>ValidationException</code> before a request.</p>
     *
     * @param email account email address
     * @param token optional reset token delivered by email
     * @param newPassword replacement password
     * @return response data containing the affected {@code email}
     * @throws ValidationException if the email or new password is invalid
     */
    public Map<String, Object> resetPasswordResult(String email, String token, String newPassword) {
        requireEmail(email);
        requireId(newPassword, "New password");
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        if (token != null) body.put("token", token);
        body.put("new_password", newPassword);
        return callMap("Failed to reset password",
                () -> http.put("/authentication/reset-password", serialise(body)));
    }

    private String requireGoogleProvider(String provider) {
        String value = requireId(provider, "Provider");
        if (!"google".equals(value)) {
            throw new ValidationException("Provider must be google");
        }
        return value;
    }
}
