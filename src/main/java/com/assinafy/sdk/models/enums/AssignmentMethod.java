package com.assinafy.sdk.models.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AssignmentMethod {
    VIRTUAL("virtual"),
    COLLECT("collect");

    private final String value;

    AssignmentMethod(String value) {
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
