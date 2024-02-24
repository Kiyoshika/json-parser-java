package org.json;

import java.util.ArrayList;
import java.util.List;

public class JsonArray {
    private List<Object> arrayItems = new ArrayList<Object>();

    public JsonArray() {}

    public int length() {
        return this.arrayItems.size();
    }

    public void add(Object item) {
        this.arrayItems.add(item);
    }

    public void remove(int i) {
        this.arrayItems.remove(i);
    }

    public Object get(int i) {
        return this.arrayItems.get(i);
    }

    public JsonResult getObject(int i) {
        return (JsonResult)this.arrayItems.get(i);
    }

    public int getInt(int i) {
        return (int)this.get(i);
    }

    public String getString(int i) {
        return (String)this.get(i);
    }

    public double getDouble(int i) {
        return (double)this.get(i);
    }

    public JsonArray getArray(int i) {
        return (JsonArray)this.get(i);
    }

    public boolean isNull(int i) {
        return this.get(i) == null;
    }

    public static JsonArray fromString(String arrayString) throws Exception {
        arrayString = arrayString.trim();
        JsonArray jsonArray = new JsonArray();
        
        List<String> arrayItems = JsonArray.splitArrayString(arrayString);
        for (String arrayItem : arrayItems) {
            switch (JsonUtil.getValueType(arrayItem.charAt(0), arrayItem)) {
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
                    JsonArray array = JsonArray.fromString(arrayItem);
                    jsonArray.add(array);
                    break;
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
                i += body.length() - 1;
                currentItem = new StringBuilder();
                continue;
            }

            if (!insideQuotes && currentChar == '[') {
                String body = JsonUtil.extractStringBetween(arrayString, i, '[', ']', true);
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
}
