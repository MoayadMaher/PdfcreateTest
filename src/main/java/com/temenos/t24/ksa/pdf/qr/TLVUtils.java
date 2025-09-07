package com.temenos.t24.ksa.pdf.qr;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class TLVUtils {
    private TLVUtils() {}

    /** Encodes one TLV entry: 1-byte tag, 1-byte length, then UTF-8 value bytes. */
    static byte[] encodeEntry(int tag, String value) { // package-private: testable
        if (tag < 1 || tag > 255) {
            throw new IllegalArgumentException("Tag out of range: " + tag);
        }
        byte[] valueBytes = value != null ? value.getBytes(StandardCharsets.UTF_8) : new byte[0];
        if (valueBytes.length > 255) {
            throw new IllegalArgumentException("TLV value exceeds 255 bytes for tag " + tag);
        }
        byte[] tlv = new byte[2 + valueBytes.length];
        tlv[0] = (byte) tag;
        tlv[1] = (byte) valueBytes.length;
        System.arraycopy(valueBytes, 0, tlv, 2, valueBytes.length);
        return tlv;
    }

    /** Returns the raw TLV byte array for ZATCA tags 1–5 (and 6–9 if present). */
    static byte[] generateTLVBytes(ZatcaQRData data) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
        // Phase 1 (mandatory)
        baos.write(encodeEntry(1, data.sellerName), 0, encodeEntry(1, data.sellerName).length); // or do once and reuse
        baos.write(encodeEntry(2, data.vatNumber), 0, encodeEntry(2, data.vatNumber).length);
        baos.write(encodeEntry(3, data.timestamp), 0, encodeEntry(3, data.timestamp).length);
        baos.write(encodeEntry(4, data.invoiceTotalWithVat), 0, encodeEntry(4, data.invoiceTotalWithVat).length);
        baos.write(encodeEntry(5, data.vatTotal), 0, encodeEntry(5, data.vatTotal).length);
        // Phase 2 (optional)
//        if (data.invoiceHash != null)    baos.write(encodeEntry(6, data.invoiceHash), 0, encodeEntry(6, data.invoiceHash).length);
//        if (data.signature != null)      baos.write(encodeEntry(7, data.signature), 0, encodeEntry(7, data.signature).length);
//        if (data.publicKey != null)      baos.write(encodeEntry(8, data.publicKey), 0, encodeEntry(8, data.publicKey).length);
//        if (data.stampSignature != null) baos.write(encodeEntry(9, data.stampSignature), 0, encodeEntry(9, data.stampSignature).length);
        return baos.toByteArray();
    }

    /** Public API kept as-is: Base64 of the TLV bytes. */
    public static String generateBase64TLV(ZatcaQRData data) {
        return Base64.getEncoder().encodeToString(generateTLVBytes(data));
    }
}