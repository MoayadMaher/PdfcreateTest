package com.temenos.t24.ksa.pdf.qr;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TLVUtilsTest {

    @Test
    public void generatesExpectedBase64() throws Exception {
        ZatcaQRData data = new ZatcaQRData();
        data.sellerName = "ABC";
        data.vatNumber = "12345";
        data.timestamp = "2024-01-01T00:00:00Z";
        data.invoiceTotalWithVat = "100";
        data.vatTotal = "15";

        String result = TLVUtils.generateBase64TLV(data);
        assertEquals("AQNBQkMCBTEyMzQ1AxQyMDI0LTAxLTAxVDAwOjAwOjAwWgQDMTAwBQIxNQ==", result);
    }

    @Test
    public void handlesNullValues() throws Exception {
        ZatcaQRData data = new ZatcaQRData();
        data.sellerName = null;
        data.vatNumber = "12345";
        data.timestamp = "2024-01-01T00:00:00Z";
        data.invoiceTotalWithVat = "100";
        data.vatTotal = "15";

        String result = TLVUtils.generateBase64TLV(data);
        assertEquals("AQACBTEyMzQ1AxQyMDI0LTAxLTAxVDAwOjAwOjAwWgQDMTAwBQIxNQ==", result);
    }
}

