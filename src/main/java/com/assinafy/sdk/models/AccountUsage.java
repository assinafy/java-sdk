package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/** The workspace&#x27;s plan and what is left of it. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountUsage {

    /** Creates an empty response. */
    public AccountUsage() {}

    @JsonProperty("plan")
    private String plan;

    /** The plan&#x27;s name. `null` when the subscription has no plan or its plan was retired.
     * @return the plan value, or null when absent
     */
    public String getPlan() { return plan; }

    /** Sets plan.
     * @param value the plan value
     */
    public void setPlan(String value) { plan = value; }

    @JsonProperty("documents_renewal_date")
    private String documentsRenewalDate;

    /** When the document allowance next renews. On a custom contract, the next boundary of the contract&#x27;s billing day. `null` while the subscription awaits its first payment, or on a contract with no billing day.
     * @return the documents_renewal_date value, or null when absent
     */
    public String getDocumentsRenewalDate() { return documentsRenewalDate; }

    /** Sets documents_renewal_date.
     * @param value the documents_renewal_date value
     */
    public void setDocumentsRenewalDate(String value) { documentsRenewalDate = value; }

    @JsonProperty("balances")
    private Map<String, Balance> balances;

    /** Document and credit balances, as in **Get current subscription**.
     * @return the balances value, or null when absent
     */
    public Map<String, Balance> getBalances() { return balances; }

    /** Sets balances.
     * @param value the balances value
     */
    public void setBalances(Map<String, Balance> value) { balances = value; }

}
