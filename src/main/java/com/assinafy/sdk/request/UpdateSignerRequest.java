package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateSignerRequest {

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("whatsapp_phone_number")
    private String whatsappPhoneNumber;

    @JsonProperty("cpf")
    private String cpf;

    public UpdateSignerRequest() {}

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

    public static final class Builder {
        private final UpdateSignerRequest req = new UpdateSignerRequest();

        public Builder fullName(String fullName) { req.setFullName(fullName); return this; }
        public Builder email(String email) { req.setEmail(email); return this; }
        public Builder whatsappPhoneNumber(String phone) { req.setWhatsappPhoneNumber(phone); return this; }
        public Builder cpf(String cpf) { req.setCpf(cpf); return this; }
        public UpdateSignerRequest build() { return req; }
    }
}
