package com.temenos.t24.ksa.pdf;

import com.temenos.t24.ksa.pdf.model.InvoiceData;
import com.temenos.t24.ksa.pdf.model.InvoiceParser;
import com.temenos.t24.ksa.pdf.qr.TLVUtils;
import com.temenos.t24.ksa.pdf.qr.ZatcaQRData;
import com.temenos.t24.ksa.pdf.util.PdfTableFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.awt.color.ICC_Profile;
import java.awt.color.ColorSpace;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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

    public static String createPDF(String args) {
        if (args == null || args.trim().isEmpty()) {
            throw new IllegalArgumentException("Input arguments cannot be null or empty");
        }
        
        // Convert the raw string to your data model
        // TODO remove this print statement
        System.out.println("Received args: " + args);
        InvoiceData data = InvoiceParser.parse(args);
        return createPDF(data);
    }

    public static String createPDF(InvoiceData data) {
        if (data == null) {
            throw new IllegalArgumentException("InvoiceData cannot be null");
        }

        // TODO remove this print statement
        System.out.println("inputed data" + data);

        String path = validateAndSanitizePath(data.path, "Output PDF path");
        String arabicFontPath = validateAndSanitizePath(data.arabicFontPath, "Arabic font path");
        String logoPath = validateAndSanitizePath(data.logoPath, "Logo path");
        String footerPath = validateAndSanitizePath(data.footerPath, "Footer path");

        ICC_Profile profile = ICC_Profile.getInstance(ColorSpace.CS_sRGB);
        try (InputStream icc = new ByteArrayInputStream(profile.getData())) {

            // Basic diagnostics (you said to keep logs until server rollout)
            System.out.println("===================================================");
            File[] filesToCheck = { new File(logoPath), new File(footerPath), new File(arabicFontPath) };
            for (File f : filesToCheck) {
                System.out.println("File path = " + f.getAbsolutePath());
                System.out.println("File exists? " + f.exists());
                System.out.println("File readable? " + f.canRead());
                System.out.println("---------------------------------------------------");
            }
            System.out.println("Using built-in sRGB ICC profile");
            System.out.println("===================================================");


            PdfFont pdfFont = PdfFontFactory.createFont(arabicFontPath, PdfEncodings.IDENTITY_H);

            // ---- PDF/A setup (one document only) ----
            PdfOutputIntent oi = new PdfOutputIntent(
                    "sRGB IEC61966-2.1", "", "http://www.color.org", "sRGB IEC61966-2.1", icc);

            PdfWriter writer = new PdfWriter(path);
            PdfADocument pdf = new PdfADocument(writer, PdfAConformanceLevel.PDF_A_3B, oi);
            Document document = new Document(pdf);
            document.setFont(pdfFont);

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

            document.close();
            System.out.println("doc. creation is done");

        } catch (SecurityException e) {
            System.err.println("Security error: " + e.getMessage());
            throw e; // Re-throw security exceptions
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid argument: " + e.getMessage());
            throw e; // Re-throw argument validation exceptions
        } catch (java.io.IOException e) {
            System.err.println("File I/O error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create PDF due to file I/O error", e);
        } catch (Exception e) {
            System.err.println("Unexpected error during PDF creation: " + e.getMessage());
            e.printStackTrace();
            Throwable cause = e.getCause();
            if (cause != null) {
                System.err.println("Root cause: " + cause.getMessage());
            }
            throw new RuntimeException("Failed to create PDF", e);
        }

        return path;
    }

    // Convert "dd MMM yyyy HH:mm:ss" to "yyyy-MM-ddTHH:mm:ssZ"
    private static String convertDateTimeToIso(String invoiceDateTime) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = inputFormat.parse(invoiceDateTime);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return invoiceDateTime;
    }

    /**
     * Validates and sanitizes file paths to prevent path traversal attacks and ensure file accessibility.
     */
    private static String validateAndSanitizePath(String filePath, String pathDescription) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException(pathDescription + " cannot be null or empty");
        }
        
        String normalizedPath = filePath.trim();
        
        // Check for path traversal attempts
        if (normalizedPath.contains("..") || normalizedPath.contains("//")) {
            throw new SecurityException("Path traversal detected in " + pathDescription + ": " + normalizedPath);
        }
        
        // Validate file exists and is readable (except for output path)
        if (!pathDescription.toLowerCase().contains("output")) {
            File file = new File(normalizedPath);
            if (!file.exists()) {
                throw new IllegalArgumentException(pathDescription + " does not exist: " + normalizedPath);
            }
            if (!file.canRead()) {
                throw new SecurityException(pathDescription + " is not readable: " + normalizedPath);
            }
        }
        
        return normalizedPath;
    }

    static class PdfEventHandler implements IEventHandler {
        @Override
        public void handleEvent(Event event) {
            // currently empty (kept for parity with previous behavior)
        }
    }
}