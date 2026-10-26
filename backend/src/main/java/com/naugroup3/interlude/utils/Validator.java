package com.naugroup3.interlude.utils;

import java.util.regex.Pattern;

public final class Validator {
    public static final String USERNAME_REGEX = "^[a-zA-Z0-9_-]{3,16}$";
    private static final Pattern USERNAME_PATTERN = Pattern.compile(USERNAME_REGEX);

    private Validator() {}

    public static boolean validate_username(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }
}
