package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


/** Envelope reminder result. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnvelopeResendResult {

    /** Creates an empty response. */
    public EnvelopeResendResult() {}

    @JsonProperty("envelope_id")
    private String envelopeId;

    /** Envelope ID.
     * @return the envelope_id value, or null when absent
     */
    public String getEnvelopeId() { return envelopeId; }

    /** Sets envelope_id.
     * @param value the envelope_id value
     */
    public void setEnvelopeId(String value) { envelopeId = value; }

    @JsonProperty("signer_id")
    private String signerId;

    /** Signer ID.
     * @return the signer_id value, or null when absent
     */
    public String getSignerId() { return signerId; }

    /** Sets signer_id.
     * @param value the signer_id value
     */
    public void setSignerId(String value) { signerId = value; }

    @JsonProperty("is_sent")
    private Boolean isSent;

    /** Whether every notification method sent the reminder.
     * @return the is_sent value, or null when absent
     */
    public Boolean getIsSent() { return isSent; }

    /** Sets is_sent.
     * @param value the is_sent value
     */
    public void setIsSent(Boolean value) { isSent = value; }

}
