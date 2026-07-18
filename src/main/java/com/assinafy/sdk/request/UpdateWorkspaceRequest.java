package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateWorkspaceRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("primary_color")
    private String primaryColor;

    @JsonProperty("secondary_color")
    private String secondaryColor;

    /**
     * Who signers see as the notification sender: {@code User} (default) shows the document
     * owner's name; {@code Account} shows this account's name.
     */
    @JsonProperty("notification_sender_type")
    private String notificationSenderType;

    public UpdateWorkspaceRequest() {}

    public static Builder builder() {
        return new Builder();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }

    public String getSecondaryColor() { return secondaryColor; }
    public void setSecondaryColor(String secondaryColor) { this.secondaryColor = secondaryColor; }

    public String getNotificationSenderType() { return notificationSenderType; }
    public void setNotificationSenderType(String notificationSenderType) { this.notificationSenderType = notificationSenderType; }

    public static final class Builder {
        private final UpdateWorkspaceRequest req = new UpdateWorkspaceRequest();

        public Builder name(String name) { req.setName(name); return this; }
        public Builder primaryColor(String color) { req.setPrimaryColor(color); return this; }
        public Builder secondaryColor(String color) { req.setSecondaryColor(color); return this; }
        /** {@code User} (default) or {@code Account} — who signers see as the notification sender. */
        public Builder notificationSenderType(String type) { req.setNotificationSenderType(type); return this; }
        public UpdateWorkspaceRequest build() { return req; }
    }
}
