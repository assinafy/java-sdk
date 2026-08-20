package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Authenticated-user profile returned by {@code GET /users/self} and authentication operations.
 * Dates are ISO-8601 strings; telephone, government ID, and scheduled-deletion date may be null.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthUser {

    private String id;
    private String name;
    private String email;
    private String telephone;

    @JsonProperty("government_id")
    private String governmentId;

    @JsonProperty("is_email_verified")
    private Boolean isEmailVerified;

    @JsonProperty("has_accepted_terms")
    private Boolean hasAcceptedTerms;

    @JsonProperty("is_password_set")
    private Boolean isPasswordSet;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("to_be_deleted_at")
    private String toBeDeletedAt;

    /**
     * Creates an empty authenticated user.
     */
    public AuthUser() {}

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
     * Returns the email address.
     *
     * @return the email address
     */
    public String getEmail() { return email; }

    /**
     * Sets the email address.
     *
     * @param email the email address
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Returns the telephone.
     *
     * @return the telephone
     */
    public String getTelephone() { return telephone; }

    /**
     * Sets the telephone.
     *
     * @param telephone the telephone
     */
    public void setTelephone(String telephone) { this.telephone = telephone; }

    /**
     * Returns the government ID.
     *
     * @return the government ID
     */
    public String getGovernmentId() { return governmentId; }

    /**
     * Sets the government ID.
     *
     * @param governmentId the government ID
     */
    public void setGovernmentId(String governmentId) { this.governmentId = governmentId; }

    /**
     * Returns the email-verification flag.
     *
     * @return the email-verification flag
     */
    public Boolean getIsEmailVerified() { return isEmailVerified; }

    /**
     * Sets the email-verification flag.
     *
     * @param emailVerified the email verified
     */
    public void setIsEmailVerified(Boolean emailVerified) { isEmailVerified = emailVerified; }

    /**
     * Returns the terms-acceptance flag.
     *
     * @return the terms-acceptance flag
     */
    public Boolean getHasAcceptedTerms() { return hasAcceptedTerms; }

    /**
     * Sets the terms-acceptance flag.
     *
     * @param acceptedTerms the accepted terms
     */
    public void setHasAcceptedTerms(Boolean acceptedTerms) { hasAcceptedTerms = acceptedTerms; }

    /**
     * Returns the password-set flag.
     *
     * @return the password-set flag
     */
    public Boolean getIsPasswordSet() { return isPasswordSet; }

    /**
     * Sets the password-set flag.
     *
     * @param passwordSet the password set
     */
    public void setIsPasswordSet(Boolean passwordSet) { isPasswordSet = passwordSet; }

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

    /**
     * Returns the scheduled-deletion timestamp.
     *
     * @return the scheduled-deletion timestamp
     */
    public String getToBeDeletedAt() { return toBeDeletedAt; }

    /**
     * Sets the scheduled-deletion timestamp.
     *
     * @param toBeDeletedAt the scheduled-deletion timestamp
     */
    public void setToBeDeletedAt(String toBeDeletedAt) { this.toBeDeletedAt = toBeDeletedAt; }
}
