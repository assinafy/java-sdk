package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A workspace tag. The full object ({@code created_at}/{@code updated_at} populated) is
 * returned by the Tag and document-tag endpoints; when a tag appears inline inside a
 * document or template payload only {@code id}, {@code name} and {@code color} are present.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tag {

    @JsonProperty("resource")
    private String resource;

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    /** Six-character hex colour without a leading {@code #}, or {@code null}. */
    @JsonProperty("color")
    private String color;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    public Tag() {}

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
