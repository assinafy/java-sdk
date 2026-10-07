package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The authenticated user's enrolled two-factor methods and how many recovery codes remain.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MfaStatus {

    @JsonProperty("methods")
    private List<MfaMethod> methods;

    @JsonProperty("recovery_codes_remaining")
    private Integer recoveryCodesRemaining;

    /**
     * Creates an empty instance.
     */
    public MfaStatus() {}

    /**
     * Returns the enrolled methods.
     *
     * @return the enrolled methods
     */
    public List<MfaMethod> getMethods() { return methods; }

    /**
     * Sets the enrolled methods.
     *
     * @param methods the enrolled methods
     */
    public void setMethods(List<MfaMethod> methods) { this.methods = methods; }

    /**
     * Returns the number of unused recovery codes.
     *
     * @return the number of unused recovery codes
     */
    public Integer getRecoveryCodesRemaining() { return recoveryCodesRemaining; }

    /**
     * Sets the number of unused recovery codes.
     *
     * @param recoveryCodesRemaining the number of unused recovery codes
     */
    public void setRecoveryCodesRemaining(Integer recoveryCodesRemaining) { this.recoveryCodesRemaining = recoveryCodesRemaining; }
}
