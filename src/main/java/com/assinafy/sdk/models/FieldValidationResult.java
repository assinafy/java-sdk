package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldValidationResult {

    @JsonProperty("type")
    private String type;

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("field_id")
    private String fieldId;

    public FieldValidationResult() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getFieldId() { return fieldId; }
    public void setFieldId(String fieldId) { this.fieldId = fieldId; }
}
