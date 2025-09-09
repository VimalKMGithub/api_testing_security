package org.vimal.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtility {
    private DateTimeUtility() {
    }

    private static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    public static String getCurrentFormattedLocalTimeStamp() {
        return getCurrentFormattedLocalTimeStamp(DEFAULT_DATE_TIME_FORMATTER);
    }

    private static String getCurrentFormattedLocalTimeStamp(DateTimeFormatter dateTimeFormatter) {
        return LocalDateTime.now()
                .format(dateTimeFormatter);
    }
}
