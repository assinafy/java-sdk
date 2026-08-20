package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the webhook event type in an Assinafy API response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookEventTypeInfo {

    @JsonProperty("id")
    private String id;

    @JsonProperty("description")
    private String description;

    /**
     * Creates an empty webhook event type.
     */
    public WebhookEventTypeInfo() {}

    /**
     * Returns the ID.
     *
     * @return the ID
     */
    public String getId() { return id; }

    /**
     * Sets the ID.
     *
     * @param id the ID
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns the description.
     *
     * @return the description
     */
    public String getDescription() { return description; }

    /**
     * Sets the description.
     *
     * @param description the description
     */
    public void setDescription(String description) { this.description = description; }
}
