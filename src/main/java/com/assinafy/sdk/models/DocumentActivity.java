package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    public DocumentActivity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getPayload() { return payload; }
    public void setPayload(Object payload) { this.payload = payload; }

    public ActivityOrigin getOrigin() { return origin; }
    public void setOrigin(ActivityOrigin origin) { this.origin = origin; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
