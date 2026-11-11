package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


/** Signing secret issued for a webhook endpoint. Store securely; never log it. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookEndpointSecret {

    /** Creates an empty response. */
    public WebhookEndpointSecret() {}

    @JsonProperty("secret")
    private String secret;

    /** Standard Webhooks secret: `whsec_` followed by the base64-encoded key.
     * @return the secret value, or null when absent
     */
    public String getSecret() { return secret; }

    /** Sets secret.
     * @param value the secret value
     */
    public void setSecret(String value) { secret = value; }

    @JsonProperty("previous_secret_expires_at")
    private String previousSecretExpiresAt;

    /** When the previous secret stops signing; only returned with keep_previous enabled.
     * @return the previous_secret_expires_at value, or null when absent
     */
    public String getPreviousSecretExpiresAt() { return previousSecretExpiresAt; }

    /** Sets previous_secret_expires_at.
     * @param value the previous_secret_expires_at value
     */
    public void setPreviousSecretExpiresAt(String value) { previousSecretExpiresAt = value; }

}
