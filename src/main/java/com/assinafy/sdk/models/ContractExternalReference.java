package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** An identifier for a contract in an external system. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractExternalReference {

    @JsonProperty("system")
    private String system;

    @JsonProperty("id")
    private String id;

    /** Creates an empty external reference. */
    public ContractExternalReference() {}

    /**
     * Creates an external reference.
     *
     * @param system external system code
     * @param id identifier in that system
     */
    public ContractExternalReference(String system, String id) {
        this.system = system;
        this.id = id;
    }

    /** {@return the normalized external system code} */
    public String getSystem() { return system; }

    /**
     * Sets external system code.
     *
     * @param system external system code
     */
    public void setSystem(String system) { this.system = system; }

    /** {@return the identifier in the external system} */
    public String getId() { return id; }

    /**
     * Sets identifier in the external system.
     *
     * @param id identifier in the external system
     */
    public void setId(String id) { this.id = id; }
}
