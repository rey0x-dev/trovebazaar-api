package com.trovebazaar.trovebazaarapi.utility;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public final class TextNormalizer {

    private TextNormalizer() {}

    public static String normalizeText(String input) {
        if (input == null) {
            return null;
        }

        if (input.isBlank()) {
            return "";
        }

        String words [] = input.trim().toLowerCase(Locale.ROOT).split("\\s+");

        return Arrays.stream(words)
                .map(word -> word.substring(0, 1).toUpperCase(Locale.ROOT)
                        + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
