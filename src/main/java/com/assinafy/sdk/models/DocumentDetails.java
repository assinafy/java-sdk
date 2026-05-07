package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentDetails {

    @JsonProperty("id")
    private String id;

    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("status")
    private String status;

    @JsonProperty("assignment")
    private Assignment assignment;

    @JsonProperty("download_url")
    private String downloadUrl;

    @JsonProperty("download_final_url")
    private String downloadFinalUrl;

    @JsonProperty("signing_url")
    private String signingUrl;

    @JsonProperty("artifacts")
    private DocumentArtifacts artifacts;

    @JsonProperty("pages")
    private List<Object> pages;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("is_closed")
    private Boolean isClosed;

    @JsonProperty("decline_reason")
    private String declineReason;

    @JsonProperty("activities")
    private List<DocumentActivity> activities;

    public DocumentDetails() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Assignment getAssignment() { return assignment; }
    public void setAssignment(Assignment assignment) { this.assignment = assignment; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public String getDownloadFinalUrl() { return downloadFinalUrl; }
    public void setDownloadFinalUrl(String downloadFinalUrl) { this.downloadFinalUrl = downloadFinalUrl; }

    public String getSigningUrl() { return signingUrl; }
    public void setSigningUrl(String signingUrl) { this.signingUrl = signingUrl; }

    public DocumentArtifacts getArtifacts() { return artifacts; }
    public void setArtifacts(DocumentArtifacts artifacts) { this.artifacts = artifacts; }

    public List<Object> getPages() { return pages; }
    public void setPages(List<Object> pages) { this.pages = pages; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getIsClosed() { return isClosed; }
    public void setIsClosed(Boolean isClosed) { this.isClosed = isClosed; }

    public String getDeclineReason() { return declineReason; }
    public void setDeclineReason(String declineReason) { this.declineReason = declineReason; }

    public List<DocumentActivity> getActivities() { return activities; }
    public void setActivities(List<DocumentActivity> activities) { this.activities = activities; }
}
