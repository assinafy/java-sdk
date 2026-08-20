package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload for creating a workspace.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateWorkspaceRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("primary_color")
    private String primaryColor;

    @JsonProperty("secondary_color")
    private String secondaryColor;

    /**
     * Who signers see as the notification sender: {@code User} (default) shows the document
     * owner's name; {@code Account} shows this account's name.
     */
    @JsonProperty("notification_sender_type")
    private String notificationSenderType;

    /**
     * Creates an empty workspace-creation request.
     */
    public CreateWorkspaceRequest() {}

    /**
     * Creates a workspace-creation request.
     *
     * @param name the name
     */
    public CreateWorkspaceRequest(String name) {
        this.name = name;
    }

    /**
     * Creates a builder for a workspace-creation request.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

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
     * Returns the primary color.
     *
     * @return the primary color
     */
    public String getPrimaryColor() { return primaryColor; }

    /**
     * Sets the primary color.
     *
     * @param primaryColor the primary color
     */
    public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }

    /**
     * Returns the secondary color.
     *
     * @return the secondary color
     */
    public String getSecondaryColor() { return secondaryColor; }

    /**
     * Sets the secondary color.
     *
     * @param secondaryColor the secondary color
     */
    public void setSecondaryColor(String secondaryColor) { this.secondaryColor = secondaryColor; }

    /**
     * Returns the notification sender type.
     *
     * @return the notification sender type
     */
    public String getNotificationSenderType() { return notificationSenderType; }

    /**
     * Sets the notification sender type.
     *
     * @param notificationSenderType the notification sender type
     */
    public void setNotificationSenderType(String notificationSenderType) { this.notificationSenderType = notificationSenderType; }

    /**
     * Builder for {@link CreateWorkspaceRequest}.
     */
    public static final class Builder {
        private final CreateWorkspaceRequest req = new CreateWorkspaceRequest();

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
         * Sets the primary color for the object being built.
         *
         * @param color the primary color
         * @return this builder
         */
        public Builder primaryColor(String color) { req.setPrimaryColor(color); return this; }

        /**
         * Sets the secondary color for the object being built.
         *
         * @param color the secondary color
         * @return this builder
         */
        public Builder secondaryColor(String color) { req.setSecondaryColor(color); return this; }

        /**
         * {@code User} (default) or {@code Account} — who signers see as the notification sender.
         *
         * @param type the notification sender type
         * @return this builder
         */
        public Builder notificationSenderType(String type) { req.setNotificationSenderType(type); return this; }

        /**
         * Builds the configured workspace-creation request.
         *
         * @return the configured workspace-creation request
         */
        public CreateWorkspaceRequest build() { return req; }
    }
}
