package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


/** Every e-mail and push alert, keyed by notification type. `false` means the workspace never sends it. New keys may be added. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationPolicyEvents {

    /** Creates an empty response. */
    public NotificationPolicyEvents() {}

    @JsonProperty("DocumentCompleted")
    private Boolean documentCompleted;

    /** DocumentCompleted
     * @return the DocumentCompleted value, or null when absent
     */
    public Boolean getDocumentCompleted() { return documentCompleted; }

    /** Sets DocumentCompleted.
     * @param value the DocumentCompleted value
     */
    public void setDocumentCompleted(Boolean value) { documentCompleted = value; }

    @JsonProperty("SignerDeclined")
    private Boolean signerDeclined;

    /** SignerDeclined
     * @return the SignerDeclined value, or null when absent
     */
    public Boolean getSignerDeclined() { return signerDeclined; }

    /** Sets SignerDeclined.
     * @param value the SignerDeclined value
     */
    public void setSignerDeclined(Boolean value) { signerDeclined = value; }

    @JsonProperty("DocumentCancelled")
    private Boolean documentCancelled;

    /** DocumentCancelled
     * @return the DocumentCancelled value, or null when absent
     */
    public Boolean getDocumentCancelled() { return documentCancelled; }

    /** Sets DocumentCancelled.
     * @param value the DocumentCancelled value
     */
    public void setDocumentCancelled(Boolean value) { documentCancelled = value; }

    @JsonProperty("DocumentAboutToExpire")
    private Boolean documentAboutToExpire;

    /** DocumentAboutToExpire
     * @return the DocumentAboutToExpire value, or null when absent
     */
    public Boolean getDocumentAboutToExpire() { return documentAboutToExpire; }

    /** Sets DocumentAboutToExpire.
     * @param value the DocumentAboutToExpire value
     */
    public void setDocumentAboutToExpire(Boolean value) { documentAboutToExpire = value; }

    @JsonProperty("DocumentExpired")
    private Boolean documentExpired;

    /** DocumentExpired
     * @return the DocumentExpired value, or null when absent
     */
    public Boolean getDocumentExpired() { return documentExpired; }

    /** Sets DocumentExpired.
     * @param value the DocumentExpired value
     */
    public void setDocumentExpired(Boolean value) { documentExpired = value; }

    @JsonProperty("DocumentExpirationReset")
    private Boolean documentExpirationReset;

    /** DocumentExpirationReset
     * @return the DocumentExpirationReset value, or null when absent
     */
    public Boolean getDocumentExpirationReset() { return documentExpirationReset; }

    /** Sets DocumentExpirationReset.
     * @param value the DocumentExpirationReset value
     */
    public void setDocumentExpirationReset(Boolean value) { documentExpirationReset = value; }

    @JsonProperty("DocumentProcessingFailed")
    private Boolean documentProcessingFailed;

    /** DocumentProcessingFailed
     * @return the DocumentProcessingFailed value, or null when absent
     */
    public Boolean getDocumentProcessingFailed() { return documentProcessingFailed; }

    /** Sets DocumentProcessingFailed.
     * @param value the DocumentProcessingFailed value
     */
    public void setDocumentProcessingFailed(Boolean value) { documentProcessingFailed = value; }

    @JsonProperty("TemplateProcessingFailed")
    private Boolean templateProcessingFailed;

    /** TemplateProcessingFailed
     * @return the TemplateProcessingFailed value, or null when absent
     */
    public Boolean getTemplateProcessingFailed() { return templateProcessingFailed; }

    /** Sets TemplateProcessingFailed.
     * @param value the TemplateProcessingFailed value
     */
    public void setTemplateProcessingFailed(Boolean value) { templateProcessingFailed = value; }

    @JsonProperty("SignerWhatsappFailed")
    private Boolean signerWhatsappFailed;

    /** SignerWhatsappFailed
     * @return the SignerWhatsappFailed value, or null when absent
     */
    public Boolean getSignerWhatsappFailed() { return signerWhatsappFailed; }

    /** Sets SignerWhatsappFailed.
     * @param value the SignerWhatsappFailed value
     */
    public void setSignerWhatsappFailed(Boolean value) { signerWhatsappFailed = value; }

    @JsonProperty("ContractRenewalNotice")
    private Boolean contractRenewalNotice;

    /** ContractRenewalNotice
     * @return the ContractRenewalNotice value, or null when absent
     */
    public Boolean getContractRenewalNotice() { return contractRenewalNotice; }

    /** Sets ContractRenewalNotice.
     * @param value the ContractRenewalNotice value
     */
    public void setContractRenewalNotice(Boolean value) { contractRenewalNotice = value; }

    @JsonProperty("ContractExpiring")
    private Boolean contractExpiring;

    /** ContractExpiring
     * @return the ContractExpiring value, or null when absent
     */
    public Boolean getContractExpiring() { return contractExpiring; }

    /** Sets ContractExpiring.
     * @param value the ContractExpiring value
     */
    public void setContractExpiring(Boolean value) { contractExpiring = value; }

    @JsonProperty("ContractExpired")
    private Boolean contractExpired;

    /** ContractExpired
     * @return the ContractExpired value, or null when absent
     */
    public Boolean getContractExpired() { return contractExpired; }

    /** Sets ContractExpired.
     * @param value the ContractExpired value
     */
    public void setContractExpired(Boolean value) { contractExpired = value; }

    @JsonProperty("ContractRenewed")
    private Boolean contractRenewed;

    /** ContractRenewed
     * @return the ContractRenewed value, or null when absent
     */
    public Boolean getContractRenewed() { return contractRenewed; }

    /** Sets ContractRenewed.
     * @param value the ContractRenewed value
     */
    public void setContractRenewed(Boolean value) { contractRenewed = value; }

    @JsonProperty("SignerViewed")
    private Boolean signerViewed;

    /** SignerViewed
     * @return the SignerViewed value, or null when absent
     */
    public Boolean getSignerViewed() { return signerViewed; }

    /** Sets SignerViewed.
     * @param value the SignerViewed value
     */
    public void setSignerViewed(Boolean value) { signerViewed = value; }

    @JsonProperty("SignerSigned")
    private Boolean signerSigned;

    /** SignerSigned
     * @return the SignerSigned value, or null when absent
     */
    public Boolean getSignerSigned() { return signerSigned; }

    /** Sets SignerSigned.
     * @param value the SignerSigned value
     */
    public void setSignerSigned(Boolean value) { signerSigned = value; }

    @JsonProperty("SignerIdentityReviewRequired")
    private Boolean signerIdentityReviewRequired;

    /** Turns off the push only; the identity review e-mail is always sent.
     * @return the SignerIdentityReviewRequired value, or null when absent
     */
    public Boolean getSignerIdentityReviewRequired() { return signerIdentityReviewRequired; }

    /** Sets SignerIdentityReviewRequired.
     * @param value the SignerIdentityReviewRequired value
     */
    public void setSignerIdentityReviewRequired(Boolean value) { signerIdentityReviewRequired = value; }

}
