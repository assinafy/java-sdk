package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An unconfirmed authenticator-app enrollment. The {@code secret} is returned only once and
 * cannot be retrieved again; two-factor authentication is not active until the enrollment is
 * confirmed.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TotpEnrollment {

    @JsonProperty("id")
    private String id;

    @JsonProperty("secret")
    private String secret;

    @JsonProperty("provisioning_uri")
    private String provisioningUri;

    /**
     * Creates an empty instance.
     */
    public TotpEnrollment() {}

    /**
     * Returns the method ID to pass when confirming.
     *
     * @return the method ID to pass when confirming
     */
    public String getId() { return id; }

    /**
     * Sets the method ID to pass when confirming.
     *
     * @param id the method ID to pass when confirming
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns the shared TOTP secret.
     *
     * @return the shared TOTP secret
     */
    public String getSecret() { return secret; }

    /**
     * Sets the shared TOTP secret.
     *
     * @param secret the shared TOTP secret
     */
    public void setSecret(String secret) { this.secret = secret; }

    /**
     * Returns the {@code otpauth://} URI to render as a QR code.
     *
     * @return the {@code otpauth://} URI to render as a QR code
     */
    public String getProvisioningUri() { return provisioningUri; }

    /**
     * Sets the {@code otpauth://} URI to render as a QR code.
     *
     * @param provisioningUri the {@code otpauth://} URI to render as a QR code
     */
    public void setProvisioningUri(String provisioningUri) { this.provisioningUri = provisioningUri; }
}
