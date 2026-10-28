package com.assinafy.sdk.request;

import com.assinafy.sdk.models.ContractExternalReference;
import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Partial contract terms for {@code PUT
 * /accounts/{accountId}/documents/{documentId}/contract}.
 *
 * <p>Only setters that are called appear in the JSON body. Calling a nullable setter with
 * {@code null} therefore sends an explicit JSON {@code null}, which clears that term according to
 * the API contract.
 */
public class SetContractRequest {

    private final Map<String, Object> attributes = new LinkedHashMap<>();

    /** Creates an empty contract request. */
    public SetContractRequest() {}

    /** {@return a new fluent builder} */
    public static Builder builder() { return new Builder(); }

    /**
     * Returns the exact fields that will be sent.
     *
     * @return immutable wire-name/value fields, including explicitly supplied nulls
     */
    @JsonAnyGetter
    public Map<String, Object> getAttributes() { return Collections.unmodifiableMap(attributes); }

    /**
     * Sets {@code draft}, {@code active}, {@code expired}, or {@code terminated}.
     *
     * @param status {@code draft}, {@code active}, {@code expired}, or {@code terminated}
     */
    public void setStatus(String status) { attributes.put("status", status); }

    /**
     * Sets counterparty name, or {@code null} to clear it.
     *
     * @param value counterparty name, or {@code null} to clear it
     */
    public void setCounterpartyName(String value) { attributes.put("counterparty_name", value); }

    /**
     * Sets CPF/CNPJ, or {@code null} to clear it.
     *
     * @param value CPF/CNPJ, or {@code null} to clear it
     */
    public void setCounterpartyTaxpayerId(String value) { attributes.put("counterparty_taxpayer_id", value); }

    /**
     * Sets nonnegative contract value, or {@code null} to clear it.
     *
     * @param value nonnegative contract value, or {@code null} to clear it
     */
    public void setValue(BigDecimal value) { attributes.put("value", value); }

    /**
     * Sets three-letter ISO 4217 code, or {@code null} to reset it.
     *
     * @param currency three-letter ISO 4217 code, or {@code null} to reset it
     */
    public void setCurrency(String currency) { attributes.put("currency", currency); }

    /**
     * Sets start date in {@code YYYY-MM-DD} form, or {@code null} to clear it.
     *
     * @param date start date in {@code YYYY-MM-DD} form, or {@code null} to clear it
     */
    public void setStartDate(String date) { attributes.put("start_date", date); }

    /**
     * Sets end date in {@code YYYY-MM-DD} form, or {@code null} to clear it.
     *
     * @param date end date in {@code YYYY-MM-DD} form, or {@code null} to clear it
     */
    public void setEndDate(String date) { attributes.put("end_date", date); }

    /**
     * Sets whether the lifecycle renews automatically; {@code null} resets to false.
     *
     * @param value whether the lifecycle renews automatically; {@code null} resets to false
     */
    public void setAutoRenew(Boolean value) { attributes.put("auto_renew", value); }

    /**
     * Sets renewal term from 1 to 600 months, or {@code null} for the default.
     *
     * @param months renewal term from 1 to 600 months, or {@code null} for the default
     */
    public void setRenewalTermMonths(Integer months) { attributes.put("renewal_term_months", months); }

    /**
     * Sets notice period from 0 to 3650 days, or {@code null} to disable it.
     *
     * @param days notice period from 0 to 3650 days, or {@code null} to disable it
     */
    public void setNoticePeriodDays(Integer days) { attributes.put("notice_period_days", days); }

    /**
     * Sets active workspace-member ID, or {@code null} to clear it.
     *
     * @param ownerId active workspace-member ID, or {@code null} to clear it
     */
    public void setOwnerId(String ownerId) { attributes.put("owner_id", ownerId); }

    /**
     * Sets up to 50 integration fields, or {@code null} to clear them.
     *
     * @param metadata up to 50 integration fields, or {@code null} to clear them
     */
    public void setMetadata(Map<String, String> metadata) { attributes.put("metadata", metadata); }

    /**
     * Sets replacement external-reference list; empty or {@code null} clears it.
     *
     * @param references replacement external-reference list; empty or {@code null} clears it
     */
    public void setExternalReferences(List<ContractExternalReference> references) {
        attributes.put("external_references", references);
    }

    /**
     * Sets parent contract document, or {@code null} to unlink a pending amendment.
     *
     * @param documentId parent contract document, or {@code null} to unlink a pending amendment
     */
    public void setParentDocumentId(String documentId) { attributes.put("parent_document_id", documentId); }

