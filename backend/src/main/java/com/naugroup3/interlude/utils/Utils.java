package com.naugroup3.interlude.utils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public final class Utils {
    public static LocalDateTime parse_release_date(String release_date) {
        if (release_date == null || release_date.isBlank()) return null;
        return OffsetDateTime.parse(release_date).toLocalDateTime();
    }
}
