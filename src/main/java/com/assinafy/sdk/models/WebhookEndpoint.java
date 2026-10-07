package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A URL that receives the account's webhook events. An account has 1 endpoint, or up to 3 on
 * paid plans; every active endpoint subscribed to an event receives it.
 *
 * <p>When {@link #getSigningEnabled()} is {@code true}, deliveries carry Standard Webhooks
 * {@code webhook-signature} headers; verify them with
 * {@link com.assinafy.sdk.util.WebhookSignature}.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookEndpoint {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("url")
    private String url;

    @JsonProperty("email")
    private String email;

    @JsonProperty("events")
    private List<String> events;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("signing_enabled")
    private Boolean signingEnabled;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    /**
     * Creates an empty instance.
     */
    public WebhookEndpoint() {}

    /**
     * Returns the endpoint ID.
     *
     * @return the endpoint ID
     */
    public String getId() { return id; }

    /**
     * Sets the endpoint ID.
     *
     * @param id the endpoint ID
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns the label that tells endpoints apart, or {@code null}.
     *
     * @return the label that tells endpoints apart, or {@code null}
     */
    public String getName() { return name; }

    /**
     * Sets the label that tells endpoints apart, or {@code null}.
     *
     * @param name the label that tells endpoints apart, or {@code null}
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns the URL that receives the events.
     *
     * @return the URL that receives the events
     */
    public String getUrl() { return url; }

    /**
     * Sets the URL that receives the events.
     *
     * @param url the URL that receives the events
     */
    public void setUrl(String url) { this.url = url; }

    /**
     * Returns the contact email for delivery-failure notices.
     *
     * @return the contact email for delivery-failure notices
     */
    public String getEmail() { return email; }

    /**
     * Sets the contact email for delivery-failure notices.
     *
     * @param email the contact email for delivery-failure notices
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Returns the event types delivered to this endpoint.
     *
     * @return the event types delivered to this endpoint
     */
    public List<String> getEvents() { return events; }

    /**
     * Sets the event types delivered to this endpoint.
     *
     * @param events the event types delivered to this endpoint
     */
    public void setEvents(List<String> events) { this.events = events; }

    /**
     * Returns whether events are delivered to this endpoint.
     *
     * @return whether events are delivered to this endpoint
     */
    public Boolean getIsActive() { return isActive; }

    /**
     * Sets whether events are delivered to this endpoint.
     *
     * @param isActive whether events are delivered to this endpoint
     */
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    /**
     * Returns whether deliveries are signed.
     *
     * @return whether deliveries are signed
     */
    public Boolean getSigningEnabled() { return signingEnabled; }

    /**
     * Sets whether deliveries are signed.
     *
     * @param signingEnabled whether deliveries are signed
     */
    public void setSigningEnabled(Boolean signingEnabled) { this.signingEnabled = signingEnabled; }

    /**
     * Returns the creation timestamp (ISO 8601).
     *
     * @return the creation timestamp (ISO 8601)
     */
    public String getCreatedAt() { return createdAt; }

    /**
     * Sets the creation timestamp (ISO 8601).
     *
     * @param createdAt the creation timestamp (ISO 8601)
     */
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    /**
     * Returns the last-update timestamp (ISO 8601).
     *
     * @return the last-update timestamp (ISO 8601)
     */
    public String getUpdatedAt() { return updatedAt; }

    /**
     * Sets the last-update timestamp (ISO 8601).
     *
     * @param updatedAt the last-update timestamp (ISO 8601)
     */
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
