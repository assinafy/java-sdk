package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

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

    public TemplateSigner() {}

    public static Builder builder() {
        return new Builder();
    }

    public String getRoleId() { return roleId; }
    public void setRoleId(String roleId) { this.roleId = roleId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getVerificationMethod() { return verificationMethod; }
    public void setVerificationMethod(String verificationMethod) { this.verificationMethod = verificationMethod; }

    public List<String> getNotificationMethods() { return notificationMethods; }
    public void setNotificationMethods(List<String> notificationMethods) { this.notificationMethods = notificationMethods; }

    public static final class Builder {
        private final TemplateSigner ts = new TemplateSigner();

        public Builder roleId(String roleId) { ts.setRoleId(roleId); return this; }
        public Builder id(String id) { ts.setId(id); return this; }
        public Builder verificationMethod(String method) { ts.setVerificationMethod(method); return this; }
        public Builder notificationMethods(List<String> methods) { ts.setNotificationMethods(methods); return this; }
        public TemplateSigner build() { return ts; }
    }
}
