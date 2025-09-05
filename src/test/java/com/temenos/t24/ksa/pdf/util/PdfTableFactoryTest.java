package com.temenos.t24.ksa.pdf.util;

import static org.junit.jupiter.api.Assertions.*;

import com.itextpdf.io.exceptions.IOException;

import org.junit.jupiter.api.Test;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.element.Table;
import com.temenos.t24.ksa.pdf.model.IbanDetails;
import com.temenos.t24.ksa.pdf.model.InvoiceData;

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
}

