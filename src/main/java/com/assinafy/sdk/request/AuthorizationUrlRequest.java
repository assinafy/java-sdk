package com.assinafy.sdk.request;

import com.assinafy.sdk.models.enums.OAuthScope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Inputs for building an OAuth consent URL.
 *
 * <p>Only {@code clientId}, {@code redirectUri} and {@code scopes} are required; the rest let you
 * skip discovery or supply your own randomness.
 *
 * <pre>{@code
 * AuthorizationUrlRequest request = AuthorizationUrlRequest.builder()
 *         .clientId(System.getenv("ASSINAFY_CLIENT_ID"))
 *         .redirectUri("https://myapp.com/oauth/callback")
 *         .scopes(OAuthScope.DOCUMENTS_READ, OAuthScope.DOCUMENTS_WRITE, OAuthScope.OFFLINE_ACCESS)
 *         .build();
 * }</pre>
 */
public class AuthorizationUrlRequest {

    private String clientId;
    private String redirectUri;
    private List<OAuthScope> scopes = new ArrayList<>();
    private String authorizationEndpoint;
    private String issuer;
    private String state;
    private String codeVerifier;
    private String nonce;
    private boolean nonceSuppressed;
    private String prompt;

    /** Creates an empty authorization-URL request. */
    public AuthorizationUrlRequest() {}

    /** {@return a new builder} */
    public static Builder builder() {
        return new Builder();
    }

    /** {@return the application's {@code client_id}} */
    public String getClientId() { return clientId; }

    /**
     * Sets the application's client ID.
     *
     * @param clientId the application's {@code client_id}
     */
    public void setClientId(String clientId) { this.clientId = clientId; }

    /** {@return the redirect URI the user returns to} */
    public String getRedirectUri() { return redirectUri; }

