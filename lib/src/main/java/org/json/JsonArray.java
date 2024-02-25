package org.json;

import java.util.ArrayList;
import java.util.List;

public class JsonArray {
    private List<JsonItem> arrayItems = new ArrayList<JsonItem>();

    public JsonArray() {}

    public int length() {
        return this.arrayItems.size();
    }

    public void add(JsonItem item) {
        this.arrayItems.add(item);
    }

    public void remove(int i) {
        this.arrayItems.remove(i);
    }

    public JsonItem get(int i) {
        return this.arrayItems.get(i);
    }

    public JsonResult getObject(int i) {
        return this.get(i).getObject();
    }

    public int getInt(int i) {
        return this.get(i).getInt();
    }

    public String getString(int i) {
        return this.get(i).getString();
    }

    public double getDouble(int i) {
        return this.get(i).getDouble();
    }

    public JsonArray getArray(int i) {
        return this.get(i).getArray();
    }

    public boolean getBoolean(int i) {
        return this.get(i).getBoolean();
    }

    public boolean isNull(int i) {
        return this.get(i).isNull();
    }

    public void addString(String value) {
        this.arrayItems.add(new JsonItem(value, JsonType.STRING));
    }

    public void addInt(int value) {
        this.arrayItems.add(new JsonItem(value, JsonType.INTEGER));
    }

    public void addDouble(double value) {
        this.arrayItems.add(new JsonItem(value, JsonType.DOUBLE));
    }

    public void addObject(JsonResult value) {
        this.arrayItems.add(new JsonItem(value, JsonType.OBJECT));
    }

    public void addNull() {
        this.arrayItems.add(new JsonItem(null, JsonType.NULL));
    }

    public void addArray(JsonArray value) {
        this.arrayItems.add(new JsonItem(value, JsonType.ARRAY));
    }

    public void addBoolean(boolean value) {
        this.arrayItems.add(new JsonItem(value, JsonType.BOOLEAN));
    }

    public String toString() {
        StringBuilder arrayString = new StringBuilder();
        arrayString.append('[');

        int n_items = this.arrayItems.size();
        int i = 0;
        for (JsonItem item : this.arrayItems) {
            switch (item.getType()) {
                case STRING:
                    arrayString.append('"');
                    arrayString.append(item.getValue());
                    arrayString.append('"');
                    break;
                default:
                    arrayString.append(item.getValue());
                    break;
            }

            i += 1;
            if (i < n_items) {
                arrayString.append(',');
            }
        }

        arrayString.append(']');

        return arrayString.toString();
    }

    public static JsonArray fromString(String arrayString) throws Exception {
        arrayString = arrayString.trim();
        JsonArray jsonArray = new JsonArray();
        
        List<String> arrayItems = JsonArray.splitArrayString(arrayString);
        for (String arrayItem : arrayItems) {
            switch (JsonUtil.getValueType(arrayItem.charAt(0), arrayItem)) {
                case STRING:
                    String content = arrayItem.substring(1, arrayItem.length() - 1);
                    jsonArray.add(new JsonItem(content, JsonType.STRING));
                    break;
                case INTEGER:
                    jsonArray.add(new JsonItem(Integer.parseInt(arrayItem), JsonType.INTEGER));
                    break;
                case DOUBLE:
                    jsonArray.add(new JsonItem(Double.parseDouble(arrayItem), JsonType.DOUBLE));
                    break;
                case NULL:
                    jsonArray.add(new JsonItem(null, JsonType.NULL));
                    break;
                case OBJECT:
                    JsonParser parser = new JsonParser();
                    JsonResult result = parser.parse(arrayItem);
                    jsonArray.add(new JsonItem(result, JsonType.OBJECT));
                    break;
                case ARRAY:
                    JsonArray array = JsonArray.fromString(arrayItem);
                    jsonArray.add(new JsonItem(array, JsonType.ARRAY));
                    break;
                case BOOLEAN:
                    // value is pre-sanitized so it will either be true/false
                    boolean value = true;
                    if (arrayItem.equals("false")) {
                        value = false;
                    }
                    jsonArray.add(new JsonItem(value, JsonType.BOOLEAN));
                case INVALID:
                    break;
            }
        }

        return jsonArray;
    }

    private static List<String> splitArrayString(String arrayString) {
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
                String body = JsonUtil.extractStringBetween(arrayString, i, '{', '}', true);
                splitItems.add(body);
                i += body.length();
                currentItem = new StringBuilder();
                continue;
            }

            if (!insideQuotes && currentChar == '[') {
                String body = JsonUtil.extractStringBetween(arrayString, i, '[', ']', true);
                splitItems.add(body);
                i += body.length();
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
}
