package org.browsit.conversations.api.util;

import java.util.regex.Pattern;

/**
 * @author Illusion created on 2/9/2023
 */
public final class StringValidator {

    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + '§' + "[0-9A-FK-ORX]");

    public static String clean(String input) {
        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }
}
