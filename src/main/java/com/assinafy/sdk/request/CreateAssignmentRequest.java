package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request payload for creating a document assignment.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateAssignmentRequest {

    @JsonProperty("method")
    private String method;

    @JsonProperty("signers")
    private List<SignerReference> signers;

    @JsonProperty("message")
    private String message;

    @JsonProperty("expires_at")
    private String expiresAt;

    @JsonProperty("copy_receivers")
    private List<String> copyReceivers;

    @JsonProperty("entries")
    private List<Object> entries;

    /**
     * Creates an empty assignment-creation request.
     */
    public CreateAssignmentRequest() {}

    /**
     * Creates a builder for a assignment-creation request.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the method.
     *
     * @return the method
     */
    public String getMethod() { return method; }

    /**
     * Sets the method.
     *
     * @param method the method
     */
    public void setMethod(String method) { this.method = method; }

    /**
     * Returns the signers.
     *
     * @return the signers
     */
    public List<SignerReference> getSigners() { return signers; }

    /**
     * Sets the signers.
     *
     * @param signers the signers
     */
    public void setSigners(List<SignerReference> signers) { this.signers = signers; }

    /**
     * Returns the message.
     *
     * @return the message
     */
    public String getMessage() { return message; }

    /**
     * Sets the message.
     *
     * @param message the message
     */
    public void setMessage(String message) { this.message = message; }

    /**
     * Returns the expiration timestamp.
     *
     * @return the expiration timestamp
     */
    public String getExpiresAt() { return expiresAt; }

    /**
     * Sets the expiration timestamp.
     *
     * @param expiresAt the expiration timestamp
     */
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

    /**
     * Returns the copy recipients.
     *
     * @return the copy recipients
     */
    public List<String> getCopyReceivers() { return copyReceivers; }

    /**
     * Sets the copy recipients.
     *
     * @param copyReceivers the copy recipients
     */
    public void setCopyReceivers(List<String> copyReceivers) { this.copyReceivers = copyReceivers; }

    /**
     * Returns the entries.
     *
     * @return the entries
     */
    public List<Object> getEntries() { return entries; }

    /**
     * Sets the entries.
     *
     * @param entries the entries
     */
    public void setEntries(List<Object> entries) { this.entries = entries; }

    /**
     * Builder for {@link CreateAssignmentRequest}.
     */
    public static final class Builder {
        private final CreateAssignmentRequest req = new CreateAssignmentRequest();

        /** Creates an empty builder. */
        public Builder() {}

        /**
         * Sets the method for the object being built.
         *
         * @param method the method
         * @return this builder
         */
        public Builder method(String method) { req.setMethod(method); return this; }

        /**
         * Sets the signers for the object being built.
         *
         * @param signers the signers
         * @return this builder
         */
        public Builder signers(List<SignerReference> signers) { req.setSigners(signers); return this; }

        /**
         * Sets the message for the object being built.
         *
         * @param message the message
         * @return this builder
         */
        public Builder message(String message) { req.setMessage(message); return this; }

        /**
         * Sets the expiration timestamp for the object being built.
         *
         * @param expiresAt the expiration timestamp
         * @return this builder
         */
        public Builder expiresAt(String expiresAt) { req.setExpiresAt(expiresAt); return this; }

        /**
         * Sets the copy recipients for the object being built.
         *
         * @param copyReceivers the copy recipients
         * @return this builder
         */
        public Builder copyReceivers(List<String> copyReceivers) { req.setCopyReceivers(copyReceivers); return this; }

        /**
         * Sets the entries for the object being built.
         *
         * @param entries the entries
         * @return this builder
         */
        public Builder entries(List<Object> entries) { req.setEntries(entries); return this; }

        /**
         * Builds the configured assignment-creation request.
         *
         * @return the configured assignment-creation request
         */
        public CreateAssignmentRequest build() { return req; }
    }
}
