package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the field definition in an Assinafy API response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldDefinition {

    @JsonProperty("resource")
    private String resource;

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private String type;

    @JsonProperty("regex")
    private String regex;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("is_required")
    private Boolean isRequired;

    @JsonProperty("is_standard")
    private Boolean isStandard;

    @JsonProperty("is_pre_defined")
    private Boolean isPreDefined;

    @JsonProperty("is_read_only")
    private Boolean isReadOnly;

    @JsonProperty("is_visible")
    private Boolean isVisible;

    /**
     * Creates an empty field definition.
     */
    public FieldDefinition() {}

    /**
     * Returns the resource type.
     *
     * @return the resource type
     */
    public String getResource() { return resource; }

    /**
     * Sets the resource type.
     *
     * @param resource the resource type
     */
    public void setResource(String resource) { this.resource = resource; }

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
     * Returns the type.
     *
     * @return the type
     */
    public String getType() { return type; }

    /**
     * Sets the type.
     *
     * @param type the type
     */
    public void setType(String type) { this.type = type; }

    /**
     * Returns the regex.
     *
     * @return the regex
     */
    public String getRegex() { return regex; }

    /**
     * Sets the regex.
     *
     * @param regex the regex
     */
    public void setRegex(String regex) { this.regex = regex; }

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
     * Returns the required flag.
     *
     * @return the required flag
     */
    public Boolean getIsRequired() { return isRequired; }

    /**
     * Sets the required flag.
     *
     * @param isRequired the required flag
     */
    public void setIsRequired(Boolean isRequired) { this.isRequired = isRequired; }

    /**
     * Returns the standard-field flag.
     *
     * @return the standard-field flag
     */
    public Boolean getIsStandard() { return isStandard; }

    /**
     * Sets the standard-field flag.
     *
     * @param isStandard the standard-field flag
     */
    public void setIsStandard(Boolean isStandard) { this.isStandard = isStandard; }

    /**
     * Returns the predefined-field flag.
     *
     * @return the predefined-field flag
     */
    public Boolean getIsPreDefined() { return isPreDefined; }

    /**
     * Sets the predefined-field flag.
     *
     * @param isPreDefined the predefined-field flag
     */
    public void setIsPreDefined(Boolean isPreDefined) { this.isPreDefined = isPreDefined; }

    /**
     * Returns the read-only flag.
     *
     * @return the read-only flag
     */
    public Boolean getIsReadOnly() { return isReadOnly; }

    /**
     * Sets the read-only flag.
     *
     * @param isReadOnly the read-only flag
     */
    public void setIsReadOnly(Boolean isReadOnly) { this.isReadOnly = isReadOnly; }

    /**
     * Returns the visibility flag.
     *
     * @return the visibility flag
     */
    public Boolean getIsVisible() { return isVisible; }

    /**
     * Sets the visibility flag.
     *
     * @param isVisible the visibility flag
     */
    public void setIsVisible(Boolean isVisible) { this.isVisible = isVisible; }
}
