package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


/** Cancellation outcome for one document of an envelope. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnvelopeCancellation {

    /** Creates an empty response. */
    public EnvelopeCancellation() {}

    @JsonProperty("id")
    private String id;

    /** Document ID.
     * @return the id value, or null when absent
     */
    public String getId() { return id; }

    /** Sets id.
     * @param value the id value
     */
    public void setId(String value) { id = value; }

    @JsonProperty("status")
    private String status;

    /** Document status after cancellation.
     * @return the status value, or null when absent
     */
    public String getStatus() { return status; }

    /** Sets status.
     * @param value the status value
     */
    public void setStatus(String value) { status = value; }

    @JsonProperty("cancelled")
    private Boolean cancelled;

    /** Whether this call cancelled the document.
     * @return the cancelled value, or null when absent
     */
    public Boolean getCancelled() { return cancelled; }

    /** Sets cancelled.
     * @param value the cancelled value
     */
    public void setCancelled(Boolean value) { cancelled = value; }

}
