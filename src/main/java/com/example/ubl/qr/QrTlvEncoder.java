package com.example.ubl.qr;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Encode QR fields using ZATCA TLV format.
 * Each value is encoded as: tag (1 byte) + length (1 byte) + UTF-8 bytes.
 */
public final class QrTlvEncoder {
    private QrTlvEncoder() {}

    public static String encodeBase64(String sellerName, String sellerVat,
                                      String isoTimestamp, String totalInclVat,
                                      String vatTotal) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(out, 1, sellerName);
        write(out, 2, sellerVat);
        write(out, 3, isoTimestamp);
        write(out, 4, totalInclVat);
        write(out, 5, vatTotal);
        return Base64.getEncoder().encodeToString(out.toByteArray());
    }

    private static void write(ByteArrayOutputStream out, int tag, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        out.write(tag);
        out.write(bytes.length);
        out.write(bytes, 0, bytes.length);
    }
}
