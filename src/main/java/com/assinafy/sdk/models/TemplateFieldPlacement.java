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

    public TemplateFieldPlacement() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFieldId() { return fieldId; }
    public void setFieldId(String fieldId) { this.fieldId = fieldId; }

    public String getRoleId() { return roleId; }
    public void setRoleId(String roleId) { this.roleId = roleId; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Object getDisplaySettings() { return displaySettings; }
    public void setDisplaySettings(Object displaySettings) { this.displaySettings = displaySettings; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
