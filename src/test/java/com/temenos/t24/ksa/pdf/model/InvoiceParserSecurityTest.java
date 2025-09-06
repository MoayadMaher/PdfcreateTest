package com.temenos.t24.ksa.pdf.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for enhanced InvoiceParser security features.
 */
class InvoiceParserSecurityTest {

    @Test
    void testNullInputHandling() {
        assertThrows(IllegalArgumentException.class, () -> {
            InvoiceParser.parse(null);
        });
    }

    @Test
    void testEmptyInputHandling() {
        assertThrows(IllegalArgumentException.class, () -> {
            InvoiceParser.parse("");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            InvoiceParser.parse("   ");
        });
    }

    @Test
    void testInvalidFormatHandling() {
        // Test input with too few fields
        assertThrows(IllegalArgumentException.class, () -> {
            InvoiceParser.parse("field1<fm>field2<fm>field3");
        });
    }

    @Test
    void testControlCharacterSanitization() {
        String inputWithControlChars = "test-output/invoice.pdf<fm>INV001\u0000<sm>2024-01-01<fm>Customer\u001f<sm>Details<sm>Name<sm1>Arabic<sm>Address<sm1>ArabicAddr<sm>City<sm1>ArabicCity<sm>Country<sm1>ArabicCountry<fm>LineItem<vm>100<sm>200<fm>Totals<fm>IBAN";
        
        InvoiceData result = InvoiceParser.parse(inputWithControlChars);
        
        // Verify control characters are removed
        assertFalse(result.header.invoiceNumber.contains("\u0000"));
        // Check if englishName exists before testing
        if (result.customer.englishName != null) {
            assertFalse(result.customer.englishName.contains("\u001f"));
        }
    }

    @Test
    void testMalformedLineItemHandling() {
        String input = "test-output/invoice.pdf<fm>INV001<sm>2024-01-01<fm>Customer<sm>Details<fm>MalformedItem<vm>ValidItem<sm>Description<sm>100<sm>1<sm>0<sm>100<sm>15<sm>15<sm>115<fm>100<sm>15<sm>115<fm>TAX123<sm>IBAN456";
        
        InvoiceData result = InvoiceParser.parse(input);
        
        // Should have only the valid line item, malformed one should be skipped
        assertEquals(1, result.lineItems.size());
        assertEquals("Description", result.lineItems.get(0).arabicDescription);
    }

    @Test
    void testArrayBoundsProtection() {
        // Test with minimal valid input to check bounds protection
        String minimalInput = "test-output/invoice.pdf<fm>INV001<fm>Customer<fm>LineItem<sm>Description<fm>100<fm>TAX123";
        
        assertDoesNotThrow(() -> {
            InvoiceData result = InvoiceParser.parse(minimalInput);
            assertNotNull(result);
        });
    }

    @Test
    void testPartialDataHandling() {
        String partialInput = "test-output/invoice.pdf<fm>INV001<sm>2024-01-01<fm>Customer<sm>Details<fm>Item<sm>Desc<sm>100<sm>1<sm>0<sm>100<fm>100<sm>0<sm>15<sm>115<fm>TAX123<sm>IBAN456";
        
        InvoiceData result = InvoiceParser.parse(partialInput);
        
        // Should handle partial data gracefully
        assertNotNull(result.header);
        assertNotNull(result.customer);
        assertNotNull(result.totals);
        assertNotNull(result.iban);
    }
}