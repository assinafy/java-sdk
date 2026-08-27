package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** One zero-filled monthly ({@code YYYY-MM}) or daily ({@code YYYY-MM-DD}) document KPI row. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentStatsRow {

    private String period;
    @JsonProperty("documents_uploaded") private Integer documentsUploaded;
    @JsonProperty("documents_sent") private Integer documentsSent;
    @JsonProperty("signature_requests") private Integer signatureRequests;
    @JsonProperty("signature_requests_notification_email")
    @JsonAlias("signature_requests_email")
    private Integer signatureRequestsNotificationEmail;
    @JsonProperty("signature_requests_notification_whatsapp")
    @JsonAlias("signature_requests_whatsapp")
    private Integer signatureRequestsNotificationWhatsapp;
    @JsonProperty("signature_requests_notification_bypass")
    private Integer signatureRequestsNotificationBypass;
    @JsonProperty("signature_requests_verification_email")
    private Integer signatureRequestsVerificationEmail;
    @JsonProperty("signature_requests_verification_whatsapp")
    private Integer signatureRequestsVerificationWhatsapp;
    @JsonProperty("signature_requests_verification_bypass")
    private Integer signatureRequestsVerificationBypass;
    @JsonProperty("signature_requests_verification_digital_certificate")
    private Integer signatureRequestsVerificationDigitalCertificate;
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
     * Returns requests for which an email notification was sent.
     *
     * @return email-notified signature requests
     */
    public Integer getSignatureRequestsNotificationEmail() { return signatureRequestsNotificationEmail; }

    /**
     * Sets requests for which an email notification was sent.
     *
     * @param value email-notified signature requests
     */
    public void setSignatureRequestsNotificationEmail(Integer value) { signatureRequestsNotificationEmail = value; }

    /**
     * Returns requests for which a WhatsApp notification was sent.
     *
     * @return WhatsApp-notified signature requests
     */
    public Integer getSignatureRequestsNotificationWhatsapp() { return signatureRequestsNotificationWhatsapp; }

    /**
     * Sets requests for which a WhatsApp notification was sent.
     *
     * @param value WhatsApp-notified signature requests
     */
    public void setSignatureRequestsNotificationWhatsapp(Integer value) { signatureRequestsNotificationWhatsapp = value; }

    /**
     * Returns requests for which notification delivery was bypassed.
     *
     * @return notification-bypassed signature requests
     */
    public Integer getSignatureRequestsNotificationBypass() { return signatureRequestsNotificationBypass; }

    /**
     * Sets requests for which notification delivery was bypassed.
     *
     * @param value notification-bypassed signature requests
     */
    public void setSignatureRequestsNotificationBypass(Integer value) { signatureRequestsNotificationBypass = value; }

    /**
     * Returns requests verified using an email token.
     *
     * @return email-verified signature requests
     */
    public Integer getSignatureRequestsVerificationEmail() { return signatureRequestsVerificationEmail; }

    /**
     * Sets requests verified using an email token.
     *
     * @param value email-verified signature requests
     */
    public void setSignatureRequestsVerificationEmail(Integer value) { signatureRequestsVerificationEmail = value; }

    /**
     * Returns requests verified using a WhatsApp token.
     *
     * @return WhatsApp-verified signature requests
     */
    public Integer getSignatureRequestsVerificationWhatsapp() { return signatureRequestsVerificationWhatsapp; }

    /**
     * Sets requests verified using a WhatsApp token.
     *
     * @param value WhatsApp-verified signature requests
     */
    public void setSignatureRequestsVerificationWhatsapp(Integer value) { signatureRequestsVerificationWhatsapp = value; }

    /**
     * Returns requests signed without token verification.
     *
     * @return verification-bypassed signature requests
     */
    public Integer getSignatureRequestsVerificationBypass() { return signatureRequestsVerificationBypass; }

    /**
     * Sets requests signed without token verification.
     *
     * @param value verification-bypassed signature requests
     */
    public void setSignatureRequestsVerificationBypass(Integer value) { signatureRequestsVerificationBypass = value; }

    /**
     * Returns requests verified with an ICP-Brasil digital certificate.
     *
     * @return digital-certificate-verified signature requests
     */
    public Integer getSignatureRequestsVerificationDigitalCertificate() {
        return signatureRequestsVerificationDigitalCertificate;
    }

    /**
     * Sets requests verified with an ICP-Brasil digital certificate.
     *
     * @param value digital-certificate-verified signature requests
     */
    public void setSignatureRequestsVerificationDigitalCertificate(Integer value) {
        signatureRequestsVerificationDigitalCertificate = value;
    }

    /**
     * Returns email-notified requests using the legacy accessor name.
     *
     * @return email-notified signature requests
     * @deprecated Use {@link #getSignatureRequestsNotificationEmail()}.
     */
    @Deprecated
    @JsonIgnore
    public Integer getSignatureRequestsEmail() { return getSignatureRequestsNotificationEmail(); }

    /**
     * Sets email-notified requests using the legacy accessor name.
     *
     * @param value email-notified signature requests
     * @deprecated Use {@link #setSignatureRequestsNotificationEmail(Integer)}.
     */
    @Deprecated
    @JsonIgnore
    public void setSignatureRequestsEmail(Integer value) { setSignatureRequestsNotificationEmail(value); }

    /**
     * Returns WhatsApp-notified requests using the legacy accessor name.
     *
     * @return WhatsApp-notified signature requests
     * @deprecated Use {@link #getSignatureRequestsNotificationWhatsapp()}.
     */
    @Deprecated
    @JsonIgnore
    public Integer getSignatureRequestsWhatsapp() { return getSignatureRequestsNotificationWhatsapp(); }

    /**
     * Sets WhatsApp-notified requests using the legacy accessor name.
     *
     * @param value WhatsApp-notified signature requests
     * @deprecated Use {@link #setSignatureRequestsNotificationWhatsapp(Integer)}.
     */
    @Deprecated
    @JsonIgnore
    public void setSignatureRequestsWhatsapp(Integer value) { setSignatureRequestsNotificationWhatsapp(value); }

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
