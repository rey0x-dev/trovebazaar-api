package com.trovebazaar.trovebazaarapi.tests.service;

import com.trovebazaar.trovebazaarapi.service.ProductCodeGenerator;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ProductCodeGeneratorTest {

    @Test
    void shouldGenerateSixCharacterQrCode() {

        String code = ProductCodeGenerator.generateCode();
        assertNotNull(code, "Generated code should not be null");
        assertEquals(6, code.length(), "Code length must be exactly 6 characters");
    }

    @Test
    void testAllowedCharactersAndNumbers() {

        String code = ProductCodeGenerator.generateCode();
        assertEquals(6, code.length());
        assertTrue(code.matches("^[A-Z0-9]{6}$"));
    }

    @Test
    void shouldGenerateDifferentCodes(){

        String code = ProductCodeGenerator.generateCode();
        String code2 = ProductCodeGenerator.generateCode();
        assertNotEquals(code, code2);
    }

    @RepeatedTest(50)
    void testMultipleTimesInLoop(){

        String code = ProductCodeGenerator.generateCode();
        assertEquals(6, code.length());
        assertTrue(code.matches("^[A-Z0-9]{6}$"));
    }

    @Test
    void shouldGenerateUniqueCode(){

        int batchSize = 1000;
        Set<String> uniqueCodes = new HashSet<>();

        for (int i = 0; i < batchSize; i++) {
            uniqueCodes.add(ProductCodeGenerator.generateCode());
        }

        assertTrue(uniqueCodes.size() > batchSize*.99, "Too many collisions detected in random code generation.");

    }
}
