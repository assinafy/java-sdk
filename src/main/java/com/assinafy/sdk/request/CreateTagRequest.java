package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Body for {@code POST /accounts/{account_id}/tags} (Create Tag).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateTagRequest {

    /** Tag name (required). */
    @JsonProperty("name")
    private String name;

    /** Optional six-character hex colour without a leading {@code #} (e.g. {@code FF0000}). */
    @JsonProperty("color")
    private String color;

    /**
     * Creates an empty tag-creation request.
     */
    public CreateTagRequest() {}

    /**
     * Creates a tag-creation request.
     *
     * @param name the name
     */
    public CreateTagRequest(String name) { this.name = name; }

    /**
     * Creates a builder for a tag-creation request.
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
     * Builder for {@link CreateTagRequest}.
     */
    public static final class Builder {
        private final CreateTagRequest req = new CreateTagRequest();

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
         * Builds the configured tag-creation request.
         *
         * @return the configured tag-creation request
         */
        public CreateTagRequest build() { return req; }
    }
}