    /**
     * Sets {@code amendment}, {@code renewal}, or {@code termination}.
     *
     * @param type {@code amendment}, {@code renewal}, or {@code termination}
     */
    public void setRelationType(String type) { attributes.put("relation_type", type); }

    /**
     * Sets optimistic-concurrency timestamp, or {@code null} for last-write-wins.
     *
     * @param timestamp optimistic-concurrency timestamp, or {@code null} for last-write-wins
     */
    public void setUpdatedAt(String timestamp) { attributes.put("updated_at", timestamp); }

    /** Fluent builder for {@link SetContractRequest}. */
    public static final class Builder {
        private final SetContractRequest request = new SetContractRequest();

        /** Creates an empty builder. */
        public Builder() {}

        /**
         * Sets contract status.
         *
         * @param value contract status
         * @return this builder
         */
        public Builder status(String value) { request.setStatus(value); return this; }

        /**
         * Sets counterparty name, or {@code null}.
         *
         * @param value counterparty name, or {@code null}
         * @return this builder
         */
        public Builder counterpartyName(String value) { request.setCounterpartyName(value); return this; }

        /**
         * Sets CPF/CNPJ, or {@code null}.
         *
         * @param value CPF/CNPJ, or {@code null}
         * @return this builder
         */
        public Builder counterpartyTaxpayerId(String value) {
            request.setCounterpartyTaxpayerId(value); return this;
        }

        /**
         * Sets contract value, or {@code null}.
         *
         * @param value contract value, or {@code null}
         * @return this builder
         */
        public Builder value(BigDecimal value) { request.setValue(value); return this; }

        /**
         * Sets currency code, or {@code null}.
         *
         * @param value currency code, or {@code null}
         * @return this builder
         */
        public Builder currency(String value) { request.setCurrency(value); return this; }

        /**
         * Sets start date, or {@code null}.
         *
         * @param value start date, or {@code null}
         * @return this builder
         */
        public Builder startDate(String value) { request.setStartDate(value); return this; }

        /**
         * Sets end date, or {@code null}.
         *
         * @param value end date, or {@code null}
         * @return this builder
         */
        public Builder endDate(String value) { request.setEndDate(value); return this; }

        /**
         * Sets automatic-renewal flag, or {@code null}.
         *
         * @param value automatic-renewal flag, or {@code null}
         * @return this builder
         */
        public Builder autoRenew(Boolean value) { request.setAutoRenew(value); return this; }

        /**
         * Sets renewal term in months, or {@code null}.
         *
         * @param value renewal term in months, or {@code null}
         * @return this builder
         */
        public Builder renewalTermMonths(Integer value) { request.setRenewalTermMonths(value); return this; }

        /**
         * Sets notice period in days, or {@code null}.
         *
         * @param value notice period in days, or {@code null}
         * @return this builder
         */
        public Builder noticePeriodDays(Integer value) { request.setNoticePeriodDays(value); return this; }

        /**
         * Sets owner ID, or {@code null}.
         *
         * @param value owner ID, or {@code null}
         * @return this builder
         */
        public Builder ownerId(String value) { request.setOwnerId(value); return this; }

        /**
         * Sets metadata, or {@code null}.
         *
         * @param value metadata, or {@code null}
         * @return this builder
         */
        public Builder metadata(Map<String, String> value) { request.setMetadata(value); return this; }

        /**
         * Sets replacement references, empty or {@code null} to clear.
         *
         * @param value replacement references, empty or {@code null} to clear
         * @return this builder
         */
        public Builder externalReferences(List<ContractExternalReference> value) {
            request.setExternalReferences(value); return this;
        }

        /**
         * Sets parent document ID, or {@code null}.
         *
         * @param value parent document ID, or {@code null}
         * @return this builder
         */
        public Builder parentDocumentId(String value) { request.setParentDocumentId(value); return this; }

        /**
         * Sets relation type.
         *
         * @param value relation type
         * @return this builder
         */
        public Builder relationType(String value) { request.setRelationType(value); return this; }

        /**
         * Sets optimistic-concurrency timestamp, or {@code null}.
         *
         * @param value optimistic-concurrency timestamp, or {@code null}
         * @return this builder
         */
        public Builder updatedAt(String value) { request.setUpdatedAt(value); return this; }

        /** {@return the configured request} */
        public SetContractRequest build() { return request; }
    }
}
