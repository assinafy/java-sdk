package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateFieldRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private String type;

    @JsonProperty("regex")
    private String regex;

    @JsonProperty("is_required")
    private Boolean isRequired;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("is_read_only")
    private Boolean isReadOnly;

    @JsonProperty("is_visible")
    private Boolean isVisible;

    public UpdateFieldRequest() {}

    public static Builder builder() { return new Builder(); }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRegex() { return regex; }
    public void setRegex(String regex) { this.regex = regex; }

    public Boolean getIsRequired() { return isRequired; }
    public void setIsRequired(Boolean isRequired) { this.isRequired = isRequired; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsReadOnly() { return isReadOnly; }
    public void setIsReadOnly(Boolean isReadOnly) { this.isReadOnly = isReadOnly; }

    public Boolean getIsVisible() { return isVisible; }
    public void setIsVisible(Boolean isVisible) { this.isVisible = isVisible; }

    public static final class Builder {
        private final UpdateFieldRequest req = new UpdateFieldRequest();

        public Builder name(String name) { req.setName(name); return this; }
        public Builder type(String type) { req.setType(type); return this; }
        public Builder regex(String regex) { req.setRegex(regex); return this; }
        public Builder isRequired(boolean v) { req.setIsRequired(v); return this; }
        public Builder isActive(boolean v) { req.setIsActive(v); return this; }
        public Builder isReadOnly(boolean v) { req.setIsReadOnly(v); return this; }
        public Builder isVisible(boolean v) { req.setIsVisible(v); return this; }
        public UpdateFieldRequest build() { return req; }
    }
}
