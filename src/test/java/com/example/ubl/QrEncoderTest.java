package com.example.ubl;

import com.example.ubl.qr.QrTlvEncoder;

public class QrEncoderTest {
    public static void run() {
        String qr = QrTlvEncoder.encodeBase64("ABC", "123456789",
                "2024-01-01T00:00:00Z", "100.00", "15.00");
        Assertions.assertEquals("AQNBQkMCCTEyMzQ1Njc4OQMUMjAyNC0wMS0wMVQwMDowMDowMFoEBjEwMC4wMAUFMTUuMDA=",
                qr, "qr");
    }
}
