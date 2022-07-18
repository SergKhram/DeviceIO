package io.github.sergkhram.ui.generators;

import com.mifmif.common.regex.Generex;

public class Generator {
    public static String generateRandomString(String pattern) {
        return new Generex(pattern).random();
    }

    public static String generateRandomString(Integer length) {
        return generateRandomString(String.format("[A-Z]{1}[a-z]{%s}", length));
    }

    public static String generateRandomString() {
        return generateRandomString(10);
    }
}
