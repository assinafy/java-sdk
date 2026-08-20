package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/** Cost breakdown for an assignment or template document, with current account balances. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CostEstimate {

    @JsonProperty("documents")
    private Integer documents;

    @JsonProperty("credits")
    private BigDecimal credits;

    @JsonProperty("needs_extra_document")
    private Boolean needsExtraDocument;

    @JsonProperty("extra_document_cost")
    private BigDecimal extraDocumentCost;

    @JsonProperty("total_credits")
    private BigDecimal totalCredits;

    @JsonProperty("breakdown")
    private List<CostEstimateBreakdownItem> breakdown;

    @JsonProperty("document_balance")
    private BigDecimal documentBalance;

    @JsonProperty("credit_balance")
    private BigDecimal creditBalance;

    @JsonProperty("has_sufficient_resources")
    private Boolean hasSufficientResources;

    @JsonProperty("blocking_reason")
    private String blockingReason;

    @JsonProperty("message")
    private String message;

    /**
     * Creates an empty cost estimate.
     */
    public CostEstimate() {}

    /**
     * Returns the documents.
     *
     * @return the documents
     */
    public Integer getDocuments() { return documents; }

    /**
     * Sets the documents.
     *
     * @param documents the documents
     */
    public void setDocuments(Integer documents) { this.documents = documents; }

    /**
     * Returns the credits.
     *
     * @return the credits
     */
    public BigDecimal getCredits() { return credits; }

    /**
     * Sets the credits.
     *
     * @param credits the credits
     */
    public void setCredits(BigDecimal credits) { this.credits = credits; }

    /**
     * Returns the extra-document requirement.
     *
     * @return the extra-document requirement
     */
    public Boolean getNeedsExtraDocument() { return needsExtraDocument; }

    /**
     * Sets the extra-document requirement.
     *
     * @param needsExtraDocument the extra-document requirement
     */
    public void setNeedsExtraDocument(Boolean needsExtraDocument) { this.needsExtraDocument = needsExtraDocument; }

    /**
     * Returns the extra-document cost.
     *
     * @return the extra-document cost
     */
    public BigDecimal getExtraDocumentCost() { return extraDocumentCost; }

    /**
     * Sets the extra-document cost.
     *
     * @param extraDocumentCost the extra-document cost
     */
    public void setExtraDocumentCost(BigDecimal extraDocumentCost) { this.extraDocumentCost = extraDocumentCost; }

    /**
     * Returns the total credit cost.
     *
     * @return the total credit cost
     */
    public BigDecimal getTotalCredits() { return totalCredits; }

    /**
     * Sets the total credit cost.
     *
     * @param totalCredits the total credit cost
     */
    public void setTotalCredits(BigDecimal totalCredits) { this.totalCredits = totalCredits; }

    /**
     * Returns the breakdown.
     *
     * @return the breakdown
     */
    public List<CostEstimateBreakdownItem> getBreakdown() { return breakdown; }

    /**
     * Sets the breakdown.
     *
     * @param breakdown the breakdown
     */
    public void setBreakdown(List<CostEstimateBreakdownItem> breakdown) { this.breakdown = breakdown; }

    /**
     * Returns the document balance.
     *
     * @return the document balance
     */
    public BigDecimal getDocumentBalance() { return documentBalance; }

    /**
     * Sets the document balance.
     *
     * @param documentBalance the document balance
     */
    public void setDocumentBalance(BigDecimal documentBalance) { this.documentBalance = documentBalance; }

    /**
     * Returns the credit balance.
     *
     * @return the credit balance
     */
    public BigDecimal getCreditBalance() { return creditBalance; }

    /**
     * Sets the credit balance.
     *
     * @param creditBalance the credit balance
     */
    public void setCreditBalance(BigDecimal creditBalance) { this.creditBalance = creditBalance; }

    /**
     * Returns the resource-sufficiency flag.
     *
     * @return the resource-sufficiency flag
     */
    public Boolean getHasSufficientResources() { return hasSufficientResources; }

    /**
     * Sets the resource-sufficiency flag.
     *
     * @param hasSufficientResources the resource-sufficiency flag
     */
    public void setHasSufficientResources(Boolean hasSufficientResources) { this.hasSufficientResources = hasSufficientResources; }

    /**
     * Returns the blocking reason.
     *
     * @return the blocking reason
     */
    public String getBlockingReason() { return blockingReason; }

    /**
     * Sets the blocking reason.
     *
     * @param blockingReason the blocking reason
     */
    public void setBlockingReason(String blockingReason) { this.blockingReason = blockingReason; }

    /**
     * Returns the message.
     *
     * @return the message
     */
    public String getMessage() { return message; }

    /**
     * Sets the message.
     *
     * @param message the message
     */
    public void setMessage(String message) { this.message = message; }
}
