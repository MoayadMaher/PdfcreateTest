package com.example.ubl;

/**
 * Simple test runner executed via Maven's test phase.
 */
public class TestRunner {
    public static void main(String[] args) throws Exception {
        TotalsTest.run();
        XmlBuildTest.run();
        QrEncoderTest.run();
        System.out.println("All tests passed");
    }
}
