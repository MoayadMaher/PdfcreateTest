package com.temenos.t24.ksa.pdf;

import com.temenos.t24.ksa.pdf.util.ExceptionUtils;
import com.temenos.t24.ksa.pdf.util.SecurityUtils;
import java.io.File;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    
    public static void main(String[] args) {
        LOGGER.info("T24 PDF Creator Test Application");
        LOGGER.info("=================================");
        
        try {
            String dummyData = createDummyData();

            com.temenos.t24.ksa.pdf.PDFcreator pdfCreator = new PDFcreator();

            LOGGER.info("Generating PDF with dummy data...");
            String pdfPath = pdfCreator.createPDF(dummyData);
            
            LOGGER.info("PDF generated successfully!");
            LOGGER.info("PDF location: " + SecurityUtils.sanitizeInput(pdfPath));

            File pdfFile = new File(pdfPath);
            if (pdfFile.exists()) {
                LOGGER.info("File size: " + pdfFile.length() + " bytes");
            } else {
                LOGGER.warning("Warning: PDF file was not created!");
            }
            
        } catch (Exception e) {
            String errorMsg = ExceptionUtils.logSecurely("PDF creation application", e);
            System.err.println("Error: " + errorMsg);
            System.exit(1);
        }
    }
    
    private static String createDummyData() {
        // Use secure, relative paths within allowed directories
        String path = "test-output/tax_invoice_test.new.pdf";

        String billDetails = "INV2025000008817467<sm>03 SEP 2025 12:25:56<sm>20240801<sm>20241001";

        String customerDetails = "TAX2000141<sm><sm>Mohammad Najar<sm1>محمد النجار<sm>Baghdad Test<sm1>بغداد العرصاات<sm>Baghdad<sm1>بغداد<sm>JORDAN<sm1>العراق";

        String taxDetails = "20240403<sm>ELCK20240005 رسوم تبليغ<sm1>EMPTY<sm>1000<sm>1<sm><sm>1000<sm>15<sm>150<sm>1150<vm>20240403<sm>ELCK20240005 البريد<sm1>EMPTY<sm>21<sm>1<sm><sm>21<sm>15<sm>3.15<sm>24.15<vm>20240403<sm>ELCK20240005 رسوم سويفت<sm1>EMPTY<sm>150<sm>1<sm><sm>150<sm>15<sm>22.5<sm>172.5<vm>20240104<sm>ELCK20240005 رسوم البريد السريع<sm1>EMPTY<sm>150<sm>1<sm><sm>150<sm>15<sm>22.5<sm>172.5<vm>20240104<sm>ELCK20240005 عمولة تدقيق<sm1>EMPTY<sm>2812.5<sm>1<sm><sm>2812.5<sm>15<sm>421.88<sm>3234.38<vm>20240104<sm>ELCK20240005 رسوم سويفت<sm1>EMPTY<sm>150<sm>1<sm><sm>150<sm>15<sm>22.5<sm>172.5<vm>20240317<sm>ELCK20240008 رسوم تبليغ<sm1>EMPTY<sm>1000<sm>1<sm><sm>1000<sm>15<sm>150<sm>1150<vm>20240317<sm>ELCK20240008 رسوم سويفت<sm1>EMPTY<sm>150<sm>1<sm><sm>150<sm>15<sm>22.5<sm>172.5";
        String totalDetails = "5433.5<sm>0<sm>815.03<sm>6248.53";
        String ibanDetails = "311057847300003<sm>SA9701100001232001079000";
        
        // Use relative paths within the project structure
        String fontPath = "test-resources/fonts/NotoNaskhArabic-Regular.ttf";
        String logoPath = "test-resources/images/logo.png";
        String footerPath = "test-resources/images/footer.png";

        // Validate all paths before returning
        if (!SecurityUtils.isPathSafe(path)) {
            throw new SecurityException("Output path is not safe");
        }
        if (!SecurityUtils.isFileAccessible(fontPath)) {
            LOGGER.warning("Font file not accessible, using default: " + fontPath);
            fontPath = ""; // Use system default
        }
        if (!SecurityUtils.isFileAccessible(logoPath)) {
            LOGGER.warning("Logo file not accessible: " + logoPath);
            logoPath = ""; // Skip logo
        }
        if (!SecurityUtils.isFileAccessible(footerPath)) {
            LOGGER.warning("Footer file not accessible: " + footerPath);
            footerPath = ""; // Skip footer
        }
        
        return path + "<fm>" + billDetails + "<fm>" + customerDetails + "<fm>" + taxDetails + "<fm>" + totalDetails + "<fm>" + ibanDetails + "<fm>" + fontPath + "<fm>" + logoPath + "<fm>" + footerPath;
    }
}