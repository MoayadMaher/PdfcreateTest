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
    public void throwsExceptionOnMissingLineItemDescription() {
        String args = String.join("<fm>",
                "/tmp",
                "INV1<sm>20240101<sm>20240101<sm>20240102",
                "VAT1",
                "20240101",
                "",
                ""
        );

        // With security improvements, we now properly validate input format
        assertThrows(IllegalArgumentException.class, () -> InvoiceParser.parse(args));
    }
}

