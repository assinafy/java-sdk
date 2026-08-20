package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload for updating a field definition.
 */
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

    /**
     * Creates an empty field-definition update request.
     */
    public UpdateFieldRequest() {}

    /**
     * Creates a builder for a field-definition update request.
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
     * Returns the regex.
     *
     * @return the regex
     */
    public String getRegex() { return regex; }

    /**
     * Sets the regex.
     *
     * @param regex the regex
     */
    public void setRegex(String regex) { this.regex = regex; }

    /**
     * Returns the required flag.
     *
     * @return the required flag
     */
    public Boolean getIsRequired() { return isRequired; }

    /**
     * Sets the required flag.
     *
     * @param isRequired the required flag
     */
    public void setIsRequired(Boolean isRequired) { this.isRequired = isRequired; }

    /**
     * Returns the active flag.
     *
     * @return the active flag
     */
    public Boolean getIsActive() { return isActive; }

    /**
     * Sets the active flag.
     *
     * @param isActive the active flag
     */
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    /**
     * Returns the read-only flag.
     *
     * @return the read-only flag
     */
    public Boolean getIsReadOnly() { return isReadOnly; }

    /**
     * Sets the read-only flag.
     *
     * @param isReadOnly the read-only flag
     */
    public void setIsReadOnly(Boolean isReadOnly) { this.isReadOnly = isReadOnly; }

    /**
     * Returns the visibility flag.
     *
     * @return the visibility flag
     */
    public Boolean getIsVisible() { return isVisible; }

    /**
     * Sets the visibility flag.
     *
     * @param isVisible the visibility flag
     */
    public void setIsVisible(Boolean isVisible) { this.isVisible = isVisible; }

    /**
     * Builder for {@link UpdateFieldRequest}.
     */
    public static final class Builder {
        private final UpdateFieldRequest req = new UpdateFieldRequest();

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
         * Sets the type for the object being built.
         *
         * @param type the type
         * @return this builder
         */
        public Builder type(String type) { req.setType(type); return this; }

        /**
         * Sets the regex for the object being built.
         *
         * @param regex the regex
         * @return this builder
         */
        public Builder regex(String regex) { req.setRegex(regex); return this; }

        /**
         * Sets the required flag for the object being built.
         *
         * @param v whether the field is required
         * @return this builder
         */
        public Builder isRequired(boolean v) { req.setIsRequired(v); return this; }

        /**
         * Sets the active flag for the object being built.
         *
         * @param v whether the field is active
         * @return this builder
         */
        public Builder isActive(boolean v) { req.setIsActive(v); return this; }

        /**
         * Sets the read-only flag for the object being built.
         *
         * @param v whether the field is read-only
         * @return this builder
         */
        public Builder isReadOnly(boolean v) { req.setIsReadOnly(v); return this; }

        /**
         * Sets the visibility flag for the object being built.
         *
         * @param v whether the field is visible
         * @return this builder
         */
        public Builder isVisible(boolean v) { req.setIsVisible(v); return this; }

        /**
         * Builds the configured field-definition update request.
         *
         * @return the configured field-definition update request
         */
        public UpdateFieldRequest build() { return req; }
    }
}
