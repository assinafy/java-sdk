package com.assinafy.sdk.models.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Supported assignment method values.
 */
public enum AssignmentMethod {
    /** Virtual assignment method. */
    VIRTUAL("virtual"),
    /** Collect assignment method. */
    COLLECT("collect");

    private final String value;

    AssignmentMethod(String value) {
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
