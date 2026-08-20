package com.assinafy.sdk.exceptions;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Base unchecked exception for SDK validation, transport, and API failures. */
public class AssinafyException extends RuntimeException {

    /** Immutable diagnostic fields. */
    private final Map<String, Object> context;

    /**
     * Create an SDK exception without structured context.
     *
     * @param message error message
     */
    public AssinafyException(String message) {
        super(message);
        this.context = Collections.emptyMap();
    }

    /**
     * Create an SDK exception with immutable structured context.
     *
     * @param message error message
     * @param context diagnostic fields; {@code null} is treated as empty
     */
    public AssinafyException(String message, Map<String, Object> context) {
        super(message);
        this.context = context != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(context))
                : Collections.emptyMap();
    }

    /**
     * Create an SDK exception with structured context and an underlying cause.
     *
     * @param message error message
     * @param context diagnostic fields; {@code null} is treated as empty
     * @param cause underlying failure
     */
    public AssinafyException(String message, Map<String, Object> context, Throwable cause) {
        super(message, cause);
        this.context = context != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(context))
                : Collections.emptyMap();
    }

    /** {@return immutable diagnostic context} */
    public Map<String, Object> getContext() {
        return context;
    }
}
