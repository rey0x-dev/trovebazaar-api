package com.trovebazaar.trovebazaarapi.service;

import java.security.SecureRandom;

public class ProductCodeGenerator {


    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final int CODE_LENGTH = 6;

    private static final SecureRandom RANDOM = new SecureRandom();

    private ProductCodeGenerator() {
    }

    public static String generateCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i<CODE_LENGTH; i++) {

            int index = RANDOM.nextInt(CHARACTERS.length());

            code.append(CHARACTERS.charAt(index));
        }

        return code.toString();
    }
}
