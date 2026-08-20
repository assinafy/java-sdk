package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Owner-facing document email preferences returned by
 * {@code GET /users/self/notification-preferences}. Every property is present in a successful API
 * response; {@code true} means that email is enabled.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationPreferences {

    @JsonProperty("DocumentCompleted")
    private Boolean documentCompleted;
    @JsonProperty("SignerDeclined")
    private Boolean signerDeclined;
    @JsonProperty("DocumentCancelled")
    private Boolean documentCancelled;
    @JsonProperty("DocumentAboutToExpire")
    private Boolean documentAboutToExpire;
    @JsonProperty("DocumentExpired")
    private Boolean documentExpired;
    @JsonProperty("DocumentExpirationReset")
    private Boolean documentExpirationReset;
    @JsonProperty("DocumentProcessingFailed")
    private Boolean documentProcessingFailed;
    @JsonProperty("TemplateProcessingFailed")
    private Boolean templateProcessingFailed;
    @JsonProperty("SignerWhatsappFailed")
    private Boolean signerWhatsappFailed;

    /** Creates an empty notification-preferences model. */
    public NotificationPreferences() {}

    /**
     * Whether to email when every signer has signed and the document is certified.
     *
     * @return the document completed
     */
    public Boolean getDocumentCompleted() { return documentCompleted; }

    /**
     * Sets the document completed.
     *
     * @param value the document completed
     */
    public void setDocumentCompleted(Boolean value) { documentCompleted = value; }

    /**
     * Whether to email when a signer declines.
     *
     * @return the signer declined
     */
    public Boolean getSignerDeclined() { return signerDeclined; }

    /**
     * Sets the signer declined.
     *
     * @param value the signer declined
     */
    public void setSignerDeclined(Boolean value) { signerDeclined = value; }

    /**
     * Whether to email when a document is cancelled.
     *
     * @return the document cancelled
     */
    public Boolean getDocumentCancelled() { return documentCancelled; }

    /**
     * Sets the document cancelled.
     *
     * @param value the document cancelled
     */
    public void setDocumentCancelled(Boolean value) { documentCancelled = value; }

    /**
     * Whether to email when a signature deadline is approaching.
     *
     * @return the document about to expire
     */
    public Boolean getDocumentAboutToExpire() { return documentAboutToExpire; }

    /**
     * Sets the document about to expire.
     *
     * @param value the document about to expire
     */
    public void setDocumentAboutToExpire(Boolean value) { documentAboutToExpire = value; }

    /**
     * Whether to email when a signature deadline passes.
     *
     * @return the document expired
     */
    public Boolean getDocumentExpired() { return documentExpired; }

    /**
     * Sets the document expired.
     *
     * @param value the document expired
     */
    public void setDocumentExpired(Boolean value) { documentExpired = value; }

    /**
     * Whether to email when a signature deadline is extended.
     *
     * @return the document expiration reset
     */
    public Boolean getDocumentExpirationReset() { return documentExpirationReset; }

    /**
     * Sets the document expiration reset.
     *
     * @param value the document expiration reset
     */
    public void setDocumentExpirationReset(Boolean value) { documentExpirationReset = value; }

    /**
     * Whether to email when an uploaded document cannot be processed.
     *
     * @return the document processing failed
     */
    public Boolean getDocumentProcessingFailed() { return documentProcessingFailed; }

    /**
     * Sets the document processing failed.
     *
     * @param value the document processing failed
     */
    public void setDocumentProcessingFailed(Boolean value) { documentProcessingFailed = value; }

    /**
     * Whether to email when a template cannot be processed.
     *
     * @return the template processing failed
     */
    public Boolean getTemplateProcessingFailed() { return templateProcessingFailed; }

    /**
     * Sets the template processing failed.
     *
     * @param value the template processing failed
     */
    public void setTemplateProcessingFailed(Boolean value) { templateProcessingFailed = value; }

    /**
     * Whether to email when a signer WhatsApp notification cannot be delivered.
     *
     * @return the signer WhatsApp failure preference
     */
    public Boolean getSignerWhatsappFailed() { return signerWhatsappFailed; }

    /**
     * Sets the signer WhatsApp failure preference.
     *
     * @param value the signer WhatsApp failure preference
     */
    public void setSignerWhatsappFailed(Boolean value) { signerWhatsappFailed = value; }
}
