package com.trovebazaar.trovebazaarapi.tests.utility;

import com.trovebazaar.trovebazaarapi.utility.TextNormalizer;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class TextNormalizerTest {

    @Test
    void shouldNormalizeTextToTitleCaseAndTrimWhitespace() {
        String text = "   Coacalco de BERRIOZABAL   ";

        String normalizedText = TextNormalizer.normalizeText(text);

        assertEquals("Coacalco De Berriozabal", normalizedText);
    }

    @Test
    void shouldReturnNullWhenTextIsNull() {
        String normalizedText = TextNormalizer.normalizeText(null);

        assertNull(normalizedText);
    }

    @Test
    void shouldReturnEmptyStringWhenTextIsEmpty() {
        String normalizedText = TextNormalizer.normalizeText(" ");
        assertEquals("", normalizedText);
    }

    @Test
    void shouldCollapseRepeatedWhitespaceBetweenWords() {
        String text = "Coacalco          De      Berriozabal";
        String normalizedText = TextNormalizer.normalizeText(text);
        assertEquals("Coacalco De Berriozabal", normalizedText);
    }

    @Test
    void shouldReturnNormalizedSentence(){

        String sentence = "  nEW     nAME ";
        String normalizedSentence = TextNormalizer.normalizedSentence(sentence);

        assertEquals("New name", normalizedSentence);
    }
}
