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
    private List<Object> notificationHistory;

    public AssignmentSigner() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getWhatsappPhoneNumber() { return whatsappPhoneNumber; }
    public void setWhatsappPhoneNumber(String whatsappPhoneNumber) { this.whatsappPhoneNumber = whatsappPhoneNumber; }

    public Boolean getHasAcceptedTerms() { return hasAcceptedTerms; }
    public void setHasAcceptedTerms(Boolean hasAcceptedTerms) { this.hasAcceptedTerms = hasAcceptedTerms; }

    public String getVerificationMethod() { return verificationMethod; }
    public void setVerificationMethod(String verificationMethod) { this.verificationMethod = verificationMethod; }

    public List<String> getNotificationMethods() { return notificationMethods; }
    public void setNotificationMethods(List<String> notificationMethods) { this.notificationMethods = notificationMethods; }

    public Integer getStep() { return step; }
    public void setStep(Integer step) { this.step = step; }

    public Boolean getNotified() { return notified; }
    public void setNotified(Boolean notified) { this.notified = notified; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public List<Object> getNotificationHistory() { return notificationHistory; }
    public void setNotificationHistory(List<Object> notificationHistory) { this.notificationHistory = notificationHistory; }
}
