package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents the webhook subscription in an Assinafy API response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookSubscription {

    @JsonProperty("url")
    private String url;

    @JsonProperty("email")
    private String email;

    @JsonProperty("events")
    private List<String> events;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("updated_at")
    private String updatedAt;

    /**
     * Creates an empty webhook subscription.
     */
    public WebhookSubscription() {}

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

    /**
     * Returns the email address.
     *
     * @return the email address
     */
    public String getEmail() { return email; }

    /**
     * Sets the email address.
     *
     * @param email the email address
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Returns the events.
     *
     * @return the events
     */
    public List<String> getEvents() { return events; }

    /**
     * Sets the events.
     *
     * @param events the events
     */
    public void setEvents(List<String> events) { this.events = events; }

    /**
     * Returns the active flag.
     *
     * @return the active flag
     */
    public Boolean getIsActive() { return isActive; }

    /**
     * Sets the active flag.
     *
     * @param isActive the active flag
     */
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    /**
     * Returns the last-update timestamp.
     *
     * @return the last-update timestamp
     */
    public String getUpdatedAt() { return updatedAt; }

    /**
     * Sets the last-update timestamp.
     *
     * @param updatedAt the last-update timestamp
     */
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
