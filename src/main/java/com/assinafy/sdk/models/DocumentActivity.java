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

    @JsonProperty("origin")
    private String origin;

    @JsonProperty("created_at")
    private String createdAt;

    public DocumentActivity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
