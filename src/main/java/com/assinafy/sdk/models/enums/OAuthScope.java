package com.assinafy.sdk.models.enums;

/**
 * Permissions an OAuth application may request on the authorization endpoint.
 *
 * <p>The user approves every requested scope or none of them, so request the minimum your
 * integration needs — each one is another line the user reads before deciding. If a feature needs
 * more later, run the flow again requesting the larger set.
 *
 * <p>Billing and subscriptions, workspace membership, credential management and administration are
 * never reachable with an OAuth token, whatever its scopes.
 */
public enum OAuthScope {

    /** Read documents, their pages, tags, signers, assignments and activity. */
    DOCUMENTS_READ("documents:read"),

    /**
     * Create, update and delete documents, and manage their signers, assignments and activity.
     * Spends the workspace's notification credits, because sending for signature notifies signers.
     */
    DOCUMENTS_WRITE("documents:write"),

    /** Read reusable document templates, their pages, roles, fields and tags. */
    TEMPLATES_READ("templates:read"),

    /** Create, update and delete templates, their pages, roles, fields and tags. */
    TEMPLATES_WRITE("templates:write"),

    /** Read the workspace's profile, theme and logo. */
    ACCOUNT_READ("account:read"),

    /**
     * Identify the authenticated user through the OpenID Connect {@code sub} claim, receive an
     * {@code id_token}, and enable {@code GET /oauth/userinfo}.
     */
    OPENID("openid"),

    /** Include the user's name in the {@code id_token} and userinfo claims. */
    PROFILE("profile"),

    /** Include the user's email and its verification status in the {@code id_token} and userinfo claims. */
    EMAIL("email"),

    /**
     * Request a refresh token so the application keeps working after the user's session expires
     * without prompting them again. Granted only to a client that explicitly asks for it, and it
     * never appears in the access token's own {@code scope}, because it is a request-time signal
     * rather than a permission.
     */
    OFFLINE_ACCESS("offline_access");

    private final String value;

    OAuthScope(String value) {
        this.value = value;
    }

    /** {@return the wire value sent in the {@code scope} parameter} */
    public String getValue() {
        return value;
    }

    /**
     * Resolve a wire value to its constant.
     *
     * @param value scope string as it appears in a {@code scope} parameter
     * @return the matching constant, or {@code null} when the value is unknown
     */
    public static OAuthScope fromValue(String value) {
        for (OAuthScope scope : values()) {
            if (scope.value.equals(value)) return scope;
        }
        return null;
    }
}
