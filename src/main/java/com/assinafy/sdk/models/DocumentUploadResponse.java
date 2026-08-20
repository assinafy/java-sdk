package com.assinafy.sdk.models;

import com.assinafy.sdk.util.ResponseHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents the document upload response in an Assinafy API response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentUploadResponse {

    @JsonProperty("resource")
    private String resource;

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

    @JsonProperty("signing_url")
    private String signingUrl;

    @JsonProperty("pages")
    private List<DocumentPage> pages;

    @JsonProperty("tags")
    private List<Tag> tags;

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

    /**
     * Creates an empty document upload response.
     */
    public DocumentUploadResponse() {}

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
     * Returns the template ID.
     *
     * @return the template ID
     */
    public String getTemplateId() { return templateId; }

    /**
     * Sets the template ID.
     *
     * @param templateId the template ID
     */
    public void setTemplateId(String templateId) { this.templateId = templateId; }

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
     * Returns the assignment.
     *
     * @return the assignment
     */
    public Object getAssignment() { return assignment; }

    /**
     * Sets the assignment.
     *
     * @param assignment the assignment
     */
    public void setAssignment(Object assignment) { this.assignment = assignment; }

    /**
     * Typed view of {@link #getAssignment()} that preserves the legacy raw accessor.
     *
     * @return the assignment details
     */
    @JsonIgnore
    public Assignment getAssignmentDetails() {
        if (assignment == null) return null;
        return assignment instanceof Assignment typed
                ? typed
                : ResponseHandler.convert(assignment, Assignment.class);
    }

    /**
     * Returns the artifacts.
     *
     * @return the artifacts
     */
    public DocumentArtifacts getArtifacts() { return artifacts; }

    /**
     * Sets the artifacts.
     *
     * @param artifacts the artifacts
     */
    public void setArtifacts(DocumentArtifacts artifacts) { this.artifacts = artifacts; }

    /**
     * Returns the signing URL.
     *
     * @return the signing URL
     */
    public String getSigningUrl() { return signingUrl; }

    /**
     * Sets the signing URL.
     *
     * @param signingUrl the signing URL
     */
    public void setSigningUrl(String signingUrl) { this.signingUrl = signingUrl; }

    /**
     * Returns the pages.
     *
     * @return the pages
     */
    public List<DocumentPage> getPages() { return pages; }

    /**
     * Sets the pages.
     *
     * @param pages the pages
     */
    public void setPages(List<DocumentPage> pages) { this.pages = pages; }

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

    /**
     * Returns the closed flag.
     *
     * @return the closed flag
     */
    public Boolean getIsClosed() { return isClosed; }

    /**
     * Sets the closed flag.
     *
     * @param isClosed the closed flag
     */
    public void setIsClosed(Boolean isClosed) { this.isClosed = isClosed; }

    /**
     * Returns the decline reason.
     *
     * @return the decline reason
     */
    public String getDeclineReason() { return declineReason; }

    /**
     * Sets the decline reason.
     *
     * @param declineReason the decline reason
     */
    public void setDeclineReason(String declineReason) { this.declineReason = declineReason; }

    /**
     * Returns the declining signer.
     *
     * @return the declining signer
     */
    public Object getDeclinedBy() { return declinedBy; }

    /**
     * Sets the declining signer.
     *
     * @param declinedBy the declining signer
     */
    public void setDeclinedBy(Object declinedBy) { this.declinedBy = declinedBy; }

    /**
     * Typed view of the documented {@code declined_by} signer.
     *
     * @return the declining signer
     */
    @JsonIgnore
    public Signer getDeclinedBySigner() {
        if (declinedBy == null) return null;
        return declinedBy instanceof Signer signer
                ? signer
                : ResponseHandler.convert(declinedBy, Signer.class);
    }
}
