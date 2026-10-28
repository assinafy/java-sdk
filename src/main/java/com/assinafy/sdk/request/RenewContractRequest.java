package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Terms for starting a contract's next lifecycle term. */
public class RenewContractRequest {

    private final Map<String, Object> attributes = new LinkedHashMap<>();

    /** Creates an empty renewal request. */
    public RenewContractRequest() {}

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
     * Sets later end date, or {@code null} to renew by the configured term.
     *
     * @param date later end date, or {@code null} to renew by the configured term
     */
    public void setEndDate(String date) { attributes.put("end_date", date); }

    /**
     * Sets replacement value, or {@code null} to clear it.
     *
     * @param value replacement value, or {@code null} to clear it
     */
    public void setValue(BigDecimal value) { attributes.put("value", value); }

    /**
     * Sets current term, required when no nonnull end date is supplied.
     *
     * @param term current term, required when no nonnull end date is supplied
     */
    public void setTerm(Integer term) { attributes.put("term", term); }

    /**
     * Sets optional optimistic-concurrency timestamp.
     *
     * @param timestamp optional optimistic-concurrency timestamp
     */
    public void setUpdatedAt(String timestamp) { attributes.put("updated_at", timestamp); }

    /** Fluent builder for {@link RenewContractRequest}. */
    public static final class Builder {
        private final RenewContractRequest request = new RenewContractRequest();

        /** Creates an empty builder. */
        public Builder() {}

        /**
         * Sets later end date, or {@code null}.
         *
         * @param value later end date, or {@code null}
         * @return this builder
         */
        public Builder endDate(String value) { request.setEndDate(value); return this; }

        /**
         * Sets replacement value, or {@code null}.
         *
         * @param value replacement value, or {@code null}
         * @return this builder
         */
        public Builder value(BigDecimal value) { request.setValue(value); return this; }

        /**
         * Sets current contract term.
         *
         * @param value current contract term
         * @return this builder
         */
        public Builder term(Integer value) { request.setTerm(value); return this; }

        /**
         * Sets optimistic-concurrency timestamp.
         *
         * @param value optimistic-concurrency timestamp
         * @return this builder
         */
        public Builder updatedAt(String value) { request.setUpdatedAt(value); return this; }

        /** {@return the configured request} */
        public RenewContractRequest build() { return request; }
    }
}
