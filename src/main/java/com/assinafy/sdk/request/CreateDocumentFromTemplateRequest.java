package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateDocumentFromTemplateRequest {

    @JsonProperty("signers")
    private List<TemplateSigner> signers;

    @JsonProperty("name")
    private String name;

    @JsonProperty("message")
    private String message;

    @JsonProperty("expires_at")
    private String expiresAt;

    @JsonProperty("editor_fields")
    private List<Object> editorFields;

    public CreateDocumentFromTemplateRequest() {}

    public static Builder builder() {
        return new Builder();
    }

    public List<TemplateSigner> getSigners() { return signers; }
    public void setSigners(List<TemplateSigner> signers) { this.signers = signers; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

    public List<Object> getEditorFields() { return editorFields; }
    public void setEditorFields(List<Object> editorFields) { this.editorFields = editorFields; }

    public static final class Builder {
        private final CreateDocumentFromTemplateRequest req = new CreateDocumentFromTemplateRequest();

        public Builder signers(List<TemplateSigner> signers) { req.setSigners(signers); return this; }
        public Builder name(String name) { req.setName(name); return this; }
        public Builder message(String message) { req.setMessage(message); return this; }
        public Builder expiresAt(String expiresAt) { req.setExpiresAt(expiresAt); return this; }
        public Builder editorFields(List<Object> fields) { req.setEditorFields(fields); return this; }
        public CreateDocumentFromTemplateRequest build() { return req; }
    }
}
