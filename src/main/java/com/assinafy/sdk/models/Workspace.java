package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * A workspace account, as returned by the {@code /accounts} endpoints. The
 * {@code notification_sender_type}, {@code roles} and {@code is_delete_allowed} fields are
 * populated on the create/update/get responses.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Workspace {

    @JsonProperty("resource")
    private String resource;

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("primary_color")
    private String primaryColor;

    @JsonProperty("secondary_color")
    private String secondaryColor;

    /** {@code User} (default) shows the document owner as the notification sender; {@code Account} shows this account. */
    @JsonProperty("notification_sender_type")
    private String notificationSenderType;

    /** The authenticated user's roles on this account (e.g. {@code owner}). */
    @JsonProperty("roles")
    private List<String> roles;

    @JsonProperty("is_delete_allowed")
    private Boolean isDeleteAllowed;

    @JsonProperty("limits")
    private Map<String, Integer> limits;

    @JsonProperty("created_at")
    private String createdAt;

    /**
     * Creates an empty workspace.
     */
    public Workspace() {}

    /**
     * Returns the resource type.
     *
     * @return the resource type
     */
    public String getResource() { return resource; }

    /**
     * Sets the resource type.
     *
     * @param resource the resource type
     */
    public void setResource(String resource) { this.resource = resource; }

    /**
     * Returns the ID.
     *
     * @return the ID
     */
    public String getId() { return id; }

    /**
     * Sets the ID.
     *
     * @param id the ID
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns the name.
     *
     * @return the name
     */
    public String getName() { return name; }

    /**
     * Sets the name.
     *
     * @param name the name
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns the primary color.
     *
     * @return the primary color
     */
    public String getPrimaryColor() { return primaryColor; }

    /**
     * Sets the primary color.
     *
     * @param primaryColor the primary color
     */
    public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }

    /**
     * Returns the secondary color.
     *
     * @return the secondary color
     */
    public String getSecondaryColor() { return secondaryColor; }

    /**
     * Sets the secondary color.
     *
     * @param secondaryColor the secondary color
     */
    public void setSecondaryColor(String secondaryColor) { this.secondaryColor = secondaryColor; }

    /**
     * Returns the notification sender type.
     *
     * @return the notification sender type
     */
    public String getNotificationSenderType() { return notificationSenderType; }

    /**
     * Sets the notification sender type.
     *
     * @param notificationSenderType the notification sender type
     */
    public void setNotificationSenderType(String notificationSenderType) { this.notificationSenderType = notificationSenderType; }

    /**
     * Returns the roles.
     *
     * @return the roles
     */
    public List<String> getRoles() { return roles; }

    /**
     * Sets the roles.
     *
     * @param roles the roles
     */
    public void setRoles(List<String> roles) { this.roles = roles; }

    /**
     * Returns the deletion-permission flag.
     *
     * @return the deletion-permission flag
     */
    public Boolean getIsDeleteAllowed() { return isDeleteAllowed; }

    /**
     * Sets the deletion-permission flag.
     *
     * @param isDeleteAllowed the deletion-permission flag
     */
    public void setIsDeleteAllowed(Boolean isDeleteAllowed) { this.isDeleteAllowed = isDeleteAllowed; }

    /**
     * Returns plan limits keyed by resource, currently including {@code webhook_endpoints}.
     *
     * @return plan limits, or {@code null} when the response omits them
     */
    public Map<String, Integer> getLimits() { return limits; }

    /**
     * Sets plan limits.
     *
     * @param limits plan limits
     */
    public void setLimits(Map<String, Integer> limits) { this.limits = limits; }

    /**
     * Returns the creation timestamp.
     *
     * @return the creation timestamp
     */
    public String getCreatedAt() { return createdAt; }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt the creation timestamp
     */
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    @JsonProperty("signing_features")
    private Map<String, Boolean> signingFeatures;

    /** Only with `?expand=signing_features` on `GET /v1/accounts/{accountId}`: which signing features the workspace&#x27;s subscription includes now (its plan&#x27;s and any granted to it), so a client offers only the identity checks and notices it can use. `DigitalCertificate` and `VerificationIdDocumentFaceMatchBasic` gate the `DigitalCertificate` and `IdDocumentFaceMatchBasic` verification methods, `WhatsappNotification` the `Whatsapp` notification method (and so the `Whatsapp` verification method), and `VerificationBypass` the `Bypass` verification method. `false` means creating an assignment, or a document from a template, with that method (or estimating its cost) answers `403`. `Embed` gates embedded signing: with `false`, the embed origins endpoints, `&quot;embedded&quot;: true` on a signer and minting an embed session answer `403` with code `PlanRestricted`. `true` is not a promise: credits, the assignment&#x27;s own rules (a `DigitalCertificate` signer needs a `virtual` assignment) and a change of plan still apply. Requires the `account:read` OAuth scope.
     * @return the signing_features value, or null when absent
     */
    public Map<String, Boolean> getSigningFeatures() { return signingFeatures; }

    /** Sets signing_features.
     * @param value the signing_features value
     */
    public void setSigningFeatures(Map<String, Boolean> value) { signingFeatures = value; }

}
