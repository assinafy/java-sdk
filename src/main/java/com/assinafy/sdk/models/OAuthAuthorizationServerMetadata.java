package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

/**
 * RFC 8414 authorization-server metadata, served bare (no {@code {status, message, data}} envelope)
 * at {@code GET {issuer}/.well-known/oauth-authorization-server}.
 *
 * <p>Every endpoint URL an OAuth client needs comes from here, so nothing has to be hardcoded. The
 * document is published by the authorization server — a different host from this API:
 *
 * <pre>{@code
 * {
 *   "issuer": "https://auth.assinafy.com.br",
 *   "authorization_endpoint": "https://auth.assinafy.com.br/oauth/authorize",
 *   "token_endpoint": "https://api.assinafy.com.br/v1/oauth/token",
 *   "revocation_endpoint": "https://api.assinafy.com.br/v1/oauth/revoke",
 *   "userinfo_endpoint": "https://api.assinafy.com.br/v1/oauth/userinfo",
 *   "jwks_uri": "https://auth.assinafy.com.br/.well-known/jwks.json",
 *   "scopes_supported": ["documents:read", "documents:write", "templates:read",
 *                        "templates:write", "account:read", "openid", "profile",
 *                        "email", "offline_access"],
 *   "response_types_supported": ["code"],
 *   "grant_types_supported": ["authorization_code", "refresh_token"],
 *   "code_challenge_methods_supported": ["S256"],
 *   "token_endpoint_auth_methods_supported": ["client_secret_post", "none"],
 *   "authorization_response_iss_parameter_supported": true,
 *   "client_id_metadata_document_supported": true
 * }
 * }</pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthAuthorizationServerMetadata {

    @JsonProperty("issuer")
    private String issuer;

    @JsonProperty("authorization_endpoint")
    private String authorizationEndpoint;

    @JsonProperty("token_endpoint")
    private String tokenEndpoint;

    @JsonProperty("revocation_endpoint")
    private String revocationEndpoint;

    @JsonProperty("userinfo_endpoint")
    private String userinfoEndpoint;

    @JsonProperty("jwks_uri")
    private String jwksUri;

    @JsonProperty("scopes_supported")
    private List<String> scopesSupported;

    @JsonProperty("response_types_supported")
    private List<String> responseTypesSupported;

    @JsonProperty("grant_types_supported")
    private List<String> grantTypesSupported;

    @JsonProperty("code_challenge_methods_supported")
    private List<String> codeChallengeMethodsSupported;

    @JsonProperty("token_endpoint_auth_methods_supported")
    private List<String> tokenEndpointAuthMethodsSupported;

    @JsonProperty("authorization_response_iss_parameter_supported")
    private Boolean authorizationResponseIssParameterSupported;

    @JsonProperty("client_id_metadata_document_supported")
    private Boolean clientIdMetadataDocumentSupported;

    /** Creates an empty metadata document. */
    public OAuthAuthorizationServerMetadata() {}

    /** {@return the issuer identifier, which the callback's {@code iss} must equal} */
    public String getIssuer() { return issuer; }

    /**
     * Sets the issuer identifier.
     *
     * @param issuer the issuer identifier
     */
    public void setIssuer(String issuer) { this.issuer = issuer; }

    /** {@return the browser-facing consent endpoint} */
    public String getAuthorizationEndpoint() { return authorizationEndpoint; }

    /**
     * Sets the authorization endpoint.
     *
     * @param authorizationEndpoint the browser-facing consent endpoint
     */
    public void setAuthorizationEndpoint(String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
    }

    /** {@return the token endpoint, which lives on the API host rather than the issuer} */
    public String getTokenEndpoint() { return tokenEndpoint; }

    /**
     * Sets the token endpoint.
     *
     * @param tokenEndpoint the token endpoint
     */
    public void setTokenEndpoint(String tokenEndpoint) { this.tokenEndpoint = tokenEndpoint; }

    /** {@return the token revocation endpoint} */
    public String getRevocationEndpoint() { return revocationEndpoint; }

    /**
     * Sets the revocation endpoint.
     *
     * @param revocationEndpoint the token revocation endpoint
     */
    public void setRevocationEndpoint(String revocationEndpoint) {
        this.revocationEndpoint = revocationEndpoint;
    }

    /** {@return the OpenID Connect userinfo endpoint} */
    public String getUserinfoEndpoint() { return userinfoEndpoint; }

    /**
     * Sets the userinfo endpoint.
     *
     * @param userinfoEndpoint the OpenID Connect userinfo endpoint
     */
    public void setUserinfoEndpoint(String userinfoEndpoint) { this.userinfoEndpoint = userinfoEndpoint; }

    /** {@return the JWKS document used to validate an {@code id_token} signature} */
    public String getJwksUri() { return jwksUri; }

    /**
     * Sets the JWKS URI.
     *
     * @param jwksUri the JWKS document URI
     */
    public void setJwksUri(String jwksUri) { this.jwksUri = jwksUri; }

    /** {@return the scopes the authorization server accepts, never {@code null}} */
    public List<String> getScopesSupported() {
        return scopesSupported != null ? scopesSupported : Collections.emptyList();
    }

    /**
     * Sets the supported scopes.
     *
     * @param scopesSupported scopes the authorization server accepts
     */
    public void setScopesSupported(List<String> scopesSupported) { this.scopesSupported = scopesSupported; }

    /** {@return the supported response types; Assinafy publishes {@code code} only} */
    public List<String> getResponseTypesSupported() {
        return responseTypesSupported != null ? responseTypesSupported : Collections.emptyList();
    }

    /**
     * Sets the supported response types.
     *
     * @param responseTypesSupported supported response types
     */
    public void setResponseTypesSupported(List<String> responseTypesSupported) {
        this.responseTypesSupported = responseTypesSupported;
    }

    /** {@return the supported grant types: {@code authorization_code} and {@code refresh_token}} */
    public List<String> getGrantTypesSupported() {
        return grantTypesSupported != null ? grantTypesSupported : Collections.emptyList();
    }

    /**
     * Sets the supported grant types.
     *
     * @param grantTypesSupported supported grant types
     */
    public void setGrantTypesSupported(List<String> grantTypesSupported) {
        this.grantTypesSupported = grantTypesSupported;
    }

    /** {@return the supported PKCE challenge methods; Assinafy publishes {@code S256} only} */
    public List<String> getCodeChallengeMethodsSupported() {
        return codeChallengeMethodsSupported != null ? codeChallengeMethodsSupported : Collections.emptyList();
    }

    /**
     * Sets the supported PKCE challenge methods.
     *
     * @param codeChallengeMethodsSupported supported PKCE challenge methods
     */
    public void setCodeChallengeMethodsSupported(List<String> codeChallengeMethodsSupported) {
        this.codeChallengeMethodsSupported = codeChallengeMethodsSupported;
    }

    /**
     * {@return how a client authenticates at the token endpoint: {@code client_secret_post} for a
     * confidential application, {@code none} for a public one}
     */
    public List<String> getTokenEndpointAuthMethodsSupported() {
        return tokenEndpointAuthMethodsSupported != null
                ? tokenEndpointAuthMethodsSupported : Collections.emptyList();
    }

    /**
     * Sets the supported token-endpoint authentication methods.
     *
     * @param tokenEndpointAuthMethodsSupported supported authentication methods
     */
    public void setTokenEndpointAuthMethodsSupported(List<String> tokenEndpointAuthMethodsSupported) {
        this.tokenEndpointAuthMethodsSupported = tokenEndpointAuthMethodsSupported;
    }

    /** {@return whether the server sends the RFC 9207 {@code iss} parameter; Assinafy always does} */
    public Boolean getAuthorizationResponseIssParameterSupported() {
        return authorizationResponseIssParameterSupported;
    }

    /**
     * Sets the RFC 9207 support flag.
     *
     * @param supported whether the server sends the {@code iss} parameter
     */
    public void setAuthorizationResponseIssParameterSupported(Boolean supported) {
        this.authorizationResponseIssParameterSupported = supported;
    }

    /** {@return whether a client may identify itself with a client-ID metadata document} */
    public Boolean getClientIdMetadataDocumentSupported() { return clientIdMetadataDocumentSupported; }

    /**
     * Sets the client-ID metadata document support flag.
     *
     * @param supported whether client-ID metadata documents are accepted
     */
    public void setClientIdMetadataDocumentSupported(Boolean supported) {
        this.clientIdMetadataDocumentSupported = supported;
    }
}
