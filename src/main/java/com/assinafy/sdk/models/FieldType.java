package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the field type in an Assinafy API response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldType {

    @JsonProperty("type")
    private String type;

    @JsonProperty("name")
    private String name;

    /**
     * Creates an empty field type.
     */
    public FieldType() {}

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
}
