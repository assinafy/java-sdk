package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Origin metadata (IP address and user agent) captured for a document activity or
 * webhook event. {@code null} when the originating request context is not recorded.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActivityOrigin {

    @JsonProperty("ip")
    private String ip;

    @JsonProperty("user-agent")
    private String userAgent;

    /**
     * Creates an empty activity origin.
     */
    public ActivityOrigin() {}

    /**
     * Returns the IP address.
     *
     * @return the IP address
     */
    public String getIp() { return ip; }

    /**
     * Sets the IP address.
     *
     * @param ip the IP address
     */
    public void setIp(String ip) { this.ip = ip; }

    /**
     * Returns the user agent.
     *
     * @return the user agent
     */
    public String getUserAgent() { return userAgent; }

    /**
     * Sets the user agent.
     *
     * @param userAgent the user agent
     */
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
}
