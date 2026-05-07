package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterWebhookRequest {

    @JsonProperty("url")
    private String url;

    @JsonProperty("email")
    private String email;

    @JsonProperty("events")
    private List<String> events;

    @JsonProperty("is_active")
    private Boolean isActive;

    public RegisterWebhookRequest() {}

    public static Builder builder() {
        return new Builder();
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<String> getEvents() { return events; }
    public void setEvents(List<String> events) { this.events = events; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public static final class Builder {
        private final RegisterWebhookRequest req = new RegisterWebhookRequest();

        public Builder url(String url) { req.setUrl(url); return this; }
        public Builder email(String email) { req.setEmail(email); return this; }
        public Builder events(List<String> events) { req.setEvents(events); return this; }
        public Builder isActive(boolean isActive) { req.setIsActive(isActive); return this; }
        public RegisterWebhookRequest build() { return req; }
    }
}
