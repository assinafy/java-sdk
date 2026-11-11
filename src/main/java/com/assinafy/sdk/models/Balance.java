package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/** A document or credit balance for the current billing cycle. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Balance {

    /** Creates an empty response. */
    public Balance() {}

    @JsonProperty("balance")
    private BigDecimal balance;

    /** Remaining quantity; null for unlimited.
     * @return the balance value, or null when absent
     */
    public BigDecimal getBalance() { return balance; }

    /** Sets balance.
     * @param value the balance value
     */
    public void setBalance(BigDecimal value) { balance = value; }

    @JsonProperty("limit")
    private BigDecimal limit;

    /** Allowance per cycle/pool; null when not cycle-limited or unlimited.
     * @return the limit value, or null when absent
     */
    public BigDecimal getLimit() { return limit; }

    /** Sets limit.
     * @param value the limit value
     */
    public void setLimit(BigDecimal value) { limit = value; }

    @JsonProperty("spent")
    private BigDecimal spent;

    /** Quantity consumed in the current cycle.
     * @return the spent value, or null when absent
     */
    public BigDecimal getSpent() { return spent; }

    /** Sets spent.
     * @param value the spent value
     */
    public void setSpent(BigDecimal value) { spent = value; }

}
