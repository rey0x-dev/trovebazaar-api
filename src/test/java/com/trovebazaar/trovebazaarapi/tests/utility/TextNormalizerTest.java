package com.trovebazaar.trovebazaarapi.tests.utility;

import com.trovebazaar.trovebazaarapi.utility.TextNormalizer;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class TextNormalizerTest {

    @Test
    public void shouldNormalizeTextToTitleCaseAndTrimWhitespace() {
        String text = "   Coacalco de BERRIOZABAL   ";

        String normalizedText = TextNormalizer.normalizeText(text);

        assertEquals("Coacalco De Berriozabal", normalizedText);
    }

    @Test
    public void shouldReturnNullWhenTextIsNull() {
        String normalizedText = TextNormalizer.normalizeText(null);

        assertNull(normalizedText);
    }

    @Test
    public void shouldReturnEmptyStringWhenTextIsEmpty() {
        String normalizedText = TextNormalizer.normalizeText(" ");
        assertEquals("", normalizedText);
    }

    @Test
    public void shouldCollapseRepeatedWhitespaceBetweenWords() {
        String text = "Coacalco          De      Berriozabal";
        String normalizedText = TextNormalizer.normalizeText(text);
        assertEquals("Coacalco De Berriozabal", normalizedText);
    }
}
