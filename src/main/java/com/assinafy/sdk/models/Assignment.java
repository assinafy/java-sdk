package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

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

    public Assignment() {}

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<AssignmentSigner> getSigners() { return signers; }
    public void setSigners(List<AssignmentSigner> signers) { this.signers = signers; }

    public List<Object> getCopyReceivers() { return copyReceivers; }
    public void setCopyReceivers(List<Object> copyReceivers) { this.copyReceivers = copyReceivers; }

    public List<AssignmentItem> getItems() { return items; }
    public void setItems(List<AssignmentItem> items) { this.items = items; }

    public AssignmentSummary getSummary() { return summary; }
    public void setSummary(AssignmentSummary summary) { this.summary = summary; }

    public List<SigningUrl> getSigningUrls() { return signingUrls; }
    public void setSigningUrls(List<SigningUrl> signingUrls) { this.signingUrls = signingUrls; }
}
