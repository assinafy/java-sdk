package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Signer-facing identity-verification state or newly opened session. Capture URLs are bearer
 * credentials; preserve their fragments and never log them.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SignerIdentityVerification {

    @JsonProperty("id")
    private String id;

    @JsonProperty("can_start")
    private Boolean canStart;

    @JsonProperty("status")
    private String status;

    @JsonProperty("url")
    private String url;

    @JsonProperty("expires_at")
    private String expiresAt;

    @JsonProperty("completed_at")
    private String completedAt;

    /** Creates an empty identity verification. */
    public SignerIdentityVerification() {}

    /** Provider session ID, populated when starting a session.
     * @return the id value
     */
    public String getId() { return id; }

    /** Sets the id.
     * @param id the id value
     */
    public void setId(String id) { this.id = id; }

    /** Whether no session exists or the previous session expired before capture.
     * @return the can_start value
     */
    public Boolean getCanStart() { return canStart; }

    /** Sets the canStart.
     * @param canStart the can_start value
     */
    public void setCanStart(Boolean canStart) { this.canStart = canStart; }

    /** Pending, InReview, Approved, Declined, Expired, Abandoned, or a future status.
     * @return the status value
     */
    public String getStatus() { return status; }

    /** Sets the status.
     * @param status the status value
     */
    public void setStatus(String status) { this.status = status; }

    /** Hosted capture URL; open unchanged, including its fragment, without logging it.
     * @return the url value
     */
    public String getUrl() { return url; }

    /** Sets the url.
     * @param url the url value
     */
    public void setUrl(String url) { this.url = url; }

    /** ISO 8601 capture-link expiry; processing and review may finish later.
     * @return the expires_at value
     */
    public String getExpiresAt() { return expiresAt; }

    /** Sets the expiresAt.
     * @param expiresAt the expires_at value
     */
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

    /** ISO 8601 completion timestamp, or null.
     * @return the completed_at value
     */
    public String getCompletedAt() { return completedAt; }

    /** Sets the completedAt.
     * @param completedAt the completed_at value
     */
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }
}
