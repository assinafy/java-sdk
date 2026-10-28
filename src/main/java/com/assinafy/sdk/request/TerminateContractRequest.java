package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Optional reason and concurrency precondition for terminating a contract. */
public class TerminateContractRequest {

    private final Map<String, Object> attributes = new LinkedHashMap<>();

    /** Creates an empty termination request. */
    public TerminateContractRequest() {}

    /** {@return a new fluent builder} */
    public static Builder builder() { return new Builder(); }

    /**
     * Returns the exact fields that will be sent.
     *
     * @return immutable wire-name/value fields, including explicitly supplied nulls
     */
    @JsonAnyGetter
    public Map<String, Object> getAttributes() { return Collections.unmodifiableMap(attributes); }

    /**
     * Sets activity reason of at most 500 characters, or {@code null}.
     *
     * @param reason activity reason of at most 500 characters, or {@code null}
     */
    public void setReason(String reason) { attributes.put("reason", reason); }

    /**
     * Sets optional optimistic-concurrency timestamp.
     *
     * @param timestamp optional optimistic-concurrency timestamp
     */
    public void setUpdatedAt(String timestamp) { attributes.put("updated_at", timestamp); }

    /** Fluent builder for {@link TerminateContractRequest}. */
    public static final class Builder {
        private final TerminateContractRequest request = new TerminateContractRequest();

        /** Creates an empty builder. */
        public Builder() {}

        /**
         * Sets activity reason, or {@code null}.
         *
         * @param value activity reason, or {@code null}
         * @return this builder
         */
        public Builder reason(String value) { request.setReason(value); return this; }

        /**
         * Sets optimistic-concurrency timestamp.
         *
         * @param value optimistic-concurrency timestamp
         * @return this builder
         */
        public Builder updatedAt(String value) { request.setUpdatedAt(value); return this; }

        /** {@return the configured request} */
        public TerminateContractRequest build() { return request; }
    }
}
