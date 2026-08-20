package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** One zero-filled monthly ({@code YYYY-MM}) or daily ({@code YYYY-MM-DD}) document KPI row. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentStatsRow {

    private String period;
    @JsonProperty("documents_uploaded") private Integer documentsUploaded;
    @JsonProperty("documents_sent") private Integer documentsSent;
    @JsonProperty("signature_requests") private Integer signatureRequests;
    @JsonProperty("signature_requests_email") private Integer signatureRequestsEmail;
    @JsonProperty("signature_requests_whatsapp") private Integer signatureRequestsWhatsapp;
    @JsonProperty("signature_requests_viewed") private Integer signatureRequestsViewed;
    @JsonProperty("signature_requests_completed") private Integer signatureRequestsCompleted;
    @JsonProperty("documents_certified") private Integer documentsCertified;

    /**
     * Creates an empty document stats row.
     */
    public DocumentStatsRow() {}

    /**
     * Returns the period.
     *
     * @return the period
     */
    public String getPeriod() { return period; }

    /**
     * Sets the period.
     *
     * @param period the period
     */
    public void setPeriod(String period) { this.period = period; }

    /**
     * Returns the documents uploaded.
     *
     * @return the documents uploaded
     */
    public Integer getDocumentsUploaded() { return documentsUploaded; }

    /**
     * Sets the documents uploaded.
     *
     * @param value the documents uploaded
     */
    public void setDocumentsUploaded(Integer value) { documentsUploaded = value; }

    /**
     * Returns the documents sent.
     *
     * @return the documents sent
     */
    public Integer getDocumentsSent() { return documentsSent; }

    /**
     * Sets the documents sent.
     *
     * @param value the documents sent
     */
    public void setDocumentsSent(Integer value) { documentsSent = value; }

    /**
     * Returns the signature requests.
     *
     * @return the signature requests
     */
    public Integer getSignatureRequests() { return signatureRequests; }

    /**
     * Sets the signature requests.
     *
     * @param value the signature requests
     */
    public void setSignatureRequests(Integer value) { signatureRequests = value; }

    /**
     * Returns the signature requests email.
     *
     * @return the signature requests email
     */
    public Integer getSignatureRequestsEmail() { return signatureRequestsEmail; }

    /**
     * Sets the signature requests email.
     *
     * @param value the signature requests email
     */
    public void setSignatureRequestsEmail(Integer value) { signatureRequestsEmail = value; }

    /**
     * Returns the signature requests whatsapp.
     *
     * @return the signature requests whatsapp
     */
    public Integer getSignatureRequestsWhatsapp() { return signatureRequestsWhatsapp; }

    /**
     * Sets the signature requests whatsapp.
     *
     * @param value the signature requests whatsapp
     */
    public void setSignatureRequestsWhatsapp(Integer value) { signatureRequestsWhatsapp = value; }

    /**
     * Returns the signature requests viewed.
     *
     * @return the signature requests viewed
     */
    public Integer getSignatureRequestsViewed() { return signatureRequestsViewed; }

    /**
     * Sets the signature requests viewed.
     *
     * @param value the signature requests viewed
     */
    public void setSignatureRequestsViewed(Integer value) { signatureRequestsViewed = value; }

    /**
     * Returns the signature requests completed.
     *
     * @return the signature requests completed
     */
    public Integer getSignatureRequestsCompleted() { return signatureRequestsCompleted; }

    /**
     * Sets the signature requests completed.
     *
     * @param value the signature requests completed
     */
    public void setSignatureRequestsCompleted(Integer value) { signatureRequestsCompleted = value; }

    /**
     * Returns the documents certified.
     *
     * @return the documents certified
     */
    public Integer getDocumentsCertified() { return documentsCertified; }

    /**
     * Sets the documents certified.
     *
     * @param value the documents certified
     */
    public void setDocumentsCertified(Integer value) { documentsCertified = value; }
}
