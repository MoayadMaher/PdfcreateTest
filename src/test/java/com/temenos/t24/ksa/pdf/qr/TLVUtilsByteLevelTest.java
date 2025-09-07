package com.temenos.t24.ksa.pdf.qr;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;

class TLVUtilsByteLevelTest {

    @Test
    void encodesTagLenValue_correctly_withMultiByteUTF8() {
        ZatcaQRData d = new ZatcaQRData();
        d.sellerName = "ABĆ"; // Ć = 2 bytes in UTF-8
        d.vatNumber = "123";
        d.timestamp = "2024-01-01T00:00:00Z";
        d.invoiceTotalWithVat = "10.00";
        d.vatTotal = "1.50";

        byte[] tlv = TLVUtils.generateTLVBytes(d); // or: Base64.getDecoder().decode(TLVUtils.generateBase64TLV(d))

        int i = 0;
        // Tag 1
        assertEquals(1, tlv[i++] & 0xFF);
        int len1 = tlv[i++] & 0xFF;
        assertEquals(4, len1, "UTF-8 length of 'ABĆ' should be 4");
        String v1 = new String(tlv, i, len1, StandardCharsets.UTF_8);
        assertEquals("ABĆ", v1);
        i += len1;

        // Tag 2
        assertEquals(2, tlv[i++] & 0xFF);
        int len2 = tlv[i++] & 0xFF;
        assertEquals(3, len2);
        assertEquals("123", new String(tlv, i, len2, StandardCharsets.UTF_8));
        i += len2;

        // Tag 3
        assertEquals(3, tlv[i++] & 0xFF);
        int len3 = tlv[i++] & 0xFF;
        assertEquals("2024-01-01T00:00:00Z".length(), len3);
        i += len3;

        // Tag 4
        assertEquals(4, tlv[i++] & 0xFF);
        int len4 = tlv[i++] & 0xFF;
        assertEquals(5, len4);
        assertEquals("10.00", new String(tlv, i, len4, StandardCharsets.UTF_8));
        i += len4;

        // Tag 5
        assertEquals(5, tlv[i++] & 0xFF);
        int len5 = tlv[i++] & 0xFF;
        assertEquals(4, len5);
        assertEquals("1.50", new String(tlv, i, len5, StandardCharsets.UTF_8));
        i += len5;

        // no extra bytes expected
        assertEquals(i, tlv.length);
    }

    @Test
    void throwsOnValueOver255Bytes() {
        ZatcaQRData d = new ZatcaQRData();
        d.sellerName = new String(new char[256]).replace('\0', 'a'); // 256 ASCII 'a' -> 256 bytes
        d.vatNumber = "1"; d.timestamp = "2024-01-01T00:00:00Z"; d.invoiceTotalWithVat = "1"; d.vatTotal = "1";
        assertThrows(IllegalArgumentException.class, () -> TLVUtils.generateBase64TLV(d));
    }

    @Test
    void allowsExactly255Bytes() {
        ZatcaQRData d = new ZatcaQRData();
        d.sellerName = new String(new char[255]).replace('\0', 'a'); // 255 bytes
        d.vatNumber = "1"; d.timestamp = "2024-01-01T00:00:00Z"; d.invoiceTotalWithVat = "1"; d.vatTotal = "1";
        assertDoesNotThrow(() -> TLVUtils.generateBase64TLV(d));
    }

    @Test
    void nullFieldsEncodeAsZeroLength() {
        ZatcaQRData d = new ZatcaQRData();
        d.sellerName = null; // length 0
        d.vatNumber = "123";
        d.timestamp = "2024-01-01T00:00:00Z";
        d.invoiceTotalWithVat = "100";
        d.vatTotal = "15";

        byte[] tlv = TLVUtils.generateTLVBytes(d);
        assertEquals(1, tlv[0] & 0xFF);
        assertEquals(0, tlv[1] & 0xFF); // zero-length value for tag 1
    }
}