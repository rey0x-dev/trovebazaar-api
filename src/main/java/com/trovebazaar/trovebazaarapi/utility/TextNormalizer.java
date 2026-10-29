package com.trovebazaar.trovebazaarapi.utility;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public final class TextNormalizer {

    private TextNormalizer() {}

    public static String normalizeText(String input) {
        if (input == null || input.isBlank()) {
            return input == null ? null : "";
        }

        String words [] = input.trim().toLowerCase(Locale.ROOT).split("\\s+");

        return Arrays.stream(words)
                .map(word -> word.substring(0, 1).toUpperCase(Locale.ROOT)
                        + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    public static String normalizedSentence(String sentence) {

       if (sentence == null || sentence.isBlank()) {
           return sentence == null ? null : "";
       }

       String normalizedSentence = sentence.toLowerCase(Locale.ROOT)
               .trim()
               .replaceAll("\\s+", " ");

       return normalizedSentence.substring(0, 1).toUpperCase(Locale.ROOT) + normalizedSentence.substring(1);
    }

}
