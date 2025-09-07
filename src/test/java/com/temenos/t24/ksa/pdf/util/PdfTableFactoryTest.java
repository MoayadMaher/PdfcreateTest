package com.temenos.t24.ksa.pdf.util;

import static org.junit.jupiter.api.Assertions.*;

import com.itextpdf.io.exceptions.IOException;

import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import org.junit.jupiter.api.Test;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.element.Table;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.temenos.t24.ksa.pdf.model.IbanDetails;
import com.temenos.t24.ksa.pdf.model.InvoiceData;

import java.nio.file.Files;
import java.nio.file.Path;

public class PdfTableFactoryTest {

    @Test
    public void createInfoHeaderTableStructure() throws Exception {
        InvoiceData data = new InvoiceData();
        data.iban = new IbanDetails();
        data.iban.ibanNumber = "IBAN123";
        data.iban.taxRegistrationNumber = "TRN";

        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        Table table = PdfTableFactory.createInfoHeaderTable(data, font);
        assertEquals(2, table.getNumberOfColumns());
        assertEquals(4, table.getChildren().size());
    }

    @Test
    public void createHeaderTableInvalidLogoPathThrows() throws Exception {
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        assertThrows(IOException.class,
                () -> PdfTableFactory.createHeaderTable(font, "bad://path"));
    }

    @Test
    void pdfContainsExpectedText() throws Exception {
        // 1) Build minimal data and font
        InvoiceData data = new InvoiceData();
        data.iban = new IbanDetails();
        data.iban.ibanNumber = "IBAN123";
        data.iban.taxRegistrationNumber = "TRN";

        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // 2) Create a temp PDF and WRITE content into it
        Path tmp = Files.createTempFile("inv", ".pdf");
        try (PdfWriter writer = new PdfWriter(tmp.toString());
             PdfDocument pdf = new PdfDocument(writer);
             Document doc = new Document(pdf)) {

            Table infoHeader = PdfTableFactory.createInfoHeaderTable(data, font);
            doc.add(infoHeader);
            // Add a tiny heading so we can assert both English & Arabic tokens
            doc.add(new com.itextpdf.layout.element.Paragraph("Invoice Number"));
        }

        // 3) READ the generated PDF and extract text
        try (PdfDocument rd = new PdfDocument(new PdfReader(tmp.toString()))) {
            String page1 = com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
                    .getTextFromPage(rd.getPage(1)); // default extraction strategy
            assertTrue(page1.contains("Invoice Number"));
            assertTrue(page1.contains("IBAN123")); // from the info header table
        }
    }
}

