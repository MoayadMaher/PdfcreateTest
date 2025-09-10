package com.example.ubl;

import com.example.ubl.builder.UblInvoiceBuilder;
import com.example.ubl.model.*;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

public class XmlBuildTest {
    public static void run() throws Exception {
        Seller seller = new Seller("S", "123", null, "st", "city", null, null);
        Buyer buyer = new Buyer("B", null, "st2", "city2", null, null, "SA");
        InvoiceLine line = new InvoiceLine("1", "A", "EA", new BigDecimal("1"), new BigDecimal("10"));
        InvoiceData data = new InvoiceData();
        data.setInvoiceId("INV1");
        data.setIssueDate(LocalDate.now());
        data.setIssueTime(LocalTime.NOON);
        data.setSeller(seller);
        data.setBuyer(buyer);
        data.setLines(Collections.singletonList(line));

        UblInvoiceBuilder builder = new UblInvoiceBuilder();
        Document doc = builder.build(data);
        Assertions.assertEquals("Invoice", doc.getDocumentElement().getLocalName(), "root");
        NodeList currency = doc.getElementsByTagNameNS("urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2", "DocumentCurrencyCode");
        Assertions.assertTrue(currency.getLength() == 1, "currency tag");
        Assertions.assertEquals("SAR", currency.item(0).getTextContent(), "currency value");
    }
}
