package com.temenos.t24.ksa.pdf;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        System.out.println("T24 PDF Creator Test Application");
        System.out.println("=================================");

        try {

            String dummyData = createDummyData();

            com.temenos.t24.ksa.pdf.PDFcreator pdfCreator = new PDFcreator();

            System.out.println("Generating PDF with dummy data...");
            String pdfPath = pdfCreator.createPDF(dummyData);

            System.out.println("PDF generated successfully!");
            System.out.println("PDF location: " + pdfPath);

            File pdfFile = new File(pdfPath);
            if (pdfFile.exists()) {
                System.out.println("File size: " + pdfFile.length() + " bytes");
            } else {
                System.out.println("Warning: PDF file was not created!");
            }

            // Also generate XML aligned with ZATCA UBL
            System.out.println("Generating XML with dummy data...");
            String xmlPath = XMLcreator.createXML(dummyData);
            System.out.println("XML generated successfully!");
            System.out.println("XML location: " + xmlPath);

        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String createDummyData() {

        String path = "test-output/tax_invoice_test.new.pdf";

        String billDetails = "INV2025000008817467<sm>03 SEP 2025 12:25:56<sm>20240801<sm>20241001";

        String customerDetails = "TAX2000141<sm><sm>Mohammad Najar<sm1>محمد النجار<sm>Baghdad Test<sm1>بغداد العرصاات<sm>Baghdad<sm1>بغداد<sm>JORDAN<sm1>العراق";

        String taxDetails = "20240403<sm>ELCK20240005 رسوم تبليغ<sm1>EMPTY<sm>1000<sm>1<sm><sm>1000<sm>15<sm>150<sm>1150<vm>20240403<sm>ELCK20240005 البريد<sm1>EMPTY<sm>21<sm>1<sm><sm>21<sm>15<sm>3.15<sm>24.15<vm>20240403<sm>ELCK20240005 رسوم سويفت<sm1>EMPTY<sm>150<sm>1<sm><sm>150<sm>15<sm>22.5<sm>172.5<vm>20240104<sm>ELCK20240005 رسوم البريد السريع<sm1>EMPTY<sm>150<sm>1<sm><sm>150<sm>15<sm>22.5<sm>172.5<vm>20240104<sm>ELCK20240005 عمولة تدقيق<sm1>EMPTY<sm>2812.5<sm>1<sm><sm>2812.5<sm>15<sm>421.88<sm>3234.38<vm>20240104<sm>ELCK20240005 رسوم سويفت<sm1>EMPTY<sm>150<sm>1<sm><sm>150<sm>15<sm>22.5<sm>172.5<vm>20240317<sm>ELCK20240008 رسوم تبليغ<sm1>EMPTY<sm>1000<sm>1<sm><sm>1000<sm>15<sm>150<sm>1150<vm>20240317<sm>ELCK20240008 رسوم سويفت<sm1>EMPTY<sm>150<sm>1<sm><sm>150<sm>15<sm>22.5<sm>172.5";
        String totalDetails = "5433.5<sm>0<sm>815.03<sm>6248.53";
        String ibanDetails = "311057847300003<sm>SA9701100001232001079000";
        String fontPath = "/Users/mmj/IdeaProjects/PdfcreateTest/test-resources/fonts/NotoNaskhArabic-Regular.ttf"; // Use system font
        String logoPath = "/Users/mmj/IdeaProjects/PdfcreateTest/test-resources/images/logo.png";
        String footerPath = "/Users/mmj/IdeaProjects/PdfcreateTest/test-resources/images/footer.png";


        return path + "<fm>" + billDetails + "<fm>" + customerDetails + "<fm>" + taxDetails + "<fm>" + totalDetails + "<fm>" + ibanDetails + "<fm>" + fontPath + "<fm>" + logoPath + "<fm>" + footerPath;
    }
}
