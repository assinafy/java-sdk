package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * The JSON body delivered to a webhook endpoint when a subscribed event fires. Mirrors the
 * documented common envelope: {@code id}, {@code event}, {@code message}, {@code payload},
 * {@code origin}, {@code created_at}, {@code subject}, {@code object} and {@code account_id}.
 *
 * <p>{@code subject} (the actor that triggered the event) and {@code object} (the entity the
 * event is about) are polymorphic — each carries a {@code type} discriminator — and are
 * therefore exposed as {@code Map<String, Object>}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookPayload {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("event")
    private String event;

    @JsonProperty("message")
    private String message;

    /**
     * Event-specific extra data. Stored untyped because, while it is usually an object or
     * {@code null}, some event types deliver an empty array ({@code []}) — binding that to a
     * {@code Map} would fail the whole parse. {@link #getPayload()} exposes the common
     * object-shaped case as a map (returning {@code null} for the array case).
     */
    @JsonProperty("payload")
    private Object payloadRaw;

    /** Originating IP / user agent of the request that triggered the event. */
    @JsonProperty("origin")
    private Map<String, Object> origin;

    /** Event creation time as a Unix timestamp (seconds). */
    @JsonProperty("created_at")
    private Long createdAt;

    /** The actor that triggered the event (carries a {@code type} discriminator). */
    @JsonProperty("subject")
    private Map<String, Object> subject;

    /** The entity the event is about (carries a {@code type} discriminator). */
    @JsonProperty("object")
    private Map<String, Object> object;

    @JsonProperty("account_id")
    private String accountId;

    private final Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * Creates an empty webhook payload.
     */
    public WebhookPayload() {}

    /**
     * Returns the additional properties.
     *
     * @return the additional properties
     */
    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    /**
     * Sets the additional property.
     *
     * @param name the name
     * @param value the additional property
     */
    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        additionalProperties.put(name, value);
    }

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
     * The {@code payload} when it is object-shaped, otherwise {@code null} (e.g. an empty array).
     *
     * @return the payload
     */
    @JsonIgnore
    @SuppressWarnings("unchecked")
    public Map<String, Object> getPayload() {
        return payloadRaw instanceof Map ? (Map<String, Object>) payloadRaw : null;
    }

    /**
     * Sets the payload.
     *
     * @param payload the payload
     */
    @JsonIgnore
    public void setPayload(Map<String, Object> payload) { this.payloadRaw = payload; }

    /**
     * The raw {@code payload} exactly as delivered: an object, an array, or {@code null}.
     *
     * @return the raw webhook payload
     */
    @JsonIgnore
    public Object getPayloadRaw() { return payloadRaw; }

    /**
     * Returns the origin.
     *
     * @return the origin
     */
    public Map<String, Object> getOrigin() { return origin; }

    /**
     * Sets the origin.
     *
     * @param origin the origin
     */
    public void setOrigin(Map<String, Object> origin) { this.origin = origin; }

    /**
     * Returns the creation timestamp.
     *
     * @return the creation timestamp
     */
    public Long getCreatedAt() { return createdAt; }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt the creation timestamp
     */
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    /**
     * Returns the subject.
     *
     * @return the subject
     */
    public Map<String, Object> getSubject() { return subject; }

    /**
     * Sets the subject.
     *
     * @param subject the subject
     */
    public void setSubject(Map<String, Object> subject) { this.subject = subject; }

    /**
     * Returns the object.
     *
     * @return the object
     */
    public Map<String, Object> getObject() { return object; }

    /**
     * Sets the object.
     *
     * @param object the object
     */
    public void setObject(Map<String, Object> object) { this.object = object; }

    /**
     * Returns the account ID.
     *
     * @return the account ID
     */
    public String getAccountId() { return accountId; }

    /**
     * Sets the account ID.
     *
     * @param accountId the account ID
     */
    public void setAccountId(String accountId) { this.accountId = accountId; }
}
