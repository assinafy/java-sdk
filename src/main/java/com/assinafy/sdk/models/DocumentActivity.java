package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the document activity in an Assinafy API response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentActivity {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("event")
    private String event;

    @JsonProperty("message")
    private String message;

    /** Event-specific payload. May be an object or an (empty) array, so it is left untyped. */
    @JsonProperty("payload")
    private Object payload;

    /** Originating IP / user agent, or {@code null} when not recorded. */
    @JsonProperty("origin")
    private ActivityOrigin origin;

    @JsonProperty("created_at")
    private String createdAt;

    /**
     * Creates an empty document activity.
     */
    public DocumentActivity() {}

    /**
     * Returns the ID.
     *
     * @return the ID
     */
    public Long getId() { return id; }

    /**
     * Sets the ID.
     *
     * @param id the ID
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Returns the event.
     *
     * @return the event
     */
    public String getEvent() { return event; }

    /**
     * Sets the event.
     *
     * @param event the event
     */
    public void setEvent(String event) { this.event = event; }

    /**
     * Returns the message.
     *
     * @return the message
     */
    public String getMessage() { return message; }

    /**
     * Sets the message.
     *
     * @param message the message
     */
    public void setMessage(String message) { this.message = message; }

    /**
     * Returns the payload.
     *
     * @return the payload
     */
    public Object getPayload() { return payload; }

    /**
     * Sets the payload.
     *
     * @param payload the payload
     */
    public void setPayload(Object payload) { this.payload = payload; }

    /**
     * Returns the origin.
     *
     * @return the origin
     */
    public ActivityOrigin getOrigin() { return origin; }

    /**
     * Sets the origin.
     *
     * @param origin the origin
     */
    public void setOrigin(ActivityOrigin origin) { this.origin = origin; }

    /**
     * Returns the creation timestamp.
     *
     * @return the creation timestamp
     */
    public String getCreatedAt() { return createdAt; }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt the creation timestamp
     */
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
