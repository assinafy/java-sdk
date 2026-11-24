package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Delivery history for one document signer; opened_at is a hint, not proof of reading. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SignerNotificationHistory {
    /** Creates an empty response model. */
    public SignerNotificationHistory() {}

    @JsonProperty("signer_id")
    private String signerId;

    /** The contact ID.
     * @return the signer_id value, or null when absent
     */
    public String getSignerId() { return signerId; }

    /** Sets signer_id.
     * @param value the signer_id value
     */
    public void setSignerId(String value) { signerId = value; }

    @JsonProperty("notification_history")
    private List<NotificationHistoryEntry> notificationHistory;

    /** Notification attempts and their delivery/open timestamps.
     * @return the notification_history value, or null when absent
     */
    public List<NotificationHistoryEntry> getNotificationHistory() { return notificationHistory; }

    /** Sets notification_history.
     * @param value the notification_history value
     */
    public void setNotificationHistory(List<NotificationHistoryEntry> value) { notificationHistory = value; }

}
