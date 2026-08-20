package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response of resending a signer's signature-request notification. Applies to any
 * notification channel (email or WhatsApp).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResendNotificationResponse {

    @JsonProperty("is_sent")
    private Boolean isSent;

    @JsonProperty("document_id")
    private String documentId;

    @JsonProperty("signer_id")
    private String signerId;

    /**
     * Creates an empty resend notification response.
     */
    public ResendNotificationResponse() {}

    /**
     * Returns the sent flag.
     *
     * @return the sent flag
     */
    public Boolean getIsSent() { return isSent; }

    /**
     * Sets the sent flag.
     *
     * @param isSent the sent flag
     */
    public void setIsSent(Boolean isSent) { this.isSent = isSent; }

    /**
     * Returns the document ID.
     *
     * @return the document ID
     */
    public String getDocumentId() { return documentId; }

    /**
     * Sets the document ID.
     *
     * @param documentId the document ID
     */
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    /**
     * Returns the signer ID.
     *
     * @return the signer ID
     */
    public String getSignerId() { return signerId; }

    /**
     * Sets the signer ID.
     *
     * @param signerId the signer ID
     */
    public void setSignerId(String signerId) { this.signerId = signerId; }
}
