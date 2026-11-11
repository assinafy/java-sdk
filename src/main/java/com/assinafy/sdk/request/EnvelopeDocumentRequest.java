package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.math.BigDecimal;

/** Document input for envelope creation and POST /accounts/{accountId}/envelopes/{envelopeId}/documents. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EnvelopeDocumentRequest {
    /** Creates an empty request. */
    public EnvelopeDocumentRequest() {}

    /** {@return a new builder} */
    public static Builder builder() { return new Builder(); }

    @JsonProperty("id")
    private String id;

    /** Processed, unassigned document ID.
     * @return the id value, or null when absent
     */
    public String getId() { return id; }

    /** Sets id.
     * @param value the id value
     */
    public void setId(String value) { id = value; }

    @JsonProperty("method")
    private String method;

    /** virtual (default) or collect.
     * @return the method value, or null when absent
     */
    public String getMethod() { return method; }

    /** Sets method.
     * @param value the method value
     */
    public void setMethod(String value) { method = value; }

    @JsonProperty("entries")
    private List<Object> entries;

    /** Collect pages with page_id and fields containing signer_id, field_id and display_settings.
     * @return the entries value, or null when absent
     */
    public List<Object> getEntries() { return entries; }

    /** Sets entries.
     * @param value the entries value
     */
    public void setEntries(List<Object> value) { entries = value; }

    @JsonProperty("signer_ids")
    private List<String> signerIds;

    /** Unique IDs of the envelope signers on this document.
     * @return the signer_ids value, or null when absent
     */
    public List<String> getSignerIds() { return signerIds; }

    /** Sets signer_ids.
     * @param value the signer_ids value
     */
    public void setSignerIds(List<String> value) { signerIds = value; }

    @JsonProperty("expires_at")
    private String expiresAt;

    /** ISO 8601 expiration at least one hour ahead; null omits it.
     * @return the expires_at value, or null when absent
     */
    public String getExpiresAt() { return expiresAt; }

    /** Sets expires_at.
     * @param value the expires_at value
     */
    public void setExpiresAt(String value) { expiresAt = value; }

    @JsonProperty("copy_receiver_ids")
    private List<String> copyReceiverIds;

    /** Signer IDs that only receive a copy of this document.
     * @return the copy_receiver_ids value, or null when absent
     */
    public List<String> getCopyReceiverIds() { return copyReceiverIds; }

    /** Sets copy_receiver_ids.
     * @param value the copy_receiver_ids value
     */
    public void setCopyReceiverIds(List<String> value) { copyReceiverIds = value; }

    @JsonProperty("max_total_credits")
    private BigDecimal maxTotalCredits;

    /** Nonnegative cost ceiling for adding this document; ignored during envelope creation.
     * @return the max_total_credits value, or null when absent
     */
    public BigDecimal getMaxTotalCredits() { return maxTotalCredits; }

    /** Sets max_total_credits.
     * @param value the max_total_credits value
     */
    public void setMaxTotalCredits(BigDecimal value) { maxTotalCredits = value; }

    /** Builder for EnvelopeDocumentRequest. */
    public static final class Builder {
        private final EnvelopeDocumentRequest request = new EnvelopeDocumentRequest();
        /** Creates an empty builder. */
        public Builder() {}
        /** Sets id.
         * @param value the id value
         * @return this builder
         */
        public Builder id(String value) { request.setId(value); return this; }
        /** Sets method.
         * @param value the method value
         * @return this builder
         */
        public Builder method(String value) { request.setMethod(value); return this; }
        /** Sets entries.
         * @param value the entries value
         * @return this builder
         */
        public Builder entries(List<Object> value) { request.setEntries(value); return this; }
        /** Sets signer_ids.
         * @param value the signer_ids value
         * @return this builder
         */
        public Builder signerIds(List<String> value) { request.setSignerIds(value); return this; }
        /** Sets expires_at.
         * @param value the expires_at value
         * @return this builder
         */
        public Builder expiresAt(String value) { request.setExpiresAt(value); return this; }
        /** Sets copy_receiver_ids.
         * @param value the copy_receiver_ids value
         * @return this builder
         */
        public Builder copyReceiverIds(List<String> value) { request.setCopyReceiverIds(value); return this; }
        /** Sets max_total_credits.
         * @param value the max_total_credits value
         * @return this builder
         */
        public Builder maxTotalCredits(BigDecimal value) { request.setMaxTotalCredits(value); return this; }
        /** {@return the configured request} */
        public EnvelopeDocumentRequest build() { return request; }
    }
}
