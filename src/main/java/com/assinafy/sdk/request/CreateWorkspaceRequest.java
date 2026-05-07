package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateWorkspaceRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("primary_color")
    private String primaryColor;

    @JsonProperty("secondary_color")
    private String secondaryColor;

    public CreateWorkspaceRequest() {}

    public CreateWorkspaceRequest(String name) {
        this.name = name;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }

    public String getSecondaryColor() { return secondaryColor; }
    public void setSecondaryColor(String secondaryColor) { this.secondaryColor = secondaryColor; }

    public static final class Builder {
        private final CreateWorkspaceRequest req = new CreateWorkspaceRequest();

        public Builder name(String name) { req.setName(name); return this; }
        public Builder primaryColor(String color) { req.setPrimaryColor(color); return this; }
        public Builder secondaryColor(String color) { req.setSecondaryColor(color); return this; }
        public CreateWorkspaceRequest build() { return req; }
    }
}
