package rest.bef.demo.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class StringUtil {

    private static final Map<String, Pattern> PATTERNS = new HashMap<>();
    private static final String ALPHA_NUM_REGEX = "[A-Za-z0-9]*";
    private static final String USERNAME_REGEX = "[A-Za-z0-9_]*";
    private static final String SHARED_KEY_REGEX = "[a-zA-Z0-9\\-\\.]{32,48}";
    private static final String EMAIL_REGEX =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public static Boolean toBoolean(String input) {
        try {
            return Boolean.parseBoolean(input);
        } catch (Exception e) {
            return null;
        }
    }

    public static Long toLong(String input) {
        if (input == null || input.trim().length() == 0)
            return null;

        if (input.length() > 0) {
            try {
                return Long.valueOf(input);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    public static Integer toInt(String input) {
        if (input == null || input.trim().length() == 0)
            return null;

        if (input.length() > 0) {
            try {
                return Integer.valueOf(input);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    public static boolean isNumeric(String input) {
        if (!isValid(input))
            return false;

        for (int i = 0; i < input.length(); i++)
            if (!Character.isDigit(input.charAt(i)))
                return false;

        return true;
    }

    public static boolean isUsername(String username) {
        return matches(USERNAME_REGEX, username);
    }

    public static boolean isValid(String input) {
        return isValid(input, 1);
    }

    public static boolean isValid(String input, int minLength) {
        return input != null && input.trim().length() >= minLength;
    }

    public static boolean isEmail(String input) {
        return matches(EMAIL_REGEX, input);
    }

    public static boolean isSharedKey(String input) {
        return matches(SHARED_KEY_REGEX, input);
    }

    public static boolean isAlphaNum(String input) {
        return matches(ALPHA_NUM_REGEX, input);
    }

    public static boolean matches(String regex, String text) {
        if (!PATTERNS.containsKey(regex))
            PATTERNS.put(regex, Pattern.compile(regex));

        return isValid(text) && PATTERNS.get(regex).matcher(text).matches();
    }

}
