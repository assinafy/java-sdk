package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.math.BigDecimal;

/** Request for POST /accounts/{accountId}/envelopes and /envelopes/estimate-cost. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateEnvelopeRequest {
    /** Creates an empty request. */
    public CreateEnvelopeRequest() {}

    /** {@return a new builder} */
    public static Builder builder() { return new Builder(); }

    @JsonProperty("name")
    private String name;

    /** Envelope name, 1 to 255 characters, without a less-than sign.
     * @return the name value, or null when absent
     */
    public String getName() { return name; }

    /** Sets name.
     * @param value the name value
     */
    public void setName(String value) { name = value; }

    @JsonProperty("message")
    private String message;

    /** Optional invitation message.
     * @return the message value, or null when absent
     */
    public String getMessage() { return message; }

    /** Sets message.
     * @param value the message value
     */
    public void setMessage(String value) { message = value; }

    @JsonProperty("signers")
    private List<SignerReference> signers;

    /** One to twenty unique existing signers; steps are not supported.
     * @return the signers value, or null when absent
     */
    public List<SignerReference> getSigners() { return signers; }

    /** Sets signers.
     * @param value the signers value
     */
    public void setSigners(List<SignerReference> value) { signers = value; }

    @JsonProperty("documents")
    private List<EnvelopeDocumentRequest> documents;

    /** One to twenty processed unassigned documents in envelope order.
     * @return the documents value, or null when absent
     */
    public List<EnvelopeDocumentRequest> getDocuments() { return documents; }

    /** Sets documents.
     * @param value the documents value
     */
    public void setDocuments(List<EnvelopeDocumentRequest> value) { documents = value; }

    @JsonProperty("max_total_credits")
    private BigDecimal maxTotalCredits;

    /** Nonnegative cost ceiling; ignored when estimating.
     * @return the max_total_credits value, or null when absent
     */
    public BigDecimal getMaxTotalCredits() { return maxTotalCredits; }

    /** Sets max_total_credits.
     * @param value the max_total_credits value
     */
    public void setMaxTotalCredits(BigDecimal value) { maxTotalCredits = value; }

    /** Builder for CreateEnvelopeRequest. */
    public static final class Builder {
        private final CreateEnvelopeRequest request = new CreateEnvelopeRequest();
        /** Creates an empty builder. */
        public Builder() {}
        /** Sets name.
         * @param value the name value
         * @return this builder
         */
        public Builder name(String value) { request.setName(value); return this; }
        /** Sets message.
         * @param value the message value
         * @return this builder
         */
        public Builder message(String value) { request.setMessage(value); return this; }
        /** Sets signers.
         * @param value the signers value
         * @return this builder
         */
        public Builder signers(List<SignerReference> value) { request.setSigners(value); return this; }
        /** Sets documents.
         * @param value the documents value
         * @return this builder
         */
        public Builder documents(List<EnvelopeDocumentRequest> value) { request.setDocuments(value); return this; }
        /** Sets max_total_credits.
         * @param value the max_total_credits value
         * @return this builder
         */
        public Builder maxTotalCredits(BigDecimal value) { request.setMaxTotalCredits(value); return this; }
        /** {@return the configured request} */
        public CreateEnvelopeRequest build() { return request; }
    }
}
