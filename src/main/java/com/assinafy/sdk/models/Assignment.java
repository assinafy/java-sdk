package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Assignment {

    @JsonProperty("id")
    private String id;

    @JsonProperty("sender_email")
    private String senderEmail;

    @JsonProperty("method")
    private String method;

    @JsonProperty("expires_at")
    private String expiresAt;

    @JsonProperty("expiration")
    private String expiration;

    @JsonProperty("message")
    private String message;

    @JsonProperty("signers")
    private List<Signer> signers;

    @JsonProperty("copy_receivers")
    private List<String> copyReceivers;

    @JsonProperty("items")
    private List<Object> items;

    @JsonProperty("summary")
    private AssignmentSummary summary;

    @JsonProperty("signing_urls")
    private Map<String, String> signingUrls;

    public Assignment() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

    public String getExpiration() { return expiration; }
    public void setExpiration(String expiration) { this.expiration = expiration; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<Signer> getSigners() { return signers; }
    public void setSigners(List<Signer> signers) { this.signers = signers; }

    public List<String> getCopyReceivers() { return copyReceivers; }
    public void setCopyReceivers(List<String> copyReceivers) { this.copyReceivers = copyReceivers; }

    public List<Object> getItems() { return items; }
    public void setItems(List<Object> items) { this.items = items; }

    public AssignmentSummary getSummary() { return summary; }
    public void setSummary(AssignmentSummary summary) { this.summary = summary; }

    public Map<String, String> getSigningUrls() { return signingUrls; }
    public void setSigningUrls(Map<String, String> signingUrls) { this.signingUrls = signingUrls; }
}
