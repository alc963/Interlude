package com.naugroup3.interlude.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ValidatorTest {
    @ParameterizedTest
    @ValueSource(strings = {"smi", "sum-i", "Sumi123", "___", "1234567890123456"})
    void accepts_valid_usernames(String username) {
        assertThat(Validator.validate_username(username)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"sm", "12345678901234567", "sp ace", "bad!char", "user.name", "üser", "abc\n", " abc"})
    void rejects_invalid_usernames(String username) {
        assertThat(Validator.validate_username(username)).isFalse();
    }
}
