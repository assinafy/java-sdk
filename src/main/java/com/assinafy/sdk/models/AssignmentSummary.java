package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents the assignment summary in an Assinafy API response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssignmentSummary {

    @JsonProperty("signer_count")
    private Integer signerCount;

    @JsonProperty("completed_count")
    private Integer completedCount;

    @JsonProperty("signers")
    private List<Object> signers;

    /**
     * Creates an empty assignment summary.
     */
    public AssignmentSummary() {}

    /**
     * Returns the signer count.
     *
     * @return the signer count
     */
    public Integer getSignerCount() { return signerCount; }

    /**
     * Sets the signer count.
     *
     * @param signerCount the signer count
     */
    public void setSignerCount(Integer signerCount) { this.signerCount = signerCount; }

    /**
     * Returns the completed signer count.
     *
     * @return the completed signer count
     */
    public Integer getCompletedCount() { return completedCount; }

    /**
     * Sets the completed signer count.
     *
     * @param completedCount the completed signer count
     */
    public void setCompletedCount(Integer completedCount) { this.completedCount = completedCount; }

    /**
     * Returns the signers.
     *
     * @return the signers
     */
    public List<Object> getSigners() { return signers; }

    /**
     * Sets the signers.
     *
     * @param signers the signers
     */
    public void setSigners(List<Object> signers) { this.signers = signers; }
}
