package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

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

    public CreateSignerRequest() {}

    public static Builder builder() {
        return new Builder();
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getWhatsappPhoneNumber() { return whatsappPhoneNumber; }
    public void setWhatsappPhoneNumber(String whatsappPhoneNumber) { this.whatsappPhoneNumber = whatsappPhoneNumber; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public static final class Builder {
        private final CreateSignerRequest req = new CreateSignerRequest();

        public Builder fullName(String fullName) { req.setFullName(fullName); return this; }
        public Builder email(String email) { req.setEmail(email); return this; }
        public Builder whatsappPhoneNumber(String phone) { req.setWhatsappPhoneNumber(phone); return this; }
        public Builder phone(String phone) { req.setWhatsappPhoneNumber(phone); return this; }
        public Builder cpf(String cpf) { req.setCpf(cpf); return this; }
        public Builder metadata(Map<String, Object> metadata) { req.setMetadata(metadata); return this; }
        public CreateSignerRequest build() { return req; }
    }
}
