package com.assinafy.sdk.exceptions;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Base unchecked exception for SDK validation, transport, and API failures. */
public class AssinafyException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Immutable top-level snapshot of diagnostic fields. */
    @SuppressWarnings("serial") // Diagnostic values are not required to support Java serialization.
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
     * Create an SDK exception with a shallow, immutable structured-context snapshot.
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

    /** {@return an immutable shallow snapshot of diagnostic context} */
    public Map<String, Object> getContext() {
        return context;
    }
}
