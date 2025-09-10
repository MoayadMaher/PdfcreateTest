package com.example.ubl;

import java.math.BigDecimal;

/** Simple assertion utilities for tests. */
public final class Assertions {
    private Assertions() {}

    public static void assertEquals(Object expected, Object actual, String msg) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(msg + " expected=" + expected + " actual=" + actual);
        }
    }

    public static void assertEquals(BigDecimal expected, BigDecimal actual, String msg) {
        if (expected == null ? actual != null : expected.compareTo(actual) != 0) {
            throw new AssertionError(msg + " expected=" + expected + " actual=" + actual);
        }
    }

    public static void assertTrue(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }
}
