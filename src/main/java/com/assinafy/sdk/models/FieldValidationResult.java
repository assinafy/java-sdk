package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the field validation result in an Assinafy API response.
 */
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

    /**
     * Creates an empty field validation result.
     */
    public FieldValidationResult() {}

    /**
     * Returns the type.
     *
     * @return the type
     */
    public String getType() { return type; }

    /**
     * Sets the type.
     *
     * @param type the type
     */
    public void setType(String type) { this.type = type; }

    /**
     * Returns the success flag.
     *
     * @return the success flag
     */
    public Boolean getSuccess() { return success; }

    /**
     * Sets the success flag.
     *
     * @param success the success flag
     */
    public void setSuccess(Boolean success) { this.success = success; }

    /**
     * Returns the error message.
     *
     * @return the error message
     */
    public String getErrorMessage() { return errorMessage; }

    /**
     * Sets the error message.
     *
     * @param errorMessage the error message
     */
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    /**
     * Returns the field ID.
     *
     * @return the field ID
     */
    public String getFieldId() { return fieldId; }

    /**
     * Sets the field ID.
     *
     * @param fieldId the field ID
     */
    public void setFieldId(String fieldId) { this.fieldId = fieldId; }
}
