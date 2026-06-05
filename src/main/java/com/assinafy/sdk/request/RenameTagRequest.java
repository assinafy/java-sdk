package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Body for {@code PUT /accounts/{account_id}/tags/{tag_id}} (Rename Tag). Both fields are
 * optional; only the supplied fields are changed.
 *
 * <p>The API treats {@code color} as tri-state: omitting it leaves the colour unchanged, sending
 * an explicit {@code null} clears it, and sending a value sets it. Use {@link Builder#color} to
 * set, and {@link Builder#clearColor} to clear — a builder with neither leaves the colour intact.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RenameTagRequest {

    @JsonProperty("name")
    private String name;

    /** Six-character hex colour without a leading {@code #}, or {@code null}. */
    @JsonProperty("color")
    private String color;

    /** When {@code true}, an explicit {@code "color": null} is sent to clear the colour. */
    @JsonIgnore
    private boolean clearColor;

    public RenameTagRequest() {}

    public RenameTagRequest(String name) { this.name = name; }

    public static Builder builder() { return new Builder(); }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public boolean isClearColor() { return clearColor; }
    public void setClearColor(boolean clearColor) { this.clearColor = clearColor; }

    public static final class Builder {
        private final RenameTagRequest req = new RenameTagRequest();

        public Builder name(String name) { req.setName(name); return this; }
        public Builder color(String color) { req.setColor(color); return this; }
        /** Request that the tag's colour be cleared (sends {@code "color": null}). */
        public Builder clearColor() { req.setClearColor(true); return this; }
        public RenameTagRequest build() { return req; }
    }
}
