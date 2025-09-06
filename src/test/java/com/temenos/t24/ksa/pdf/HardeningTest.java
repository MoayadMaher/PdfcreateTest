package com.temenos.t24.ksa.pdf;

import com.temenos.t24.ksa.pdf.model.InvoiceParser;
import com.temenos.t24.ksa.pdf.qr.TLVUtils;
import com.temenos.t24.ksa.pdf.qr.ZatcaQRData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify security hardening improvements.
 */
public class HardeningTest {

    @Test
    public void testInputValidationInPDFCreator() {
        // Test null input
        assertThrows(IllegalArgumentException.class, () -> PDFcreator.createPDF((String) null));
        
        // Test empty input
        assertThrows(IllegalArgumentException.class, () -> PDFcreator.createPDF(""));
        assertThrows(IllegalArgumentException.class, () -> PDFcreator.createPDF("   "));
        
        // Test null data object
        assertThrows(IllegalArgumentException.class, () -> PDFcreator.createPDF((com.temenos.t24.ksa.pdf.model.InvoiceData) null));
    }

    @Test
    public void testPathTraversalPrevention() {
        String maliciousInput = String.join("<fm>",
                "../../../etc/passwd", // Path traversal attempt
                "INV1<sm>20240101<sm>20240101<sm>20240102",
                "VAT1<sm>GRP1<sm>John<sm1>جون<sm>Addr<sm1>عنوان<sm>City<sm1>مدينة<sm>Country<sm1>بلد",
                "",
                "100<sm>0<sm>15<sm>115",
                "TRN123<sm>IBAN123",
                "../../../etc/passwd", // Font path traversal
                "../../../etc/passwd", // Logo path traversal  
                "../../../etc/passwd"  // Footer path traversal
        );

        // Should throw SecurityException for path traversal
        assertThrows(SecurityException.class, () -> PDFcreator.createPDF(maliciousInput));
    }

    @Test
    public void testInvalidFilePathHandling() {
        String invalidFileInput = String.join("<fm>",
                "test-output/valid.pdf",
                "INV1<sm>20240101<sm>20240101<sm>20240102",
                "VAT1<sm>GRP1<sm>John<sm1>جون<sm>Addr<sm1>عنوان<sm>City<sm1>مدينة<sm>Country<sm1>بلد",
                "",
                "100<sm>0<sm>15<sm>115",
                "TRN123<sm>IBAN123",
                "nonexistent-font.ttf", // Non-existent font file
                "test-resources/images/logo.png",
                "test-resources/images/footer.png"
        );

        // Should throw IllegalArgumentException for non-existent file
        assertThrows(IllegalArgumentException.class, () -> PDFcreator.createPDF(invalidFileInput));
    }

    @Test
    public void testTLVUtilsInputValidation() {
        // Test null data in TLVUtils
        assertThrows(IllegalArgumentException.class, () -> TLVUtils.generateBase64TLV(null));
        
        // Test valid data still works
        ZatcaQRData validData = new ZatcaQRData();
        validData.sellerName = "Test Seller";
        validData.vatNumber = "123456789";
        validData.timestamp = "2024-01-01T00:00:00Z";
        validData.invoiceTotalWithVat = "100.00";
        validData.vatTotal = "15.00";
        
        assertDoesNotThrow(() -> TLVUtils.generateBase64TLV(validData));
        String result = TLVUtils.generateBase64TLV(validData);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testInvoiceParserRobustness() {
        // Test comprehensive input validation
        assertThrows(IllegalArgumentException.class, () -> InvoiceParser.parse(null));
        assertThrows(IllegalArgumentException.class, () -> InvoiceParser.parse(""));
        assertThrows(IllegalArgumentException.class, () -> InvoiceParser.parse("   "));
        
        // Test insufficient fields
        assertThrows(IllegalArgumentException.class, () -> InvoiceParser.parse("one<fm>two<fm>three"));
        
        // Test that malformed line items are skipped gracefully
        String inputWithMalformedItems = String.join("<fm>",
                "/tmp/test.pdf",
                "INV1<sm>20240101<sm>20240101<sm>20240102",
                "VAT1<sm>GRP1<sm>John<sm1>جون<sm>Addr<sm1>عنوان<sm>City<sm1>مدينة<sm>Country<sm1>بلد",
                "malformed<vm>20240101<sm>valid_desc<sm1>valid_en_desc<sm>100<sm>1<sm>0<sm>100<sm>15<sm>15<sm>115",
                "100<sm>0<sm>15<sm>115",
                "TRN123<sm>IBAN123"
        );
        
        // Should not throw exception, but skip malformed items
        assertDoesNotThrow(() -> InvoiceParser.parse(inputWithMalformedItems));
        com.temenos.t24.ksa.pdf.model.InvoiceData result = InvoiceParser.parse(inputWithMalformedItems);
        assertEquals(1, result.lineItems.size()); // Only valid item should be parsed
    }
}