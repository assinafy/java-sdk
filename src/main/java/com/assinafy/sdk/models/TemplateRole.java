package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the template role in an Assinafy API response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplateRole {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    /** Role kind, e.g. {@code Signer} or {@code Editor}. */
    @JsonProperty("assignment_type")
    private String assignmentType;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    private final Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * Creates an empty template role.
     */
    public TemplateRole() {}

    /**
     * Returns the additional properties.
     *
     * @return the additional properties
     */
    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() { return additionalProperties; }

    /**
     * Sets the additional property.
     *
     * @param name the name
     * @param value the additional property
     */
    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) { additionalProperties.put(name, value); }

    /**
     * Returns the ID.
     *
     * @return the ID
     */
    public String getId() { return id; }

    /**
     * Sets the ID.
     *
     * @param id the ID
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns the name.
     *
     * @return the name
     */
    public String getName() { return name; }

    /**
     * Sets the name.
     *
     * @param name the name
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns the assignment type.
     *
     * @return the assignment type
     */
    public String getAssignmentType() { return assignmentType; }

    /**
     * Sets the assignment type.
     *
     * @param assignmentType the assignment type
     */
    public void setAssignmentType(String assignmentType) { this.assignmentType = assignmentType; }

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
