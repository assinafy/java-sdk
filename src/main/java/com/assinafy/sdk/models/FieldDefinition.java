package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldDefinition {

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

    public FieldDefinition() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRegex() { return regex; }
    public void setRegex(String regex) { this.regex = regex; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsRequired() { return isRequired; }
    public void setIsRequired(Boolean isRequired) { this.isRequired = isRequired; }

    public Boolean getIsStandard() { return isStandard; }
    public void setIsStandard(Boolean isStandard) { this.isStandard = isStandard; }

    public Boolean getIsPreDefined() { return isPreDefined; }
    public void setIsPreDefined(Boolean isPreDefined) { this.isPreDefined = isPreDefined; }

    public Boolean getIsReadOnly() { return isReadOnly; }
    public void setIsReadOnly(Boolean isReadOnly) { this.isReadOnly = isReadOnly; }

    public Boolean getIsVisible() { return isVisible; }
    public void setIsVisible(Boolean isVisible) { this.isVisible = isVisible; }
}
