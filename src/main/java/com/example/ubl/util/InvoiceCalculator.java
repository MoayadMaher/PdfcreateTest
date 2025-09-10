package com.example.ubl.util;

import com.example.ubl.model.InvoiceData;
import com.example.ubl.model.InvoiceLine;
import com.example.ubl.model.InvoiceTotals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/** Utility to calculate invoice totals with discount handling. */
public final class InvoiceCalculator {
    private InvoiceCalculator() {}

    public static InvoiceTotals calculate(InvoiceData data) {
        List<BigDecimal> lineNets = new ArrayList<>();
        BigDecimal sumNet = BigDecimal.ZERO;
        for (InvoiceLine line : data.getLines()) {
            BigDecimal lineNet = line.getUnitPriceNet().multiply(line.getQuantity());
            lineNet = lineNet.setScale(2, RoundingMode.HALF_UP);
            lineNets.add(lineNet);
            sumNet = sumNet.add(lineNet);
        }
        BigDecimal discount = data.getDiscountAmount() == null ? BigDecimal.ZERO : data.getDiscountAmount();
        discount = discount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalVat = BigDecimal.ZERO;
        if (discount.compareTo(BigDecimal.ZERO) > 0 && sumNet.compareTo(BigDecimal.ZERO) > 0) {
            for (int i = 0; i < lineNets.size(); i++) {
                InvoiceLine line = data.getLines().get(i);
                BigDecimal lineNet = lineNets.get(i);
                BigDecimal proportion = lineNet.divide(sumNet, 10, RoundingMode.HALF_UP);
                BigDecimal base = lineNet.subtract(discount.multiply(proportion));
                BigDecimal lineVat = base.multiply(line.getVatPercent())
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                totalVat = totalVat.add(lineVat);
            }
        } else {
            for (int i = 0; i < lineNets.size(); i++) {
                InvoiceLine line = data.getLines().get(i);
                BigDecimal lineVat = lineNets.get(i).multiply(line.getVatPercent())
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                totalVat = totalVat.add(lineVat);
            }
        }
        BigDecimal taxExclusive = sumNet.subtract(discount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxInclusive = taxExclusive.add(totalVat).setScale(2, RoundingMode.HALF_UP);
        return new InvoiceTotals(sumNet.setScale(2, RoundingMode.HALF_UP),
                taxExclusive, totalVat.setScale(2, RoundingMode.HALF_UP), taxInclusive, discount);
    }
}
