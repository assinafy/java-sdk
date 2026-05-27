package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Body for {@code PUT /accounts/{account_id}/tags/{tag_id}} (Rename Tag). Both fields are
 * optional; only the supplied fields are changed.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RenameTagRequest {

    @JsonProperty("name")
    private String name;

    /** Six-character hex colour without a leading {@code #}, or {@code null}. */
    @JsonProperty("color")
    private String color;

    public RenameTagRequest() {}

    public RenameTagRequest(String name) { this.name = name; }

    public static Builder builder() { return new Builder(); }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public static final class Builder {
        private final RenameTagRequest req = new RenameTagRequest();

        public Builder name(String name) { req.setName(name); return this; }
        public Builder color(String color) { req.setColor(color); return this; }
        public RenameTagRequest build() { return req; }
    }
}
