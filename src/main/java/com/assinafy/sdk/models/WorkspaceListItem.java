package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkspaceListItem {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("is_delete_allowed")
    private Boolean isDeleteAllowed;

    @JsonProperty("roles")
    private List<String> roles;

    @JsonProperty("created_at")
    private String createdAt;

    public WorkspaceListItem() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getIsDeleteAllowed() { return isDeleteAllowed; }
    public void setIsDeleteAllowed(Boolean isDeleteAllowed) { this.isDeleteAllowed = isDeleteAllowed; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
