package com.assinafy.sdk.exceptions;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Indicates that caller input does not satisfy an SDK or API request contract. */
public class ValidationException extends AssinafyException {

    private static final long serialVersionUID = 1L;

    /** Immutable field-level errors. */
    @SuppressWarnings("serial") // Field errors may contain non-serializable diagnostic values.
    private final Map<String, Object> errors;

    /**
     * Create a validation exception without field-level errors.
     *
     * @param message validation message
     */
    public ValidationException(String message) {
        super(message);
        this.errors = Collections.emptyMap();
    }

    /**
     * Create a validation exception with immutable field-level errors.
     *
     * @param message validation message
     * @param errors field or rule errors; {@code null} is treated as empty
     */
    public ValidationException(String message, Map<String, Object> errors) {
        super(message, Map.of("errors", errors != null ? errors : Map.of()));
        this.errors = errors != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(errors))
                : Collections.emptyMap();
    }

    /** {@return immutable field-level validation errors} */
    public Map<String, Object> getErrors() {
        return errors;
    }
}
