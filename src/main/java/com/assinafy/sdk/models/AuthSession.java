package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * JWT access token, authenticated user, and account memberships returned by login operations.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthSession {

    @JsonProperty("access_token")
    private String accessToken;
    private AuthUser user;
    private List<AuthAccount> accounts;

    /**
     * Creates an empty authentication session.
     */
    public AuthSession() {}

    /**
     * Returns the access token.
     *
     * @return the access token
     */
    public String getAccessToken() { return accessToken; }

    /**
     * Sets the access token.
     *
     * @param accessToken the access token
     */
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    /**
     * Returns the user.
     *
     * @return the user
     */
    public AuthUser getUser() { return user; }

    /**
     * Sets the user.
     *
     * @param user the user
     */
    public void setUser(AuthUser user) { this.user = user; }

    /**
     * Returns the authenticated user's account memberships.
     *
     * @return accessible account memberships
     */
    public List<AuthAccount> getAccounts() { return accounts; }

    /**
     * Sets the authenticated user's account memberships.
     *
     * @param accounts accessible account memberships
     */
    public void setAccounts(List<AuthAccount> accounts) { this.accounts = accounts; }
}
