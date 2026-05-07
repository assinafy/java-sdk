package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssignmentSummary {

    @JsonProperty("signer_count")
    private Integer signerCount;

    @JsonProperty("completed_count")
    private Integer completedCount;

    @JsonProperty("signers")
    private List<Object> signers;

    public AssignmentSummary() {}

    public Integer getSignerCount() { return signerCount; }
    public void setSignerCount(Integer signerCount) { this.signerCount = signerCount; }

    public Integer getCompletedCount() { return completedCount; }
    public void setCompletedCount(Integer completedCount) { this.completedCount = completedCount; }

    public List<Object> getSigners() { return signers; }
    public void setSigners(List<Object> signers) { this.signers = signers; }
}
