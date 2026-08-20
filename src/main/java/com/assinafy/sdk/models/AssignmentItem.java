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

    /**
     * Creates an empty assignment item.
     */
    public AssignmentItem() {}

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
     * Returns the page.
     *
     * @return the page
     */
    public Object getPage() { return page; }

    /**
     * Sets the page.
     *
     * @param page the page
     */
    public void setPage(Object page) { this.page = page; }

    /**
     * Returns the signer.
     *
     * @return the signer
     */
    public Object getSigner() { return signer; }

    /**
     * Sets the signer.
     *
     * @param signer the signer
     */
    public void setSigner(Object signer) { this.signer = signer; }

    /**
     * Returns the field.
     *
     * @return the field
     */
    public Object getField() { return field; }

    /**
     * Sets the field.
     *
     * @param field the field
     */
    public void setField(Object field) { this.field = field; }

    /**
     * Returns the display settings.
     *
     * @return the display settings
     */
    public Object getDisplaySettings() { return displaySettings; }

    /**
     * Sets the display settings.
     *
     * @param displaySettings the display settings
     */
    public void setDisplaySettings(Object displaySettings) { this.displaySettings = displaySettings; }

    /**
     * Returns the API wire value.
     *
     * @return the API wire value
     */
    public Object getValue() { return value; }

    /**
     * Sets the value.
     *
     * @param value the value
     */
    public void setValue(Object value) { this.value = value; }

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
}
