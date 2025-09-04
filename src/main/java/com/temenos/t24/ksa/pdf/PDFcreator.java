package com.temenos.t24.ksa.pdf;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.*;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.*;
import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;

import com.ibm.icu.text.Bidi;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.temenos.t24.ksa.pdf.model.InvoiceData;
import com.temenos.t24.ksa.pdf.model.InvoiceLineItem;
import com.temenos.t24.ksa.pdf.model.InvoiceParser;
import com.temenos.t24.ksa.pdf.util.PdfTableFactory;

import java.io.File;


public class PDFcreator {

    public static String createPDF(String args) {
        // Convert the raw string to your data model
        // TODO remove this print statement
        System.out.println("Received args: " + args);
        InvoiceData data = InvoiceParser.parse(args);
        return createPDF(data);
    }

    public static String createPDF(InvoiceData data) {

        // TODO remove this print statement
        System.out.println("inputed data" + data);

        String path = data.path;
        String arabicFontPath = data.arabicFontPath;
        String logoPath = data.logoPath;
        String footerPath = data.footerPath;


        try {

            // TODO remove this for loop check this just to check the availability
            System.out.println("===================================================");
            File[] filesToCheck = {new File(logoPath), new File(footerPath), new File(arabicFontPath)};
            for (int i = 0; i < 3; i++) {
                System.out.println("File path = " + filesToCheck[i].getAbsolutePath());
                System.out.println("File exists? " + filesToCheck[i].exists());
                System.out.println("File readable? " + filesToCheck[i].canRead());
                System.out.println("===================================================");
            }

            PdfFont pdfFont = PdfFontFactory.createFont(
                    arabicFontPath,
                    PdfEncodings.IDENTITY_H
            );

            PdfWriter pdfWritter = new PdfWriter(path);
            PdfDocument pdfDocument = new PdfDocument(pdfWritter);
            Document document = new Document(pdfDocument);


            String qrContent = "==wMA4MC1jEVQxNTozMDowMFoEBzEwMDAuMDAFByN0wNC0MzUwMDAwMwMUMjAyMi5yZHMCDzMxMDEyMjM29JzIFJlY2AQxC";
            BarcodeQRCode qrCode = new BarcodeQRCode(qrContent);
            PdfFormXObject qrObject = qrCode.createFormXObject(pdfDocument);
            Image qrImage = new Image(qrObject);
            qrImage.setWidth(100);
            qrImage.setHeight(100);
            qrImage.setFixedPosition(1, 36, 730);
            document.add(qrImage);

            System.out.println("tables creation started");

            Table header      = PdfTableFactory.createHeaderTable(pdfFont, logoPath);
            Table infoHeader  = PdfTableFactory.createInfoHeaderTable(data, pdfFont);
            Table summary     = PdfTableFactory.createInvoiceSummaryTable(data, pdfFont);
            Table customer    = PdfTableFactory.createCustomerDetailsTable(data, pdfFont);
            Table lineItems   = PdfTableFactory.createLineItemsTable(data, pdfFont);
            Table totals      = PdfTableFactory.createTotalsTable(data, pdfFont);

            System.out.println("tables creation done");

            // then add them to the document
            document.add(header);
            document.add(qrImage);
            document.add(infoHeader);
            document.add(summary);
            document.add(customer);
            document.add(lineItems);
            document.add(totals);

            PdfEventHandler eventHandler = new PdfEventHandler();
            pdfDocument.addEventHandler(PdfDocumentEvent.END_PAGE, eventHandler);
            Rectangle pageSize = pdfDocument.getLastPage().getPageSize();
            float x = pageSize.getLeft() + 36;
            float y = pageSize.getBottom() - 20;
            String imgFooter = footerPath;
            ImageData dataFooter = ImageDataFactory.create(imgFooter);
            Image imageFooter = new Image(dataFooter);
            imageFooter.setMarginTop(20F);
            imageFooter.setFixedPosition(pdfDocument.getPageNumber(pdfDocument.getLastPage()), x, y);
            imageFooter.setAutoScale(true);
            imageFooter.setHorizontalAlignment(HorizontalAlignment.CENTER);
            document.add(imageFooter);
            document.close();

            System.out.println("doc. creation is done");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception occured " + e.getMessage());
            Throwable cause = e.getCause();
            if (cause != null) {
                System.err.println("Root cause: " + cause);
                cause.printStackTrace();
            }
        }

        return path;
    }

    static class PdfEventHandler implements IEventHandler {
        @Override
        public void handleEvent(Event event) {

        }
    }

}
