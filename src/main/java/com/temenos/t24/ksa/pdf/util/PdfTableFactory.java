package com.temenos.t24.ksa.pdf.util;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.BaseDirection;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.temenos.t24.ksa.pdf.model.InvoiceData;
import com.temenos.t24.ksa.pdf.model.InvoiceLineItem;

import java.net.MalformedURLException;

import static com.temenos.t24.ksa.pdf.util.TextUtil.convertDate;
import static com.temenos.t24.ksa.pdf.util.TextUtil.processArabic;

/**
 * Factory for creating invoice tables for PDF output.
 */
public final class PdfTableFactory {

    private PdfTableFactory() {
    }

    /**
     * Header table with bank name and logo.
     */
    public static Table createHeaderTable(PdfFont arabicFont, String logoPath) throws MalformedURLException {
        float[] widths = {380F, 220F};
        Table table = new Table(widths);

        table.addCell(new Cell().add(
                        new Paragraph(processArabic("المصرف الأهلي العراقي"))
                                .setFont(arabicFont)
                                .setFontSize(12.5F)
                                .setTextAlignment(TextAlignment.RIGHT)
                                .setBaseDirection(BaseDirection.RIGHT_TO_LEFT))
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE));

        ImageData logoData = ImageDataFactory.create(logoPath);

        Image logo = new Image(logoData).scaleToFit(150, 150).setFixedPosition(1, 420, 750);
        table.addCell(new Cell().add(logo).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
        return table;
    }

    /**
     * Info header table (IBAN and VAT registration).
     */
    public static Table createInfoHeaderTable(InvoiceData data, PdfFont arabicFont) {
        float[] widths = {175F, 109F};
        Table table = new Table(widths).setMarginTop(30F).setHorizontalAlignment(HorizontalAlignment.CENTER);

        // IBAN number (English)
        table.addCell(new Cell().add(new Paragraph(data.iban.ibanNumber))
                .setFontSize(8F)
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(Border.NO_BORDER));

        // Arabic label for VAT registration number
        table.addCell(new Cell().add(new Paragraph(processArabic(": رقم تسجيل الضريبة القيمة المضافة"))
                        .setFont(arabicFont).setFontSize(8F)
                        .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                        .setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER));

        // VAT registration number (English)
        table.addCell(new Cell().add(new Paragraph(data.iban.taxRegistrationNumber))
                .setFontSize(8F)
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(Border.NO_BORDER));

        // Arabic label for IBAN
        table.addCell(new Cell().add(new Paragraph(":" + "IBAN" + processArabic(" رقم"))
                        .setFont(arabicFont).setFontSize(8F)
                        .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                        .setTextAlignment(TextAlignment.LEFT))
                .setBorder(Border.NO_BORDER));

        return table;
    }

