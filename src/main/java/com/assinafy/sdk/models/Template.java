package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Template {

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

    public Template() {}

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() { return additionalProperties; }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) { additionalProperties.put(name, value); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public List<TemplateRole> getRoles() { return roles; }
    public void setRoles(List<TemplateRole> roles) { this.roles = roles; }

    public List<TemplatePage> getPages() { return pages; }
    public void setPages(List<TemplatePage> pages) { this.pages = pages; }

    public List<Tag> getTags() { return tags; }
    public void setTags(List<Tag> tags) { this.tags = tags; }

    public List<Tag> getDefaultDocumentTags() { return defaultDocumentTags; }
    public void setDefaultDocumentTags(List<Tag> defaultDocumentTags) { this.defaultDocumentTags = defaultDocumentTags; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
