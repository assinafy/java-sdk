package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Result of looking up a signed document by its public signature hash. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentVerification {

    @JsonProperty("hash")
    private String hash;

    @JsonProperty("id")
    private String id;

    @JsonProperty("status")
    private String status;

    @JsonProperty("page_count")
    private String pageCount;

    @JsonProperty("signer_count")
    private String signerCount;

    @JsonProperty("completed_count")
    private Integer completedCount;

    @JsonProperty("completed_at")
    private String completedAt;

    @JsonProperty("verified_at")
    private String verifiedAt;

    @JsonProperty("is_valid")
    private Boolean isValid;

    @JsonProperty("message")
    private String message;

    /**
     * Creates an empty document verification.
     */
    public DocumentVerification() {}

    /**
     * Returns the hash.
     *
     * @return the hash
     */
    public String getHash() { return hash; }

    /**
     * Sets the hash.
     *
     * @param hash the hash
     */
    public void setHash(String hash) { this.hash = hash; }

    /**
     * Returns the ID.
     *
     * @return the ID
     */
    public String getId() { return id; }

    /**
     * Sets the ID.
     *
     * @param id the ID
     */
    public void setId(String id) { this.id = id; }

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
     * Returns the page count.
     *
     * @return the page count
     */
    public String getPageCount() { return pageCount; }

    /**
     * Sets the page count.
     *
     * @param pageCount the page count
     */
    public void setPageCount(String pageCount) { this.pageCount = pageCount; }

    /**
     * Returns the signer count.
     *
     * @return the signer count
     */
    public String getSignerCount() { return signerCount; }

    /**
     * Sets the signer count.
     *
     * @param signerCount the signer count
     */
    public void setSignerCount(String signerCount) { this.signerCount = signerCount; }

    /**
     * Returns the completed signer count.
     *
     * @return the completed signer count
     */
    public Integer getCompletedCount() { return completedCount; }

    /**
     * Sets the completed signer count.
     *
     * @param completedCount the completed signer count
     */
    public void setCompletedCount(Integer completedCount) { this.completedCount = completedCount; }

    /**
     * Returns the completion timestamp.
     *
     * @return the completion timestamp
     */
    public String getCompletedAt() { return completedAt; }

    /**
     * Sets the completion timestamp.
     *
     * @param completedAt the completion timestamp
     */
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }

    /**
     * Returns the verification timestamp.
     *
     * @return the verification timestamp
     */
    public String getVerifiedAt() { return verifiedAt; }

    /**
     * Sets the verification timestamp.
     *
     * @param verifiedAt the verification timestamp
     */
    public void setVerifiedAt(String verifiedAt) { this.verifiedAt = verifiedAt; }

    /**
     * Returns the validity flag.
     *
     * @return the validity flag
     */
    public Boolean getIsValid() { return isValid; }

    /**
     * Sets the validity flag.
     *
     * @param isValid the validity flag
     */
    public void setIsValid(Boolean isValid) { this.isValid = isValid; }

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
}
