package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/** Documents sent together to the same signers. Each document keeps its own assignment, status, certificate and downloads. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentEnvelope {

    /** Creates an empty response. */
    public DocumentEnvelope() {}

    @JsonProperty("resource")
    private String resource;

    /** Present in single-resource responses.
     * @return the resource value, or null when absent
     */
    public String getResource() { return resource; }

    /** Sets resource.
     * @param value the resource value
     */
    public void setResource(String value) { resource = value; }

    @JsonProperty("id")
    private String id;

    /** id
     * @return the id value, or null when absent
     */
    public String getId() { return id; }

    /** Sets id.
     * @param value the id value
     */
    public void setId(String value) { id = value; }

    @JsonProperty("name")
    private String name;

    /** name
     * @return the name value, or null when absent
     */
    public String getName() { return name; }

    /** Sets name.
     * @param value the name value
     */
    public void setName(String value) { name = value; }

    @JsonProperty("message")
    private String message;

    /** message
     * @return the message value, or null when absent
     */
    public String getMessage() { return message; }

    /** Sets message.
     * @param value the message value
     */
    public void setMessage(String value) { message = value; }

    @JsonProperty("documents")
    private List<Document> documents;

    /** The envelope&#x27;s documents, in the order it lists them.
     * @return the documents value, or null when absent
     */
    public List<Document> getDocuments() { return documents; }

    /** Sets documents.
     * @param value the documents value
     */
    public void setDocuments(List<Document> value) { documents = value; }

    @JsonProperty("counts")
    private Map<String, Integer> counts;

    /** How many of its documents are in each status (status code =&gt; count); only the statuses present. An envelope has no status of its own. `null` once it lists no document: every one deleted or taken out.
     * @return the counts value, or null when absent
     */
    public Map<String, Integer> getCounts() { return counts; }

    /** Sets counts.
     * @param value the counts value
     */
    public void setCounts(Map<String, Integer> value) { counts = value; }

    @JsonProperty("signing_urls")
    private List<SigningUrl> signingUrls;

    /** One entry per signer, as on an assignment (`SigningUrl`): the link of the document their invitation is sent from while it waits for them, else of their first document that still does (the e-mail page asks that document for the invitation again; any of their links opens the whole envelope).
     * @return the signing_urls value, or null when absent
     */
    public List<SigningUrl> getSigningUrls() { return signingUrls; }

    /** Sets signing_urls.
     * @param value the signing_urls value
     */
    public void setSigningUrls(List<SigningUrl> value) { signingUrls = value; }

    @JsonProperty("created_at")
    private String createdAt;

    /** created at
     * @return the created_at value, or null when absent
     */
    public String getCreatedAt() { return createdAt; }

    /** Sets created_at.
     * @param value the created_at value
     */
    public void setCreatedAt(String value) { createdAt = value; }

}
