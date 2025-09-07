package com.temenos.t24.ksa.pdf.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;


public final class TextUtil {
    private TextUtil() {}

    /** Shapes and reorders Arabic text for right‑to‑left display. */
    public static String processArabic(String text) {
        try {
            ArabicShaping arabicShaping = new ArabicShaping(ArabicShaping.LETTERS_SHAPE);
            String shaped = arabicShaping.shape(text);
            Bidi bidi = new Bidi(shaped, Bidi.REORDER_DEFAULT);
            return bidi.writeReordered(Bidi.DO_MIRRORING);
        } catch (ArabicShapingException e) {
            // fallback on error
            return text;
        }
    }

    /** Converts a date from yyyyMMdd to dd MMM yyyy (uppercase). */
    public static String convertDate(String dateVal) {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat format2 = new SimpleDateFormat("dd MMM yyyy");
        try {
            Date date = format1.parse(dateVal);
            return format2.format(date).toUpperCase();
        } catch (ParseException e) {
            return dateVal;
        }
    }
}