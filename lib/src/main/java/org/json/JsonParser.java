
package org.json;

import java.util.ArrayList;
import java.util.List;

public class JsonParser {
    private JsonState currentState;
    private JsonState nextState;
    private String validTokens;
    private String terminatingTokens;
    private StringBuilder currentKey;
    private StringBuilder currentValue;
    private boolean valueIsString;
    private JsonResult parseResult = new JsonResult();

    public JsonResult parse(String jsonString) throws Exception {

        this.valueIsString = false;

        this.setState(JsonState.START_BODY, jsonString, 0, '0');
        this.currentKey = new StringBuilder();
        this.currentValue = new StringBuilder();

        for (int i = 0; i < jsonString.length(); i++) {
            char currentChar = jsonString.charAt(i);

            if (this.currentState == JsonState.VALUE_CONTENT && currentChar == '"') {
                this.valueIsString = !this.valueIsString;
            }

            // consume whitespace
            if (this.isWhitespace(currentChar)) {
                if ((this.currentState == JsonState.VALUE_CONTENT && this.currentValue.length() == 0) || // special case when we have a space character before adding any content to the value
                    (this.currentState != JsonState.KEY_CONTENT && this.currentState != JsonState.VALUE_CONTENT)) {
                        continue;
                    }
            }

            if (this.validTokens != null && this.validTokens.indexOf(currentChar) == -1) {
                throw new Exception("Unexpected token '" + currentChar + "'.");
            }

            else if (!this.valueIsString && this.terminatingTokens != null && this.terminatingTokens.indexOf(currentChar) != -1) {
                switch (currentChar) {
                    case '"': {
                        if (this.currentState == JsonState.KEY_CONTENT) {
                            this.nextState = JsonState.KEY_VALUE_SEPARATOR;
                        }
                    }
                    break;

                    case ',': {
                        if (this.nextState == JsonState.VALUE_KEY_SEPARATOR) {
                            this.nextState = JsonState.START_KEY_QUOTE;
                            this.insertValue(jsonString, i, currentChar);
                        }
                    }
                    break;

                    case '}': {
                        if (this.nextState == JsonState.VALUE_KEY_SEPARATOR) {
                            this.nextState = JsonState.END_BODY;
                            this.insertValue(jsonString, i, currentChar);
                        }
                    }
                    break;

                    /*
                     * These characters handle a special case where we need to parse a substring as
                     * a value, such as a nested JSON body, JSON array or specific string (e.g., "null").
                     * We shift the index by how long the string is (and bind it to the max valid length)
                     * after inserting the value into the parseResult and determine the next state based on
                     * if the following character is a comma (which indicates we need to parse another key)
                     * or a close bracket which indicates this is the last value in the JSON.
                     */
                    case '{':
                    case '[':
                    case 'n': {
                        if (this.currentState == JsonState.VALUE_CONTENT) {
                            this.nextState = JsonState.VALUE_KEY_SEPARATOR;
                            i += this.insertValue(jsonString, i, currentChar);
                            if (i >= jsonString.length()) {
                                throw new Exception("Incomplete JSON.");
                            }

                            currentChar = jsonString.charAt(i);
                            while (this.isWhitespace(currentChar) && i < jsonString.length()) {
                                i += 1;
                                currentChar = jsonString.charAt(i);
                            }

                            if (currentChar == ',') {
                                this.nextState = JsonState.START_KEY_QUOTE;
                            } else if (currentChar == '}') {
                                this.nextState = JsonState.END_BODY;
                            }
                        }
                    }
                    break;
                }

                this.setState(this.nextState, jsonString, i, currentChar);
            }

            else {
                switch (this.currentState) {
                    case KEY_CONTENT:
                        this.currentKey.append(currentChar);
                        break;
                    case VALUE_CONTENT:
                        this.currentValue.append(currentChar);
                        break;
                    default:
                        this.setState(this.nextState, jsonString, i, currentChar);
                        break;
                }
            }

        }

        if (this.currentState != JsonState.END_BODY) {
            throw new Exception("No end body found.");
        }

        return this.parseResult;
    }

