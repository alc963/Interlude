package com.naugroup3.interlude.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class UtilsTest {
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void returns_null_for_missing_release_date(String release_date) {
        assertThat(Utils.parse_release_date(release_date)).isNull();
    }

    @Test
    void parses_utc_release_date() {
        assertThat(Utils.parse_release_date("2026-05-02T12:30:00Z"))
            .isEqualTo(LocalDateTime.of(2026, 5, 2, 12, 30));
    }

    @Test
    void keeps_local_time_of_offset_release_date() {
        assertThat(Utils.parse_release_date("2026-05-02T12:30:00+02:00"))
            .isEqualTo(LocalDateTime.of(2026, 5, 2, 12, 30));
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-a-date", "2024-05-01", "2024-05-01T12:30:00"})
    void throws_for_unparseable_release_date(String release_date) {
        assertThatThrownBy(() -> Utils.parse_release_date(release_date))
            .isInstanceOf(DateTimeParseException.class);
    }
}
