package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request payload for registering a webhook.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterWebhookRequest {

    @JsonProperty("url")
    private String url;

    @JsonProperty("email")
    private String email;

    @JsonProperty("events")
    private List<String> events;

    @JsonProperty("is_active")
    private Boolean isActive;

    /**
     * Creates an empty webhook-registration request.
     */
    public RegisterWebhookRequest() {}

    /**
     * Creates a builder for a webhook-registration request.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

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
     * Builder for {@link RegisterWebhookRequest}.
     */
    public static final class Builder {
        private final RegisterWebhookRequest req = new RegisterWebhookRequest();

        /** Creates an empty builder. */
        public Builder() {}

        /**
         * Sets the URL for the object being built.
         *
         * @param url the URL
         * @return this builder
         */
        public Builder url(String url) { req.setUrl(url); return this; }

        /**
         * Sets the email address for the object being built.
         *
         * @param email the email address
         * @return this builder
         */
        public Builder email(String email) { req.setEmail(email); return this; }

        /**
         * Sets the events for the object being built.
         *
         * @param events the events
         * @return this builder
         */
        public Builder events(List<String> events) { req.setEvents(events); return this; }

        /**
         * Sets the active flag for the object being built.
         *
         * @param isActive the active flag
         * @return this builder
         */
        public Builder isActive(boolean isActive) { req.setIsActive(isActive); return this; }

        /**
         * Builds the configured webhook-registration request.
         *
         * @return the configured webhook-registration request
         */
        public RegisterWebhookRequest build() { return req; }
    }
}
