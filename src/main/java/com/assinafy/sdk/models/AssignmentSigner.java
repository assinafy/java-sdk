package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A signer as represented inside {@code assignment.signers}. Extends the base signer
 * fields with the per-assignment verification, notification and sequencing metadata
 * described in the Assignment Signer Object reference.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssignmentSigner {

    @JsonProperty("resource")
    private String resource;

    @JsonProperty("id")
    private String id;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("whatsapp_phone_number")
    private String whatsappPhoneNumber;

    @JsonProperty("has_accepted_terms")
    private Boolean hasAcceptedTerms;

    @JsonProperty("verification_method")
    private String verificationMethod;

    @JsonProperty("notification_methods")
    private List<String> notificationMethods;

    @JsonProperty("step")
    private Integer step;

    @JsonProperty("notified")
    private Boolean notified;

    @JsonProperty("completed")
    private Boolean completed;

    @JsonProperty("notification_history")
    private List<NotificationHistoryEntry> notificationHistory;

    /**
     * Creates an empty assignment signer.
     */
    public AssignmentSigner() {}

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
     * Returns the full name.
     *
     * @return the full name
     */
    public String getFullName() { return fullName; }

    /**
     * Sets the full name.
     *
     * @param fullName the full name
     */
    public void setFullName(String fullName) { this.fullName = fullName; }

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
     * Returns the WhatsApp phone number.
     *
     * @return the WhatsApp phone number
     */
    public String getWhatsappPhoneNumber() { return whatsappPhoneNumber; }

    /**
     * Sets the WhatsApp phone number.
     *
     * @param whatsappPhoneNumber the WhatsApp phone number
     */
    public void setWhatsappPhoneNumber(String whatsappPhoneNumber) { this.whatsappPhoneNumber = whatsappPhoneNumber; }

    /**
     * Returns the terms-acceptance flag.
     *
     * @return the terms-acceptance flag
     */
    public Boolean getHasAcceptedTerms() { return hasAcceptedTerms; }

    /**
     * Sets the terms-acceptance flag.
     *
     * @param hasAcceptedTerms the terms-acceptance flag
     */
    public void setHasAcceptedTerms(Boolean hasAcceptedTerms) { this.hasAcceptedTerms = hasAcceptedTerms; }

    /**
     * Returns the verification method.
     *
     * @return the verification method
     */
    public String getVerificationMethod() { return verificationMethod; }

    /**
     * Sets the verification method.
     *
     * @param verificationMethod the verification method
     */
    public void setVerificationMethod(String verificationMethod) { this.verificationMethod = verificationMethod; }

    /**
     * Returns the notification methods.
     *
     * @return the notification methods
     */
    public List<String> getNotificationMethods() { return notificationMethods; }

    /**
     * Sets the notification methods.
     *
     * @param notificationMethods the notification methods
     */
    public void setNotificationMethods(List<String> notificationMethods) { this.notificationMethods = notificationMethods; }

    /**
     * Returns the step.
     *
     * @return the step
     */
    public Integer getStep() { return step; }

    /**
     * Sets the step.
     *
     * @param step the step
     */
    public void setStep(Integer step) { this.step = step; }

    /**
     * Returns the notification flag.
     *
     * @return the notification flag
     */
    public Boolean getNotified() { return notified; }

    /**
     * Sets the notification flag.
     *
     * @param notified the notification flag
     */
    public void setNotified(Boolean notified) { this.notified = notified; }

    /**
     * Returns the completed.
     *
     * @return the completed
     */
    public Boolean getCompleted() { return completed; }

    /**
     * Sets the completed.
     *
     * @param completed the completed
     */
    public void setCompleted(Boolean completed) { this.completed = completed; }

    /**
     * Returns the per-signer notification delivery history.
     *
     * @return the notification history
     */
    public List<NotificationHistoryEntry> getNotificationHistory() { return notificationHistory; }

    /**
     * Sets the per-signer notification delivery history.
     *
     * @param notificationHistory the notification history
     */
    public void setNotificationHistory(List<NotificationHistoryEntry> notificationHistory) {
        this.notificationHistory = notificationHistory;
    }
}
