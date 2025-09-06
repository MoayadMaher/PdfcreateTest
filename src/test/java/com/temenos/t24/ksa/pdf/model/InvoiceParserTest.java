package com.temenos.t24.ksa.pdf.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class InvoiceParserTest {

    @Test
    public void parsesMinimalInput() {
        String args = String.join("<fm>",
                "/tmp",
                "INV1<sm>20240101<sm>20240101<sm>20240102",
                "VAT1<sm>GRP1<sm>John<sm1>جون<sm>Addr<sm1>عنوان<sm>City<sm1>مدينة<sm>Country<sm1>بلد",
                "",
                "100<sm>0<sm>15<sm>115",
                "TRN123<sm>IBAN123"
        );

        InvoiceData data = InvoiceParser.parse(args);
        assertNull(data.arabicFontPath);
        assertNull(data.logoPath);
        assertNull(data.footerPath);
        assertNotNull(data.lineItems);
        assertTrue(data.lineItems.isEmpty());
        assertEquals("INV1", data.header.invoiceNumber);
    }

    @Test
    public void throwsExceptionOnInvalidInput() {
        String args = String.join("<fm>",
                "/tmp",
                "INV1<sm>20240101<sm>20240101<sm>20240102",
                "VAT1",
                "20240101",
                "",
                ""
        );

        assertThrows(IllegalArgumentException.class, () -> InvoiceParser.parse(args));
    }

    @Test
    public void throwsExceptionOnNullInput() {
        assertThrows(IllegalArgumentException.class, () -> InvoiceParser.parse(null));
    }

    @Test
    public void throwsExceptionOnEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> InvoiceParser.parse(""));
        assertThrows(IllegalArgumentException.class, () -> InvoiceParser.parse("   "));
    }

    @Test
    public void handlesMalformedLineItemsGracefully() {
        String args = String.join("<fm>",
                "/tmp",
                "INV1<sm>20240101<sm>20240101<sm>20240102",
                "VAT1<sm>GRP1<sm>John<sm1>جون<sm>Addr<sm1>عنوان<sm>City<sm1>مدينة<sm>Country<sm1>بلد",
                "malformed<vm>20240101<sm>desc", // one malformed, one valid line item
                "100<sm>0<sm>15<sm>115",
                "TRN123<sm>IBAN123"
        );

        InvoiceData data = InvoiceParser.parse(args);
        assertNotNull(data.lineItems);
        assertEquals(1, data.lineItems.size()); // Only the valid item should be parsed
        assertEquals("20240101", data.lineItems.get(0).supplyDate);
    }
}

