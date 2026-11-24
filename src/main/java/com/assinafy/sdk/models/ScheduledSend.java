package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Deferred assignment or envelope state. Available on staging; polling requires documents:write. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduledSend {
    /** Creates an empty response model. */
    public ScheduledSend() {}

    @JsonProperty("resource")
    private String resource;

    /** Resource.
     * @return the resource value, or null when absent
     */
    public String getResource() { return resource; }

    /** Sets resource.
     * @param value the resource value
     */
    public void setResource(String value) { resource = value; }

    @JsonProperty("id")
    private String id;

    /** Id.
     * @return the id value, or null when absent
     */
    public String getId() { return id; }

    /** Sets id.
     * @param value the id value
     */
    public void setId(String value) { id = value; }

    @JsonProperty("type")
    private String type;

    /** What it creates.
     * @return the type value, or null when absent
     */
    public String getType() { return type; }

    /** Sets type.
     * @param value the type value
     */
    public void setType(String value) { type = value; }

    @JsonProperty("document_ids")
    private List<String> documentIds;

    /** The documents it sends: one for an assignment, the envelope's for an envelope. A deleted document is left out.
     * @return the document_ids value, or null when absent
     */
    public List<String> getDocumentIds() { return documentIds; }

    /** Sets document_ids.
     * @param value the document_ids value
     */
    public void setDocumentIds(List<String> value) { documentIds = value; }

    @JsonProperty("status")
    private String status;

    /** pending until send_at (or sendNow); processing while its request is being made; then sent (the request was made; its invitations go out right after), failed or, if cancelled while pending, cancelled. A send whose run was interrupted while processing made nothing, and fails within about an hour.
     * @return the status value, or null when absent
     */
    public String getStatus() { return status; }

    /** Sets status.
     * @param value the status value
     */
    public void setStatus(String value) { status = value; }

    @JsonProperty("send_at")
    private String sendAt;

    /** Send at.
     * @return the send_at value, or null when absent
     */
    public String getSendAt() { return sendAt; }

    /** Sets send_at.
     * @param value the send_at value
     */
    public void setSendAt(String value) { sendAt = value; }

    @JsonProperty("assignment_id")
    private String assignmentId;

    /** The assignment it created, once an assignment is sent.
     * @return the assignment_id value, or null when absent
     */
    public String getAssignmentId() { return assignmentId; }

    /** Sets assignment_id.
     * @param value the assignment_id value
     */
    public void setAssignmentId(String value) { assignmentId = value; }

    @JsonProperty("envelope_id")
    private String envelopeId;

    /** The envelope it created, once an envelope is sent; null again once the envelope is deleted.
     * @return the envelope_id value, or null when absent
     */
    public String getEnvelopeId() { return envelopeId; }

    /** Sets envelope_id.
     * @param value the envelope_id value
     */
    public void setEnvelopeId(String value) { envelopeId = value; }

    @JsonProperty("failure_reason")
    private String failureReason;

    /** Why it was not sent, once failed: the refusal the create route answered then (no documents or credits left, a signer removed, the document already sent, the plan without Envelopes), that the workspace is being deleted, or that the member who scheduled it left the workspace. Written in Portuguese.
     * @return the failure_reason value, or null when absent
     */
    public String getFailureReason() { return failureReason; }

    /** Sets failure_reason.
     * @param value the failure_reason value
     */
    public void setFailureReason(String value) { failureReason = value; }

    @JsonProperty("created_at")
    private String createdAt;

    /** Created at.
     * @return the created_at value, or null when absent
     */
    public String getCreatedAt() { return createdAt; }

    /** Sets created_at.
     * @param value the created_at value
     */
    public void setCreatedAt(String value) { createdAt = value; }

    @JsonProperty("processed_at")
    private String processedAt;

    /** When it was sent, failed or cancelled.
     * @return the processed_at value, or null when absent
     */
    public String getProcessedAt() { return processedAt; }

    /** Sets processed_at.
     * @param value the processed_at value
     */
    public void setProcessedAt(String value) { processedAt = value; }

}
