package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The token set returned by {@code POST /oauth/token} for both the {@code authorization_code} and
 * {@code refresh_token} grants.
 *
 * <p>Per RFC 6749 §5.1 this is a flat JSON object, <em>not</em> the {@code {status, message, data}}
 * envelope the rest of the API uses:
 *
 * <pre>{@code
 * {
 *   "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "token_type": "Bearer",
 *   "expires_in": 3600,
 *   "scope": "documents:read documents:write",
 *   "refresh_token": "def50200f1e2...",
 *   "id_token": "eyJraWQiOiJEQlR0S0..."
 * }
 * }</pre>
 *
 * <p>Read {@link #getScope()} rather than assuming every requested permission was granted, and
 * persist {@link #getRefreshToken()} <em>before</em> doing anything else with the response: refresh
 * tokens rotate, and replaying a retired one ends the whole connection.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthTokens {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("id_token")
    private String idToken;

    /** Creates an empty token set. */
    public OAuthTokens() {}

    /** {@return the bearer access token to send as {@code Authorization: Bearer ...}} */
    public String getAccessToken() { return accessToken; }

    /**
     * Sets the access token.
     *
     * @param accessToken the bearer access token
     */
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    /** {@return the token type, always {@code Bearer}} */
    public String getTokenType() { return tokenType; }

    /**
     * Sets the token type.
     *
     * @param tokenType the token type
     */
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    /** {@return the access token's lifetime in seconds, normally {@code 3600}} */
    public Integer getExpiresIn() { return expiresIn; }

    /**
     * Sets the access-token lifetime.
     *
     * @param expiresIn lifetime in seconds
     */
    public void setExpiresIn(Integer expiresIn) { this.expiresIn = expiresIn; }

    /**
     * {@return the refresh token, or {@code null} when {@code offline_access} was not requested
     * and consented}
     */
    public String getRefreshToken() { return refreshToken; }

    /**
     * Sets the refresh token.
     *
     * @param refreshToken the refresh token
     */
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    /**
     * {@return the space-separated scopes actually granted to this access token;
     * {@code offline_access} never appears here even when it was requested}
     */
    public String getScope() { return scope; }

    /**
     * Sets the granted scopes.
     *
     * @param scope space-separated granted scopes
     */
    public void setScope(String scope) { this.scope = scope; }

    /**
     * {@return the signed OpenID Connect {@code id_token} (RS256), or {@code null} when the
     * {@code openid} scope was not granted}
     */
    public String getIdToken() { return idToken; }

    /**
     * Sets the OpenID Connect ID token.
     *
     * @param idToken the signed {@code id_token}
     */
    public void setIdToken(String idToken) { this.idToken = idToken; }
}
