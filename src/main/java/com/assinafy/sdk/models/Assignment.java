package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents the assignment in an Assinafy API response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Assignment {

    @JsonProperty("resource")
    private String resource;

    @JsonProperty("id")
    private String id;

    @JsonProperty("sender_email")
    private String senderEmail;

    @JsonProperty("method")
    private String method;

    @JsonProperty("expires_at")
    private String expiresAt;

    @JsonProperty("message")
    private String message;

    @JsonProperty("signers")
    private List<AssignmentSigner> signers;

    @JsonProperty("copy_receivers")
    private List<Object> copyReceivers;

    @JsonProperty("items")
    private List<AssignmentItem> items;

    @JsonProperty("summary")
    private AssignmentSummary summary;

    @JsonProperty("signing_urls")
    private List<SigningUrl> signingUrls;

    /**
     * Creates an empty assignment.
     */
    public Assignment() {}

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
     * Returns the sender email address.
     *
     * @return the sender email address
     */
    public String getSenderEmail() { return senderEmail; }

    /**
     * Sets the sender email address.
     *
     * @param senderEmail the sender email address
     */
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }

    /**
     * Returns the method.
     *
     * @return the method
     */
    public String getMethod() { return method; }

    /**
     * Sets the method.
     *
     * @param method the method
     */
    public void setMethod(String method) { this.method = method; }

    /**
     * Returns the expiration timestamp.
     *
     * @return the expiration timestamp
     */
    public String getExpiresAt() { return expiresAt; }

    /**
     * Sets the expiration timestamp.
     *
     * @param expiresAt the expiration timestamp
     */
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

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
     * Returns the signers.
     *
     * @return the signers
     */
    public List<AssignmentSigner> getSigners() { return signers; }

    /**
     * Sets the signers.
     *
     * @param signers the signers
     */
    public void setSigners(List<AssignmentSigner> signers) { this.signers = signers; }

    /**
     * Returns the copy recipients.
     *
     * @return the copy recipients
     */
    public List<Object> getCopyReceivers() { return copyReceivers; }

    /**
     * Sets the copy recipients.
     *
     * @param copyReceivers the copy recipients
     */
    public void setCopyReceivers(List<Object> copyReceivers) { this.copyReceivers = copyReceivers; }

    /**
     * Returns the items.
     *
     * @return the items
     */
    public List<AssignmentItem> getItems() { return items; }

    /**
     * Sets the items.
     *
     * @param items the items
     */
    public void setItems(List<AssignmentItem> items) { this.items = items; }

    /**
     * Returns the summary.
     *
     * @return the summary
     */
    public AssignmentSummary getSummary() { return summary; }

    /**
     * Sets the summary.
     *
     * @param summary the summary
     */
    public void setSummary(AssignmentSummary summary) { this.summary = summary; }

    /**
     * Returns the signing URLs.
     *
     * @return the signing URLs
     */
    public List<SigningUrl> getSigningUrls() { return signingUrls; }

    /**
     * Sets the signing URLs.
     *
     * @param signingUrls the signing URLs
     */
    public void setSigningUrls(List<SigningUrl> signingUrls) { this.signingUrls = signingUrls; }
}
