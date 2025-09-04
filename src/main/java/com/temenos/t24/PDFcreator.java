package com.temenos.t24;

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

import java.io.File;


public class PDFcreator {
    public static String createPDF(String args){

        System.out.println("inputed args" + args);

        String[] argsArray = args.split("<fm>");
        String path = argsArray[0];
        String arabicFontPath =  argsArray[6]; //"C:\\Users\\moayadj\\IdeaProjects\\PDFcreatorTaxGTS\\test-resources\\fonts\\NotoNaskhArabic-Regular.ttf";
        String logoPath = argsArray[7]; //"C:\\Users\\moayadj\\IdeaProjects\\PDFcreatorTaxGTS\\\\test-resources\\images\\NBI_LOGO.jpg";
        String footerPath = argsArray[8];
        String[] taxBillDetailsArr = argsArray[1].split("<sm>");
        String[] custDetailsArr = argsArray[2].split("<sm>");
        List<String> taxDetailsArrList = Arrays.asList(argsArray[3].split("<vm>"));
        String[] taxTotalDetailsArr = argsArray[4].split("<sm>");
        String[] taxIbanDetArr = argsArray[5].split("<sm>");

        System.out.println("argsArray" + Arrays.toString(argsArray));
        System.out.println("path" + path);
        System.out.println("arabicFontPath" + arabicFontPath);
        System.out.println("logoPath" + logoPath);
        System.out.println("footerPath" + footerPath);
        System.out.println("taxBillDetailsArr" + Arrays.toString(taxBillDetailsArr));
        System.out.println("custDetailsArr" + Arrays.toString(custDetailsArr));
        System.out.println("taxTotalDetailsArr" + Arrays.toString(taxTotalDetailsArr));
        System.out.println("taxIbanDetArr" + Arrays.toString(taxIbanDetArr));

        try {
            File f = new File(arabicFontPath);
            System.out.println("Font path = " + arabicFontPath);
            System.out.println("Font exists? " + f.exists());
            System.out.println("Font readable? " + f.canRead());

            PdfFont pdfFont = PdfFontFactory.createFont(
                    arabicFontPath,
                    PdfEncodings.IDENTITY_H
            );

            PdfWriter pdfWritter = new PdfWriter(path);
            PdfDocument pdfDocument = new PdfDocument(pdfWritter);
            Document document = new Document(pdfDocument);

            System.out.println("header creation started");

            // Page header
            float [] pointColumnWidths1 = {380F,220F};

            System.out.println("pointColumnWidths1" + Arrays.toString(pointColumnWidths1));

            Table table1 = new Table(pointColumnWidths1);

            System.out.println("table1 init");

            System.out.println("imgArabic" + logoPath);

            ImageData dataArab = ImageDataFactory.create(logoPath);

            System.out.println("imgArabic" + logoPath);

            Image imgArab = new Image(dataArab);

            System.out.println("image init");

            imgArab.scaleToFit(150, 150);
            imgArab.setFixedPosition(1, 420, 750);

            System.out.println("image init size");


            table1.addCell(
                    new Cell().add(
                                    new Paragraph(processArabic("المصرف الأهلي العراقي"))
                                            .setFont(pdfFont)
                                            .setFontSize(12.5F)
                                            .setTextAlignment(TextAlignment.RIGHT)
                                            .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                            ).setBorder(Border.NO_BORDER)
                            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            );

            table1.addCell(new Cell().add(imgArab)
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT)
            );


            System.out.println("header creation is done");



            String qrContent = "https://example.com";
            BarcodeQRCode qrCode = new BarcodeQRCode(qrContent);
            PdfFormXObject qrObject = qrCode.createFormXObject(pdfDocument);
            Image qrImage = new Image(qrObject);
            qrImage.setWidth(100);
            qrImage.setHeight(100);
            qrImage.setFixedPosition(1, 36, 730);
            document.add(qrImage);

            System.out.println("QR code added successfully to PDF");


            System.out.println("info header creation started");

            // Info header
            float [] pointColumnWidths = {175F, 109F};
            Table table2 = new Table(pointColumnWidths);
            table2.setMarginTop(30F);
            table2.setHorizontalAlignment(HorizontalAlignment.CENTER);
            if (!taxIbanDetArr[0].equals("EMPTY")) {
                table2.addCell(
                        new Cell().add(
                                        new Paragraph(taxIbanDetArr[0])
                                                .setFontSize(8F)
                                                .setTextAlignment(TextAlignment.RIGHT)
                                ).setBorder(Border.NO_BORDER)
                                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                );
            }else {
                table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
            }

            table2.addCell(
                    new Cell().add(
                                    new Paragraph(processArabic(": رقم تسجيل الضريبة القيمة المضافة"))
                                            .setFont(pdfFont)
                                            .setFontSize(8F)
                                            .setCharacterSpacing(0)
                                            .setPadding(0F)
                                            .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                            .setTextAlignment(TextAlignment.RIGHT)
                            )
                            .setBorder(Border.NO_BORDER)
                            .setMarginBottom(0)
            );

            if (!taxIbanDetArr[1].equals("EMPTY")) {
                table2.addCell(new Cell()
                        .add(new Paragraph(taxIbanDetArr[1]))
                        .setFontSize(8F)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setBorder(Border.NO_BORDER)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE));
            }else {
                table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
            }

