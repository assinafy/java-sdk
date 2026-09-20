package com.assinafy.sdk.request;

import com.assinafy.sdk.exceptions.ValidationException;

/**
 * Credentials identifying your OAuth application at the token and revocation endpoints, sent with
 * the {@code client_secret_post} method the authorization server publishes.
 *
 * <p>Create the application under <b>Settings &rarr; OAuth applications</b> in the Assinafy app;
 * you receive a {@code client_id} and, for a <b>Confidential</b> application, a
 * {@code client_secret} shown exactly once.
 *
 * <p>A <b>Public</b> application — one running on the user's device, which cannot keep a secret —
 * is never issued a secret and authenticates with PKCE alone; use {@link #publicClient(String)}.
 * A secret must never reach browser code, a mobile binary, or a repository.
 *
 * @param clientId the application's {@code client_id}
 * @param clientSecret the application's {@code client_secret}, or {@code null} for a public client
 */
public record OAuthClient(String clientId, String clientSecret) {

    /**
     * Validates the credentials.
     *
     * @throws ValidationException if {@code clientId} is blank, or {@code clientSecret} is present
     *                             but blank
     */
    public OAuthClient {
        if (clientId == null || clientId.isBlank()) {
            throw new ValidationException("OAuth client ID is required");
        }
        if (clientSecret != null && clientSecret.isBlank()) {
            throw new ValidationException("OAuth client secret must not be blank");
        }
    }

    /**
     * Credentials for a confidential application, which runs on a server you control.
     *
     * @param clientId the application's {@code client_id}
     * @param clientSecret the application's {@code client_secret}
     * @return the credentials
     * @throws ValidationException if either value is blank
     */
    public static OAuthClient confidential(String clientId, String clientSecret) {
        if (clientSecret == null) {
            throw new ValidationException("OAuth client secret is required for a confidential client");
        }
        return new OAuthClient(clientId, clientSecret);
    }

    /**
     * Credentials for a public application, which runs on the user's device and authenticates with
     * PKCE instead of a secret.
     *
     * @param clientId the application's {@code client_id}
     * @return the credentials
     * @throws ValidationException if {@code clientId} is blank
     */
    public static OAuthClient publicClient(String clientId) {
        return new OAuthClient(clientId, null);
    }

    /** {@return a representation that never discloses the client secret} */
    @Override
    public String toString() {
        return "OAuthClient[clientId=" + clientId
                + ", clientSecret=" + (clientSecret != null ? "<redacted>" : "none") + "]";
    }
}
