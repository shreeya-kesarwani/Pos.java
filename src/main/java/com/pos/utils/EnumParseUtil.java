package com.pos.utils;

import java.util.Optional;

public final class EnumParseUtil {

    private EnumParseUtil() {}

    public static <E extends Enum<E>> Optional<E> parseEnum(Class<E> enumClass, String value) {
        if (value == null) return Optional.empty();

        String v = value.trim();
        if (v.isEmpty()) return Optional.empty();

        try {
            return Optional.of(Enum.valueOf(enumClass, v.toUpperCase()));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
