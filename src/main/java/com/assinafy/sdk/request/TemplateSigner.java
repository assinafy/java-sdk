package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request payload for template signer operations.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemplateSigner {

    @JsonProperty("role_id")
    private String roleId;

    @JsonProperty("id")
    private String id;

    @JsonProperty("verification_method")
    private String verificationMethod;

    @JsonProperty("notification_methods")
    private List<String> notificationMethods;

    /** Optional positive integer controlling signing order (see {@link SignerReference#getStep()}). */
    @JsonProperty("step")
    private Integer step;

    /**
     * Creates an empty template signer.
     */
    public TemplateSigner() {}

    /**
     * Creates a builder for a template signer.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the role ID.
     *
     * @return the role ID
     */
    public String getRoleId() { return roleId; }

    /**
     * Sets the role ID.
     *
     * @param roleId the role ID
     */
    public void setRoleId(String roleId) { this.roleId = roleId; }

    /**
     * Returns the ID.
     *
     * @return the ID
     */
    public String getId() { return id; }

    /**
     * Sets the ID.
     *
     * @param id the ID
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns the verification method.
     *
     * @return the verification method
     */
    public String getVerificationMethod() { return verificationMethod; }

    /**
     * Sets the verification method.
     *
     * @param verificationMethod the verification method
     */
    public void setVerificationMethod(String verificationMethod) { this.verificationMethod = verificationMethod; }

    /**
     * Returns the notification methods.
     *
     * @return the notification methods
     */
    public List<String> getNotificationMethods() { return notificationMethods; }

    /**
     * Sets the notification methods.
     *
     * @param notificationMethods the notification methods
     */
    public void setNotificationMethods(List<String> notificationMethods) { this.notificationMethods = notificationMethods; }

    /**
     * Returns the step.
     *
     * @return the step
     */
    public Integer getStep() { return step; }

    /**
     * Sets the step.
     *
     * @param step the step
     */
    public void setStep(Integer step) { this.step = step; }

    /**
     * Builder for {@link TemplateSigner}.
     */
    public static final class Builder {
        private final TemplateSigner ts = new TemplateSigner();

        /** Creates an empty builder. */
        public Builder() {}

        /**
         * Sets the role ID for the object being built.
         *
         * @param roleId the role ID
         * @return this builder
         */
        public Builder roleId(String roleId) { ts.setRoleId(roleId); return this; }

        /**
         * Sets the ID for the object being built.
         *
         * @param id the ID
         * @return this builder
         */
        public Builder id(String id) { ts.setId(id); return this; }

        /**
         * Sets the verification method for the object being built.
         *
         * @param method the verification method
         * @return this builder
         */
        public Builder verificationMethod(String method) { ts.setVerificationMethod(method); return this; }

        /**
         * Sets the notification methods for the object being built.
         *
         * @param methods the notification methods
         * @return this builder
         */
        public Builder notificationMethods(List<String> methods) { ts.setNotificationMethods(methods); return this; }

        /**
         * Sets the step for the object being built.
         *
         * @param step the step
         * @return this builder
         */
        public Builder step(Integer step) { ts.setStep(step); return this; }

        /**
         * Builds the configured template signer.
         *
         * @return the configured template signer
         */
        public TemplateSigner build() { return ts; }
    }
}
