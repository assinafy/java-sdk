package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


/** A single-use embed session for an embedded signer. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmbedSession {

    /** Creates an empty response. */
    public EmbedSession() {}

    @JsonProperty("url")
    private String url;

    /** Load this URL in an iframe within 5 minutes. Opening it spends the token.
     * @return the url value, or null when absent
     */
    public String getUrl() { return url; }

    /** Sets url.
     * @param value the url value
     */
    public void setUrl(String value) { url = value; }

    @JsonProperty("expires_at")
    private String expiresAt;

    /** expires at
     * @return the expires_at value, or null when absent
     */
    public String getExpiresAt() { return expiresAt; }

    /** Sets expires_at.
     * @param value the expires_at value
     */
    public void setExpiresAt(String value) { expiresAt = value; }

}
