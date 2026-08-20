package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the document status info in an Assinafy API response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentStatusInfo {

    @JsonProperty("code")
    private String code;

    @JsonProperty("deletable")
    private Boolean deletable;

    /**
     * Creates an empty document status info.
     */
    public DocumentStatusInfo() {}

    /**
     * Returns the code.
     *
     * @return the code
     */
    public String getCode() { return code; }

    /**
     * Sets the code.
     *
     * @param code the code
     */
    public void setCode(String code) { this.code = code; }

    /**
     * Returns the deletion eligibility.
     *
     * @return the deletion eligibility
     */
    public Boolean getDeletable() { return deletable; }

    /**
     * Sets the deletion eligibility.
     *
     * @param deletable the deletion eligibility
     */
    public void setDeletable(Boolean deletable) { this.deletable = deletable; }
}
