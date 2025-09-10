package com.example.ubl;

import com.example.ubl.model.*;
import com.example.ubl.util.InvoiceCalculator;

import java.math.BigDecimal;
import java.util.Arrays;

public class TotalsTest {
    public static void run() {
        Seller seller = new Seller("S", "123", null, "st", "city", null, null);
        Buyer buyer = new Buyer(null, null, "st2", "city2", null, null, "SA");
        InvoiceLine l1 = new InvoiceLine("1", "A", "EA", new BigDecimal("2"), new BigDecimal("100"));
        InvoiceLine l2 = new InvoiceLine("2", "B", "EA", new BigDecimal("1"), new BigDecimal("50"));
        InvoiceData data = new InvoiceData();
        data.setInvoiceId("1");
        data.setIssueDate(java.time.LocalDate.now());
        data.setIssueTime(java.time.LocalTime.NOON);
        data.setSeller(seller);
        data.setBuyer(buyer);
        data.setLines(Arrays.asList(l1, l2));

        InvoiceTotals totals = InvoiceCalculator.calculate(data);
        Assertions.assertEquals(new BigDecimal("250.00"), totals.getLineExtensionTotal(), "lineExt");
        Assertions.assertEquals(new BigDecimal("250.00"), totals.getTaxExclusive(), "taxExcl");
        Assertions.assertEquals(new BigDecimal("37.50"), totals.getTaxTotal(), "vat");
        Assertions.assertEquals(new BigDecimal("287.50"), totals.getTaxInclusive(), "total");

        data.setDiscountAmount(new BigDecimal("50"));
        totals = InvoiceCalculator.calculate(data);
        Assertions.assertEquals(new BigDecimal("250.00"), totals.getLineExtensionTotal(), "lineExt2");
        Assertions.assertEquals(new BigDecimal("200.00"), totals.getTaxExclusive(), "taxExcl2");
        Assertions.assertEquals(new BigDecimal("30.00"), totals.getTaxTotal(), "vat2");
        Assertions.assertEquals(new BigDecimal("230.00"), totals.getTaxInclusive(), "total2");
    }
}
