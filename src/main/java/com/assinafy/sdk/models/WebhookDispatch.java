package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the webhook dispatch in an Assinafy API response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookDispatch {

    /** Resource type discriminator; always {@code activity_dispatching_history} when present. */
    @JsonProperty("resource")
    private String resource;

    @JsonProperty("id")
    private String id;

    @JsonProperty("event")
    private String event;

    @JsonProperty("activity_id")
    private Long activityId;

    @JsonProperty("endpoint")
    private String endpoint;

    @JsonProperty("payload")
    private Object payload;

    @JsonProperty("delivered")
    private Boolean delivered;

    @JsonProperty("http_status")
    private Integer httpStatus;

    @JsonProperty("response_body")
    private String responseBody;

    @JsonProperty("error")
    private String error;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    /**
     * Creates an empty webhook dispatch.
     */
    public WebhookDispatch() {}

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
     * Returns the event.
     *
     * @return the event
     */
    public String getEvent() { return event; }

    /**
     * Sets the event.
     *
     * @param event the event
     */
    public void setEvent(String event) { this.event = event; }

    /**
     * Returns the activity id.
     *
     * @return the activity id
     */
    public Long getActivityId() { return activityId; }

    /**
     * Sets the activity id.
     *
     * @param activityId the activity id
     */
    public void setActivityId(Long activityId) { this.activityId = activityId; }

    /**
     * Returns the endpoint.
     *
     * @return the endpoint
     */
    public String getEndpoint() { return endpoint; }

    /**
     * Sets the endpoint.
     *
     * @param endpoint the endpoint
     */
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    /**
     * Returns the payload.
     *
     * @return the payload
     */
    public Object getPayload() { return payload; }

    /**
     * Sets the payload.
     *
     * @param payload the payload
     */
    public void setPayload(Object payload) { this.payload = payload; }

    /**
     * Returns the delivery flag.
     *
     * @return the delivery flag
     */
    public Boolean getDelivered() { return delivered; }

    /**
     * Sets the delivery flag.
     *
     * @param delivered the delivery flag
     */
    public void setDelivered(Boolean delivered) { this.delivered = delivered; }

    /**
     * Returns the http status.
     *
     * @return the http status
     */
    public Integer getHttpStatus() { return httpStatus; }

    /**
     * Sets the http status.
     *
     * @param httpStatus the http status
     */
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }

    /**
     * Returns the response body.
     *
     * @return the response body
     */
    public String getResponseBody() { return responseBody; }

    /**
     * Sets the response body.
     *
     * @param responseBody the response body
     */
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }

    /**
     * Returns the error.
     *
     * @return the error
     */
    public String getError() { return error; }

    /**
     * Sets the error.
     *
     * @param error the error
     */
    public void setError(String error) { this.error = error; }

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
     * Returns the last-update timestamp.
     *
     * @return the last-update timestamp
     */
    public String getUpdatedAt() { return updatedAt; }

    /**
     * Sets the last-update timestamp.
     *
     * @param updatedAt the last-update timestamp
     */
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
