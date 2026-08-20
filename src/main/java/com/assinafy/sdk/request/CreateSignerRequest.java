package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Request payload for creating a signer.
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
     * Brazilian CPF (non-digits are stripped before sending). Sent for parity with the PHP/TS
     * SDKs; note the documented signer contract is {@code full_name}/{@code email}/
     * {@code whatsapp_phone_number} only, and the current API does not persist or return CPF.
     */
    @JsonProperty("cpf")
    private String cpf;

    /**
     * Arbitrary metadata. Sent as-is; note the current signer API does not persist or return
     * metadata (it is silently ignored), so do not rely on reading it back from a {@code Signer}.
     */
    @JsonProperty("metadata")
    private Map<String, Object> metadata;

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
     * Returns the CPF.
     *
     * @return the CPF
     */
    public String getCpf() { return cpf; }

    /**
     * Sets the CPF.
     *
     * @param cpf the CPF
     */
    public void setCpf(String cpf) { this.cpf = cpf; }

    /**
     * Returns the metadata.
     *
     * @return the metadata
     */
    public Map<String, Object> getMetadata() { return metadata; }

    /**
     * Sets the metadata.
     *
     * @param metadata the metadata
     */
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

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
         * Sets the CPF for the object being built.
         *
         * @param cpf the CPF
         * @return this builder
         */
        public Builder cpf(String cpf) { req.setCpf(cpf); return this; }

        /**
         * Sets the metadata for the object being built.
         *
         * @param metadata the metadata
         * @return this builder
         */
        public Builder metadata(Map<String, Object> metadata) { req.setMetadata(metadata); return this; }

        /**
         * Builds the configured signer-creation request.
         *
         * @return the configured signer-creation request
         */
        public CreateSignerRequest build() { return req; }
    }
}
