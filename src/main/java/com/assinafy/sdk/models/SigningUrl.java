package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A direct signing URL generated for a single signer, as returned inside
 * {@code assignment.signing_urls}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SigningUrl {

    @JsonProperty("signer_id")
    private String signerId;

    @JsonProperty("url")
    private String url;

    public SigningUrl() {}

    public String getSignerId() { return signerId; }
    public void setSignerId(String signerId) { this.signerId = signerId; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
