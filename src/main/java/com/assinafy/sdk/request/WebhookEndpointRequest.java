package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request payload for creating or updating a webhook endpoint. Only non-null fields are sent, so
 * an update changes exactly the fields that were set.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebhookEndpointRequest {

    @JsonProperty("url")
    private String url;

    @JsonProperty("email")
    private String email;

    @JsonProperty("events")
    private List<String> events;

    @JsonProperty("name")
    private String name;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("signing_enabled")
    private Boolean signingEnabled;

    /**
     * Creates an empty webhook-endpoint request.
     */
    public WebhookEndpointRequest() {}

    /**
     * Creates a builder for a webhook-endpoint request.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the URL that receives the events.
     *
     * @return the URL
     */
    public String getUrl() { return url; }

    /**
     * Sets the URL that receives the events.
     *
     * @param url an absolute HTTP(S) URL
     */
    public void setUrl(String url) { this.url = url; }

    /**
     * Returns the contact email for delivery-failure notices.
     *
     * @return the email address
     */
    public String getEmail() { return email; }

    /**
     * Sets the contact email for delivery-failure notices.
     *
     * @param email the email address
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Returns the event types to deliver.
     *
     * @return the event types
     */
    public List<String> getEvents() { return events; }

    /**
     * Sets the event types to deliver.
     *
     * @param events the event types
     */
    public void setEvents(List<String> events) { this.events = events; }

    /**
     * Returns the label that tells endpoints apart.
     *
     * @return the label
     */
    public String getName() { return name; }

    /**
     * Sets the label that tells endpoints apart.
     *
     * @param name the label
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns whether events are delivered.
     *
     * @return the active flag
     */
    public Boolean getIsActive() { return isActive; }

    /**
     * Sets whether events are delivered.
     *
     * @param isActive the active flag
     */
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    /**
     * Returns whether deliveries are signed.
     *
     * @return the signing flag
     */
    public Boolean getSigningEnabled() { return signingEnabled; }

    /**
     * Sets whether deliveries are signed.
     *
     * @param signingEnabled the signing flag
     */
    public void setSigningEnabled(Boolean signingEnabled) { this.signingEnabled = signingEnabled; }

    /**
     * Builder for {@link WebhookEndpointRequest}.
     */
    public static final class Builder {
        private final WebhookEndpointRequest req = new WebhookEndpointRequest();

        /** Creates an empty builder. */
        public Builder() {}

        /**
         * Sets the URL that receives the events.
         *
         * @param url an absolute HTTP(S) URL, unique within the workspace
         * @return this builder
         */
        public Builder url(String url) { req.setUrl(url); return this; }

        /**
         * Sets the contact email for delivery-failure notices.
         *
         * @param email the email address
         * @return this builder
         */
        public Builder email(String email) { req.setEmail(email); return this; }

        /**
         * Sets the event types to deliver.
         *
         * @param events the event types (see {@code WebhookResource.listEventTypes()})
         * @return this builder
         */
        public Builder events(List<String> events) { req.setEvents(events); return this; }

        /**
         * Sets the label that tells endpoints apart.
         *
         * @param name the label
         * @return this builder
         */
        public Builder name(String name) { req.setName(name); return this; }

        /**
         * Sets whether events are delivered.
         *
         * @param isActive the active flag
         * @return this builder
         */
        public Builder isActive(boolean isActive) { req.setIsActive(isActive); return this; }

        /**
         * Sets whether deliveries carry a Standard Webhooks signature.
         *
         * @param signingEnabled {@code true} to sign deliveries; {@code false} discards the secret
         * @return this builder
         */
        public Builder signingEnabled(boolean signingEnabled) { req.setSigningEnabled(signingEnabled); return this; }

        /**
         * Builds the configured webhook-endpoint request.
         *
         * @return the configured webhook-endpoint request
         */
        public WebhookEndpointRequest build() { return req; }
    }
}
