package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An API key as returned by the {@code /users/api-keys} endpoints. {@code GET} returns a
 * masked value (only the last four characters are visible); {@code POST} returns the full,
 * freshly generated key exactly once.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiKey {

    @JsonProperty("api_key")
    private String apiKey;

    public ApiKey() {}

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
}
