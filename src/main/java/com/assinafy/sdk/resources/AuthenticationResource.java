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
     * @param email account email address
     * @throws ValidationException if the email is invalid
     */
    public void requestPasswordReset(String email) {
        requestPasswordResetResult(email);
    }

    /**
     * Send password-reset instructions and return {@code {email}} from the response.
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
