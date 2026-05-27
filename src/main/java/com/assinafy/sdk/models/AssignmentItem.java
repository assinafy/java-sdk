package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An item to be completed within an assignment, as returned inside {@code assignment.items}.
 * The nested {@code page}, {@code signer} and {@code field} structures are left untyped
 * ({@code Object}) because their shape varies by item type.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssignmentItem {

    @JsonProperty("id")
    private String id;

    @JsonProperty("page")
    private Object page;

    @JsonProperty("signer")
    private Object signer;

    @JsonProperty("field")
    private Object field;

    @JsonProperty("display_settings")
    private Object displaySettings;

    @JsonProperty("value")
    private Object value;

    @JsonProperty("completed")
    private Boolean completed;

    public AssignmentItem() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Object getPage() { return page; }
    public void setPage(Object page) { this.page = page; }

    public Object getSigner() { return signer; }
    public void setSigner(Object signer) { this.signer = signer; }

    public Object getField() { return field; }
    public void setField(Object field) { this.field = field; }

    public Object getDisplaySettings() { return displaySettings; }
    public void setDisplaySettings(Object displaySettings) { this.displaySettings = displaySettings; }

    public Object getValue() { return value; }
    public void setValue(Object value) { this.value = value; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }
}
