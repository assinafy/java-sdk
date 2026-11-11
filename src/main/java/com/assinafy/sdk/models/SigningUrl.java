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

    /**
     * Creates an empty signing url.
     */
    public SigningUrl() {}

    /**
     * Returns the signer ID.
     *
     * @return the signer ID
     */
    public String getSignerId() { return signerId; }

    /**
     * Sets the signer ID.
     *
     * @param signerId the signer ID
     */
    public void setSignerId(String signerId) { this.signerId = signerId; }

    /**
     * Returns the URL.
     *
     * @return the URL
     */
    public String getUrl() { return url; }

    /**
     * Sets the URL.
     *
     * @param url the URL
     */
    public void setUrl(String url) { this.url = url; }
    @JsonProperty("embedded")
    private Boolean embedded;

    /** Whether the signer uses an embed session; url is null when true.
     * @return the embedded value, or null when absent
     */
    public Boolean getEmbedded() { return embedded; }

    /** Sets embedded.
     * @param value the embedded value
     */
    public void setEmbedded(Boolean value) { embedded = value; }

}
