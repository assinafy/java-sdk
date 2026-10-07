package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An enrolled two-factor method of the authenticated user.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MfaMethod {

    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private String type;

    @JsonProperty("label")
    private String label;

    @JsonProperty("confirmed_at")
    private String confirmedAt;

    @JsonProperty("last_used_at")
    private String lastUsedAt;

    /**
     * Creates an empty instance.
     */
    public MfaMethod() {}

    /**
     * Returns the method ID.
     *
     * @return the method ID
     */
    public String getId() { return id; }

    /**
     * Sets the method ID.
     *
     * @param id the method ID
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns the method type, such as {@code Totp}.
     *
     * @return the method type, such as {@code Totp}
     */
    public String getType() { return type; }

    /**
     * Sets the method type, such as {@code Totp}.
     *
     * @param type the method type, such as {@code Totp}
     */
    public void setType(String type) { this.type = type; }

    /**
     * Returns the label given at enrollment, or {@code null}.
     *
     * @return the label given at enrollment, or {@code null}
     */
    public String getLabel() { return label; }

    /**
     * Sets the label given at enrollment, or {@code null}.
     *
     * @param label the label given at enrollment, or {@code null}
     */
    public void setLabel(String label) { this.label = label; }

    /**
     * Returns when the method was confirmed (ISO 8601).
     *
     * @return when the method was confirmed (ISO 8601)
     */
    public String getConfirmedAt() { return confirmedAt; }

    /**
     * Sets when the method was confirmed (ISO 8601).
     *
     * @param confirmedAt when the method was confirmed (ISO 8601)
     */
    public void setConfirmedAt(String confirmedAt) { this.confirmedAt = confirmedAt; }

    /**
     * Returns when the method was last used (ISO 8601), or {@code null}.
     *
     * @return when the method was last used (ISO 8601), or {@code null}
     */
    public String getLastUsedAt() { return lastUsedAt; }

    /**
     * Sets when the method was last used (ISO 8601), or {@code null}.
     *
     * @param lastUsedAt when the method was last used (ISO 8601), or {@code null}
     */
    public void setLastUsedAt(String lastUsedAt) { this.lastUsedAt = lastUsedAt; }
}
