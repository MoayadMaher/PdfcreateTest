package com.example.ubl;

import com.example.ubl.builder.UblInvoiceBuilder;
import com.example.ubl.model.*;
import com.example.ubl.util.*;

import org.w3c.dom.Document;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;

/** Example usage of the UBL invoice builder. */
public class Main {
    public static void main(String[] args) throws Exception {
        Seller seller = new Seller("ACME Trading", "123456789", null,
                "King Fahd Rd", "Riyadh", null, "12345");
        Buyer buyer = new Buyer("Customer", "987654321",
                "Olaya St", "Riyadh", null, null, "SA");
        InvoiceLine line1 = new InvoiceLine("1", "Product A", "EA",
                new BigDecimal("2"), new BigDecimal("100"));
        InvoiceLine line2 = new InvoiceLine("2", "Product B", "EA",
                new BigDecimal("1"), new BigDecimal("50"));
        InvoiceData data = new InvoiceData();
        data.setInvoiceId("INV-1000");
        data.setIssueDate(LocalDate.now());
        data.setIssueTime(LocalTime.now().withNano(0));
        data.setSeller(seller);
        data.setBuyer(buyer);
        data.setLines(Arrays.asList(line1, line2));
        data.setPaymentMeansCode("10");

        InvoiceTotals totals = InvoiceCalculator.calculate(data);
        InvoiceData.QrFields qr = new InvoiceData.QrFields(seller.getName(), seller.getVatNumber(),
                DateTimeUtil.toIso8601(data.getIssueDate(), data.getIssueTime(), ZoneId.systemDefault()),
                totals.getTaxInclusive().toPlainString(), totals.getTaxTotal().toPlainString());
        data.setQrFields(qr);

        UblInvoiceBuilder builder = new UblInvoiceBuilder();
        Document doc = builder.build(data);
        XmlUtil.write(doc, Paths.get("target/invoice.xml"));

        System.out.println("Total excl VAT: " + totals.getTaxExclusive());
        System.out.println("VAT total: " + totals.getTaxTotal());
        System.out.println("Total incl VAT: " + totals.getTaxInclusive());
        if (data.getQrFields() != null) {
            System.out.println("QR included");
        }
    }
}
