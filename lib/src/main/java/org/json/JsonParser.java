
package org.json;

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
                if (this.currentValue.length() == 0 || // special case when we have a space character before adding any content to the value
                    (this.currentState != JsonState.KEY_CONTENT &&
                    this.currentState != JsonState.VALUE_CONTENT)) {
                        continue;
                    }
            }

            if (this.validTokens != null && this.validTokens.indexOf(currentChar) == -1) {
                throw new Exception("Unexpected token '" + currentChar + "'.");
            }

            if (!this.valueIsString && this.terminatingTokens != null && this.terminatingTokens.indexOf(currentChar) != -1) {
                i += this.setState(this.nextState, jsonString, i, currentChar);
            }

            switch (this.currentState) {
                case KEY_CONTENT:
                    this.currentKey.append(currentChar);
                    break;
                case VALUE_CONTENT:
                    this.currentValue.append(currentChar);
                    break;
                default:
                    i += this.setState(this.nextState, jsonString, i, currentChar);
                    break;
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

    private int setState(JsonState newState, String jsonString, int jsonStringIndex, char currentChar) throws Exception {
        int indexOffset = 0;
        this.currentState = newState;
        switch (newState) {
            case START_BODY:
                this.validTokens = "{";
                this.terminatingTokens = null;
                this.nextState = JsonState.START_KEY_QUOTE;
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
                this.terminatingTokens = ",{}n";
                this.nextState = JsonState.VALUE_KEY_SEPARATOR;
                break;
            case VALUE_KEY_SEPARATOR: {
                indexOffset = this.insertValue(jsonString, jsonStringIndex, currentChar);
                currentChar = jsonString.charAt(jsonStringIndex + indexOffset);
                while (this.isWhitespace(currentChar)) {
                    indexOffset += 1;
                    currentChar = jsonString.charAt(jsonStringIndex + indexOffset);
                }
                this.validTokens = ",}";
                this.terminatingTokens = null;
                if (currentChar == ',') {
                    this.nextState = JsonState.START_KEY_QUOTE;
                } else if (currentChar == '}') {
                    this.nextState = JsonState.END_BODY;
                }
                break;
            }
        }

        return indexOffset;
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
            throw new Exception("Invalid JSON value for key '" + key + "'.");
        }
        JsonParser parser = new JsonParser();
        JsonResult result = parser.parse(innerJson);
        this.parseResult.add(key, result);
        return innerJson.length();
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
                int offset = this.parseObjectValue(keyString, jsonString, jsonStringIndex);
                this.resetKeyValue();
                return offset;

            case ARRAY:
                /* TODO: */
                return 0;
            
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

    private String extractJson(String jsonString, int jsonStringIndex) {
        int bracketCounter = 1;
        int startIndex = jsonStringIndex;
        for (int i = jsonStringIndex + 1; i < jsonString.length(); i++) {
            if (jsonString.charAt(i) == '{') {
                bracketCounter += 1;
            } else if (jsonString.charAt(i) == '}') {
                bracketCounter -= 1;
            }

            if (bracketCounter == 0) {
                return jsonString.substring(startIndex, i + 1);
            }
        }

        return null;
    }

    private void resetKeyValue() {
        this.valueIsString = false;
        this.currentKey = new StringBuilder();
        this.currentValue = new StringBuilder();
    }
}
