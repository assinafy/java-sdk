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

    public Workspace() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }

    public String getSecondaryColor() { return secondaryColor; }
    public void setSecondaryColor(String secondaryColor) { this.secondaryColor = secondaryColor; }

    public String getNotificationSenderType() { return notificationSenderType; }
    public void setNotificationSenderType(String notificationSenderType) { this.notificationSenderType = notificationSenderType; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public Boolean getIsDeleteAllowed() { return isDeleteAllowed; }
    public void setIsDeleteAllowed(Boolean isDeleteAllowed) { this.isDeleteAllowed = isDeleteAllowed; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
