package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A workspace account, as returned by the {@code /accounts} endpoints. The
 * {@code notification_sender_type}, {@code roles} and {@code is_delete_allowed} fields are
 * populated on the create/update/get responses.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Workspace {

    @JsonProperty("resource")
    private String resource;

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("primary_color")
    private String primaryColor;

    @JsonProperty("secondary_color")
    private String secondaryColor;

    /** {@code User} (default) shows the document owner as the notification sender; {@code Account} shows this account. */
    @JsonProperty("notification_sender_type")
    private String notificationSenderType;

    /** The authenticated user's roles on this account (e.g. {@code owner}). */
    @JsonProperty("roles")
    private List<String> roles;

    @JsonProperty("is_delete_allowed")
    private Boolean isDeleteAllowed;

    @JsonProperty("created_at")
    private String createdAt;

    /**
     * Creates an empty workspace.
     */
    public Workspace() {}

    /**
     * Returns the resource type.
     *
     * @return the resource type
     */
    public String getResource() { return resource; }

    /**
     * Sets the resource type.
     *
     * @param resource the resource type
     */
    public void setResource(String resource) { this.resource = resource; }

    /**
     * Returns the ID.
     *
     * @return the ID
     */
    public String getId() { return id; }

    /**
     * Sets the ID.
     *
     * @param id the ID
     */
    public void setId(String id) { this.id = id; }

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
     * Returns the roles.
     *
     * @return the roles
     */
    public List<String> getRoles() { return roles; }

    /**
     * Sets the roles.
     *
     * @param roles the roles
     */
    public void setRoles(List<String> roles) { this.roles = roles; }

    /**
     * Returns the deletion-permission flag.
     *
     * @return the deletion-permission flag
     */
    public Boolean getIsDeleteAllowed() { return isDeleteAllowed; }

    /**
     * Sets the deletion-permission flag.
     *
     * @param isDeleteAllowed the deletion-permission flag
     */
    public void setIsDeleteAllowed(Boolean isDeleteAllowed) { this.isDeleteAllowed = isDeleteAllowed; }

    /**
     * Returns the creation timestamp.
     *
     * @return the creation timestamp
     */
    public String getCreatedAt() { return createdAt; }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt the creation timestamp
     */
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
