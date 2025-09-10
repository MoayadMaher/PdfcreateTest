package com.example.ubl.model;

import java.math.BigDecimal;

/** Computed invoice totals. */
public class InvoiceTotals {
    private final BigDecimal lineExtensionTotal; // sum lines before discount
    private final BigDecimal taxExclusive;
    private final BigDecimal taxTotal;
    private final BigDecimal taxInclusive;
    private final BigDecimal discount;

    public InvoiceTotals(BigDecimal lineExtensionTotal, BigDecimal taxExclusive,
                         BigDecimal taxTotal, BigDecimal taxInclusive,
                         BigDecimal discount) {
        this.lineExtensionTotal = lineExtensionTotal;
        this.taxExclusive = taxExclusive;
        this.taxTotal = taxTotal;
        this.taxInclusive = taxInclusive;
        this.discount = discount;
    }

    public BigDecimal getLineExtensionTotal() { return lineExtensionTotal; }
    public BigDecimal getTaxExclusive() { return taxExclusive; }
    public BigDecimal getTaxTotal() { return taxTotal; }
    public BigDecimal getTaxInclusive() { return taxInclusive; }
    public BigDecimal getDiscount() { return discount; }
}
