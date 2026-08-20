package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the template in an Assinafy API response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Template {

    @JsonProperty("resource")
    private String resource;

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("document_name")
    private String documentName;

    @JsonProperty("message")
    private String message;

    @JsonProperty("status")
    private String status;

    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("roles")
    private List<TemplateRole> roles;

    @JsonProperty("pages")
    private List<TemplatePage> pages;

    @JsonProperty("tags")
    private List<Tag> tags;

    /** Tags automatically attached to documents created from this template. */
    @JsonProperty("default_document_tags")
    private List<Tag> defaultDocumentTags;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    private final Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * Creates an empty template.
     */
    public Template() {}

    /**
     * Returns the additional properties.
     *
     * @return the additional properties
     */
    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() { return additionalProperties; }

    /**
     * Sets the additional property.
     *
     * @param name the name
     * @param value the additional property
     */
    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) { additionalProperties.put(name, value); }

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
     * Returns the document name.
     *
     * @return the document name
     */
    public String getDocumentName() { return documentName; }

    /**
     * Sets the document name.
     *
     * @param documentName the document name
     */
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    /**
     * Returns the message.
     *
     * @return the message
     */
    public String getMessage() { return message; }

    /**
     * Sets the message.
     *
     * @param message the message
     */
    public void setMessage(String message) { this.message = message; }

    /**
     * Returns the status.
     *
     * @return the status
     */
    public String getStatus() { return status; }

    /**
     * Sets the status.
     *
     * @param status the status
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * Returns the account ID.
     *
     * @return the account ID
     */
    public String getAccountId() { return accountId; }

    /**
     * Sets the account ID.
     *
     * @param accountId the account ID
     */
    public void setAccountId(String accountId) { this.accountId = accountId; }

    /**
     * Returns the roles.
     *
     * @return the roles
     */
    public List<TemplateRole> getRoles() { return roles; }

    /**
     * Sets the roles.
     *
     * @param roles the roles
     */
    public void setRoles(List<TemplateRole> roles) { this.roles = roles; }

    /**
     * Returns the pages.
     *
     * @return the pages
     */
    public List<TemplatePage> getPages() { return pages; }

    /**
     * Sets the pages.
     *
     * @param pages the pages
     */
    public void setPages(List<TemplatePage> pages) { this.pages = pages; }

    /**
     * Returns the tags.
     *
     * @return the tags
     */
    public List<Tag> getTags() { return tags; }

    /**
     * Sets the tags.
     *
     * @param tags the tags
     */
    public void setTags(List<Tag> tags) { this.tags = tags; }

    /**
     * Returns the default document tags.
     *
     * @return the default document tags
     */
    public List<Tag> getDefaultDocumentTags() { return defaultDocumentTags; }

    /**
     * Sets the default document tags.
     *
     * @param defaultDocumentTags the default document tags
     */
    public void setDefaultDocumentTags(List<Tag> defaultDocumentTags) { this.defaultDocumentTags = defaultDocumentTags; }

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

    /**
     * Returns the last-update timestamp.
     *
     * @return the last-update timestamp
     */
    public String getUpdatedAt() { return updatedAt; }

    /**
     * Sets the last-update timestamp.
     *
     * @param updatedAt the last-update timestamp
     */
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
