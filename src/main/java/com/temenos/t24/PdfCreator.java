package com.temenos.t24;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.BaseDirection;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;

public class PdfCreator {
    
    public String createPdf(String args) {
        String[] argsArray = args.split("<fm>");
        System.out.println("Input String: " + args);
        String path = argsArray[0];
        String arabicFontPath = argsArray[6];
        String logoPath = argsArray[7];
        String footerPath = argsArray[8];

        System.out.println("Font path is " + arabicFontPath);
        System.out.println("PDF File Path is " + path);

        String[] yTaxBillDetailsArr = argsArray[1].split("<sm>");
        String[] yCustomerDetailsArr = argsArray[2].split("<sm>");
        List<String> yTaxDetailsArrList = Arrays.asList(argsArray[3].split("<sm>"));
        String[] yTaxTotalDetailsArr = argsArray[4].split("<sm>");
        String[] yTaxIbanDetArr = argsArray[5].split("<sm>");

        System.out.println("yTaxBillDetailsArr  value is " + yTaxBillDetailsArr);
        System.out.println("yCustomerDetailsArr  value is " + yCustomerDetailsArr);
        System.out.println("yTaxDetailsArrList  value is " + yTaxDetailsArrList);
        System.out.println("yTaxTotalDetailsArr  value is " + yTaxTotalDetailsArr);
        System.out.println("yTaxIbanDetArr  value is " + yTaxIbanDetArr);

        try {
            PdfFont pdfFont;
            try {
                pdfFont = PdfFontFactory.createFont(arabicFontPath, PdfEncodings.IDENTITY_H);
            } catch (Exception e) {
                System.out.println("Warning: Could not load specified font: " + arabicFontPath);
                System.out.println("Using system default font instead.");
                pdfFont = PdfFontFactory.createFont();
            }

            PdfWriter pdfWriter = new PdfWriter(path);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            Document document = new Document(pdfDocument);
            
            // Simple QR code using iText barcodes
            String qrContent = "https://example.com";
            try {
                BarcodeQRCode qrCode = new BarcodeQRCode(qrContent);
                PdfFormXObject qrObject = qrCode.createFormXObject(pdfDocument);
                Image qrImage = new Image(qrObject);
                qrImage.setWidth(100);
                qrImage.setHeight(100);
                qrImage.setFixedPosition(1, 36, 750); // top-left
                document.add(qrImage);
                System.out.println("QR code added successfully to PDF");
            } catch (Exception e) {
                System.out.println("Warning: Could not add QR code: " + e.getMessage());
            }
            
            float[] pointColumWidth5 = {380F,220F};
            Table table6 = new Table(pointColumWidth5);
            String imgArab = logoPath;
            Image imgArabic = null;
            try {
                ImageData dataArabic = ImageDataFactory.create(imgArab);
                imgArabic = new Image(dataArabic);
                imgArabic.scaleToFit(150, 150).setHorizontalAlignment(HorizontalAlignment.RIGHT);
            } catch (Exception e) {
                System.out.println("Warning: Could not load logo image: " + imgArab);
                System.out.println("Continuing without logo image.");
            }

            table6.addCell(new Cell().add(new Paragraph("المصرف الآهلي العراقي"))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setFontSize(12.5F)
                    .setBorder(Border.NO_BORDER)
                    .setFont(pdfFont)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));

            if (imgArabic != null) {
                table6.addCell(new Cell().add(imgArabic)
                        .setBorder(Border.NO_BORDER)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE));
            } else {
                table6.addCell(new Cell().add(new Paragraph("Logo"))
                        .setBorder(Border.NO_BORDER)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE));
            }



            float[] pointColumWidth = {175F, 100F};
            Table table1 = new Table(pointColumWidth);
            table1.setWidth(30F);
            table1.setHorizontalAlignment(HorizontalAlignment.CENTER);
            if (!yTaxIbanDetArr[0].equals("EMPTY")){
                table1.addCell(new Cell().add(new Paragraph(yTaxIbanDetArr[0])).setFontSize(8F)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setBorder(Border.NO_BORDER)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE));
            } else {
                table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
            }
            table1.addCell(new Cell().add(new Paragraph(": رقم تسحيل ضريبة القيمة المضافة"))
                    .setFont(pdfFont)
                    .setFontSize(8F)
                    .setCharacterSpacing(0)
                    .setPadding(0)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBorder(Border.NO_BORDER)
                    .setMarginBottom(0));

            if (!yTaxIbanDetArr[1].equals("EMPTY")){
                table1.addCell(new Cell().add(new Paragraph(yTaxIbanDetArr[1])).setFontSize(8F)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setBorder(Border.NO_BORDER)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE));
            } else {
                table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
            }
            table1.addCell(new Cell().add(new Paragraph(": رقم الآيبان"))
                    .setFont(pdfFont)
                    .setCharacterSpacing(0)
                    .setFontSize(8F)
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0F)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(0));

            System.out.println("Table 1 is created");

            // Table 2 Start
            float[] pointColumWidth1 = {90F, 100F, 90F, 90F, 150F, 90F};
            Table table2 = new Table(pointColumWidth1);
            table2.setWidth(10F);
            table2.addCell(new Cell(0, 6).add(new Paragraph("فاتوة ضريبة"))
                    .setFont(pdfFont)
                    .setFontSize(15F)
                    .setBold()
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(0F)
                    .setBackgroundColor(new DeviceRgb(182, 248,252)));
            table2.addCell(new Cell().add(new Paragraph("Invoice Number")).setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));
            table2.addCell(new Cell().add(new Paragraph(yTaxBillDetailsArr[0])).setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));
            table2.addCell(new Cell().add(new Paragraph(": رقم الفاتورة"))
                    .setFontSize(10F)
                    .setFont(pdfFont)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.RIGHT));
            table2.addCell(new Cell().add(new Paragraph("Invoice Date")).setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));
            table2.addCell(new Cell().add(new Paragraph(yTaxBillDetailsArr[1])).setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));
            table2.addCell(new Cell().add(new Paragraph(": تاريخ الفاتورة"))
                    .setFontSize(10F)
                    .setFont(pdfFont)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.RIGHT));
            table2.addCell(new Cell().add(new Paragraph("تاريخ الفاتورة"))
                    .setFontSize(8F)
                    .setFont(pdfFont)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.RIGHT));
            table2.addCell(new Cell().add(new Paragraph("Service Start Date")).setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));
            table2.addCell(new Cell().add(new Paragraph(yTaxBillDetailsArr[2])).setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));
            table2.addCell(new Cell().add(new Paragraph(": تاريخ بداية الخدمة"))
                    .setFontSize(10F)
                    .setFont(pdfFont)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.RIGHT));
            table2.addCell(new Cell().add(new Paragraph("Service End Date")).setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));
            table2.addCell(new Cell().add(new Paragraph(yTaxBillDetailsArr[3])).setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));
            table2.addCell(new Cell().add(new Paragraph(": تاريخ نهاية الخدمة"))
                    .setFontSize(10F)
                    .setFont(pdfFont)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.RIGHT));
            System.out.println("Table 2 is created");

            // Table 3 Start
            float[] pointColumWidth2 = {50F, 200F, 100F, 50F, 200F, 100F};
            Table table3 = new Table(pointColumWidth2);
            table3.setWidth(10F);
            table3.addCell(new Cell(0,3).add(new Paragraph("Customer Details"))
                    .setFontSize(10F)
                    .setFont(pdfFont)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setBackgroundColor(new DeviceRgb(182, 248,252))
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(Border.NO_BORDER));
            table3.addCell(new Cell().add(new Paragraph("بيانات العميل"))
                    .setFontSize(10F)
                    .setFont(pdfFont)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBackgroundColor(new DeviceRgb(182, 248,252))
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorderLeft(Border.NO_BORDER));

            String [] yCustomerNameArr = yCustomerDetailsArr[2].split("<sm1>");
            String [] yCustomerAddArr = yCustomerDetailsArr[3].split("<sm1>");

            table3.addCell(new Cell(2,0).add(new Paragraph("Name")).setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));

            if(!yCustomerNameArr[0].equals("EMPTY")) {
                table3.addCell(new Cell().add(new Paragraph(yCustomerNameArr[0])).setFontSize(7F)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setBorder(Border.NO_BORDER)
                        .setTextAlignment(TextAlignment.CENTER));
            } else {
                table3.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
            }

            table3.addCell(new Cell(2,0).add(new Paragraph("الاسم"))
                    .setFontSize(8F)
                    .setFont(pdfFont)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));

            table3.addCell(new Cell(2,0).add(new Paragraph("Address"))
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));
            if(!yCustomerAddArr[0].equals("EMPTY")) {
                table3.addCell(new Cell().add(new Paragraph(yCustomerAddArr[0])).setFontSize(8F)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setBorder(Border.NO_BORDER)
                        .setTextAlignment(TextAlignment.CENTER));
            } else {
                table3.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
            }
            table3.addCell(new Cell(2,0).add(new Paragraph("العنوان"))
                    .setFontSize(8F)
                    .setFont(pdfFont)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));
            if (!yCustomerNameArr[1].equals("EMPTY")) {
                table3.addCell(new Cell().add(new Paragraph(yCustomerNameArr[1]))
                        .setFontSize(7F)
                        .setFont(pdfFont)
                        .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setBorder(Border.NO_BORDER)
                        .setTextAlignment(TextAlignment.CENTER));
            } else {
                table3.addCell(new Cell().add(new Paragraph("")).setBorderTop(Border.NO_BORDER));
            }

            if (!yCustomerAddArr[1].equals("EMPTY")) {
                table3.addCell(new Cell().add(new Paragraph(yCustomerAddArr[1]))
                        .setFontSize(7F)
                        .setFont(pdfFont)
                        .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setBorder(Border.NO_BORDER)
                        .setTextAlignment(TextAlignment.CENTER));
            } else {
                table3.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
            }

            String[] yCustCityArr = yCustomerDetailsArr[4].split("<sm1>");
            table3.addCell(new Cell(2,0).add(new Paragraph("VAT Number")).setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));
            table3.addCell(new Cell(2,0).add(new Paragraph(yCustomerDetailsArr[0])).setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setTextAlignment(TextAlignment.CENTER));
            table3.addCell(new Cell(2,0).add(new Paragraph("الرقم الضريبي"))
                    .setFontSize(8F)
                    .setFont(pdfFont)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));
            table3.addCell(new Cell(2,0).add(new Paragraph("City")).setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));
            if (!yCustCityArr[0].equals("EMPTY")) {
                table3.addCell(new Cell().add(new Paragraph(yCustCityArr[0])).setFontSize(8F)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBorder(Border.NO_BORDER));
            } else {
                table3.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
            }

            table3.addCell(new Cell(2,0).add(new Paragraph("المدينة"))
                    .setFontSize(8F)
                    .setFont(pdfFont)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));

            if (!yCustCityArr[1].equals("EMPTY")) {
                table3.addCell(new Cell().add(new Paragraph(yCustCityArr[1]))
                        .setFontSize(8F)
                        .setFont(pdfFont)
                        .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBorderTop(Border.NO_BORDER));
            } else {
                table3.addCell(new Cell().add(new Paragraph("")).setBorderTop(Border.NO_BORDER));
            }

            String[] yCustCountryArr = yCustomerDetailsArr[5].split("<sm1>");
            table3.addCell(new Cell(2,0).add(new Paragraph("Country")).setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setTextAlignment(TextAlignment.CENTER));
            table3.addCell(new Cell(2,0).add(new Paragraph("الرقم الضريبي للمجموعة"))
                    .setFontSize(8F)
                    .setFont(pdfFont)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));

            table3.addCell(new Cell(2,0).add(new Paragraph("Country"))
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));

            if (!yCustCountryArr[0].equals("EMPTY")) {
                table3.addCell(new Cell().add(new Paragraph(yCustCountryArr[0])).setFontSize(8F)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBorder(Border.NO_BORDER));
            } else {
                table3.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
            }

            table3.addCell(new Cell().add(new Paragraph("البلد"))
                    .setFontSize(8F)
                    .setFont(pdfFont)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));

            if (!yCustCountryArr[1].equals("EMPTY")) {
                table3.addCell(new Cell().add(new Paragraph(yCustCountryArr[1]))
                        .setFontSize(8F)
                        .setFont(pdfFont)
                        .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setBorderTop(Border.NO_BORDER));
            } else {
                table3.addCell(new Cell().add(new Paragraph("")).setBorderTop(Border.NO_BORDER));
            }

            // Table 3 End
            System.out.println("Table 3 is created");

            // Table 4 Start
            float[] pointColumWidth3 = {75F, 200F, 50F, 50F, 30F, 120F, 30F, 80F, 75F};
            Table table4 = new Table(pointColumWidth3);
            table4.setWidth(10F);
            table4.addCell(new Cell().add(new Paragraph("Supply Date"))
                    .setFontSize(8F)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorderBottom(Border.NO_BORDER)
                    .setBackgroundColor(new DeviceRgb(203, 248, 252)));
            table4.addCell(new Cell().add(new Paragraph("Description"))
                    .setFontSize(8F)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorderBottom(Border.NO_BORDER)
                    .setBackgroundColor(new DeviceRgb(203, 248, 252)));
            table4.addCell(new Cell().add(new Paragraph("Unit Price"))
                    .setFontSize(8F)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorderBottom(Border.NO_BORDER)
                    .setBackgroundColor(new DeviceRgb(203, 248, 252)));
            table4.addCell(new Cell().add(new Paragraph("QTY"))
                    .setFontSize(8F)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorderBottom(Border.NO_BORDER)
                    .setBackgroundColor(new DeviceRgb(203, 248, 252)));
            table4.addCell(new Cell().add(new Paragraph("Disc."))
                    .setFontSize(8F)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorderBottom(Border.NO_BORDER)
                    .setBackgroundColor(new DeviceRgb(203, 248, 252)));
            table4.addCell(new Cell().add(new Paragraph("Total excluding Tax"))
                    .setFontSize(8F)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorderBottom(Border.NO_BORDER)
                    .setBackgroundColor(new DeviceRgb(203, 248, 252)));
            table4.addCell(new Cell().add(new Paragraph("Rate"))
                    .setFontSize(8F)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorderBottom(Border.NO_BORDER)
                    .setBackgroundColor(new DeviceRgb(203, 248, 252)));
            table4.addCell(new Cell().add(new Paragraph("Tax Amount"))
                    .setFontSize(8F)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorderBottom(Border.NO_BORDER)
                    .setBackgroundColor(new DeviceRgb(203, 248, 252)));
            table4.addCell(new Cell().add(new Paragraph("Total Price"))
                    .setFontSize(8F)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorderBottom(Border.NO_BORDER)
                    .setBackgroundColor(new DeviceRgb(203, 248, 252)));
            table4.addCell(new Cell().add(new Paragraph("تاريخ الخدمة"))
                    .setFontSize(8F)
                    .setFont(pdfFont)
                    .setBorder(Border.NO_BORDER)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(new DeviceRgb(203, 248, 252)));
            table4.addCell(new Cell().add(new Paragraph("وصف الخدمة"))
                    .setFontSize(8F)
                    .setFont(pdfFont)
                    .setBorder(Border.NO_BORDER)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(new DeviceRgb(203, 248, 252)));
            table4.addCell(new Cell().add(new Paragraph("سعر الوحدة"))
                    .setFontSize(8F)
                    .setFont(pdfFont)
                    .setBorder(Border.NO_BORDER)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(new DeviceRgb(203, 248, 252)));
            table4.addCell(new Cell().add(new Paragraph("الكمية"))
                    .setFontSize(8F)
                    .setFont(pdfFont)
                    .setBorder(Border.NO_BORDER)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(new DeviceRgb(203, 248, 252)));
            table4.addCell(new Cell().add(new Paragraph("الخصم"))
                    .setFontSize(8F)
                    .setFont(pdfFont)
                    .setBorder(Border.NO_BORDER)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(new DeviceRgb(203, 248, 252)));
            table4.addCell(new Cell().add(new Paragraph("الاجمالي باستثناء ضريبة"))
                    .setFontSize(8F)
                    .setFont(pdfFont)
                    .setBorder(Border.NO_BORDER)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(new DeviceRgb(203, 248, 252)));
            table4.addCell(new Cell().add(new Paragraph("النسبة"))
                    .setFontSize(8F)
                    .setFont(pdfFont)
                    .setBorder(Border.NO_BORDER)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(new DeviceRgb(203, 248, 252)));
            table4.addCell(new Cell().add(new Paragraph("قيمة الضريبة"))
                    .setFontSize(8F)
                    .setFont(pdfFont)
                    .setBorder(Border.NO_BORDER)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(new DeviceRgb(203, 248, 252)));
            table4.addCell(new Cell().add(new Paragraph("السعر الاجمالي"))
                    .setFontSize(8F)
                    .setFont(pdfFont)
                    .setBorder(Border.NO_BORDER)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(new DeviceRgb(203, 248, 252)));

            for ( int i = 0 ; i < yTaxDetailsArrList.size(); i++) {
                try {
                    String[] yTaxDetailsArr = yTaxDetailsArrList.get(i).split("<sm1>");
                    if (yTaxDetailsArr.length < 3) {
                        System.out.println("Warning: Skipping tax detail item " + i + " - insufficient data");
                        continue;
                    }
                    
                    table4.addCell(new Cell(2,0).add(new Paragraph(convertDate(yTaxDetailsArr[0]))).setFontSize(8F)
                            .setVerticalAlignment(VerticalAlignment.MIDDLE));
                    
                    String[] yTaxDescArr = yTaxDetailsArr[1].split("<sm2>");
                    if (yTaxDescArr.length > 0 && !yTaxDescArr[0].equals("EMPTY")){
                        table4.addCell(new Cell().add(new Paragraph(yTaxDescArr[0]))
                                .setFontSize(8F)
                                .setFont(pdfFont)
                                .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setBorderBottom(Border.NO_BORDER));
                    } else {
                        table4.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
                    }
                    
                    // Add remaining fields with bounds checking
                    String[] remainingFields = yTaxDetailsArr[2].split("<sm>");
                    for (int j = 0; j < Math.min(remainingFields.length, 7); j++) {
                        table4.addCell(new Cell(2,0).add(new Paragraph(remainingFields[j])).setFontSize(8F)
                                .setVerticalAlignment(VerticalAlignment.MIDDLE));
                    }
                    
                    // Fill remaining cells if needed
                    for (int j = remainingFields.length; j < 7; j++) {
                        table4.addCell(new Cell(2,0).add(new Paragraph("")).setFontSize(8F)
                                .setVerticalAlignment(VerticalAlignment.MIDDLE));
                    }
                    
                    if (yTaxDescArr.length > 1 && !yTaxDescArr[1].equals("EMPTY")){
                        table4.addCell(new Cell().add(new Paragraph(yTaxDescArr[1]))
                                .setFontSize(8F)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setBorderBottom(Border.NO_BORDER));
                    } else {
                        table4.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
                    }
                } catch (Exception e) {
                    System.out.println("Warning: Error processing tax detail item " + i + ": " + e.getMessage());
                    // Add empty cells to maintain table structure
                    for (int j = 0; j < 9; j++) {
                        table4.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
                    }
                }
            }

            // Table 4 End
            System.out.println("Table 4 is created");

            // Table 5 Start
            float[] pointColumWidth4 = {200F, 200F, 200F};
            Table table5 = new Table(pointColumWidth4);
            table5.setHorizontalAlignment(HorizontalAlignment.CENTER);
            table5.setWidth(10F);
            table5.addCell(new Cell().add(new Paragraph("Total (Excluding VAT)"))
                    .setBold()
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));
            table5.addCell(new Cell().add(new Paragraph(yTaxTotalDetailsArr[0])).setBold().setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));
            table5.addCell(new Cell().add(new Paragraph("الاجمالي (باستثناء الضريبة)"))
                    .setBold()
                    .setFont(pdfFont)
                    .setFontSize(10F)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.RIGHT));
            table5.addCell(new Cell().add(new Paragraph("Total Descount"))
                    .setBold()
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));
            table5.addCell(new Cell().add(new Paragraph(yTaxTotalDetailsArr[1])).setBold().setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));
            table5.addCell(new Cell().add(new Paragraph("الخصم الكلي"))
                    .setBold()
                    .setFontSize(10F)
                    .setFont(pdfFont)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.RIGHT));
            table5.addCell(new Cell().add(new Paragraph("Total VAT"))
                    .setBold()
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));
            table5.addCell(new Cell().add(new Paragraph(yTaxTotalDetailsArr[2])).setBold().setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));
            table5.addCell(new Cell().add(new Paragraph("الاجمالي الضريبي"))
                    .setBold()
                    .setFontSize(10F)
                    .setFont(pdfFont)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.RIGHT));
            table5.addCell(new Cell().add(new Paragraph("Total including VAT"))
                    .setBold()
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));
            table5.addCell(new Cell().add(new Paragraph(yTaxTotalDetailsArr[3])).setBold().setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));
            table5.addCell(new Cell().add(new Paragraph("الاجمالي (بما في ذلك الضريبة)"))
                    .setBold()
                    .setFontSize(10F)
                    .setFont(pdfFont)
                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                    .setTextAlignment(TextAlignment.RIGHT));
            // Table 5 End

            System.out.println("Table 5 is created");

            document.add(table6);
            document.add(table1);
            document.add(table2);
            document.add(table3);
            document.add(table4);
            document.add(table5);

            PdfEventHandler eventHandler = new PdfEventHandler();
            pdfDocument.addEventHandler(PdfDocumentEvent.END_PAGE, eventHandler);
            // Try to add footer image, but continue without it if it fails
            try {
                Rectangle pageSize = pdfDocument.getLastPage().getPageSize();
                float x = pageSize.getLeft() + 36;
                float y = pageSize.getBottom() - 20;
                ImageData footerImageData = ImageDataFactory.create(footerPath);
                Image imageFooter = new Image(footerImageData);
                imageFooter.setMarginTop(20F);
                imageFooter.setFixedPosition(pdfDocument.getPageNumber(pdfDocument.getLastPage()), x, y);
                imageFooter.setAutoScale(true);
                imageFooter.setHorizontalAlignment(HorizontalAlignment.CENTER);
                document.add(imageFooter);
            } catch (Exception e) {
                System.out.println("Warning: Could not load footer image: " + footerPath);
                System.out.println("Continuing without footer image.");
            }
            document.close();

            System.out.println("PDF Created");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception occurred " + e.getMessage());
        }

        return path;
    }

    class PdfEventHandler implements IEventHandler {
        @Override
        public  void handleEvent(Event event) {

        }
    }

    @SuppressWarnings("unused")
    private boolean isArabic(String word){
        word = word.trim().replaceAll(" ", "");
        for (int i = 0; i < word.length(); i++) {
            int c = word.codePointAt(i);
            if ((c >= 0x0600 && c <= 0x06FF) || (c >= 0x0750 && c <= 0x077F))
                i += Character.charCount(c);
            else
                return false;
        }

        return true;
    }

    public String convertDate(String dateVal)
    {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat format2 = new SimpleDateFormat("dd MM yyyy");
        try {
            Date date = format1.parse(dateVal);
            dateVal = format2.format(date).toUpperCase();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateVal;
    }
}