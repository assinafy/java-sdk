package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


/** Certified-copy availability and resend result. No unmasked contact is returned. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CertifiedCopy {

    /** Creates an empty response. */
    public CertifiedCopy() {}

    @JsonProperty("available")
    private Boolean available;

    /** Whether the signer may request another copy.
     * @return the available value, or null when absent
     */
    public Boolean getAvailable() { return available; }

    /** Sets available.
     * @param value the available value
     */
    public void setAvailable(Boolean value) { available = value; }

    @JsonProperty("notification_method")
    private String notificationMethod;

    /** Email or Whatsapp, or null when no delivery channel is available.
     * @return the notification_method value, or null when absent
     */
    public String getNotificationMethod() { return notificationMethod; }

    /** Sets notification_method.
     * @param value the notification_method value
     */
    public void setNotificationMethod(String value) { notificationMethod = value; }

    @JsonProperty("resends_left")
    private Integer resendsLeft;

    /** Remaining WhatsApp resends; null for email.
     * @return the resends_left value, or null when absent
     */
    public Integer getResendsLeft() { return resendsLeft; }

    /** Sets resends_left.
     * @param value the resends_left value
     */
    public void setResendsLeft(Integer value) { resendsLeft = value; }

    @JsonProperty("sent_to")
    private String sentTo;

    /** Masked destination returned after resending.
     * @return the sent_to value, or null when absent
     */
    public String getSentTo() { return sentTo; }

    /** Sets sent_to.
     * @param value the sent_to value
     */
    public void setSentTo(String value) { sentTo = value; }

}