    /**
     * Invoice summary table (invoice number, dates).
     */
    public static Table createInvoiceSummaryTable(InvoiceData data, PdfFont arabicFont) {
        float[] widths = {90F, 100F, 90F, 90F, 150F, 90F};
        Table table = new Table(widths).setMarginTop(10F);

        // Title row
        table.addCell(new Cell(0, 6).add(new Paragraph(processArabic("فاتورة ضريبية"))
                        .setFont(arabicFont)
                        .setFontSize(15F)
                        .setBold()
                        .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                        .setTextAlignment(TextAlignment.CENTER))
                .setPadding(0F)
                .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(182, 248, 252))));

        // Invoice number
        table.addCell(new Cell().add(new Paragraph("Invoice Number")).setFontSize(8F));
        table.addCell(new Cell().add(new Paragraph(data.header.invoiceNumber)).setFontSize(8F));
        table.addCell(new Cell().add(new Paragraph(processArabic("رقم الفاتورة"))
                .setFontSize(10F)
                .setFont(arabicFont)
                .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setTextAlignment(TextAlignment.RIGHT)));

        // Invoice date
        table.addCell(new Cell().add(new Paragraph("Invoice Date")).setFontSize(8F));
        table.addCell(new Cell().add(new Paragraph(data.header.invoiceDateTime)).setFontSize(8F));
        table.addCell(new Cell().add(new Paragraph(processArabic("تاريخ الفاتورة"))
                .setFontSize(10F)
                .setFont(arabicFont)
                .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setTextAlignment(TextAlignment.RIGHT)));

        // Service start date
        table.addCell(new Cell().add(new Paragraph("Service Start Date")).setFontSize(8F));
        table.addCell(new Cell().add(new Paragraph(convertDate(data.header.serviceStartDate))).setFontSize(8F));
        table.addCell(new Cell().add(new Paragraph(processArabic("تاريخ بدء الخدمة"))
                .setFontSize(10F)
                .setFont(arabicFont)
                .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setTextAlignment(TextAlignment.RIGHT)));

        // Service end date
        table.addCell(new Cell().add(new Paragraph("Service End Date")).setFontSize(8F));
        table.addCell(new Cell().add(new Paragraph(convertDate(data.header.serviceEndDate))).setFontSize(8F));
        table.addCell(new Cell().add(new Paragraph(processArabic("تاريخ إنتهاء الخدمة"))
                .setFontSize(10F)
                .setFont(arabicFont)
                .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setTextAlignment(TextAlignment.RIGHT)));

        return table;
    }

    /**
     * Customer details table.
     */
    public static Table createCustomerDetailsTable(InvoiceData data, PdfFont arabicFont) {
        float[] widths = {50F, 200F, 100F, 50F, 200F, 100F};
        Table table = new Table(widths).setMarginTop(10F);

        // Header row: English
        table.addCell(new Cell(0, 3)
                .add(new Paragraph("Customer Details"))
                .setFontSize(10F)
                .setTextAlignment(TextAlignment.LEFT)
                .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(182, 248, 252)))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorderRight(Border.NO_BORDER));

        // Header row: Arabic
        table.addCell(new Cell(0, 3).add(
                        new Paragraph(processArabic("بيانات العميل"))
                                .setFontSize(10F)
                                .setFont(arabicFont)
                                .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                .setTextAlignment(TextAlignment.RIGHT)
                                .setBorderLeft(Border.NO_BORDER))
                .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(182, 248, 252))));

        // Name row
        table.addCell(new Cell(2, 0).add(new Paragraph("Name")).setFontSize(8F));
        table.addCell(new Cell().add(new Paragraph(data.customer.englishName))
                .setFontSize(7F)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorderBottom(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER));

        table.addCell(new Cell(2, 0).add(new Paragraph(processArabic("الاسم"))
                .setFontSize(8F)
                .setFont(arabicFont)
                .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setTextAlignment(TextAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)));
        // Address row
        table.addCell(new Cell(2, 0).add(new Paragraph("Address")).setFontSize(8F));
        table.addCell(new Cell().add(new Paragraph(data.customer.englishAddress))
                .setFontSize(8F)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorderBottom(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER));
        table.addCell(new Cell(2, 0).add(new Paragraph(processArabic("العنوان"))
                .setFontSize(8F)
                .setFont(arabicFont)
                .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setTextAlignment(TextAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)));

        // Arabic name and address rows (second line)
        table.addCell(new Cell().add(new Paragraph(processArabic(data.customer.arabicName)))
                .setFontSize(7F)
                .setFont(arabicFont)
                .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorderTop(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER));

        table.addCell(new Cell().add(new Paragraph(processArabic(data.customer.arabicAddress))
                        .setFontSize(7F)
                        .setFont(arabicFont)
                        .setBaseDirection(BaseDirection.RIGHT_TO_LEFT))
                .setBorderTop(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER));

        // VAT number row
        table.addCell(new Cell(2, 0).add(new Paragraph("VAT Number")).setFontSize(8F));
        table.addCell(new Cell(2, 0).add(new Paragraph(data.customer.vatNumber))
                .setFontSize(8F)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.CENTER));
        table.addCell(new Cell(2, 0).add(new Paragraph(processArabic("الرقم الضريبي"))
                .setFontSize(8F)
                .setFont(arabicFont)
                .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setTextAlignment(TextAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)));

        // City row
        table.addCell(new Cell(2, 0).add(new Paragraph("City")).setFontSize(8F));
        table.addCell(new Cell().add(new Paragraph(data.customer.englishCity))
                .setFontSize(8F)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.CENTER)
                .setBorderBottom(Border.NO_BORDER));

        table.addCell(new Cell(2, 0).add(new Paragraph(processArabic("المدينة"))
                .setFontSize(8F)
                .setFont(arabicFont)
                .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setTextAlignment(TextAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)));

        table.addCell(new Cell().add(new Paragraph(processArabic(data.customer.arabicCity))
                        .setFontSize(8F)
                        .setFont(arabicFont)
                        .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorderTop(Border.NO_BORDER));

        // VAT group and country rows
        table.addCell(new Cell(2, 0).add(new Paragraph("VAT Group#")).setFontSize(8F));
        table.addCell(new Cell(2, 0).add(new Paragraph(data.customer.vatGroupNumber))
                .setFontSize(8F)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.CENTER));

        table.addCell(new Cell(2, 0).add(new Paragraph(processArabic("الرقم الضريبي للمجموعة"))
                .setFontSize(8F)
                .setFont(arabicFont)
                .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setTextAlignment(TextAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)));

        table.addCell(new Cell(2, 0).add(new Paragraph("Country")).setFontSize(8F));
        table.addCell(new Cell().add(new Paragraph(data.customer.englishCountry))
                .setFontSize(8F)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.CENTER)
                .setBorderBottom(Border.NO_BORDER));

        table.addCell(new Cell(2, 0).add(new Paragraph(processArabic("البلد"))
                .setFontSize(8F)
                .setFont(arabicFont)
                .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setTextAlignment(TextAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)));

        table.addCell(new Cell().add(new Paragraph(processArabic(data.customer.arabicCountry))
                        .setFontSize(8F)
                        .setFont(arabicFont)
                        .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                        .setTextAlignment(TextAlignment.CENTER))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorderTop(Border.NO_BORDER));

        return table;
    }

    /**
     * Line items table.
     */
    public static Table createLineItemsTable(InvoiceData data, PdfFont arabicFont) {
        float[] widths = {75F, 200F, 50F, 50F, 30F, 120F, 30F, 80F, 75F};
        Table table = new Table(widths).setMarginTop(10F);

        // English column headers
        table.addCell(new Cell().add(new Paragraph("Supply Date")).setFontSize(8F)
                .setTextAlignment(TextAlignment.CENTER).setBorderBottom(Border.NO_BORDER)
                .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247))));

        table.addCell(new Cell().add(new Paragraph("Description")).setFontSize(8F)
                .setTextAlignment(TextAlignment.CENTER).setBorderBottom(Border.NO_BORDER)
                .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247))));

        table.addCell(new Cell().add(new Paragraph("Unit Price")).setFontSize(8F)
                .setTextAlignment(TextAlignment.CENTER).setBorderBottom(Border.NO_BORDER)
                .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247))));

        table.addCell(new Cell().add(new Paragraph("QTY")).setFontSize(8F)
                .setTextAlignment(TextAlignment.CENTER).setBorderBottom(Border.NO_BORDER)
                .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247))));

        table.addCell(new Cell().add(new Paragraph("Disc.")).setFontSize(8F)
                .setTextAlignment(TextAlignment.CENTER).setBorderBottom(Border.NO_BORDER)
                .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247))));

        table.addCell(new Cell().add(new Paragraph("Total excluding Tax")).setFontSize(8F)
                .setTextAlignment(TextAlignment.CENTER).setBorderBottom(Border.NO_BORDER)
                .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247))));

        table.addCell(new Cell().add(new Paragraph("Rate")).setFontSize(8F)
                .setTextAlignment(TextAlignment.CENTER).setBorderBottom(Border.NO_BORDER)
                .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247))));

        table.addCell(new Cell().add(new Paragraph("Tax Amount")).setFontSize(8F)
                .setTextAlignment(TextAlignment.CENTER).setBorderBottom(Border.NO_BORDER)
                .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247))));

        table.addCell(new Cell().add(new Paragraph("Total Price")).setFontSize(8F)
                .setTextAlignment(TextAlignment.CENTER).setBorderBottom(Border.NO_BORDER)
                .setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247))));

        // Arabic column headers
        table.addCell(new Cell().add(new Paragraph(processArabic("تاريخ الخدمة"))
                .setFontSize(8F).setFont(arabicFont).setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setTextAlignment(TextAlignment.CENTER)).setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247))));

        table.addCell(new Cell().add(new Paragraph(processArabic("وصف الخدمة"))
                .setFontSize(8F).setFont(arabicFont).setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setTextAlignment(TextAlignment.CENTER)).setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247))));

        table.addCell(new Cell().add(new Paragraph(processArabic("سعر الوحدة"))
                .setFontSize(8F).setFont(arabicFont).setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setTextAlignment(TextAlignment.CENTER)).setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247))));

        table.addCell(new Cell().add(new Paragraph(processArabic("الكمية"))
                .setFontSize(8F).setFont(arabicFont).setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setTextAlignment(TextAlignment.CENTER)).setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247))));

        table.addCell(new Cell().add(new Paragraph(processArabic("الخصم"))
                .setFontSize(8F).setFont(arabicFont).setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setTextAlignment(TextAlignment.CENTER)).setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247))));

        table.addCell(new Cell().add(new Paragraph(processArabic("الإجمالي باستثناء الضريبة"))
                .setFontSize(8F).setFont(arabicFont).setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setTextAlignment(TextAlignment.CENTER)).setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247))));

        table.addCell(new Cell().add(new Paragraph(processArabic("النسبة"))
                .setFontSize(8F).setFont(arabicFont).setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setTextAlignment(TextAlignment.CENTER)).setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247))));

        table.addCell(new Cell().add(new Paragraph(processArabic("مبلغ الضريبة"))
                .setFontSize(8F).setFont(arabicFont).setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setTextAlignment(TextAlignment.CENTER)).setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247))));

        table.addCell(new Cell().add(new Paragraph(processArabic("السعر الكلي"))
                .setFontSize(8F).setFont(arabicFont).setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setTextAlignment(TextAlignment.CENTER)).setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(203, 245, 247))));

        // Rows for each invoice line item
        for (InvoiceLineItem item : data.lineItems) {
            // supply date with rowspan
            table.addCell(new Cell(2, 0)
                    .add(new Paragraph(convertDate(item.supplyDate)))
                    .setFontSize(8F)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));

            // Arabic description
            if (item.arabicDescription != null && !"EMPTY".equalsIgnoreCase(item.arabicDescription)) {
                table.addCell(new Cell()
                        .add(new Paragraph(processArabic(item.arabicDescription))
                                .setFontSize(8F)
                                .setFont(arabicFont)
                                .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                                .setTextAlignment(TextAlignment.CENTER))
                        .setBorderBottom(Border.NO_BORDER));
            } else {
                table.addCell(new Cell().add(new Paragraph("")).setBorderBottom(Border.NO_BORDER));
            }

            // Numeric and rate columns
            table.addCell(new Cell(2, 0).add(new Paragraph(item.unitPrice != null ? item.unitPrice : "")).setFontSize(8F).setVerticalAlignment(VerticalAlignment.MIDDLE));
            table.addCell(new Cell(2, 0).add(new Paragraph(item.quantity != null ? item.quantity : "")).setFontSize(8F).setVerticalAlignment(VerticalAlignment.MIDDLE));
            table.addCell(new Cell(2, 0).add(new Paragraph(item.discount != null ? item.discount : "")).setFontSize(8F).setVerticalAlignment(VerticalAlignment.MIDDLE));
            table.addCell(new Cell(2, 0).add(new Paragraph(item.totalExcludingTax != null ? item.totalExcludingTax : "")).setFontSize(8F).setVerticalAlignment(VerticalAlignment.MIDDLE));
            table.addCell(new Cell(2, 0).add(new Paragraph(item.rate != null ? item.rate : "")).setFontSize(8F).setVerticalAlignment(VerticalAlignment.MIDDLE));
            table.addCell(new Cell(2, 0).add(new Paragraph(item.taxAmount != null ? item.taxAmount : "")).setFontSize(8F).setVerticalAlignment(VerticalAlignment.MIDDLE));
            table.addCell(new Cell(2, 0).add(new Paragraph(item.totalPrice != null ? item.totalPrice : "")).setFontSize(8F).setVerticalAlignment(VerticalAlignment.MIDDLE));

            // English description cell, after numeric columns
            if (item.englishDescription != null && !"EMPTY".equalsIgnoreCase(item.englishDescription)) {
                table.addCell(new Cell().add(new Paragraph(item.englishDescription))
                        .setFontSize(8F)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBorderTop(Border.NO_BORDER));
            } else {
                table.addCell(new Cell().add(new Paragraph("")).setBorderTop(Border.NO_BORDER));
            }
        }
        return table;
    }

    /**
     * Totals table.
     */
    public static Table createTotalsTable(InvoiceData data, PdfFont arabicFont) {
        float[] widths = {200F, 200F, 200F};
        Table table = new Table(widths).setHorizontalAlignment(HorizontalAlignment.CENTER).setMarginTop(10F);

        // Total excluding VAT
        table.addCell(new Cell().add(new Paragraph("Total (Excluding VAT)")).setBold().setFontSize(8F));
        table.addCell(new Cell().add(new Paragraph(data.totals.totalExcludingVat)).setBold().setFontSize(8F));
        table.addCell(new Cell().add(new Paragraph(processArabic("الإجمالي (باستثناء ضريبة القيمة المضافة)"))
                .setBold().setFont(arabicFont).setFontSize(10F)
                .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setTextAlignment(TextAlignment.RIGHT)));

        // Total discount
        table.addCell(new Cell().add(new Paragraph("Total Discount")).setBold().setFontSize(8F));
        table.addCell(new Cell().add(new Paragraph(data.totals.totalDiscount)).setBold().setFontSize(8F));
        table.addCell(new Cell().add(new Paragraph(processArabic("مجموع الخصومات"))
                .setBold().setFontSize(10F).setFont(arabicFont)
                .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setTextAlignment(TextAlignment.RIGHT)));

        // Total VAT
        table.addCell(new Cell().add(new Paragraph("Total VAT")).setBold().setFontSize(8F));
        table.addCell(new Cell().add(new Paragraph(data.totals.totalVat)).setBold().setFontSize(8F));
        table.addCell(new Cell().add(new Paragraph(processArabic("مجموع ضريبة القيمة المضافة"))
                .setBold().setFontSize(10F).setFont(arabicFont)
                .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setTextAlignment(TextAlignment.RIGHT)));

        // Amount includes VAT
        table.addCell(new Cell().add(new Paragraph("Amount includes VAT")).setBold().setFontSize(8F));
        table.addCell(new Cell().add(new Paragraph(data.totals.amountIncludesVat)).setBold().setFontSize(8F));
        table.addCell(new Cell().add(new Paragraph(processArabic("المجموع شامل ضريبة القيمة المضافة"))
                .setBold().setFontSize(10F).setFont(arabicFont)
                .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setTextAlignment(TextAlignment.RIGHT)));

        return table;
    }

}