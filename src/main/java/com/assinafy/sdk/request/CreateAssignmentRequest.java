package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

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

    public CreateAssignmentRequest() {}

    public static Builder builder() {
        return new Builder();
    }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public List<SignerReference> getSigners() { return signers; }
    public void setSigners(List<SignerReference> signers) { this.signers = signers; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

    public List<String> getCopyReceivers() { return copyReceivers; }
    public void setCopyReceivers(List<String> copyReceivers) { this.copyReceivers = copyReceivers; }

    public List<Object> getEntries() { return entries; }
    public void setEntries(List<Object> entries) { this.entries = entries; }

    public static final class Builder {
        private final CreateAssignmentRequest req = new CreateAssignmentRequest();

        public Builder method(String method) { req.setMethod(method); return this; }
        public Builder signers(List<SignerReference> signers) { req.setSigners(signers); return this; }
        public Builder message(String message) { req.setMessage(message); return this; }
        public Builder expiresAt(String expiresAt) { req.setExpiresAt(expiresAt); return this; }
        public Builder copyReceivers(List<String> copyReceivers) { req.setCopyReceivers(copyReceivers); return this; }
        public Builder entries(List<Object> entries) { req.setEntries(entries); return this; }
        public CreateAssignmentRequest build() { return req; }
    }
}
