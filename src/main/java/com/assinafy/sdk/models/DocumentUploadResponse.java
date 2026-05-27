package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentUploadResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("template_id")
    private String templateId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("status")
    private String status;

    @JsonProperty("assignment")
    private Object assignment;

    @JsonProperty("artifacts")
    private DocumentArtifacts artifacts;

    @JsonProperty("pages")
    private List<DocumentPage> pages;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("is_closed")
    private Boolean isClosed;

    @JsonProperty("decline_reason")
    private String declineReason;

    @JsonProperty("declined_by")
    private Object declinedBy;

    public DocumentUploadResponse() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Object getAssignment() { return assignment; }
    public void setAssignment(Object assignment) { this.assignment = assignment; }

    public DocumentArtifacts getArtifacts() { return artifacts; }
    public void setArtifacts(DocumentArtifacts artifacts) { this.artifacts = artifacts; }

    public List<DocumentPage> getPages() { return pages; }
    public void setPages(List<DocumentPage> pages) { this.pages = pages; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getIsClosed() { return isClosed; }
    public void setIsClosed(Boolean isClosed) { this.isClosed = isClosed; }

    public String getDeclineReason() { return declineReason; }
    public void setDeclineReason(String declineReason) { this.declineReason = declineReason; }

    public Object getDeclinedBy() { return declinedBy; }
    public void setDeclinedBy(Object declinedBy) { this.declinedBy = declinedBy; }
}
