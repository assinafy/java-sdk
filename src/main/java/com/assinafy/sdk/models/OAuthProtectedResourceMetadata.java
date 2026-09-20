package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

/**
 * RFC 9728 protected-resource metadata, served bare (no {@code {status, message, data}} envelope)
 * at {@code GET {apiOrigin}/.well-known/oauth-protected-resource}.
 *
 * <p>It identifies this API as a protected resource, names the authorization server allowed to
 * issue tokens for it, and lists the scopes it accepts:
 *
 * <pre>{@code
 * {
 *   "resource": "https://api.assinafy.com.br",
 *   "authorization_servers": ["https://auth.assinafy.com.br"],
 *   "scopes_supported": ["documents:read", "documents:write", "templates:read",
 *                        "templates:write", "account:read", "openid", "profile", "email"],
 *   "bearer_methods_supported": ["header"]
 * }
 * }</pre>
 *
 * <p>{@code offline_access} is deliberately absent: it is a request-time signal to the
 * authorization server, not a permission this API enforces. The document is also referenced from
 * the {@code WWW-Authenticate: Bearer resource_metadata="..."} challenge on a 401 or 403.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthProtectedResourceMetadata {

    @JsonProperty("resource")
    private String resource;

    @JsonProperty("authorization_servers")
    private List<String> authorizationServers;

    @JsonProperty("scopes_supported")
    private List<String> scopesSupported;

    @JsonProperty("bearer_methods_supported")
    private List<String> bearerMethodsSupported;

    /** Creates an empty metadata document. */
    public OAuthProtectedResourceMetadata() {}

    /** {@return the canonical resource identifier to send as the RFC 8707 {@code resource}} */
    public String getResource() { return resource; }

    /**
     * Sets the resource identifier.
     *
     * @param resource the canonical resource identifier
     */
    public void setResource(String resource) { this.resource = resource; }

    /** {@return the issuers allowed to mint tokens for this API, never {@code null}} */
    public List<String> getAuthorizationServers() {
        return authorizationServers != null ? authorizationServers : Collections.emptyList();
    }

    /**
     * Sets the authorization servers.
     *
     * @param authorizationServers issuers allowed to mint tokens for this API
     */
    public void setAuthorizationServers(List<String> authorizationServers) {
        this.authorizationServers = authorizationServers;
    }

    /** {@return the scopes this API accepts, never {@code null}} */
    public List<String> getScopesSupported() {
        return scopesSupported != null ? scopesSupported : Collections.emptyList();
    }

    /**
     * Sets the supported scopes.
     *
     * @param scopesSupported scopes this API accepts
     */
    public void setScopesSupported(List<String> scopesSupported) {
        this.scopesSupported = scopesSupported;
    }

    /** {@return how a token may be presented; Assinafy publishes {@code header} only} */
    public List<String> getBearerMethodsSupported() {
        return bearerMethodsSupported != null ? bearerMethodsSupported : Collections.emptyList();
    }

    /**
     * Sets the supported bearer methods.
     *
     * @param bearerMethodsSupported how a token may be presented
     */
    public void setBearerMethodsSupported(List<String> bearerMethodsSupported) {
        this.bearerMethodsSupported = bearerMethodsSupported;
    }
}
