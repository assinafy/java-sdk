package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/** Credit cost of resending one signer's notification. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResendCostEstimate {

    @JsonProperty("total")
    private BigDecimal total;
    @JsonProperty("breakdown")
    private List<CostEstimateBreakdownItem> breakdown;
    @JsonProperty("credit_balance")
    private BigDecimal creditBalance;
    @JsonProperty("has_sufficient_credits")
    private Boolean hasSufficientCredits;

    /** Creates an empty resend cost estimate. */
    public ResendCostEstimate() {}

    /** {@return the total resend cost in credits} */
    public BigDecimal getTotal() { return total; }

    /**
     * Sets total resend cost in credits.
     *
     * @param total total resend cost in credits
     */
    public void setTotal(BigDecimal total) { this.total = total; }

    /** {@return the charges contributing to the total} */
    public List<CostEstimateBreakdownItem> getBreakdown() { return breakdown; }

    /**
     * Sets charges contributing to the total.
     *
     * @param breakdown charges contributing to the total
     */
    public void setBreakdown(List<CostEstimateBreakdownItem> breakdown) { this.breakdown = breakdown; }

    /** {@return available credits, or {@code null} for an unlimited balance} */
    public BigDecimal getCreditBalance() { return creditBalance; }

    /**
     * Sets available credits, or {@code null}.
     *
     * @param creditBalance available credits, or {@code null}
     */
    public void setCreditBalance(BigDecimal creditBalance) { this.creditBalance = creditBalance; }

    /** {@return whether the workspace can pay the resend cost} */
    public Boolean getHasSufficientCredits() { return hasSufficientCredits; }

    /**
     * Sets whether the workspace can pay the resend cost.
     *
     * @param hasSufficientCredits whether the workspace can pay the resend cost
     */
    public void setHasSufficientCredits(Boolean hasSufficientCredits) {
        this.hasSufficientCredits = hasSufficientCredits;
    }
}