    private boolean isWhitespace(char currentChar) {
        return currentChar == ' ' || currentChar == '\t' || currentChar == '\n' || currentChar == '\r';
    }

    private void setState(JsonState newState, String jsonString, int jsonStringIndex, char currentChar) throws Exception {
        this.currentState = newState;
        switch (newState) {
            case START_BODY: {
                this.validTokens = "{";
                this.terminatingTokens = null;
                // check for a special case of empty JSON. this is handled here, otherwise
                // trailing delimiters in START_KEY_QUOTE aren't flagged, 
                // e.g., { "key": "trailing", } is valid (when it shouldn't be)
                jsonStringIndex += 1;
                currentChar = jsonString.charAt(jsonStringIndex);
                while (this.isWhitespace(currentChar) && jsonStringIndex < jsonString.length()) {
                    currentChar = jsonString.charAt(jsonStringIndex);
                    jsonStringIndex += 1;
                }
                if (currentChar == '}') {
                    this.nextState = JsonState.END_BODY;
                } else {
                    this.nextState = JsonState.START_KEY_QUOTE;
                }
            }
                break;
            case START_KEY_QUOTE:
                this.validTokens = "\"";
                this.terminatingTokens = null;
                this.nextState = JsonState.KEY_CONTENT;
                break;
            case KEY_CONTENT:
                this.validTokens = null;
                this.terminatingTokens = "\"";
                this.nextState = JsonState.END_KEY_QUOTE;
                break;
            case END_KEY_QUOTE:
                this.validTokens = "\"";
                this.terminatingTokens = null;
                this.nextState = JsonState.KEY_VALUE_SEPARATOR;
                break;
            case KEY_VALUE_SEPARATOR:
                this.validTokens = ":";
                this.terminatingTokens = null;
                this.nextState = JsonState.VALUE_CONTENT;
                break;
            case VALUE_CONTENT:
                this.validTokens = null;
                this.terminatingTokens = ",{}n[";
                this.nextState = JsonState.VALUE_KEY_SEPARATOR;
                break;
            case END_BODY:
                this.validTokens = "}";
                this.terminatingTokens = null;
                this.nextState = JsonState.END_BODY;
                break;
        }
    }

    private JsonType getValueType(char currentChar, String value) {

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

    private void parseStringValue(String key, String value) throws Exception {
        if (value.length() == 0) {
            this.parseResult.add(key, "");
            return;
        }
        
        if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
            this.parseResult.add(key, value.substring(1, value.length() - 1));
            return;
        }

        throw new Exception("Couldn't parse string value.");
    }

    private void parseNullValue(String key, String jsonString, int jsonStringIndex) throws Exception {
        if ((jsonString.length() - jsonStringIndex < 4) ||
            !jsonString.substring(jsonStringIndex, jsonStringIndex + 4).equals("null")) {
                throw new Exception("Couldn't parse null value.");
            }

            this.parseResult.addNull(key);
    }

    private int parseObjectValue(String key, String jsonString, int jsonStringIndex) throws Exception {
        String innerJson = this.extractJson(jsonString, jsonStringIndex);
        if (innerJson == null) {
            throw new Exception("Invalid object value for key '" + key + "'.");
        }
        JsonParser parser = new JsonParser();
        JsonResult result = parser.parse(innerJson);
        this.parseResult.add(key, result);
        return innerJson.length();
    }

    private JsonArray createArrayFromString(String arrayString) throws Exception {
        JsonArray jsonArray = new JsonArray();
        
        List<String> arrayItems = this.splitArrayString(arrayString);
        for (String arrayItem : arrayItems) {
            switch (this.getValueType(arrayItem.charAt(0), arrayItem)) {
                case STRING:
                    String content = arrayItem.substring(1, arrayItem.length() - 1);
                    jsonArray.add(content);
                    break;
                case INTEGER:
                    jsonArray.add(Integer.parseInt(arrayItem));
                    break;
                case DOUBLE:
                    jsonArray.add(Double.parseDouble(arrayItem));
                    break;
                case NULL:
                    jsonArray.add(null);
                    break;
                case OBJECT:
                    JsonParser parser = new JsonParser();
                    JsonResult result = parser.parse(arrayItem);
                    jsonArray.add(result);
                    break;
                case ARRAY:
                    JsonArray array = this.createArrayFromString(arrayItem);
                    jsonArray.add(array);
                    break;
            }
        }

        return jsonArray;
    }

