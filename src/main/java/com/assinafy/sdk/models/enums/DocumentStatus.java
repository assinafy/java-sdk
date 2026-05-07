package com.assinafy.sdk.models.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DocumentStatus {
    UPLOADING("uploading"),
    UPLOADED("uploaded"),
    METADATA_PROCESSING("metadata_processing"),
    METADATA_READY("metadata_ready"),
    PENDING_SIGNATURE("pending_signature"),
    EXPIRED("expired"),
    CERTIFICATING("certificating"),
    CERTIFICATED("certificated"),
    REJECTED_BY_SIGNER("rejected_by_signer"),
    REJECTED_BY_USER("rejected_by_user"),
    FAILED("failed");

    private final String value;

    DocumentStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
