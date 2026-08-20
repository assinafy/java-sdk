package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/** One charge contributing to an assignment or template-document cost estimate. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CostEstimateBreakdownItem {

    @JsonProperty("code")
    private String code;

    @JsonProperty("name")
    private String name;

    @JsonProperty("cost")
    private BigDecimal cost;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("unit_cost")
    private BigDecimal unitCost;

    /**
     * Creates an empty cost estimate breakdown item.
     */
    public CostEstimateBreakdownItem() {}

    /**
     * Returns the code.
     *
     * @return the code
     */
    public String getCode() { return code; }

    /**
     * Sets the code.
     *
     * @param code the code
     */
    public void setCode(String code) { this.code = code; }

    /**
     * Returns the name.
     *
     * @return the name
     */
    public String getName() { return name; }

    /**
     * Sets the name.
     *
     * @param name the name
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns the cost.
     *
     * @return the cost
     */
    public BigDecimal getCost() { return cost; }

    /**
     * Sets the cost.
     *
     * @param cost the cost
     */
    public void setCost(BigDecimal cost) { this.cost = cost; }

    /**
     * Returns the quantity.
     *
     * @return the quantity
     */
    public Integer getQuantity() { return quantity; }

    /**
     * Sets the quantity.
     *
     * @param quantity the quantity
     */
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    /**
     * Returns the unit cost.
     *
     * @return the unit cost
     */
    public BigDecimal getUnitCost() { return unitCost; }

    /**
     * Sets the unit cost.
     *
     * @param unitCost the unit cost
     */
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
}
