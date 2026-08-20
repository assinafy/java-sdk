package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A field placement configured on a template page, as returned inside
 * {@code template.pages[].fields}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplateFieldPlacement {

    @JsonProperty("id")
    private String id;

    @JsonProperty("field_id")
    private String fieldId;

    @JsonProperty("role_id")
    private String roleId;

    @JsonProperty("label")
    private String label;

    @JsonProperty("display_settings")
    private Object displaySettings;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    /**
     * Creates an empty template field placement.
     */
    public TemplateFieldPlacement() {}

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
     * Returns the field ID.
     *
     * @return the field ID
     */
    public String getFieldId() { return fieldId; }

    /**
     * Sets the field ID.
     *
     * @param fieldId the field ID
     */
    public void setFieldId(String fieldId) { this.fieldId = fieldId; }

    /**
     * Returns the role ID.
     *
     * @return the role ID
     */
    public String getRoleId() { return roleId; }

    /**
     * Sets the role ID.
     *
     * @param roleId the role ID
     */
    public void setRoleId(String roleId) { this.roleId = roleId; }

    /**
     * Returns the label.
     *
     * @return the label
     */
    public String getLabel() { return label; }

    /**
     * Sets the label.
     *
     * @param label the label
     */
    public void setLabel(String label) { this.label = label; }

    /**
     * Returns the display settings.
     *
     * @return the display settings
     */
    public Object getDisplaySettings() { return displaySettings; }

    /**
     * Sets the display settings.
     *
     * @param displaySettings the display settings
     */
    public void setDisplaySettings(Object displaySettings) { this.displaySettings = displaySettings; }

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