            table2.addCell(
                    new Cell().add(
                                    new Paragraph(":" + "IBAN" + processArabic(" رقم"))
                                            .setFont(pdfFont)
                                            .setFontSize(8F)
                                            .setCharacterSpacing(0)
                                            .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                            .setTextAlignment(TextAlignment.LEFT)
                            )
                            .setBorder(Border.NO_BORDER)
                            .setPadding(0F)
                            .setMarginTop(0)
            );


            System.out.println("info header creation is done");
            System.out.println("First table  creation started");

            // First table
            float [] pointColumnWidths3 = {90F, 100F, 90F, 90F, 150F, 90F};
            Table table3 = new Table(pointColumnWidths3);
            table3.setMarginTop(10F);

            table3.addCell(
                    new Cell(0, 6).add(
                                    new Paragraph(processArabic("فاتورة ضريبية"))
                                            .setFont(pdfFont)
                                            .setFontSize(15F)
                                            .setBold()
                                            .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                            .setTextAlignment(TextAlignment.CENTER)
                            )
                            .setPadding(0F)
                            .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(182, 248, 252)))
            );

            table3.addCell(new Cell()
                    .add(new Paragraph("Invoice Number")).setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
            );

            table3.addCell(new Cell()
                    .add(new Paragraph(taxBillDetailsArr[0]))
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
            );

            table3.addCell(new Cell().add(
                            new Paragraph(processArabic("رقم الفاتورة"))
                                    .setFontSize(10F)
                                    .setFont(pdfFont)
                                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                    .setTextAlignment(TextAlignment.RIGHT)
                    )
            );

            table3.addCell(new Cell()
                    .add(new Paragraph("Invoice Date"))
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
            );

            table3.addCell(new Cell()
                    .add(new Paragraph(taxBillDetailsArr[1]))
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
            );

            table3.addCell(new Cell().add(
                            new Paragraph(processArabic("تاريخ الفاتورة"))
                                    .setFontSize(10F)
                                    .setFont(pdfFont)
                                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                    .setTextAlignment(TextAlignment.RIGHT)
                    )
            );

            table3.addCell(new Cell()
                    .add(new Paragraph("Service Start Date"))
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
            );

            table3.addCell(new Cell()
                    .add(new Paragraph(convertDate(taxBillDetailsArr[2])))
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
            );

            table3.addCell(new Cell().add(
                            new Paragraph(processArabic("تاريخ بدء الخدمة"))
                                    .setFontSize(10F)
                                    .setFont(pdfFont)
                                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                    .setTextAlignment(TextAlignment.RIGHT)
                    )
            );

            table3.addCell(new Cell()
                    .add(new Paragraph("Service End Date"))
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
            );

            table3.addCell(new Cell()
                    .add(new Paragraph(convertDate(taxBillDetailsArr[3])))
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
            );

            table3.addCell(new Cell().add(
                            new Paragraph(processArabic("تاريخ إنتهاء الخدمة"))
                                    .setFontSize(10F)
                                    .setFont(pdfFont)
                                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                    .setTextAlignment(TextAlignment.RIGHT)
                    )
            );

            System.out.println("First table creation is done");
            System.out.println("Second table  creation started");


            // Second table - Customer Details
            float [] pointColumnWidths4 = {50F, 200F, 100F, 50F, 200F, 100F};
            Table table4 = new Table(pointColumnWidths4);
            table4.setMarginTop(10F);

            table4.addCell(new Cell(0, 3)
                    .add(new Paragraph("Customer Details"))
                    .setFontSize(10F)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(182, 248, 252)))
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorderRight(Border.NO_BORDER)
            );

            table4.addCell(new Cell(0, 3).add(
                                    new Paragraph(processArabic("بيانات العميل"))
                                            .setFontSize(10F)
                                            .setFont(pdfFont)
                                            .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                            .setTextAlignment(TextAlignment.RIGHT)
                                            .setBorderLeft(Border.NO_BORDER)
                            )
                            .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(182, 248, 252)))
            );

            String[] customerNameArr = custDetailsArr[2].split("<sm1>");
            String[] customerAddrArr = custDetailsArr[3].split("<sm1>");

            table4.addCell(new Cell(2,0)
                    .add(new Paragraph("Name"))
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
            );

            if (!customerNameArr[0].equals("EMPTY")) {
                table4.addCell(new Cell()
                        .add(new Paragraph(customerNameArr[0]))
                        .setFontSize(7F)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setBorderBottom(Border.NO_BORDER)
                        .setTextAlignment(TextAlignment.CENTER)
                );
            }else {
                table4.addCell(new Cell()
                        .add(new Paragraph(""))
                        .setBorderBottom(Border.NO_BORDER)
                );
            }

            table4.addCell(new Cell(2,0).add(
                            new Paragraph(processArabic("الاسم"))
                                    .setFontSize(8F)
                                    .setFont(pdfFont)
                                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                    .setTextAlignment(TextAlignment.RIGHT)
                                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    )
            );

            table4.addCell(new Cell(2,0)
                    .add(new Paragraph("Address"))
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
            );

            if (!customerAddrArr[0].equals("EMPTY")) {
                table4.addCell(new Cell().add(
                                new Paragraph(customerAddrArr[0]))
                        .setFontSize(8F)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setBorderBottom(Border.NO_BORDER)
                        .setTextAlignment(TextAlignment.CENTER)
                );
            }else {
                table4.addCell(new Cell()
                        .add(new Paragraph(""))
                        .setBorderBottom(Border.NO_BORDER)
                );
            }

            table4.addCell(new Cell(2,0).add(
                            new Paragraph(processArabic("العنوان"))
                                    .setFontSize(8F)
                                    .setFont(pdfFont)
                                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                    .setTextAlignment(TextAlignment.RIGHT)
                                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    )
            );

            if (!customerNameArr[1].equals("EMPTY")) {
                table4.addCell(new Cell()
                        .add((new Paragraph(customerNameArr[1])))
                        .setFontSize(7F)
                        .setFont(pdfFont)
                        .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setBorderTop(Border.NO_BORDER)
                        .setTextAlignment(TextAlignment.CENTER)
                );

            }else {
                table4.addCell(new Cell()
                        .add(new Paragraph(""))
                        .setBorderTop(Border.NO_BORDER)
                );
            }

            if (!customerAddrArr[1].equals("EMPTY")) {
                table4.addCell(new Cell()
                        .add(new Paragraph(processArabic(customerAddrArr[1]))
                                        .setFontSize(7F)
                                        .setFont(pdfFont)
                                        .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                //.setTextAlignment(TextAlignment.RIGHT)
                        )
                        .setBorderTop(Border.NO_BORDER)
                        .setTextAlignment(TextAlignment.CENTER)
                );

            }else {
                table4.addCell(new Cell()
                        .add(new Paragraph(""))
                        .setBorderTop(Border.NO_BORDER)
                );
            }

            String[] custCityArr = custDetailsArr[4].split("<sm1>");

            table4.addCell(new Cell(2,0)
                    .add(new Paragraph("VAT Number"))
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
            );

            table4.addCell(new Cell(2,0)
                    .add(new Paragraph(custDetailsArr[0]))
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setTextAlignment(TextAlignment.CENTER)
            );

            table4.addCell(new Cell(2,0).add(
                            new Paragraph(processArabic("الرقم الضريبي"))
                                    .setFontSize(8F)
                                    .setFont(pdfFont)
                                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                    .setTextAlignment(TextAlignment.RIGHT)
                                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    )
            );

            table4.addCell(new Cell(2,0)
                    .add(new Paragraph("City"))
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
            );

            if (!custCityArr[0].equals("EMPTY")) {
                table4.addCell(new Cell()
                        .add(new Paragraph(custCityArr[0]))
                        .setFontSize(8F)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBorderBottom(Border.NO_BORDER));
            }else {
                table4.addCell(new Cell()
                        .add(new Paragraph(""))
                        .setBorder(Border.NO_BORDER)
                );
            }

            table4.addCell(new Cell(2,0).add(
                            new Paragraph(processArabic("المدينة"))
                                    .setFontSize(8F)
                                    .setFont(pdfFont)
                                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                    .setTextAlignment(TextAlignment.RIGHT)
                                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    )
            );

            if (!custCityArr[1].equals("EMPTY")) {
                table4.addCell(new Cell()
                        .add(new Paragraph(processArabic(custCityArr[1]))
                                .setFontSize(8F)
                                .setFont(pdfFont)
                                .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                .setTextAlignment(TextAlignment.CENTER)
                        )
                        .setBorderTop(Border.NO_BORDER)
                );
            }else {
                table4.addCell(new Cell()
                        .add(new Paragraph(""))
                        .setBorderTop(Border.NO_BORDER)
                );
            }


            String[] custCountryArr = custDetailsArr[5].split("<sm1>");
            table4.addCell(new Cell(2,0)
                    .add(new Paragraph("VAT Group#"))
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
            );

            table4.addCell(new Cell(2,0)
                    .add(new Paragraph(custDetailsArr[1]))
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setTextAlignment(TextAlignment.CENTER)
            );

            table4.addCell(new Cell(2,0).add(
                            new Paragraph(processArabic("الرقم الضريبي للمجموعة"))
                                    .setFontSize(8F)
                                    .setFont(pdfFont)
                                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                    .setTextAlignment(TextAlignment.RIGHT)
                                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    )
            );

            table4.addCell(new Cell(2,0)
                    .add(new Paragraph("Country"))
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
            );

            if(!custCountryArr[0].equals("EMPTY")) {
                table4.addCell(new Cell()
                        .add(new Paragraph(custCountryArr[0]))
                        .setFontSize(8F)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBorderBottom(Border.NO_BORDER)
                );
            }else {
                table4.addCell(new Cell()
                        .add(new Paragraph(""))
                        .setBorderBottom(Border.NO_BORDER)
                );
            }

            table4.addCell(new Cell(2,0).add(
                            new Paragraph(processArabic("البلد"))
                                    .setFontSize(8F)
                                    .setFont(pdfFont)
                                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                    .setTextAlignment(TextAlignment.RIGHT)
                                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    )
            );

            if(!custCountryArr[1].equals("EMPTY")) {
                table4.addCell(new Cell()
                        .add(new Paragraph(processArabic(custCountryArr[1]))
                                .setFontSize(8F)
                                .setFont(pdfFont)
                                .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                .setTextAlignment(TextAlignment.CENTER)
                        )
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setBorderTop(Border.NO_BORDER)
                );
            }else {
                table4.addCell(new Cell()
                        .add(new Paragraph(""))
                        .setBorderTop(Border.NO_BORDER)
                );
            }

            System.out.println("Second table creation is done");
            System.out.println("Third table  creation started");

            // Third table
            float [] pointColumnWidths5 = {75F, 200F, 50F, 50F, 30F, 120F, 30F, 80F, 75F};
            Table table5 = new Table(pointColumnWidths5);
            table5.setMarginTop(10F);

            table5.addCell(new Cell()
                    .add(new Paragraph("Supply Date"))
                    .setFontSize(8F)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorderBottom(Border.NO_BORDER)
                    .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247)))
            );

            table5.addCell(new Cell()
                    .add(new Paragraph("Description"))
                    .setFontSize(8F)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorderBottom(Border.NO_BORDER)
                    .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247)))
            );

            table5.addCell(new Cell()
                    .add(new Paragraph("Unit Price"))
                    .setFontSize(8F)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorderBottom(Border.NO_BORDER)
                    .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247)))
            );

            table5.addCell(new Cell().add(new Paragraph("QTY"))
                    .setFontSize(8F)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorderBottom(Border.NO_BORDER)
                    .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247)))
            );

            table5.addCell(new Cell()
                    .add(new Paragraph("Disc."))
                    .setFontSize(8F)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorderBottom(Border.NO_BORDER)
                    .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247)))
            );

            table5.addCell(new Cell()
                    .add(new Paragraph("Total excluding Tax"))
                    .setFontSize(8F)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorderBottom(Border.NO_BORDER)
                    .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247)))
            );

            table5.addCell(new Cell()
                    .add(new Paragraph("Rate"))
                    .setFontSize(8F)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorderBottom(Border.NO_BORDER)
                    .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247)))
            );

            table5.addCell(new Cell()
                    .add(new Paragraph("Tax Amount"))
                    .setFontSize(8F)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorderBottom(Border.NO_BORDER)
                    .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247)))
            );

            table5.addCell(new Cell()
                    .add(new Paragraph("Total Price"))
                    .setFontSize(8F)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorderBottom(Border.NO_BORDER)
                    .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247)))
            );

            table5.addCell(new Cell().add(
                                    new Paragraph(processArabic("تاريخ الخدمة"))
                                            .setFontSize(8F)
                                            .setFont(pdfFont)
                                            .setBorderTop(Border.NO_BORDER)
                                            .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                            .setTextAlignment(TextAlignment.CENTER)
                            )
                            .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247)))
            );

            table5.addCell(new Cell().add(
                                    new Paragraph(processArabic("وصف الخدمة"))
                                            .setFontSize(8F)
                                            .setFont(pdfFont)
                                            .setBorderTop(Border.NO_BORDER)
                                            .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                            .setTextAlignment(TextAlignment.CENTER)
                            )
                            .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247)))
            );

            table5.addCell(new Cell().add(
                                    new Paragraph(processArabic("سعر الوحدة"))
                                            .setFontSize(8F)
                                            .setFont(pdfFont)
                                            .setBorderTop(Border.NO_BORDER)
                                            .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                            .setTextAlignment(TextAlignment.CENTER)
                            )
                            .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247)))
            );

            table5.addCell(new Cell().add(
                                    new Paragraph(processArabic("الكمية"))
                                            .setFontSize(8F)
                                            .setFont(pdfFont)
                                            .setBorderTop(Border.NO_BORDER)
                                            .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                            .setTextAlignment(TextAlignment.CENTER)
                            )
                            .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247)))
            );

            table5.addCell(new Cell().add(
                                    new Paragraph(processArabic("الخصم"))
                                            .setFontSize(8F)
                                            .setFont(pdfFont)
                                            .setBorderTop(Border.NO_BORDER)
                                            .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                            .setTextAlignment(TextAlignment.CENTER)
                            )
                            .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247)))
            );

            table5.addCell(new Cell().add(
                                    new Paragraph(processArabic("الإجمالي باستثناء الضريبة"))
                                            .setFontSize(8F)
                                            .setFont(pdfFont)
                                            .setBorderTop(Border.NO_BORDER)
                                            .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                            .setTextAlignment(TextAlignment.CENTER)
                            )
                            .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247)))
            );

            table5.addCell(new Cell().add(
                                    new Paragraph(processArabic("النسبة"))
                                            .setFontSize(8F)
                                            .setFont(pdfFont)
                                            .setBorderTop(Border.NO_BORDER)
                                            .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                            .setTextAlignment(TextAlignment.CENTER)
                            )
                            .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247)))
            );

            table5.addCell(new Cell().add(
                                    new Paragraph(processArabic("مبلغ الضريبة"))
                                            .setFontSize(8F)
                                            .setFont(pdfFont)
                                            .setBorderTop(Border.NO_BORDER)
                                            .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                            .setTextAlignment(TextAlignment.CENTER)
                            )
                            .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247)))
            );

            table5.addCell(new Cell().add(
                                    new Paragraph(processArabic("السعر الكلي"))
                                            .setFontSize(8F)
                                            .setFont(pdfFont)
                                            .setBorderTop(Border.NO_BORDER)
                                            .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                            .setTextAlignment(TextAlignment.CENTER)
                            )
                            .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247)))
            );

            for (int i=0; i<taxDetailsArrList.size(); i++)
            {
                String[] taxDetailsArr = taxDetailsArrList.get(i).split("<sm>");
                table5.addCell(new Cell(2,0)
                        .add(new Paragraph(convertDate(taxDetailsArr[0])))
                        .setFontSize(8F)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                );

                String[] taxDescArr = taxDetailsArr[1].split("<sm1>");
                if(!taxDescArr[0].equals("EMPTY")) {
                    table5.addCell(new Cell()
                            .add(new Paragraph(processArabic(taxDescArr[0]))
                                    .setFontSize(8F)
                                    .setFont(pdfFont)
                                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                    .setTextAlignment(TextAlignment.CENTER)
                            )
                                    .setBorderBottom(Border.NO_BORDER)
                    );
                }else {
                    table5.addCell(new Cell()
                            .add(new Paragraph(""))
                            .setBorderBottom(Border.NO_BORDER)
                    );
                }

                table5.addCell(new Cell(2,0)
                        .add(new Paragraph(taxDetailsArr[2]))
                        .setFontSize(8F)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                );

                table5.addCell(new Cell(2,0)
                        .add(new Paragraph(taxDetailsArr[3])).setFontSize(8F)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                );

                table5.addCell(new Cell(2,0)
                        .add(new Paragraph(taxDetailsArr[4]))
                        .setFontSize(8F)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                );

                table5.addCell(new Cell(2,0).add(
                                new Paragraph(taxDetailsArr[5]))
                        .setFontSize(8F)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                );

                table5.addCell(new Cell(2,0)
                        .add(new Paragraph(taxDetailsArr[6]))
                        .setFontSize(8F)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                );

                table5.addCell(new Cell(2,0)
                        .add(new Paragraph(taxDetailsArr[7]))
                        .setFontSize(8F)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                );

                table5.addCell(new Cell(2,0)
                        .add(new Paragraph(taxDetailsArr[8]))
                        .setFontSize(8F)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                );

                if(!taxDescArr[1].equals("EMPTY")) {
                    table5.addCell(new Cell()
                            .add(new Paragraph(taxDescArr[1]))
                            .setFontSize(8F)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setBorderTop(Border.NO_BORDER)
                    );
                }else {
                    table5.addCell(new Cell()
                            .add(new Paragraph(""))
                            .setBorderTop(Border.NO_BORDER)
                    );
                }
            }

            System.out.println("Third table creation is done");
            System.out.println("Forth  table  creation started");

            // Forth table
            float [] pointColumnWidths6 = {200F, 200F, 200F};
            Table table6 = new Table(pointColumnWidths6);
            table6.setHorizontalAlignment(HorizontalAlignment.CENTER);
            table6.setMarginTop(10F);

            table6.addCell(new Cell()
                    .add(new Paragraph("Total (Excluding VAT)"))
                    .setBold()
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
            );

            table6.addCell(new Cell()
                    .add(new Paragraph(taxTotalDetailsArr[0])).setBold().setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
            );

            table6.addCell(new Cell().add(
                            new Paragraph(processArabic("الإجمالي (باستثناء ضريبة القيمة المضافة)"))
                                    .setBold()
                                    .setFont(pdfFont)
                                    .setFontSize(10F)
                                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                    .setTextAlignment(TextAlignment.RIGHT)
                    )
            );

            table6.addCell(new Cell()
                    .add(new Paragraph("Total Discount"))
                    .setBold()
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
            );

            table6.addCell(new Cell()
                    .add(new Paragraph(taxTotalDetailsArr[1]))
                    .setBold().setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
            );

            table6.addCell(new Cell().add(
                            new Paragraph(processArabic("مجموع الخصومات"))
                                    .setBold()
                                    .setFontSize(10F)
                                    .setFont(pdfFont)
                                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                    .setTextAlignment(TextAlignment.RIGHT)
                    )
            );

            table6.addCell(new Cell().add(new Paragraph("Total VAT")).setBold().setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
            );

            table6.addCell(new Cell()
                    .add(new Paragraph(taxTotalDetailsArr[2]))
                    .setBold()
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
            );

            table6.addCell(new Cell().add(
                            new Paragraph(processArabic("مجموع ضريبة القيمة المضافة"))
                                    .setBold()
                                    .setFontSize(10F)
                                    .setFont(pdfFont)
                                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                    .setTextAlignment(TextAlignment.RIGHT)
                    )
            );

            table6.addCell(new Cell()
                    .add(new Paragraph("Amount includes VAT"))
                    .setBold()
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
            );

            table6.addCell(new Cell()
                    .add(new Paragraph(taxTotalDetailsArr[3]))
                    .setBold()
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
            );

            table6.addCell(new Cell().add(
                            new Paragraph(processArabic("المجموع شامل ضريبة القيمة المضافة"))
                                    .setBold()
                                    .setFontSize(10F)
                                    .setFont(pdfFont)
                                    .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                    .setTextAlignment(TextAlignment.RIGHT)
                    )
            );

            System.out.println("Forth table creation is done");

            /*
             *
             * TODO uncommint
             *
             * */

            document.add(table1);
            document.add(qrImage);
            document.add(table2);
            document.add(table3);
            document.add(table4);
            document.add(table5);
            document.add(table6);

            PdfEventHandler eventHandler = new PdfEventHandler();
            pdfDocument.addEventHandler(PdfDocumentEvent.END_PAGE, eventHandler);
            Rectangle pageSize = pdfDocument.getLastPage().getPageSize();
            float x = pageSize.getLeft() + 36;
            float y = pageSize.getBottom()-20;
            String imgFooter =  footerPath;
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
            System.out.println("Exception occured "+ e.getMessage());
            Throwable cause = e.getCause();
            if (cause != null) {
                System.err.println("Root cause: " + cause);
                cause.printStackTrace();
            }
        }

        return path;
    }

    public static String convertDate(String dateVal) {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat format2 = new SimpleDateFormat("dd MMM yyyy");
        try {
            Date date = format1.parse(dateVal);
            dateVal = format2.format(date).toUpperCase();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateVal;
    }

    static class PdfEventHandler implements IEventHandler {
        @Override
        public void handleEvent(Event event) {

        }
    }

    public static String processArabic(String text) throws ArabicShapingException {
        // Apply Arabic shaping (joins letters properly)
        ArabicShaping arabicShaping = new ArabicShaping(ArabicShaping.LETTERS_SHAPE);
        String shaped = arabicShaping.shape(text);

        // Apply bidi (right-to-left reordering)
        Bidi bidi = new Bidi(shaped, Bidi.REORDER_DEFAULT);
        return bidi.writeReordered(Bidi.DO_MIRRORING);
    }

}
