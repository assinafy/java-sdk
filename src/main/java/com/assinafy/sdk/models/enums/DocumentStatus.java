package com.assinafy.sdk.models.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Supported document status values.
 */
public enum DocumentStatus {
    /** Uploading document status. */
    UPLOADING("uploading"),
    /** Uploaded document status. */
    UPLOADED("uploaded"),
    /** Metadata processing document status. */
    METADATA_PROCESSING("metadata_processing"),
    /** Metadata ready document status. */
    METADATA_READY("metadata_ready"),
    /** Pending signature document status. */
    PENDING_SIGNATURE("pending_signature"),
    /** Expired document status. */
    EXPIRED("expired"),
    /** Certificating document status. */
    CERTIFICATING("certificating"),
    /** Certificated document status. */
    CERTIFICATED("certificated"),
    /** Rejected by signer document status. */
    REJECTED_BY_SIGNER("rejected_by_signer"),
    /** Rejected by user document status. */
    REJECTED_BY_USER("rejected_by_user"),
    /** Failed document status. */
    FAILED("failed");

    private final String value;

    DocumentStatus(String value) {
        this.value = value;
    }

    /**
     * Returns the API wire value.
     *
     * @return the API wire value
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Returns the API wire value.
     *
     * @return the API wire value
     */
    @Override
    public String toString() {
        return value;
    }
}