    /**
     * Sets the redirect URI.
     *
     * @param redirectUri one of the application's registered redirect URIs, matched character for
     *                    character; must be {@code https://} and carry no fragment
     */
    public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }

    /** {@return the permissions to request, never {@code null}} */
    public List<OAuthScope> getScopes() { return scopes; }

    /**
     * Sets the permissions to request.
     *
     * @param scopes permissions to request; {@code null} clears the list
     */
    public void setScopes(List<OAuthScope> scopes) {
        this.scopes = scopes != null ? new ArrayList<>(scopes) : new ArrayList<>();
    }

    /** {@return the authorization endpoint override, or {@code null} to discover it} */
    public String getAuthorizationEndpoint() { return authorizationEndpoint; }

    /**
     * Sets the authorization endpoint, skipping discovery.
     *
     * @param authorizationEndpoint the consent endpoint to use; {@code null} discovers it
     */
    public void setAuthorizationEndpoint(String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
    }

    /** {@return the issuer override, or {@code null} to discover it} */
    public String getIssuer() { return issuer; }

    /**
     * Sets the issuer to discover from, and the value the callback's {@code iss} must equal.
     *
     * @param issuer the issuer identifier; {@code null} discovers it
     */
    public void setIssuer(String issuer) { this.issuer = issuer; }

    /** {@return the caller-supplied {@code state}, or {@code null} to generate one} */
    public String getState() { return state; }

    /**
     * Sets a caller-supplied {@code state} instead of a generated one.
     *
     * @param state a value unique to this connection attempt
     */
    public void setState(String state) { this.state = state; }

    /** {@return the caller-supplied PKCE verifier, or {@code null} to generate one} */
    public String getCodeVerifier() { return codeVerifier; }

    /**
     * Sets a caller-supplied PKCE verifier instead of a generated one.
     *
     * @param codeVerifier 43-128 characters from {@code A-Z a-z 0-9 - . _ ~}
     */
    public void setCodeVerifier(String codeVerifier) { this.codeVerifier = codeVerifier; }

    /** {@return the caller-supplied OpenID Connect nonce, or {@code null} for the default} */
    public String getNonce() { return nonce; }

    /**
     * Sets a caller-supplied OpenID Connect nonce. By default a nonce is generated when the
     * {@code openid} scope is requested and omitted otherwise.
     *
     * @param nonce the nonce to echo in the {@code id_token}
     */
    public void setNonce(String nonce) { this.nonce = nonce; }

    /** {@return whether the nonce is suppressed even when {@code openid} is requested} */
    public boolean isNonceSuppressed() { return nonceSuppressed; }

    /**
     * Suppresses the nonce even when the {@code openid} scope is requested.
     *
     * @param nonceSuppressed {@code true} to omit the {@code nonce} parameter
     */
    public void setNonceSuppressed(boolean nonceSuppressed) { this.nonceSuppressed = nonceSuppressed; }

    /** {@return the OpenID Connect {@code prompt} value, or {@code null} to omit it} */
    public String getPrompt() { return prompt; }

    /**
     * Sets the OpenID Connect {@code prompt} parameter.
     *
     * @param prompt for example {@code consent} to force the approval screen again
     */
    public void setPrompt(String prompt) { this.prompt = prompt; }

    /** Fluent builder for {@link AuthorizationUrlRequest}. */
    public static final class Builder {
        private final AuthorizationUrlRequest req = new AuthorizationUrlRequest();

        /** Creates an empty builder. */
        public Builder() {}

        /**
         * Sets the application's client ID.
         *
         * @param clientId the application's {@code client_id}
         * @return this builder
         */
        public Builder clientId(String clientId) { req.setClientId(clientId); return this; }

        /**
         * Sets the redirect URI.
         *
         * @param redirectUri a registered {@code https://} redirect URI without a fragment
         * @return this builder
         */
        public Builder redirectUri(String redirectUri) { req.setRedirectUri(redirectUri); return this; }

        /**
         * Sets the permissions to request.
         *
         * @param scopes permissions to request
         * @return this builder
         */
        public Builder scopes(OAuthScope... scopes) {
            req.setScopes(scopes != null ? Arrays.asList(scopes) : null);
            return this;
        }

        /**
         * Sets the permissions to request.
         *
         * @param scopes permissions to request
         * @return this builder
         */
        public Builder scopes(List<OAuthScope> scopes) { req.setScopes(scopes); return this; }

        /**
         * Sets the authorization endpoint, skipping discovery.
         *
         * @param authorizationEndpoint the consent endpoint to use
         * @return this builder
         */
        public Builder authorizationEndpoint(String authorizationEndpoint) {
            req.setAuthorizationEndpoint(authorizationEndpoint);
            return this;
        }

        /**
         * Sets the issuer to discover from and to expect on the callback.
         *
         * @param issuer the issuer identifier
         * @return this builder
         */
        public Builder issuer(String issuer) { req.setIssuer(issuer); return this; }

        /**
         * Sets a caller-supplied {@code state}.
         *
         * @param state a value unique to this connection attempt
         * @return this builder
         */
        public Builder state(String state) { req.setState(state); return this; }

        /**
         * Sets a caller-supplied PKCE verifier.
         *
         * @param codeVerifier 43-128 characters from {@code A-Z a-z 0-9 - . _ ~}
         * @return this builder
         */
        public Builder codeVerifier(String codeVerifier) { req.setCodeVerifier(codeVerifier); return this; }

        /**
         * Sets a caller-supplied OpenID Connect nonce.
         *
         * @param nonce the nonce to echo in the {@code id_token}
         * @return this builder
         */
        public Builder nonce(String nonce) { req.setNonce(nonce); return this; }

        /**
         * Suppresses the nonce even when the {@code openid} scope is requested.
         *
         * @return this builder
         */
        public Builder withoutNonce() { req.setNonceSuppressed(true); return this; }

        /**
         * Sets the OpenID Connect {@code prompt} parameter.
         *
         * @param prompt for example {@code consent}
         * @return this builder
         */
        public Builder prompt(String prompt) { req.setPrompt(prompt); return this; }

        /** {@return the configured authorization-URL request} */
        public AuthorizationUrlRequest build() { return req; }
    }
}
