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

    public WebhookPayload() {}

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        additionalProperties.put(name, value);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    /** The {@code payload} when it is object-shaped, otherwise {@code null} (e.g. an empty array). */
    @JsonIgnore
    @SuppressWarnings("unchecked")
    public Map<String, Object> getPayload() {
        return payloadRaw instanceof Map ? (Map<String, Object>) payloadRaw : null;
    }

    @JsonIgnore
    public void setPayload(Map<String, Object> payload) { this.payloadRaw = payload; }

    /** The raw {@code payload} exactly as delivered: an object, an array, or {@code null}. */
    @JsonIgnore
    public Object getPayloadRaw() { return payloadRaw; }

    public Map<String, Object> getOrigin() { return origin; }
    public void setOrigin(Map<String, Object> origin) { this.origin = origin; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public Map<String, Object> getSubject() { return subject; }
    public void setSubject(Map<String, Object> subject) { this.subject = subject; }

    public Map<String, Object> getObject() { return object; }
    public void setObject(Map<String, Object> object) { this.object = object; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
}