    private int parseArrayValue(String key, String jsonString, int jsonStringIndex) throws Exception {
        String arrayString = this.extractArray(jsonString, jsonStringIndex);
        JsonArray jsonArray = this.createArrayFromString(arrayString);
        this.parseResult.add(key, jsonArray);
        return arrayString.length();
    }

    private void parseDoubleValue(String key, String value) {
        this.parseResult.add(key, Double.parseDouble(value));
    }

    private void parseIntValue(String key, String value) {
        this.parseResult.add(key, Integer.parseInt(value));
    }

    private int insertValue(String jsonString, int jsonStringIndex, char currentChar) throws Exception {
        String keyString = this.currentKey.toString();

        if (keyString.length() == 0) {
            throw new Exception("Key cannot be empty.");
        }

        if (this.parseResult.containsKey(keyString)) {
            throw new Exception("Duplicate key '" + keyString + "'.");
        }

        String valueString = this.currentValue.toString().trim();

        int offset;
        switch (this.getValueType(currentChar, valueString)) {
            case STRING:
                this.parseStringValue(keyString, valueString);
                this.resetKeyValue();
                return 0;

            case NULL:
                this.parseNullValue(keyString, jsonString, jsonStringIndex);
                this.resetKeyValue();
                return 4; /* length of "null" */
            
            case OBJECT:
                offset = this.parseObjectValue(keyString, jsonString, jsonStringIndex);
                this.resetKeyValue();
                return offset;

            case ARRAY:
                offset = this.parseArrayValue(keyString, jsonString, jsonStringIndex);
                this.resetKeyValue();
                return offset;
            
            case DOUBLE:
                this.parseDoubleValue(keyString, valueString);
                this.resetKeyValue();
                return 0;

            case INTEGER:
                this.parseIntValue(keyString, valueString);
                this.resetKeyValue();
                return 0;
            
            case INVALID:
                throw new Exception("Invalid value type found.");
        }

        return 0;
    }

    private String extractStringBetween(String jsonString, int jsonStringIndex, char startChar, char endChar, boolean inclusive) {
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

    private String extractJson(String jsonString, int jsonStringIndex) {
        return this.extractStringBetween(jsonString, jsonStringIndex, '{', '}', true);
    }

    private String extractArray(String jsonString, int jsonStringIndex) {
        return this.extractStringBetween(jsonString, jsonStringIndex, '[', ']', true);
    }

    private List<String> splitArrayString(String arrayString) {
        boolean insideQuotes = false;
        List<String> splitItems = new ArrayList<String>();
        StringBuilder currentItem = new StringBuilder();

        // start at 1 to avoid the opening bracket '[', otherwise we parse an array infinitely until stack overflow
        for (int i = 1; i < arrayString.length() - 1; i++) {
            char currentChar = arrayString.charAt(i);

            if (currentChar == '"') {
                insideQuotes = !insideQuotes;
            }

            if (!insideQuotes && currentChar == ' ') {
                continue;
            }

            if (!insideQuotes && currentChar == '{') {
                String body = this.extractStringBetween(arrayString, i, '{', '}', true);
                splitItems.add(body);
                i += body.length() - 1;
                currentItem = new StringBuilder();
                continue;
            }

            if (!insideQuotes && currentChar == '[') {
                String body = this.extractStringBetween(arrayString, i, '[', ']', true);
                splitItems.add(body);
                i += body.length() - 1;
                currentItem = new StringBuilder();
                continue;
            }

            if (!insideQuotes && currentChar == ',' && currentItem.length() > 0) {
                splitItems.add(currentItem.toString());
                currentItem = new StringBuilder();
            } else {
                currentItem.append(currentChar);
            }
        }

        if (currentItem.length() > 0) {
            splitItems.add(currentItem.toString());
        }

        return splitItems;
    }

    private void resetKeyValue() {
        this.valueIsString = false;
        this.currentKey = new StringBuilder();
        this.currentValue = new StringBuilder();
    }
}
