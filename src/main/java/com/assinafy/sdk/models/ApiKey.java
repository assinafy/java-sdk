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

    /**
     * Creates an empty API key.
     */
    public ApiKey() {}

    /**
     * Returns the API key.
     *
     * @return the API key
     */
    public String getApiKey() { return apiKey; }

    /**
     * Sets the API key.
     *
     * @param apiKey the API key
     */
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
}
