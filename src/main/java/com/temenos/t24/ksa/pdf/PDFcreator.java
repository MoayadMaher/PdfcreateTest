package com.temenos.t24.ksa.pdf;

import com.temenos.t24.ksa.pdf.model.InvoiceData;
import com.temenos.t24.ksa.pdf.model.InvoiceParser;
import com.temenos.t24.ksa.pdf.qr.TLVUtils;
import com.temenos.t24.ksa.pdf.qr.ZatcaQRData;
import com.temenos.t24.ksa.pdf.util.PdfTableFactory;
import com.temenos.t24.ksa.pdf.util.SecurityUtils;
import com.temenos.t24.ksa.pdf.util.ExceptionUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.awt.color.ICC_Profile;
import java.awt.color.ColorSpace;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfAConformanceLevel;   // correct package
import com.itextpdf.kernel.pdf.PdfOutputIntent;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.pdfa.PdfADocument;

public class PDFcreator {
    private static final Logger LOGGER = Logger.getLogger(PDFcreator.class.getName());

    public static String createPDF(String args) {
        try {
            // Input validation
            if (args == null || args.trim().isEmpty()) {
                throw new IllegalArgumentException("PDF creation arguments cannot be null or empty");
            }
            
            LOGGER.info("Starting PDF creation with provided arguments");
            InvoiceData data = InvoiceParser.parse(args);
            return createPDF(data);
        } catch (IllegalArgumentException e) {
            String errorMsg = ExceptionUtils.handleParsingException("PDF arguments", e);
            LOGGER.severe(errorMsg);
            throw new RuntimeException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = ExceptionUtils.logSecurely("PDF creation from string arguments", e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    public static String createPDF(InvoiceData data) {
        // Input validation
        if (data == null) {
            throw new IllegalArgumentException("Invoice data cannot be null");
        }

        LOGGER.info("Starting PDF creation with parsed invoice data");

        String path = data.path;
        String arabicFontPath = data.arabicFontPath;
        String logoPath = data.logoPath;
        String footerPath = data.footerPath;
        
        // Validate file paths for security
        if (!SecurityUtils.isPathSafe(path)) {
            throw new SecurityException("Output path is not safe: " + SecurityUtils.sanitizeInput(path));
        }
        
        if (arabicFontPath != null && !SecurityUtils.isFileAccessible(arabicFontPath)) {
            throw new IllegalArgumentException("Arabic font file is not accessible: " + SecurityUtils.sanitizeInput(arabicFontPath));
        }
        
        if (logoPath != null && !SecurityUtils.isFileAccessible(logoPath)) {
            throw new IllegalArgumentException("Logo file is not accessible: " + SecurityUtils.sanitizeInput(logoPath));
        }
        
        if (footerPath != null && !SecurityUtils.isFileAccessible(footerPath)) {
            throw new IllegalArgumentException("Footer file is not accessible: " + SecurityUtils.sanitizeInput(footerPath));
        }

        try {
            ICC_Profile profile = ICC_Profile.getInstance(ColorSpace.CS_sRGB);
            try (InputStream icc = new ByteArrayInputStream(profile.getData())) {

                // Log file validation (but don't expose full paths in production)
                LOGGER.info("Validating required files for PDF generation");
                File[] filesToCheck = { new File(logoPath), new File(footerPath), new File(arabicFontPath) };
                for (File f : filesToCheck) {
                    if (!f.exists()) {
                        throw new java.io.FileNotFoundException("Required file not found: " + f.getName());
                    }
                    if (!f.canRead()) {
                        throw new java.io.IOException("Cannot read required file: " + f.getName());
                    }
                }
                LOGGER.info("All required files validated successfully");

                PdfFont pdfFont;
                try {
                    pdfFont = PdfFontFactory.createFont(arabicFontPath, PdfEncodings.IDENTITY_H);
                } catch (Exception e) {
                    throw new RuntimeException(ExceptionUtils.handleFileException(arabicFontPath, e), e);
                }

                // ---- PDF/A setup (one document only) ----
                PdfOutputIntent oi = new PdfOutputIntent(
                        "sRGB IEC61966-2.1", "", "http://www.color.org", "sRGB IEC61966-2.1", icc);

                PdfWriter writer;
                PdfADocument pdf;
                Document document;
                
                try {
                    writer = new PdfWriter(path);
                    pdf = new PdfADocument(writer, PdfAConformanceLevel.PDF_A_3B, oi);
                    document = new Document(pdf);
                    document.setFont(pdfFont);
                } catch (Exception e) {
                    throw new RuntimeException(ExceptionUtils.handlePdfException("PDF document initialization", e), e);
                }

            // ---- Build ZATCA QR (Phase I TLV) and add to PDF ----
            ZatcaQRData qrData = new ZatcaQRData();
            qrData.sellerName = "National Bank of Iraq";
            qrData.vatNumber  = data.iban.taxRegistrationNumber;          // seller’s VAT registration
            qrData.timestamp  = convertDateTimeToIso(data.header.invoiceDateTime);
            qrData.invoiceTotalWithVat = data.totals.amountIncludesVat;
            qrData.vatTotal = data.totals.totalVat;

            String qrContent = TLVUtils.generateBase64TLV(qrData);
            BarcodeQRCode qrCode = new BarcodeQRCode(qrContent);


            PdfFormXObject qrObject = qrCode.createFormXObject(pdf);
            Image qrImage = new Image(qrObject)
                    .setWidth(100)
                    .setHeight(100)
                    .setFixedPosition(1, 36, 730);
            document.add(qrImage);

            System.out.println("tables creation started");

            Table header = PdfTableFactory.createHeaderTable(pdfFont, logoPath);
            Table infoHeader = PdfTableFactory.createInfoHeaderTable(data, pdfFont);
            Table summary = PdfTableFactory.createInvoiceSummaryTable(data, pdfFont);
            Table customer = PdfTableFactory.createCustomerDetailsTable(data, pdfFont);
            Table lineItems = PdfTableFactory.createLineItemsTable(data, pdfFont);
            Table totals = PdfTableFactory.createTotalsTable(data, pdfFont);

            System.out.println("tables creation done");

            // Add tables
            document.add(header);
            document.add(qrImage);
            document.add(infoHeader);
            document.add(summary);
            document.add(customer);
            document.add(lineItems);
            document.add(totals);

                // Footer & event handling must also use the same pdf
                try {
                    PdfEventHandler eventHandler = new PdfEventHandler();
                    pdf.addEventHandler(PdfDocumentEvent.END_PAGE, eventHandler);

                    Rectangle pageSize = pdf.getLastPage().getPageSize();
                    float x = pageSize.getLeft() + 36;
                    float y = pageSize.getBottom() - 20;

                    ImageData dataFooter = ImageDataFactory.create(footerPath);
                    Image imageFooter = new Image(dataFooter);
                    imageFooter.setMarginTop(20F);
                    imageFooter.setFixedPosition(pdf.getPageNumber(pdf.getLastPage()), x, y);
                    imageFooter.setAutoScale(true);
                    imageFooter.setHorizontalAlignment(HorizontalAlignment.CENTER);
                    document.add(imageFooter);
                } catch (Exception e) {
                    throw new RuntimeException(ExceptionUtils.handlePdfException("footer creation", e), e);
                }

                document.close();
                LOGGER.info("PDF document creation completed successfully");
                
            } catch (java.io.IOException e) {
                throw new RuntimeException(ExceptionUtils.handleFileException("ICC profile", e), e);
            }
        } catch (Exception e) {
            String errorMsg = ExceptionUtils.handlePdfException("PDF creation", e);
            LOGGER.severe(errorMsg);
            throw new RuntimeException(errorMsg, e);
        }

        return path;
    }

    // Convert "dd MMM yyyy HH:mm:ss" to "yyyy-MM-ddTHH:mm:ssZ"
    private static String convertDateTimeToIso(String invoiceDateTime) {
        if (invoiceDateTime == null || invoiceDateTime.trim().isEmpty()) {
            return "";
        }
        
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = inputFormat.parse(invoiceDateTime);
            return outputFormat.format(date);
        } catch (ParseException e) {
            String errorMsg = ExceptionUtils.handleParsingException("date time conversion", e);
            LOGGER.warning(errorMsg);
            return invoiceDateTime; // Return original if parsing fails
        }
    }

    static class PdfEventHandler implements IEventHandler {
        @Override
        public void handleEvent(Event event) {
            // currently empty (kept for parity with previous behavior)
        }
    }
}