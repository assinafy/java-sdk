package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Contract terms tracked on a workspace document. */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("document_contract")
public final class DocumentContract implements ContractRecord {

    @JsonProperty("resource")
    private String resource;
    @JsonProperty("document_id")
    private String documentId;
    @JsonProperty("status")
    private String status;
    @JsonProperty("counterparty_name")
    private String counterpartyName;
    @JsonProperty("counterparty_taxpayer_id")
    private String counterpartyTaxpayerId;
    @JsonProperty("value")
    private BigDecimal value;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("start_date")
    private String startDate;
    @JsonProperty("end_date")
    private String endDate;
    @JsonProperty("auto_renew")
    private Boolean autoRenew;
    @JsonProperty("renewal_term_months")
    private Integer renewalTermMonths;
    @JsonProperty("notice_period_days")
    private Integer noticePeriodDays;
    @JsonProperty("term")
    private Integer term;
    @JsonProperty("owner_id")
    private String ownerId;
    @JsonProperty("metadata")
    private Map<String, String> metadata;
    @JsonProperty("external_references")
    private List<ContractExternalReference> externalReferences;
    @JsonProperty("document")
    private Document document;
    @JsonProperty("amendments")
    private List<DocumentContractAmendment> amendments;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;

    /** Creates an empty document contract. */
    public DocumentContract() {}

    @Override
    public String getResource() { return resource; }

    /**
     * Sets API resource discriminator.
     *
     * @param resource API resource discriminator
     */
    public void setResource(String resource) { this.resource = resource; }

    @Override
    public String getDocumentId() { return documentId; }

    /**
     * Sets contract document ID.
     *
     * @param documentId contract document ID
     */
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    /** {@return {@code draft}, {@code active}, {@code expired}, or {@code terminated}} */
    public String getStatus() { return status; }

    /**
     * Sets contract lifecycle status.
     *
     * @param status contract lifecycle status
     */
    public void setStatus(String status) { this.status = status; }

    /** {@return the counterparty name, or {@code null}} */
    public String getCounterpartyName() { return counterpartyName; }

    /**
     * Sets counterparty name, or {@code null}.
     *
     * @param counterpartyName counterparty name, or {@code null}
     */
    public void setCounterpartyName(String counterpartyName) { this.counterpartyName = counterpartyName; }

    /** {@return normalized CPF/CNPJ of the counterparty, or {@code null}} */
    public String getCounterpartyTaxpayerId() { return counterpartyTaxpayerId; }

    /**
     * Sets normalized CPF/CNPJ, or {@code null}.
     *
     * @param counterpartyTaxpayerId normalized CPF/CNPJ, or {@code null}
     */
    public void setCounterpartyTaxpayerId(String counterpartyTaxpayerId) {
        this.counterpartyTaxpayerId = counterpartyTaxpayerId;
    }

    /** {@return the monetary value, or {@code null}} */
    public BigDecimal getValue() { return value; }

    /**
     * Sets monetary value, or {@code null}.
     *
     * @param value monetary value, or {@code null}
     */
    public void setValue(BigDecimal value) { this.value = value; }

    /** {@return the ISO 4217 currency code, or {@code null}} */
    public String getCurrency() { return currency; }

    /**
     * Sets ISO 4217 currency code, or {@code null}.
     *
     * @param currency ISO 4217 currency code, or {@code null}
     */
    public void setCurrency(String currency) { this.currency = currency; }

    /** {@return the start date in {@code YYYY-MM-DD} form, or {@code null}} */
    public String getStartDate() { return startDate; }

    /**
     * Sets start date in {@code YYYY-MM-DD} form, or {@code null}.
     *
     * @param startDate start date in {@code YYYY-MM-DD} form, or {@code null}
     */
    public void setStartDate(String startDate) { this.startDate = startDate; }

    /** {@return the end date in {@code YYYY-MM-DD} form, or {@code null}} */
    public String getEndDate() { return endDate; }

    /**
     * Sets end date in {@code YYYY-MM-DD} form, or {@code null}.
     *
     * @param endDate end date in {@code YYYY-MM-DD} form, or {@code null}
     */
    public void setEndDate(String endDate) { this.endDate = endDate; }

    /** {@return whether the lifecycle automatically renews the contract} */
    public Boolean getAutoRenew() { return autoRenew; }

    /**
     * Sets whether the lifecycle automatically renews the contract.
     *
     * @param autoRenew whether the lifecycle automatically renews the contract
     */
    public void setAutoRenew(Boolean autoRenew) { this.autoRenew = autoRenew; }

    /** {@return the renewal term in months, or {@code null}} */
    public Integer getRenewalTermMonths() { return renewalTermMonths; }

    /**
     * Sets renewal term in months, or {@code null}.
     *
     * @param renewalTermMonths renewal term in months, or {@code null}
     */
    public void setRenewalTermMonths(Integer renewalTermMonths) { this.renewalTermMonths = renewalTermMonths; }

    /** {@return the advance notice period in days, or {@code null}} */
    public Integer getNoticePeriodDays() { return noticePeriodDays; }

    /**
     * Sets advance notice period in days, or {@code null}.
     *
     * @param noticePeriodDays advance notice period in days, or {@code null}
     */
    public void setNoticePeriodDays(Integer noticePeriodDays) { this.noticePeriodDays = noticePeriodDays; }

    /** {@return the current contract term, starting at 1} */
    public Integer getTerm() { return term; }

    /**
     * Sets current contract term.
     *
     * @param term current contract term
     */
    public void setTerm(Integer term) { this.term = term; }

    /** {@return the workspace-member owner ID, or {@code null}} */
    public String getOwnerId() { return ownerId; }

    /**
     * Sets workspace-member owner ID, or {@code null}.
     *
     * @param ownerId workspace-member owner ID, or {@code null}
     */
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    /** {@return integrator metadata, or {@code null}} */
    public Map<String, String> getMetadata() { return metadata; }

    /**
     * Sets integrator metadata, or {@code null}.
     *
     * @param metadata integrator metadata, or {@code null}
     */
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }

    /** {@return identifiers in external systems} */
    public List<ContractExternalReference> getExternalReferences() { return externalReferences; }

    /**
     * Sets identifiers in external systems.
     *
     * @param externalReferences identifiers in external systems
     */
    public void setExternalReferences(List<ContractExternalReference> externalReferences) {
        this.externalReferences = externalReferences;
    }

    /** {@return the expanded document when {@code expand=document} was requested, otherwise {@code null}} */
    public Document getDocument() { return document; }

    /**
     * Sets expanded document, or {@code null}.
     *
     * @param document expanded document, or {@code null}
     */
    public void setDocument(Document document) { this.document = document; }

    /** {@return ordered amendments on single and related-family reads, otherwise {@code null}} */
    public List<DocumentContractAmendment> getAmendments() { return amendments; }

    /**
     * Sets ordered contract amendments.
     *
     * @param amendments ordered contract amendments
     */
    public void setAmendments(List<DocumentContractAmendment> amendments) { this.amendments = amendments; }

    /** {@return the creation timestamp} */
    public String getCreatedAt() { return createdAt; }

    /**
     * Sets creation timestamp.
     *
     * @param createdAt creation timestamp
     */
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    /** {@return the optimistic-concurrency timestamp} */
    public String getUpdatedAt() { return updatedAt; }

    /**
     * Sets optimistic-concurrency timestamp.
     *
     * @param updatedAt optimistic-concurrency timestamp
     */
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
