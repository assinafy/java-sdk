package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Delivery result for one assignment notification channel. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationHistoryEntry {

    @JsonProperty("event")
    private String event;

    @JsonProperty("status")
    private String status;

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("sent_at")
    private String sentAt;

    @JsonProperty("failed_at")
    private String failedAt;

    /**
     * Creates an empty notification history entry.
     */
    public NotificationHistoryEntry() {}

    /**
     * Returns the event.
     *
     * @return the event
     */
    public String getEvent() { return event; }

    /**
     * Sets the event.
     *
     * @param event the event
     */
    public void setEvent(String event) { this.event = event; }

    /**
     * Returns the status.
     *
     * @return the status
     */
    public String getStatus() { return status; }

    /**
     * Sets the status.
     *
     * @param status the status
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * Returns the error code.
     *
     * @return the error code
     */
    public String getErrorCode() { return errorCode; }

    /**
     * Sets the error code.
     *
     * @param errorCode the error code
     */
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    /**
     * Returns the error message.
     *
     * @return the error message
     */
    public String getErrorMessage() { return errorMessage; }

    /**
     * Sets the error message.
     *
     * @param errorMessage the error message
     */
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    /**
     * Returns the send timestamp.
     *
     * @return the send timestamp
     */
    public String getSentAt() { return sentAt; }

    /**
     * Sets the send timestamp.
     *
     * @param sentAt the send timestamp
     */
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }

    /**
     * Returns the failure timestamp.
     *
     * @return the failure timestamp
     */
    public String getFailedAt() { return failedAt; }

    /**
     * Sets the failure timestamp.
     *
     * @param failedAt the failure timestamp
     */
    public void setFailedAt(String failedAt) { this.failedAt = failedAt; }
}
