package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request payload for creating a document from a template.
 */
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

    /** Tag names to attach to the new document. Names that don't exist yet are auto-created. */
    @JsonProperty("tags")
    private List<String> tags;

    /**
     * Creates an empty document-from-template request.
     */
    public CreateDocumentFromTemplateRequest() {}

    /**
     * Creates a builder for a document-from-template request.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the signers.
     *
     * @return the signers
     */
    public List<TemplateSigner> getSigners() { return signers; }

    /**
     * Sets the signers.
     *
     * @param signers the signers
     */
    public void setSigners(List<TemplateSigner> signers) { this.signers = signers; }

    /**
     * Returns the name.
     *
     * @return the name
     */
    public String getName() { return name; }

    /**
     * Sets the name.
     *
     * @param name the name
     */
    public void setName(String name) { this.name = name; }

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
     * @param expiresAt ISO-8601 expiration timestamp; whole-second UTC form is recommended
     */
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

    /**
     * Returns the editor fields.
     *
     * @return the editor fields
     */
    public List<Object> getEditorFields() { return editorFields; }

    /**
     * Sets the editor fields.
     *
     * @param editorFields the editor fields
     */
    public void setEditorFields(List<Object> editorFields) { this.editorFields = editorFields; }

    /**
     * Returns the tags.
     *
     * @return the tags
     */
    public List<String> getTags() { return tags; }

    /**
     * Sets the tags.
     *
     * @param tags the tags
     */
    public void setTags(List<String> tags) { this.tags = tags; }

    /**
     * Builder for {@link CreateDocumentFromTemplateRequest}.
     */
    public static final class Builder {
        private final CreateDocumentFromTemplateRequest req = new CreateDocumentFromTemplateRequest();

        /** Creates an empty builder. */
        public Builder() {}

        /**
         * Sets the signers for the object being built.
         *
         * @param signers the signers
         * @return this builder
         */
        public Builder signers(List<TemplateSigner> signers) { req.setSigners(signers); return this; }

        /**
         * Sets the name for the object being built.
         *
         * @param name the name
         * @return this builder
         */
        public Builder name(String name) { req.setName(name); return this; }

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
         * @param expiresAt ISO-8601 expiration timestamp; whole-second UTC form is recommended
         * @return this builder
         */
        public Builder expiresAt(String expiresAt) { req.setExpiresAt(expiresAt); return this; }

        /**
         * Sets the editor fields for the object being built.
         *
         * @param fields the fields
         * @return this builder
         */
        public Builder editorFields(List<Object> fields) { req.setEditorFields(fields); return this; }

        /**
         * Sets the tags for the object being built.
         *
         * @param tags the tags
         * @return this builder
         */
        public Builder tags(List<String> tags) { req.setTags(tags); return this; }

        /**
         * Builds the configured document-from-template request.
         *
         * @return the configured document-from-template request
         */
        public CreateDocumentFromTemplateRequest build() { return req; }
    }
}
