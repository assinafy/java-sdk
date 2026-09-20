package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OpenID Connect claims about the user who authorized an OAuth token, returned by
 * {@code GET /oauth/userinfo}.
 *
 * <p>Per OIDC Core §5.3.2 the response is a flat claims object, <em>not</em> this API's usual
 * {@code {status, message, data}} envelope:
 *
 * <pre>{@code
 * {
 *   "sub": "d6zqpbyog2v3xvxerwn8la94",
 *   "name": "Maria Silva",
 *   "email": "maria@example.com",
 *   "email_verified": true
 * }
 * }</pre>
 *
 * <p>{@link #getSub()} always present; the endpoint itself requires the {@code openid} scope.
 * {@link #getName()} requires {@code profile}, and {@link #getEmail()} plus
 * {@link #getEmailVerified()} require {@code email}; an ungranted claim comes back {@code null}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthUserInfo {

    @JsonProperty("sub")
    private String sub;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("email_verified")
    private Boolean emailVerified;

    /** Creates an empty claims object. */
    public OAuthUserInfo() {}

    /** {@return the user's stable identifier} */
    public String getSub() { return sub; }

    /**
     * Sets the subject identifier.
     *
     * @param sub the user's stable identifier
     */
    public void setSub(String sub) { this.sub = sub; }

    /** {@return the user's name, or {@code null} without the {@code profile} scope} */
    public String getName() { return name; }

    /**
     * Sets the user's name.
     *
     * @param name the user's name
     */
    public void setName(String name) { this.name = name; }

    /** {@return the user's email address, or {@code null} without the {@code email} scope} */
    public String getEmail() { return email; }

    /**
     * Sets the user's email address.
     *
     * @param email the user's email address
     */
    public void setEmail(String email) { this.email = email; }

    /** {@return whether the email is verified, or {@code null} without the {@code email} scope} */
    public Boolean getEmailVerified() { return emailVerified; }

    /**
     * Sets the email verification flag.
     *
     * @param emailVerified whether the email address is verified
     */
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
}
