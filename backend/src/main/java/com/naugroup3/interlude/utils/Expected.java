package com.naugroup3.interlude.utils;

import java.util.NoSuchElementException;

public sealed interface Expected<T, E> {
    record Success<T, E>(T value) implements Expected<T, E> {}
    record Failure<T, E>(E error) implements Expected<T, E> {}

    default boolean has_value() {
        return this instanceof Success<T, E>;
    }
    default T value() {
        if (this instanceof Success<T, E> s) return s.value();
        throw new NoSuchElementException("Expected is Failure");
    }
    default E error() {
        if (this instanceof Failure<T, E> f) return f.error();
        throw new NoSuchElementException("Expected is Success");
    }
}