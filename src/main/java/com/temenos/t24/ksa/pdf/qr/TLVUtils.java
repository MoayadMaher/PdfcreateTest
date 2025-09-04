package com.temenos.t24.ksa.pdf.qr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utility for generating Tag–Length–Value (TLV) data and encoding it in Base64
 * for ZATCA-compliant QR codes.  Tags 1–5 are mandatory for Phase 1; tags 6–9
 * are optional for Phase 2 (invoice hash, signature, public key, stamp signature).
 */
public final class TLVUtils {

    private TLVUtils() { /* prevent instantiation */ }

    /** Encodes a single TLV entry: one-byte tag, one-byte length, then value bytes. */
    private static byte[] encodeEntry(int tag, String value) {
        // Use an empty byte array for null values
        byte[] valueBytes = value != null ? value.getBytes(StandardCharsets.UTF_8) : new byte[0];
        byte[] tlv = new byte[2 + valueBytes.length];
        tlv[0] = (byte) tag;             // tag identifier (1 byte)
        tlv[1] = (byte) valueBytes.length; // length (1 byte)
        System.arraycopy(valueBytes, 0, tlv, 2, valueBytes.length);
        return tlv;
    }

    /**
     * Generates a Base64-encoded TLV string from the provided QR data.
     * Only non-null fields are encoded; Phase 2 fields (6–9) are included if present.
     *
     * @param data a populated ZatcaQRData instance
     * @return a Base64 string representing the TLV byte array
     */
    public static String generateBase64TLV(ZatcaQRData data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Mandatory fields (Phase 1)
        baos.write(encodeEntry(1, data.sellerName));
        baos.write(encodeEntry(2, data.vatNumber));
        baos.write(encodeEntry(3, data.timestamp));
        baos.write(encodeEntry(4, data.invoiceTotalWithVat));
        baos.write(encodeEntry(5, data.vatTotal));
        // Optional Phase 2 fields
//        if (data.invoiceHash   != null) baos.writeBytes(encodeEntry(6, data.invoiceHash));
//        if (data.signature     != null) baos.writeBytes(encodeEntry(7, data.signature));
//        if (data.publicKey     != null) baos.writeBytes(encodeEntry(8, data.publicKey));
//        if (data.stampSignature != null) baos.writeBytes(encodeEntry(9, data.stampSignature));
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}