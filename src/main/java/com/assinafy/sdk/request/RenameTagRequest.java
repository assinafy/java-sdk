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

    /**
     * Creates an empty tag-update request.
     */
    public RenameTagRequest() {}

    /**
     * Creates a tag-update request.
     *
     * @param name the name
     */
    public RenameTagRequest(String name) { this.name = name; }

    /**
     * Creates a builder for a tag-update request.
     *
     * @return a new builder
     */
    public static Builder builder() { return new Builder(); }

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
     * Returns the color.
     *
     * @return the color
     */
    public String getColor() { return color; }

    /**
     * Sets the color.
     *
     * @param color the color
     */
    public void setColor(String color) { this.color = color; }

    /**
     * Returns whether the request explicitly clears the color.
     *
     * @return true when the request explicitly clears the color; otherwise false
     */
    public boolean isClearColor() { return clearColor; }

    /**
     * Sets the color-clearing flag.
     *
     * @param clearColor the color-clearing flag
     */
    public void setClearColor(boolean clearColor) { this.clearColor = clearColor; }

    /**
     * Builder for {@link RenameTagRequest}.
     */
    public static final class Builder {
        private final RenameTagRequest req = new RenameTagRequest();

        /** Creates an empty builder. */
        public Builder() {}

        /**
         * Sets the name for the object being built.
         *
         * @param name the name
         * @return this builder
         */
        public Builder name(String name) { req.setName(name); return this; }

        /**
         * Sets the color for the object being built.
         *
         * @param color the color
         * @return this builder
         */
        public Builder color(String color) { req.setColor(color); return this; }

        /**
         * Request that the tag's colour be cleared (sends {@code "color": null}).
         *
         * @return this builder
         */
        public Builder clearColor() { req.setClearColor(true); return this; }

        /**
         * Builds the configured tag-update request.
         *
         * @return the configured tag-update request
         */
        public RenameTagRequest build() { return req; }
    }
}
