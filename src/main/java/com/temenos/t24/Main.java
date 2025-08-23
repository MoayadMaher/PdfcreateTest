package com.temenos.t24;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        System.out.println("T24 PDF Creator Test Application");
        System.out.println("=================================");
        
        try {
            // Create test directories if they don't exist
            createTestDirectories();
            
            // Create dummy data for testing
            String dummyData = createDummyData();
            
            // Initialize PdfCreator
            PdfCreator pdfCreator = new PdfCreator();
            
            // Generate PDF
            System.out.println("Generating PDF with dummy data...");
            String pdfPath = pdfCreator.createPdf(dummyData);
            
            System.out.println("PDF generated successfully!");
            System.out.println("PDF location: " + pdfPath);
            
            // Check if file exists
            File pdfFile = new File(pdfPath);
            if (pdfFile.exists()) {
                System.out.println("File size: " + pdfFile.length() + " bytes");
            } else {
                System.out.println("Warning: PDF file was not created!");
            }
            
        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void createTestDirectories() throws Exception {
        // Create test directories
        String[] dirs = {
            "test-output",
            "test-resources",
            "test-resources/fonts",
            "test-resources/images"
        };
        
        for (String dir : dirs) {
            Files.createDirectories(Paths.get(dir));
        }
        
        System.out.println("Test directories created successfully");
    }
    
    private static String createDummyData() {
        // Format: path<fm>billDetails<fm>customerDetails<fm>taxDetails<fm>totalDetails<fm>ibanDetails<fm>fontPath<fm>logoPath<fm>footerPath
        
        String path = "test-output/tax_invoice_test.pdf";
        
        // Bill Details: Invoice Number, Invoice Date, Service Start Date, Service End Date
        String billDetails = "INV-2024-001<sm>20241201<sm>20241101<sm>20241130";
        
        // Customer Details: VAT Number, Customer ID, Name (English/Arabic), Address (English/Arabic), City (English/Arabic), Country (English/Arabic)
        String customerDetails = "123456789<sm>CUST001<sm>ABC Company Ltd<sm1>شركة أي بي سي المحدودة<sm>123 Business Street, Dubai<sm1>شارع الأعمال 123، دبي<sm>Dubai<sm1>دبي<sm>UAE<sm1>الإمارات العربية المتحدة";
        
        // Tax Details: Multiple items with description, unit price, quantity, discount, total, tax rate, tax amount, total price
        // Format: SupplyDate<sm1>Description<sm2>ArabicDescription<sm1>UnitPrice<sm>Quantity<sm>Discount<sm>TotalExcludingTax<sm>TaxRate<sm>TaxAmount<sm>TotalPrice
        // Each item is separated by <sm> at the end
        String taxItem1 = "20241101<sm1>Internet Service<sm2>خدمة الإنترنت<sm1>100.00<sm>1<sm>0.00<sm>100.00<sm>5.00<sm>5.00<sm>105.00";
        String taxItem2 = "20241101<sm1>Phone Service<sm2>خدمة الهاتف<sm1>50.00<sm>1<sm>0.00<sm>50.00<sm>5.00<sm>2.50<sm>52.50";
        String taxDetails = taxItem1 + "<sm>" + taxItem2;
        
        // Total Details: Total excluding VAT, Total Discount, Total VAT, Total including VAT
        String totalDetails = "150.00<sm>0.00<sm>7.50<sm>157.50";
        
        // IBAN Details: VAT Registration Number, IBAN Number
        String ibanDetails = "VAT123456789<sm>AE123456789012345678901";
        
        // Resource paths
        String fontPath = "/System/Library/Fonts/Arial Unicode.ttf"; // Use system font
        String logoPath = "test-resources/images/logo.png";
        String footerPath = "test-resources/images/footer.png";
        
        // Create dummy image files if they don't exist
        createDummyImageFile(logoPath);
        createDummyImageFile(footerPath);
        
        return path + "<fm>" + billDetails + "<fm>" + customerDetails + "<fm>" + taxDetails + "<fm>" + totalDetails + "<fm>" + ibanDetails + "<fm>" + fontPath + "<fm>" + logoPath + "<fm>" + footerPath;
    }
    

    
    private static void createDummyImageFile(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                // Create a simple dummy image file (1x1 pixel PNG)
                byte[] dummyPng = {
                    (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47, (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A,
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0D, (byte) 0x49, (byte) 0x48, (byte) 0x44, (byte) 0x52,
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
                    (byte) 0x08, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x90, (byte) 0x77, (byte) 0x53,
                    (byte) 0xDE, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0C, (byte) 0x49, (byte) 0x44, (byte) 0x41,
                    (byte) 0x54, (byte) 0x08, (byte) 0x99, (byte) 0x63, (byte) 0xF8, (byte) 0xCF, (byte) 0xCF, (byte) 0x00,
                    (byte) 0x00, (byte) 0x03, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x18, (byte) 0x18, (byte) 0x00,
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x49, (byte) 0x45, (byte) 0x4E, (byte) 0x44, (byte) 0xAE,
                    (byte) 0x42, (byte) 0x60, (byte) 0x82
                };
                Files.write(Paths.get(imagePath), dummyPng);
                System.out.println("Created dummy image file: " + imagePath);
            }
        } catch (Exception e) {
            System.out.println("Warning: Could not create dummy image file: " + e.getMessage());
        }
    }
}