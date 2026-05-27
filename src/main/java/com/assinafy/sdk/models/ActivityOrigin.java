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

    public ActivityOrigin() {}

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
}
