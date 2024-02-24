package org.json;

public class JsonUtil {
    public static JsonType getValueType(char currentChar, String value) {

        switch (currentChar) {
            case 'n':
                return JsonType.NULL;
            case '{':
                return JsonType.OBJECT;
            case '[':
                return JsonType.ARRAY;
        }

        if (value.length() == 0) {
            return JsonType.INVALID;
        }

        if (value.charAt(0) == '"') {
            return JsonType.STRING;
        }

        if (value.indexOf('.') != -1) {
            return JsonType.DOUBLE;
        }

        // assumes every other set of characters forms an integer, even if it's not true.
        // parseInt() would throw an exception if remaining characters does not match an int.
        return JsonType.INTEGER;
    }

    public static String extractStringBetween(String jsonString, int jsonStringIndex, char startChar, char endChar, boolean inclusive) {
        int bracketCounter = 1;
        int startIndex = jsonStringIndex;
        for (int i = jsonStringIndex + 1; i < jsonString.length(); i++) {
            if (jsonString.charAt(i) == startChar) {
                bracketCounter += 1;
            } else if (jsonString.charAt(i) == endChar) {
                bracketCounter -= 1;
            }

            if (bracketCounter == 0) {
                if (inclusive) {
                    return jsonString.substring(startIndex, i + 1);
                }

                return jsonString.substring(startIndex + 1, i);
            }
        }

        return null;
    }

    public static String extractJson(String jsonString, int jsonStringIndex) {
        return JsonUtil.extractStringBetween(jsonString, jsonStringIndex, '{', '}', true);
    }

    public static String extractArray(String jsonString, int jsonStringIndex) {
        return JsonUtil.extractStringBetween(jsonString, jsonStringIndex, '[', ']', true);
    }
}
