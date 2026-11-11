package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


/** The workspace&#x27;s notification rules, set by its owners. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationPolicy {

    /** Creates an empty response. */
    public NotificationPolicy() {}

    @JsonProperty("editable")
    private Boolean editable;

    /** Whether the requesting user is an owner and can change the policy.
     * @return the editable value, or null when absent
     */
    public Boolean getEditable() { return editable; }

    /** Sets editable.
     * @param value the editable value
     */
    public void setEditable(Boolean value) { editable = value; }

    @JsonProperty("owners_receive_all")
    private Boolean ownersReceiveAll;

    /** Owners also get the push alerts for every member&#x27;s documents. Each owner&#x27;s own preferences still apply.
     * @return the owners_receive_all value, or null when absent
     */
    public Boolean getOwnersReceiveAll() { return ownersReceiveAll; }

    /** Sets owners_receive_all.
     * @param value the owners_receive_all value
     */
    public void setOwnersReceiveAll(Boolean value) { ownersReceiveAll = value; }

    @JsonProperty("events")
    private NotificationPolicyEvents events;

    /** events
     * @return the events value, or null when absent
     */
    public NotificationPolicyEvents getEvents() { return events; }

    /** Sets events.
     * @param value the events value
     */
    public void setEvents(NotificationPolicyEvents value) { events = value; }

}
