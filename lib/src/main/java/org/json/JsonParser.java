
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

    private void parseStringValue(String key, String value) throws Exception {
        if (value.length() == 0) {
            this.parseResult.addString(key, "");
            return;
        }
        
        if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
            this.parseResult.addString(key, value.substring(1, value.length() - 1));
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
        String innerJson = JsonUtil.extractJson(jsonString, jsonStringIndex);
        if (innerJson == null) {
            throw new Exception("Invalid object value for key '" + key + "'.");
        }
        JsonParser parser = new JsonParser();
        JsonResult result = parser.parse(innerJson);
        this.parseResult.addObject(key, result);
        return innerJson.length();
    }

    private int parseArrayValue(String key, String jsonString, int jsonStringIndex) throws Exception {
        String arrayString = JsonUtil.extractArray(jsonString, jsonStringIndex);
        JsonArray jsonArray = JsonArray.fromString(arrayString);
        this.parseResult.addArray(key, jsonArray);
        return arrayString.length();
    }

    private void parseDoubleValue(String key, String value) throws Exception {
        this.parseResult.addDouble(key, Double.parseDouble(value));
    }

    private void parseIntValue(String key, String value) throws Exception {
        this.parseResult.addInt(key, Integer.parseInt(value));
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
        switch (JsonUtil.getValueType(currentChar, valueString)) {
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

    private void resetKeyValue() {
        this.valueIsString = false;
        this.currentKey = new StringBuilder();
        this.currentValue = new StringBuilder();
    }
}
