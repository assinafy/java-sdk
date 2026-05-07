package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignerReference {

    @JsonProperty("id")
    private String id;

    @JsonProperty("verification_method")
    private String verificationMethod;

    @JsonProperty("notification_methods")
    private List<String> notificationMethods;

    public SignerReference() {}

    public static SignerReference ofId(String id) {
        SignerReference ref = new SignerReference();
        ref.setId(id);
        return ref;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getVerificationMethod() { return verificationMethod; }
    public void setVerificationMethod(String verificationMethod) { this.verificationMethod = verificationMethod; }

    public List<String> getNotificationMethods() { return notificationMethods; }
    public void setNotificationMethods(List<String> notificationMethods) { this.notificationMethods = notificationMethods; }

    public static final class Builder {
        private final SignerReference ref = new SignerReference();

        public Builder id(String id) { ref.setId(id); return this; }
        public Builder verificationMethod(String method) { ref.setVerificationMethod(method); return this; }
        public Builder notificationMethods(List<String> methods) { ref.setNotificationMethods(methods); return this; }
        public SignerReference build() { return ref; }
    }
}
