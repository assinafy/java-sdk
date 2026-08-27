package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload for updating a signer.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateSignerRequest {

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("whatsapp_phone_number")
    private String whatsappPhoneNumber;

    @JsonProperty("government_id")
    @JsonAlias("cpf")
    private String governmentId;

    /**
     * Creates an empty signer-update request.
     */
    public UpdateSignerRequest() {}

    /**
     * Creates a builder for a signer-update request.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the full name.
     *
     * @return the full name
     */
    public String getFullName() { return fullName; }

    /**
     * Sets the full name.
     *
     * @param fullName the full name
     */
    public void setFullName(String fullName) { this.fullName = fullName; }

    /**
     * Returns the email address.
     *
     * @return the email address
     */
    public String getEmail() { return email; }

    /**
     * Sets the email address.
     *
     * @param email the email address
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Returns the WhatsApp phone number.
     *
     * @return the WhatsApp phone number
     */
    public String getWhatsappPhoneNumber() { return whatsappPhoneNumber; }

    /**
     * Sets the WhatsApp phone number.
     *
     * @param whatsappPhoneNumber the WhatsApp phone number
     */
    public void setWhatsappPhoneNumber(String whatsappPhoneNumber) { this.whatsappPhoneNumber = whatsappPhoneNumber; }

    /**
     * Returns the government ID.
     *
     * @return the government ID
     */
    public String getGovernmentId() { return governmentId; }

    /**
     * Sets the government ID.
     *
     * @param governmentId the government ID
     */
    public void setGovernmentId(String governmentId) { this.governmentId = governmentId; }

    /**
     * @deprecated Use {@link #getGovernmentId()}; the API wire field is {@code government_id}.
     *
     * @return the CPF or CNPJ
     */
    @Deprecated
    @JsonIgnore
    public String getCpf() { return governmentId; }

    /**
     * @deprecated Use {@link #setGovernmentId(String)}.
     *
     * @param cpf the CPF or CNPJ
     */
    @Deprecated
    @JsonIgnore
    public void setCpf(String cpf) { this.governmentId = cpf; }

    /**
     * Builder for {@link UpdateSignerRequest}.
     */
    public static final class Builder {
        private final UpdateSignerRequest req = new UpdateSignerRequest();

        /** Creates an empty builder. */
        public Builder() {}

        /**
         * Sets the full name for the object being built.
         *
         * @param fullName the full name
         * @return this builder
         */
        public Builder fullName(String fullName) { req.setFullName(fullName); return this; }

        /**
         * Sets the email address for the object being built.
         *
         * @param email the email address
         * @return this builder
         */
        public Builder email(String email) { req.setEmail(email); return this; }

        /**
         * Sets the WhatsApp phone number for the object being built.
         *
         * @param phone the WhatsApp phone number
         * @return this builder
         */
        public Builder whatsappPhoneNumber(String phone) { req.setWhatsappPhoneNumber(phone); return this; }

        /**
         * Sets the government ID for the object being built.
         *
         * @param governmentId the government ID
         * @return this builder
         */
        public Builder governmentId(String governmentId) { req.setGovernmentId(governmentId); return this; }

        /**
         * @deprecated Use {@link #governmentId(String)}.
         *
         * @param cpf the CPF or CNPJ
         * @return this builder
         */
        @Deprecated
        public Builder cpf(String cpf) { req.setCpf(cpf); return this; }

        /**
         * Builds the configured signer-update request.
         *
         * @return the configured signer-update request
         */
        public UpdateSignerRequest build() { return req; }
    }
}
