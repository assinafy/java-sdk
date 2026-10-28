package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Map;

/** A document linked to amend, renew, or terminate a parent contract. */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("document_contract_amendment")
public final class DocumentContractAmendment implements ContractRecord {

    @JsonProperty("resource")
    private String resource;
    @JsonProperty("document_id")
    private String documentId;
    @JsonProperty("document_name")
    private String documentName;
    @JsonProperty("parent_document_id")
    private String parentDocumentId;
    @JsonProperty("relation_type")
    private String relationType;
    @JsonProperty("number")
    private Integer number;
    @JsonProperty("status")
    private String status;
    @JsonProperty("document_status")
    private String documentStatus;
    @JsonProperty("apply_error")
    private String applyError;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("terms")
    private Map<String, Object> terms;
    @JsonProperty("changes")
    private Map<String, Object> changes;
    @JsonProperty("applied_at")
    private String appliedAt;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;

    /** Creates an empty amendment. */
    public DocumentContractAmendment() {}

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
     * Sets amendment document ID.
     *
     * @param documentId amendment document ID
     */
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    /** {@return the amendment document name} */
    public String getDocumentName() { return documentName; }

    /**
     * Sets amendment document name.
     *
     * @param documentName amendment document name
     */
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    /** {@return the root contract document ID} */
    public String getParentDocumentId() { return parentDocumentId; }

    /**
     * Sets root contract document ID.
     *
     * @param parentDocumentId root contract document ID
     */
    public void setParentDocumentId(String parentDocumentId) { this.parentDocumentId = parentDocumentId; }

    /** {@return {@code amendment}, {@code renewal}, or {@code termination}} */
    public String getRelationType() { return relationType; }

    /**
     * Sets contract relation type.
     *
     * @param relationType contract relation type
     */
    public void setRelationType(String relationType) { this.relationType = relationType; }

    /** {@return the applied amendment number, or {@code null} while pending} */
    public Integer getNumber() { return number; }

    /**
     * Sets applied amendment number, or {@code null}.
     *
     * @param number applied amendment number, or {@code null}
     */
    public void setNumber(Integer number) { this.number = number; }

    /** {@return {@code pending} or {@code applied}} */
    public String getStatus() { return status; }

    /**
     * Sets amendment status.
     *
     * @param status amendment status
     */
    public void setStatus(String status) { this.status = status; }

    /** {@return the amendment document's own lifecycle status} */
    public String getDocumentStatus() { return documentStatus; }

    /**
     * Sets amendment document lifecycle status.
     *
     * @param documentStatus amendment document lifecycle status
     */
    public void setDocumentStatus(String documentStatus) { this.documentStatus = documentStatus; }

    /** {@return why the delta has not applied, or {@code null}} */
    public String getApplyError() { return applyError; }

    /**
     * Sets application error, or {@code null}.
     *
     * @param applyError application error, or {@code null}
     */
    public void setApplyError(String applyError) { this.applyError = applyError; }

    /** {@return the currency used by monetary terms, or {@code null}} */
    public String getCurrency() { return currency; }

    /**
     * Sets currency used by monetary terms, or {@code null}.
     *
     * @param currency currency used by monetary terms, or {@code null}
     */
    public void setCurrency(String currency) { this.currency = currency; }

    /** {@return the terms changed by this instrument, or {@code null}} */
    public Map<String, Object> getTerms() { return terms; }

    /**
     * Sets terms changed by this instrument, or {@code null}.
     *
     * @param terms terms changed by this instrument, or {@code null}
     */
    public void setTerms(Map<String, Object> terms) { this.terms = terms; }

    /** {@return parent fields moved as {@code from}/{@code to} pairs, or {@code null}} */
    public Map<String, Object> getChanges() { return changes; }

    /**
     * Sets parent fields moved as {@code from}/{@code to} pairs, or {@code null}.
     *
     * @param changes parent fields moved as {@code from}/{@code to} pairs, or {@code null}
     */
    public void setChanges(Map<String, Object> changes) { this.changes = changes; }

    /** {@return the application timestamp, or {@code null} while pending} */
    public String getAppliedAt() { return appliedAt; }

    /**
     * Sets application timestamp, or {@code null}.
     *
     * @param appliedAt application timestamp, or {@code null}
     */
    public void setAppliedAt(String appliedAt) { this.appliedAt = appliedAt; }

    /** {@return the link creation timestamp} */
    public String getCreatedAt() { return createdAt; }

    /**
     * Sets link creation timestamp.
     *
     * @param createdAt link creation timestamp
     */
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    /** {@return the optimistic-concurrency timestamp for this link} */
    public String getUpdatedAt() { return updatedAt; }

    /**
     * Sets optimistic-concurrency timestamp.
     *
     * @param updatedAt optimistic-concurrency timestamp
     */
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
