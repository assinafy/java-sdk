package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentStatusInfo {

    @JsonProperty("code")
    private String code;

    @JsonProperty("deletable")
    private Boolean deletable;

    public DocumentStatusInfo() {}

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Boolean getDeletable() { return deletable; }
    public void setDeletable(Boolean deletable) { this.deletable = deletable; }
}