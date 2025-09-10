package com.example.ubl.model;

import java.math.BigDecimal;

/** Invoice line item. */
public class InvoiceLine {
    private String id;
    private String description;
    private String unitCode;
    private BigDecimal quantity;
    private BigDecimal unitPriceNet;
    private BigDecimal vatPercent = new BigDecimal("15.0");

    public InvoiceLine() {}

    public InvoiceLine(String id, String description, String unitCode,
                       BigDecimal quantity, BigDecimal unitPriceNet) {
        this.id = id;
        this.description = description;
        this.unitCode = unitCode;
        this.quantity = quantity;
        this.unitPriceNet = unitPriceNet;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getUnitCode() { return unitCode; }
    public void setUnitCode(String unitCode) { this.unitCode = unitCode; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPriceNet() { return unitPriceNet; }
    public void setUnitPriceNet(BigDecimal unitPriceNet) { this.unitPriceNet = unitPriceNet; }
    public BigDecimal getVatPercent() { return vatPercent; }
    public void setVatPercent(BigDecimal vatPercent) { this.vatPercent = vatPercent; }
}
