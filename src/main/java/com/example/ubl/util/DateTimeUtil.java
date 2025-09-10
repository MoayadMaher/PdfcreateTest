package com.example.ubl.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/** Date time utilities. */
public final class DateTimeUtil {
    private DateTimeUtil() {}

    /**
     * Convert to ISO8601 string with offset.
     */
    public static String toIso8601(LocalDate date, LocalTime time, ZoneId zone) {
        return ZonedDateTime.of(date, time, zone).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
