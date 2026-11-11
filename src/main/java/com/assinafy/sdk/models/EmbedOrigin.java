package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


/** An origin allowed to embed the workspace&#x27;s signing pages. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmbedOrigin {

    /** Creates an empty response. */
    public EmbedOrigin() {}

    @JsonProperty("id")
    private String id;

    /** Embed origin ID.
     * @return the id value, or null when absent
     */
    public String getId() { return id; }

    /** Sets id.
     * @param value the id value
     */
    public void setId(String value) { id = value; }

    @JsonProperty("origin")
    private String origin;

    /** Exact `scheme://host[:port]`.
     * @return the origin value, or null when absent
     */
    public String getOrigin() { return origin; }

    /** Sets origin.
     * @param value the origin value
     */
    public void setOrigin(String value) { origin = value; }

    @JsonProperty("created_at")
    private String createdAt;

    /** created at
     * @return the created_at value, or null when absent
     */
    public String getCreatedAt() { return createdAt; }

    /** Sets created_at.
     * @param value the created_at value
     */
    public void setCreatedAt(String value) { createdAt = value; }

}
