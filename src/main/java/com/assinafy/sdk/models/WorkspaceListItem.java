package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents the workspace list item in an Assinafy API response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkspaceListItem {

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

    @JsonProperty("notification_sender_type")
    private String notificationSenderType;

    @JsonProperty("is_delete_allowed")
    private Boolean isDeleteAllowed;

    @JsonProperty("roles")
    private List<String> roles;

    @JsonProperty("created_at")
    private String createdAt;

    /**
     * Creates an empty workspace list item.
     */
    public WorkspaceListItem() {}

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
