package com.assinafy.sdk.models.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Supported document artifact name values.
 */
public enum DocumentArtifactName {
    /** Original document artifact. */
    ORIGINAL("original"),
    /** Certificated document artifact. */
    CERTIFICATED("certificated"),
    /** Certificate page document artifact. */
    CERTIFICATE_PAGE("certificate-page"),
    /** PAdES document artifact. */
    PADES("pades"),
    /** Bundle document artifact. */
    BUNDLE("bundle");

    private final String value;

    DocumentArtifactName(String value) {
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
