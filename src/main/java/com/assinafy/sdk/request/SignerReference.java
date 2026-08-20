package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request payload for signer reference operations.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignerReference {

    @JsonProperty("id")
    private String id;

    @JsonProperty("verification_method")
    private String verificationMethod;

    @JsonProperty("notification_methods")
    private List<String> notificationMethods;

    /**
     * Positive integer controlling signing order. Signers sharing the same step sign in
     * parallel; a step activates only after every signer in the previous step has signed.
     * When supplied for one signer it must be supplied for all.
     */
    @JsonProperty("step")
    private Integer step;

    /**
     * Creates an empty signer reference.
     */
    public SignerReference() {}

    /**
     * Creates a signer reference for an existing signer ID.
     *
     * @param id existing signer ID
     * @return a signer reference for the supplied ID
     */
    public static SignerReference ofId(String id) {
        SignerReference ref = new SignerReference();
        ref.setId(id);
        return ref;
    }

    /**
     * Creates a builder for a signer reference.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

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
     * Builder for {@link SignerReference}.
     */
    public static final class Builder {
        private final SignerReference ref = new SignerReference();

        /** Creates an empty builder. */
        public Builder() {}

        /**
         * Sets the ID for the object being built.
         *
         * @param id the ID
         * @return this builder
         */
        public Builder id(String id) { ref.setId(id); return this; }

        /**
         * Sets the verification method for the object being built.
         *
         * @param method the verification method
         * @return this builder
         */
        public Builder verificationMethod(String method) { ref.setVerificationMethod(method); return this; }

        /**
         * Sets the notification methods for the object being built.
         *
         * @param methods the notification methods
         * @return this builder
         */
        public Builder notificationMethods(List<String> methods) { ref.setNotificationMethods(methods); return this; }

        /**
         * Sets the step for the object being built.
         *
         * @param step the step
         * @return this builder
         */
        public Builder step(Integer step) { ref.setStep(step); return this; }

        /**
         * Builds the configured signer reference.
         *
         * @return the configured signer reference
         */
        public SignerReference build() { return ref; }
    }
}
