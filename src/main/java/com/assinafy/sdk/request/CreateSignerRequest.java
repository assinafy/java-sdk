package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Request payload for creating a signer ({@code POST /v1/accounts/{accountId}/signers}).
 *
 * <pre>{
 *   "full_name": "John Dove",
 *   "email": "signer@example.invalid",
 *   "whatsapp_phone_number": "+5548999990000",
 *   "government_id": "390.533.447-05"
 * }</pre>
 *
 * <p>{@code full_name} is required; every other field is optional and omitted when unset.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateSignerRequest {

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("whatsapp_phone_number")
    private String whatsappPhoneNumber;

    /**
     * Brazilian CPF (11 digits) or CNPJ (14 characters, possibly alphanumeric). Formatting is
     * accepted; the API normalizes the value on save.
     */
    @JsonProperty("government_id")
    @JsonAlias("cpf")
    private String governmentId;

    /**
     * Creates an empty signer-creation request.
     */
    public CreateSignerRequest() {}

    /**
     * Creates a builder for a signer-creation request.
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
     * Returns the CPF or CNPJ sent as {@code government_id}.
     *
     * @return the CPF or CNPJ
     */
    public String getGovernmentId() { return governmentId; }

    /**
     * Sets the CPF or CNPJ sent as {@code government_id}.
     *
     * @param governmentId the CPF or CNPJ
     */
    public void setGovernmentId(String governmentId) { this.governmentId = governmentId; }

    /**
     * Returns the CPF or CNPJ.
     *
     * @return the CPF or CNPJ
     * @deprecated Use {@link #getGovernmentId()}; the API wire field is {@code government_id}.
     */
    @Deprecated
    @JsonIgnore
    public String getCpf() { return governmentId; }

    /**
     * Sets the CPF or CNPJ.
     *
     * @param cpf the CPF or CNPJ
     * @deprecated Use {@link #setGovernmentId(String)}.
     */
    @Deprecated
    @JsonIgnore
    public void setCpf(String cpf) { this.governmentId = cpf; }

    /**
     * Builder for {@link CreateSignerRequest}.
     */
    public static final class Builder {
        private final CreateSignerRequest req = new CreateSignerRequest();

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
         * Sets the phone for the object being built.
         *
         * @param phone the phone
         * @return this builder
         */
        public Builder phone(String phone) { req.setWhatsappPhoneNumber(phone); return this; }

        /**
         * Sets the CPF or CNPJ sent as {@code government_id}.
         *
         * @param governmentId the CPF or CNPJ
         * @return this builder
         */
        public Builder governmentId(String governmentId) { req.setGovernmentId(governmentId); return this; }

        /**
         * Sets the CPF or CNPJ.
         *
         * @param cpf the CPF or CNPJ
         * @return this builder
         * @deprecated Use {@link #governmentId(String)}.
         */
        @Deprecated
        public Builder cpf(String cpf) { req.setGovernmentId(cpf); return this; }

        /**
         * Builds the configured signer-creation request.
         *
         * @return the configured signer-creation request
         */
        public CreateSignerRequest build() { return req; }
    }
}
