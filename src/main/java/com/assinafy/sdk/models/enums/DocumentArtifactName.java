package com.assinafy.sdk.models.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DocumentArtifactName {
    ORIGINAL("original"),
    CERTIFICATED("certificated"),
    CERTIFICATE_PAGE("certificate-page"),
    BUNDLE("bundle");

    private final String value;

    DocumentArtifactName(String value) {
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
