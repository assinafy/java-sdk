package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Account membership returned inside an {@link AuthSession}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthAccount {

    private String id;
    private String name;
    private List<String> roles;

    @JsonProperty("is_delete_allowed")
    private Boolean isDeleteAllowed;

    @JsonProperty("created_at")
    private String createdAt;

    /** Creates an empty account membership. */
    public AuthAccount() {}

    /**
     * Returns the account ID.
     *
     * @return the account ID
     */
    public String getId() { return id; }

    /**
     * Sets the account ID.
     *
     * @param id the account ID
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns the account name.
     *
     * @return the account name
     */
    public String getName() { return name; }

    /**
     * Sets the account name.
     *
     * @param name the account name
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns the authenticated user's roles in the account.
     *
     * @return account roles
     */
    public List<String> getRoles() { return roles; }

    /**
     * Sets the authenticated user's roles in the account.
     *
     * @param roles account roles
     */
    public void setRoles(List<String> roles) { this.roles = roles; }

    /**
     * Returns whether the authenticated user may delete the account.
     *
     * @return the deletion-permission flag
     */
    public Boolean getIsDeleteAllowed() { return isDeleteAllowed; }

    /**
     * Sets whether the authenticated user may delete the account.
     *
     * @param isDeleteAllowed the deletion-permission flag
     */
    public void setIsDeleteAllowed(Boolean isDeleteAllowed) { this.isDeleteAllowed = isDeleteAllowed; }

    /**
     * Returns the account creation timestamp.
     *
     * @return the ISO-8601 creation timestamp
     */
    public String getCreatedAt() { return createdAt; }

    /**
     * Sets the account creation timestamp.
     *
     * @param createdAt the ISO-8601 creation timestamp
     */
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
