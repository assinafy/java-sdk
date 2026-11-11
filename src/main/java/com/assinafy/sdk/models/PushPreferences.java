package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


/** Document alerts sent as push notifications to the user&#x27;s phones, keyed by notification type. `true` means the push is sent. Separate from the e-mail preferences. New keys may be added. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PushPreferences {

    /** Creates an empty response. */
    public PushPreferences() {}

    @JsonProperty("SignerViewed")
    private Boolean signerViewed;

    /** A signer opened the document for the first time.
     * @return the SignerViewed value, or null when absent
     */
    public Boolean getSignerViewed() { return signerViewed; }

    /** Sets SignerViewed.
     * @param value the SignerViewed value
     */
    public void setSignerViewed(Boolean value) { signerViewed = value; }

    @JsonProperty("SignerSigned")
    private Boolean signerSigned;

    /** A signer signed the document.
     * @return the SignerSigned value, or null when absent
     */
    public Boolean getSignerSigned() { return signerSigned; }

    /** Sets SignerSigned.
     * @param value the SignerSigned value
     */
    public void setSignerSigned(Boolean value) { signerSigned = value; }

    @JsonProperty("DocumentCompleted")
    private Boolean documentCompleted;

    /** Every signer has signed and the document is certified.
     * @return the DocumentCompleted value, or null when absent
     */
    public Boolean getDocumentCompleted() { return documentCompleted; }

    /** Sets DocumentCompleted.
     * @param value the DocumentCompleted value
     */
    public void setDocumentCompleted(Boolean value) { documentCompleted = value; }

    @JsonProperty("SignerDeclined")
    private Boolean signerDeclined;

    /** A signer declined to sign.
     * @return the SignerDeclined value, or null when absent
     */
    public Boolean getSignerDeclined() { return signerDeclined; }

    /** Sets SignerDeclined.
     * @param value the SignerDeclined value
     */
    public void setSignerDeclined(Boolean value) { signerDeclined = value; }

    @JsonProperty("DocumentAboutToExpire")
    private Boolean documentAboutToExpire;

    /** The signature deadline is approaching.
     * @return the DocumentAboutToExpire value, or null when absent
     */
    public Boolean getDocumentAboutToExpire() { return documentAboutToExpire; }

    /** Sets DocumentAboutToExpire.
     * @param value the DocumentAboutToExpire value
     */
    public void setDocumentAboutToExpire(Boolean value) { documentAboutToExpire = value; }

    @JsonProperty("DocumentExpired")
    private Boolean documentExpired;

    /** The signature deadline passed.
     * @return the DocumentExpired value, or null when absent
     */
    public Boolean getDocumentExpired() { return documentExpired; }

    /** Sets DocumentExpired.
     * @param value the DocumentExpired value
     */
    public void setDocumentExpired(Boolean value) { documentExpired = value; }

    @JsonProperty("SignerIdentityReviewRequired")
    private Boolean signerIdentityReviewRequired;

    /** A signer&#x27;s identity awaits the sender&#x27;s review. The e-mail for this is always sent.
     * @return the SignerIdentityReviewRequired value, or null when absent
     */
    public Boolean getSignerIdentityReviewRequired() { return signerIdentityReviewRequired; }

    /** Sets SignerIdentityReviewRequired.
     * @param value the SignerIdentityReviewRequired value
     */
    public void setSignerIdentityReviewRequired(Boolean value) { signerIdentityReviewRequired = value; }

    @JsonProperty("SignerWhatsappFailed")
    private Boolean signerWhatsappFailed;

    /** A WhatsApp notification to a signer could not be delivered.
     * @return the SignerWhatsappFailed value, or null when absent
     */
    public Boolean getSignerWhatsappFailed() { return signerWhatsappFailed; }

    /** Sets SignerWhatsappFailed.
     * @param value the SignerWhatsappFailed value
     */
    public void setSignerWhatsappFailed(Boolean value) { signerWhatsappFailed = value; }

}
