package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResendEmailResponse {

    @JsonProperty("is_sent")
    private Boolean isSent;

    @JsonProperty("document_id")
    private String documentId;

    @JsonProperty("signer_id")
    private String signerId;

    public ResendEmailResponse() {}

    public Boolean getIsSent() { return isSent; }
    public void setIsSent(Boolean isSent) { this.isSent = isSent; }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getSignerId() { return signerId; }
    public void setSignerId(String signerId) { this.signerId = signerId; }
}
